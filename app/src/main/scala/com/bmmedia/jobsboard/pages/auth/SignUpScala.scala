package com.bmmedia.jobsboard.pages.auth

import io.circe.generic.auto.*
import io.circe.syntax.*
import io.circe.parser.*

import tyrian.*
import tyrian.Html.*
import tyrian.cmds.*
import com.bmmedia.common.*
import cats.effect.*
import tyrian.http.*
import com.bmmedia.jobsboard.pages.Page
import concurrent.duration.DurationInt
import com.bmmedia.jobsboard.*

import com.bmmedia.jobsboard.domain.auth.UserRegister
import io.circe.Encoder

final case class SignUpPage(
    firstName: String = "",
    lastName: String = "",
    email: String = "",
    password: String = "",
    confirmPassword: String = "",
    company: String = "",
    status: Option[Page.Status] = None
) extends Page {
  import SignUpPage.*

  override def initCmd: Cmd[IO, App.Msg] = Cmd.None

  override def update(msg: App.Msg): (Page, Cmd[IO, App.Msg]) = msg match {
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
        (
          this,
          Endpoints.signUp.call(
            UserRegister(email, firstName, lastName, password, Some(company), Some(""))
          )
        )
      }
    }
    case SignUpError(message) => {
      (
        setErrorStatus(message),
        (Cmd.None)
      )
    }
    case SignUpSuccess(message) => {
      (
        setSuccesStatus(message),
        (Cmd.None)
      )
    }
    case _ => (this, Cmd.None)
  }

  override def view(): Html[App.Msg] = {
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
  ): Html[App.Msg] =
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

  def setSuccesStatus(message: String): Page = {
    this.copy(status = Some(Page.Status(message, Page.StatusKind.SUCCESS)))
  }

}
object SignUpPage {
  trait Msg extends App.Msg

  case class UpdateEmail(email: String)                     extends Msg
  case class UpdatePassword(password: String)               extends Msg
  case class UpdateConfirmPassword(confirmPassword: String) extends Msg
  case class UpdateFirstName(firstName: String)             extends Msg
  case class UpdateLastName(lastName: String)               extends Msg
  case class UpdateCompany(company: String)                 extends Msg

  case object NoOp                          extends Msg
  case object AttemptSignUp                 extends Msg
  case class SignUpError(message: String)   extends Msg
  case class SignUpSuccess(message: String) extends Msg

  object Endpoints {
    val signUp = new Endpoint[Msg] {
      val location = Constants.Endpoints.signUp
      val method   = Method.Post
      val headers: List[Header] = List(
        Header("Content-Type", "application/json")
      )
      val onSuccess: Response => Msg = response =>
        response.status match {
          case Status(201, _) => SignUpSuccess("Successfully signed up")
          case Status(s, _) if s >= 400 && s < 500 =>
            val json   = response.body
            val parsed = parse(json).flatMap(_.hcursor.get[String]("error"))
            parsed match {
              case Left(_)      => SignUpError("Something went wrong")
              case Right(error) => SignUpError(error)
            }
        }
      val onFailure: HttpError => Msg = e => SignUpError(e.toString())
    }
  }

}
