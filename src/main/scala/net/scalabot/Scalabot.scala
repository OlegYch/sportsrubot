package net.scalabot

import org.jibble.pircbot.{NickAlreadyInUseException, PircBot}
import util.control.Exception._

object Scalabot extends Interpreter {

  class Bot extends PircBot {
    def connectWithName(name: String) {
      setName(name)
      connect("irc.tut.by", 6667)
      //    identify("password")
      joinChannel("#java")
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
      setEncoding("UTF-8")

      override def onDisconnect = if (!ignoreDisconnect) {
        tryConnect()
      }
    }
  }

  @volatile
  var ignoreDisconnect = false

  def tryConnect(name: String = "scalabot", delay: Int = 1000) {
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

  trait MessagesHandler {
    self: PircBot =>

    override def onPrivateMessage(sender: String, login: String, hostname: String, message: String) {
      this.onMessage(sender, sender, login, hostname, getName() + ": " + message)
    }

    override def onMessage(channel: String, sender: String, login: String, hostname: String, message: String) {
      println("Channel = " + channel)
      if (sender == "lambdabot" || sender == "lambdac") {
        return
      }
      if (message.startsWith(getName() + ": ")) {
        val interpreted = interpret(message.substring((getName + ": ").length))
        interpreted.foreach(sendMessage(channel, _))
      }
    }
  }

}
