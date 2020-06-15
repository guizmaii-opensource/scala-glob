import org.scalatest.FunSuite

import com.github.salva.scala.glob._

class Tests extends FunSuite {
  def testResult(glob:String, path:String, result:MatchResult) = {
    test("glob " + glob + " ~ " + path + " --> " + result) {
      assert(new Glob(glob).matches(path) == result)
    }
  }
  def testNoMatch(glob:String, path:String) = testResult(glob, path, NoMatch)
  def testMatch(glob:String, path:String) = testResult(glob, path, Match(false))
  def testMatchDir(glob:String, path:String) = testResult(glob, path, Match(true))

  testMatch("/etc", "/etc")
  testMatch("/etc", "/etc/")
  testMatch("/etc", "/etc//")
  testMatch("/e[t]c", "/etc")
  testMatch("/?tc", "/etc")
  testMatch("/*tc", "/tc")
  testMatch("/*tc", "/etc")
  testMatch("/*tc", "/eetc")
  testMatch("/[a-z]tc", "/etc")
  testMatch("/[a-cd-g]tc", "/etc/")
  testMatch("/etc**", "/etc")
  testMatch("/etc**", "/etc/")
  testMatch("/etc**", "/etc/foo")
  testMatch("/etc**", "/etc/foo/")
  testMatch("/etc**", "/etc/foo/bar")
  testMatch("/etc**", "/etcfoo/bar")
  testMatch("/etc**", "/etc//foo")
  testMatch("/etc**", "/etcfoo//foo")
  testMatch("/etc**", "/etcfoo//foo/")

  testMatchDir("/etc/", "/etc")
  testMatchDir("/etc/", "/etc/")
  testMatchDir("/etc**/", "/etc")
  testMatchDir("/etc**/bar/", "/etcfoo///dii/bar")

  testNoMatch("/etc", "etc")
  testNoMatch("/etc", "etc/")
  testNoMatch("/etc", "/")
  testNoMatch("/etc", ".")
  testNoMatch("/etc", "./etc")
  testNoMatch("/[A-Z]tc", "/etc")
  testNoMatch("/[!a-z]tc", "/etc")
  testNoMatch("/?tc", "/eetc")

  /* matchingPartially */

  def testPartialResult(glob:String, path:String, result:MatchResult) = {
    test("glob partial " + glob + " ~ " + path + " --> " + result) {
      assert(new Glob(glob).matchesPartially(path) == result)
    }
  }
  def testPartialNoMatch(glob:String, path:String) = testPartialResult(glob, path, NoMatch)
  def testPartialMatchDir(glob:String, path:String) = testPartialResult(glob, path, Match(true))


  testPartialMatchDir("/etc/", "/")
  testPartialMatchDir("etc", "/")
  testPartialMatchDir("etc", "")
  testPartialMatchDir("/etc**", "/etc")
  testPartialMatchDir("/etc**", "/etc///")
  testPartialMatchDir("/etc**", "/etcbar/foo")
  testPartialMatchDir("/etc/**", "/etc")
  testPartialMatchDir("/etc/**", "/etc/foo")
  testPartialMatchDir("/etc/**", "/etc/foo///")

  testPartialNoMatch("/etc", "/etc")
  testPartialNoMatch("/etc", "/usr")
  testPartialNoMatch("/etc/", "/etc")
  testPartialNoMatch("/etc", "etc")
  testPartialNoMatch("etc", "etc")

}
