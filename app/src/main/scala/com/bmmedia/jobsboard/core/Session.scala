package com.bmmedia.jobsboard.core

import cats.effect.*
import tyrian.cmds.Logger
import tyrian.Cmd
import com.bmmedia.jobsboard.*

final case class Session(email: Option[String] = None, token: Option[String] = None) {
  import Session.*

  def update(msg: Msg): (Session, Cmd[IO, Msg]) = msg match {
    case SetToken(email, token) =>
      Logger.consoleLog[IO](s"Setting token for $email");
      (this.copy(email = Some(email), token = Some(token)), Cmd.None)
    case _ => (this, Cmd.None)
  }

  def initCmd: Cmd[IO, Msg] = Logger.consoleLog[IO]("Started session observer")
}

object Session {
  trait Msg extends App.Msg

  case class SetToken(email: String, token: String) extends Msg
}
