package com.bmmedia.http.routes

import tsec.authentication.Authenticator
import tsec.mac.jca.HMACSHA256
import com.bmmedia.fixtures.UsersFixture
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import cats.effect.IO
import com.bmmedia.jobsboard.domain.user.User
import com.bmmedia.jobsboard.domain.user.Role
import cats.implicits.*
import doobie.implicits.*

/*
class AuthRoutesSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers with JobFixture {

  val mockedAuthenticator: Authenticator[IO] = {
    val key                                      = HMACSHA256.unsafeGenerateKey
    val idStore: IdentityStore[IO, String, User] = (email: String) => usersRepository.find(email)

  }

}
 */
