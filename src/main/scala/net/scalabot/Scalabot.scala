package net.scalabot

import org.jibble.pircbot.{NickAlreadyInUseException, PircBot}
import org.openqa.selenium.WebDriver
import org.openqa.selenium.htmlunit.HtmlUnitDriver

import scala.util.control.Exception._

object Scalabot {

  val channel = "#football-test"
  //  val channel = "#football"

  class Bot extends PircBot {
    def connectWithName(name: String) {
      setName(name)
      connect("irc.bynets.org", 6667)
      //    identify("password")
      joinChannel(channel)
    }
  }

  var currentBot: Bot = _
  val bot = Delegate.of(currentBot)

  import net.scalabot.Scalabot.bot._

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
    currentBot = new Bot {bot =>
      setVerbose(true)
      setEncoding("cp1251")

      override def onDisconnect = if (!ignoreDisconnect) {
        tryConnect()
      }

      new Thread {
        var oldNews = news.drop(1).distinct.reverse

        override def run() {
          while (true) {
            Thread.sleep(10 * 1000)
            val newNews = news
            newNews.takeWhile(!oldNews.contains(_)).reverse.foreach { m =>
              sendMessage(channel, m)
            }
            oldNews ++= newNews
            oldNews = oldNews.distinct
            if (!bot.isConnected) return
          }
        }
        setDaemon(true)
        start()
      }
    }
  }


  def news: List[String] = {
    val f = new org.fluentlenium.adapter.IsolatedTest {
      override def getDefaultDriver: WebDriver = new HtmlUnitDriver()
    }
    try {
      import scala.collection.JavaConversions._
      f.goTo("http://www.sports.ru/football/157318697.html")
      val articles = f.find(".article-textBlock").find("p")
      val result = articles.map(t => (t.getText, t.find("a").map(_.getAttribute("href")), t.find("img").map(_.getAttribute("src")))).map {
        case (text, links, images) => text + " " + links.mkString(" ") + " " + images.mkString(" ")
      }
      println(result.mkString("\n"))
      result.toList
    } finally f.quit()
  }

  @volatile
  var ignoreDisconnect = false

  def tryConnect(name: String = "спортсру", delay: Int = 1000) {
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
}
