package net.rubywillow.bigmath

/** Derived from ch.obermuhlner.math.big
 *
 *  Advanced functions for operating on BigDecimal
 *
 */

import net.rubywillow.bigmath.internal.*
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.ln
import kotlin.math.pow

private fun checkMathContext(mathContext: MathContext) {
    if (mathContext.precision == 0) {
        throw UnsupportedOperationException("Unlimited MathContext not supported")
    }
}

private fun BigDecimal.expIntegralFractional(mathContext: MathContext): BigDecimal {
    val integralPart: BigDecimal = this.integralPart()
    if (integralPart.signum() == 0) {
        return this.expTaylor(mathContext)
    }
    val fractionalPart = this.subtract(integralPart)
    val mc = MathContext(mathContext.precision + 10, mathContext.roundingMode)
    val z: BigDecimal = BigDecimal.ONE.add(fractionalPart.divide(integralPart, mc))
    val t = z.expTaylor(mc)
    val result: BigDecimal = t.pow(integralPart.intValueExact().toLong(), mc)
    return result.round(mathContext)
}

private fun BigDecimal.expTaylor(mathContext: MathContext): BigDecimal {
    var x = this
    val mc = MathContext(mathContext.precision + 6, mathContext.roundingMode)
    x = x.divide(BigDecimal.valueOf(256), mc)
    var result: BigDecimal = ExpCalculator(x, mc).calculate()
    result = result.pow(256, mc)
    return result.round(mathContext)
}

fun BigDecimal.exp(mathContext: MathContext): BigDecimal {
    checkMathContext(mathContext)

    if (this.signum() == 0) {
        return BigDecimal.ONE
    }

    return this.expIntegralFractional(mathContext)
}

fun BigDecimal.mantissa(): BigDecimal {
    val exponent = this.exponent()
    return if (exponent == 0) {
        this
    } else this.movePointLeft(exponent)
}

fun BigDecimal.exponent(): Int {
    return this.precision() - this.scale() - 1
}

private fun piChudnovski(mathContext: MathContext): BigDecimal {
    val mc = MathContext(mathContext.precision + 10, mathContext.roundingMode)
    val value24 = BigDecimal.valueOf(24)
    val value640320 = BigDecimal.valueOf(640320)
    val value13591409 = BigDecimal.valueOf(13591409)
    val value545140134 = BigDecimal.valueOf(545140134)
    val valueDivisor = value640320.pow(3).divide(value24, mc)
    var sumA = BigDecimal.ONE
    var sumB = BigDecimal.ZERO
    var a = BigDecimal.ONE
    var dividendTerm1: Long = 5 // -(6*k - 5)
    var dividendTerm2: Long = -1 // 2*k - 1
    var dividendTerm3: Long = -1 // 6*k - 1
    var kPower3: BigDecimal
    val iterationCount = ((mc.precision + 13) / 14).toLong()
    for (k in 1..iterationCount) {
        val valueK = BigDecimal.valueOf(k)
        dividendTerm1 += -6
        dividendTerm2 += 2
        dividendTerm3 += 6
        val dividend = BigDecimal.valueOf(dividendTerm1).multiply(BigDecimal.valueOf(dividendTerm2))
            .multiply(BigDecimal.valueOf(dividendTerm3))
        kPower3 = valueK.pow(3)
        val divisor = kPower3.multiply(valueDivisor, mc)
        a = a.multiply(dividend).divide(divisor, mc)
        val b = valueK.multiply(a, mc)
        sumA = sumA.add(a)
        sumB = sumB.add(b)
    }
    val value426880 = BigDecimal.valueOf(426880)
    val value10005 = BigDecimal.valueOf(10005)
    val factor = value426880.multiply(value10005.sqrt(mc))
    val pi = factor.divide(value13591409.multiply(sumA, mc).add(value545140134.multiply(sumB, mc)), mc)
    return pi.round(mathContext)
}

private val log2Cache: BigDecimal by lazy {
    TWO.logUsingNewton(MATH_CONTEXT)
}

private val log3Cache: BigDecimal by lazy {
    THREE.logUsingNewton(MATH_CONTEXT)
}

private val log10Cache: BigDecimal by lazy {
    BigDecimal.TEN.logUsingNewton(MATH_CONTEXT)
}

private val piCache: BigDecimal by lazy {
    piChudnovski(MATH_CONTEXT)
}

private val eCache: BigDecimal by lazy {
    BigDecimal.ONE.exp(MATH_CONTEXT)
}

