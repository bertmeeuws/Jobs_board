package com.bmmedia.jobsboard.http.routes

import io.circe.generic.auto.*
import org.http4s.circe.CirceEntityCodec.*

import org.typelevel.log4cats.Logger
import com.bmmedia.jobsboard.core.Users
import com.bmmedia.jobsboard.validation.syntax.HttpValidationDsl
import cats.implicits._
import cats.effect.*
import org.http4s.*
import com.bmmedia.jobsboard.domain.user.Credentials
import com.bmmedia.jobsboard.core.Auth
import com.bmmedia.jobsboard.domain.auth.UserRegister
import org.http4s.server.Router
import tsec.authentication._
import tsec.common.SecureRandomId
import com.bmmedia.jobsboard.domain.user.User
import com.bmmedia.jobsboard.domain.security.*

class AuthRoutes[F[_]: Concurrent: Logger] private (
    auth: Auth[F],
    userRepository: Users[F]
) extends HttpValidationDsl[F] {
  import tsec.authentication._
  import com.bmmedia.jobsboard.logging.syntax.*

  private val authenticator = auth.authenticator
  private val securedHandler: SecuredRequestHandler[F, String, User, JwtToken] =
    SecuredRequestHandler(auth.authenticator)

  private val login: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "login" => {
      req.validate[Credentials] { user =>
        for {
          result <- auth.login(user)
          resp <- result match {
            case Some(token) => authenticator.embed(Response(Status.Ok), token)
            case None        => BadRequest(Map("error" -> "Invalid credentials"))
          }
        } yield resp
      }
    }
  }

  private val register: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "register" => {
      req.validate[UserRegister] { userData =>
        for {
          _      <- Logger[F].info(s"Registering new user")
          result <- auth.register(userData)
          resp <- result match {
            case Some(token) => Ok(Map("token" -> token))
            case None        => BadRequest(Map("error" -> "Invalid credentials"))
          }
        } yield resp
      }
    }
  }

  private val changePassword: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ PUT -> Root / "change-password" asAuthed email => {
      req.validate[Credentials] { user =>
        for {
          result <- auth.changePassword(user)
          resp <- result match {
            case Some(token) => Ok(Map("token" -> token))
            case None        => BadRequest(Map("error" -> "Invalid credentials"))
          }
        } yield resp
      }
    }
  }

  val authedRoutes: AuthRoutes[F]   = securedHandler.liftService(TSecAuthService(changePassword))
  val unauthedRoutes: HttpRoutes[F] = (register <+> login)

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
