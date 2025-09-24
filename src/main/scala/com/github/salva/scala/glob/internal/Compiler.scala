package com.github.salva.scala.glob.internal

import java.util.regex.Pattern

import scala.annotation.tailrec

object Compiler extends CompilerHelper {
  private val withPeriod: Compiler    = new Compiler(CompilerPatterns.withPeriod)
  private val withoutPeriod: Compiler = new Compiler(CompilerPatterns.withoutPeriod)

  def compile(glob: String, caseInsensitive: Boolean, period: Boolean): (Option[Pattern], Option[Pattern]) = {
    val compiler              = if (period) withPeriod else withoutPeriod
    val tokens                = Parser.parseGlob(glob)
    val (mayBeDir, mustBeDir) = compiler.compileToString(tokens, Nil, "0")
    (mayBeDir.map(stringAcuToRegex(_, caseInsensitive)), mustBeDir.map(stringAcuToRegex(_, caseInsensitive)))
  }
}

final class Compiler(compilerPatterns: CompilerPatterns) extends CompilerHelper {
  import compilerPatterns._

  def flushState(state: String, acu: List[String]): List[String] =
    state match {
      case "" | "0"         => acu
      case "/" | "0/"       => "/+" +: acu
      case "/**"            => pSAA1 +: acu
      case "0/**"           => pSAA1 +: acu
      case "/**/" | "0/**/" => pSAAS +: acu
      case "**"             => pAA +: acu
      case "0**"            => pStartAA +: acu
      case "**/"            => pAAS +: acu
      case "0**/"           => pZAAS +: acu
      case _                => internalError(s"""Invalid internal state "$state" reached""")
    }

  @tailrec
  def compileToString(
    tokens: List[Token],
    acu: List[String] = Nil,
    state: String,
  ): (Option[List[String]], Option[List[String]]) =
    tokens match {
      case Nil           =>
        // println("> nil, state: " + state)
        state match {
          case ""             => (Some("/*" +: acu), None)
          case "/"            => (None, Some("/*" +: acu))
          case "/**"          => (Some(pSAA0 +: acu), None)
          case "/**/"         => (None, Some(pSAA0 +: acu))
          case "0"            => (Some("" +: acu), None)
          case "0/"           => (None, Some("/+" +: acu))
          case "0/**"         => (Some(pZSAA +: acu), None)
          case "0/**/"        => (None, Some(pZSAA +: acu))
          case "**" | "0**"   => (Some(pAA +: acu), None)
          case "**/" | "0**/" => (None, Some(pAA +: acu))
          case _              => internalError(s"""Invalid internal state "$state" reached""")
        }
      case token :: tail =>
        // println("> " + token + ", state: " + state)
        token match {
          case Token.Special("/")            =>
            state match {
              case "" | "**" | "/**" | "0" | "0**" | "0/**" => compileToString(tail, acu, state + "/")
              case "/" | "/**/" | "0/" | "0/**/"            => compileToString(tail, acu, state)
              case _                                        => // compileToString(tail, flushState(state, acu), "/")
                internalError(s"""Invalid internal state "$state" reached""")
            }
          case Token.Special("**")           =>
            state match {
              case "" | "/" | "0" | "0/"         => compileToString(tail, acu, state + "**")
              case "**" | "/**" | "0**" | "0/**" => compileToString(tail, acu, state)
              case _                             => compileToString(tail, flushState(state, acu), "**")
            }
          case Token.CurlyBrackets(branches) =>
            if (tail != Nil) internalError("CurlyBrackets token is not last in queue")
            else if (branches.isEmpty) internalError("CurlyBrackets with no alternations found")
            else compileCurlyBrackets(branches, acu, state)
          case _                             =>
            val acu1 = flushState(state, acu)
            token match {
              case Token.Literal(literal)                => compileToString(tail, quoteString(literal) +: acu1, "")
              case Token.Special("*")                    => compileToString(tail, pA +: acu1, "")
              case Token.Special("?")                    => compileToString(tail, pQ +: acu1, "")
              case Token.SquareBrackets(inside, negated) => compileToString(tail, compileSquareBrackets(inside, negated, acu1), "")
              case _                                     => internalError(s"""Unexpected token "$token" found""")
            }
        }
    }

  def compileCurlyBrackets(
    branches: List[List[Token]],
    acu: List[String],
    state: String,
  ): (Option[List[String]], Option[List[String]]) = {

    def compileSide(side: List[Option[List[String]]], acu: List[String]): Option[List[String]] =
      side.flatten match {
        case Nil         => None
        case head :: Nil => Some(head ++ acu) // not really required but simplifies the generated patterns
        case flat        => Some("))" +: (intersperseAndFlatten(flat, ")|(?:") ++ ("(?:(?:" +: acu)))
      }

    val (mayBeDir, mustBeDir) = branches.map(compileToString(_, Nil, state)).unzip
    (compileSide(mayBeDir, acu), compileSide(mustBeDir, acu))
  }
}