private fun BigDecimal.logUsingNewton(mathContext: MathContext): BigDecimal {
    val maxPrecision = mathContext.precision + 20
    val acceptableError: BigDecimal = BigDecimal.ONE.movePointLeft(mathContext.precision + 1)

    val doubleX: Double = this.toDouble()
    var adaptivePrecision: Int
    var result: BigDecimal
    if (doubleX > 0.0 && this.isDoubleValue()) {
        result = BigDecimal.valueOf(ln(doubleX))
        adaptivePrecision = EXPECTED_INITIAL_PRECISION
    } else {
        result = this.divide(TWO, mathContext)
        adaptivePrecision = 1
    }

    var step: BigDecimal

    do {
        adaptivePrecision *= 3
        if (adaptivePrecision > maxPrecision) {
            adaptivePrecision = maxPrecision
        }
        val mc = MathContext(adaptivePrecision, mathContext.roundingMode)

        val expY: BigDecimal = result.exp(mc)
        step = TWO.multiply(this.subtract(expY)).divide(this.add(expY), mc)
        result += step

    } while (adaptivePrecision < maxPrecision || step.abs() > acceptableError)

    return result
}

fun BigDecimal.isDoubleValue(): Boolean {
    if (this > DOUBLE_MAX_VALUE) {
        return false
    }
    return this >= DOUBLE_MAX_VALUE.negate()
}

private val MATH_CONTEXT = MathContext(255, RoundingMode.HALF_DOWN)
private val TWO = BigDecimal("2")
private val THREE = BigDecimal("3")
private val MINUS_ONE = BigDecimal("-1")
private val ONE_HALF = BigDecimal("0.5")
private val DOUBLE_MAX_VALUE: BigDecimal = BigDecimal.valueOf(Double.MAX_VALUE)
private const val EXPECTED_INITIAL_PRECISION = 15

private val spougeFactorialConstantsCache: MutableMap<Int, List<BigDecimal>> = HashMap()
private val ROUGHLY_TWO_PI = BigDecimal("3.141592653589793") * TWO

fun BigDecimal.roundWithTrailingZeroes(mathContext: MathContext): BigDecimal {
    if (this.precision() == mathContext.precision) {
        return this
    }
    return if (this.signum() == 0) {
        BigDecimal.ZERO.setScale(mathContext.precision - 1)
    } else try {
        val stripped = this.stripTrailingZeros()
        val exponentStripped: Int = stripped.exponent() // value.precision() - value.scale() - 1;
        val zero: BigDecimal = if (exponentStripped < -1) {
            BigDecimal.ZERO.setScale(mathContext.precision - exponentStripped)
        } else {
            BigDecimal.ZERO.setScale(mathContext.precision + exponentStripped + 1)
        }
        stripped.add(zero, mathContext)
    } catch (ex: ArithmeticException) {
        this.round(mathContext)
    }
}

fun BigDecimal.integralPart(): BigDecimal {
    return this.setScale(0, RoundingMode.HALF_DOWN)
}

fun BigDecimal.fractionalPart(): BigDecimal {
    return this.subtract(this.integralPart())
}


fun BigDecimal.reciprocal(mathContext: MathContext): BigDecimal {
    return BigDecimal.ONE.divide(this, mathContext)
}

private fun BigDecimal.powInteger(integerY: BigDecimal, mathContext: MathContext): BigDecimal {
    var x = this
    var integerY = integerY
    require(integerY.fractionalPart().signum() == 0) { "Not integer value: $integerY" }
    if (integerY.signum() < 0) {
        return BigDecimal.ONE.divide(x.powInteger(-integerY, mathContext), mathContext)
    }
    val mc = MathContext(mathContext.precision.coerceAtLeast(-integerY.scale()) + 30, mathContext.roundingMode)
    var result: BigDecimal = BigDecimal.ONE
    while (integerY.signum() > 0) {
        var halfY = integerY.divide(TWO, mc)
        if (halfY.fractionalPart().signum() != 0) {
            // odd exponent -> multiply result with x
            result = result.multiply(x, mc)
            integerY = integerY.subtract(BigDecimal.ONE)
            halfY = integerY.divide(TWO, mc)
        }
        if (halfY.signum() > 0) {
            // even exponent -> square x
            x = x.multiply(x, mc)
        }
        integerY = halfY
    }
    return result.round(mathContext)
}

fun BigDecimal.log(mathContext: MathContext): BigDecimal {
    checkMathContext(mathContext)
    if (this.signum() <= 0) {
        throw ArithmeticException("Illegal log(x) for x <= 0: x = $this")
    }
    if (this.compareTo(BigDecimal.ONE) == 0) {
        return BigDecimal.ZERO
    }
    val result: BigDecimal = when (this.compareTo(BigDecimal.TEN)) {
        0 -> log10Cache
        1 -> this.logUsingExponent(mathContext)
        else -> this.logUsingTwoThree(mathContext)
    }
    return result.round(mathContext)
}

