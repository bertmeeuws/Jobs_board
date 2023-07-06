package com.bmmedia.jobsboard.pages

import tyrian.*
import cats.effect.IO
import com.bmmedia.jobsboard.pages.jobs.*
import com.bmmedia.jobsboard.pages.error.*
import com.bmmedia.jobsboard.pages.auth.*

object Page {
  trait Msg

  object Urls {
    val LOGIN    = "/login"
    val REGISTER = "/signup"
    val FORGOT   = "/forgotpassword"
    val RESET    = "/resetpassword"
    val JOBS     = "/jobs"
    val EMPTY    = ""
    val HOME     = "/"
  }

  import Urls.*
  def get(location: String) = location match {
    case `LOGIN`                   => LoginPage()
    case `REGISTER`                => SignUpPage()
    case `FORGOT`                  => ForgotPasswordPage()
    case `RESET`                   => ForgotPasswordPage()
    case `JOBS` | `HOME` | `EMPTY` => JobListPage()
    case s"/jobs/${id}"            => JobDetailPage(id)
    case _                         => NotFoundPage()

  }
}

abstract class Page {
  import Page.*

  def initCmd: Cmd[IO, Msg]

  def update(msg: Msg): (Page, Cmd[IO, Msg])

  def view(): Html[Page.Msg]
}
