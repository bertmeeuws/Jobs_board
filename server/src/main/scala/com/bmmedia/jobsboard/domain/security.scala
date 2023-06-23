package com.bmmedia.jobsboard.domain

import cats.data.{Kleisli, OptionT}
import cats.implicits._
import tsec.authorization.{AuthGroup, AuthorizationInfo, BasicRBAC}
import tsec.authentication.{SecuredRequest, TSecAuthService, TSecMiddleware}
import com.bmmedia.jobsboard.domain.user.*
import org.http4s.dsl.impl.Auth
import tsec.authentication.TSecBearerToken
import tsec.common.SecureRandomId
import cats.effect.*
import tsec.mac.jca.HMACSHA256
import tsec.authentication.AugmentedJWT
import tsec.authentication.JWTAuthenticator
import org.http4s.Response

object security {

  type Crypto              = HMACSHA256
  type JwtToken            = AugmentedJWT[Crypto, String]
  type Authenticator[F[_]] = JWTAuthenticator[F, String, User, Crypto]
  type AuthRoute[F[_]] =
    PartialFunction[SecuredRequest[F, User, JwtToken], F[Response[F]]]
}