private fun BigDecimal.logUsingExponent(mathContext: MathContext): BigDecimal {
    val mc = MathContext(mathContext.precision + 4, mathContext.roundingMode)
    //System.out.println("logUsingExponent(" + x + " " + mathContext + ") precision " + mc);
    val exponent = this.exponent()
    val mantissa = this.mantissa()
    var result: BigDecimal = mantissa.logUsingTwoThree(mc)
    if (exponent != 0) {
        result = result.add(BigDecimal.valueOf(exponent.toLong()).multiply(log10Cache, mc))
    }
    return result
}

private fun BigDecimal.logUsingTwoThree(mathContext: MathContext): BigDecimal {
    val mc = MathContext(mathContext.precision + 4, mathContext.roundingMode)
    //System.out.println("logUsingTwoThree(" + x + " " + mathContext + ") precision " + mc);
    var factorOfTwo = 0L
    var powerOfTwo = 1L
    var factorOfThree = 0L
    var powerOfThree = 1L
    var value = this.toDouble()
    when {
        value < 0.01 -> {
            // do nothing
        }
        value < 0.1 -> { // never happens when called by logUsingExponent()
            while (value < 0.6) {
                value *= 2.0
                factorOfTwo--
                powerOfTwo = powerOfTwo shl 1
            }
        }
        value < 0.115 -> { // (0.1 - 0.11111 - 0.115) -> (0.9 - 1.0 - 1.035)
            factorOfThree = -2
            powerOfThree = 9
        }
        value < 0.14 -> { // (0.115 - 0.125 - 0.14) -> (0.92 - 1.0 - 1.12)
            factorOfTwo = -3
            powerOfTwo = 8
        }
        value < 0.2 -> { // (0.14 - 0.16667 - 0.2) - (0.84 - 1.0 - 1.2)
            factorOfTwo = -1
            powerOfTwo = 2
            factorOfThree = -1
            powerOfThree = 3
        }
        value < 0.3 -> { // (0.2 - 0.25 - 0.3) -> (0.8 - 1.0 - 1.2)
            factorOfTwo = -2
            powerOfTwo = 4
        }
        value < 0.42 -> { // (0.3 - 0.33333 - 0.42) -> (0.9 - 1.0 - 1.26)
            factorOfThree = -1
            powerOfThree = 3
        }
        value < 0.7 -> { // (0.42 - 0.5 - 0.7) -> (0.84 - 1.0 - 1.4)
            factorOfTwo = -1
            powerOfTwo = 2
        }
        value < 1.4 -> { // (0.7 - 1.0 - 1.4) -> (0.7 - 1.0 - 1.4)
            // do nothing
        }
        value < 2.5 -> { // (1.4 - 2.0 - 2.5) -> (0.7 - 1.0 - 1.25)
            factorOfTwo = 1
            powerOfTwo = 2
        }
        value < 3.5 -> { // (2.5 - 3.0 - 3.5) -> (0.833333 - 1.0 - 1.166667)
            factorOfThree = 1
            powerOfThree = 3
        }
        value < 5.0 -> { // (3.5 - 4.0 - 5.0) -> (0.875 - 1.0 - 1.25)
            factorOfTwo = 2
            powerOfTwo = 4
        }
        value < 7.0 -> { // (5.0 - 6.0 - 7.0) -> (0.833333 - 1.0 - 1.166667)
            factorOfThree = 1
            powerOfThree = 3
            factorOfTwo = 1
            powerOfTwo = 2
        }
        value < 8.5 -> { // (7.0 - 8.0 - 8.5) -> (0.875 - 1.0 - 1.0625)
            factorOfTwo = 3
            powerOfTwo = 8
        }
        value < 10.0 -> { // (8.5 - 9.0 - 10.0) -> (0.94444 - 1.0 - 1.11111)
            factorOfThree = 2
            powerOfThree = 9
        }
        else -> {
            while (value > 1.4) { // never happens when called by logUsingExponent()
                value /= 2.0
                factorOfTwo++
                powerOfTwo = powerOfTwo shl 1
            }
        }
    }
    var correctedX = this
    var result: BigDecimal = BigDecimal.ZERO

    when {
        factorOfTwo > 0 -> {
            correctedX = correctedX.divide(BigDecimal.valueOf(powerOfTwo), mc)
            result = result.add(log2Cache.multiply(BigDecimal.valueOf(factorOfTwo), mc))
        }
        factorOfTwo < 0 -> {
            correctedX = correctedX.multiply(BigDecimal.valueOf(powerOfTwo), mc)
            result = result.subtract(log2Cache.multiply(BigDecimal.valueOf(-factorOfTwo), mc))
        }
    }

    when {
        factorOfThree > 0 -> {
            correctedX = correctedX.divide(BigDecimal.valueOf(powerOfThree), mc)
            result = result.add(log3Cache.multiply(BigDecimal.valueOf(factorOfThree), mc))
        }
        factorOfThree < 0 -> {
            correctedX = correctedX.multiply(BigDecimal.valueOf(powerOfThree), mc)
            result = result.subtract(log3Cache.multiply(BigDecimal.valueOf(-factorOfThree), mc))
        }
    }

    if (this === correctedX && result === BigDecimal.ZERO) {
        return this.logUsingNewton(mathContext)
    }

    result = result.add(correctedX.logUsingNewton(mc), mc)
    return result
}

