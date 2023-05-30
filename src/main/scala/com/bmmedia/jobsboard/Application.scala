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
import pureconfig.ConfigSource
import com.bmmedia.jobsboard.config.*
import com.bmmedia.jobsboard.config.syntax.*

object Application extends IOApp.Simple {

  val configSource = ConfigSource.default.load[EmberConfig]

  override def run: IO[Unit] =
    ConfigSource.default.loadF[IO, EmberConfig].flatMap { config =>
      EmberServerBuilder
        .default[IO]
        .withHost(config.host)
        .withPort(config.port)
        .withHttpApp(
          HttpApi[IO].endpoints.orNotFound
        )
        .build
        .useForever
    }
}
