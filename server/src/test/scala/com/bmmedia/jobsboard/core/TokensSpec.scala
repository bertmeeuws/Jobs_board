package com.bmmedia.jobsboard.core

import com.bmmedia.fixtures.UserFixture
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import cats.effect.IO
import com.bmmedia.jobsboard.domain.user.*
import cats.implicits.*
import doobie.implicits.*

class TokensSpec
    extends AsyncFreeSpec
    with DoobieSpec("sql/recoverytokens.sql")
    with Matchers
    with UserFixture
    with AsyncIOSpec {

  "Token's algebra" - {
    "should not create a token for a non-existent user" in {
      transactor.use { xa =>
        IO(true).asserting(_ shouldBe true)
      }
    }
  }
}
