package com.bmmedia.jobsboard.core

import com.bmmedia.fixtures.JobFixture
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import cats.effect.IO
import com.bmmedia.jobsboard.domain.user.User
import com.bmmedia.jobsboard.domain.user.Role
import cats.implicits.*
import doobie.implicits.*

class UserSpec
    extends AsyncFreeSpec
    with DoobieSpec
    with Matchers
    with JobFixture
    with AsyncIOSpec {
  val initScript: String = "sql/jobs.sql"

  "User's algebra" - {
    "should return a user if the given email does exist" in {
      transactor.use { xa =>
        val program = for {
          test <- sql"""
          SELECT company FROM users WHERE email = 'john.doe@example.com'
          """
            .query[String]
            .unique
            .transact(xa)
          _         <- IO.println(test)
          users     <- LiveUsers[IO](xa)
          retrieved <- users.find("john.doe@example.com")
        } yield retrieved

        program.asserting {
          case None => fail("User not found")
          case Some(retrieved) =>
            retrieved shouldBe User(
              "john.doe@example.com",
              "John",
              "Doe",
              "password123",
              Role.ADMIN,
              "BM Media".some
            )

        }
      }
    }

  }
}
