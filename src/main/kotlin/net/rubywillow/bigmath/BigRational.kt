package net.rubywillow.bigmath

import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode


/** Derived from ch.obermuhlner.math.big
 *
 * A rational number represented as a quotient of two values.
 *
 * Basic calculations with rational numbers (+ - * /) have no loss of precision.
 * This allows to use {@link BigRational} as a replacement for {@link BigDecimal} if absolute accuracy is desired.
 *
 * See http://en.wikipedia.org/wiki/Rational_number
 *
 * The values are internally stored as {@link BigDecimal} (for performance optimizations) but represented
 * as {@link BigInteger} (for mathematical correctness)
 * when accessed with {@link #getNumeratorBigInteger()} and {@link #getDenominatorBigInteger()}.
 *
 * The following basic calculations have no loss of precision:
 * {@link #add(BigRational)}
 * {@link #subtract(BigRational)}
 * {@link #multiply(BigRational)}
 * {@link #divide(BigRational)}
 * {@link #pow(int)}
 *
 * The following calculations are special cases of the ones listed above and have no loss of precision:
 * {@link #negate()}`
 * {@link #reciprocal()}`
 * {@link #increment()}`
 * {@link #decrement()}`
 *
 * Any {@link BigRational} value can be converted into an arbitrary {@link #withPrecision(int) precision} (number of significant digits)
 * or {@link #withScale(int) scale} (number of digits after the decimal point).
 *
 */

class BigRational(num: BigDecimal, dem: BigDecimal = BigDecimal.ONE) {

    val numerator: BigDecimal
    val denominator: BigDecimal

    init {
        if (dem.signum() == 0) {
            throw kotlin.ArithmeticException("Divide by Zero")
        }

        if (dem.signum() < 0) {
            numerator = -num
            denominator = -dem
        } else {
            numerator = num
            denominator = dem
        }

    }


    constructor(value: Int) : this(BigDecimal.valueOf(value.toLong()), BigDecimal.ONE)

    /**
     * Reduces this rational number to the smallest numerator/denominator with the same value.
     *
     * @return the reduced rational number
     */
    fun reduce(): BigRational {
        var n: BigInteger = numerator.toBigInteger()
        var d: BigInteger = denominator.toBigInteger()
        val gcd = n.gcd(d)
        n /= gcd
        d /= gcd
        return valueOf(n, d)
    }

    /**
     * Returns the integer part of this rational number.
     *
     *
     * Examples:
     *
     *  * `BigRational.valueOf(3.5).integerPart()` returns `BigRational.valueOf(3)`
     *
     *
     * @return the integer part of this rational number
     */
    fun integerPart(): BigRational {
        return of(numerator.subtract(numerator.remainder(denominator)), denominator)
    }

    /**
     * Returns the fraction part of this rational number.
     *
     *
     * Examples:
     *
     *  * `BigRational.valueOf(3.5).integerPart()` returns `BigRational.valueOf(0.5)`
     *
     *
     * @return the fraction part of this rational number
     */
    fun fractionPart(): BigRational {
        return of(numerator.remainder(denominator), denominator)
    }

    /**
     * Negates this rational number (inverting the sign).
     *
     *
     * The result has no loss of precision.
     *
     *
     * Examples:
     *
     *  * `BigRational.valueOf(3.5).negate()` returns `BigRational.valueOf(-3.5)`
     *
     *
     * @return the negated rational number
     */
    operator fun unaryMinus(): BigRational {
        return if (isZero()) {
            this
        } else of(numerator.negate(), denominator)
    }

    /**
     * Calculates the reciprocal of this rational number (1/x).
     *
     *
     * The result has no loss of precision.
     *
     *
     * Examples:
     *
     *  * `BigRational.valueOf(0.5).reciprocal()` returns `BigRational.valueOf(2)`
     *  * `BigRational.valueOf(-2).reciprocal()` returns `BigRational.valueOf(-0.5)`
     *
     *
     * @return the reciprocal rational number
     * @throws ArithmeticException if this number is 0 (division by zero)
     */
    fun reciprocal(): BigRational {
        return of(denominator, numerator)
    }

