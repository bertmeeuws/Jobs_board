package com.bmmedia.jobsboard.modules

import com.bmmedia.jobsboard.core.Jobs
import cats.effect.MonadCancelThrow
import cats.effect.kernel.Resource
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import cats.effect.*
import cats.implicits.*
import doobie.util.*
import cats.instances.*
import com.bmmedia.jobsboard.core.*

final class Core[F[_]] private (val jobs: Jobs[F])

def postgresResource[F[_]: Async]: Resource[F, HikariTransactor[F]] =
  for {
    ec <- ExecutionContexts.fixedThreadPool(32)
    xa <- HikariTransactor.newHikariTransactor[F](
      "org.postgresql.Driver",
      "jdbc:postgresql://localhost:5438/board",
      "docker",
      "docker",
      ec
    )
  } yield xa

// Postgres -> jobs -> core -> httpApi -> app
object Core {
  def apply[F[_]: Async] =
    postgresResource[F].evalMap { postgres =>
      LiveJobs[F](postgres).map(jobs => new Core[F](jobs))
    }
}
