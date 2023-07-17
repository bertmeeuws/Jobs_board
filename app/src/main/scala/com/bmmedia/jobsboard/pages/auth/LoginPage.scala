package com.bmmedia.jobsboard.pages.auth

import io.circe.generic.auto.*
import io.circe.syntax.*
import io.circe.parser.*

import tyrian.*
import tyrian.Html.*
import cats.effect.IO
import com.bmmedia.jobsboard.pages.*
import com.bmmedia.jobsboard.pages.Page
import com.bmmedia.common.Endpoint
import com.bmmedia.common.Constants
import tyrian.http.*
import com.bmmedia.jobsboard.core.*
import com.bmmedia.jobsboard.domain.auth.Credentials
import com.bmmedia.jobsboard.*
import tyrian.cmds.Logger

final case class LoginPage(
    email: String = "",
    password: String = "",
    status: Option[Page.Status] = None
) extends Page:
  import LoginPage.*

  override def initCmd: Cmd[IO, App.Msg] = Cmd.None

  override def update(msg: App.Msg): (Page, Cmd[IO, App.Msg]) = msg match {
    case UpdateEmail(email) =>
      (this.copy(email = email), Cmd.None)
    case UpdatePassword(password) =>
      (this.copy(password = password), Cmd.None)
    case NoOp => (this, Cmd.None)
    case AttemptLogin =>
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
      } else {
        (this, Endpoints.signIn.call(Credentials(email, password)))
      }
    case SignInError(message) => (setErrorStatus(message), Cmd.None)
    case SignInSuccess(message, token, email) =>
      (setSuccesStatus(message), Cmd.Emit(Session.SetToken(email, token)))
    case _ => (this, Cmd.None)
  }

  override def view(): Html[App.Msg] = {
    div(
      h1("Login Page update"),
      text(status.map(_.message).getOrElse("")),
      text(s"name: $email"),
      text("test"),
      text(email),
      text(password),
      form(
        name    := "signup",
        `class` := "w-full max-w-lg ml-10",
        onEvent(
          "submit",
          e => {
            e.preventDefault()
            NoOp
          }
        )
      )(
        createInput("email", "email", "email", "Email", true, UpdateEmail(_)),
        createInput("password", "password", "password", "Password", true, UpdatePassword(_))
      ),
      button(
        `class` := "bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline",
        `type` := "button",
        onClick(AttemptLogin)
      )("Sign in")
    )

  }

  def createInput(
      Id: String,
      name: String,
      `Type`: String,
      Placeholder: String,
      IsRequired: Boolean = true,
      onChange: String => App.Msg
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

object LoginPage {
  trait Msg extends App.Msg

  case object AttemptLogin extends Msg
  case object NoOp         extends Msg

  final case class UpdateEmail(email: String)       extends Msg
  final case class UpdatePassword(password: String) extends Msg

  case class SignInError(message: String)                                 extends Msg
  case class SignInSuccess(message: String, token: String, email: String) extends Msg

  object Endpoints {
    val signIn = new Endpoint[Msg] {
      val location = Constants.Endpoints.signIn
      val method   = Method.Post
      val headers: List[Header] = List(
        Header("Content-Type", "application/json")
      )
      val onSuccess: Response => Msg = response =>
        response.status match {
          case Status(200, _) =>
            Logger.consoleLog[IO](response.headers.get("Authorization").get)
            SignInSuccess(
              "Successfully signed in",
              response.headers.get("Authorization").get,
              "test@gmail.com"
            )
          case Status(s, _) if s >= 400 && s < 500 =>
            val json   = response.body
            val parsed = parse(json).flatMap(_.hcursor.get[String]("error"))
            parsed match {
              case Left(_)      => SignInError("Credentials are invalid")
              case Right(error) => SignInError(error)
            }
        }
      val onFailure: HttpError => Msg = e => SignInError(e.toString())
    }
  }
}
