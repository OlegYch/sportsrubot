import sbt._
import Keys._

object ScalabotBuild extends Build {
  def libs(scalaVersion: String) = Seq(
    "org.scala-lang" % "scala-compiler" % scalaVersion,
    "net.databinder" %% "dispatch-http" % "0.8.7",
    "org.scala-tools.testing" %% "scalacheck" % "1.9",
    "org.scalaz" %% "scalaz-core" % "6.0.3",
    "org.scalaj" %% "scalaj-http" % "0.2.9",
    "org.javassist" % "javassist" % "3.14.0-GA",
    "pircbot" % "pircbot" % "1.5.0",
    commons('io, "1.4"),
    commons('pool, "1.5.5"),
    commons('lang, "2.5")
  )

  def commons(name: Symbol, version: String) = {
    val id = "commons-" + name.toString.drop(1)
    id % id % version
  }

  lazy val root = Project(id = "scalabot",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(libraryDependencies <<= (scalaVersion)(libs)))
}