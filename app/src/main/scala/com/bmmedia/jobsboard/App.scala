package com.bmmedia.jobsboard

import scala.scalajs.js.annotation.*
import org.scalajs.dom.document
import tyrian.Html.*
import tyrian.*
import org.scalajs.dom.*
import cats.effect.*
import concurrent.duration.*
import com.bmmedia.jobsboard.App.Model
import fs2.dom.History

object App {
  type Msg = Router.Msg

  case class Model(router: Router)
}

@JSExportTopLevel("BMMediaApp")
class App extends TyrianApp[App.Msg, App.Model] {
  import App.*

  override def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) =
    val (router, cmd) = Router.startAt(window.location.pathname)
    (Model(router), cmd)

  override def subscriptions(model: Model): Sub[IO, Msg] =
    Sub.make(
      "urlChange",
      model.router.history.state.discrete
        .map(_.get)
        .map(newLocation => Router.ChangeLocation(newLocation))
    )

  override def update(model: Model): Msg => (Model, Cmd[IO, Msg]) = { case msg: Router.Msg =>
    val (newRouter, cmd) = model.router.update(msg)
    (model.copy(router = newRouter), cmd)
  }

  override def view(model: Model): Html[Msg] = div(
    createNav("jobs", "/jobs"),
    createNav("home", "/home"),
    createNav("google", "https://google.com"),
    div(
      h1("Hello World"),
      p(s"You are now at: ${model.router.location}")
    )
  )

  private def createNav(name: String, location: String) = {
    a(
      href    := location,
      `class` := "p-3 text-red-500 hover:text-red-800 mx-6",
      onEvent(
        "click",
        e => {
          e.preventDefault()
          if name == "google" then Router.ExternalRedirect(location)
          else Router.ChangeLocation(location)
        }
      )
    )(name)
  }
}
