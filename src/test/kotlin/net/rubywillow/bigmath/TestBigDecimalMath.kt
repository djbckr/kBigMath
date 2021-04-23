package net.rubywillow.bigmath

import net.rubywillow.bigmath.BigDecimalMath.bernoulli
import net.rubywillow.bigmath.BigDecimalMath.toBigDecimal
import java.math.BigDecimal
import java.math.MathContext
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

enum class TestLevel {
    Fast, Medium, Slow
}

class TestBigDecimalMath {

    private val MC_CHECK_DOUBLE = MathContext(10)
    private val TEST_LEVEL: TestLevel = getTestLevel()
    private val MC = MathContext.DECIMAL128

    @Test
    fun testInternals() {
        assertEquals(null, toCheck(Double.NaN))
        assertEquals(null, toCheck(Double.NEGATIVE_INFINITY))
        assertEquals(null, toCheck(Double.POSITIVE_INFINITY))
        assertBigDecimal(BigDecimal("1.23"), BigDecimal("1.23"), MathContext(3))
        assertBigDecimal(BigDecimal("1.23"), BigDecimal("1.23"), MathContext(2))
        assertBigDecimal(BigDecimal("1.23"), BigDecimal("1.23"), MathContext(1))
        assertBigDecimal(BigDecimal("1.24"), BigDecimal("1.23"), MathContext(3))
        assertBigDecimal(BigDecimal("1.23"), BigDecimal("1.24"), MathContext(3))
        assertThrows(IllegalArgumentException::class.java) { throw IllegalArgumentException() }
        assertThrows(IllegalArgumentException::class.java, "blabla") { throw IllegalArgumentException("blabla") }
    }

    @Test(expected = AssertionError::class)
    fun testInternalsAssertBigDecimalFail1() {
        assertBigDecimal(BigDecimal("1.25"), BigDecimal("1.23"), MathContext(3))
    }

    @Test(expected = AssertionError::class)
    fun testInternalsBigDecimalFail2() {
        assertBigDecimal(BigDecimal("1.23"), BigDecimal("1.25"), MathContext(3))
    }

    @Test(expected = AssertionError::class)
    fun testInternalsAssertThrowsFail1() {
        assertThrows(IllegalArgumentException::class.java) {}
    }

    @Test(expected = AssertionError::class)
    fun testInternalsAssertThrowsFail2() {
        assertThrows(IllegalArgumentException::class.java) { throw RuntimeException() }
    }

    @Test(expected = AssertionError::class)
    fun testInternalsAssertThrowsFail3() {
        assertThrows(
            IllegalArgumentException::class.java,
            "blabla"
        ) { throw IllegalArgumentException("another message") }
    }

    @Test
    fun testToBigDecimalFails() {
        assertToBigDecimalThrows("")
        assertToBigDecimalThrows("x")
        assertToBigDecimalThrows(" ")
        assertToBigDecimalThrows("1x")
        assertToBigDecimalThrows("1.2x")
        assertToBigDecimalThrows("1.2E3x")
        assertToBigDecimalThrows("1..2")
        assertToBigDecimalThrows("1.2.3")
        assertToBigDecimalThrows("++1")
        assertToBigDecimalThrows("+1+2")
        assertToBigDecimalThrows("--1")
        assertToBigDecimalThrows("-1-2")
        assertToBigDecimalThrows("+-1")
        assertToBigDecimalThrows("+1-2")
        assertToBigDecimalThrows("-+1")
        assertToBigDecimalThrows("-1+2")
        assertToBigDecimalThrows("1EE2")
        assertToBigDecimalThrows("1E2.3")
        assertToBigDecimalThrows("1E++2")
        assertToBigDecimalThrows("1E+2+3")
        assertToBigDecimalThrows("1E--2")
        assertToBigDecimalThrows("1E-2-3")
        assertToBigDecimalThrows("1E+-2")
        assertToBigDecimalThrows("1E+2-3")
        assertToBigDecimalThrows("1E-+2")
        assertToBigDecimalThrows("1E-2+3")
        assertToBigDecimalThrows("1E9999999999")
        assertToBigDecimalThrows("1E999999999999999")
    }