    /**
     * Returns the absolute value of this rational number.
     *
     *
     * The result has no loss of precision.
     *
     *
     * Examples:
     *
     *  * `BigRational.valueOf(-2).abs()` returns `BigRational.valueOf(2)`
     *  * `BigRational.valueOf(2).abs()` returns `BigRational.valueOf(2)`
     *
     *
     * @return the absolute rational number (positive, or 0 if this rational is 0)
     */
    fun abs(): BigRational {
        return if (isPositive()) this else -this
    }

    /**
     * Returns the signum function of this rational number.
     *
     * @return -1, 0 or 1 as the value of this rational number is negative, zero or positive.
     */
    fun signum(): Int {
        return numerator.signum()
    }

    /**
     * Calculates the increment of this rational number (+ 1).
     *
     *
     * This is functionally identical to
     * `this.add(BigRational.ONE)`
     * but slightly faster.
     *
     *
     * The result has no loss of precision.
     *
     * @return the incremented rational number
     */
    operator fun inc(): BigRational {
        return of(numerator + denominator, denominator)
    }

    /**
     * Calculates the decrement of this rational number (- 1).
     *
     *
     * This is functionally identical to
     * `this.subtract(BigRational.ONE)`
     * but slightly faster.
     *
     *
     * The result has no loss of precision.
     *
     * @return the decremented rational number
     */
    operator fun dec(): BigRational {
        return of(numerator - denominator, denominator)
    }

    /**
     * Calculates the addition (+) of this rational number and the specified argument.
     *
     *
     * The result has no loss of precision.
     *
     * @param value the rational number to add
     * @return the resulting rational number
     */
    operator fun plus(value: BigRational): BigRational {
        if (denominator == value.denominator) {
            return of(numerator + value.numerator, denominator)
        }
        val n: BigDecimal = ((numerator * value.denominator) + value.numerator) * denominator
        val d: BigDecimal = denominator * value.denominator
        return of(n, d)
    }

    operator fun plus(value: BigDecimal): BigRational {
        return of((numerator + value) * denominator, denominator)
    }

    /**
     * Calculates the addition (+) of this rational number and the specified argument.
     *
     *
     * This is functionally identical to
     * `this.add(BigRational.valueOf(value))`
     * but slightly faster.
     *
     *
     * The result has no loss of precision.
     *
     * @param value the [BigInteger] to add
     * @return the resulting rational number
     */
    operator fun plus(value: BigInteger): BigRational {
        return if (value == BigInteger.ZERO) {
            this
        } else this + BigDecimal(value)
    }

    /**
     * Calculates the addition (+) of this rational number and the specified argument.
     *
     *
     * This is functionally identical to
     * `this.add(BigRational.valueOf(value))`
     * but slightly faster.
     *
     *
     * The result has no loss of precision.
     *
     * @param value the int value to add
     * @return the resulting rational number
     */
    operator fun plus(value: Int): BigRational {
        return if (value == 0) {
            this
        } else this + BigInteger.valueOf(value.toLong())
    }

    /**
     * Calculates the subtraction (-) of this rational number and the specified argument.
     *
     *
     * The result has no loss of precision.
     *
     * @param value the rational number to subtract
     * @return the resulting rational number
     */
    operator fun minus(value: BigRational): BigRational {
        if (denominator == value.denominator) {
            return of(numerator - value.numerator, denominator)
        }
        val n: BigDecimal = ((numerator * value.denominator) - value.numerator) * denominator
        val d: BigDecimal = denominator * value.denominator
        return of(n, d)
    }


    operator fun minus(value: BigDecimal): BigRational {
        return of((numerator - value) * denominator, denominator)
    }

    /**
     * Calculates the subtraction (-) of this rational number and the specified argument.
     *
     *
     * This is functionally identical to
     * `this.subtract(BigRational.valueOf(value))`
     * but slightly faster.
     *
     *
     * The result has no loss of precision.
     *
     * @param value the [BigInteger] to subtract
     * @return the resulting rational number
     */
    operator fun minus(value: BigInteger): BigRational {
        return if (value == BigInteger.ZERO) {
            this
        } else this - BigDecimal(value)
    }

    /**
     * Calculates the subtraction (-) of this rational number and the specified argument.
     *
     *
     * This is functionally identical to
     * `this.subtract(BigRational.valueOf(value))`
     * but slightly faster.
     *
     *
     * The result has no loss of precision.
     *
     * @param value the int value to subtract
     * @return the resulting rational number
     */
    operator fun minus(value: Int): BigRational {
        return if (value == 0) {
            this
        } else this - BigInteger.valueOf(value.toLong())
    }

