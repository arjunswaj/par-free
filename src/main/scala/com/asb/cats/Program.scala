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

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

object Program {

  type T1[A] = EitherK[FileIO, CSVIO, A]
  type T2[A] = EitherK[CSVProcessor, T1, A]
  type T[A] = EitherK[LogAction, T2, A]

  val i1: T1 ~> Future = FileInterpreter or CSVIOInterpreter
  val i2: T2 ~> Future = CSVProcessorInterpreter or i1
  val i: T ~> Future = LoggerInterpreter or i2

  def program(filename: String)(implicit CI: CSVProcessors[T], CP: CSVIOs[T], F: FileIOs[T], L: LogActions[T]): Free[T, Stream[Unit]] = for {
    path <- F.getFilePath(filename)
    reader <- F.getBufferedReader(path)
    records <- CP.readCSV(reader)
    strings <- CI.stringifyRecords(records)
    res <- L.infos(strings)
    _ <- F.close(reader)
  } yield res

  def main(args: Array[String]): Unit = {
    implicit val CI: CSVProcessors[T] = CSVProcessors[T]
    implicit val CP: CSVIOs[T] = CSVIOs[T]
    implicit val F: FileIOs[T] = FileIOs[T]
    implicit val L: LogActions[T] = LogActions[T]

    val result = program("asb.csv").foldMap(i)
    val search = program("search.csv").foldMap(i)

    result.onComplete {
      case Success(_) => println("ASB Success")
      case Failure(exception) => exception.printStackTrace()
    }

    search.onComplete {
      case Success(_) => println("Search Success")
      case Failure(exception) => exception.printStackTrace()
    }

    Await.result(result, 1000 millis)
    Await.result(search, 5000 millis)
  }

}
