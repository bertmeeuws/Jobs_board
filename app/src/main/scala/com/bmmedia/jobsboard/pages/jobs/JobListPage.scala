package com.bmmedia.jobsboard.pages.jobs

import io.circe.generic.auto.*
import io.circe.syntax.*
import io.circe.parser.*

import tyrian.*
import tyrian.Html.*
import cats.effect.IO
import tyrian.cmds.*
import com.bmmedia.common.*
import cats.effect.*
import tyrian.http.*
import com.bmmedia.jobsboard.*
import com.bmmedia.jobsboard.pages.Page
import com.bmmedia.jobsboard.domain.job.Job
import com.bmmedia.jobsboard.domain.job.JobFilter

final case class JobListPage(
    isFilterTabOpen: Boolean = false,
    isSalaryFilterOpen: Boolean = false,
    jobs: List[Job] = List.empty,
    jobFilter: JobFilter = JobFilter(remote = true)
) extends Page:
  import JobListPage.*

  override def initCmd: Cmd[IO, App.Msg] = Endpoints.fetchJobs.call[JobFilter](jobFilter)

  override def update(msg: App.Msg): (Page, Cmd[IO, App.Msg]) = msg match {
    case ToggleFilterTab    => (this.copy(isFilterTabOpen = !isFilterTabOpen), Cmd.None)
    case ToggleSalaryFilter => (this.copy(isSalaryFilterOpen = !isSalaryFilterOpen), Cmd.None)
    case ReceivedJobs(jobs) =>
      println(jobs)
      (this.copy(jobs = jobs), Cmd.None)
    case FetchJobs =>
      println("Fetching jobs")
      (this, Endpoints.fetchJobs.call[JobFilter](jobFilter))
    case JobsError(message) => (this, Cmd.None)
    case _                  => (this, Cmd.None)
  }

  override def view(): Html[App.Msg] =
    section(
      div(`class` := "py-6 flex justify-center items-center bg-blue-400")(
        h1(`class` := "text-xl")(s"Job List Page ${jobs.length}")
      ),
      div(
        `class` := "grid grid-cols-3 gap-4 mx-auto max-w-[1100px] mt-24 px-4"
      )(
        div(`class` := "col-span-1")(
          renderFilters()
        ),
        ul(`class` := "col-span-2 flex flex-col justify-start space-y-4 cursor-pointer")(
          jobs.map { job =>
            li(
              `class` := "flex flex-col px-4 py-6 rounded-md border-2"
            )(
              h5(text(job.jobInfo.title)),
              p(text(s"${job.jobInfo.salaryLo} - ${job.jobInfo.salaryHi}")),
              p(text(s"${job.jobInfo.location}"))
            )
          }
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

  case object FetchJobs                    extends Msg
  case class JobsError(message: String)    extends Msg
  case class ReceivedJobs(jobs: List[Job]) extends Msg

  object Endpoints {
    val fetchJobs = new Endpoint[App.Msg] {
      val location = Constants.Endpoints.allJobs
      val method   = Method.Post
      val headers: List[Header] = List(
        Header("Content-Type", "application/json")
      )

      val onSuccess: Response => App.Msg = response =>
        response.status match {
          case Status(200, _) => {
            val json   = response.body
            val parsed = parse(json).flatMap(_.as[List[Job]])
            parsed match {
              case Left(_) => JobsError("Something went wrong")
              case Right(jobs) =>
                ReceivedJobs(jobs)
            }
          }
          case Status(s, _) if s >= 400 && s < 500 =>
            JobsError("Something went wrong")
        }
      val onFailure: HttpError => App.Msg = e => JobsError(e.toString())
    }
  }
}
