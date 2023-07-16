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

final case class LoginPage(
    email: String = "",
    password: String = "",
    status: Option[Page.Status] = None
) extends Page:
  import LoginPage.*

  override def initCmd: Cmd[IO, Page.Msg] = Cmd.None

  override def update(msg: Page.Msg): (Page, Cmd[IO, Page.Msg]) = msg match {
    case UpdateEmail(email)       => (this.copy(email = email), Cmd.None)
    case UpdatePassword(password) => (this.copy(password = password), Cmd.None)
    case NoOp                     => (this, Cmd.None)
    case AttemptLogin             => (this, Cmd.None)
    case _                        => (this, Cmd.None)
  }

  override def view(): Html[Page.Msg] = {
    div(
      h1("Login Page"),
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
        createInput("email", "Email", "email", "Email", true, UpdateEmail(_)),
        createInput("password", "Password", "password", "Password", true, UpdatePassword(_)),
        button(
          `class` := "bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline",
          `type` := "button",
          onClick(AttemptLogin)
        )("Sign up")
      )
    )

  }

  def createInput(
      Id: String,
      name: String,
      `Type`: String,
      Placeholder: String,
      IsRequired: Boolean = true,
      onChange: String => LoginPage.Msg
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

  def setSuccesStatus(message: String): Page = {
    this.copy(status = Some(Page.Status(message, Page.StatusKind.SUCCESS)))
  }

object LoginPage {
  trait Msg extends Page.Msg

  case object AttemptLogin extends Msg
  case object NoOp         extends Msg

  final case class UpdateEmail(email: String)       extends Msg
  final case class UpdatePassword(password: String) extends Msg
  case class SignInError(message: String)           extends Msg
  case class SignInSuccess(message: String)         extends Msg

  object Endpoints {
    val signIn = new Endpoint[Msg] {
      val location = Constants.Endpoints.signUp
      val method   = Method.Post
      val headers: List[Header] = List(
        Header("Content-Type", "application/json")
      )
      val onSuccess: Response => Msg = response =>
        response.status match {
          case Status(201, _) => SignInSuccess("Successfully signed in")
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
