package com.bmmedia.jobsboard.http.routes

import io.circe.generic.auto.*
import org.http4s.circe.CirceEntityCodec.*

import cats.Monad
import com.bmmedia.jobsboard.validation.syntax.HttpValidationDsl
import cats.implicits._
import cats.effect.*
import org.typelevel.log4cats.Logger
import com.bmmedia.jobsboard.core.Users
import com.bmmedia.jobsboard.domain.user.User
import org.http4s.HttpRoutes
import com.bmmedia.jobsboard.validation.syntax.*

class UserRoutes[F[_]: Concurrent: Logger] private (userRepository: Users[F])
    extends HttpValidationDsl[F] {
  import com.bmmedia.jobsboard.logging.syntax.*

  // Create user
  private val createUser: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ GET -> Root / "create" => {
      req.validate[User] { user =>
        for {
          _      <- Logger[F].info(s"Creating user $user")
          result <- userRepository.create(user)
          sanitizedUser = result.copy(password = "")
          resp <- Created(result)
        } yield resp
      }
    }

  }
}
