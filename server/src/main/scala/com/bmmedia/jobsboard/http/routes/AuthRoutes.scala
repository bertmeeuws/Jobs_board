package com.bmmedia.jobsboard.http.routes

import io.circe.generic.auto.*
import org.http4s.circe.CirceEntityCodec.*

import org.typelevel.log4cats.Logger
import com.bmmedia.jobsboard.core.Users
import com.bmmedia.jobsboard.validation.syntax.HttpValidationDsl
import cats.implicits._
import cats.effect.*
import org.http4s.*
import com.bmmedia.jobsboard.domain.user.*
import com.bmmedia.jobsboard.domain.security.*
import com.bmmedia.jobsboard.domain.auth.{Credentials, PasswordChange, UserRegister}
import com.bmmedia.jobsboard.core.Auth
import org.http4s.server.Router
import tsec.authentication._
import tsec.common.SecureRandomId
import cats.MonadThrow

class AuthRoutes[F[_]: Concurrent: Logger] private (
    auth: Auth[F],
    userRepository: Users[F]
) extends HttpValidationDsl[F] {
  import tsec.authentication._
  import com.bmmedia.jobsboard.logging.syntax.*

  private val authenticator = auth.authenticator

  private val securedHandler: SecuredRequestHandler[F, String, User, JwtToken] =
    SecuredRequestHandler(authenticator)

  private val login: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "login" => {
      req.validate[Credentials] { user =>
        val maybeJwtToken = for {
          maybeToken <- auth.login(user)
          _          <- Logger[F].info(s"Logging in user: $maybeToken")
        } yield maybeToken

        maybeJwtToken.map {
          case Some(token) => authenticator.embed(Response(Status.Ok), token)
          case None        => Response(Status.Unauthorized)
        }
      }
    }
  }

  private val register: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "register" => {
      req.validate[UserRegister] { userData =>
        val maybeJwtToken = for {
          _          <- Logger[F].info(s"Registering new user")
          maybeToken <- auth.register(userData)

        } yield maybeToken

        maybeJwtToken.map {
          case Some(token) => authenticator.embed(Response(Status.Ok), token)
          case None        => Response(Status.Unauthorized)
        }
      }
    }
  }

  private val changePassword: AuthRoute[F] = {
    case req @ PUT -> Root / "change-password" asAuthed user => {
      for {
        passwordData <- req.request.as[PasswordChange]
        result       <- auth.changePassword(user.email, passwordData)
        _            <- Logger[F].info(s"Changing password for user: $user")
        response <- result match {
          case true  => Logger[F].info("Password changed") *> Ok()
          case false => Logger[F].info("Password not changed") *> Forbidden()
        }
      } yield response
    }
  }

  private val logoutRoute: AuthRoute[F] = {
    case req @ POST -> Root / "logout" asAuthed _ => {
      val token = req.authenticator
      for {
        _        <- Logger[F].info(s"Logging out user: $token")
        _        <- authenticator.discard(token)
        response <- Ok()
      } yield response
    }
  }

  val authedRoutes =
    securedHandler.liftService(
      TSecAuthService(changePassword.orElse(logoutRoute)) <+> TSecAuthService(logoutRoute)
    )
  val unauthedRoutes = (register <+> login)

  val routes: HttpRoutes[F] = Router(
    "/auth" -> (unauthedRoutes <+> authedRoutes)
  )
}

object AuthRoutes {
  def apply[F[_]: Concurrent: Logger](
      auth: Auth[F],
      userRepository: Users[F]
  ): AuthRoutes[F] =
    new AuthRoutes[F](auth, userRepository)
}
