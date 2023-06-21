package com.bmmedia.jobsboard.modules

import cats.effect.*
import cats.implicits.*
import cats.instances.*
import com.bmmedia.jobsboard.core.*
import doobie.util.transactor.Transactor
import org.typelevel.log4cats.Logger
import com.bmmedia.jobsboard.domain.security
import com.bmmedia.jobsboard.config.SecurityConfig

final class Core[F[_]: Logger] private (val jobs: Jobs[F], val users: Users[F], val auth: Auth[F])

object Core {
  def apply[F[_]: Async: Logger](xa: Transactor[F], securityConfig: SecurityConfig) = {
    val coreF = for {
      jobs  <- LiveJobs[F](xa)
      users <- LiveUsers[F](xa)
      auth  <- LiveAuth[F](users, securityConfig)
    } yield new Core[F](jobs, users, auth)

    Resource.eval(coreF)
  }
}
