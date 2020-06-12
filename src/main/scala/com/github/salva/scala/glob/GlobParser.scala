package com.github.salva.scala.glob

import fastparse._
import NoWhitespace._

trait Token {}

case class Literal(literal:String) extends Token
//case class CurlyBrackets(val inside:Seq[Seq[Token]]) extends Token
case class CurlyBrackets(val inside:Any) extends Token
case class SquareBrackets(val inside:Seq[Range]) extends Token
case class Special(var special:String) extends Token

case class Range(val start:Char, val end:Option[String]=None)

object GlobParser {

  def dbg[T](t: T): T = {
    println(t.toString)
    t
  }

  val special = "\\[]{}/*?,"

  def regularChars[_: P]: P[String] = P(CharPred(!special.contains(_)).rep(1)./.!)
  def escapedChar[_: P]: P[String] = P ("\\" ~/ AnyChar.!)

  def literal[_: P]: P[Token] =
    P((regularChars | escapedChar).rep(1)./.map(a => dbg(Literal(a.reduce(_ + _)))))

  def special[_: P](s: String): P[Special] =
    P(P(s)./.map(_ => dbg(Special(s))))

  def doubleAsterisk[_: P]: P[Token] = special ("**")
  def asterisk[_: P]: P[Token] = special ("*")
  def slash[_: P]: P[Token] = special ("/")
  def comma[_: P]: P[Token] = special (",")

  def tokenValidInsideCurlyBrackets[_: P]: P[Token] =
    P(literal | doubleAsterisk | asterisk | slash | curlyBrackets)

  def commaSepTokens[_:P, A]:P[Seq[Seq[Token]]] =
      P((tokenValidInsideCurlyBrackets.rep./).rep(sep=",")./)

  def curlyBrackets[_: P]: P[Token] =
    P(("{" ~/ commaSepTokens ~/ "}").map(a => dbg(CurlyBrackets(a))))

  def token[_: P]: P[Token] =
    P((tokenValidInsideCurlyBrackets | comma | curlyBrackets)./)

  def tokens[_: P]: P[Seq[Token]] = P(token.rep(1)./)


}