    /**
     * Calculates the multiplication (*) of this rational number and the specified argument.
     *
     *
     * The result has no loss of precision.
     *
     * @param value the rational number to multiply
     * @return the resulting rational number
     */
    operator fun times(value: BigRational): BigRational {
        if (isZero() || value.isZero()) {
            return ZERO
        }
        if (this == ONE) {
            return value
        }
        if (value == ONE) {
            return this
        }
        val n: BigDecimal = numerator * value.numerator
        val d: BigDecimal = denominator * value.denominator
        return of(n, d)
    }

    operator fun times(value: BigDecimal): BigRational {
        val n: BigDecimal = numerator * value
        val d: BigDecimal = denominator
        return of(n, d)
    }

    /**
     * Calculates the multiplication (*) of this rational number and the specified argument.
     *
     *
     * This is functionally identical to
     * `this.multiply(BigRational.valueOf(value))`
     * but slightly faster.
     *
     *
     * The result has no loss of precision.
     *
     * @param value the [BigInteger] to multiply
     * @return the resulting rational number
     */
    operator fun times(value: BigInteger): BigRational {
        if (isZero() || value.signum() == 0) {
            return ZERO
        }
        if (this == ONE) {
            return valueOf(value)
        }
        return if (value == BigInteger.ONE) {
            this
        } else {
            this * BigDecimal(value)
        }
    }

    /**
     * Calculates the multiplication (*) of this rational number and the specified argument.
     *
     *
     * This is functionally identical to
     * `this.multiply(BigRational.valueOf(value))`
     * but slightly faster.
     *
     *
     * The result has no loss of precision.
     *
     * @param value the int value to multiply
     * @return the resulting rational number
     */
    operator fun times(value: Int): BigRational {
        return this * BigInteger.valueOf(value.toLong())
    }

    /**
     * Calculates the division (/) of this rational number and the specified argument.
     *
     *
     * The result has no loss of precision.
     *
     * @param value the rational number to divide (0 is not allowed)
     * @return the resulting rational number
     * @throws ArithmeticException if the argument is 0 (division by zero)
     */
    operator fun div(value: BigRational): BigRational {
        if (value == ONE) {
            return this
        }
        val n: BigDecimal = numerator * value.denominator
        val d: BigDecimal = denominator * value.numerator
        return of(n, d)
    }

    operator fun div(value: BigDecimal): BigRational {
        val n: BigDecimal = numerator
        val d: BigDecimal = denominator * value
        return of(n, d)
    }

    /**
     * Calculates the division (/) of this rational number and the specified argument.
     *
     *
     * This is functionally identical to
     * `this.divide(BigRational.valueOf(value))`
     * but slightly faster.
     *
     *
     * The result has no loss of precision.
     *
     * @param value the [BigInteger] to divide (0 is not allowed)
     * @return the resulting rational number
     * @throws ArithmeticException if the argument is 0 (division by zero)
     */
    operator fun div(value: BigInteger): BigRational {
        return if (value == BigInteger.ONE) {
            this
        } else this / BigDecimal(value)
    }

    /**
     * Calculates the division (/) of this rational number and the specified argument.
     *
     *
     * This is functionally identical to
     * `this.divide(BigRational.valueOf(value))`
     * but slightly faster.
     *
     *
     * The result has no loss of precision.
     *
     * @param value the int value to divide (0 is not allowed)
     * @return the resulting rational number
     * @throws ArithmeticException if the argument is 0 (division by zero)
     */
    operator fun div(value: Int): BigRational {
        return this / BigInteger.valueOf(value.toLong())
    }

    /**
     * Returns whether this rational number is zero.
     *
     * @return `true` if this rational number is zero (0), `false` if it is not zero
     */
    fun isZero(): Boolean {
        return numerator.signum() == 0
    }

    private fun isPositive(): Boolean {
        return numerator.signum() > 0
    }

    /**
     * Returns whether this rational number is an integer number without fraction part.
     *
     * @return `true` if this rational number is an integer number, `false` if it has a fraction part
     */
    fun isInteger(): Boolean {
        return isIntegerInternal() || reduce().isIntegerInternal()
    }

