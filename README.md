# scala-glob

This library allows one to check paths against file system glob
patterns.

Note that it doesn't provide methods for walking the filesystem
looking for entries matching a given pattern, though you could
write them easily on top of this library.

Actually this library is an spin-off from my other library
[spark-hugefs](github.com/salva/spark-hugefs) where it is used
just for that.

## Example

```scala
import com.github.salva.scala.glob.{Glob, Match, NoMatch}

val g = new Glob("foo/bar/**.jp{,e}g", caseInsensitive=true)

val path = "foo/bar/doz//brum/image100.JPG"

g.matches(path) match {
  case NoMatch => { /* no match */ }
  case Match(_) => println(s"$path matches $g")
}
```

## Usage

`Glob` objects provide two main methods, `matches` and `matchesPartially`.
Both take a path and return one of the following values:

  * `NoMatch`: the glob pattern doesn't match the given path.

  * `Matches(dirRequired:Boolean)`: the glob pattern does match the
    given path. The `dirRequired` flag indicates where the glob pattern
    requires the path to be a directory.
     
    For instance, the object `Glob("/foo/")` will match `"/foo"`
    and `"/foo/"` but because of the slash at the end of the pattern,
    it sets the `dirRequired` flag to true in the result. On the other
    hand, `Glob("/foo")` returns `Match(false)`.

`matchesPartially` returns whether the given path matches a fragment of
the glob pattern. For instance `Glob("/foo/bar")` will match successfully
patterns `"/"` and `"/foo"` but not `"/foo/bar"` as that case is a full
match.

The `Glob` constructor accepts the following optional argument flags:

  * `caseInsensitive`: indicates whether the matchings should be
    performed in a case-insensitive way.
     
  * `period`: Indicates whether wildcard characters inside the glob
    pattern can match period characters (`.`) at the beginning of
    directory names.
    
    For instance, by default, `Glob("*")` will not match `".foo"` but
     `Glob("*", period=true)` will do it.
     
    Note that in any case, directories `"."` and `".."` are never
    matched.
 
The set of wildcards actually supported are as follows:

  * `?`: matches any character.
  
  * `*`: matches any character sequence but does not cross directory
    boundaries.
    
  * `**`: matches any sequence of characters and may cross directory
    boundaries. For instance, some valid usages are `"foo**bar`,
    `foo/**bar`, `foo**/bar` and `foo/**/bar` (note that in this last case, `/**/`
    may match zero or more directory levels).
    
  * `[abc...]`, `[a-z...]`:  matches any character in the given class.
  
  * `[!a...]`: matches any character outside of the given class.
  
  * `{...,..., ... }`: matches any sequence of characters matching the
    subpatterns listed inside the curly brackets separated by commas.
    
  * `\ `: escapes the following character. For instance `\?` matches
    the question mark.  
  
As a general guideline, `scala-glob` mimics the globing support in
[`bash`](https://www.gnu.org/savannah-checkouts/gnu/bash/manual/bash.html#Filename-Expansion),
with the `globstar` extension. In principle, any deviation from there is
considered a bug, though there may be some corner cases where it would
be impossible to behave in the same way as bash without accessing the file
system.

## Limitations

So far, only UNIX style paths are supported. It may probably also work
for Windows paths once you replace the backslash separators (`\ `) by
forward ones (`/`).

## Internals

`scala-glob` compiles the glob expressions into regular expressions
that can then be checked efficiently using Java native Regex support.

The `Glob` object has three slots,`mayBeDir`, `mustBeDir` and `partial`,
containing
[`java.util.regex.Pattern`](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html)
objects that can be inspected or used directly for debugging purposes.
Though they may disappear without notice from future library versions. 

## Support

Bug reports, feature requests and pathes can be submitted using the
regular GitHub [tools](https://github.com/salva/scala-glob).

For questions about the module usage, I would much prefer
then to be posted in a public forum as 
[StackOverflow](https://stackoverflow.com/).
That way they would also become visible to others facing similar
issues in the future. Though, fell free to notify me about then
once posted by email (sfandino@yahoo.com).

## See also

  * Java standard
    [java.nio.file.PathMatcher](https://docs.oracle.com/javase/8/docs/api/java/nio/file/PathMatcher.html)
    class: provides similar functionality, but it doesn't support
    partial matching or indicating whether a directory is required
    after a match (those features are required in order to
    implement and efficient file system walker).

  * [OS-Lib](https://github.com/lihaoyi/os-lib):
    provides an alternative, Scala friendly way to perform most
    tasks related to querying and accessing the file system, including
    globbing. Unfortunately (AFAIK) it is tied to the native
    file system layer.

## Copying

Copyright 2020 Salvador Fandiño (sfandino@yahoo.com)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use the files in this package except in compliance with
the License. You may obtain a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

