name := "sagfs"
version := "0.1.0"
scalaVersion := "2.12.6"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "0.10.1",
  "org.http4s" %% "http4s-dsl" % "0.18.12",
  "org.eclipse.jgit" % "org.eclipse.jgit.http.server" % "4.11.0.201803080745-r"
)
