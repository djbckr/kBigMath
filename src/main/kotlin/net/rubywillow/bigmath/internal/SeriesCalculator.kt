package net.rubywillow.bigmath.internal

import net.rubywillow.bigmath.BigRational
import java.math.BigDecimal
import java.math.MathContext

abstract class SeriesCalculator(
    private val mathContext: MathContext,
    private val factorCalculator: Iterator<BigRational>,
    private val powerCalculator: Iterator<BigDecimal>,
    private val calculateInPairs: Boolean = false
) {

    private val factors = mutableListOf<BigRational>()

    fun calculate(): BigDecimal {

        val acceptableError = BigDecimal.ONE.movePointLeft(mathContext.precision + 1)
        var sum = BigDecimal.ZERO
        var step: BigDecimal

        var  i = 0
        do {
            var factor = getFactor(i)
            var xToThePower = powerCalculator.next()
            step = factor.numerator.multiply(xToThePower).divide(factor.denominator, mathContext)
            i++

            if (calculateInPairs) {
                factor = getFactor(i)
                xToThePower = powerCalculator.next()
                val step2 = factor.numerator.multiply(xToThePower).divide(factor.denominator, mathContext)
                step += step2
                i++
            }
            sum += step
        } while (step.abs() > acceptableError)
        return sum.round(mathContext)
    }

    @Synchronized
    fun getFactor(index: Int): BigRational {
        while (factors.size <= index) {
            factors.add(factorCalculator.next())
        }
        return factors[index]
    }

}