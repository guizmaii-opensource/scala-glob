package com.github.salva.scala.glob.internal

import fastparse._, NoWhitespace._

object Parser {

  def badGlob(msg:String):Nothing = throw new IllegalArgumentException(s"Bad blog pattern: $msg")

  def error[_:P](msg:String):P[Nothing] = Pass.map(_ => badGlob(msg))

  val special = "\\[]{}/*?,"

  def regularChars[_: P]: P[String] = P(CharPred(!special.contains(_)).rep(1)./.!)
  def escapedChar[_: P]: P[String] = P("\\" ~/ (CharPred(_ != '/' || badGlob("invalid char after '\\'")).! | error("missing char after '\\'")))

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
    P(("{" ~/ commaSepTokens ~/ ("}" | error("'}' missing"))).map(a => CurlyBrackets(a)))

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
    P("[" ~/ (negated ~/ (ranges ~/ "]" | error("bad character class"))).map(p => SquareBrackets(p._2, p._1)))

  def glob[_:P]:P[Seq[Token]] = P(token.rep(1)./ ~ End)

  def parseGlob(str:String):Seq[Token] = {
    parse(str, glob(_)) match {
      case Parsed.Success(tokens,_) => cleanTree(tokens)
      case Parsed.Failure(_, index, _) => badGlob(s"unknown error at position $index")
    }
  }

  def cleanTree(tokens: Seq[Token]): Seq[Token] = {

    def doIt(tokens:Seq[Token], acu:Seq[Token]): Seq[Token] = {
      tokens match {
        case Nil => acu.reverse
        case head +: tail => {
          head match {
            case CurlyBrackets(inside) => {
              if (inside.isEmpty) doIt(tail, acu)
              else CurlyBrackets(inside.map(b => doIt(b ++ tail, Nil))) +: acu
            }
            case Special(",") => doIt(tail, Literal(",") +: acu)
            case _ => doIt(tail, head +: acu)
          }
        }
      }
    }

    doIt(tokens, Nil)
  }

}
