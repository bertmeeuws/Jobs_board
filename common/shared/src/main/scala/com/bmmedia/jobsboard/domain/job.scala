package com.bmmedia.jobsboard.domain

import java.util.UUID

object job {
  case class Job(
      id: UUID,
      date: Long,
      ownerEmail: String,
      jobInfo: JobInfo,
      active: Boolean = false
  )

  final case class JobFilter(
      companies: List[String] = List(),
      locations: List[String] = List(),
      countries: List[String] = List(),
      seniorities: List[String] = List(),
      tags: List[String] = List(),
      remote: Boolean = false
  )

  case class JobInfo(
      company: String,
      title: String,
      description: String,
      externalUrl: String,
      salaryLo: Option[Int],
      salaryHi: Option[Int],
      currency: Option[String],
      remote: Boolean,
      location: String,
      country: Option[String],
      tags: Option[List[String]],
      image: Option[String],
      seniority: Option[String],
      other: Option[String]
  )

  object JobInfo {
    val empty: JobInfo =
      JobInfo("", "", ",", "", None, None, None, false, "", None, None, None, None, None)

    def minimal(
        company: String,
        title: String,
        description: String,
        externalUrl: String,
        remote: Boolean,
        location: String
    ): JobInfo =
      JobInfo(
        company,
        title,
        description,
        externalUrl,
        None,
        None,
        None,
        remote,
        location,
        None,
        None,
        None,
        None,
        None
      )
  }
}
