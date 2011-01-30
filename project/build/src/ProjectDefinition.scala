import sbt._
import de.element34.sbteclipsify._

class ProjectDefinition(info: ProjectInfo) extends DefaultProject(info) with Eclipsify with IdeaProject {
  // compiler options
  override def compileOptions = Unchecked :: ExplainTypes :: super.compileOptions.toList

  // dependencies

  // pircbot isn't in a repository, sigh
  // val pircbot = "pircbot" % "pircbot" % "1.5.0"
  val scalahttp = "org.scalaj" % "scalaj-http_2.8.0" % "0.2" withSources
  val c_io = "commons-io" % "commons-io" % "1.4" withSources
  val c_lang = "commons-lang" % "commons-lang" % "2.5" withSources
}
