package com.bmmedia.jobsboard

import org.http4s.*
import cats.effect.*
import org.http4s.dsl.*
import org.http4s.dsl.impl.*
import org.http4s.server.*
import org.http4s.implicits.*
import cats.effect.IOApp
import org.http4s.ember.server.EmberServerBuilder
import com.bmmedia.jobsboard.http.HttpApi
import com.comcast.ip4s.Host

object Application extends IOApp.Simple {

  override def run: IO[Unit] = EmberServerBuilder
    .default[IO]
    .withHttpApp(
      HttpApi[IO].endpoints.orNotFound
    )
    .build
    .useForever
}