    @Test
    fun testToBigDecimal() {
        assertToBigDecimal("0")
        assertToBigDecimal("00")
        assertToBigDecimal("0.0")
        assertToBigDecimal("0.00")
        assertToBigDecimal("00.00")
        assertToBigDecimal("+0")
        assertToBigDecimal("-0")
        assertToBigDecimal("+0E123")
        assertToBigDecimal("-0E-123")
        assertToBigDecimal(".123")
        assertToBigDecimal("123.")
        assertToBigDecimal("1")
        assertToBigDecimal("1.0")
        assertToBigDecimal("1.23")
        assertToBigDecimal("1.2300")
        assertToBigDecimal("1234567890")
        assertToBigDecimal("1234567890.1234567890123456789")
        assertToBigDecimal("001")
        assertToBigDecimal("001.23")
        assertToBigDecimal("001.2300")
        assertToBigDecimal("001234567890")
        assertToBigDecimal("001234567890.1234567890123456789")
        assertToBigDecimal("+1")
        assertToBigDecimal("+1.23")
        assertToBigDecimal("+1.2300")
        assertToBigDecimal("+1234567890")
        assertToBigDecimal("+1234567890.1234567890123456789")
        assertToBigDecimal("-1")
        assertToBigDecimal("-1.23")
        assertToBigDecimal("-1.2300")
        assertToBigDecimal("-1234567890")
        assertToBigDecimal("-1234567890.1234567890123456789")
        assertToBigDecimal("1.23E123")
        assertToBigDecimal("1.2300E123")
        assertToBigDecimal("1.23E+123")
        assertToBigDecimal("1.23E-123")
    }

    @Test
    fun testIsIntValue() {
        assertEquals(true, BigDecimal.valueOf(Int.MIN_VALUE.toLong()).isIntValue())
        assertEquals(true, BigDecimal.valueOf(Int.MAX_VALUE.toLong()).isIntValue())
        assertEquals(true, BigDecimal.valueOf(0).isIntValue())
        assertEquals(true, BigDecimal.valueOf(-55).isIntValue())
        assertEquals(true, BigDecimal.valueOf(33).isIntValue())
        assertEquals(true, BigDecimal.valueOf(-55.0).isIntValue())
        assertEquals(true, BigDecimal.valueOf(33.0).isIntValue())
        assertEquals(
            false,
            BigDecimal.valueOf(Int.MIN_VALUE.toLong()).subtract(BigDecimal.ONE).isIntValue()
        )
        assertEquals(false, BigDecimal.valueOf(Int.MAX_VALUE.toLong()).add(BigDecimal.ONE).isIntValue())
        assertEquals(false, BigDecimal.valueOf(3.333).isIntValue())
        assertEquals(false, BigDecimal.valueOf(-5.555).isIntValue())
    }

    @Test
    fun testIsLongValue() {
        assertEquals(true, BigDecimal.valueOf(Long.MIN_VALUE).isLongValue())
        assertEquals(true, BigDecimal.valueOf(Long.MAX_VALUE).isLongValue())
        assertEquals(true, BigDecimal.valueOf(0).isLongValue())
        assertEquals(true, BigDecimal.valueOf(-55).isLongValue())
        assertEquals(true, BigDecimal.valueOf(33).isLongValue())
        assertEquals(true, BigDecimal.valueOf(-55.0).isLongValue())
        assertEquals(true, BigDecimal.valueOf(33.0).isLongValue())
        assertEquals(false, BigDecimal.valueOf(Long.MIN_VALUE).subtract(BigDecimal.ONE).isLongValue())
        assertEquals(false, BigDecimal.valueOf(Long.MAX_VALUE).add(BigDecimal.ONE).isLongValue())
        assertEquals(false, BigDecimal.valueOf(3.333).isLongValue())
        assertEquals(false, BigDecimal.valueOf(-5.555).isLongValue())
    }

