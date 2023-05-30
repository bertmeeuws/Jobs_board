package com.bmmedia.jobsboard.config

import pureconfig.ConfigSource
import cats.MonadThrow
import pureconfig.ConfigReader
import cats.implicits.*
import pureconfig.error.ConfigReaderException
import scala.reflect.ClassTag

object syntax {
  extension (source: ConfigSource)
    def loadF[F[_], A](using
        reader: pureconfig.ConfigReader[A],
        F: MonadThrow[F],
        tag: ClassTag[A]
    ): F[A] =
      F.pure(source.load[A]).flatMap { value =>
        value match {
          case Left(error)  => F.raiseError[A](ConfigReaderException(error))
          case Right(value) => F.pure(value)
        }
      }
}
