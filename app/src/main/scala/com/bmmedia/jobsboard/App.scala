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
import com.bmmedia.jobsboard.core.Session
import com.bmmedia.jobsboard.core.Session.*

object App {
  trait Msg

  case class Model(router: Router, page: Page, session: Session)
}

@JSExportTopLevel("BMMediaApp")
class App extends TyrianApp[App.Msg, App.Model] {
  import App.*

  override def init(flags: Map[String, String]): (Model, Cmd[IO, App.Msg]) =
    val location            = window.location.pathname
    val page                = Page.get(location)
    val pageCmd             = page.initCmd
    val (router, routerCmd) = Router.startAt(location)

    val session    = Session()
    val sessionCmd = session.initCmd

    (Model(router, page, session), routerCmd |+| pageCmd |+| sessionCmd)

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
    case msg: Session.Msg =>
      val (newSession, cmd) = model.session.update(msg)

      (model.copy(session = newSession), cmd)
  }

  override def view(model: Model): Html[App.Msg] = div(
    Header.view(),
    model.page.view()
  )

}
