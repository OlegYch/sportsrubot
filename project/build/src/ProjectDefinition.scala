import sbt._
import de.element34.sbteclipsify._

class ProjectDefinition(info: ProjectInfo) extends DefaultProject(info) with Eclipsify with IdeaProject {
  // compiler options
  override def compileOptions = Unchecked :: ExplainTypes :: super.compileOptions.toList

  // dependencies

  // pircbot isn't in a repository, sigh
  // val pircbot = "pircbot" % "pircbot" % "1.5.0"
}
