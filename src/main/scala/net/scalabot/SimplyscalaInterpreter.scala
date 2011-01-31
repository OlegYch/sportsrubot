package net.scalabot

import java.net.HttpURLConnection
import collection.JavaConversions._
import scalaj.http.Http._
import org.apache.commons.lang.StringUtils

/**
 * @author OlegYch
 */
trait SimplyscalaInterpreter {
  var cookie = ""

  def withCookie[T](cookie: String, req: Request)(processor: HttpURLConnection => T) = {
    req.header("Cookie", cookie).process(conn => {
      new {
        val result = processor(conn);
        val newCookie = conn.getHeaderFields().getOrElse("Set-Cookie", asJavaList(List(cookie))).mkString("; ")
      }
    })
  }

  object StaticRequest extends Request(null, null, null, null, null, null)

  def interpretCode(message: String): Seq[String] = {
    val req = withCookie[String](cookie, get("http://www.simplyscala.com/interp").params(("bot", "irc"), ("code", message))) _

    val response = req(conn => StaticRequest.tryParse(conn.getInputStream(), StaticRequest.readString _))
    cookie = response.newCookie

    val result = StringUtils.split(response.result, "\r\n", 3)
    if (result contains ("New interpreter instance being created for you, this may take a few seconds.")) {
      Seq()
    } else {
      result flatMap {
        line =>
          println("line = " + line)
          line match {
            case "warning: there were deprecation warnings; re-run with -deprecation for details" => {
              None
            }
            case "warning: there were unchecked warnings; re-run with -unchecked for details" => {
              Some("Uncheked operations in your code!")
            }
            case line => Some(line)
          }
      }
    }
  }
}