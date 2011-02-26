import sbt._
import de.element34.sbteclipsify._

class ProjectDefinition(info: ProjectInfo) extends DefaultProject(info) with Eclipsify with IdeaProject {
  // compiler options
  override def compileOptions = Unchecked :: ExplainTypes :: super.compileOptions.toList

  // dependencies

  // pircbot isn't in a repository, sigh
  // val pircbot = "pircbot" % "pircbot" % "1.5.0"

  override def libraryDependencies = Set(
    "org.scalaj" % "scalaj-http_2.8.0" % "0.2" withSources,
    "org.javassist" % "javassist" % "3.14.0-GA" withSources,
    commons('io, "1.4"),
    commons('pool, "1.5.5"),
    commons('lang, "2.5")
  )

  def commons(name: Symbol, version: String) = {
    val id = "commons-" + name.toString.drop(1)
    id % id % version withSources
  }
}
