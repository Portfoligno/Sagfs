package io.github.portfoligno.sagfs.impl

import java.io.File
import java.nio.file.Path
import java.time.ZonedDateTime

import cats.effect.Effect
import cats.syntax.applicativeError._
import cats.syntax.apply._
import cats.syntax.either._
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.google.common.io.Files
import fs2.Stream
import fs2.async.once
import fs2.io.file.{readAllAsync, writeAllAsync}
import io.github.portfoligno.sagfs.GitStorage
import io.github.portfoligno.sagfs.settings.GitStorageSettings
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ResetCommand.ResetType
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.transport.RefSpec

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext
import scala.language.higherKinds

class RemoteGitStorage[F[_]](uri: String, branch: String)
  (implicit F: Effect[F], ec: ExecutionContext) extends GitStorage[F] {
  import F._

  private
  val branchRef = branch match {
    case "HEAD" => "HEAD"
    case _ => s"refs/heads/$branch"
  }

  // Clone and memoize the path
  private
  val _clone: F[F[File]] = once(
    delay(Files.createTempDir())
      .flatMap(directory => delay {
        Git
          .cloneRepository()
          .setDirectory(directory)
          .setURI(uri)
          .setNoCheckout(true)
          .call()
          .close()
        directory
      })
  )

  private
  def getRemoteHash(git: Git): F[Option[String]] = delay {
    git
      .getRepository
      .getRefDatabase
      .getRefs(Constants.R_REMOTES)
      .asScala
      .get(branchRef)
      .map(_.getObjectId.getName)
  }

  // Fetch and reset
  private
  def fetchReset(git: Git, allowEmpty: Boolean): F[Unit] = for {
    hash <- getRemoteHash(git)

    _ <- hash match {
      case Some(h) =>
        delay(git.fetch().setRefSpecs(branchRef).call()) *>
          delay(git.reset().setMode(ResetType.HARD).setRef(h).call())

      case None if allowEmpty =>
        unit

      case _ =>
        raiseError(new IllegalArgumentException(s"Branch not found: $branch"))
    }
  }
    yield ()

  // Add, commit and push
  private
  def addCommitPush(git: Git, path: Path)
    (implicit settings: GitStorageSettings): F[Unit] = {
    val add = delay {
      git
        .add()
        .addFilepattern(".")
        .call()
    }
    val commit = delay {
      import settings._

      git
        .commit()
        .setMessage(s"Update $path at ${ZonedDateTime.now()}")
        .setCommitter(committer.name, committer.email)
        .call()
    }
    val push = delay {
      git
        .push()
        .setRefSpecs(new RefSpec(s"refs/heads/master:$branchRef"))
        .call()
    }
    add *> commit *> push *> unit
  }

  // Only relative paths are relevant
  private
  def checkIsRelative(path: Path): F[Unit] = delay(require(!path.isAbsolute, path))

  // Resource handling
  private
  def open(directory: File): F[Git] = delay(Git.open(directory))
  private
  def close(git: Git): F[Unit] = delay(git.close())

  // Write bytes
  override
  def put(path: Path, bytes: Stream[F, Byte])
    (implicit settings: GitStorageSettings): F[Unit] = for {
    directory <- checkIsRelative(path) *> _clone.flatten
    absolutePath = directory.toPath.resolve(path)

    actions = { git: Git =>
      fetchReset(git, allowEmpty = true) *> // Update local files
        bytes.to(writeAllAsync(absolutePath)).compile.drain *> // Write all bytes
        addCommitPush(git, path) // Push
    }
    attemptActions = { git: Git =>
      // Retry infinitely by now
      git.tailRecM(actions.andThen(_.attempt.map(_.leftMap { e => e.printStackTrace(); git })))
    }
    _ <- bracket(open(directory))(attemptActions)(close) // Resource handling
  }
    yield ()

  // Read bytes
  override
  def get(path: Path): Stream[F, Byte] = for {
    directory <- Stream.eval(checkIsRelative(path) *> _clone.flatten)
    absolutePath = directory.toPath.resolve(path)

    actions = { git: Git =>
      Stream.eval(fetchReset(git, allowEmpty = false)) // Update local files
        .flatMap(_ => readAllAsync(absolutePath, 1024)) // Read all bytes
    }
    bytes <- Stream.bracket(open(directory))(actions, close) // Resource handling
  }
    yield bytes
}