fun BigDecimal.pow(y: BigDecimal, mathContext: MathContext): BigDecimal {
    checkMathContext(mathContext)
    if (this.signum() == 0) {
        when (y.signum()) {
            0 -> return BigDecimal.ONE.round(mathContext)
            1 -> return BigDecimal.ZERO.round(mathContext)
        }
    }

    try {
        val longValue = y.longValueExact()
        return this.pow(longValue, mathContext)
    } catch (ex: ArithmeticException) {
        // ignored
    }
    if (y.fractionalPart().signum() == 0) {
        return this.powInteger(y, mathContext)
    }

    val mc = MathContext(mathContext.precision + 6, mathContext.roundingMode)
    val result = y.multiply(this.log(mc), mc).exp(mc)
    return result.round(mathContext)
}

fun BigDecimal.pow(y: Long, mathContext: MathContext): BigDecimal {
    var x = this
    var y = y
    val mc = if (mathContext.precision == 0) mathContext else MathContext(
        mathContext.precision + 10,
        mathContext.roundingMode
    )

    if (y < 0) {
        val value: BigDecimal = this.pow(-y, mc).reciprocal(mc)
        return value.round(mathContext)
    }
    var result: BigDecimal = BigDecimal.ONE
    while (y > 0) {
        if (y and 1 == 1L) {
            // odd exponent -> multiply result with x
            result = result.multiply(x, mc)
            y -= 1
        }
        if (y > 0) {
            // even exponent -> square x
            x = x.multiply(x, mc)
        }
        y = y shr 1
    }
    return result.round(mathContext)
}

fun BigDecimal.log2(mathContext: MathContext): BigDecimal {
    checkMathContext(mathContext)
    val mc = MathContext(mathContext.precision + 4, mathContext.roundingMode)
    val result = this.log(mc).divide(log2Cache.round(mc), mc)
    return result.round(mathContext)
}

fun BigDecimal.log10(mathContext: MathContext): BigDecimal {
    checkMathContext(mathContext)
    val mc = MathContext(mathContext.precision + 2, mathContext.roundingMode)
    val result = this.log(mc).divide(log10Cache.round(mc), mc)
    return result.round(mathContext)
}


fun BigDecimal.isIntValue(): Boolean {
    try {
        this.intValueExact()
        return true
    } catch (ex: ArithmeticException) {
        // ignored
    }
    return false
}

fun BigDecimal.isLongValue(): Boolean {
    try {
        this.longValueExact()
        return true
    } catch (ex: ArithmeticException) {
        // ignored
    }
    return false
}

fun BigDecimal.significantDigits(): Int {
    val stripped = this.stripTrailingZeros()
    return if (stripped.scale() >= 0) {
        stripped.precision()
    } else {
        stripped.precision() - stripped.scale()
    }
}

fun pi(mathContext: MathContext): BigDecimal {
    checkMathContext(mathContext)
    return piCache.round(mathContext)
}

fun BigDecimal.gamma(mathContext: MathContext): BigDecimal {
    return this.subtract(BigDecimal.ONE).factorial(mathContext)
}

