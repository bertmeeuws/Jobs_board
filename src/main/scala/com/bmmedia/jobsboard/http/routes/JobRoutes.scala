package com.bmmedia.jobsboard.http.routes

import io.circe.generic.auto.*

import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.Http4sDsl
import org.http4s.HttpRoutes
import cats.Monad
import org.http4s.server.Router
import cats.implicits._

import scala.collection.mutable
import java.util.UUID
import com.bmmedia.jobsboard.domain.job.*
import com.bmmedia.jobsboard.domain.job
import com.bmmedia.jobsboard.http.responses.*
import com.bmmedia.jobsboard.domain.job.*
import cats.effect.*

import org.typelevel.log4cats.Logger
import org.checkerframework.checker.units.qual.s

class JobRoutes[F[_]: Concurrent: Logger] private extends Http4sDsl[F] {

  private val jobsDatabase = mutable.Map[UUID, Job]()

  // Get all jobs
  private val allJobsRoute: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root =>
    Ok(jobsDatabase.values)
  }

  // Get job by id
  private val findJobRoute: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root / UUIDVar(id) =>
    jobsDatabase.get(id) match {
      case Some(job) => Ok(job)
      case None      => NotFound(FailureResponse(s"Job $id not found"))
    }
  }

  private def createJob(jobInfo: JobInfo): F[Job] =
    Job(
      id = UUID.randomUUID(),
      date = System.currentTimeMillis(),
      ownerEmail = "test@gmail.com",
      jobInfo = jobInfo,
      active = true
    ).pure[F]

  import com.bmmedia.jobsboard.logging.syntax.*

  private val createJobRoute: HttpRoutes[F] = HttpRoutes.of[F] { case req @ POST -> Root =>
    for {
      _       <- Logger[F].info(s"Creating job")
      jobInfo <- req.as[JobInfo].logError(e => s"Error parsing job info: $e")
      _       <- Logger[F].info(s"Job info: $jobInfo")
      job     <- createJob(jobInfo)
      _       <- jobsDatabase.put(job.id, job).pure[F]
      resp    <- Created(job)
    } yield resp
  }

  // Update jobs/uuid {job}
  private val updateJobRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ PUT -> Root / UUIDVar(id) =>
      jobsDatabase.get(id) match {
        case Some(job) =>
          for {
            _       <- Logger[F].info(s"Updating job $id")
            jobInfo <- req.as[JobInfo].logError(e => s"Error parsing job info: $e")
            _       <- Logger[F].info(s"Job info: $jobInfo")
            updated <- job.copy(jobInfo = jobInfo).pure[F]
            _ <- Logger[F].info(
              s"Updated job: ${updated.id} ${updated.date} ${updated.ownerEmail} ${updated.jobInfo} ${updated.active}"
            )
            _    <- jobsDatabase.update(id, updated).pure[F]
            resp <- Ok(updated)
          } yield resp
        case None => NotFound(FailureResponse(s"Job $id not found"))
      }
  }

  // Delete job
  private val deleteJobRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case DELETE -> Root / UUIDVar(id) =>
      jobsDatabase.get(id) match {
        case Some(_) =>
          for {
            _    <- (println("Deleting job")).pure[F]
            _    <- jobsDatabase.remove(id).pure[F]
            resp <- Ok(s"Job $id deleted")
          } yield resp
        case None => NotFound(FailureResponse(s"Job $id not found"))
      }
  }

  val routes = Router(
    "/jobs" -> (allJobsRoute <+> findJobRoute <+> updateJobRoute <+> deleteJobRoute <+> createJobRoute)
  )
}

object JobRoutes {
  def apply[F[_]: Concurrent: Logger] = new JobRoutes[F]
}
