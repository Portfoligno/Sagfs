package io.github.portfoligno.sagfs.settings

trait GitStorageDefaults {
  implicit val settings: GitStorageSettings = GitStorageSettings()
}
object GitStorageDefaults extends GitStorageDefaults