fun BigDecimal.factorial(mathContext: MathContext): BigDecimal {
    if (this.isIntValue()) {
        return BigDecimalMath.factorial(this.intValueExact()).round(mathContext)
    }

    // https://en.wikipedia.org/wiki/Spouge%27s_approximation
    checkMathContext(mathContext)
    val mc = MathContext(mathContext.precision shl 1, mathContext.roundingMode)
    val a = mathContext.precision * 13 / 10
    val constants = getSpougeFactorialConstants(a)
    val bigA = BigDecimal.valueOf(a.toLong())
    var negative = false
    var factor = constants[0]
    for (k in 1 until a) {
        val bigK = BigDecimal.valueOf(k.toLong())
        factor = factor.add(constants[k].divide(this.add(bigK), mc))
        negative = !negative
    }
    var result = this.add(bigA).pow(this.add(BigDecimal.valueOf(0.5)), mc)
    result = result.multiply(this.negate().subtract(bigA).exp(mc))
    result *= factor
    return result.round(mathContext)
}

@Synchronized
fun getSpougeFactorialConstants(a: Int): List<BigDecimal> {
    return spougeFactorialConstantsCache.getOrPut(a) {

        val constants: MutableList<BigDecimal> = ArrayList(a)
        val mc = MathContext(a * 15 / 10)

        val c0 = pi(mc).multiply(TWO, mc).sqrt(mc)
        constants.add(c0)

        var negative = false
        for (k in 1 until a) {
            val bigK = BigDecimal.valueOf(k.toLong())
            val deltaAK = a.toLong() - k
            var ck = BigDecimal.valueOf(deltaAK).pow(bigK.subtract(ONE_HALF), mc)
            ck = ck.multiply(BigDecimal.valueOf(deltaAK).exp(mc), mc)
            ck = ck.divide(BigDecimalMath.factorial(k - 1), mc)
            if (negative) {
                ck = ck.negate()
            }
            constants.add(ck)
            negative = !negative
        }

        return constants.toList()
    }
}

fun BigDecimal.root(n: BigDecimal, mathContext: MathContext): BigDecimal {
    checkMathContext(mathContext)
    when (n.signum()) {
        -1, 0 -> throw ArithmeticException("Illegal root(x, n) for n <= 0: n = $n")
    }
    when (this.signum()) {
        0 -> return BigDecimal.ZERO
        -1 -> throw ArithmeticException("Illegal root(x, n) for x < 0: x = $this")
    }
    if (this.isDoubleValue() && n.isDoubleValue()) {
        val initialResult = this.toDouble().pow(1.0 / n.toDouble())
        if (java.lang.Double.isFinite(initialResult)) {
            return this.rootUsingNewtonRaphson(n, BigDecimal.valueOf(initialResult), mathContext)
        }
    }
    val mc = MathContext(mathContext.precision + 6, mathContext.roundingMode)
    return this.pow(BigDecimal.ONE.divide(n, mc), mathContext)
}

private fun BigDecimal.rootUsingNewtonRaphson(
    n: BigDecimal,
    initialResult: BigDecimal,
    mathContext: MathContext
): BigDecimal {
    if (n <= BigDecimal.ONE) {
        val mc = MathContext(mathContext.precision + 6, mathContext.roundingMode)
        return this.pow(BigDecimal.ONE.divide(n, mc), mathContext)
    }
    val maxPrecision = mathContext.precision * 2
    val acceptableError: BigDecimal = BigDecimal.ONE.movePointLeft(mathContext.precision + 1)
    val nMinus1 = n.subtract(BigDecimal.ONE)
    var result = initialResult
    var adaptivePrecision = 12
    if (adaptivePrecision < maxPrecision) {
        var step: BigDecimal
        do {
            adaptivePrecision *= 3
            if (adaptivePrecision > maxPrecision) {
                adaptivePrecision = maxPrecision
            }
            val mc = MathContext(adaptivePrecision, mathContext.roundingMode)
            step = this.divide(result.pow(nMinus1, mc), mc).subtract(result).divide(n, mc)
            result = result.add(step)
        } while (adaptivePrecision < maxPrecision || step.abs().compareTo(acceptableError) > 0)
    }
    return result.round(mathContext)
}

fun e(mathContext: MathContext): BigDecimal {
    checkMathContext(mathContext)
    return eCache.round(mathContext)
}

fun BigDecimal.sin(mathContext: MathContext): BigDecimal {
    var x = this
    checkMathContext(mathContext)
    val mc = MathContext(mathContext.precision + 6, mathContext.roundingMode)
    if (x.abs() > ROUGHLY_TWO_PI) {
        val mc2 = MathContext(mc.precision + 4, mathContext.roundingMode)
        val twoPi = TWO * pi(mc2)
        x = x.remainder(twoPi, mc2)
    }
    val result: BigDecimal = SinCalculator(x, mc).calculate()
    return result.round(mathContext)
}

