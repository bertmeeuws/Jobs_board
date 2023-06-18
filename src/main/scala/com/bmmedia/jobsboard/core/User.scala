package com.bmmedia.jobsboard.core

import com.bmmedia.jobsboard.domain.user.*
import doobie.util.transactor.Transactor
import doobie.implicits.*
import doobie.*
import doobie.util.*
import cats.effect.MonadCancelThrow
import cats.*
import cats.implicits.*
import doobie.postgres.implicits.*

trait Users[F[_]] {
  def create(user: User): F[User]
  def update(user: User): F[Option[User]]
  def find(email: String): F[Option[User]]
  def delete(email: String): F[Int]
}

class LiveUsers[F[_]: MonadCancelThrow] private (xa: Transactor[F]) extends Users[F] {
  override def create(user: User): F[User] = {
    val User(email, firstName, lastName, password, role, company, url) = user

    val fragment = sql"""
      INSERT INTO public.users (
      email,
      firstname, 
      lastname, 
      password, 
      company,
      role,
      createdat) VALUES (
      $email,
      $firstName, 
      $lastName, 
      $password, 
      ${company.getOrElse("")},
      USER,
      ${System.currentTimeMillis()}
      )
    """

    fragment.update.run
      .transact(xa)
      .as(user)
  }

  override def update(user: User): F[Option[User]] = {
    val User(email, firstName, lastName, password, company, role, url) = user

    sql"""
    UPDATE public.users SET firstName = $firstName, lastName = $lastName, password = $password, company = ${company.toString}, role = $role WHERE email = $email""".update.run
      .transact(xa)
      .as(user.some)
  }
  override def find(email: String): F[Option[User]] =
    sql"""SELECT * FROM public.users WHERE email = $email""".query[User].option.transact(xa)
  override def delete(email: String): F[Int] =
    sql"""DELETE FROM public.users WHERE email = $email""".update.run.transact(xa)
}

object LiveUsers {
  def apply[F[_]: MonadCancelThrow](xa: Transactor[F]) = new LiveUsers[F](xa).pure[F]
}
