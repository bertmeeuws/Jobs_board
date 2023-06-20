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
import tsec.passwordhashers._
import tsec.passwordhashers.jca._
import concurrent.duration.DurationInt
import org.typelevel.log4cats.Logger
import cats.effect.kernel.Async
import tsec.mac.jca.HMACSHA256
import tsec.jwt.JWTClaims
import tsec.jws.mac.JWTMac

trait Auth[F[_]] {
  def login(credentials: Credentials): F[Option[String]]
  def logout(token: String): F[Unit]
  def verifyToken(password: Password, hash: HashedPassword): F[Boolean]
  def createJWT(user: User): F[String]
  def register(registerData: UserRegister): F[Option[String]]
}

class LiveAuth[F[_]: Sync: Logger] private (usersRepository: Users[F]) extends Auth[F] {

  override def login(credentials: Credentials): F[Option[String]] = for {
    _    <- Logger[F].info(s"Logging in user $credentials")
    user <- usersRepository.find(credentials.email).attempt
    _    <- Logger[F].info(s"User $user")
  } yield "dasokodasd".some

  override def logout(token: String): F[Unit] = ???
  override def verifyToken(password: Password, token: HashedPassword): F[Boolean] = for {
    token <- token.pure[F]
    check <- BCrypt.checkpwBool[F]("hiThere", token)
  } yield check

  override def createJWT(user: User): F[String] = {
    for {
      key <- HMACSHA256.generateKey[F]
      claims <- JWTClaims.withDuration[F](
        expiration = Some(10.minutes),
        customFields = Seq("email" -> encodeString(user.email))
      )
      jwt <- JWTMac
        .build[F, HMACSHA256](claims, key)
      stringjwt <- JWTMac.buildToString[F, HMACSHA256](claims, key)
    } yield stringjwt
  }

  override def register(registerData: UserRegister): F[Option[String]] = {

    for {
      hAttempt <- BCrypt
        .hashpw[F](registerData.password)
        .attempt
      _ <- println(s"Hashed password $hAttempt").pure[F]
      _ <- Logger[F]
        .info(s"Hashed password $hAttempt")
      hashedPassword <- Sync[F].fromEither(hAttempt)
      _              <- Logger[F].info(s"Hashed password $hashedPassword")
    } yield hashedPassword

    BCrypt.hashpw[F](registerData.password) flatMap { hashedPassword =>
      {
        Logger[F].info(hashedPassword) *> usersRepository
          .find(registerData.email)
          .flatMap {
            case Some(existingUser) => println("User does exist").pure[F] *> None.pure[F]
            case None => {
              Logger[F].info("User does not exist") *>
                usersRepository
                  .create(
                    User(
                      registerData.email,
                      registerData.firstName,
                      registerData.lastName,
                      hashedPassword,
                      Role.RECRUITER,
                      None
                    )
                  )
                  .flatMap { user =>
                    createJWT(user).flatMap { jwt =>
                      jwt.some.pure[F]
                    }
                  }
            }
          }
      }
    }
  }
}

object LiveAuth {
  def apply[F[_]: Sync: Logger](usersRepository: Users[F]) =
    new LiveAuth[F](usersRepository).pure[F]
}
