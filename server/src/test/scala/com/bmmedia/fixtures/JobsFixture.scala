package com.bmmedia.fixtures

import com.bmmedia.jobsboard.domain.job.*
import java.util.UUID
import cats.syntax.all.*

trait JobFixture {

  val NotFoundJobUuid = UUID.fromString("6ea79557-3112-4c84-a8f5-1d1e2c300948")

  val AwesomeJobUuid = UUID.fromString("843df718-ec6e-4d49-9289-f799c0f40064")

  val AwesomeJob = Job(
    AwesomeJobUuid,
    1659186086L,
    "daniel@rockthejvm.com",
    JobInfo(
      "Awesome Company",
      "Tech Lead",
      "An awesome job in Berlin",
      "https://rockthejvm.com/awesomejob",
      2000.some,
      3000.some,
      "EUR".some,
      true,
      "Germany",
      "Berlin".some,
      Some(List("scala", "scala-3", "cats")),
      None,
      "Senior".some,
      None
    ),
    false
  )

  val InvalidJob = Job(
    null,
    42L,
    "nothing@gmail.com",
    JobInfo.empty
  )

  val UpdatedAwesomeJob = Job(
    AwesomeJobUuid,
    1659186086L,
    "daniel@rockthejvm.com",
    JobInfo(
      "Awesome Company",
      "Tech Lead",
      "An awesome job in Berlin",
      "https://rockthejvm.com/awesomejob",
      2000.some,
      3000.some,
      "EUR".some,
      true,
      "Germany",
      "Berlin".some,
      Some(List("scala", "scala-3", "cats")),
      None,
      "Senior".some,
      None
    ),
    false
  )

  val RockTheJvmNewJob = JobInfo(
    "Awesome Company",
    "Tech Lead",
    "An awesome job in Berlin",
    "https://rockthejvm.com/awesomejob",
    2000.some,
    3000.some,
    "EUR".some,
    true,
    "Germany",
    "Berlin".some,
    Some(List("scala", "scala-3", "cats")),
    None,
    "Senior".some,
    None
  )

  val RockTheJvmJobWithNotFoundId = AwesomeJob.copy(id = NotFoundJobUuid)

  val AnotherAwesomeJobUuid = UUID.fromString("19a941d0-aa19-477b-9ab0-a7033ae65c2b")
  val AnotherAwesomeJob     = AwesomeJob.copy(id = AnotherAwesomeJobUuid)

  val RockTheJvmAwesomeJob =
    AwesomeJob.copy(jobInfo = AwesomeJob.jobInfo.copy(company = "RockTheJvm"))

  val NewJobUuid = UUID.fromString("efcd2a64-4463-453a-ada8-b1bae1db4377")

  val AwesomeNewJob = JobInfo(
    "Awesome Company",
    "Tech Lead",
    "An awesome job in Berlin",
    "https://rockthejvm.com/awesomejob",
    2000.some,
    3000.some,
    "EUR".some,
    true,
    "Germany",
    "Berlin".some,
    Some(List("scala", "scala-3", "cats")),
    None,
    "Senior".some,
    None
  )
}
