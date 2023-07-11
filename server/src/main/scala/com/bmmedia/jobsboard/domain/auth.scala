package com.bmmedia.jobsboard.domain

import tsec.passwordhashers._
import tsec.passwordhashers.jca._
import tsec.passwordhashers.PasswordHash.PHash
import doobie.util.meta.Meta

object auth {
  type HashedPassword = PHash[BCrypt]
  type Password       = String

  val jwtSecret = "secret"

  final case class UserRegister(
      email: String,
      firstName: String,
      lastName: String,
      password: String,
      company: Option[String],
      url: Option[String]
  )

  enum Role {
    case ADMIN, RECRUITER
  }

  object Role {
    given metaRole: Meta[Role] = Meta[String].timap[Role](Role.valueOf(_))(_.toString)
  }

  final case class Credentials(email: String, password: String)
  final case class PasswordChange(oldPassword: String, newPassword: String)
}
