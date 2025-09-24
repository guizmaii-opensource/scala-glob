package com.github.salva.scala.glob.internal

final case class CompilerPatterns(
  pStartAA: String,
  pSAA0: String,
  pSAA1: String,
  pZSAA: String,
  pZAA: String,
  pAA: String,
  pSAAS: String,
  pAAS: String,
  pZAAS: String,
  pA: String,
  pQ: String,
)

object CompilerPatterns {
  private def make(period: Boolean): CompilerPatterns = {
    val pStartAA = if (period) "(?:(?!\\.\\.?(?:/|$))[^/]+)?" else "(?:(?!\\.)[^/]+)?"
    // Slash-Asterisk-Asterisk, no slashes required
    val pSAA0    = "(?:/" + pStartAA + ")*"
    // Slash-Asterisk-Asterisk, at least one slash required
    val pSAA1    = "(?:/" + pStartAA + ")+"
    // Zero-Slash-Asterisk-Asterisk
    val pZSAA    = pSAA1
    // Zero-Asterisk-Asterisk
    val pZAA     = pStartAA + pSAA0
    // Asterisk-Asterisk
    val pAA      = "[^/]*" + pSAA0
    // Slash-Asterisk-Asterisk-Slash
    val pSAAS    = pSAA0 + "/"
    // Asterisk-Asterisk-Slash
    val pAAS     = pAA + "/"
    // Zero-Asterisk-Asterisk-Slash
    val pZAAS    = pZAA + "/"

    // Asterisk
    val pA =
      if (period) "(?:(?<=/|^)(?!\\.\\.?(?:/|^))|(?<!/|^))[^/]*"
      else "(?:(?<=/|^)(?:[^\\./][^/]*)?|(?<!/|^)[^/]*)"

    // Question
    val pQ =
      if (period) "(?:(?<=/|^)(?!\\.\\.?(?:/|^))|(?<!/|^))[^/]"
      else "(?:(?<=/|^)[^\\./]|(?<!/|^)[^/])"

    CompilerPatterns(
      pStartAA = pStartAA,
      pSAA0 = pSAA0,
      pSAA1 = pSAA1,
      pZSAA = pZSAA,
      pZAA = pZAA,
      pAA = pAA,
      pSAAS = pSAAS,
      pAAS = pAAS,
      pZAAS = pZAAS,
      pA = pA,
      pQ = pQ,
    )
  }

  val withPeriod: CompilerPatterns    = make(true)
  val withoutPeriod: CompilerPatterns = make(false)
}
