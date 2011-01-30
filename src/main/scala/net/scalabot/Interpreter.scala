package net.scalabot

import collection.immutable.TreeMap
import org.apache.commons.lang.StringUtils
import collection.JavaConversions._

/**
 * @author OlegYch
 */
trait Interpreter {
  type SessionId = Int
  var currentSession = 0
  var interpreters = TreeMap(currentSession -> newInterpreter)

  case class Command(name: String, f: (List[String]) => Seq[String]) {
    def matches(s: String) = s.startsWith(commandString)

    def commandString = "!" + name

    def commandHelp = ""
  }

  val Numbers = "^(\\d+)$".r
  val commands = List(
    new Command("help", _ => help) {
      override def commandHelp = "this command"
    },
    new Command("new", _ => newSession) {
      override def commandHelp = "starts new session"
    },
    new Command("resume", _ match {
      case Numbers(id) :: Nil => resumeSession(id.toInt)
      case s :: Nil => List("Not a session id (%s), current session id is %s".format(s, currentSession))
      case _ => List("Need session id as a parameter, current session id is %s".format(currentSession))
    }) {
      override def commandHelp = "resumes previously created session, expects a string parameter"
    }
  )

  def interpret(command: String): Seq[String] = {
    commands.find(_ matches command) match {
      case Some(Command(_, f)) => f(StringUtils.split(command).drop(1).toList)
      case None => interpreters(currentSession).interpretCode(command)
    }
  }

  def help: List[String] = "Executes scala code" :: commands.map(c => c.commandString + " " + c.commandHelp)

  def newSession: List[String] = {
    val oldSession = currentSession
    currentSession = interpreters.lastKey + 1
    interpreters += ((currentSession, newInterpreter))
    changeSessionMessage(oldSession)
  }

  def resumeSession(id: SessionId): List[String] = {
    interpreters.get(id) match {
      case Some(_) => {
        val oldSession = currentSession
        currentSession = id;
        changeSessionMessage(oldSession)
      }
      case None => List("No session with id %s, current session id is %s".format(id, currentSession))
    }
  }

  def newInterpreter: SimplyscalaInterpreter = {
    new SimplyscalaInterpreter {}
  }

  def changeSessionMessage(oldSession: Int): List[String] = {
    List("Old session id %s".format(oldSession), "New session id %s".format(currentSession))
  }

}