    /**
     * Returns whether this rational number is an integer number without fraction part.
     *
     *
     * Will return `false` if this number is not reduced to the integer representation yet (e.g. 4/4 or 4/2)
     *
     * @return `true` if this rational number is an integer number, `false` if it has a fraction part
     * @see .isInteger
     */
    private fun isIntegerInternal(): Boolean {
        return denominator == BigDecimal.ONE
    }

    /**
     * Calculates this rational number to the power (x<sup>y</sup>) of the specified argument.
     *
     *
     * The result has no loss of precision.
     *
     * @param exponent exponent to which this rational number is to be raised
     * @return the resulting rational number
     */
    fun pow(exponent: Int): BigRational {
        if (exponent == 0) {
            return ONE
        }
        if (exponent == 1) {
            return this
        }
        val n: BigInteger
        val d: BigInteger
        if (exponent > 0) {
            n = numerator.toBigInteger().pow(exponent)
            d = denominator.toBigInteger().pow(exponent)
        } else {
            n = denominator.toBigInteger().pow(-exponent)
            d = numerator.toBigInteger().pow(-exponent)
        }
        return valueOf(n, d)
    }

    /**
     * Finds the minimum (smaller) of two rational numbers.
     *
     * @param value the rational number to compare with
     * @return the minimum rational number, either `this` or the argument `value`
     */
    private fun min(value: BigRational): BigRational {
        return if (this <= value) this else value
    }

    /**
     * Finds the maximum (larger) of two rational numbers.
     *
     * @param value the rational number to compare with
     * @return the minimum rational number, either `this` or the argument `value`
     */
    private fun max(value: BigRational): BigRational {
        return if (this >= value) this else value
    }

    /**
     * Returns a rational number with approximatively `this` value and the specified precision.
     *
     * @param precision the precision (number of significant digits) of the calculated result, or 0 for unlimited precision
     * @return the calculated rational number with the specified precision
     */
    fun withPrecision(precision: Int): BigRational {
        return valueOf(toBigDecimal(MathContext(precision)))
    }

    /**
     * Returns a rational number with approximatively `this` value and the specified scale.
     *
     * @param scale the scale (number of digits after the decimal point) of the calculated result
     * @return the calculated rational number with the specified scale
     */
    fun withScale(scale: Int): BigRational {
        return valueOf(toBigDecimal().setScale(scale, RoundingMode.HALF_UP))
    }

    private fun precision(): Int {
        return countDigits(numerator.toBigInteger()) + countDigits(denominator.toBigInteger())
    }

    /**
     * Returns this rational number as a double value.
     *
     * @return the double value
     */
    fun toDouble(): Double {
        return numerator.toDouble() / denominator.toDouble()
    }

    /**
     * Returns this rational number as a float value.
     *
     * @return the float value
     */
    fun toFloat(): Float {
        return numerator.toFloat() / denominator.toFloat()
    }

    /**
     * Returns this rational number as a [BigDecimal].
     *
     * @return the [BigDecimal] value
     */
    fun toBigDecimal(): BigDecimal {
        val precision = Math.max(precision(), MathContext.DECIMAL128.precision)
        return toBigDecimal(MathContext(precision))
    }

    /**
     * Returns this rational number as a [BigDecimal] with the precision specified by the [MathContext].
     *
     * @param mc the [MathContext] specifying the precision of the calculated result
     * @return the [BigDecimal]
     */
    fun toBigDecimal(mc: MathContext): BigDecimal {
        return numerator.divide(denominator, mc)
    }

    operator fun compareTo(other: BigRational): Int {
        return if (this === other) {
            0
        } else {
            (numerator * other.denominator).compareTo(denominator * other.numerator)
        }
    }

    override fun hashCode(): Int {
        return if (isZero()) {
            0
        } else numerator.hashCode() + denominator.hashCode()
    }

    override fun toString(): String {
        if (isZero()) {
            return "0"
        }
        return if (isIntegerInternal()) {
            numerator.toString()
        } else {
            toBigDecimal().toString()
        }
    }

    /**
     * Returns a plain string representation of this rational number without any exponent.
     *
     * @return the plain string representation
     * @see BigDecimal.toPlainString
     */
    fun toPlainString(): String {
        if (isZero()) {
            return "0"
        }
        return if (isIntegerInternal()) {
            numerator.toPlainString()
        } else {
            toBigDecimal().toPlainString()
        }
    }

