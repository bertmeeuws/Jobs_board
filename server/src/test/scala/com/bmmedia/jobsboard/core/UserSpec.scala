package com.bmmedia.jobsboard.core

import com.bmmedia.fixtures.JobFixture
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import cats.effect.IO
import com.bmmedia.jobsboard.domain.user.*
import cats.implicits.*
import doobie.implicits.*
import com.bmmedia.fixtures.UserFixture

class UserSpec
    extends AsyncFreeSpec
    with DoobieSpec("sql/users.sql")
    with Matchers
    with UserFixture
    with AsyncIOSpec {

  "User's algebra" - {
    "should return a user if the given email does exist" in {
      transactor.use { xa =>
        val program = for {
          users     <- LiveUsers[IO](xa)
          retrieved <- users.find(email)
        } yield retrieved

        program.asserting {
          case None => fail("User not found")
          case Some(retrieved) =>
            retrieved shouldBe user
        }
      }
    }

    "should not return a user if the given email does not exist" in {
      transactor.use { xa =>
        val program = for {
          users     <- LiveUsers[IO](xa)
          retrieved <- users.find(email2)
        } yield retrieved

        program.asserting {
          case None => succeed
          case Some(_) =>
            fail("User found")
        }
      }
    }

    "should update the user and return the updated user" in {
      transactor.use { xa =>
        val program = for {
          users     <- LiveUsers[IO](xa)
          retrieved <- users.find(email)
          updated   <- users.update(user.copy(firstName = "David"))
        } yield updated

        program.asserting {
          case None => fail("User not found")
          case Some(retrieved) =>
            retrieved shouldBe user.copy(firstName = "David")
        }
      }
    }

    "should return none because the user does not exist" in {
      transactor.use { xa =>
        val program = for {
          users   <- LiveUsers[IO](xa)
          updated <- users.update(user2.copy(firstName = "David"))
          _       <- IO(println(updated))
        } yield updated

        program.asserting {
          case None => succeed
          case Some(_) =>
            fail("User found")
        }
      }
    }
  }
}
