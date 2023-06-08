package com.bmmedia.jobsboard.core

import com.bmmedia.jobsboard.domain.user.*
import doobie.util.transactor.Transactor

object User {

  trait Users[F[_]] {
    def create(user: User): F[User]
    def update(user: User): F[Option[User]]
    def find(email: String): F[Option[User]]
    def delete(email: String): F[Int]
  }

  class LiveUsers[F[_]] private (xa: Transactor[F]) extends Users[F] {
    override def create(user: User): F[User]          = ???
    override def update(user: User): F[Option[User]]  = ???
    override def find(email: String): F[Option[User]] = ???
    override def delete(email: String): F[Int]        = ???
  }
}
