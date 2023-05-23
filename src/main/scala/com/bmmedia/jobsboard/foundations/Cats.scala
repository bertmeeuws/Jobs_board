package com.bmmedia.foundations

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
    trait MyFlatMap[F[]] extends Functor[F] {
      def flatMap[A,B](initialValue: F[A])(f: A => F[B]): F[B]
    }

    import cats.FlatMap

    val flatMapList = FlatMap[List]





  def main(args: Array[String]): Unit = {}
}
