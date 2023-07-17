package com.bmmedia.common

object Constants {
  val emailRegex = """^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$"""

  object Endpoints {
    val root = "http://localhost:8080"

    val signUp = "/api/auth/register"
    val signIn = "/api/auth/login"

  }
}
