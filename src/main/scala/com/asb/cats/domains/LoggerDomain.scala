package com.asb.cats.domains

import cats.InjectK
import cats.free.Free
import cats.free.Free.inject
import cats.implicits._

object LoggerDomain {

  sealed trait LogAction[A]

  case class Info(msg: String) extends LogAction[Unit]

  case class Error(msg: String, throwable: Throwable) extends LogAction[Unit]

  class LogActions[F[_]](implicit I: InjectK[LogAction, F]) {

    type LogF[A] = Free[F, A]

    def info(msg: String): LogF[Unit] =
      inject(Info(msg))

    def error(msg: String, throwable: Throwable): LogF[Unit] =
      inject(Error(msg, throwable))

    def infos(msgs: Stream[String]): LogF[Stream[Unit]] =
      msgs.map(msg => info(msg))
        .sequence[LogF, Unit]

    def errors(msgs: Stream[(String, Throwable)]): LogF[Stream[Unit]] =
      msgs.map(msg => error(msg._1, msg._2))
        .sequence[LogF, Unit]

  }

  object LogActions {
    def apply[F[_]](implicit I: InjectK[LogAction, F]): LogActions[F] = new LogActions[F]
  }

}
