package com.github.salva.scala.glob

import java.util.regex.Pattern

import com.github.salva.scala.glob.token.{CurlyBrackets, Literal, Range, Special, SquareBrackets, Token}

object GlobCompiler {

  def compileFragment(token: Token, state:String):(String,String) =
    token match {
      case Literal(literal) => (Pattern.quote(literal), "")
      case Special("*") => ("[^/]*", "")
      case Special("?") => ("[^/]", "")
      case Special("/") => do {
        ("/", "/")
      }


        case Special(",") => ","
        case
    }

  def compileToString(tokens: Seq[Token], state:String=""): String = {
    val fragments = tokens.map {
      _ match {

        case Special("**") => ".*"
        case Special("*") => "[^/]*"
        case Special("?") => "[^/]"
        case Special("/") => "/"
        case Special(",") => ","
        case CurlyBrackets(inside) => "(?:(?:" + inside.map(compileToString(_)).mkString(")|(?:") + "))"
        case SquareBrackets(inside, negated) => "[" + (if (negated) "!" else "") + inside.map(compileRange(_)).mkString + "]"
      }
    }

    fragments.toList.mkString
  }

  def compileRange(range: Range): String = {
    range match {
      case Range(start, maybeEnd) => {
        val quotedStart = Pattern.quote(start)
        maybeEnd match {
          case Some(end) => {
            val quotedEnd = Pattern.quote(end)
            if (end >= start) quotedStart + "-" + quotedEnd
            else throw new IllegalArgumentException("Bad pattern")
          }
          case None => quotedStart
        }
      }
    }
  }

  def expandTree(tokens: Seq[Token]): Seq[Token] = {
    tokens match {
      case Nil => Nil
      case head :: tail => {
        var newTail = expandTree(tail)
        head match {
          case CurlyBrackets(inside) => Seq(CurlyBrackets(inside.map(_ ++ newTail)))
          case _ => if (tail.eq(newTail)) tokens else Seq(head) ++ newTail
        }
      }
    }
  }
}