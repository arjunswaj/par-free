package com.asb.cats.interpreters.par

import cats.~>
import com.asb.cats.domains.LoggerDomain.{Error, Info, LogAction}

import scala.concurrent.Future

object LoggerInterpreter extends (LogAction ~> Future) {

  override def apply[A](fa: LogAction[A]): Future[A] = fa match {
    case Info(msg) => Future.successful({
      println(msg)
    })
    case Error(msg, throwable) => Future.successful({
      Console.err.println(msg)
      throwable.printStackTrace()
    })
  }

}
