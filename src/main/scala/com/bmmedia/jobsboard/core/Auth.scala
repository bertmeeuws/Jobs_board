package com.bmmedia.jobsboard.core

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

trait Auth[F[_]] {
  def login(credentials: Credentials): F[Option[String]]
  def logout(token: String): F[Unit]
  def verifyToken(password: Password, hash: HashedPassword): F[Boolean]
  def createJWT(user: User): F[String]
  def register(user: UserRegister): F[Option[String]]
}

class LiveAuth[F[_]: Sync] private (usersRepository: Users[F]) extends Auth[F] {
  override def login(email: String, password: Password): F[Option[String]] = ???
  override def logout(token: String): F[Unit]                              = ???
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
    for {
      token <- BCrypt.hashpw[F](registerData.password)
      user <- usersRepository.create(
        user.User(
          registerData.email,
          registerData.firstName,
          registerData.lastName,
          registerData.password,
          Role.User,
          None,
          None
        )
      )
    } yield Some(token)
  }
}

object LiveAuth {
  def apply[F[_]: Sync](usersRepository: Users[F]) =
    new LiveAuth[F](usersRepository).pure[F]
}