fun BigDecimal.asin(mathContext: MathContext): BigDecimal {
    checkMathContext(mathContext)
    if (this > BigDecimal.ONE) {
        throw ArithmeticException("Illegal asin(x) for x > 1: x = $this")
    }
    if (this < MINUS_ONE) {
        throw ArithmeticException("Illegal asin(x) for x < -1: x = $this")
    }

    if (this.signum() == -1) {
        return this.negate().asin(mathContext).negate()
    }

    val mc = MathContext(mathContext.precision + 6, mathContext.roundingMode)

    if (this >= BigDecimal.valueOf(0.707107)) {
        val xTransformed = BigDecimal.ONE.subtract(this.multiply(this)).sqrt(mc)
        return xTransformed.acos(mathContext)
    }

    val result: BigDecimal = ASinCalculator(this, mc).calculate()
    return result.round(mathContext)
}

fun BigDecimal.cos(mathContext: MathContext): BigDecimal {
    var x = this
    checkMathContext(mathContext)
    val mc = MathContext(mathContext.precision + 6, mathContext.roundingMode)
    if (x.abs() > ROUGHLY_TWO_PI) {
        val mc2 = MathContext(mc.precision + 4, mathContext.roundingMode)
        val twoPi = TWO.multiply(pi(mc2), mc2)
        x = x.remainder(twoPi, mc2)
    }
    val result: BigDecimal = CosCalculator(x, mc).calculate()
    return result.round(mathContext)
}

fun BigDecimal.acos(mathContext: MathContext): BigDecimal {
    checkMathContext(mathContext)
    if (this > BigDecimal.ONE) {
        throw ArithmeticException("Illegal acos(x) for x > 1: x = $this")
    }
    if (this < MINUS_ONE) {
        throw ArithmeticException("Illegal acos(x) for x < -1: x = $this")
    }
    val mc = MathContext(mathContext.precision + 6, mathContext.roundingMode)
    val result = pi(mc).divide(TWO, mc).subtract(this.asin(mc))
    return result.round(mathContext)
}

fun BigDecimal.tan(mathContext: MathContext): BigDecimal {
    checkMathContext(mathContext)
    if (this.signum() == 0) {
        return BigDecimal.ZERO
    }
    val mc = MathContext(mathContext.precision + 4, mathContext.roundingMode)
    val result = this.sin(mc).divide(this.cos(mc), mc)
    return result.round(mathContext)
}

fun BigDecimal.atan(mathContext: MathContext): BigDecimal {
    var x = this
    checkMathContext(mathContext)
    val mc = MathContext(mathContext.precision + 6, mathContext.roundingMode)
    x = x.divide(BigDecimal.ONE.add(x.multiply(x, mc)).sqrt(mc), mc)
    val result = this.asin(mc)
    return result.round(mathContext)
}

fun BigDecimal.atan2(x: BigDecimal, mathContext: MathContext): BigDecimal {
    checkMathContext(mathContext)
    val mc = MathContext(mathContext.precision + 3, mathContext.roundingMode)
    return when {
        x.signum() > 0 -> { // x > 0
            this.divide(x, mc).atan(mathContext)
        }
        x.signum() < 0 -> {
            when {
                this.signum() > 0 -> {  // x < 0 && y > 0
                    this.divide(x, mc).atan(mc).add(pi(mc), mathContext)
                }
                this.signum() < 0 -> { // x < 0 && y < 0
                    this.divide(x, mc).atan(mc).subtract(pi(mc), mathContext)
                }
                else -> { // x < 0 && y = 0
                    pi(mathContext)
                }
            }
        }
        else -> {
            when {
                this.signum() > 0 -> { // x == 0 && y > 0
                    pi(mc).divide(TWO, mathContext)
                }
                this.signum() < 0 -> {  // x == 0 && y < 0
                    pi(mc).divide(TWO, mathContext).negate()
                }
                else -> {
                    throw ArithmeticException("Illegal atan2(y, x) for x = 0; y = 0")
                }
            }
        }
    }
}

fun BigDecimal.cot(mathContext: MathContext): BigDecimal {
    checkMathContext(mathContext)
    if (this.signum() == 0) {
        throw ArithmeticException("Illegal cot(x) for x = 0")
    }
    val mc = MathContext(mathContext.precision + 4, mathContext.roundingMode)
    val result = this.cos(mc).divide(this.sin(mc), mc)
    return result.round(mathContext)
}

fun BigDecimal.acot(mathContext: MathContext): BigDecimal {
    checkMathContext(mathContext)
    val mc = MathContext(mathContext.precision + 4, mathContext.roundingMode)
    val result = pi(mc).divide(TWO, mc).subtract(this.atan(mc))
    return result.round(mathContext)
}

