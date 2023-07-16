package com.bmmedia.jobsboard.core

import cats.effect.*
import tyrian.cmds.Logger

final case class Session(email: Option[String] = None, token: Option[String] = None) {
  import Session.*

  def update(msg: Msg): Session = msg match {
    case SetToken(email, token) =>
      Logger.consoleLog[IO](s"Setting token for $email");
      this.copy(email = Some(email), token = Some(token))
    case _ => this
  }
}

object Session {
  trait Msg

  case class SetToken(email: String, token: String) extends Msg
}
