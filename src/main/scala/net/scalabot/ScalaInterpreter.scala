package net.scalabot

import java.security.Permission
import java.lang.SecurityException
import java.io.{FilePermission, PrintStream}

trait ScalaInterpreter {
  def NUMLINES: Int

  import scala.tools.nsc.interpreter.IMain
  import scala.tools.nsc.interpreter.Results._
  import java.io.ByteArrayOutputStream

  val scalaInt = scala.collection.mutable.Map[String, (IMain, ByteArrayOutputStream)]()

  def scalaInterpreter[T](channel: String, code: String) = this.synchronized {
    val (si, intOut) = scalaInt.getOrElseUpdate(channel, {
      val settings = new scala.tools.nsc.Settings(null)
      settings.usejavacp.value = true
      settings.lint.value = true
      settings.deprecation.value = true
      val intOut = new ByteArrayOutputStream
      val si = new IMain(settings) {
        override def parentClassLoader = Thread.currentThread.getContextClassLoader
      }
      si.quietImport("scalaz._")
      si.quietImport("Scalaz._")
      si.quietImport("org.scalacheck.Prop._")
      (si, intOut)
    })

    new {
      def interpret = interceptStd(hardenPermissions(si interpret code))

      def determineType = interceptStd(si typeOfExpression code)
    }
  }

  val scriptSM = new SecurityManager {
    System.setProperty("actors.enableForkJoin", false + "")
    val sm = System.getSecurityManager
    var activated = false

    override def checkPermission(perm: Permission) {
      if (activated) {
        val read = perm.getActions == ("read")
        val allowedMethods = Seq("accessDeclaredMembers", "suppressAccessChecks", "createClassLoader",
          "accessClassInPackage.sun.reflect").contains(perm.getName)
        val file = perm.isInstanceOf[FilePermission]
        val readClass = file && (perm.getName.endsWith(".class") || perm.getName.endsWith(".jar") || perm
          .getName.endsWith("library.properties")) && read
        val allow = readClass || (read && !file) || allowedMethods
        if (!allow) {
          println(perm)
          throw new SecurityException(perm.toString)
        }
      } else {
        if (sm != null) {
          sm.checkPermission(perm)
        }
      }

    }

    def deactivate {
      activated = false
      System.setSecurityManager(sm)
    }

    def activate {
      System.setSecurityManager(this)
      activated = true
    }
  }

  def hardenPermissions[T](f: => T): T = this.synchronized {
    try {
      scriptSM.activate
      f
    } finally {
      scriptSM.deactivate
    }
  }

  def interceptStd[T](f: => T): (T, String) = this.synchronized {
    val stdOut = System.out
    val stdErr = System.err
    val conOut = new ByteArrayOutputStream

    val stream = new PrintStream(conOut)
    try {
      System setErr stream
      System setOut stream
      val result = scala.Console.withErr(stream) {scala.Console.withOut(stream) {f}}
      stream.flush
      conOut.flush
      val std = conOut.toString
      (result, std)
    } finally {
      System setErr stdErr
      System setOut stdOut
      conOut.reset
    }
  }

  def interpretScala(msg: Msg, code: String): Seq[String] = {
    val (res, cout) = scalaInterpreter(msg.channel, code).interpret
    (res match {
      case Success => cout
      case Error => cout.replaceAll("^<console>:[0-9]+: ", "")
      case Incomplete => "error: unexpected EOF found, incomplete expression"
    }).split("\n") take NUMLINES map (m => " " + (if (m.startsWith("\n")) m.substring(1) else m))
  }
}

