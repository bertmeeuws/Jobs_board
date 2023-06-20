package com.bmmedia.jobsboard.domain

import doobie.util.meta.Meta
import org.checkerframework.checker.units.qual.C

object user {
  final case class User(
      email: String,
      firstName: String,
      lastName: String,
      password: String,
      role: Role,
      company: Option[String]
  )

  enum Role {
    case ADMIN, RECRUITER
  }

  object Role {
    given metaRole: Meta[Role] = Meta[String].timap[Role](Role.valueOf(_))(_.toString)
  }

  final case class Credentials(email: String, password: String)
}
