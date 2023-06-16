package com.bmmedia.jobsboard.domain

import tsec.passwordhashers._
import tsec.passwordhashers.jca._
import tsec.passwordhashers.PasswordHash.PHash

object auth {
  type HashedPassword = PHash[BCrypt]
  type Password       = String

}
