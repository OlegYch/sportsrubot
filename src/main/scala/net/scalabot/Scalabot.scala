package net.scalabot

import org.jibble.pircbot.PircBot
object Scalabot extends PircBot with Interpreter {
  def main(args: Array[String]) {
    setName("scalabot")
    setVerbose(true)
    setEncoding("UTF-8")
    connect()
  }

  def connect() {
    connect("irc.tut.by", 6667)
    //    identify("password")
    joinChannel("#java")
  }

  override def onDisconnect {
    while (true) {
      try {
        connect()
        return
      }
      catch {
        case e: Exception =>
          e.printStackTrace
          Thread sleep 30000
      }
    }
  }

  override def onPrivateMessage(sender: String, login: String, hostname: String, message: String) {
    onMessage(sender, sender, login, hostname, getName() + ": " + message)
  }

  override def onMessage(channel: String, sender: String, login: String, hostname: String, message: String) {
    import java.io._
    import java.net._
    println("Channel = " + channel)
    if (sender == "lambdabot" || sender == "lambdac") {
      return
    }
    if (message.startsWith(getName() + ": ")) {
      val interpreted = interpret(message.substring((getName + ": ").length))
      if (interpreted isEmpty) {
        //retry
        onMessage(channel = channel, sender = sender, login = login, hostname = hostname, message = message)
      } else {
        interpreted.foreach(sendMessage(channel, _))
      }
    }
    if (message.contains(" #") || message.startsWith("#") && !getUsers(channel).exists(_.getNick == "scala-tracbot")) {
      val number = message.replaceAll("^[^#]*#", "").replaceAll("([^0-9].*)?", "")
      if (number.length >= 3) {
        val url = "http://lampsvn.epfl.ch/trac/scala/ticket/" + number
        if (!url.endsWith("/")) {
          sendMessage(channel, "Perhaps you meant " + url)
        }
      }
    }

    if (message.contains(" r") || message.startsWith("r")) {
      val number = message.replaceAll("^[^r]*r", "").replaceAll("([^0-9].*)?", "")
      if (number.length >= 5) {
        val url = "http://lampsvn.epfl.ch/trac/scala/changeset/" + number
        if (!url.endsWith("/")) {
          sendMessage(channel, "That revision can be found at " + url)
        }
        val reader = new BufferedReader(new InputStreamReader(new URL(url).openConnection.getInputStream, "UTF-8"))
        var line = reader.readLine
        while (line != null) {
          println(line)
          line = reader.readLine
        }
      }
    }
  }
}
