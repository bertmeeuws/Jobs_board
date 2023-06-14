package com.bmmedia.jobsboard.modules

import cats.effect.*
import cats.implicits.*
import cats.instances.*
import com.bmmedia.jobsboard.core.*
import doobie.util.transactor.Transactor

final class Core[F[_]] private (val jobs: Jobs[F], val users: Users[F])

object Core {
  def apply[F[_]: Async](xa: Transactor[F]) = {
    val coreF = for {
      jobs  <- LiveJobs[F](xa)
      users <- LiveUsers[F](xa)
    } yield new Core[F](jobs, users)

    Resource.eval(coreF)
  }
}
