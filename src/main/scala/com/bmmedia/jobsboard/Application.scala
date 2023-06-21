package com.bmmedia.jobsboard

import org.http4s.*
import cats.effect.*
import org.http4s.dsl.*
import org.http4s.dsl.impl.*
import org.http4s.server.*
import org.http4s.implicits.*
import cats.effect.IOApp
import org.http4s.ember.server.EmberServerBuilder
import com.comcast.ip4s.Host
import pureconfig.ConfigSource
import com.bmmedia.jobsboard.config.*
import com.bmmedia.jobsboard.config.syntax.*

import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import com.bmmedia.jobsboard.modules.*
import cats.effect.IO

object Application extends IOApp.Simple {

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  override def run =
    ConfigSource.default.loadF[IO, AppConfig].flatMap {
      case AppConfig(postgresConfig, emberConfig, securityConfig) =>
        val appResource = for {
          xa      <- Database.makePostgresResource[IO](postgresConfig)
          core    <- Core[IO](xa, securityConfig)
          httpApi <- HttpApi[IO](core)
          server <- EmberServerBuilder
            .default[IO]
            .withHost(emberConfig.host)
            .withPort(emberConfig.port)
            .withHttpApp(
              httpApi.endpoints.orNotFound
            )
            .build
        } yield server
        appResource.use(_ => IO.println("Server started") *> IO.never)
    }
}
