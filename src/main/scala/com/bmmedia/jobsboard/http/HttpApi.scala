package com.bmmedia.jobsboard.http

import com.bmmedia.jobsboard.http.routes.*
import cats.Monad
import org.http4s.server.Router
import cats.implicits._

class HttpApi[F[_]: Monad] private {
  private val healthRoutes = HealthRoutes[F].routes
  private val jobRoutes    = JobRoutes[F].routes

  val endpoints = Router(
    "/api" -> (healthRoutes <+> jobRoutes)
  )
}

object HttpApi {
  def apply[F[_]: Monad] = new HttpApi[F]
}
