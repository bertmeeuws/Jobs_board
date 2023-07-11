package com.bmmedia.jobsboard.pages.auth

import tyrian.*
import tyrian.Html.*
import cats.effect.IO
import com.bmmedia.jobsboard.pages.Page
import tyrian.cmds.Logger
import com.bmmedia.common.Constants

final case class SignUpPage(
    firstName: String = "",
    lastName: String = "",
    email: String = "",
    password: String = "",
    confirmPassword: String = "",
    company: String = "",
    status: Option[Page.Status] = None
) extends Page {
  import com.bmmedia.jobsboard.pages.auth.SignUpPage._

  override def initCmd: Cmd[IO, Page.Msg] = Cmd.None

  override def update(msg: Page.Msg): (Page, Cmd[IO, Page.Msg]) = msg match {
    case UpdateEmail(email) => (this.copy(email = email), Cmd.None)
    case UpdateFirstName(firstName) =>
      (this.copy(firstName = firstName), Cmd.None)
    case UpdateLastName(lastName) =>
      (this.copy(lastName = lastName), Cmd.None)
    case UpdatePassword(password) =>
      (this.copy(password = password), Cmd.None)
    case UpdateConfirmPassword(confirmPassword) =>
      (
        this.copy(confirmPassword = confirmPassword),
        Cmd.None
      )
    case UpdateCompany(company) =>
      (this.copy(company = company), Cmd.None)
    case NoOp => (this, Cmd.None)
    case AttemptSignUp => {
      if (!email.matches(Constants.emailRegex)) {
        (
          setErrorStatus("Please enter a valid email address"),
          Cmd.None
        )
      } else if (password.isEmpty()) {
        (
          setErrorStatus("Please enter a password"),
          Cmd.None
        )
      } else if (password != confirmPassword) {
        (
          setErrorStatus("Passwords do not match"),
          Cmd.None
        )
      } else {
        (this, Logger.consoleLog[IO]("Signing up", email, password))
      }
    }

    case _ => (this, Cmd.None)
  }

  override def view(): Html[Page.Msg] = {
    div()(
      h1("Sign up Page"),
      status match {
        case Some(status) => div(`class` := "text-red-500")(text(status.message))
        case None         => div()
      },
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
      )(createInput("firstName", "First Name", "text", "First Name", true, UpdateFirstName(_))),
      createInput("lastName", "Last Name", "text", "Last Name", true, UpdateLastName(_)),
      createInput("email", "Email", "email", "Email", true, UpdateEmail(_)),
      createInput("password", "Password", "password", "Password", true, UpdatePassword(_)),
      createInput(
        "confirmPassword",
        "Confirm Password",
        "password",
        "Confirm Password",
        true,
        UpdateConfirmPassword(_)
      ),
      createInput("company", "Company", "text", "Company", true, UpdateCompany(_)),
      button(
        `class` := "bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline",
        `type` := "button",
        onClick(AttemptSignUp)
      )("Sign up")
    )
  }

  def createInput(
      Id: String,
      name: String,
      `Type`: String,
      Placeholder: String,
      IsRequired: Boolean = true,
      onChange: String => Msg
  ): Html[Page.Msg] =
    div(
      `class` := "form-input"
    )(
      label(`class` := "block text-gray-700 text-sm font-bold mb-2", `for` := name)(
        if (IsRequired) span(`class` := "text-red-500")("*") else span(),
        text(name)
      ),
      input(
        `class` := "shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline",
        id          := Id,
        `type`      := `Type`,
        placeholder := Placeholder,
        onInput(onChange)
      )
    )

  def setErrorStatus(message: String): Page = {
    this.copy(status = Some(Page.Status(message, Page.StatusKind.ERROR)))
  }

}
object SignUpPage {
  trait Msg extends Page.Msg

  case class UpdateEmail(email: String)                     extends Msg
  case class UpdatePassword(password: String)               extends Msg
  case class UpdateConfirmPassword(confirmPassword: String) extends Msg
  case class UpdateFirstName(firstName: String)             extends Msg
  case class UpdateLastName(lastName: String)               extends Msg
  case class UpdateCompany(company: String)                 extends Msg

  case object NoOp          extends Msg
  case object AttemptSignUp extends Msg
}
