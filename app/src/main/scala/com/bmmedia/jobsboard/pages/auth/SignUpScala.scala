package com.bmmedia.jobsboard.pages.auth

import tyrian.*
import tyrian.Html.*
import cats.effect.IO
import com.bmmedia.jobsboard.pages.Page.Msg
import com.bmmedia.jobsboard.pages.Page
import com.bmmedia.jobsboard.pages.auth.SignUpPage.NoOp

final case class SignUpPage(
    firstName: String,
    lastName: String,
    email: String,
    password: String,
    confirmPassword: String,
    company: String
) extends Page:
  override def initCmd: Cmd[IO, Page.Msg] = Cmd.None

  override def update(msg: Page.Msg): (Page, Cmd[IO, Page.Msg]) = (this, Cmd.None)

  override def view(): Html[Page.Msg] =
    div(
      h1("Sign up Page")
        form (
          name    := "signup",
          `class` := "w-full max-w-lg",
          onEvent(
            "submit",
            e => {
              e.preventDefault()
              NoOp
            }
          )
        )(
          // inputs
        )
    )

object SignUpPage {
  trait Msg extends Page.Msg

  case object NoOp extends Msg
}
