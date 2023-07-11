package com.bmmedia.jobsboard.domain

object auth {
  final case class UserRegister(
      email: String,
      firstName: String,
      lastName: String,
      password: String,
      company: Option[String],
      url: Option[String]
  )

  final case class Credentials(email: String, password: String)
  final case class PasswordChange(oldPassword: String, newPassword: String)
}
