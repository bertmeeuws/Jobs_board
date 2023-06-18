package com.bmmedia.jobsboard.domain

object user {
  final case class User(
      email: String,
      firstName: String,
      lastName: String,
      password: String,
      role: Role,
      company: Option[String],
      url: Option[String]
  )

  enum Role {
    case Admin
    case User
  }

  final case class Credentials(email: String, password: String)
}
