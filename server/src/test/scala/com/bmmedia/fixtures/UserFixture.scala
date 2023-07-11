package com.bmmedia.fixtures

import com.bmmedia.jobsboard.domain.user.*
import cats.implicits.*
import com.bmmedia.jobsboard.core.Users
import cats.effect.IO
import com.bmmedia.jobsboard.domain.auth.*

trait UserFixture {
// Create a user case class based on the Sql statement above

  val email  = "john.doe@example.com"
  val email2 = "bert.meeuws@outlook.com"

  val user = User(email, "John", "Doe", "password123", Role.ADMIN, "BM Media".some)
  val user2 =
    User(email2, "Bert", "Beukers", "password123456789", Role.RECRUITER, "Ziverge".some)

  val user1Credentials = Credentials("john.doe@example.com", "password123")

  val mockedUsersAlgebra = new Users[IO] {
    override def create(user: User): IO[User] = IO.pure(user)
    override def update(user: User): IO[Option[User]] =
      if (user.email == email) IO.pure(user.some) else IO.pure(none)
    override def find(email: String): IO[Option[User]] =
      if (email == user.email) IO.pure(user.some) else IO.pure(none)
    override def delete(email: String): IO[Int] =
      if (email == user.email) IO.pure(1) else IO.pure(0)
  }
}
