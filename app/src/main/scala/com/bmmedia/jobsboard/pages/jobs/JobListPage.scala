package com.bmmedia.jobsboard.pages.jobs

import tyrian.*
import tyrian.Html.*
import cats.effect.IO
import com.bmmedia.jobsboard.*
import com.bmmedia.jobsboard.pages.Page

final case class JobListPage(isFilterTabOpen: Boolean = false, isSalaryFilterOpen: Boolean = false)
    extends Page:
  import JobListPage.*

  override def initCmd: Cmd[IO, App.Msg] = Cmd.None

  override def update(msg: App.Msg): (Page, Cmd[IO, App.Msg]) = msg match {
    case ToggleFilterTab    => (this.copy(isFilterTabOpen = !isFilterTabOpen), Cmd.None)
    case ToggleSalaryFilter => (this.copy(isSalaryFilterOpen = !isSalaryFilterOpen), Cmd.None)
    case _                  => (this, Cmd.None)
  }

  override def view(): Html[App.Msg] =
    section(
      div(`class` := "py-6 flex justify-center items-center bg-blue-400")(
        h1(`class` := "text-xl")("Job List Page")
      ),
      div(
        `class` := "grid grid-cols-3 gap-4 mx-auto max-w-[1100px] mt-24 px-4"
      )(
        div(`class` := "col-span-1")(
          renderFilters()
        ),
        ul(`class` := "col-span-2 flex flex-col justify-start space-y-4 cursor-pointer")(
          renderJobItem(),
          renderJobItem(),
          renderJobItem()
        )
      )
    )

  def renderJobItem(): Html[App.Msg] = {
    div(`class` := "flex flex-col px-4 py-6 rounded-md border-2")(
      h5(text("Rhythmos, Inc. - Intermediate Backend Developer")),
      p(text("USD 80000-140000")),
      p(text("Remote, United States"))
    )
  }

  def renderFilters(): Html[App.Msg] = {
    div(`class` := "flex flex-col space-y-4")(
      renderSalaryFilter(),
      renderSalaryFilter(),
      renderSalaryFilter()
    )
  }

  def renderSalaryFilter(): Html[App.Msg] =
    div(`class` := "flex flex-col px-4 py-6 rounded-md border-2")(
      div(`class` := "flex justify-between items-center flex-col")(
        div(`class` := "flex justify-between items-center w-full pb-2 border-b-[1px]")(
          p(text("Filter")),
          p(`class` := "text-3xl cursor-pointer", onClick(ToggleSalaryFilter))(
            if isSalaryFilterOpen then text("-") else text("+")
          )
        ),
        if isSalaryFilterOpen then {
          div(
            p(text("Min. salary")),
            input(`class` := "w-full border-2 rounded-md p-2")
          )
        } else {
          div()
        }
      )
    )

object JobListPage {
  trait Msg extends App.Msg

  case object ToggleFilterTab    extends Msg
  case object ToggleSalaryFilter extends Msg
}
