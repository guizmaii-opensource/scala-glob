package com.github.salva.scala.glob.token

sealed trait Token {}

case class Special(special:String) extends Token
case class Literal(literal:String) extends Token
case class CurlyBrackets(inside:Seq[Seq[Token]]) extends Token
case class SquareBrackets(inside:Seq[Range], negated:Boolean) extends Token

case class Range(start:String, end:Option[String])

