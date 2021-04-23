package net.rubywillow.bigmath.internal

import net.rubywillow.bigmath.BigRational
import java.math.BigDecimal
import java.math.MathContext


fun seqExpCalculator() = sequence {
    var n = 0
    var oneOverFactorialOfN = BigRational.ONE

    while (true) {
        yield(oneOverFactorialOfN)
        n++
        oneOverFactorialOfN /= n
    }
}

fun powerNIterator(x: BigDecimal, mathContext: MathContext) = sequence {

    var powerOfX: BigDecimal = BigDecimal.ONE

    while (true) {
        yield(powerOfX)
        powerOfX = powerOfX.multiply(x, mathContext)
    }
}

fun seqSinCalculator() = sequence {

    var n: Int = 0
    var negative: Boolean = false
    var factorial2nPlus1: BigRational = BigRational.ONE

    while (true) {

        var factor = factorial2nPlus1.reciprocal()
        if (negative) {
            factor = -factor
        }

        yield(factor)

        n++
        factorial2nPlus1 *= (2 * n)
        factorial2nPlus1 *= (2 * n + 1)
        negative = !negative
    }

}

fun powerTwoNPlusOneIterator(x: BigDecimal, mathContext: MathContext) = sequence {

    val xPowerTwo: BigDecimal = x.multiply(x, mathContext)
    var powerOfX: BigDecimal = x

    while (true) {
        yield(powerOfX)
        powerOfX = powerOfX.multiply(xPowerTwo, mathContext)
    }

}

fun seqASinCalculator() = sequence {

    var n = 0
    var factorial2n = BigRational.ONE
    var factorialN = BigRational.ONE
    var fourPowerN = BigRational.ONE

    while (true) {
        val factor: BigRational = factorial2n / fourPowerN * factorialN * factorialN * (2 * n + 1)

        yield(factor)

        n++;
        factorial2n *= (2 * n - 1) * (2 * n)
        factorialN *= n
        fourPowerN *= 4

    }
}

fun seqCosCalculator() = sequence {

    var n = 0
    var negative = false
    var factorial2n = BigRational.ONE

    while (true) {
        var factor = factorial2n.reciprocal()
        if (negative) {
            factor = -factor
        }

        yield(factor)

        n++;
        factorial2n *= (2 * n - 1) * (2 * n);
        negative = !negative;

    }

}

fun powerTwoNIterator(x: BigDecimal, mathContext: MathContext) = sequence {

    val xPowerTwo: BigDecimal = x.multiply(x, mathContext)
    var powerOfX: BigDecimal = BigDecimal.ONE

    while (true) {
        yield(powerOfX)
        powerOfX = powerOfX.multiply(xPowerTwo, mathContext)
    }

}

fun seqSinhCalculator() = sequence {
    var n = 0
    var factorial2nPlus1 = BigRational.ONE

    while (true) {
        yield(factorial2nPlus1.reciprocal())

        n++
        factorial2nPlus1 *= (2 * n)
        factorial2nPlus1 *= (2 * n + 1)
    }
}

fun seqCoshCalculator() = sequence {
    var n = 0
    var factorial2n = BigRational.ONE

    while (true) {
        yield(factorial2n.reciprocal())

        n++
        factorial2n *= (2 * n - 1) * (2 * n)
    }
}