    @Test
    fun testIsDoubleValue() {
        assertEquals(true, BigDecimal.valueOf(Double.MIN_VALUE).isDoubleValue())
        assertEquals(true, BigDecimal.valueOf(Double.MAX_VALUE).isDoubleValue())
        assertEquals(true, BigDecimal.valueOf(-Double.MAX_VALUE).isDoubleValue())
        assertEquals(true, BigDecimal.valueOf(-Double.MIN_VALUE).isDoubleValue())
        assertEquals(true, BigDecimal.valueOf(0).isDoubleValue())
        assertEquals(true, BigDecimal.valueOf(-55).isDoubleValue())
        assertEquals(true, BigDecimal.valueOf(33).isDoubleValue())
        assertEquals(true, BigDecimal("1E-325").isDoubleValue())
        assertEquals(true, BigDecimal("-1E-325").isDoubleValue())
        assertEquals(true, BigDecimal("1E-325").isDoubleValue())
        assertEquals(true, BigDecimal("-1E-325").isDoubleValue())
        assertEquals(false, BigDecimal.valueOf(Double.MAX_VALUE).add(BigDecimal.valueOf(1)).isDoubleValue())
        assertEquals(false, BigDecimal.valueOf(-Double.MAX_VALUE).subtract(BigDecimal.valueOf(1)).isDoubleValue())
        assertEquals(false, BigDecimal("1E309").isDoubleValue())
        assertEquals(false, BigDecimal("-1E309").isDoubleValue())
    }

    @Test
    fun testMantissa() {
        assertEquals(0,  BigDecimal.ZERO.compareTo(BigDecimal.ZERO.mantissa()))
        assertEquals(0,  BigDecimal("1.2345").compareTo(BigDecimal("123.45").mantissa()))
        assertEquals(0,  BigDecimal("1.2345").compareTo(BigDecimal("0.00012345").mantissa()))
        assertEquals(0,  BigDecimal("1.2345").compareTo(BigDecimal("123.45").mantissa()))
        assertEquals(0,  BigDecimal("1.2345").compareTo(BigDecimal("0.00012345").mantissa()))
        assertEquals(0,  BigDecimal("1.2345").compareTo(BigDecimal("123.45000").mantissa()))
        assertEquals(0,  BigDecimal("1.2345").compareTo(BigDecimal("0.00012345000").mantissa()))
        assertEquals(0,  BigDecimal("1.2345").compareTo(BigDecimal("12.345E1").mantissa()))
        assertEquals(0,  BigDecimal("1.2345").compareTo(BigDecimal("0.0012345E-1").mantissa()))
        assertEquals(-1, BigDecimal("1.2345").compareTo(BigDecimal("123.459999").mantissa()))
        assertEquals(1,  BigDecimal("1.2345").compareTo(BigDecimal("0.000123").mantissa()))
    }

    @Test
    fun testExponent() {
        assertEquals(0,  BigDecimal.ZERO.exponent())
        assertEquals(0,  BigDecimal("1.2345").exponent())
        assertEquals(2,  BigDecimal("123.45").exponent())
        assertEquals(-2, BigDecimal("0.012345").exponent())
        assertEquals(0,  BigDecimal("123.45E-2").exponent())
        assertEquals(0,  BigDecimal("0.012345E2").exponent())
        assertEquals(3,  BigDecimal("1.2345E3").exponent())
        assertEquals(5,  BigDecimal("123.45E3").exponent())
        assertEquals(1,  BigDecimal("0.012345E3").exponent())
    }

