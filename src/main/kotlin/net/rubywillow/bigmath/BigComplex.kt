package net.rubywillow.bigmath

import net.rubywillow.bigmath.BigDecimalMath.factorial
import java.math.BigDecimal
import java.math.MathContext
import java.util.*


/**  Derived from ch.obermuhlner.math.big
 *
 * Represents a complex number consisting of a real and an imaginary {@link BigDecimal} part in the form {@code a + bi}.
 *
 * <p>It generally follows the design of {@link BigDecimal} with some convenience improvements like overloaded operator methods.</p>
 *
 * <p>The biggest difference to {@link BigDecimal} is that {@link BigComplex#equals(Object) BigComplex.equals(Object)} implements the <strong>mathematical</strong> equality
 * and <strong>not</strong> the strict technical equality.
 * This was a difficult decision because it means that {@code BigComplex} behaves slightly different than {@link BigDecimal}
 * but considering that the strange equality of {@link BigDecimal} is a major source of bugs we
 * decided it was worth the slight inconsistency.
 * If you need the strict equality use {@link BigComplex#strictEquals(Object)}`.</p>
 *
 * <p>This class is immutable and therefore inherently thread safe.</p>
 */

class BigComplex(val re: BigDecimal, val im: BigDecimal) {

    companion object {
        val ZERO = BigComplex(BigDecimal.ZERO, BigDecimal.ZERO)
        val ONE = BigComplex(BigDecimal.ONE, BigDecimal.ZERO)
        val I = BigComplex(BigDecimal.ZERO, BigDecimal.ONE)
        val TWO: BigDecimal = BigDecimal.valueOf(2)
    }

    /////
    operator fun plus(other: BigComplex): BigComplex {
        return valueOf(re + other.re, im + other.im)
    }

    operator fun plus(other: BigDecimal): BigComplex {
        return valueOf(re + other, im)
    }

    operator fun plus(other: Double): BigComplex {
        return plus(BigDecimal.valueOf(other))
    }

    fun add(other: BigComplex, mathContext: MathContext): BigComplex {
        return valueOf(re.add(other.re, mathContext), im.add(other.im, mathContext))
    }

    fun add(other: BigDecimal, mathContext: MathContext): BigComplex {
        return valueOf(re.add(other, mathContext), im)
    }

    /////
    operator fun minus(other: BigComplex): BigComplex {
        return valueOf(re - other.re, im - other.im)
    }

    operator fun minus(other: BigDecimal): BigComplex {
        return valueOf(re - other, im)
    }

    operator fun minus(other: Double): BigComplex {
        return minus(BigDecimal.valueOf(other))
    }

    fun subtract(other: BigComplex, mathContext: MathContext): BigComplex {
        return valueOf(re.subtract(other.re, mathContext), im.subtract(other.im, mathContext))
    }

    fun subtract(other: BigDecimal, mathContext: MathContext): BigComplex {
        return valueOf(re.subtract(other, mathContext), im)
    }

    /////
    operator fun times(other: BigComplex): BigComplex {
        return valueOf(
            re * other.re - im * other.im,
            re * other.im + im * other.re)
    }

    operator fun times(value: BigDecimal): BigComplex {
        return valueOf(
            re * value,
            im * value
        )
    }

    operator fun times(value: Double): BigComplex {
        return times(BigDecimal.valueOf(value))
    }

    fun multiply(value: BigComplex, mathContext: MathContext): BigComplex {
        return valueOf(
            re.multiply(value.re, mathContext).subtract(im.multiply(value.im, mathContext), mathContext),
            re.multiply(value.im, mathContext).add(im.multiply(value.re, mathContext), mathContext)
        )
    }

    fun multiply(value: BigDecimal, mathContext: MathContext): BigComplex {
        return valueOf(
            re.multiply(value, mathContext),
            im.multiply(value, mathContext)
        )
    }

    /////
    fun divide(value: BigComplex, mathContext: MathContext): BigComplex {
        return multiply(value.reciprocal(mathContext), mathContext)
    }

    fun divide(value: BigDecimal, mathContext: MathContext): BigComplex {
        return valueOf(
            re.divide(value, mathContext),
            im.divide(value, mathContext)
        )
    }

    fun divide(value: Double, mathContext: MathContext): BigComplex {
        return divide(BigDecimal.valueOf(value), mathContext)
    }

    /////
    fun reciprocal(mathContext: MathContext): BigComplex {
        val scale = absSquare(mathContext)
        return valueOf(
            re.divide(scale, mathContext),
            -im.divide(scale, mathContext)
        )
    }

