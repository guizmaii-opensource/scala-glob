package com.github.salva.scala.glob.internal

import java.util.regex.Pattern
import scala.annotation.tailrec

object PartialCompiler extends CompilerHelper {
  private val withPeriod: PartialCompiler    = new PartialCompiler(CompilerPatterns.withPeriod)
  private val withoutPeriod: PartialCompiler = new PartialCompiler(CompilerPatterns.withoutPeriod)

  def compile(glob: String, caseInsensitive: Boolean, period: Boolean): Pattern = {
    val compiler = if (period) withPeriod else withoutPeriod
    val acu      = compiler.compileToString(Parser.parseGlob(glob), List("(?:"), "", 1)
    stringAcuToRegex(acu, caseInsensitive)
  }
}

final class PartialCompiler(compilerPatterns: CompilerPatterns) extends CompilerHelper {
  import compilerPatterns._

  @tailrec
  def compileToString(tokens: List[Token], acu: List[String], state: String, open: Int): List[String] =
    tokens match {
      case Nil           => closeOpen("(?!)" +: acu, open)
      case token :: tail =>
        token match {
          case Token.Special("/")            =>
            state match {
              case ""  => compileToString(tail, acu, state + "/", open)
              case "/" => compileToString(tail, acu, state, open)
              case _   => internalError(s"""Invalid internal state "$state" reached """)
            }
          case Token.Special("**")           =>
            state match {
              case ""  => closeOpen(pAA +: acu, open)
              case "/" => closeOpen(pSAA0 +: acu, open)
              case _   => internalError(s"""Invalid internal state "$state" reached """)
            }
          case Token.CurlyBrackets(branches) =>
            if (tail != Nil) internalError("CurlyBrackets token is not last in queue")
            else if (branches.isEmpty) internalError("CurlyBrackets with no alternations found")
            else closeOpen(compileCurlyBrackets(branches, acu, state), open)
          case _                             =>
            val (acu1, open1) = flushState(state, acu, open)
            token match {
              case Token.Literal(literal)                => compileToString(tail, quoteString(literal) +: acu1, "", open1)
              case Token.Special("*")                    => compileToString(tail, pA +: acu1, "", open1)
              case Token.Special("?")                    => compileToString(tail, pQ +: acu1, "", open1)
              case Token.SquareBrackets(inside, negated) => compileToString(tail, compileSquareBrackets(inside, negated, acu1), "", open1)
              case _                                     => internalError(s"""Unexpected token "$token" found""")
            }
        }
    }

  def compileCurlyBrackets(branches: List[List[Token]], acu: List[String], state: String): List[String] =
    "))" +: (intersperseAndFlatten(branches.map(compileToString(_, Nil, state, 0)), ")|(?:") ++ ("(?:(?:" +: acu))

  def flushState(state: String, acu: List[String], open: Int): (List[String], Int) =
    state match {
      case ""  => (acu, open)
      case "/" => ("(?:/+(?:" +: acu, open + 2)
      case _   => internalError(s"""Invalid internal state "$state" reached""")
    }

  def closeOpen(acu: List[String], open: Int): List[String] = List.fill(open)(")?") ++ acu
}
