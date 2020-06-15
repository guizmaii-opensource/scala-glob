package com.github.salva.scala.glob

import com.github.salva.scala.glob.internal.{Compiler, PartialCompiler}

sealed trait MatchResult
case class Match(mustBeDir:Boolean) extends MatchResult
case object NoMatch extends MatchResult

class Glob(val glob:String) extends Serializable {
  val (mayBeDir, mustBeDir) = Compiler.compile(glob)
  lazy val partial = PartialCompiler.compile(glob)

  def matches(path:String): MatchResult = {
    if (mayBeDir.map(_.matcher(path).matches).getOrElse(false)) Match(false)
    else if (mustBeDir.map(_.matcher(path).matches).getOrElse(false)) Match(true)
    else NoMatch
  }

  def matchesPartially(path:String): MatchResult =
    if (partial.matcher(path).matches) Match(true) else NoMatch
}