package io.github.portfoligno.sagfs

import java.nio.file.Path

import cats.effect.Effect
import fs2.Stream
import io.github.portfoligno.sagfs.impl.RemoteGitStorage
import io.github.portfoligno.sagfs.settings.GitStorageSettings

import scala.concurrent.ExecutionContext
import scala.language.higherKinds

trait GitStorage[F[_]] {
  def put(path: Path, bytes: Stream[F, Byte])(implicit settings: GitStorageSettings): F[Unit]

  def get(path: Path): Stream[F, Byte]
}

object GitStorage {
  def apply[F[_] : Effect](uri: String, branch: String)(implicit ec: ExecutionContext): GitStorage[F] =
    new RemoteGitStorage[F](uri, branch)
}
