package com.asb.cats.domains

import cats.InjectK
import cats.free.Free
import cats.free.Free.inject
import cats.implicits._
import org.apache.commons.csv.CSVRecord

object CSVProcessorDomain {

  sealed trait CSVProcessor[A]

  case class Stringify(csvRecord: CSVRecord) extends CSVProcessor[String]

  class CSVProcessors[F[_]](implicit I: InjectK[CSVProcessor, F]) {

    type CSVProcessorsF[A] = Free[F, A]

    def stringify(csvRecord: CSVRecord): Free[F, String] =
      inject(Stringify(csvRecord))

    def stringifyRecords(csvRecords: Stream[CSVRecord]): Free[F, Stream[String]] =
      csvRecords.map(rec => stringify(rec))
        .sequence[CSVProcessorsF, String]

  }

  object CSVProcessors {
    def apply[F[_]](implicit I: InjectK[CSVProcessor, F]): CSVProcessors[F] = new CSVProcessors[F]
  }

}
