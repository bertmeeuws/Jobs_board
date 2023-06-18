package com.bmmedia.jobsboard.core

import io.circe.Encoder.encodeString

import com.bmmedia.jobsboard.domain.user.*
import cats.implicits.*
import cats.*
import cats.effect.MonadCancelThrow
import tsec.passwordhashers._
import tsec.passwordhashers.jca._
import cats.effect.IO
import cats.effect.kernel.Sync
import com.bmmedia.jobsboard.domain.auth.*
import com.bmmedia.jobsboard.domain.user
import tsec.jws.mac.JWTMac
import tsec.mac.jca.HMACSHA256
import tsec.jwt.JWTClaims
import concurrent.duration.DurationInt
import org.typelevel.log4cats.Logger
import org.checkerframework.checker.units.qual.s

trait Auth[F[_]] {
  def login(credentials: Credentials): F[Option[String]]
  def logout(token: String): F[Unit]
  def verifyToken(password: Password, hash: HashedPassword): F[Boolean]
  def createJWT(user: User): F[String]
  def register(user: UserRegister): F[Option[String]]
}

class LiveAuth[F[_]: Sync: Logger] private (usersRepository: Users[F]) extends Auth[F] {
  import com.bmmedia.jobsboard.logging.syntax.*

  override def login(credentials: Credentials): F[Option[String]] = ???
  override def logout(token: String): F[Unit]                     = ???
  override def verifyToken(password: Password, token: HashedPassword): F[Boolean] = for {
    token <- token.pure[F]
    check <- BCrypt.checkpwBool[F]("hiThere", token)
  } yield check

  override def createJWT(user: User): F[String] = {
    for {
      token <- BCrypt.hashpw[F](user.password)
    } yield token
  }

  override def register(registerData: UserRegister): F[Option[String]] = {
    BCrypt.hashpw[F](registerData.password).flatMap { hashedPassword =>
      println(s"hashedPassword: $hashedPassword")
      usersRepository
        .create(
          User(
            registerData.email,
            registerData.firstName,
            registerData.lastName,
            hashedPassword,
            Role.User,
            None,
            None
          )
        )
        .flatMap { user =>
          for {
            _   <- Logger[F].info(s"Creating user $user")
            key <- HMACSHA256.generateKey[F]
            _ <- Logger[F].info(
              s"Creating JWT for user ${user.email} with key ${key}"
            )
            claims <- JWTClaims.withDuration[F](
              expiration = Some(10.minutes),
              customFields = Seq("email" -> encodeString(user.email))
            )
            jwt <- JWTMac
              .build[F, HMACSHA256](claims, key) // You can sign and build a jwt object directly
            stringjwt <- JWTMac.buildToString[F, HMACSHA256](claims, key)
          } yield Some(stringjwt)
        }
    }
  }
}

object LiveAuth {
  def apply[F[_]: Sync: Logger](usersRepository: Users[F]) =
    new LiveAuth[F](usersRepository).pure[F]
}