    @Test
    fun testSignificantDigits() {
        assertEquals(1, BigDecimal.ZERO.significantDigits())
        assertEquals(3, BigDecimal("123").significantDigits())
        assertEquals(3, BigDecimal("12.3").significantDigits())
        assertEquals(3, BigDecimal("1.23").significantDigits())
        assertEquals(3, BigDecimal("0.123").significantDigits())
        assertEquals(3, BigDecimal("0.0123").significantDigits())
        assertEquals(3, BigDecimal("0.00123").significantDigits())
        assertEquals(3, BigDecimal("12.300").significantDigits())
        assertEquals(3, BigDecimal("1.2300").significantDigits())
        assertEquals(3, BigDecimal("0.12300").significantDigits())
        assertEquals(6, BigDecimal("123000").significantDigits())
        assertEquals(6, BigDecimal("123000.00").significantDigits())
        assertEquals(3, BigDecimal("-123").significantDigits())
        assertEquals(3, BigDecimal("-12.3").significantDigits())
        assertEquals(3, BigDecimal("-1.23").significantDigits())
        assertEquals(3, BigDecimal("-0.123").significantDigits())
        assertEquals(3, BigDecimal("-0.0123").significantDigits())
        assertEquals(3, BigDecimal("-0.00123").significantDigits())
        assertEquals(3, BigDecimal("-12.300").significantDigits())
        assertEquals(3, BigDecimal("-1.2300").significantDigits())
        assertEquals(3, BigDecimal("-0.12300").significantDigits())
        assertEquals(6, BigDecimal("-123000").significantDigits())
        assertEquals(6, BigDecimal("-123000.00").significantDigits())
    }

    @Test
    fun testIntegralPart() {
        assertEquals(0, BigDecimal.ZERO.compareTo(BigDecimal.ZERO.integralPart()))
        assertEquals(0, BigDecimal("1")      .compareTo(BigDecimal("1.2345").integralPart()))
        assertEquals(0, BigDecimal("123")    .compareTo(BigDecimal("123.45").integralPart()))
        assertEquals(0, BigDecimal("0")      .compareTo(BigDecimal("0.12345").integralPart()))
        assertEquals(0, BigDecimal("-1")     .compareTo(BigDecimal("-1.2345").integralPart()))
        assertEquals(0, BigDecimal("-123")   .compareTo(BigDecimal("-123.45").integralPart()))
        assertEquals(0, BigDecimal("-0")     .compareTo(BigDecimal("-0.12345").integralPart()))
        assertEquals(0, BigDecimal("123E987").compareTo(BigDecimal("123E987").integralPart()))
        assertEquals(0, BigDecimal("0")      .compareTo(BigDecimal("123E-987").integralPart()))
    }

    @Test
    fun testFractionalPart() {
        assertEquals(0, BigDecimal.ZERO.compareTo(BigDecimal.ZERO.fractionalPart()))
        assertEquals(0, BigDecimal("0.2345")  .compareTo(BigDecimal("1.2345").fractionalPart()))
        assertEquals(0, BigDecimal("0.45")    .compareTo(BigDecimal("123.45").fractionalPart()))
        assertEquals(0, BigDecimal("0.12345") .compareTo(BigDecimal("0.12345").fractionalPart()))
        assertEquals(0, BigDecimal("-0.2345") .compareTo(BigDecimal("-1.2345").fractionalPart()))
        assertEquals(0, BigDecimal("-0.45")   .compareTo(BigDecimal("-123.45").fractionalPart()))
        assertEquals(0, BigDecimal("-0.12345").compareTo(BigDecimal("-0.12345").fractionalPart()))
        assertEquals(0, BigDecimal("0")       .compareTo(BigDecimal("123E987").fractionalPart()))
        assertEquals(0, BigDecimal("123E-987").compareTo(BigDecimal("123E-987").fractionalPart()))
    }

