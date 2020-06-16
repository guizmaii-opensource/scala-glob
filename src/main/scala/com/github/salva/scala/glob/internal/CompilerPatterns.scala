package com.github.salva.scala.glob.internal

trait CompilerPatterns {

  def period: Boolean

  lazy val pStartAA = if (period) "(?:(?!\\.\\.?(?:/|$))[^/]+)?" else "(?:(?!\\.)[^/]+)?"
  // Slash-Asterisk-Asterisk
  lazy val pSAA = "(?:/" + pStartAA + ")*"
  // Zero-Slash-Asterisk-Asterisk
  lazy val pZSAA = "(?:/" + pStartAA + ")+"
  // Zero-Asterisk-Asterisk
  lazy val pZAA = pStartAA + pSAA
  // Asterisk-Asterisk
  lazy val pAA = "[^/]*" + pSAA
  // Slash-Asterisk-Asterisk-Slash
  lazy val pSAAS = pSAA + "/"
  // Asterisk-Asterisk-Slash
  lazy val pAAS = pAA + "/"
  // Zero-Asterisk-Asterisk-Slash
  lazy val pZAAS = pZAA + "/"
  // Asterisk
  lazy val pA = if (period) "(?:(?<=/|^)(?!\\.\\.?(?:/|^))|(?<!/|^))[^/]*"
  else        "(?:(?<=/|^)(?:[^\\./][^/]*)?|(?<!/|^)[^/]*)"
  // Question
  lazy val pQ = if (period) "(?:(?<=/|^)(?!\\.\\.?(?:/|^))|(?<!/|^))[^/]"
  else        "(?:(?<=/|^)[^\\./]|(?<!/|^)[^/])"
}
