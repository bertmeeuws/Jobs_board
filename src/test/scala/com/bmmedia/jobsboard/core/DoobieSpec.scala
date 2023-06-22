package com.bmmedia.jobsboard.core

import cats.effect.*
import doobie.*
import doobie.implicits.*
import doobie.util.*
import org.testcontainers.containers.PostgreSQLContainer
import com.bmmedia.jobsboard.modules.Database
import com.bmmedia.jobsboard.config.PostgresConfig
import doobie.hikari.HikariTransactor

trait DoobieSpec(val initScript: String = "sql/jobs.sql") {

  val postgres: Resource[IO, PostgreSQLContainer[Nothing]] = {
    val acquire = IO {
      val container: PostgreSQLContainer[Nothing] =
        new PostgreSQLContainer("postgres").withInitScript(initScript)
      container.start()
      container
    }
    val release = (container: PostgreSQLContainer[Nothing]) => IO(container.stop())
    Resource.make(acquire)(release)
  }

  val transactor: Resource[IO, Transactor[IO]] = for {
    container <- postgres
    ce        <- ExecutionContexts.fixedThreadPool[IO](1)
    xa <- HikariTransactor.newHikariTransactor[IO](
      container.getDriverClassName(),
      container.getJdbcUrl(),
      container.getUsername(),
      container.getPassword(),
      ce
    )
  } yield xa
}
