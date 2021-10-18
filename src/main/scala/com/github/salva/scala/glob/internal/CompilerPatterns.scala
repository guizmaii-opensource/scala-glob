package com.github.salva.scala.glob.internal

trait CompilerPatterns {

  def period: Boolean

  lazy val pStartAA = if (period) "(?:(?!\\.\\.?(?:/|$))[^/]+)?" else "(?:(?!\\.)[^/]+)?"
  // Slash-Asterisk-Asterisk, no slashes required
  lazy val pSAA0    = "(?:/" + pStartAA + ")*"
  // Slash-Asterisk-Asterisk, at least one slash required
  lazy val pSAA1    = "(?:/" + pStartAA + ")+"
  // Zero-Slash-Asterisk-Asterisk
  lazy val pZSAA    = pSAA1
  // Zero-Asterisk-Asterisk
  lazy val pZAA     = pStartAA + pSAA0
  // Asterisk-Asterisk
  lazy val pAA      = "[^/]*" + pSAA0
  // Slash-Asterisk-Asterisk-Slash
  lazy val pSAAS    = pSAA0 + "/"
  // Asterisk-Asterisk-Slash
  lazy val pAAS     = pAA + "/"
  // Zero-Asterisk-Asterisk-Slash
  lazy val pZAAS    = pZAA + "/"

  // Asterisk
  lazy val pA =
    if (period) "(?:(?<=/|^)(?!\\.\\.?(?:/|^))|(?<!/|^))[^/]*"
    else "(?:(?<=/|^)(?:[^\\./][^/]*)?|(?<!/|^)[^/]*)"

  // Question
  lazy val pQ =
    if (period) "(?:(?<=/|^)(?!\\.\\.?(?:/|^))|(?<!/|^))[^/]"
    else "(?:(?<=/|^)[^\\./]|(?<!/|^)[^/])"
}
