package com.bmmedia.jobsboard.core

import com.bmmedia.jobsboard.domain.job.*
import java.util.UUID
import cats.*
import cats.implicits.*
import doobie.implicits.*
import doobie.*
import doobie.util.*
import doobie.postgres.implicits.*
import cats.effect.MonadCancelThrow

// Core is the same as algebra
trait Jobs[F[_]] {
  def create(ownerEmail: String, jobInfo: JobInfo): F[UUID]
  def update(id: String, jobInfo: JobInfo): F[Option[Job]]
  def find(id: String): F[Option[Job]]
  def delete(id: String): F[Int]
  def findAll(): F[List[Job]]
}

class LiveJobs[F[_]: MonadCancelThrow] private (xa: Transactor[F]) extends Jobs[F] {
  def create(ownerEmail: String, jobInfo: JobInfo): F[UUID] =
    sql"INSERT INTO jobs (id, date, owner_email, job_info, active) VALUES (${UUID
        .randomUUID()}, ${System.currentTimeMillis()}, $ownerEmail, $jobInfo, true) RETURNING id)"
      .query[UUID]
      .unique
      .transact(xa)
  def update(id: String, jobInfo: JobInfo): F[Option[Job]] = ???
  def find(id: String): F[Option[Job]]                     = ???
  def delete(id: String): F[Int]                           = ???
  def findAll(): F[List[Job]]                              = ???
}

object LiveJobs {
  def apply[F[_]: MonadCancelThrow](xa: Transactor[F]): F[LiveJobs[F]] =
    new LiveJobs[F](xa).pure[F]
}
