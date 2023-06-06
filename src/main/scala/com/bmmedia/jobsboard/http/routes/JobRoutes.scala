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
import com.bmmedia.jobsboard.http.responses.*
import com.bmmedia.jobsboard.domain.job.*
import cats.effect.*

import org.typelevel.log4cats.Logger
import org.checkerframework.checker.units.qual.s
import com.bmmedia.jobsboard.core.Jobs
import com.bmmedia.jobsboard.validation.syntax.*
import cats.data.Validated

class JobRoutes[F[_]: Concurrent: Logger] private (jobsRepository: Jobs[F])
    extends HttpValidationDsl[F] {

  // Get all jobs
  private val allJobsRoute: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root =>
    for {
      _    <- Logger[F].info("Getting all jobs")
      jobs <- jobsRepository.findAll()
      resp <- Ok(jobs)
    } yield resp
  }

  // Get job by id
  private val findJobRoute: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root / UUIDVar(id) =>
    for {
      _   <- Logger[F].info(s"Getting job $id")
      job <- jobsRepository.find(id)
      response <- job match {
        case Some(job) => Ok(job)
        case None      => NotFound(FailureResponse(s"Job $id not found"))
      }
    } yield response
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
    req.validate[JobInfo] { jobInfo =>
      for {
        _ <- Logger[F].info(s"Creating job")
        jobInfo <- req
          .as[JobInfo]
        jobInfo <- req.as[JobInfo].logError(e => s"Error parsing job info: $e")
        _       <- Logger[F].info(s"Job info: $jobInfo")
        job     <- createJob(jobInfo)
        _       <- Logger[F].info("Job created")
        uuid    <- jobsRepository.create(job.ownerEmail, jobInfo)
        _ <- Logger[F].info(
          s"Created job: ${uuid}"
        )
        resp <- Ok(uuid)
      } yield resp
    }

  }

  // Update jobs/uuid {job}
  private val updateJobRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ PUT -> Root / UUIDVar(id) =>
      jobsRepository.find(id).flatMap {
        case Some(job) =>
          for {
            _       <- Logger[F].info(s"Updating job $id")
            jobInfo <- req.as[JobInfo].logError(e => s"Error parsing job info: $e")
            _       <- Logger[F].info(s"Found job with info: $jobInfo")
            updated <- jobsRepository.update(id, jobInfo)
            _ <- Logger[F].info(
              s"Updated job: ${updated}"
            )
            resp <- updated match {
              case Some(job) => Ok(job)
              case None      => NotFound(FailureResponse(s"Job $id could not be updated"))
            }
          } yield resp
        case None => NotFound(FailureResponse(s"Job $id not found"))
      }
  }

  // Delete job
  private val deleteJobRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case DELETE -> Root / UUIDVar(id) =>
      jobsRepository.find(id).flatMap {
        {
          case Some(job) =>
            for {
              _    <- Logger[F].info(s"Deleting job $id")
              _    <- jobsRepository.delete(job.id).pure[F]
              resp <- Ok(s"Job $id deleted")
            } yield resp
          case None => NotFound(FailureResponse(s"Job $id not found"))
        }
      }
  }

  val routes = Router(
    "/jobs" -> (allJobsRoute <+> findJobRoute <+> updateJobRoute <+> deleteJobRoute <+> createJobRoute)
  )
}

object JobRoutes {
  def apply[F[_]: Concurrent: Logger](jobsRepository: Jobs[F]) = new JobRoutes[F](jobsRepository)
}
