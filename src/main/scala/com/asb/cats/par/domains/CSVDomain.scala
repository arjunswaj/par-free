package com.asb.cats.par.domains

import java.io.{Reader, Writer}

import cats.InjectK
import cats.free.Free
import cats.free.Free.inject
import org.apache.commons.csv.CSVRecord

object CSVDomain {

  sealed trait CSVIO[A]

  case class ReadCSV(reader: Reader) extends CSVIO[Stream[CSVRecord]]

  case class WriteCSV(writer: Writer, records: Stream[Iterable[String]]) extends CSVIO[Unit]

  class CSVIOs[F[_]](implicit I: InjectK[CSVIO, F]) {

    def readCSV(reader: Reader): Free[F, Stream[CSVRecord]] =
      inject(ReadCSV(reader))

    def writeCSV(writer: Writer, records: Stream[Iterable[String]]): Free[F, Unit] =
      inject(WriteCSV(writer, records))

  }

  object CSVIOs {
    implicit def apply[F[_]](implicit I: InjectK[CSVIO, F]): CSVIOs[F] = new CSVIOs[F]
  }

}