fun BigDecimal.sinh(mathContext: MathContext): BigDecimal {
    checkMathContext(mathContext)
    val mc = MathContext(mathContext.precision + 4, mathContext.roundingMode)
    val result: BigDecimal = SinhCalculator(this, mc).calculate()
    return result.round(mathContext)
}

fun BigDecimal.cosh(mathContext: MathContext): BigDecimal {
    checkMathContext(mathContext)
    val mc = MathContext(mathContext.precision + 4, mathContext.roundingMode)
    val result: BigDecimal = CoshCalculator(this, mc).calculate()
    return result.round(mathContext)
}

fun BigDecimal.tanh(mathContext: MathContext): BigDecimal {
    checkMathContext(mathContext)
    val mc = MathContext(mathContext.precision + 6, mathContext.roundingMode)
    val result = this.sinh(mc).divide(this.cosh(mc), mc)
    return result.round(mathContext)
}

fun BigDecimal.coth(mathContext: MathContext): BigDecimal {
    checkMathContext(mathContext)
    val mc = MathContext(mathContext.precision + 6, mathContext.roundingMode)
    val result = this.cosh(mc).divide(this.sinh(mc), mc)
    return result.round(mathContext)
}

fun BigDecimal.asinh(mathContext: MathContext): BigDecimal {
    checkMathContext(mathContext)
    val mc = MathContext(mathContext.precision + 10, mathContext.roundingMode)
    val result = this.add(this.multiply(this, mc).add(BigDecimal.ONE, mc).sqrt(mc)).log(mc)
    return result.round(mathContext)
}

fun BigDecimal.acosh(mathContext: MathContext): BigDecimal {
    checkMathContext(mathContext)
    val mc = MathContext(mathContext.precision + 6, mathContext.roundingMode)
    val result = this.add(this.multiply(this).subtract(BigDecimal.ONE).sqrt(mc)).log(mc)
    return result.round(mathContext)
}

fun BigDecimal.atanh(mathContext: MathContext): BigDecimal {
    if (this >= BigDecimal.ONE) {
        throw ArithmeticException("Illegal atanh(x) for x >= 1: x = $this")
    }
    if (this <= MINUS_ONE) {
        throw ArithmeticException("Illegal atanh(x) for x <= -1: x = $this")
    }
    checkMathContext(mathContext)
    val mc = MathContext(mathContext.precision + 6, mathContext.roundingMode)
    val result = BigDecimal.ONE.add(this).divide(BigDecimal.ONE.subtract(this), mc).log(mc).multiply(ONE_HALF)
    return result.round(mathContext)
}

fun BigDecimal.acoth(mathContext: MathContext): BigDecimal {
    checkMathContext(mathContext)
    val mc = MathContext(mathContext.precision + 6, mathContext.roundingMode)
    val result = this.add(BigDecimal.ONE).divide(this.subtract(BigDecimal.ONE), mc).log(mc).multiply(ONE_HALF)
    return result.round(mathContext)
}


object BigDecimalMath {

    private val factorialCache = ArrayList<BigDecimal>(100)

    init {
        var result: BigDecimal = BigDecimal.ONE
        for (i in 0L until 100) {
            factorialCache.add(result)
            result = result.multiply(BigDecimal.valueOf(i))
        }
        factorialCache.add(result)
    }

    fun toBigDecimal(string: String): BigDecimal {
        return toBigDecimal(string, MathContext.UNLIMITED)
    }

    fun toBigDecimal(string: String, mathContext: MathContext): BigDecimal {
        val len = string.length
        if (len < 600) {
            return BigDecimal(string, mathContext)
        }
        val splitLength = len / if (len >= 10000) 8 else 5
        return toBigDecimal(string, mathContext, splitLength)
    }

