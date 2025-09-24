package com.github.salva.scala.glob.internal

sealed trait Token
object Token {
  final case class Special(special: String)                              extends Token
  final case class Literal(literal: String)                              extends Token
  final case class CurlyBrackets(inside: List[List[Token]])              extends Token
  final case class SquareBrackets(inside: List[Range], negated: Boolean) extends Token
}

final case class Range(start: String, end: Option[String])
