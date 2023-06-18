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

class AuthRoutes[F[_]: Concurrent: Logger] private (
    auth: Auth[F],
    userRepository: Users[F]
) extends HttpValidationDsl[F] {

  import com.bmmedia.jobsboard.logging.syntax.*

  private val login: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "login" => {
      req.validate[Credentials] { user =>
        for {
          _      <- Logger[F].info(s"Logging in user $user")
          result <- auth.login(user)
          resp   <- Ok(result)
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
          _ <- result match {
            case Some(_) => Logger[F].info(s"Registered new user")
            case None    => Logger[F].info(s"Failed to register new user")
          }
          _    <- Logger[F].info(s"Registered new user")
          resp <- Ok(result)
        } yield resp
      }
    }
  }

  val routes: HttpRoutes[F] = Router(
    "/auth" -> (register <+> login)
  )
}

object AuthRoutes {
  def apply[F[_]: Concurrent: Logger](
      auth: Auth[F],
      userRepository: Users[F]
  ): AuthRoutes[F] =
    new AuthRoutes[F](auth, userRepository)
}