    fun toBigDecimal(string: String, mathContext: MathContext, splitLength: Int): BigDecimal {
        val len = string.length
        if (len < splitLength) {
            return BigDecimal(string, mathContext)
        }
        val chars = string.toCharArray()
        var numberHasSign = false
        var negative = false
        var numberIndex = 0
        var dotIndex = -1
        var expIndex = -1
        var expHasSign = false
        var scale = 0
        for (i in 0 until len) {
            when (chars[i]) {
                '+' -> if (expIndex >= 0) {
                    if (expHasSign) {
                        throw NumberFormatException("Multiple signs in exponent")
                    }
                    expHasSign = true
                } else {
                    if (numberHasSign) {
                        throw NumberFormatException("Multiple signs in number")
                    }
                    numberHasSign = true
                    numberIndex = i + 1
                }
                '-' -> if (expIndex >= 0) {
                    if (expHasSign) {
                        throw NumberFormatException("Multiple signs in exponent")
                    }
                    expHasSign = true
                } else {
                    if (numberHasSign) {
                        throw NumberFormatException("Multiple signs in number")
                    }
                    numberHasSign = true
                    negative = true
                    numberIndex = i + 1
                }
                'e', 'E' -> {
                    if (expIndex >= 0) {
                        throw NumberFormatException("Multiple exponent markers")
                    }
                    expIndex = i
                }
                '.' -> {
                    if (dotIndex >= 0) {
                        throw NumberFormatException("Multiple decimal points")
                    }
                    dotIndex = i
                }
                else -> if (dotIndex >= 0 && expIndex == -1) {
                    scale++
                }
            }
        }
        val numberEndIndex: Int
        var exp = 0
        if (expIndex >= 0) {
            numberEndIndex = expIndex
            val expString = String(chars, expIndex + 1, len - expIndex - 1)
            exp = expString.toInt()
            scale = adjustScale(scale, exp.toLong())
        } else {
            numberEndIndex = len
        }
        var result: BigDecimal
        result = if (dotIndex >= 0) {
            val leftLength = dotIndex - numberIndex
            val bigDecimalLeft = toBigDecimalRecursive(chars, numberIndex, leftLength, exp, splitLength)
            val rightLength = numberEndIndex - dotIndex - 1
            val bigDecimalRight =
                toBigDecimalRecursive(chars, dotIndex + 1, rightLength, exp - rightLength, splitLength)
            bigDecimalLeft.add(bigDecimalRight)
        } else {
            toBigDecimalRecursive(chars, numberIndex, numberEndIndex - numberIndex, exp, splitLength)
        }
        if (scale != 0) {
            result = result.setScale(scale)
        }
        if (negative) {
            result = result.negate()
        }
        if (mathContext.precision != 0) {
            result = result.round(mathContext)
        }
        return result
    }

    private fun adjustScale(scale: Int, exp: Long): Int {
        val adjustedScale = scale - exp
        if (adjustedScale > Int.MAX_VALUE || adjustedScale < Int.MIN_VALUE) throw NumberFormatException("Scale out of range: $adjustedScale while adjusting scale $scale to exponent $exp")
        return adjustedScale.toInt()
    }

    private fun toBigDecimalRecursive(
        chars: CharArray,
        offset: Int,
        length: Int,
        scale: Int,
        splitLength: Int
    ): BigDecimal {
        if (length > splitLength) {
            val mid = length / 2
            val bigDecimalLeft = toBigDecimalRecursive(chars, offset, mid, scale + length - mid, splitLength)
            val bigDecimalRight = toBigDecimalRecursive(chars, offset + mid, length - mid, scale, splitLength)
            return bigDecimalLeft.add(bigDecimalRight)
        }
        return if (length == 0) {
            BigDecimal.ZERO
        } else BigDecimal(chars, offset, length).movePointRight(scale)
    }

    fun factorial(n: Int): BigDecimal {
        if (n < 0) {
            throw ArithmeticException("Illegal factorial(n) for n < 0: n = $n")
        }
        if (n < factorialCache.size) {
            return factorialCache[n]
        }
        val result = factorialCache[factorialCache.size - 1]
        return result * factorialRecursion(factorialCache.size, n)
    }

    private fun factorialLoop(n1: Int, n2: Int): BigDecimal {
        var n1 = n1.toLong()
        val limit = Long.MAX_VALUE / n2
        var accu: Long = 1
        var result = BigDecimal.ONE
        while (n1 <= n2) {
            if (accu <= limit) {
                accu *= n1
            } else {
                result *= BigDecimal.valueOf(accu)
                accu = n1
            }
            n1++
        }
        return result * BigDecimal.valueOf(accu)
    }

    private fun factorialRecursion(n1: Int, n2: Int): BigDecimal {
        val threshold = if (n1 > 200) 80 else 150
        if (n2 - n1 < threshold) {
            return factorialLoop(n1, n2)
        }
        val mid = n1 + n2 shr 1
        return factorialRecursion(mid + 1, n2).multiply(factorialRecursion(n1, mid))
    }

    fun bernoulli(n: Int, mathContext: MathContext): BigDecimal {
        if (n < 0) {
            throw ArithmeticException("Illegal bernoulli(n) for n < 0: n = $n")
        }
        return BigRational.bernoulli(n).toBigDecimal(mathContext)
    }


}