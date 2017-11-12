package com.asb.cats.domains

import java.io._
import java.nio.file.Path

import cats.InjectK
import cats.free.Free
import cats.free.Free.inject

object FileIODomain {

  sealed trait FileIO[A]

  case class CreateTempFile(filename: String, ext: String) extends FileIO[Path]

  case class GetFilePath(filename: String) extends FileIO[Path]

  case class GetBufferedReader(path: Path) extends FileIO[BufferedReader]

  case class Close(closeable: Closeable) extends FileIO[Unit]

  case class GetBufferedWriter(path: Path) extends FileIO[BufferedWriter]


  class FileIOs[F[_]](implicit I: InjectK[FileIO, F]) {

    def createFilePath(filename: String, ext: String): Free[F, Path] =
      inject(CreateTempFile(filename, ext))

    def getFilePath(string: String): Free[F, Path] =
      inject(GetFilePath(string))

    def getBufferedReader(path: Path): Free[F, BufferedReader] =
      inject(GetBufferedReader(path))

    def close(closeable: Closeable): Free[F, Unit] =
      inject(Close(closeable))

    def getBufferedWriter(path: Path): Free[F, BufferedWriter] =
      inject(GetBufferedWriter(path))
  }

  object FileIOs {
    implicit def apply[F[_]](implicit I: InjectK[FileIO, F]): FileIOs[F] = new FileIOs[F]
  }

}
