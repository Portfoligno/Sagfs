package io.github.portfoligno.sagfs.impl

import fs2.Stream
import io.github.portfoligno.sagfs.GitStorage
import org.http4s.dsl.impl.Path

import scala.language.higherKinds

class GitStorageImpl[F[_]](uri: String, branch: Option[String]) extends GitStorage[F] {
  override
  def put(path: Path, bytes: Stream[F, Byte]): F[Unit] = ???

  override
  def get(path: Path): Stream[F, Byte] = ???
}
