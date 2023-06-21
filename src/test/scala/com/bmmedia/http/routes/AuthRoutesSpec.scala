package com.bmmedia.http.routes

import tsec.authentication.Authenticator
import tsec.mac.jca.HMACSHA256

class AuthRoutesSpec
    extends AsyncFreeSpec
    with AsyncIOSpec
    with Matchers
    with Http4sDsl[IO]
    with JobFixture {

  val mockedAuthenticator: Authenticator[IO] = {
    val key                                      = HMACSHA256.unsafeGenerateKey
    val idStore: IdentityStore[IO, String, User] = (email: String) => usersRepository.find(email)

  }

}
