package com.bmmedia.jobsboard.http.routes

import org.http4s.dsl.Http4sDsl
import org.http4s.HttpRoutes
import cats.Monad
import org.http4s.server.Router
import cats.implicits._

class JobRoutes[F[_]: Monad] private extends Http4sDsl[F] {

  // Get all jobs
  private val allJobsRoute: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root =>
    Ok("Get all jobs")
  }

  // Get job by id
  private val findJobRoute: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root / UUIDVar(id) =>
    Ok(s"Get job with id $id")
  }

  // Update jobs/uuid {job}
  private val updateJobRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ PUT -> Root / UUIDVar(id) =>
      Ok(s"Update job with id $id")
  }

  // Delete job
  private val deleteJobRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case DELETE -> Root / UUIDVar(id) =>
      Ok(s"Delete job with id $id")
  }

  val routes = Router(
    "/jobs" -> (allJobsRoute <+> findJobRoute <+> updateJobRoute <+> deleteJobRoute)
  )
}

object JobRoutes {
  def apply[F[_]: Monad] = new JobRoutes[F]
}
