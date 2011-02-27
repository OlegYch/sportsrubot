package net.scalabot

import scalaj.http.Http._
import org.apache.commons.lang.StringUtils

/**
 * @author OlegYch
 */
trait SimplyscalaInterpreter {
  val request = new PersistentRequest {
    def perform(req: Request): String = perform(req, conn => req.tryParse(conn.getInputStream(), req.readString _))
  }

  def interpretCode(message: Message): Seq[String] = {
    interpretCode(Context.set(message))
    interpretCode(message.message)
  }

  def interpretCode(message: String): Seq[String] = {
    val response = request.perform(get("http://www.simplyscala.com/interp").params(("bot", "irc"), ("code", message)))

    val result = StringUtils.split(response, "\r\n", 3)
    if (result startsWith Seq("Please be patient.", "New interpreter instance being created for you, this may take a few seconds.")) {
      interpretCode(message)
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

object Context {
  def set(message: Message) = "val context = " +
      <context>
        <sender>
          {message.sender}
        </sender>{message.users.map(u =>
        <user>
          {u}
        </user>)}
      </context>.toString.replaceAll("\\r|\\n|\\s", "")
}
