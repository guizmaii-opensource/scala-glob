package com.github.salva.scala.glob.internal

import fastparse._
import NoWhitespace._

import scala.annotation.nowarn

@nowarn("msg=unused value")
object Parser {

  def badGlob(msg: String): Nothing = throw new IllegalArgumentException(s"Bad blog pattern: $msg")

  def error[Dummy: P](msg: String): P[Nothing] = Pass.map(_ => badGlob(msg)): @nowarn("msg=dead code following this construct")

  val special = "\\[]{}/*?,"

  def regularChars[Dummy: P]: P[String] = P(CharPred(!special.contains(_)).rep(1)./.!)

  @nowarn("msg=dead code following this construct")
  def escapedChar[Dummy: P]: P[String] =
    P("\\" ~/ (CharPred(_ != '/' || badGlob("invalid char after '\\'")).! | error("missing char after '\\'")))

  def literal[Dummy: P]: P[Token] = P((regularChars | escapedChar).rep(1)./.map(a => Token.Literal(a.reduce(_ + _))))

  def special[Dummy: P](s: String): P[Token.Special] = P(P(s)./.map(_ => Token.Special(s)))

  def doubleAsterisk[Dummy: P]: P[Token] = special("**")
  def asterisk[Dummy: P]: P[Token]       = special("*")
  def question[Dummy: P]: P[Token]       = special("?")
  def slash[Dummy: P]: P[Token]          = special("/")
  def comma[Dummy: P]: P[Token]          = special(",")

  def tokenValidInsideCurlyBrackets[Dummy: P]: P[Token] =
    P(literal | doubleAsterisk | asterisk | question | slash | curlyBrackets | squareBrackets)

  def commaSepTokens[Dummy: P, A]: P[List[List[Token]]] = P(
    (tokenValidInsideCurlyBrackets.rep./.map(_.toList)).rep(sep = ",")./.map(_.toList)
  )

  def curlyBrackets[Dummy: P]: P[Token] = P(("{" ~/ commaSepTokens ~/ ("}" | error("'}' missing"))).map(a => Token.CurlyBrackets(a)))

  def token[Dummy: P]: P[Token] = P((tokenValidInsideCurlyBrackets | comma | curlyBrackets)./)

  def anyButClosingSquare[Dummy: P]: P[String] = P(escapedChar | CharPred(_ != ']').!)

  def firstRangeInsideSquareBrackets[Dummy: P]: P[Range] =
    P(((escapedChar | AnyChar.!) ~/ (("-" ~ anyButClosingSquare./).?)).map(p => Range(p._1, p._2)))

  def rangeInsideSquareBrackets[Dummy: P]: P[Range] = P(
    (anyButClosingSquare ~/ (("-" ~ anyButClosingSquare./).?)).map(p => Range(p._1, p._2))
  )

  def ranges[Dummy: P]: P[List[Range]] = P(
    (firstRangeInsideSquareBrackets ~/ (rangeInsideSquareBrackets.rep)).map(p => p._1 :: (p._2.toList))
  )

  def negated[Dummy: P]: P[Boolean] = P(("!"./.!.?.map(_.nonEmpty)))

  def squareBrackets[Dummy: P]: P[Token] =
    P("[" ~/ (negated ~/ (ranges ~/ "]" | error("bad character class"))).map(p => Token.SquareBrackets(p._2, p._1)))

  def glob[Dummy: P]: P[List[Token]] = P(token.rep(1)./.map(_.toList) ~ End)

  def parseGlob(str: String): List[Token] =
    parse(str, glob(_)) match {
      case Parsed.Success(tokens, _)   => cleanTree(tokens)
      case Parsed.Failure(_, index, _) => badGlob(s"unknown error at position $index")
    }

  def cleanTree(tokens: List[Token]): List[Token] = {

    def doIt(tokens: List[Token], acu: List[Token]): List[Token] =
      tokens match {
        case Nil          => acu.reverse
        case head :: tail =>
          head match {
            case Token.CurlyBrackets(inside) =>
              if (inside.isEmpty) doIt(tail, acu)
              else (Token.CurlyBrackets(inside.map(b => doIt(b ++ tail, Nil))) +: acu).reverse
            case Token.Special(",")          => doIt(tail, Token.Literal(",") +: acu)
            case _                           => doIt(tail, head +: acu)
          }
      }

    doIt(tokens, Nil)
  }

}
