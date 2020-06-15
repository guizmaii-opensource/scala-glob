package com.github.salva.scala.glob.internal

import java.util.regex.Pattern

object CompilerHelper {
  lazy val dontQuote = Pattern.compile("""[a-zA-Z0-9,"']*""")
}

trait CompilerHelper {

  def stringAcuToRegex(acu:Seq[String]):Pattern = Pattern.compile(acu.reverse.mkString)

  def intersperseAndFlatten[A](seq: Seq[Seq[A]], sep: A): Seq[A] = {
    val sepSeq = Seq(sep)
    if (seq.isEmpty) Nil else seq.head ++ seq.tail.flatMap(sepSeq ++ _)
  }

  def internalError(msg:String):Nothing =
    throw new IllegalStateException(s"""Internal error: ${msg}. This is an internal error, report it, please!""")

  def quoteString(literal:String):String = {
    if (CompilerHelper.dontQuote.matcher(literal).matches) literal else Pattern.quote(literal)
  }

  def quoteChar(literal:String):String = {
    if (literal.length != 1) internalError("Char length is bigger than one")
    else if (CompilerHelper.dontQuote.matcher(literal).matches) literal else "\\" + literal
  }

  def compileSquareBrackets(ranges:Seq[Range], negated:Boolean, acu:Seq[String]):Seq[String] = {
    val start = if (negated) "[^" else "["
    "]" +: ranges.foldLeft(start +: acu)((acu, range) => compileRange(range) +: acu)
  }

  def compileRange(range: Range): String = {
    range match {
      case Range(start, maybeEnd) => {
        val quotedStart = quoteChar(start)
        maybeEnd match {
          case Some(end) => {
            if (end >= start) quotedStart + "-" + quoteChar(end)
            else throw new IllegalArgumentException("Bad range in pattern")
          }
          case None => quotedStart
        }
      }
    }
  }
}
