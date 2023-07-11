package com.bmmedia.jobsboard.domain

import doobie.util.meta.Meta
import org.checkerframework.checker.units.qual.C
import com.bmmedia.jobsboard.domain.auth.Role

object user {
  final case class User(
      email: String,
      firstName: String,
      lastName: String,
      password: String,
      role: Role,
      company: Option[String]
  )

}
