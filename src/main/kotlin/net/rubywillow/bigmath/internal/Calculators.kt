package net.rubywillow.bigmath.internal

import java.math.BigDecimal
import java.math.MathContext

class ExpCalculator(x: BigDecimal, mathContext: MathContext) : SeriesCalculator(
    mathContext,
    seqExpCalculator().iterator(),
    powerNIterator(x, mathContext).iterator()
)

class SinCalculator(x: BigDecimal, mathContext: MathContext) : SeriesCalculator(
    mathContext,
    seqSinCalculator().iterator(),
    powerTwoNPlusOneIterator(x, mathContext).iterator(),
    true
)

class ASinCalculator(x: BigDecimal, mathContext: MathContext) : SeriesCalculator(
    mathContext,
    seqASinCalculator().iterator(),
    powerTwoNPlusOneIterator(x, mathContext).iterator()
)

class CosCalculator(x: BigDecimal, mathContext: MathContext) : SeriesCalculator(
    mathContext,
    seqCosCalculator().iterator(),
    powerTwoNIterator(x, mathContext).iterator(),
    true
)

class SinhCalculator(x: BigDecimal, mathContext: MathContext) : SeriesCalculator(
    mathContext,
    seqSinhCalculator().iterator(),
    powerTwoNPlusOneIterator(x, mathContext).iterator(),
    true
)

class CoshCalculator(x: BigDecimal, mathContext: MathContext) : SeriesCalculator(
    mathContext,
    seqCoshCalculator().iterator(),
    powerTwoNIterator(x, mathContext).iterator(),
    true
)
