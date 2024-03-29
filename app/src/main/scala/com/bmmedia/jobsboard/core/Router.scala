package com.bmmedia.jobsboard.core

import fs2.dom.History
import cats.effect.IO
import tyrian.*
import org.scalajs.dom.Window
import com.bmmedia.jobsboard.*

case class Router private (location: String, history: History[IO, String]) {
  import Router.*

  def update(msg: Msg): (Router, Cmd[IO, Msg]) = msg match {
    case ChangeLocation(newLocation, browserTriggered) =>
      if (location == newLocation) (this, Cmd.None)
      else
        val historyCmd = if (browserTriggered) Cmd.None else goto(newLocation)
        (this.copy(location = newLocation), historyCmd)
    case ExternalRedirect(newLocation) =>
      (this, navigateToExternal(newLocation))
    case _ => (this, Cmd.None)
  }

  def navigateToExternal(url: String) = Cmd.SideEffect[IO] {
    history.replaceState(url, url)
  }

  def goto[M](location: String): Cmd[IO, M] = Cmd.SideEffect[IO] {
    history.pushState(location, location)
  }
}

object Router {
  trait Msg extends App.Msg

  case class ChangeLocation(location: String, browserTriggered: Boolean = false) extends Msg
  case class ExternalRedirect(location: String)                                  extends Msg

  def startAt[M](initialLocation: String): (Router, Cmd[IO, M]) =
    val router = Router(initialLocation, History[IO, String])
    (router, router.goto(initialLocation))
}
