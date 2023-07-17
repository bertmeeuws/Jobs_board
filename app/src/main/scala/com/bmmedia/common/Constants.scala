package com.bmmedia.common

object Constants {
  val emailRegex = """^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$"""

  object Endpoints {
    val root = "http://localhost:8020"

    val signUp = "/api/auth/register"
    val signIn = "/api/auth/login"

  }

  object cookies {
    val duration = 10 * 24 * 3600 * 1000

  }
}
