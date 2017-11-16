package com.asb.cats

import cats._
import cats.data._
import cats.free.Free
import cats.implicits._
import com.asb.cats.domains.CSVIODomain.{CSVIO, CSVIOs}
import com.asb.cats.domains.CSVProcessorDomain.{CSVProcessor, CSVProcessors}
import com.asb.cats.domains.FileIODomain.{FileIO, FileIOs}
import com.asb.cats.domains.LoggerDomain.{LogAction, LogActions}
import com.asb.cats.interpreters.par.{CSVIOInterpreter, CSVProcessorInterpreter, FileInterpreter, LoggerInterpreter}
import com.asb.cats.programs.CSVReader

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object Program {

  type T1[A] = EitherK[FileIO, CSVIO, A]
  type T[A] = EitherK[CSVProcessor, T1, A]

  val i1: T1 ~> Future = FileInterpreter or CSVIOInterpreter
  val i2: T ~> Future = CSVProcessorInterpreter or i1

  val i: LogAction ~> Future = LoggerInterpreter

  def printer(str: Stream[String])(implicit L: LogActions[LogAction]): Free[LogAction, Stream[Unit]] = for {
    res <- L.infos(str)
  } yield res

  def main(args: Array[String]): Unit = {
    implicit val CI: CSVProcessors[T] = CSVProcessors[T]
    implicit val CP: CSVIOs[T] = CSVIOs[T]
    implicit val F: FileIOs[T] = FileIOs[T]
    implicit val L: LogActions[LogAction] = LogActions[LogAction]

    val asbFuture = CSVReader.read("asb.csv").foldMap(i2)
    val searchFuture = CSVReader.read("search.csv").foldMap(i2)

    val strings = for {
      asb <- asbFuture
      search <- searchFuture
    } yield asb ++ search

    val results = strings.map(printer)
      .map(_.foldMap(i))

    Await.result(results, 62 millis)
  }

}
