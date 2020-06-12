package com.github.salva.scala.glob

import atto.Atto._
import atto.Parser

trait Token {}

case class Literal(literal:String) extends Token
case class CurlyBrackets(val inside:Seq[Seq[Token]]) extends Token
case class SquareBrackets(val inside:Seq[Range]) extends Token
case class Special(var special:Char) extends Token

case class Range(val start:Char, val end:Option[String]=None)

object GlobParser {
  val special = "\\[]{}/*?,"

  def escapedChar =
    (char('\\') ~ anyChar).map(_._2)

  def literal:Parser[Token] = many1(choice(noneOf(special), escapedChar)).map(a => Literal(a.toList.mkString))

  def specialChar(c:Char):Parser[Token] = char(c).map(c => Special(c))

  def comma:Parser[Token] = specialChar(',')
  def doubleAsterisk: Parser[Token] = (char('*')~char('*')).map(_ => Special('+'))
  def asterisk: Parser[Token] = specialChar('*')
  def slash: Parser[Token] = specialChar('/')
  def anyToken:Parser[Token] = (choice(comma, anyTokenValidInsideCurlyBrackets /*,curlyBrackets*/))

  def anyTokenValidInsideCurlyBrackets =
    choice(
      literal,
      doubleAsterisk,
      asterisk,
      slash,
      curlyBrackets
    )

  def insideCurlyBrackets = sepBy(many(anyTokenValidInsideCurlyBrackets), comma)

  def curlyBrackets:Parser[Token] =
    braces(insideCurlyBrackets).map(CurlyBrackets(_))
}
