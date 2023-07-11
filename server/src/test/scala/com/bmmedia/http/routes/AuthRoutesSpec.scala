package com.bmmedia.http.routes

import tsec.authentication.Authenticator
import tsec.mac.jca.HMACSHA256
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import cats.effect.IO
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
