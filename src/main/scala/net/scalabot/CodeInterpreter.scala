package net.scalabot

trait CodeInterpreter {
  def interpretCode(message: Message): Seq[String]
}