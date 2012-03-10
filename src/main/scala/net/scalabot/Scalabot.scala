package net.scalabot

import org.jibble.pircbot.{NickAlreadyInUseException, PircBot}
import util.control.Exception._

object Scalabot extends Interpreter[MultibotInterpreter] {

  Heroku.init()

  class Bot extends PircBot {
    def connectWithName(name: String) {
      setName(name)
      connect("irc.tut.by", 6667)
      //    identify("password")
      joinChannel("#java")
      joinChannel("#programming")
    }
  }

  var currentBot: Bot = _
  val bot = Delegate.of(currentBot)

  import bot._

  def main(args: Array[String]) {
    newBot
    val mainThread = Thread.currentThread
    new Thread {
      override def run = {
        Console.readLine;
        println("Exiting")
        mainThread.interrupt
        killBot
      }

      setDaemon(true)
      start
    }
    tryConnect()
  }

  def killBot: Unit = {
    if (currentBot != null) {
      ignoreDisconnect = true
      handling(classOf[Exception]).by(_.printStackTrace).apply({
        disconnect;
        dispose
      })
    }
  }

  def newBot {
    killBot
    currentBot = new Bot with MessagesHandler {
      setVerbose(true)
      setEncoding("cp1251")

      override def onDisconnect = if (!ignoreDisconnect) {
        tryConnect()
      }
    }
  }

  @volatile
  var ignoreDisconnect = false

  def tryConnect(name: String = "multibot", delay: Int = 1000) {
    ignoreDisconnect = true
    def reconnect(name: String = name) = {
      Thread.sleep(delay)
      newBot
      tryConnect(name, delay * 2 % 100000)
    }
    handling(classOf[NickAlreadyInUseException]).
      by(_ => reconnect(name + "_")).
      or(handling(classOf[Exception]).
      by(e => {
      e.printStackTrace();
      reconnect()
    })).apply(connectWithName(name))
    ignoreDisconnect = false
  }

  trait MessagesHandler extends PircBot {
    override def onPrivateMessage(sender: String, login: String, hostname: String, message: String) {
      onMessage(sender, sender, login, hostname, message)
    }

    override def onMessage(channel: String, sender: String, login: String, hostname: String,
                           message: String) {
      if (sender == "lambdabot" || sender == "lambdac") {
        return
      }
      val interpreted = interpret(Message(channel, message, sender, getUsers(channel).map(_.getNick)))
      interpreted.foreach(sendMessage(channel, _))
    }
  }

  def newInterpreter = new MultibotInterpreter {}
}
