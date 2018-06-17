package io.github.portfoligno.sagfs.settings

case class GitStorageSettings(
  committer: Committer = Committer("Git Storage Bot", "sagfs@noreply.portfoligno.github.io")
)
