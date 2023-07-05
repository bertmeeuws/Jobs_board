package com.bmmedia.jobsboard.domain

import tsec.passwordhashers._
import tsec.passwordhashers.jca._
import tsec.passwordhashers.PasswordHash.PHash

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
}
