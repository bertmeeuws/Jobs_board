package com.bmmedia.jobsboard.modules

import cats.effect.*
import cats.implicits.*
import cats.instances.*
import com.bmmedia.jobsboard.core.*
import doobie.util.transactor.Transactor

final class Core[F[_]] private (val jobs: Jobs[F], val users: Users[F])

object Core {
  def apply[F[_]: Async](xa: Transactor[F]) =
    Resource
      .eval(
        (LiveJobs[F](xa), LiveUsers[F](xa))
      )
      .map { case (jobs: Jobs[F], users: Users[F]) =>
        new Core[F](jobs, users)
      }
}
