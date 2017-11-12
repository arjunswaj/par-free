package com.asb.cats.interpreters.par

import java.nio.file.{Files, Paths}

import cats.~>
import com.asb.cats.domains.FileIODomain._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

object FileInterpreter extends (FileIO ~> Future) {

  override def apply[A](fa: FileIO[A]): Future[A] = fa match {
    case CreateTempFile(filename, ext) => Future {
      Files.createTempFile(filename, ext)
    }
    case GetFilePath(filename) => Future {
      Paths.get(filename)
    }
    case GetBufferedReader(path) => Future {
      Files.newBufferedReader(path)
    }
    case Close(closeable) => Future {
      closeable.close()
    }
    case GetBufferedWriter(path) => Future {
      Files.newBufferedWriter(path)
    }
  }

}
