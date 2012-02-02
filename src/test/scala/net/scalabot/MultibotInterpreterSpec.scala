package net.scalabot

import org.specs2._

/**
 * will fail in sbt with
 * Exception in thread "Thread-0" java.lang.Error: typeConstructor inapplicable for &lt;none>
 * but works in intellij
 */
class MultibotInterpreterSpec extends Specification {
  def mutlibot: MultibotInterpreter = new MultibotInterpreter {}

  def msg(message: String) = Message(channel = "channel", message = message, sender = "", users = Nil)

  def is = "multibot" ^
    "should execute scala code" ! {
      val code = mutlibot.interpretCode(msg("! 1+1"))
      (code shouldEqual Seq(" res0: Int = 2"))
    } ^
    "should execute scala code with space" ! {
      val code = mutlibot.interpretCode(msg("! 1 + 1 "))
      code mustEqual Seq(" res0: Int = 2")
    } ^
    "should determine type" ! {
      val code = mutlibot.interpretCode(msg("!type Nil"))
      code mustEqual Seq("scala.collection.immutable.Nil.type")
    } ^
    "should execute haskell code" ! {
      val code = mutlibot.interpretCode(msg(">> 1+1"))
      code mustEqual Seq("2 :: (Num t) => t")
    } ^
    "should execute ruby code" ! {
      val code = mutlibot.interpretCode(msg("% 1+1"))
      code mustEqual Seq("=> 2")
    }
}
