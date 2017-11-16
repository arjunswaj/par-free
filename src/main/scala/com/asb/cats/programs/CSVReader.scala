package com.asb.cats.programs

import cats.data.EitherK
import cats.free.Free
import com.asb.cats.domains.CSVIODomain.{CSVIO, CSVIOs}
import com.asb.cats.domains.CSVProcessorDomain.{CSVProcessor, CSVProcessors}
import com.asb.cats.domains.FileIODomain.{FileIO, FileIOs}

object CSVReader {
  type T1[A] = EitherK[FileIO, CSVIO, A]
  type T[A] = EitherK[CSVProcessor, T1, A]

  def read(filename: String)(implicit CI: CSVProcessors[T], CP: CSVIOs[T], F: FileIOs[T]): Free[T, Stream[String]] = for {
    path <- F.getFilePath(filename)
    reader <- F.getBufferedReader(path)
    records <- CP.readCSV(reader)
    strings <- CI.stringifyRecords(records)
    _ <- F.close(reader)
  } yield strings

}