    /**
     * Returns the string representation of this rational number in the form "numerator/denominator".
     *
     *
     * The resulting string is a valid input of the [.valueOf] method.
     *
     *
     * Examples:
     *
     *  * `BigRational.valueOf(0.5).toRationalString()` returns `"1/2"`
     *  * `BigRational.valueOf(2).toRationalString()` returns `"2"`
     *  * `BigRational.valueOf(4, 4).toRationalString()` returns `"4/4"` (not reduced)
     *
     *
     * @return the rational number string representation in the form "numerator/denominator", or "0" if the rational number is 0.
     * @see .valueOf
     * @see .valueOf
     */
    fun toRationalString(): String {
        if (isZero()) {
            return "0"
        }
        return if (isIntegerInternal()) {
            numerator.toString()
        } else {
            "$numerator/$denominator"
        }
    }

    /**
     * Returns the string representation of this rational number as integer and fraction parts in the form "integerPart fractionNominator/fractionDenominator".
     *
     *
     * The integer part is omitted if it is 0 (when this absolute rational number is smaller than 1).
     *
     * The fraction part is omitted it it is 0 (when this rational number is an integer).
     *
     * If this rational number is 0, then "0" is returned.
     *
     *
     * Example: `BigRational.valueOf(3.5).toIntegerRationalString()` returns `"3 1/2"`.
     *
     * @return the integer and fraction rational string representation
     * @see .valueOf
     */
    fun toIntegerRationalString(): String {
        val fractionNumerator: BigDecimal = numerator.remainder(denominator)
        val integerNumerator: BigDecimal = numerator.subtract(fractionNumerator)
        val integerPart = integerNumerator.divide(denominator)
        val result = StringBuilder()
        if (integerPart.signum() != 0) {
            result.append(integerPart)
        }
        if (fractionNumerator.signum() != 0) {
            if (result.isNotEmpty()) {
                result.append(' ')
            }
            result.append(fractionNumerator.abs())
            result.append('/')
            result.append(denominator)
        }
        if (result.isEmpty()) {
            result.append('0')
        }
        return result.toString()
    }

    /**
     * Returns the smallest of the specified rational numbers.
     *
     * @param values the rational numbers to compare
     * @return the smallest rational number, 0 if no numbers are specified
     */
    fun min(vararg values: BigRational): BigRational? {
        if (values.size == 0) {
            return BigRational.ZERO
        }
        var result = values[0]
        for (i in 1 until values.size) {
            result = result.min(values[i])
        }
        return result
    }

