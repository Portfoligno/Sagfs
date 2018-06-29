name := "sagfs"
version := "0.1.0"
scalaVersion := "2.12.6"

resolvers += "jitpack" at "https://jitpack.io"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "1.0.0-RC2",
  "org.http4s" %% "http4s-dsl" % "0.18.12",
  "org.eclipse.jgit" % "org.eclipse.jgit.http.server" % "4.11.0.201803080745-r",
  "com.google.guava" % "guava" % "25.1-jre",
  "com.github.portfoligno" % "scala-relative-path" % "0.3.0"
)
