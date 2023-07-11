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
import com.bmmedia.jobsboard.core.Router
import com.bmmedia.jobsboard.components.*
import com.bmmedia.jobsboard.pages.Page

object App {
  type Msg = Router.Msg | Page.Msg

  case class Model(router: Router, page: Page)
}

@JSExportTopLevel("BMMediaApp")
class App extends TyrianApp[App.Msg, App.Model] {
  import App.*

  override def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) =
    val location = window.location.pathname
    val page     = Page.get(location)
    val pageCmd  = page.initCmd

    val (router, cmd) = Router.startAt(location)

    (Model(router, page), cmd |+| pageCmd)

  override def subscriptions(model: Model): Sub[IO, Msg] =
    Sub.make(
      "urlChange",
      model.router.history.state.discrete
        .map(_.get)
        .map(newLocation => Router.ChangeLocation(newLocation))
    )

  override def update(model: Model): Msg => (Model, Cmd[IO, Msg]) = {
    case msg: Router.Msg =>
      val (newRouter, routerCmd) = model.router.update(msg)
      if (model.router == newRouter) (model, Cmd.None)
      else {
        val newPage    = Page.get(newRouter.location)
        val newPageCmd = newPage.initCmd

        (model.copy(router = newRouter, page = newPage), routerCmd |+| newPageCmd)
      }
    case msg: Page.Msg =>
      val (newPage, cmd) = model.page.update(msg)
      (model.copy(page = newPage), cmd)
  }

  override def view(model: Model): Html[App.Msg] = div(
    Header.view(),
    model.page.view()
  )

}
