package com.bmmedia.jobsboard.core

import cats.effect.*
import tyrian.cmds.Logger
import tyrian.Cmd
import com.bmmedia.jobsboard.*
import tyrian.Html.*
import org.scalajs.dom.document
import cats.effect.*
import cats.implicits.*
import com.bmmedia.common.Constants
import scala.scalajs.js.Date

final case class Session(email: Option[String] = None, token: Option[String] = None) {
  import Session.*
  import Session.Commands.*

  def update(msg: Msg): (Session, Cmd[IO, App.Msg]) = msg match {
    case SetToken(email, token) =>
      (
        this.copy(email = Some(email), token = Some(token)),
        Commands.setAllSessionsCookies(email, token, isFresh = true)
      )
  }

  def initCmd: Cmd[IO, App.Msg] = {
    val maybeCookies = for {
      email <- getCookie("email")
      token <- getCookie("token")
    } yield (email, token)

    maybeCookies match {
      case Some((email, token)) =>
        Logger.consoleLog[IO](s"Found cookies for $email")
        Cmd.Emit(SetToken(email, token))
      case _ =>
        Logger.consoleLog[IO]("No cookies found")
        Cmd.None
    }
  }
}

object Session {
  trait Msg extends App.Msg

  case class SetToken(email: String, token: String) extends Msg

  object Commands {
    def setSessionCookie(name: String, value: String, isFresh: Boolean): Cmd[IO, Msg] =
      Cmd.SideEffect[IO] {
        if (getCookie(name).isEmpty || isFresh) {
          document.cookie = s"$name=$value;expires=Fri, 31 Dec 9999 23:59:59 GMT;path=/"
        }
      }

    def setAllSessionsCookies(email: String, token: String, isFresh: Boolean): Cmd[IO, Msg] =
      setSessionCookie("email", email, isFresh) |+| setSessionCookie("token", token, isFresh)

    def clearSessionCookie(name: String): Cmd[IO, Msg] =
      Cmd.SideEffect[IO] {
        document.cookie = s"$name=;expires=${new Date(0)};path=/"
      }

    def clearAllSessionsCookies(): Cmd[IO, Msg] =
      clearSessionCookie("email") |+| clearSessionCookie("token")

    def getCookie(name: String): Option[String] =
      document.cookie
        .split(";")
        .map(_.trim)
        .find(_.startsWith(s"$name="))
        .map(_.split("="))
        .map(_(1))
  }
}
