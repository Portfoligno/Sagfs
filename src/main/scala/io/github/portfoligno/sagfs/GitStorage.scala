package io.github.portfoligno.sagfs

import fs2.Stream
import io.github.portfoligno.sagfs.impl.GitStorageImpl
import org.http4s.dsl.impl.Path

import scala.language.higherKinds

trait GitStorage[F[_]] {
  def put(path: Path, bytes: Stream[F, Byte]): F[Unit]

  def get(path: Path): Stream[F, Byte]
}

object GitStorage {
  def apply[F[_]](uri: String, branch: Option[String] = None): GitStorage[F] =
    new GitStorageImpl[F](uri, branch)
}
