package com.bmmedia.jobsboard.core

import com.bmmedia.fixtures.JobFixture
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import cats.effect.IO

class JobSpec
    extends AsyncFreeSpec
    with DoobieSpec("sql/jobs.sql")
    with Matchers
    with JobFixture
    with AsyncIOSpec {

  "Job's algebra" - {
    "should return no job if the given UUID does not exist" in {
      transactor.use { xa =>
        val program = for {
          jobs      <- LiveJobs[IO](xa)
          retrieved <- jobs.find(NotFoundJobUuid)
        } yield retrieved

        program.asserting { retrieved =>
          retrieved shouldBe None
        }
      }
    }

    "should return a job if the given UUID does" in {
      transactor.use { xa =>
        val program = for {
          jobs      <- LiveJobs[IO](xa)
          retrieved <- jobs.find(NotFoundJobUuid)
        } yield retrieved

        program.asserting { retrieved =>
          retrieved.nonEmpty shouldBe true
        }
      }
    }
  }
}
