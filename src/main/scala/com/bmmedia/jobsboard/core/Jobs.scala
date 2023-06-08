package com.bmmedia.jobsboard.core

import com.bmmedia.jobsboard.domain.job.*
import java.util.UUID
import cats.*
import cats.implicits.*
import doobie.implicits.*
import cats.data.*
import doobie.*
import doobie.util.*
import doobie.postgres.implicits.*
import cats.effect.MonadCancelThrow
import com.bmmedia.jobsboard.domain.pagination.Pagination

// Core is the same as algebra
trait Jobs[F[_]] {
  def create(ownerEmail: String, jobInfo: JobInfo): F[UUID]
  def update(id: UUID, jobInfo: JobInfo): F[Option[Job]]
  def find(id: UUID): F[Option[Job]]
  def delete(id: UUID): F[Int]
  def findAll(): F[List[Job]]
  def findAll(pagination: Pagination, filters: JobFilter): F[List[Job]]
}

class LiveJobs[F[_]: MonadCancelThrow] private (xa: Transactor[F]) extends Jobs[F] {
  override def create(ownerEmail: String, jobInfo: JobInfo): F[UUID] =
    sql"""
    INSERT INTO jobs(
      date,
      owneremail,
      company,
      title,
      description,
      externalurl,
      salarylo,
      salaryhi,
      currency,
      remote,
      location,
      country,
      tags,
      image,
      seniority,
      other,
      active
    ) VALUES (
      ${System.currentTimeMillis()},
      $ownerEmail,
      ${jobInfo.company},
      ${jobInfo.title},
      ${jobInfo.description},
      ${jobInfo.externalUrl},
      ${jobInfo.salaryLo},
      ${jobInfo.salaryHi},
      ${jobInfo.currency},
      ${jobInfo.remote},
      ${jobInfo.location},
      ${jobInfo.country},
      ${jobInfo.tags},
      ${jobInfo.image},
      ${jobInfo.seniority},
      ${jobInfo.other},
      false
    )
    """.update
      .withUniqueGeneratedKeys[UUID]("id")
      .transact(xa)

  override def find(id: UUID): F[Option[Job]] =
    sql"""
      SELECT 
        id,
        date,
        ownerEmail,
        company,
        title,
        description,
        externalUrl,
        salarylo,
        salaryhi,
        currency,
        remote,
        location,
        country,
        tags,
        image,
        seniority,
        other,
        active
      FROM jobs WHERE id = $id
    """
      .query[Job]
      .option
      .transact(xa)

  override def delete(id: UUID): F[Int] =
    sql"DELETE FROM jobs WHERE id = $id".update.run
      .transact(xa)

  override def update(id: UUID, jobInfo: JobInfo) =
    sql"""
      UPDATE jobs
      SET 
        company = ${jobInfo.company},
        title = ${jobInfo.title},
        description = ${jobInfo.description},
        externalurl = ${jobInfo.externalUrl},
        salarylo = ${jobInfo.salaryLo},
        salaryhi = ${jobInfo.salaryHi},
        currency = ${jobInfo.currency},
        remote = ${jobInfo.remote},
        location = ${jobInfo.location},
        country = ${jobInfo.country},
        tags = ${jobInfo.tags},
        image = ${jobInfo.image},
        seniority = ${jobInfo.seniority},
        other = ${jobInfo.other}
      WHERE id = ${id}
        """.update.run
      .transact(xa)
      .flatMap(_ => find(id))

  override def findAll(pagination: Pagination, filters: JobFilter): F[List[Job]] = {
    val fragment = fr"""SELECT 
      id,
      date,
      owneremail,
      company,
      title,
      description,
      externalUrl,
      salarylo,
      salaryhi,
      currency,
      remote,
      location,
      country,
      tags,
      image,
      seniority,
      other,
      active"""

    val fragment2 = fr"FROM jobs"

    val Pagination(offset, limit)                                             = pagination
    val JobFilter(companies, locations, countries, seniorities, tags, remote) = filters

    val f1 = companies.toNel.map(companies => Fragments.in(fr"company", companies))
    val f2 = locations.toNel.map(locations => Fragments.in(fr"location", locations))
    val f3 = countries.toNel.map(countries => Fragments.in(fr"country", countries))
    val f4 = seniorities.toNel.map(seniorities => Fragments.in(fr"seniority", seniorities))
    val f5 =
      tags.toNel.map(tagss => Fragments.or(tagss.toList.map(tag => fr"$tag=any(tags)"): _*))

    val f6 = remote.some.map(remote => fr"remote=$remote")

    val whereClause = Fragments.whereAndOpt(f1, f2, f3, f4, f5, f6)

    val query =
      (fragment ++ fragment2 ++ whereClause ++ fr"ORDER BY date DESC LIMIT $limit OFFSET $offset")
        .query[Job]
        .to[List]
        .transact(xa)

    query
  }

  override def findAll(): F[List[Job]] =
    sql"""SELECT 
      id,
      date,
      owneremail,
      company,
      title,
      description,
      externalUrl,
      salarylo,
      salaryhi,
      currency,
      remote,
      location,
      country,
      tags,
      image,
      seniority,
      other,
      active
    FROM jobs"""
      .query[Job]
      .to[List]
      .transact(xa)
}

object LiveJobs {
  given jobRead: Read[Job] = Read[
    (
        UUID,
        Long,
        String,
        String,
        String,
        String,
        String,
        Option[Int],
        Option[Int],
        Option[String],
        Boolean,
        String,
        Option[String],
        Option[List[String]],
        Option[String],
        Option[String],
        Option[String],
        Boolean
    )
  ].map {
    case (
          id: UUID,
          date: Long,
          ownerEmail: String,
          company: String,
          title: String,
          description: String,
          externalUrl: String,
          salaryLo: Option[Int] @unchecked,
          salaryHi: Option[Int] @unchecked,
          currency: Option[String] @unchecked,
          remote: Boolean,
          location: String,
          country: Option[String] @unchecked,
          tags: Option[List[String]] @unchecked,
          image: Option[String] @unchecked,
          seniority: Option[String] @unchecked,
          other: Option[String] @unchecked,
          active: Boolean
        ) =>
      Job(
        id = id,
        date = date,
        ownerEmail = ownerEmail,
        JobInfo(
          company = company,
          title = title,
          description = description,
          externalUrl = externalUrl,
          salaryLo = salaryLo,
          salaryHi = salaryHi,
          currency = currency,
          remote = remote,
          location = location,
          country = country,
          tags = tags,
          image = image,
          seniority = seniority,
          other = other
        ),
        active = active
      )
  }

  def apply[F[_]: MonadCancelThrow](xa: Transactor[F]): F[LiveJobs[F]] =
    new LiveJobs[F](xa).pure[F]
}
