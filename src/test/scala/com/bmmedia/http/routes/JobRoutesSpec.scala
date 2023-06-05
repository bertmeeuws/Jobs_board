package com.bmmedia.http.routes

import io.circe.generic.auto.*
import org.http4s.circe.CirceEntityCodec.*
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
import com.bmmedia.jobsboard.http.routes.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.http4s.HttpRoutes
import org.http4s.Request
import org.http4s.implicits.*

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

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  val jobRoutes: HttpRoutes[IO] = JobRoutes[IO](jobs).routes

  "JobRoutes" - {
    "should return a job with a given id" in {
      for {
        response <- jobRoutes.orNotFound.run(
          Request(GET, uri"/jobs/843df718-ec6e-4d49-9289-f799c0f40064")
        )
        retrieved <- response.as[Job]
      } yield {
        response.status shouldBe Ok
        retrieved shouldBe AwesomeJob
      }
    }
  }

}
