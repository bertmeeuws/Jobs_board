package com.bmmedia.jobsboard.http

import com.bmmedia.jobsboard.http.routes.*
import cats.effect.*
import org.http4s.server.Router
import cats.implicits._
import org.typelevel.log4cats.Logger

class HttpApi[F[_]: Concurrent: Logger] private {
  private val healthRoutes = HealthRoutes[F].routes
  private val jobRoutes    = JobRoutes[F].routes

  val endpoints = Router(
    "/api" -> (healthRoutes <+> jobRoutes)
  )
}

object HttpApi {
  def apply[F[_]: Concurrent: Logger] = new HttpApi[F]
}
