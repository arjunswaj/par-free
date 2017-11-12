package com.asb.cats.interpreters.par

import cats.~>
import com.asb.cats.domains.CSVIODomain.{CSVIO, ReadCSV, WriteCSV}
import org.apache.commons.csv.{CSVFormat, CSVPrinter}

import scala.concurrent.Future
import scala.util.Try

object CSVIOInterpreter extends (CSVIO ~> Future) {

  import scala.collection.JavaConverters._

  override def apply[A](fa: CSVIO[A]): Future[A] = fa match {
    case ReadCSV(reader) => Future.fromTry(Try {
      CSVFormat.RFC4180
        .withFirstRecordAsHeader()
        .parse(reader)
        .getRecords.iterator().asScala.toStream
    })

    case WriteCSV(writer, records) => Future.fromTry(Try {
      val csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator("\n")
      val csvFilePrinter = new CSVPrinter(writer, csvFileFormat)
      records.foreach(record => csvFilePrinter.printRecord(record))
    })

  }

}
