package com.asb.cats.interpreters.par

import cats.~>
import com.asb.cats.domains.CSVProcessorDomain.{CSVProcessor, Stringify}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object CSVProcessorInterpreter extends (CSVProcessor ~> Future) {

  import scala.collection.JavaConverters._

  override def apply[A](fa: CSVProcessor[A]): Future[A] = fa match {
    case Stringify(csvRecord) => Future {
      csvRecord.iterator()
        .asScala
        .foldLeft(new StringBuilder())((b, a) => {
          b.append(a).append(", ")
          b
        }).dropRight(2).toString()
    }
  }
}