    /**
     * Returns the largest of the specified rational numbers.
     *
     * @param values the rational numbers to compare
     * @return the largest rational number, 0 if no numbers are specified
     * @see .max
     */
    fun max(vararg values: BigRational): BigRational? {
        if (values.size == 0) {
            return BigRational.ZERO
        }
        var result = values[0]
        for (i in 1 until values.size) {
            result = result.max(values[i])
        }
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BigRational) return false
        return numerator == other.numerator &&
                denominator == other.denominator
    }

    companion object {

        /**
         * The value 0 as [BigRational].
         */
        val ZERO: BigRational = BigRational(0)

        /**
         * The value 1 as [BigRational].
         */
        val ONE: BigRational = BigRational(1)

        /**
         * The value 2 as [BigRational].
         */
        val TWO: BigRational = BigRational(2)

        /**
         * The value 10 as [BigRational].
         */
        val TEN: BigRational = BigRational(10)

        private fun of(num: BigDecimal, dem: BigDecimal): BigRational {
            if (num.signum() == 0 && dem.signum() != 0) {
                return ZERO
            }
            return if (num.compareTo(BigDecimal.ONE) == 0 && dem.compareTo(BigDecimal.ONE) == 0) {
                ONE
            } else BigRational(num, dem)
        }

        private fun countDigits(number: BigInteger): Int {
            val factor = Math.log(2.0) / Math.log(10.0)
            val digitCount = (factor * number.bitLength() + 1).toInt()
            return if (BigInteger.TEN.pow(digitCount - 1) > number) {
                digitCount - 1
            } else {
                digitCount
            }
        }

        /**
         * Creates a rational number of the specified int value.
         *
         * @param value the int value
         * @return the rational number
         */
        fun valueOf(value: Int): BigRational {
            if (value == 0) {
                return ZERO
            }
            return if (value == 1) {
                ONE
            } else BigRational(value)
        }

        /**
         * Creates a rational number of the specified numerator/denominator int values.
         *
         * @param numerator the numerator int value
         * @param denominator the denominator int value (0 not allowed)
         * @return the rational number
         * @throws ArithmeticException if the denominator is 0 (division by zero)
         */
        fun valueOf(numerator: Int, denominator: Int): BigRational {
            return of(BigDecimal.valueOf(numerator.toLong()), BigDecimal.valueOf(denominator.toLong()))
        }

        /**
         * Creates a rational number of the specified integer and fraction parts.
         *
         *
         * Useful to create numbers like 3 1/2 (= three and a half = 3.5) by calling
         * `BigRational.valueOf(3, 1, 2)`.
         *
         * To create a negative rational only the integer part argument is allowed to be negative:
         * to create -3 1/2 (= minus three and a half = -3.5) call `BigRational.valueOf(-3, 1, 2)`.
         *
         * @param integer the integer part int value
         * @param fractionNumerator the fraction part numerator int value (negative not allowed)
         * @param fractionDenominator the fraction part denominator int value (0 or negative not allowed)
         * @return the rational number
         * @throws ArithmeticException if the fraction part denominator is 0 (division by zero),
         * or if the fraction part numerator or denominator is negative
         */
        fun valueOf(integer: Int, fractionNumerator: Int, fractionDenominator: Int): BigRational {
            if (fractionNumerator < 0 || fractionDenominator < 0) {
                throw ArithmeticException("Negative value")
            }
            val integerPart = valueOf(integer)
            val fractionPart = valueOf(fractionNumerator, fractionDenominator)
            return if (integerPart.isPositive()) integerPart + fractionPart else integerPart - fractionPart
        }

        /**
         * Creates a rational number of the specified numerator/denominator BigInteger values.
         *
         * @param numerator the numerator [BigInteger] value
         * @param denominator the denominator [BigInteger] value (0 not allowed)
         * @return the rational number
         * @throws ArithmeticException if the denominator is 0 (division by zero)
         */
        fun valueOf(numerator: BigInteger, denominator: BigInteger): BigRational {
            return of(BigDecimal(numerator), BigDecimal(denominator))
        }

        /**
         * Creates a rational number of the specified [BigInteger] value.
         *
         * @param value the [BigInteger] value
         * @return the rational number
         */
        fun valueOf(value: BigInteger): BigRational {
            if (value == BigInteger.ZERO) {
                return ZERO
            }
            return if (value == BigInteger.ONE) {
                ONE
            } else valueOf(value, BigInteger.ONE)
        }

        /**
         * Creates a rational number of the specified double value.
         *
         * @param value the double value
         * @return the rational number
         * @throws NumberFormatException if the double value is Infinite or NaN.
         */
        fun valueOf(value: Double): BigRational {
            if (value == 0.0) {
                return ZERO
            }
            if (value == 1.0) {
                return ONE
            }
            if (java.lang.Double.isInfinite(value)) {
                throw NumberFormatException("Infinite")
            }
            if (java.lang.Double.isNaN(value)) {
                throw NumberFormatException("NaN")
            }
            return valueOf(BigDecimal(value.toString()))
        }

        /**
         * Creates a rational number of the specified [BigDecimal] value.
         *
         * @param value the double value
         * @return the rational number
         */
        fun valueOf(value: BigDecimal): BigRational {
            if (value == BigDecimal.ZERO) {
                return ZERO
            }
            if (value == BigDecimal.ONE) {
                return ONE
            }
            val scale = value.scale()
            return when {
                scale == 0 -> {
                    BigRational(value, BigDecimal.ONE)
                }
                scale < 0 -> {
                    val n = BigDecimal(value.unscaledValue()) * BigDecimal.ONE.movePointLeft(value.scale())
                    BigRational(n, BigDecimal.ONE)
                }
                else -> {
                    val n = BigDecimal(value.unscaledValue())
                    val d = BigDecimal.ONE.movePointRight(value.scale())
                    BigRational(n, d)
                }
            }
        }

        /**
         * Creates a rational number of the specified string representation.
         *
         *
         * The accepted string representations are:
         *
         *  * Output of [BigRational.toString] : "integerPart.fractionPart"
         *  * Output of [BigRational.toRationalString] : "numerator/denominator"
         *  * Output of `toString()` of [BigDecimal], [BigInteger], [Integer], ...
         *  * Output of `toString()` of [Double], [Float] - except "Infinity", "-Infinity" and "NaN"
         *
         *
         * @param string the string representation to convert
         * @return the rational number
         * @throws ArithmeticException if the denominator is 0 (division by zero)
         */
        fun valueOf(string: String): BigRational {
            val strings = string.split("/".toRegex()).toTypedArray()
            var result = valueOfSimple(strings[0])
            for (i in 1 until strings.size) {
                result /= valueOfSimple(strings[i])
            }
            return result
        }

        private fun valueOfSimple(string: String): BigRational {
            return valueOf(BigDecimal(string))
        }
/*
        fun valueOf(
            positive: Boolean,
            integerPart: String?,
            fractionPart: String?,
            fractionRepeatPart: String?,
            exponentPart: String?
        ): BigRational {
            var result = ZERO
            if (fractionRepeatPart != null && fractionRepeatPart.length > 0) {
                val lotsOfNines = BigInteger.TEN.pow(fractionRepeatPart.length).subtract(BigInteger.ONE)
                result = valueOf(BigInteger(fractionRepeatPart), lotsOfNines)
            }
            if (fractionPart != null && fractionPart.length > 0) {
                result = result.add(valueOf(BigInteger(fractionPart)))
                result = result.divide(BigInteger.TEN.pow(fractionPart.length))
            }
            if (integerPart != null && integerPart.length > 0) {
                result = result.add(BigInteger(integerPart))
            }
            if (exponentPart != null && exponentPart.length > 0) {
                val exponent = exponentPart.toInt()
                val powerOfTen = BigInteger.TEN.pow(Math.abs(exponent))
                result = if (exponent >= 0) result.multiply(powerOfTen) else result.divide(powerOfTen)
            }
            if (!positive) {
                result = -result
            }
            return result
        }
*/
        /**
         * Creates a rational number of the specified numerator/denominator BigDecimal values.
         *
         * @param numerator the numerator [BigDecimal] value
         * @param denominator the denominator [BigDecimal] value (0 not allowed)
         * @return the rational number
         * @throws ArithmeticException if the denominator is 0 (division by zero)
         */
        fun valueOf(numerator: BigDecimal, denominator: BigDecimal): BigRational? {
            return valueOf(numerator) / valueOf(denominator)
        }

        private val bernoulliCache: MutableList<BigRational> = ArrayList()

        /**
         * Calculates the Bernoulli number for the specified index.
         *
         *
         * This function calculates the **first Bernoulli numbers** and therefore `bernoulli(1)` returns -0.5
         *
         * Note that `bernoulli(x)` for all odd x &gt; 1 returns 0
         *
         * See: [Wikipedia: Bernoulli number](https://en.wikipedia.org/wiki/Bernoulli_number)
         *
         * @param n the index of the Bernoulli number to be calculated (starting at 0)
         * @return the Bernoulli number for the specified index
         * @throws ArithmeticException if x is lesser than 0
         */
        fun bernoulli(n: Int): BigRational {

            if (n < 0) {
                throw ArithmeticException("Illegal bernoulli(n) for n < 0: n = $n")
            }

            if (n == 1) {
                return valueOf(-1, 2)
            } else if (n % 2 == 1) {
                return ZERO
            }

            synchronized(bernoulliCache) {
                val index = n / 2
                if (bernoulliCache.size <= index) {
                    for (i in bernoulliCache.size..index) {
                        bernoulliCache.add(calculateBernoulli(i * 2))
                    }
                }
                return bernoulliCache[index]
            }

        }

        private fun calculateBernoulli(n: Int): BigRational {
            var result = ZERO
            for (k in 0..n) {
                var jSum = ZERO
                var bin = ONE
                for (j in 0..k) {
                    val jPowN = valueOf(j).pow(n)
                    jSum = if (j % 2 == 0) {
                        jSum + (bin * jPowN)
                    } else {
                        jSum - (bin * jPowN)
                    }
                    bin = bin * valueOf(k - j) / valueOf(j + 1)
                }
                jSum /= valueOf(k + 1)
                result += jSum
            }
            return result
        }


    }
}