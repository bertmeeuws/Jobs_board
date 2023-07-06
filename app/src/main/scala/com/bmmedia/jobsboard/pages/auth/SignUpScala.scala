package com.bmmedia.jobsboard.pages.auth

import tyrian.*
import tyrian.Html.*
import cats.effect.IO
import com.bmmedia.jobsboard.pages.Page.Msg
import com.bmmedia.jobsboard.pages.Page

final case class SignUpPage() extends Page:

  override def initCmd: Cmd[IO, Page.Msg] = Cmd.None

  override def update(msg: Page.Msg): (Page, Cmd[IO, Page.Msg]) = (this, Cmd.None)

  override def view(): Html[Page.Msg] =
    div(
      h1("Sign up Page")
    )
