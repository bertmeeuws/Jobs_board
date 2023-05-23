package com.bmmedia.foundations

import scala.math.Integral.Implicits.infixIntegralOps

object Cats {

  trait MyFunctor[F[_]] {
    def map[A, B](initialValue: F[A])(f: A => B): F[B]
  }

  import cats.Functor
  import cats.instances.list.*

  val listFunctor = Functor[List]

  val mappedList = listFunctor.map(List(1, 2, 3))(_ * 2)

  def increment[F[_]](container: F[Int])(using functor: Functor[F]): F[Int] =
    functor.map(container)(_ + 1)

    import cats.syntax.functor.*

    def increment2[F[_]](container: F[Int])(using functor: Functor[F]): F[Int] =
      container.map(_ + 1)

    // Applicative - pure, wrap existing values into wrapper values
    trait MyApplicative[F[_]] extends MyFunctor[F] {
      def pure[A](value: A): F[A]
    }

    import cats.Applicative
    val applicativeList = Applicative[List]

    val aSimpleList = applicativeList.pure(42)

    import cats.syntax.applicative.*

    val aSimpleList_v2 = 42.pure[List]

    // FlatMap
    trait MyFlatMap[F[_]] extends Functor[F] {
      def flatMap[A, B](initialValue: F[A])(f: A => F[B]): F[B]
    }

    import cats.FlatMap

    val flatMapList    = FlatMap[List]
    val flatMappedList = flatMapList.flatMap(List(1, 2, 3))(x => List(x, x + 1))

    import cats.syntax.flatMap.*

    def crossProduct[F[_]: FlatMap, A, B](containerA: F[A], containerB: F[B]): F[(A, B)] = {
      containerA.flatMap(a => containerB.map(b => (a, b)))
    }

    def crossProduct_v2[F[_]: FlatMap, A, B](containerA: F[A], containerB: F[B]): F[(A, B)] = {
      for {
        a <- containerA
        b <- containerB
      } yield (a, b)
    }

    // Monad
    trait MyMonad[F[_]] extends MyApplicative[F] with MyFlatMap[F] {
      override def map[A, B](initialValue: F[A])(f: A => B): F[B] =
        flatMap(initialValue)(a => pure(f(a)))
    }

    import cats.Monad

    val monadList    = Monad[List]
    val extendedList = monadList.map(List(1, 2, 3))(_ + 2)

    def crossProduct_v3[F[_]: Monad, A, B](containerA: F[A], containerB: F[B]): F[(A, B)] = {
      for {
        a <- containerA
        b <- containerB
      } yield (a, b)
    }

    trait ApplicativeError[F[_], E] extends Applicative[F] {
      def raiseError[A](error: E): F[A]
    }

    import cats.ApplicativeError

    type ErrorOr[A] = Either[String, A]
    val applicativeEither          = ApplicativeError[ErrorOr, String]
    val desiredValue: ErrorOr[Int] = applicativeEither.pure(42)
    val anError: ErrorOr[Int]      = applicativeEither.raiseError("Something went wrong")

    import cats.syntax.applicativeError.*
    val failedValue_v2: ErrorOr[Int] = "Something went wrong".raiseError[ErrorOr, Int]

    trait MyMonadError[F[_], E] extends Monad[F] with ApplicativeError[F, E]

    import cats.MonadError

    val monadErrorEither = MonadError[ErrorOr, String]

  def main(args: Array[String]): Unit = {
    Unit
  }
}
