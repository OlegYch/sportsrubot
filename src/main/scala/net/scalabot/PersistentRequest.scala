package net.scalabot

import java.net.HttpURLConnection
import collection.JavaConversions._
import scalaj.http.Http._

/**
 * @author OlegYch
 */
trait PersistentRequest {
  var cookie = ""

  def perform[T](req: Request, processor: HttpURLConnection => T) = {
    req.header("Cookie", cookie).process(conn => {
      this.cookie = conn.getHeaderFields().getOrElse("Set-Cookie", asJavaList(List(cookie))).mkString("; ")
      processor(conn)
    })
  }
}
