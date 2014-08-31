import sbt._
import Keys._
//import com.typesafe.startscript.StartScriptPlugin

object ScalabotBuild extends Build {
  def libs(scalaVersion: String) = Seq(
    "org.scala-lang" % "scala-compiler" % scalaVersion,
    "org.javassist" % "javassist" % "3.14.0-GA",
    "pircbot" % "pircbot" % "1.5.0",
    "org.fluentlenium" % "fluentlenium-core" % "0.9.0",
    commons('io, "2.2"),
    commons('pool, "1.5.5"),
    commons('lang, "2.5")
  )

  def commons(name: Symbol, version: String) = {
    val id = "commons-" + name.toString.drop(1)
    id % id % version
  }

  lazy val root = Project(id = "scalabot",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      scalaVersion := "2.11.2",
      libraryDependencies <<= (scalaVersion)(libs)
    )
//      ++ StartScriptPlugin.startScriptForClassesSettings
  )
}