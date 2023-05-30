package com.bmmedia.jobsboard.http.routes

import org.http4s.dsl.Http4sDsl
import org.http4s.HttpRoutes
import cats.Monad
import org.http4s.server.Router

class HealthRoutes[F[_]: Monad] private extends Http4sDsl[F] {

  private val getHealthRoute: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root =>
    Ok("Server is live")
  }

  val routes = Router("/health" -> getHealthRoute)
}

object HealthRoutes {
  def apply[F[_]: Monad] = new HealthRoutes[F]
}
