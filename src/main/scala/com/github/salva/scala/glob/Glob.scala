package com.github.salva.scala.glob

import com.github.salva.scala.glob.internal.{Compiler, PartialCompiler}
import scala.language.implicitConversions

sealed trait MatchResult {
  def ||(other:MatchResult):MatchResult
  def &&(other:MatchResult):MatchResult
  def toBoolean:Boolean
}

object MatchResult {
  implicit def toBoolean(matchResult: MatchResult) = matchResult.toBoolean
}

case class Match(mustBeDir:Boolean) extends MatchResult {
  override def ||(other:MatchResult): MatchResult = {
    if (mustBeDir) {
      other match {
        case NoMatch => this
        case _ => other
      }
    }
    else this
  }
  override def &&(other:MatchResult):MatchResult = {
    other match {
      case NoMatch => other
      case Match(_) => if (mustBeDir) this else other
    }
  }
  override def toBoolean:Boolean = true
}

case object NoMatch extends MatchResult {
  override def ||(other:MatchResult):MatchResult = other
  override def &&(other:MatchResult):MatchResult = this
  override def toBoolean:Boolean = false
}

class Glob(val glob:String, val caseInsensitive:Boolean=false, val period:Boolean=false) extends Serializable {
  val (mayBeDir, mustBeDir) = Compiler.compile(glob, caseInsensitive, period)
  lazy val partial = PartialCompiler.compile(glob, caseInsensitive, period)

  def matches(path:String): MatchResult = {
    if (mayBeDir.map(_.matcher(path).matches).getOrElse(false)) Match(false)
    else if (mustBeDir.map(_.matcher(path).matches).getOrElse(false)) Match(true)
    else NoMatch
  }

  def matchesPartially(path:String): MatchResult =
    if (partial.matcher(path).matches) Match(true) else NoMatch

  override def toString: String = s"Glob($glob)"
}