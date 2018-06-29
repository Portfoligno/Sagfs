package io.github.portfoligno.sagfs

import cats.effect.Effect
import fs2.Stream
import io.github.portfoligno.sagfs.impl.RemoteGitStorage
import io.github.portfoligno.sagfs.settings.GitStorageSettings
import io.github.portfoligno.scala.path.relative.RelativePath

import scala.concurrent.ExecutionContext
import scala.language.higherKinds

trait GitStorage[F[_]] {
  def put(path: RelativePath, bytes: Stream[F, Byte])(implicit settings: GitStorageSettings): F[Unit]

  def get(path: RelativePath): Stream[F, Byte]
}

object GitStorage {
  def apply[F[_] : Effect](uri: String, branch: String)(implicit ec: ExecutionContext): GitStorage[F] =
    new RemoteGitStorage[F](uri, branch)
}
