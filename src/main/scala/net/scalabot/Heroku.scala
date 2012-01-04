package net.scalabot

import util.Properties
import java.net.{InetSocketAddress, ServerSocket}

object Heroku {
  def init(port: Option[String] = Properties.envOrNone("PORT")) {
    port.foreach {port =>
      new ServerSocket().bind(new InetSocketAddress("0.0.0.0", port.toInt))
    }
  }

  def main(s: Array[String]) {
    init(Some("8080"))
    Console.readLine()
  }
}