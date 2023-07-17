package com.bmmedia.jobsboard.pages.auth

import tyrian.*
import tyrian.Html.*
import cats.effect.IO
import com.bmmedia.jobsboard.*
import com.bmmedia.jobsboard.pages.Page

final case class ForgotPasswordPage() extends Page:

  override def initCmd: Cmd[IO, App.Msg] = Cmd.None

  override def update(msg: App.Msg): (Page, Cmd[IO, App.Msg]) = (this, Cmd.None)

  override def view(): Html[App.Msg] =
    div(
      h1("Forgot password Page")
    )
