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

trait Auth[F[_]] {
  def login(email: String, password: Password): F[Option[String]]
  def logout(token: String): F[Unit]
  def verifyToken(password: Password, hash: HashedPassword): F[Boolean]
  def createJWT(user: User): F[String]
}

class LiveAuth[F[_]: Sync] private (usersRepository: Users[F]) extends Auth[F] {
  override def login(email: String, password: Password): F[Option[String]] = ???
  override def logout(token: String): F[Unit]                              = ???
  override def verifyToken(password: Password, token: HashedPassword): F[Boolean] = for {
    token <- token.pure[F]
    check <- BCrypt.checkpwBool[F]("hiThere", token)
  } yield check

  override def createJWT(user: User): F[String] = ???
}

object LiveAuth {
  def apply[F[_]: Sync](usersRepository: Users[F]) =
    new LiveAuth[F](usersRepository).pure[F]
}
