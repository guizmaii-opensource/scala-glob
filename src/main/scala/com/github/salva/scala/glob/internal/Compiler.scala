package com.github.salva.scala.glob.internal

import java.util.regex.Pattern

import scala.annotation.tailrec

object Compiler extends CompilerHelper {

  def compile(glob:String, caseInsensitive:Boolean):(Option[Pattern], Option[Pattern]) = {
    val tokens = Parser.parseGlob(glob)
    val (mayBeDir, mustBeDir) = compileToString(tokens, Nil, "0")
    (mayBeDir.map(stringAcuToRegex(_, caseInsensitive)),
      mustBeDir.map(stringAcuToRegex(_, caseInsensitive)))
  }

  @tailrec
  def compileToString(tokens: Seq[Token], acu: Seq[String] = Nil, state: String): (Option[Seq[String]], Option[Seq[String]]) = {
    tokens match {
      case Nil => {
        state match {
          case ""             => (Some("/*"       +: acu), None                   )
          case "/"            => (None                   , Some("/*"       +: acu))
          case "/**"          => (Some("(?:/.*)?" +: acu), None                   )
          case "/**/"         => (None                   , Some("(?:/.*)?" +: acu))
          case "0"            => (Some(""         +: acu), None                   )
          case "0/"           => (None                   , Some("/"        +: acu))
          case "0/**"         => (Some("/.*"      +: acu), None                   )
          case "0/**/"        => (None                   , Some("/.*"      +: acu))
          case "**"  | "0**"  => (Some(".*"       +: acu), None                   )
          case "**/" | "0**/" => (None                   , Some(".*"       +: acu))
          case _ => internalError(s"""Invalid internal state "$state" reached""")
        }
      }
      case token::tail => {
        token match {
          case Special("/") => {
            state match {
              case "" | "**" | "/**" | "0" | "0**" | "0/**" => compileToString(tail, acu, state + "/")
              case "/" | "/**/" | "0/" | "0/**/" => compileToString(tail, acu, state)
              case _ => compileToString(tail, flushState(state, acu), "/")
            }
          }
          case Special("**") => {
            state match {
              case "" | "/" | "0" => compileToString(tail, acu, state + "**")
              case "**" | "/**" | "0/**" => compileToString(tail, acu, state)
              case _ => compileToString(tail, flushState(state, acu), "**")
            }
          }
          case CurlyBrackets(branches) => {
            if (tail != Nil) internalError("CurlyBrackets token is not last in queue")
            else if (branches.isEmpty) internalError("CurlyBrackets with no alternations found")
            else compileCurlyBrackets(branches, acu, state)
          }
          case _ => {
            val acu1 = flushState(state, acu)
            token match {
              case Literal(literal) => compileToString(tail, quoteString(literal) +: acu1, "")
              case Special("*") => compileToString(tail, ".*" +: acu1, "")
              case Special("?") => compileToString(tail, "." +: acu1, "")
              case SquareBrackets(inside, negated) =>
                compileToString(tail, compileSquareBrackets(inside, negated, acu1), "")
              case _ => internalError(s"""Unexpected token "$token" found""")
            }
          }
        }
      }
    }
  }

  def flushState(state:String, acu:Seq[String]):Seq[String] = {
    state match {
      case "" | "0" => acu
      case "/" | "0/" => "/+" +: acu
      case "/**" | "0/**" => "/.*" +: acu
      case "/**/" | "0/**/" => "/(.*/)?" +: acu
      case "**" | "0**" => ".*" +: acu
      case "**/" | "0**/" => ".*/" +: acu
      case _ => internalError(s"""Invalid internal state "$state" reached""")
    }
  }

  def compileCurlyBrackets(branches: Seq[Seq[Token]], acu:Seq[String], state:String):(Option[Seq[String]], Option[Seq[String]]) = {

    def compileSide(side:Seq[Option[Seq[String]]], acu:Seq[String]):Option[Seq[String]] = {
      side.flatten match {
        case Nil => None
        case head::Nil => Some(head ++ acu) // not really required but simplifies the generated patterns
        case flat => Some("))" +: ((intersperseAndFlatten(flat, ")|(?:") ++ ("(?:(?:" +: acu))))
      }
    }

    val (mayBeDir, mustBeDir) = branches.map(compileToString(_, Nil, state)).unzip
    (compileSide(mayBeDir, acu), compileSide(mustBeDir, acu))
  }
}
