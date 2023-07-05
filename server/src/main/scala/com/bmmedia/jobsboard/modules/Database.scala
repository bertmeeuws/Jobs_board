package com.bmmedia.jobsboard.modules

import cats.effect.*
import cats.implicits.*
import doobie.util.*
import cats.instances.*
import doobie.hikari.HikariTransactor
import com.bmmedia.jobsboard.config.PostgresConfig

object Database {
  def makePostgresResource[F[_]: Async](config: PostgresConfig): Resource[F, HikariTransactor[F]] =
    for {
      ec <- ExecutionContexts.fixedThreadPool(config.threads)
      xa <- HikariTransactor.newHikariTransactor[F](
        config.driver,
        config.url,
        config.username,
        config.password,
        ec
      )
    } yield xa
}