    @Test
    fun testRoundWithTrailingZeroes() {
        val mc = MathContext(5)
        assertEquals("0.0000",            BigDecimal("0").roundWithTrailingZeroes(mc).toString())
        assertEquals("0.0000",            BigDecimal("0.00000000000").roundWithTrailingZeroes(mc).toString())
        assertEquals("1.2345",            BigDecimal("1.2345").roundWithTrailingZeroes(mc).toString())
        assertEquals("123.45",            BigDecimal("123.45").roundWithTrailingZeroes(mc).toString())
        assertEquals("0.0012345",         BigDecimal("0.0012345").roundWithTrailingZeroes(mc).toString())
        assertEquals("1.2346",            BigDecimal("1.234567").roundWithTrailingZeroes(mc).toString())
        assertEquals("1.2300",            BigDecimal("1.23").roundWithTrailingZeroes(mc).toString())
        assertEquals("1.2300",            BigDecimal("1.230000").roundWithTrailingZeroes(mc).toString())
        assertEquals("123.46",            BigDecimal("123.4567").roundWithTrailingZeroes(mc).toString())
        assertEquals("0.0012346",         BigDecimal("0.001234567").roundWithTrailingZeroes(mc).toString())
        assertEquals("0.0012300",         BigDecimal("0.00123").roundWithTrailingZeroes(mc).toString())
        assertEquals("1.2346E+100",       BigDecimal("1.234567E+100").roundWithTrailingZeroes(mc).toString())
        assertEquals("1.2346E-100",       BigDecimal("1.234567E-100").roundWithTrailingZeroes(mc).toString())
        assertEquals("1.2300E+100",       BigDecimal("1.23E+100").roundWithTrailingZeroes(mc).toString())
        assertEquals("1.2300E-100",       BigDecimal("1.23E-100").roundWithTrailingZeroes(mc).toString())
        assertEquals("1.2346E+999999999", BigDecimal("1.234567E+999999999").roundWithTrailingZeroes(mc).toString())
        assertEquals("1.2346E-999999999", BigDecimal("1.234567E-999999999").roundWithTrailingZeroes(mc).toString())
        assertEquals("1.2300E+999999999", BigDecimal("1.23E+999999999").roundWithTrailingZeroes(mc).toString())
        assertEquals("1.2300E-999999999", BigDecimal("1.23E-999999999").roundWithTrailingZeroes(mc).toString())
    }

