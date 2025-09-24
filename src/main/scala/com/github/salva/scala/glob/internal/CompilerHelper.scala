package com.github.salva.scala.glob.internal

import java.util.regex.Pattern

object CompilerHelper {
  private val dontQuote: Pattern = Pattern.compile("""[a-zA-Z0-9,"']*""")
}

trait CompilerHelper {

  def stringAcuToRegex(acu: List[String], caseInsensitive: Boolean): Pattern = {
    val flags = if (caseInsensitive) Pattern.CASE_INSENSITIVE else 0
    Pattern.compile(acu.reverse.mkString, flags)
  }

  def intersperseAndFlatten[A](seq: List[List[A]], sep: A): List[A] =
    if (seq.isEmpty) Nil
    else {
      val sepList = List(sep)
      seq.head ++ seq.tail.flatMap(sepList ++ _)
    }

  def internalError(msg: String): Nothing =
    throw new IllegalStateException(s"""Internal error: $msg. This is an internal error, report it, please!""")

  def quoteString(literal: String): String = if (CompilerHelper.dontQuote.matcher(literal).matches) literal else Pattern.quote(literal)

  def quoteChar(literal: String): String =
    if (literal.length != 1) internalError("Char length is bigger than one")
    else if (CompilerHelper.dontQuote.matcher(literal).matches) literal
    else "\\" + literal

  def compileSquareBrackets(ranges: List[Range], negated: Boolean, acu: List[String]): List[String] = {
    val start = if (negated) "[^" else "["
    "]" +: ranges.foldLeft(start +: acu)((acu, range) => compileRange(range) +: acu)
  }

  def compileRange(range: Range): String =
    range match {
      case Range(start, maybeEnd) =>
        val quotedStart = quoteChar(start)
        maybeEnd match {
          case Some(end) =>
            if (end >= start) quotedStart + "-" + quoteChar(end)
            else throw new IllegalArgumentException("Bad range in pattern")
          case None      => quotedStart
        }
    }
}
