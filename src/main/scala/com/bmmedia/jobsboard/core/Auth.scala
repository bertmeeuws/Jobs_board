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
import tsec.authentication.IdentityStore
import cats.data.OptionT
import tsec.authentication.BackingStore
import tsec.common.SecureRandomId
import cats.effect.kernel.Ref
import tsec.authentication.JWTAuthenticator

trait Auth[F[_]] {
  def login(credentials: Credentials): F[Option[JwtToken]]
  def logout(token: String): F[Unit]
  def verifyToken(password: Password, hash: HashedPassword): F[Boolean]
  def register(registerData: UserRegister): F[Option[JwtToken]]
  def changePassword(email: String, credentials: PasswordChange): F[Boolean]
  def authenticator: Authenticator[F]
}

class LiveAuth[F[_]: Sync: Logger] private (
    usersRepository: Users[F],
    override val authenticator: Authenticator[F]
) extends Auth[F] {

  override def login(credentials: Credentials): F[Option[JwtToken]] = for {
    _    <- Logger[F].info(s"Logging in user")
    user <- usersRepository.find(credentials.email)
    response <- user match {
      case None => {
        Logger[F].info("User not found") *> none[JwtToken].pure[F]
      }
      case Some(user) => {
        Logger[F].info(s"User found: $user") *>
          verifyToken(credentials.password, PasswordHash(user.password)).flatMap { verified =>
            if (verified) {
              Logger[F].info("Password verified") *>
                authenticator.create(user.email).map(Some(_))
            } else {
              Logger[F].info("Password not verified") *> none[JwtToken].pure[F]
            }
          }
      }
    }
  } yield response

  override def logout(token: String): F[Unit] = ???
  override def verifyToken(password: Password, token: PasswordHash[BCrypt]): F[Boolean] = for {
    token   <- token.pure[F]
    isValid <- BCrypt.checkpwBool[F](password, token)
  } yield isValid

  /*
  override def createJWT(user: User): F[JwtToken] = {
    for {
      key <- HMACSHA256.generateKey[F]
      claims <- JWTClaims.withDuration[F](
        expiration = Some(10.minutes),
        customFields =
          Seq("email" -> encodeString(user.email), "role" -> encodeString(user.role.toString))
      )
      jwt <- JWTMac
        .build[F, HMACSHA256](claims, key)

    } yield jwt
  }
   */

  override def register(registerData: UserRegister): F[Option[JwtToken]] = {

    for {
      _              <- Logger[F].info(s"User data: $registerData")
      hashedPassword <- BCrypt.hashpw[F](registerData.password)
      existingUser   <- usersRepository.find(registerData.email)
      response <- existingUser match {
        case Some(user) => Logger[F].info(s"User already exists: $user") *> none[JwtToken].pure[F]
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
                Logger[F].info(s"User created: $user") *>
                  authenticator.create(user.email).map(Some(_))
              }
        }
      }
    } yield {
      response
    }
  }

  override def changePassword(email: String, credentials: PasswordChange): F[Boolean] = {
    for {
      user <- usersRepository.find(email)
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

}

object LiveAuth {
  def apply[F[_]: Sync: Logger](usersRepository: Users[F]) = {
    // 1. Identity store
    val idStore: IdentityStore[F, String, User] = (email: String) =>
      OptionT(usersRepository.find(email))

    // 2. Backing store for JWT tokens
    // Todo
    val tokenStore = Ref.of[F, Map[SecureRandomId, JwtToken]](Map.empty).map { ref =>
      new BackingStore[F, SecureRandomId, JwtToken] {
        // Use a ref
        override def get(id: SecureRandomId): OptionT[F, JwtToken] = OptionT(ref.get.map(_.get(id)))
        override def put(elem: JwtToken): F[JwtToken] =
          ref.update(_.updated(elem.id, elem)).as(elem)
        override def delete(id: SecureRandomId): F[Unit] = ref.update(_ - id)
        override def update(v: JwtToken): F[JwtToken]    = ref.update(_.updated(v.id, v)).as(v)
      }
    }

    // 3. Hashing key
    val keyF = HMACSHA256.buildKey[F]("secret".getBytes("UTF-8"))

    for {
      key        <- keyF
      tokenStore <- tokenStore
      authenticator = JWTAuthenticator.backed.inBearerToken(
        expiryDuration = 10.day,
        maxIdle = None,
        tokenStore = tokenStore,
        identityStore = idStore,
        signingKey = key
      )
    } yield new LiveAuth[F](usersRepository, authenticator)
  }
}
