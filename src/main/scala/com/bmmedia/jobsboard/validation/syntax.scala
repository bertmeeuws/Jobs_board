package com.bmmedia.jobsboard.validation

import io.circe.generic.auto.*
import org.http4s.circe.CirceEntityCodec.*

import org.http4s.Request
import cats.*
import cats.implicits.*
import cats.data.*
import com.bmmedia.jobsboard.domain.job.*

import com.bmmedia.jobsboard.validation.Validation.*
import org.http4s.*
import org.http4s.implicits.*

import cats.data.Validated.*

import org.typelevel.log4cats.Logger
import com.bmmedia.jobsboard.logging.syntax.*
import com.bmmedia.jobsboard.http.responses.*
import org.http4s.dsl.impl.Responses.ForbiddenOps
import org.http4s.dsl.Http4sDsl

object syntax {

  def validateEntity[A](entity: A)(using validator: Validator[A]): ValidationResult[A] =
    validator.validate(entity)

  trait HttpValidationDsl[F[_]: MonadThrow: Logger] extends Http4sDsl[F] {

    extension (req: Request[F])
      def validate[A: Validator](
          serverLogicIfValid: A => F[Response[F]]
      )(using EntityDecoder[F, A]): F[Response[F]] =
        req
          .as[A]
          .logError(e => s"Parsing payload failed $e")
          .map(validateEntity)
          .flatMap {
            case Valid(entity) =>
              serverLogicIfValid(entity) // F[Response[F]]
            case Invalid(errors) =>
              BadRequest(FailureResponse(errors.toList.map(_.errorMessage).mkString(", ")))
          }
  }
}