    fun conjugate(): BigComplex {
        return valueOf(re, -im)
    }

    operator fun unaryMinus(): BigComplex {
        return valueOf(-re, -im)
    }

    /////

    fun abs(mathContext: MathContext): BigDecimal {
        return absSquare(mathContext).sqrt(mathContext)
    }

    fun absSquare(mathContext: MathContext): BigDecimal {
        return re.multiply(re, mathContext).add(im.multiply(im, mathContext), mathContext)
    }

    fun angle(mathContext: MathContext): BigDecimal {
        return im.atan2(re, mathContext)
    }

    fun isReal(): Boolean {
        return im.signum() == 0
    }

    fun re(): BigComplex {
        return valueOf(re, BigDecimal.ZERO)
    }

    fun im(): BigComplex {
        return valueOf(BigDecimal.ZERO, im)
    }

    fun round(mathContext: MathContext): BigComplex {
        return valueOf(re.round(mathContext), im.round(mathContext))
    }

    /////
    override fun hashCode(): Int {
        return Objects.hash(re, im)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BigComplex) return false
        return re == other.re && im == other.im
    }

    override fun toString(): String {
        return if (im.signum() >= 0) {
            "($re + $im i)"
        } else {
            "(" + re + " - " + im.negate() + " i)"
        }
    }

    /////
    fun valueOf(real: BigDecimal): BigComplex {
        return valueOf(real, BigDecimal.ZERO)
    }

    fun valueOf(real: Double): BigComplex {
        return valueOf(BigDecimal.valueOf(real), BigDecimal.ZERO)
    }

    fun valueOf(real: Double, imag: Double): BigComplex {
        return valueOf(BigDecimal.valueOf(real), BigDecimal.valueOf(imag))
    }

    fun valueOf(real: BigDecimal, imag: BigDecimal): BigComplex {
        if (real.signum() == 0) {
            if (imag.signum() == 0) {
                return ZERO
            }
            if (imag.compareTo(BigDecimal.ONE) == 0) {
                return I
            }
        }
        return if (imag.signum() == 0 && real.compareTo(BigDecimal.ONE) == 0) {
            ONE
        } else BigComplex(real, imag)
    }

    fun valueOfPolar(radius: BigDecimal, angle: BigDecimal, mathContext: MathContext): BigComplex {
        return if (radius.signum() == 0) {
            ZERO
        } else valueOf(
            radius.multiply(angle.cos(mathContext), mathContext),
            radius.multiply(angle.sin(mathContext), mathContext)
        )
    }

    fun valueOfPolar(radius: Double, angle: Double, mathContext: MathContext): BigComplex {
        return valueOfPolar(BigDecimal.valueOf(radius), BigDecimal.valueOf(angle), mathContext)
    }

    //////////////////////////////////


    /**
     * Calculates the factorial of the specified [BigComplex].
     *
     *
     * This implementation uses
     * [Spouge's approximation](https://en.wikipedia.org/wiki/Spouge%27s_approximation)
     * to calculate the factorial for non-integer values.
     *
     *
     * This involves calculating a series of constants that depend on the desired precision.
     * Since this constant calculation is quite expensive (especially for higher precisions),
     * the constants for a specific precision will be cached
     * and subsequent calls to this method with the same precision will be much faster.
     *
     *
     * It is therefore recommended to do one call to this method with the standard precision of your application during the startup phase
     * and to avoid calling it with many different precisions.
     *
     *
     * See: [Wikipedia: Factorial - Extension of factorial to non-integer values of argument](https://en.wikipedia.org/wiki/Factorial#Extension_of_factorial_to_non-integer_values_of_argument)
     *
     * @param x the [BigComplex]
     * @param mathContext the [MathContext] used for the result
     * @return the factorial [BigComplex]
     * @throws ArithmeticException if x is a negative integer value (-1, -2, -3, ...)
     * @see BigDecimalMath.factorial
     * @see .gamma
     */
    fun factorial(mathContext: MathContext): BigComplex {
        if (this.isReal() && this.re.isIntValue()) {
            return valueOf(factorial(this.re.intValueExact()).round(mathContext))
        }

        // https://en.wikipedia.org/wiki/Spouge%27s_approximation
        val mc = MathContext(mathContext.precision * 2, mathContext.roundingMode)
        val a = mathContext.precision * 13 / 10
        val constants = getSpougeFactorialConstants(a)
        val bigA = BigDecimal.valueOf(a.toLong())
        var negative = false
        var factor: BigComplex = valueOf(constants[0])
        for (k in 1 until a) {
            val bigK = BigDecimal.valueOf(k.toLong())
            factor = factor.add(valueOf(constants[k]).divide(this.plus(bigK), mc), mc)
            negative = !negative
        }
        val y = this.add(bigA, mc)
        val z = this.add(BigDecimal.valueOf(0.5), mc)
        var result = y.pow(z, mc)
        result = result.multiply(-this.subtract(bigA, mc).exp(mc), mc)
        result = result.multiply(factor, mc)
        return result.round(mathContext)
    }

    /**
     * Calculates the gamma function of the specified [BigComplex].
     *
     *
     * This implementation uses [.factorial] internally,
     * therefore the performance implications described there apply also for this method.
     *
     *
     * See: [Wikipedia: Gamma function](https://en.wikipedia.org/wiki/Gamma_function)
     *
     * @param x the [BigComplex]
     * @param mathContext the [MathContext] used for the result
     * @return the gamma [BigComplex]
     * @throws ArithmeticException if x-1 is a negative integer value (-1, -2, -3, ...)
     * @see BigDecimalMath.gamma
     * @see .factorial
     */
    fun gamma(mathContext: MathContext): BigComplex {
        return (this - 1.0).factorial(mathContext)
    }


    /**
     * Calculates the natural exponent of [BigComplex] x (e<sup>x</sup>) in the complex domain.
     *
     *
     * See: [Wikipedia: Exponent (Complex plane)](https://en.wikipedia.org/wiki/Exponential_function#Complex_plane)
     *
     * @param x the [BigComplex] to calculate the exponent for
     * @param mathContext the [MathContext] used for the result
     * @return the calculated exponent [BigComplex] with the precision specified in the `mathContext`
     */
    fun exp(mathContext: MathContext): BigComplex {
        val mc = MathContext(mathContext.precision + 4, mathContext.roundingMode)
        val expRe = this.re.exp(mc)
        return valueOf(
            expRe.multiply(this.im.cos(mc), mc).round(mathContext),
            expRe.multiply(this.im.sin(mc), mc)
        ).round(mathContext)
    }

    /**
     * Calculates the sine (sinus) of [BigComplex] x in the complex domain.
     *
     *
     * See: [Wikipedia: Sine (Sine with a complex argument)](https://en.wikipedia.org/wiki/Sine#Sine_with_a_complex_argument)
     *
     * @param x the [BigComplex] to calculate the sine for
     * @param mathContext the [MathContext] used for the result
     * @return the calculated sine [BigComplex] with the precision specified in the `mathContext`
     */
    fun sin(mathContext: MathContext): BigComplex {
        val mc = MathContext(mathContext.precision + 4, mathContext.roundingMode)
        return valueOf(
            this.re.sin(mc).multiply(this.im.cosh(mc), mc).round(mathContext),
            this.re.cos(mc).multiply(this.im.sinh(mc), mc).round(mathContext)
        )
    }

    /**
     * Calculates the cosine (cosinus) of [BigComplex] x in the complex domain.
     *
     * @param x the [BigComplex] to calculate the cosine for
     * @param mathContext the [MathContext] used for the result
     * @return the calculated cosine [BigComplex] with the precision specified in the `mathContext`
     */
    fun cos(mathContext: MathContext): BigComplex {
        val mc = MathContext(mathContext.precision + 4, mathContext.roundingMode)
        return valueOf(
            this.re.cos(mc).multiply(this.im.cosh(mc), mc).round(mathContext),
            this.re.sin(mc).multiply(this.im.sinh(mc), mc).negate().round(mathContext)
        )
    }

    /**
     * Calculates the tangens of [BigComplex] x in the complex domain.
     *
     * @param x the [BigComplex] to calculate the tangens for
     * @param mathContext the [MathContext] used for the result
     * @return the calculated tangens [BigComplex] with the precision specified in the `mathContext`
     */
    fun tan(mathContext: MathContext): BigComplex {
        val mc = MathContext(mathContext.precision + 4, mathContext.roundingMode)
        // return sin(x, mc).divide(cos(x, mc), mc).round(mathContext);
        return this.sin(mc).divide(cos(mc), mc).round(mathContext)
    }

    /**
     * Calculates the arc tangens (inverted tangens) of [BigComplex] x in the complex domain.
     *
     *
     * See: [Wikipedia: Inverse trigonometric functions (Extension to complex plane)](https://en.wikipedia.org/wiki/Inverse_trigonometric_functions#Extension_to_complex_plane)
     *
     * @param x the [BigComplex] to calculate the arc tangens for
     * @param mathContext the [MathContext] used for the result
     * @return the calculated arc tangens [BigComplex] with the precision specified in the `mathContext`
     */
    fun atan(mathContext: MathContext): BigComplex {
        val mc = MathContext(mathContext.precision + 4, mathContext.roundingMode)
        // return log(I.subtract(x, mc).divide(I.add(x, mc), mc), mc).divide(I, mc).divide(TWO, mc).round(mathContext);
        return I.subtract(this, mc).divide(I.add(this, mc), mc).log(mc).divide(I, mc).divide(TWO, mc).round(mathContext)
    }

    /**
     * Calculates the arc cotangens (inverted cotangens) of [BigComplex] x in the complex domain.
     *
     *
     * See: [Wikipedia: Inverse trigonometric functions (Extension to complex plane)](https://en.wikipedia.org/wiki/Inverse_trigonometric_functions#Extension_to_complex_plane)
     *
     * @param x the [BigComplex] to calculate the arc cotangens for
     * @param mathContext the [MathContext] used for the result
     * @return the calculated arc cotangens [BigComplex] with the precision specified in the `mathContext`
     */
    fun acot(mathContext: MathContext): BigComplex {
        val mc = MathContext(mathContext.precision + 4, mathContext.roundingMode)
        // return log(x.add(I, mc).divide(x.subtract(I, mc), mc), mc).divide(I, mc).divide(TWO, mc).round(mathContext);
        return this.add(I, mc).divide(this.subtract(I, mc), mc).log(mc).divide(I, mc).divide(TWO, mc).round(mathContext)
    }

    /**
     * Calculates the arc sine (inverted sine) of [BigComplex] x in the complex domain.
     *
     *
     * See: [Wikipedia: Inverse trigonometric functions (Extension to complex plane)](https://en.wikipedia.org/wiki/Inverse_trigonometric_functions#Extension_to_complex_plane)
     *
     * @param x the [BigComplex] to calculate the arc sine for
     * @param mathContext the [MathContext] used for the result
     * @return the calculated arc sine [BigComplex] with the precision specified in the `mathContext`
     */
    fun asin(mathContext: MathContext): BigComplex {
        val mc = MathContext(mathContext.precision + 4, mathContext.roundingMode)
        // return I.negate().multiply(log(I.multiply(x, mc).add(sqrt(BigComplex.ONE.subtract(x.multiply(x, mc), mc), mc), mc), mc), mc).round(mathContext);
        return -I.multiply(I.multiply(this, mc).add(ONE.subtract(this.multiply(this, mc), mc).sqrt(mc), mc).log(mc), mc).round(mathContext)
    }

    /**
     * Calculates the arc cosine (inverted cosine) of [BigComplex] x in the complex domain.
     *
     *
     * See: [Wikipedia: Inverse trigonometric functions (Extension to complex plane)](https://en.wikipedia.org/wiki/Inverse_trigonometric_functions#Extension_to_complex_plane)
     *
     * @param x the [BigComplex] to calculate the arc cosine for
     * @param mathContext the [MathContext] used for the result
     * @return the calculated arc cosine [BigComplex] with the precision specified in the `mathContext`
     */
    fun acos(mathContext: MathContext): BigComplex {
        val mc = MathContext(mathContext.precision + 4, mathContext.roundingMode)
        // return I.negate().multiply(log(x.add(sqrt(x.multiply(x, mc).subtract(BigComplex.ONE, mc), mc), mc), mc), mc).round(mathContext);
        return -I.multiply(this.add(this.multiply(this, mc).subtract(ONE, mc).sqrt(mc), mc).log(mc), mc).round(mathContext)
    }

    /**
     * Calculates the square root of [BigComplex] x in the complex domain (√x).
     *
     *
     * See [Wikipedia: Square root (Square root of an imaginary number)](https://en.wikipedia.org/wiki/Square_root#Square_root_of_an_imaginary_number)
     *
     * @param x the [BigComplex] to calculate the square root for
     * @param mathContext the [MathContext] used for the result
     * @return the calculated square root [BigComplex] with the precision specified in the `mathContext`
     */
    fun sqrt(mathContext: MathContext): BigComplex {
        // https://math.stackexchange.com/questions/44406/how-do-i-get-the-square-root-of-a-complex-number
        val mc = MathContext(mathContext.precision + 4, mathContext.roundingMode)
        val magnitude = this.abs(mc)
        val a = this.add(magnitude, mc)
        return a.divide(a.abs(mc), mc).multiply(magnitude.sqrt(mc), mc).round(mathContext)
    }

    /**
     * Calculates the natural logarithm of [BigComplex] x in the complex domain.
     *
     *
     * See: [Wikipedia: Complex logarithm](https://en.wikipedia.org/wiki/Complex_logarithm)
     *
     * @param x the [BigComplex] to calculate the natural logarithm for
     * @param mathContext the [MathContext] used for the result
     * @return the calculated natural logarithm [BigComplex] with the precision specified in the `mathContext`
     */
    fun log(mathContext: MathContext): BigComplex {
        // https://en.wikipedia.org/wiki/Complex_logarithm
        val mc1 = MathContext(mathContext.precision + 20, mathContext.roundingMode)
        val mc2 = MathContext(mathContext.precision + 5, mathContext.roundingMode)
        return valueOf(
            this.abs(mc1).log(mc1).round(mathContext),
            this.angle(mc2)
        ).round(mathContext)
    }

    /**
     * Calculates [BigComplex] x to the power of `long` y (x<sup>y</sup>).
     *
     *
     * The implementation tries to minimize the number of multiplications of [x][BigComplex] (using squares whenever possible).
     *
     *
     * See: [Wikipedia: Exponentiation - efficient computation](https://en.wikipedia.org/wiki/Exponentiation#Efficient_computation_with_integer_exponents)
     *
     * @param x the [BigComplex] value to take to the power
     * @param y the `long` value to serve as exponent
     * @param mathContext the [MathContext] used for the result
     * @return the calculated x to the power of y with the precision specified in the `mathContext`
     */
    fun pow(y: Long, mathContext: MathContext): BigComplex {
        var x = this
        var y = y
        val mc = MathContext(mathContext.precision + 10, mathContext.roundingMode)
        if (y < 0) {
            return ONE.divide(x.pow(-y, mc), mc).round(mathContext)
        }
        var result = ONE
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

    /**
     * Calculates [BigComplex] x to the power of [BigDecimal] y (x<sup>y</sup>).
     *
     * @param x the [BigComplex] value to take to the power
     * @param y the [BigDecimal] value to serve as exponent
     * @param mathContext the [MathContext] used for the result
     * @return the calculated x to the power of y with the precision specified in the `mathContext`
     */
    fun pow(y: BigDecimal, mathContext: MathContext): BigComplex {
        val mc = MathContext(mathContext.precision + 4, mathContext.roundingMode)
        val angleTimesN = this.angle(mc).multiply(y, mc)
        return valueOf(
            angleTimesN.cos(mc),
            angleTimesN.sin(mc)
        ).multiply(this.abs(mc).pow(y, mc), mc).round(mathContext)
    }

    /**
     * Calculates [BigComplex] x to the power of [BigComplex] y (x<sup>y</sup>).
     *
     * @param x the [BigComplex] value to take to the power
     * @param y the [BigComplex] value to serve as exponent
     * @param mathContext the [MathContext] used for the result
     * @return the calculated x to the power of y with the precision specified in the `mathContext`
     */
    fun pow(y: BigComplex, mathContext: MathContext): BigComplex {
        val mc = MathContext(mathContext.precision + 4, mathContext.roundingMode)
        return y.multiply(this.log(mc), mc).exp(mc).round(mathContext)
    }

    /**
     * Calculates the [BigDecimal] n'th root of [BigComplex] x (<sup>n</sup>√x).
     *
     *
     * See [Wikipedia: Square root](http://en.wikipedia.org/wiki/Square_root)
     * @param x the [BigComplex] value to calculate the n'th root
     * @param n the [BigDecimal] defining the root
     * @param mathContext the [MathContext] used for the result
     *
     * @return the calculated n'th root of x with the precision specified in the `mathContext`
     */
    fun root(n: BigDecimal, mathContext: MathContext): BigComplex {
        val mc = MathContext(mathContext.precision + 4, mathContext.roundingMode)
        return this.pow(BigDecimal.ONE.divide(n, mc), mc).round(mathContext)
    }

    /**
     * Calculates the [BigComplex] n'th root of [BigComplex] x (<sup>n</sup>√x).
     *
     *
     * See [Wikipedia: Square root](http://en.wikipedia.org/wiki/Square_root)
     * @param x the [BigComplex] value to calculate the n'th root
     * @param n the [BigComplex] defining the root
     * @param mathContext the [MathContext] used for the result
     *
     * @return the calculated n'th root of x with the precision specified in the `mathContext`
     */
    fun root(n: BigComplex, mathContext: MathContext): BigComplex {
        val mc = MathContext(mathContext.precision + 4, mathContext.roundingMode)
        return this.pow(ONE.divide(n, mc), mc).round(mathContext)
    }


}