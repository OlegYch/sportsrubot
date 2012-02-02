package net.scalabot

import util.parsing.json.{JSON, JSONObject, JSONArray, JSONType}
import dispatch._
import java.io._

trait MultibotInterpreter extends CodeInterpreter with ScalaInterpreter {
  val NUMLINES = 5
  val INNUMLINES = 8

  def interpretCode(message: Message) = {
    serve(Msg(message.sender, message.sender, "login", "hostname", message.message))
  }

  object Cmd {
    def unapply(s: String) = if (s.contains(' ')) {
      Some(s.split(" ", 2).toList)
    } else {
      Some(s :: Nil)
    }
  }

  def serve(msg: Msg): Seq[String] = msg.message match {
    case "@help" => Seq("'! scala'  '% ruby'  ', clojure' '>> haskell'")

    case Cmd("!" :: m :: Nil) => interpretScala(msg, m)

    case Cmd("!paste" :: m :: Nil) => // Http(url(m) >- {source => serve(msg.copy(message = "! " + source))})
      val conOut = new ByteArrayOutputStream
      (new Http with NoLogging)(url(m) >>> new PrintStream(conOut))
      serve(msg.copy(message = "! " + conOut))

    case Cmd("!type" :: m :: Nil) => {
      val (si, cout) = scalaInterpreter(msg.channel, m).determineType
      Seq(si map (_.toString) getOrElse "Failed to determine type.")
    }
    case "!reset" => scalaInt -= msg.channel; Seq()
    case "!reset-all" => scalaInt.clear; Seq()

    case Cmd("!scalex" :: m :: Nil) => respondJSON(msg)(:/("api.scalex.org") <<? Map("q" -> m)) {
      case Some(JSONObject(map)) =>
        map.get("results").map {
          case JSONArray(arr) =>
            Some(arr.map {
              case JSONObject(e) =>
                val JSONObject(p) = e("parent")
                p("name").toString + p("typeParams").toString + " " + e("name") + e("typeParams") + ": " +
                  e("valueParams") + ": " + e("resultType") +
                  e.get("comment").map {
                    case JSONObject(c) => val JSONObject(s) = c("short"); " '" + s("txt") + "'"
                  }.getOrElse("")
            }.mkString("\n"))
        } getOrElse Some(map("error").toString)
      case e => Some("unexpected: " + e)
    }

    case Cmd("!!" :: m :: Nil) => respond(msg)(
      :/("www.simplyscala.com") / "interp" <<? Map("bot" -> "irc", "code" -> m)) {
      case "warning: there were deprecation warnings; re-run with -deprecation for details" |
           "warning: there were unchecked warnings; re-run with -unchecked for details" |
           "New interpreter instance being created for you, this may take a few seconds." |
           "Please be patient." => Seq()
      case line => Seq(line.replaceAll("^res[0-9]+: ", ""))
    }

    case Cmd("," :: m :: Nil) => respondJSON(msg)(:/("try-clojure.org") / "eval.json" <<? Map("expr" -> m)) {
      case Some(JSONObject(map)) => Some(map.get("result").getOrElse(map("message")).toString)
      case e => Some("unexpected: " + e)
    }

    case Cmd(">>" :: m :: Nil) => respondJSON(msg)(
      :/("tryhaskell.org") / "haskell.json" <<? Map("method" -> "eval", "expr" -> m)) {
      case Some(JSONObject(map)) => Some(
        map.get("result").map(_ + " :: " + map("type")).getOrElse(map("error")).toString)
      case e => Some("unexpected: " + e)
    }

    case Cmd("%" :: m :: Nil) => respondJSON(msg)(:/("tryruby.org") / "levels/1/challenges/0" <:<
      Map("Accept" -> "application/json, text/javascript, */*; q=0.01",
        "Content-Type" -> "application/x-www-form-urlencoded; charset=UTF-8",
        "X-Requested-With" -> "XMLHttpRequest",
        "Connection" -> "keep-alive") <<< Seq("cmd" -> m) >\ "UTF-8") {
      case Some(JSONObject(map)) => Some(map("output").toString)
      case e => Some("unexpected" + e)
    }

    case _ => Seq()
  }

  val cookies = scala.collection.mutable.Map[String, String]()

  def respondJSON(msg: Msg)(req: Request)(response: Option[JSONType] => Option[String]): Seq[String] =
    respond(msg)(req)(line => response(JSON.parseRaw(line)).toSeq)

  def respond(msg: Msg)(req: Request)(response: String => Seq[String]): Seq[String] = {
    val Msg(channel, sender, login, hostname, message) = msg
    val host = req.host

    val request = cookies.get(channel + host) map (c => req <:< Map("Cookie" -> c)) getOrElse req

    val handler = request >+> {
      r =>
        r >:> {
          headers =>
            headers.get("Set-Cookie")
              .foreach(h => h.foreach(c => cookies(channel + host) = c.split(";").head))
            r >~ {
              source =>
                source.getLines.take(NUMLINES).flatMap(response(_).flatMap(_.split("\n").take(INNUMLINES)))
                  .toList
            }
        }
    }

    (new Http with NoLogging).apply(handler)
  }
}

case class Msg(channel: String, sender: String, login: String, hostname: String, message: String)