package com.github.salva.scala.glob.internal

import java.util.regex.Pattern

object PartialCompiler extends CompilerHelper {

  def compile(glob:String, caseInsensitive:Boolean, period:Boolean):Pattern = {
    val compiler = new PartialCompiler(period)
    val acu = compiler.compileToString(Parser.parseGlob(glob), Seq("(?:"), "", 1)
    stringAcuToRegex(acu, caseInsensitive)
  }
}

class PartialCompiler(val period:Boolean) extends CompilerHelper with CompilerPatterns {

  // @tailrec
  def compileToString(tokens:Seq[Token], acu: Seq[String], state:String, open:Int):Seq[String] = {
    tokens match {
      case Nil => closeOpen("(?!)" +: acu, open)
      case token +: tail => {
        token match {
          case Special("/") => {
            state match {
              case "" => compileToString(tail, acu, state + "/", open)
              case "/" => compileToString(tail, acu, state, open)
              case _ => internalError(s"""Invalid internal state "$state" reached """)
            }
          }
          case Special("**") => {
            state match {
              case "" => closeOpen(pAA +: acu, open)
              case "/" => closeOpen(pSAA0 +: acu, open)
              case _ => internalError(s"""Invalid internal state "$state" reached """)
            }
          }
          case CurlyBrackets(branches) => {
            if (tail != Nil) internalError("CurlyBrackets token is not last in queue")
            else if (branches.isEmpty) internalError("CurlyBrackets with no alternations found")
            else closeOpen(compileCurlyBrackets(branches, acu, state), open)
          }
          case _ => {
            val (acu1, open1) = flushState(state, acu, open)
            token match {
              case Literal(literal) => compileToString(tail, quoteString(literal) +: acu1, "", open1)
              case Special("*") => compileToString(tail, pA +: acu1, "", open1)
              case Special("?") => compileToString(tail, pQ +: acu1, "", open1)
              case SquareBrackets(inside, negated) =>
                compileToString(tail, compileSquareBrackets(inside, negated, acu1), "", open1)
              case _ => internalError(s"""Unexpected token "$token" found""")            }
          }
        }
      }
    }
  }

  def compileCurlyBrackets(branches:Seq[Seq[Token]], acu:Seq[String], state:String): Seq[String] = {
    "))" +: (intersperseAndFlatten(branches.map(compileToString(_, Nil, state, 0)), ")|(?:") ++ ("(?:(?:" +: acu))
  }

  def flushState(state:String, acu:Seq[String], open:Int): (Seq[String], Int) = {
    state match {
      case "" => (acu, open)
      case "/" => ("(?:/+(?:" +: acu, open + 2)
      case _ => internalError(s"""Invalid internal state "$state" reached""")
    }
  }

  def closeOpen(acu:Seq[String], open:Int):Seq[String] = Seq.fill(open)(")?") ++ acu
}
