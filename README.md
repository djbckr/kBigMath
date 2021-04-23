# kBigMath
### a Kotlinized version of BigMath
### Derived from https://github.com/eobermuhlner/big-math

This project appreciates the work done by eobermuhlner. It's a well-done
library that is well written and appears to be well tested.

While eobermuhlner/big-math can be used in Kotlin as-is, it doesn't lend itself
to some of the Kotlin way of doing things. The internal sequential calculators
underwent particularly large changes which reduced the code-base by quite a bit.

I also added operator functions where I could, although having to pass MathContext
in many operations precluded some of those options - particularly divide().
