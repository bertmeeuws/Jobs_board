package com.bmmedia.http.routes

import com.bmmedia.fixtures.JobFixture
import org.scalatest.freespec.AsyncFreeSpec
import org.http4s.dsl.Http4sDsl
import cats.effect.testing.scalatest.{AsyncIOSpec, EffectTestSupport}
import cats.implicits.*
import cats.effect.*
import org.scalatest.matchers.should.Matchers
import com.bmmedia.jobsboard.core.*
import com.bmmedia.jobsboard.domain.job.*
import java.util.UUID

class JobRoutesSpec
    extends AsyncFreeSpec
    with AsyncIOSpec
    with Matchers
    with Http4sDsl[IO]
    with JobFixture {

  // Preperation
  val jobs: Jobs[IO] = new Jobs[IO] {
    override def create(ownerEmail: String, jobInfo: JobInfo): IO[UUID] = IO.pure(NewJobUuid)

    override def update(id: UUID, jobInfo: JobInfo): IO[Option[Job]] = IO.pure(AwesomeJob.some)
    override def find(id: UUID): IO[Option[Job]]                     = IO.pure(AwesomeJob.some)

    override def delete(id: UUID): IO[Int] = IO.pure(1)

    override def findAll(): IO[List[Job]] = IO.pure(List(AwesomeJob))
  }
}
