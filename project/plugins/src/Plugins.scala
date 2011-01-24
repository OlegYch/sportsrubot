import sbt._

class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
  // adds "eclipse" command to generate an eclipse .classpath and .project from the sbt project definition
  lazy val eclipsify = "de.element34" % "sbt-eclipsify" % "0.7.0"

  // adds "idea" command to generate idea project definition from sbt project definition
  lazy val sbtIdeaRepo = "sbt-idea-repo" at "http://mpeltonen.github.com/maven/"
  lazy val sbtIdea = "com.github.mpeltonen" % "sbt-idea-plugin" % "0.2.0"
}