# Scracthpad

## Fastparse

### Lookahead that keeps the result:

```scala
  def &![_:P, A](a: => P[A]): P[A] = {
    var saved: Option[A] = None
    def save(a:A): Boolean = { saved = Some(a); true }
    P(&(a.filter(save(_))) ~ Pass.map(_ => saved.get))
  }

  def both[_:P] = P(&!(AnyChar.!.map(a=>Literal(a))) ~ (AnyChar ~ (AnyChar.!.map(c=>Literal(c)))))
```