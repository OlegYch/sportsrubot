package net.scalabot

import org.fluentlenium.adapter.IsolatedTest
import org.fluentlenium.core.domain.FluentWebElement
import org.jibble.pircbot.{NickAlreadyInUseException, PircBot}
import org.openqa.selenium.WebDriver
import org.openqa.selenium.htmlunit.HtmlUnitDriver

import scala.util.control.Exception._

object Scalabot {

//      val channel = "#football-test"
  val channel = "#football"

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
    currentBot = new Bot {
      bot =>
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
            newNews.takeWhile(!oldNews.contains(_)).reverse.take(5).foreach { m =>
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

  def news: List[String] = deadlineNews
//  def news: List[String] = uadeadlineNews

  def transferNews: List[String] = {
    val f = xml.XML.load(new java.net.URL("http://www.sports.ru/stat/export/rss/taglenta.xml?id=1685207"))
    val articles = f \\ "item"
    val result = articles.flatMap { a =>
      for {
        name <- a \ "title"
        url <- a \ "link"
      } yield s"${name.text} http://${url.text}".replaceAll( """[\r\n]""", "").replaceAllLiterally("&laquo;", "\"").replaceAllLiterally("&raquo;", "\"")
    }
    result.toList
  }

  def withFluentlenium(f: IsolatedTest => List[String]) = {
    val fluent = new org.fluentlenium.adapter.IsolatedTest {
      override def getDefaultDriver: WebDriver = new HtmlUnitDriver()
    }
    try {
      f(fluent)
    } finally fluent.quit()
  }

  def footballbyNews: List[String] = withFluentlenium { f =>
    import scala.collection.JavaConversions._
    f.goTo("http://www.football.by/news/66360.html")
    val articles = f.find(".newstext")
    val result = articles.map(t => (t.getText, t.find("a").map(_.getAttribute("href")), t.find("img").map(_.getAttribute("src")))).map {
      case (text, links, images) => text + " " + links.mkString(" ") + " " + images.mkString(" ")
    }
    result.toList
  }

  def deadlineNews: List[String] = withFluentlenium { f =>
    import scala.collection.JavaConversions._
    f.goTo("http://www.sports.ru/tribuna/blogs/england/826569.html")
    val articles = f.find(".article-textBlock").find("p")
    parseNews(articles)
  }

  def uadeadlineNews: List[String] = withFluentlenium { f =>
    import scala.collection.JavaConversions._
    f.goTo("http://www.ua-football.com/foreign/transfers/1440964889-transfernyy-dedlayn-podrobnyy-onlayn-dvuh-poslednih-dney.html")
    val articles = f.find(".block_text").find("p")
    parseNews(articles)
  }

  def parseNews(articles: Seq[FluentWebElement]) = {
    import scala.collection.JavaConversions._
    case class News(t: FluentWebElement) {
      val hasDate = t.find("strong").nonEmpty
      val links = {
        t.find("a").map(_.getAttribute("href"))
      }
      val images = {
        (t.find("img") ++ t.find("iframe")).map(_.getAttribute("src"))
      }
      val text = t.getText
      override val toString = text + " " + links.mkString(" ") + " " + images.mkString(" ")
    }
    val news = articles.map(t => News(t))
    val imagesResorted = news.zipWithIndex.sortBy { case (n, i) => if (n.images.nonEmpty) i * 2 - 3 else i * 2 }.map(_._1)
    println(imagesResorted.mkString("\n"))
    val result = imagesResorted.collect {
      case n if n.hasDate || n.images.nonEmpty => n.toString
    }
    result.toList
      .filterNot(_.trim.isEmpty)
      .flatMap(s => s.grouped(400).toList.reverse)
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
