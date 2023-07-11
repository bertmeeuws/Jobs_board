package com.bmmedia.jobsboard.pages.jobs

import tyrian.*
import tyrian.Html.*
import cats.effect.IO
import com.bmmedia.jobsboard.pages.Page.Msg
import com.bmmedia.jobsboard.pages.Page

final case class JobDetailPage(id: String) extends Page:

  override def initCmd: Cmd[IO, Page.Msg] = Cmd.None

  override def update(msg: Page.Msg): (Page, Cmd[IO, Page.Msg]) = (this, Cmd.None)

  override def view(): Html[Page.Msg] =
    div(
      h1("Job detail page")
    )
