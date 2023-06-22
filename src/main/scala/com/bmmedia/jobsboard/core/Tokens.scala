package com.bmmedia.jobsboard.core

import doobie.util.transactor
import doobie.util.transactor.Transactor
import com.bmmedia.jobsboard.config.TokenConfig
import cats.effect.*
import cats.implicits.*
import org.typelevel.log4cats.Logger

trait Tokens[F[_]] {
  def generate: F[String]
  def getToken(email: String): F[Option[String]]
  def removeToken(email: String): F[Unit]
  def validateToken(email: String, token: String): F[Boolean]
}

class LiveToken[F[_]: MonadCancelThrow: Logger](users: Users[F])(
    transactor: Transactor[F],
    tokenConfig: TokenConfig
) extends Tokens[F] {
  override def generate: F[String]                                     = ???
  override def getToken(email: String): F[Option[String]]              = ???
  override def removeToken(email: String): F[Unit]                     = ???
  override def validateToken(email: String, token: String): F[Boolean] = ???
}

object LiveToken {
  def apply[F[_]: MonadCancelThrow: Logger](
      users: Users[F]
  )(transactor: Transactor[F], tokenConfig: TokenConfig): F[Tokens[F]] =
    new LiveToken[F](users)(transactor, tokenConfig).pure[F]
}
