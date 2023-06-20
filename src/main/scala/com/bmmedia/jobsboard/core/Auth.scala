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
import com.bmmedia.jobsboard.domain.security.*

trait Auth[F[_]] {
  def login(credentials: Credentials): F[Option[String]]
  def logout(token: String): F[Unit]
  def verifyToken(password: Password, hash: HashedPassword): F[Boolean]
  def createJWT(user: User): F[String]
  def register(registerData: UserRegister): F[Option[String]]
  def changePassword(email: String, credentials: PasswordChange): F[Boolean]
  val authenticator: Authenticator[F]

}

class LiveAuth[F[_]: Sync: Logger] private (
    usersRepository: Users[F],
    authenticator: Authenticator[F]
) extends Auth[F] {

  override def login(credentials: Credentials): F[Option[String]] = for {
    _    <- Logger[F].info(s"Logging in user")
    user <- usersRepository.find(credentials.email)
    response <- user match {
      case Some(user) => {
        Logger[F].info(s"User found: $user") *>
          verifyToken(credentials.password, PasswordHash(user.password)).flatMap { verified =>
            if (verified) {
              Logger[F].info("Password verified") *>
                createJWT(user).flatMap { jwt =>
                  jwt.some.pure[F]
                }
            } else {
              Logger[F].info("Password not verified") *> none[String].pure[F]
            }
          }
      }
      case None => {
        Logger[F].info("User not found") *> none[String].pure[F]
      }
    }
  } yield response

  override def logout(token: String): F[Unit] = ???
  override def verifyToken(password: Password, token: PasswordHash[BCrypt]): F[Boolean] = for {
    token   <- token.pure[F]
    isValid <- BCrypt.checkpwBool[F](password, token)
  } yield isValid

  override def createJWT(user: User): F[String] = {
    for {
      key <- HMACSHA256.generateKey[F]
      claims <- JWTClaims.withDuration[F](
        expiration = Some(10.minutes),
        customFields =
          Seq("email" -> encodeString(user.email), "role" -> encodeString(user.role.toString))
      )
      jwt <- JWTMac
        .build[F, HMACSHA256](claims, key)
      stringjwt <- JWTMac.buildToString[F, HMACSHA256](claims, key)
    } yield stringjwt
  }

  override def register(registerData: UserRegister): F[Option[String]] = {

    for {
      _              <- Logger[F].info(s"User data: $registerData")
      hashedPassword <- BCrypt.hashpw[F](registerData.password)
      existingUser   <- usersRepository.find(registerData.email)
      response <- existingUser match {
        case Some(user) => Logger[F].info(s"User already exists: $user") *> none[String].pure[F]
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
                  registerData.company
                )
              )
              .flatMap { user =>
                createJWT(user).flatMap { jwt =>
                  jwt.some.pure[F]
                }
              }
        }
      }
    } yield {
      response
    }
  }

  override def changePassword(email: String, credentials: PasswordChange): F[Boolean] = {
    for {
      user <- usersRepository.find(credentials.email)
      response <- user match {
        case Some(user) => {
          Logger[F].info(s"User found: $user") *>
            verifyToken(credentials.oldPassword, PasswordHash(user.password)).flatMap { verified =>
              if (verified) {
                Logger[F].info("Password verified") *>
                  BCrypt.hashpw[F](credentials.newPassword).flatMap { hashedPassword =>
                    usersRepository.update(user.copy(password = hashedPassword)).as(true)
                  }
              } else {
                Logger[F].info("Password not verified") *> false.pure[F]
              }
            }
        }
        case None => {
          Logger[F].info("User not found") *> false.pure[F]
        }
      }
    } yield response
  }

  override val authenticator: Authenticator[F] = {
    JWTAuthenticator(
      expiryDuration = 10.minutes,
      maxIdle = None,
      tokenStore = authenticator.tokenStore,
      identityStore = authenticator.identityStore,
      signingKey = HMACSHA256.generateKey[F]
    )
  }
}

object LiveAuth {
  def apply[F[_]: Sync: Logger](usersRepository: Users[F], authenticator: Authenticator[F]) =
    new LiveAuth[F](usersRepository).pure[F]
}
