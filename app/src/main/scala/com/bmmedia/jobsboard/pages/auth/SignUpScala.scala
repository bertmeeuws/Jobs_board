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
) extends Page {
  override def initCmd: Cmd[IO, Page.Msg] = Cmd.None

  override def update(msg: Page.Msg): (Page, Cmd[IO, Page.Msg]) = (this, Cmd.None)

  override def view(): Html[Page.Msg] = {
    div()(
      h1("Sign up Page"),
      form(
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
        createInput("firstName", "text", "First Name", firstName),
        createInput("lastName", "text", "Last Name", lastName),
        createInput("email", "email", "Email", email),
        createInput("password", "password", "Password", password),
        createInput("confirmPassword", "password", "Confirm Password", confirmPassword),
        createInput("company", "text", "Company", company),
        button(
          `class` := "bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline",
          `type` := "submit"
        )("Sign up")
      )
    )
  }

  def createInput(
      Id: String,
      `Type`: String,
      Placeholder: String,
      Value: String
  ): Html[Page.Msg] =
    input(
      `class` := "shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline",
      id          := Id,
      `type`      := `Type`,
      placeholder := Placeholder,
      value       := Value
    )

}
object SignUpPage {
  trait Msg extends Page.Msg

  case object NoOp extends Msg
}