    @Test
    fun testE() {
        // Result from wolframalpha.com: exp(1)
        val expected = BigDecimal(
            "2.7182818284590452353602874713526624977572470936999595749669676277240766303535475945713821785251664274274663919320030599218174135966290435729003342952605956307381323286279434907632338298807531952510190115738341879307021540891499348841675092447614606680822648001684774118537423454424371075390777449920695517027618386062613313845830007520449338265602976067371132007093287091274437470472306969772093101416928368190255151086574637721112523897844250569536967707854499699679468644549059879316368892300987931277361782154249992295763514822082698951936680331825288693984964651058209392398294887933203625094431173012381970684161403970198376793206832823764648042953118023287825098194558153017567173613320698112509961818815930416903515988885193458072738667385894228792284998920868058257492796104841984443634632449684875602336248270419786232090021609902353043699418491463140934317381436405462531520961836908887070167683964243781405927145635490613031072085103837505101157477041718986106873969655212671546889570350354021234078498193343210681701210056278802351930332247450158539047304199577770935036604169973297250886876966403555707162268447162560798826517871341951246652010305921236677194325278675398558944896970964097545918569563802363701621120477427228364896134225164450781824423529486363721417402388934412479635743702637552944483379980161254922785092577825620926226483262779333865664816277251640191059004916449982893150566047258027786318641551956532442586982946959308019152987211725563475463964479101459040905862984967912874068705048958586717479854667757573205681288459205413340539220001137863009455606881667400169842055804033637953764520304024322566135278369511778838638744396625322498506549958862342818997077332761717839280349465014345588970719425863987727547109629537415211151368350627526023264847287039207643100595841166120545297030"
        )
        assertPrecisionCalculation(
            expected,
            { mathContext -> e(mathContext) },
            10
        )
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testEUnlimitedFail() {
        e(MathContext.UNLIMITED)
    }

    @Test
    fun testPi() {
        // Result from wolframalpha.com: pi
        val expected = BigDecimal(
            "3.1415926535897932384626433832795028841971693993751058209749445923078164062862089986280348253421170679821480865132823066470938446095505822317253594081284811174502841027019385211055596446229489549303819644288109756659334461284756482337867831652712019091456485669234603486104543266482133936072602491412737245870066063155881748815209209628292540917153643678925903600113305305488204665213841469519415116094330572703657595919530921861173819326117931051185480744623799627495673518857527248912279381830119491298336733624406566430860213949463952247371907021798609437027705392171762931767523846748184676694051320005681271452635608277857713427577896091736371787214684409012249534301465495853710507922796892589235420199561121290219608640344181598136297747713099605187072113499999983729780499510597317328160963185950244594553469083026425223082533446850352619311881710100031378387528865875332083814206171776691473035982534904287554687311595628638823537875937519577818577805321712268066130019278766111959092164201989380952572010654858632788659361533818279682303019520353018529689957736225994138912497217752834791315155748572424541506959508295331168617278558890750983817546374649393192550604009277016711390098488240128583616035637076601047101819429555961989467678374494482553797747268471040475346462080466842590694912933136770289891521047521620569660240580381501935112533824300355876402474964732639141992726042699227967823547816360093417216412199245863150302861829745557067498385054945885869269956909272107975093029553211653449872027559602364806654991198818347977535663698074265425278625518184175746728909777727938000816470600161452491921732172147723501414419735685481613611573525521334757418494684385233239073941433345477624168625189835694855620992192221842725502542568876717904946016534668049886272327917860857843838279679766814541009538"
        )
        assertPrecisionCalculation(
            expected,
            { mathContext -> pi(mathContext) },
            10
        )
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testPiUnlimitedFail() {
        pi(MathContext.UNLIMITED)
    }

    @Test
    fun testBernoulli() {
        assertBigDecimal(toCheck(1.0)!!,           bernoulli(0, MC), MC_CHECK_DOUBLE)
        assertBigDecimal(toCheck(-1.0 / 2)!!,      bernoulli(1, MC), MC_CHECK_DOUBLE)
        assertBigDecimal(toCheck(1.0 / 6)!!,       bernoulli(2, MC), MC_CHECK_DOUBLE)
        assertBigDecimal(toCheck(0.0)!!,           bernoulli(3, MC), MC_CHECK_DOUBLE)
        assertBigDecimal(toCheck(-1.0 / 30)!!,     bernoulli(4, MC), MC_CHECK_DOUBLE)
        assertBigDecimal(toCheck(0.0)!!,           bernoulli(5, MC), MC_CHECK_DOUBLE)
        assertBigDecimal(toCheck(1.0 / 42)!!,      bernoulli(6, MC), MC_CHECK_DOUBLE)
        assertBigDecimal(toCheck(0.0)!!,           bernoulli(7, MC), MC_CHECK_DOUBLE)
        assertBigDecimal(toCheck(-1.0 / 30)!!,     bernoulli(8, MC), MC_CHECK_DOUBLE)
        assertBigDecimal(toCheck(0.0)!!,           bernoulli(9, MC), MC_CHECK_DOUBLE)
        assertBigDecimal(toCheck(5.0 / 66)!!,      bernoulli(10, MC), MC_CHECK_DOUBLE)
        assertBigDecimal(toCheck(0.0)!!,           bernoulli(11, MC), MC_CHECK_DOUBLE)
        assertBigDecimal(toCheck(-691.0 / 2730)!!, bernoulli(12, MC), MC_CHECK_DOUBLE)
        assertBigDecimal(toCheck(0.0)!!,           bernoulli(13, MC), MC_CHECK_DOUBLE)
        assertBigDecimal(toCheck(7.0 / 6)!!,       bernoulli(14, MC), MC_CHECK_DOUBLE)
        assertBigDecimal(toCheck(0.0)!!,           bernoulli(15, MC), MC_CHECK_DOUBLE)
        assertBigDecimal(toCheck(-3617.0 / 510)!!, bernoulli(16, MC), MC_CHECK_DOUBLE)
        assertBigDecimal(toCheck(0.0)!!,           bernoulli(17, MC), MC_CHECK_DOUBLE)
        assertBigDecimal(toCheck(43867.0 / 798)!!, bernoulli(18, MC), MC_CHECK_DOUBLE)
    }

    /*************************************************************************************/
    private fun assertToBigDecimal(string: String) {
        val expected = BigDecimal(string)
        for (i in 2..9) {
            val actual = toBigDecimal(string, MathContext.UNLIMITED, i)
            assertTrue(expected.compareTo(actual) == 0, "toBigDecimal(_,_,$i) $expected compareTo $actual")
            assertEquals(expected, actual)
        }
    }

    private fun assertToBigDecimalThrows(string: String) {
        assertThrows(NumberFormatException::class.java) { BigDecimal(string) }
        assertThrows(NumberFormatException::class.java) {
            toBigDecimal(
                string,
                MathContext.UNLIMITED,
                1
            )
        }
    }

    private fun assertPrecisionCalculation(
        precisionCalculation: (MathContext) -> BigDecimal,
        startPrecision: Int,
        endPrecision: Int
    ) {
        val expected: BigDecimal = precisionCalculation(MathContext(endPrecision * 2))
        assertPrecisionCalculation(expected, precisionCalculation, startPrecision, endPrecision)
    }

    private fun assertPrecisionCalculation(
        expected: BigDecimal,
        precisionCalculation: (MathContext) -> BigDecimal,
        startPrecision: Int
    ) {
        assertPrecisionCalculation(expected, precisionCalculation, startPrecision, expected.precision() - 20)
    }

    private fun assertPrecisionCalculation(
        expected: BigDecimal,
        precisionCalculation: (MathContext) -> BigDecimal,
        startPrecision: Int,
        endPrecision: Int
    ) {
        var precision = startPrecision
        while (precision <= endPrecision) {
            val mathContext = MathContext(precision)
            println("Testing precision=$precision")
            assertBigDecimal(
                "precision=$precision",
                expected.round(mathContext),
                precisionCalculation(mathContext),
                mathContext
            )
            precision += getPrecisionStep()
        }
    }

    private fun getPrecisionStep(): Int {
        return when (TEST_LEVEL) {
            TestLevel.Fast -> 50
            TestLevel.Medium -> 20
            TestLevel.Slow -> 5
        }
    }

    private fun toCheck(value: Double): BigDecimal? {
        val longValue = value.toLong()
        if (value == longValue.toDouble()) {
            return BigDecimal.valueOf(longValue)
        }
        return if (java.lang.Double.isFinite(value)) {
            BigDecimal.valueOf(value)
        } else null
    }

    private fun toCheck(value: BigDecimal): BigDecimal {
        return BigDecimal.valueOf(value.round(MC_CHECK_DOUBLE).toDouble())
    }

    private fun assertBigDecimal(expected: BigDecimal, actual: BigDecimal, mathContext: MathContext): Boolean {
        return assertBigDecimal("assertBigDecimal", expected, actual, mathContext)
    }

    private fun assertBigDecimal(
        description: String,
        expected: BigDecimal,
        actual: BigDecimal,
        mathContext: MathContext
    ): Boolean {
        val calculationMathContext = MathContext(mathContext.precision + 10)
        val error = expected.subtract(actual, calculationMathContext).abs()
        val acceptableError = actual.round(mathContext).ulp()
        val fullDescription =
            "$description expected=$expected : actual=$actual : precision=$mathContext.precision : error=$error : acceptableError=$acceptableError"
        assertTrue(error <= acceptableError, fullDescription)
        return error.signum() == 0
    }

    private fun assertThrows(exceptionClass: Class<out Exception?>, runnable: Runnable): Exception? {
        return assertThrows(exceptionClass, null, runnable)
    }

    private fun assertThrows(exceptionClass: Class<out Exception?>, message: String?, runnable: Runnable): Exception? {
        val result: Exception? /* = java.lang.Exception? */
        try {
            runnable.run()
            fail("Expected: $exceptionClass.name")
        } catch (exception: Exception) {
            if (!exceptionClass.isAssignableFrom(exception.javaClass)) {
                fail("Expected: $exceptionClass.name")
            }
            if (message != null && message != exception.message) {
                fail("Expected: $exceptionClass.name with message: `$message` but received message: `$exception.message`")
            }
            result = exception
        }
        return result
    }

    private fun getTestLevel(): TestLevel {
        var level = TestLevel.Fast
        val envTestLevel = System.getenv("BIGDECIMALTEST_LEVEL")
        if (envTestLevel != null) {
            try {
                level = TestLevel.valueOf(envTestLevel)
            } catch (ex: IllegalArgumentException) {
                System.err.println("Illegal env var TEST_LEVEL: $envTestLevel")
            }
        }
        return level
    }
}

