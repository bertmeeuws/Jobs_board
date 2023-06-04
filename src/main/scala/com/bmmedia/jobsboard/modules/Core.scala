package com.bmmedia.jobsboard.modules

import cats.effect.*
import cats.implicits.*
import cats.instances.*
import com.bmmedia.jobsboard.core.*
import doobie.util.transactor.Transactor

final class Core[F[_]] private (val jobs: Jobs[F])

object Core {
  def apply[F[_]: Async](xa: Transactor[F]) =
    Resource.eval(LiveJobs[F](xa)).map(jobs => new Core[F](jobs))
}
