package com.github.salva.scala.glob

import fastparse._
import NoWhitespace._
import com.github.salva.scala.glob.token._


object GlobParser {

  val special = "\\[]{}/*?,"

  def regularChars[_: P]: P[String] = P(CharPred(!special.contains(_)).rep(1)./.!)
  def escapedChar[_: P]: P[String] = P ("\\" ~/ AnyChar.!)

  def literal[_: P]: P[Token] =
    P((regularChars | escapedChar).rep(1)./.map(a => Literal(a.reduce(_ + _))))

  def special[_: P](s: String): P[Special] =
    P(P(s)./.map(_ => Special(s)))

  def doubleAsterisk[_: P]: P[Token] = special ("**")
  def asterisk[_: P]: P[Token] = special ("*")
  def question[_:P]: P[Token] = special("?")
  def slash[_: P]: P[Token] = special ("/")
  def comma[_: P]: P[Token] = special (",")

  def tokenValidInsideCurlyBrackets[_: P]: P[Token] =
    P(literal | doubleAsterisk | asterisk | question | slash | curlyBrackets | squareBrackets)

  def commaSepTokens[_:P, A]:P[Seq[Seq[Token]]] =
      P((tokenValidInsideCurlyBrackets.rep./).rep(sep=",")./)

  def curlyBrackets[_: P]: P[Token] =
    P(("{" ~/ commaSepTokens ~/ "}").map(a => CurlyBrackets(a)))

  def token[_: P]: P[Token] =
    P((tokenValidInsideCurlyBrackets | comma | curlyBrackets)./)

  def anyButClosingSquare[_:P]:P[String] = P(escapedChar | CharPred(_ != ']').!)

  def firstRangeInsideSquareBrackets[_:P]:P[Range] =
    P(((escapedChar | AnyChar.!) ~/ (("-" ~ anyButClosingSquare./).?)).map(p => Range(p._1, p._2)))

  def rangeInsideSquareBrackets[_:P]:P[Range] =
    P((anyButClosingSquare ~/ (("-" ~ anyButClosingSquare./).?)).map(p => Range(p._1, p._2)))

  def ranges[_:P]:P[Seq[Range]] =
    P((firstRangeInsideSquareBrackets ~/ (rangeInsideSquareBrackets.rep)).map(p => p._1 :: (p._2.toList)))

  def negated[_:P]:P[Boolean] = P(("!"./.!.?.map(_.nonEmpty)))

  def squareBrackets[_:P]: P[Token] =
    P(("[" ~/ negated ~/ ranges ~/ "]").map(p => SquareBrackets(p._2, p._1)))

  def glob[_:P]:P[Seq[Token]] = P(token.rep(1)./ ~ End)
}

