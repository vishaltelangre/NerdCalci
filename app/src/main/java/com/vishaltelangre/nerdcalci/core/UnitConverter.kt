package com.vishaltelangre.nerdcalci.core

import java.math.BigDecimal
import java.math.MathContext as JavaMathContext
import kotlin.math.PI

/**
 * Categories of units to enforce dimensional safety during conversion.
 */
enum class UnitCategory {
    TIME, LENGTH, AREA, VOLUME, MASS, SPEED, ANGLE, TEMPERATURE, FREQUENCY, ENERGY, POWER, DATA, DATA_RATE,
    FORCE, FUEL_CONSUMPTION, PRESSURE, NUMERAL_SYSTEM, SCALAR
}

/**
 * Represents a unit of measurement.
 *
 * @property name Descriptive name (e.g., "Kilometer")
 * @property symbols Symbols and aliases that can trigger this unit (e.g., "km")
 * @property category The dimensional category
 * @property factor Scale factor relative to the category's base unit (multiplicative units only)
 * @property customToBase Optional custom lambda for non-linear units (e.g. Temperature) or dynamic units (CSS)
 * @property customFromBase Optional custom lambda for converting back from base
 */
data class Unit(
    val name: String,
    val symbols: List<String>,
    val category: UnitCategory,
    val factor: BigDecimal = BigDecimal.ONE,
    val customToBase: ((BigDecimal, Map<String, EvaluationResult>) -> BigDecimal)? = null,
    val customFromBase: ((BigDecimal, Map<String, EvaluationResult>) -> BigDecimal)? = null
)

object UnitConverter {
    private val CUBIC_METER_TO_LITER = BigDecimal("1000.0")

    private data class UnitRule(
        val left: UnitCategory,
        val right: UnitCategory,
        val result: (Unit, Unit) -> String?,
        val scale: BigDecimal = BigDecimal.ONE,
    )

    // Multiplication rules are explicit and ordered from most specific to most general.
    private val MULTIPLICATION_RULES = listOf(
        UnitRule(UnitCategory.LENGTH, UnitCategory.LENGTH, result = { left, right ->
            sameSymbolFamily(left, right, ::squareSymbolForFamily)
        }),
        UnitRule(UnitCategory.AREA, UnitCategory.LENGTH, result = { left, right ->
            sameAreaLengthFamily(left, right)?.let { cubeSymbolForFamily(it) }
        }),
        UnitRule(UnitCategory.LENGTH, UnitCategory.AREA, result = { left, right ->
            sameAreaLengthFamily(right, left)?.let { cubeSymbolForFamily(it) }
        }),
        UnitRule(UnitCategory.SPEED, UnitCategory.TIME, result = { left, right ->
            speedAndTimeToLength(left.symbols[0], right.symbols[0])
        })
    )

    // Division rules are explicit, reversible, and keep same-category cancellation separate.
    private val DIVISION_RULES = listOf(
        UnitRule(UnitCategory.AREA, UnitCategory.LENGTH, result = { left, right ->
            sameAreaLengthFamily(left, right)?.let { lengthSymbolForFamily(it) }
        }),
        UnitRule(UnitCategory.VOLUME, UnitCategory.LENGTH, result = { left, right ->
            sameVolumeLengthFamily(left, right)?.let { squareSymbolForFamily(it) }
        }),
        UnitRule(UnitCategory.VOLUME, UnitCategory.AREA, result = { left, right ->
            sameVolumeAreaFamily(left, right)?.let { lengthSymbolForFamily(it) }
        }),
        UnitRule(UnitCategory.TIME, UnitCategory.TIME, result = { _, _ -> "unitless" }),
        UnitRule(UnitCategory.LENGTH, UnitCategory.LENGTH, result = { _, _ -> "unitless" }),
        UnitRule(UnitCategory.AREA, UnitCategory.AREA, result = { _, _ -> "unitless" }),
        UnitRule(UnitCategory.VOLUME, UnitCategory.VOLUME, result = { _, _ -> "unitless" }),
        UnitRule(UnitCategory.MASS, UnitCategory.MASS, result = { _, _ -> "unitless" }),
        UnitRule(UnitCategory.SPEED, UnitCategory.SPEED, result = { _, _ -> "unitless" }),
        UnitRule(UnitCategory.ANGLE, UnitCategory.ANGLE, result = { _, _ -> "unitless" }),
        UnitRule(UnitCategory.FREQUENCY, UnitCategory.FREQUENCY, result = { _, _ -> "unitless" }),
        UnitRule(UnitCategory.ENERGY, UnitCategory.ENERGY, result = { _, _ -> "unitless" }),
        UnitRule(UnitCategory.POWER, UnitCategory.POWER, result = { _, _ -> "unitless" }),
        UnitRule(UnitCategory.DATA, UnitCategory.DATA, result = { _, _ -> "unitless" }),
        UnitRule(UnitCategory.DATA_RATE, UnitCategory.DATA_RATE, result = { _, _ -> "unitless" }),
        UnitRule(UnitCategory.FORCE, UnitCategory.FORCE, result = { _, _ -> "unitless" }),
        UnitRule(UnitCategory.PRESSURE, UnitCategory.PRESSURE, result = { _, _ -> "unitless" }),
        UnitRule(UnitCategory.NUMERAL_SYSTEM, UnitCategory.NUMERAL_SYSTEM, result = { _, _ -> "unitless" }),
        UnitRule(UnitCategory.LENGTH, UnitCategory.TIME, result = { left, right ->
            lengthAndTimeToSpeed(left.symbols[0], right.symbols[0])
        })
    )

    // Base units:
    // TIME: second (s)
    // LENGTH: meter (m)
    // AREA: square meter (m²)
    // VOLUME: liter (L)
    // MASS: kilogram (kg) -> Wait, let's use Gram (g) as base for easier factor math, or kg. Let's use Gram.
    // SPEED: meters per second (m/s)
    // ANGLE: radians (rad)
    // FREQUENCY: hertz (Hz)
    // ENERGY: joule (J)
    // POWER: watt (W)
    // DATA: byte (B)
    // DATA_RATE: bytes per second (Bps)

    val UNITS = listOf(
        // --- TIME --- (Base: second)
        Unit("Nanosecond", listOf("ns", "nanosecond", "nanoseconds"), UnitCategory.TIME, BigDecimal("1e-9")),
        Unit("Microsecond", listOf("µs", "us", "microsecond", "microseconds"), UnitCategory.TIME, BigDecimal("1e-6")),
        Unit("Millisecond", listOf("ms", "millisecond", "milliseconds"), UnitCategory.TIME, BigDecimal("0.001")),
        Unit("Second", listOf("s", "sec", "secs", "second", "seconds"), UnitCategory.TIME, BigDecimal.ONE),
        Unit("Minute", listOf("min", "mins", "minute", "minutes"), UnitCategory.TIME, BigDecimal("60.0")),
        Unit("Hour", listOf("h", "hr", "hrs", "hour", "hours"), UnitCategory.TIME, BigDecimal("3600.0")),
        Unit("Day", listOf("d", "day", "days"), UnitCategory.TIME, BigDecimal("86400.0")),
        Unit("Week", listOf("wk", "wks", "week", "weeks"), UnitCategory.TIME, BigDecimal("604800.0")),
        Unit("Month", listOf("mo", "mnth", "mnths", "month", "months"), UnitCategory.TIME, BigDecimal("2629746.0")), // 30.436875 days
        Unit("Year", listOf("y", "yr", "yrs", "year", "years"), UnitCategory.TIME, BigDecimal("31556952.0")), // 365.2425 days
        Unit("Lustrum", listOf("lustrum", "lustrums"), UnitCategory.TIME, BigDecimal("157784760.0")),
        Unit("Decade", listOf("decade", "decades"), UnitCategory.TIME, BigDecimal("315569520.0")),
        Unit("Century", listOf("century", "centuries"), UnitCategory.TIME, BigDecimal("3155695200.0")),
        Unit("Millennium", listOf("millennium", "millennia", "millenniums"), UnitCategory.TIME, BigDecimal("31556952000.0")),
        Unit("Decisecond", listOf("ds", "decisecond", "deciseconds"), UnitCategory.TIME, BigDecimal("0.1")),
        Unit("Centisecond", listOf("cs", "centisecond", "centiseconds"), UnitCategory.TIME, BigDecimal("0.01")),

        // --- LENGTH --- (Base: meter)
        Unit("Nanometer", listOf("nm", "nanometer", "nanometers"), UnitCategory.LENGTH, BigDecimal("1e-9")),
        Unit("Micrometer", listOf("µm", "um", "micrometer", "micrometers"), UnitCategory.LENGTH, BigDecimal("1e-6")),
        Unit("Millimeter", listOf("mm", "millimeter", "millimeters"), UnitCategory.LENGTH, BigDecimal("0.001")),
        Unit("Centimeter", listOf("cm", "centimeter", "centimeters"), UnitCategory.LENGTH, BigDecimal("0.01")),
        Unit("Decimeter", listOf("dm", "decimeter", "decimeters"), UnitCategory.LENGTH, BigDecimal("0.1")),
        Unit("Meter", listOf("m", "meter", "meters"), UnitCategory.LENGTH, BigDecimal.ONE),
        Unit("Kilometer", listOf("km", "kms", "kilometer", "kilometers"), UnitCategory.LENGTH, BigDecimal("1000.0")),
        // Imperial Length
        Unit("Inch", listOf("inch", "inches"), UnitCategory.LENGTH, BigDecimal("0.0254")),
        Unit("Foot", listOf("ft", "foot", "feet"), UnitCategory.LENGTH, BigDecimal("0.3048")),
        Unit("Yard", listOf("yd", "yard", "yards"), UnitCategory.LENGTH, BigDecimal("0.9144")),
        Unit("Mile", listOf("mi", "mile", "miles"), UnitCategory.LENGTH, BigDecimal("1609.344")),
        Unit("Furlong", listOf("fur", "furlong"), UnitCategory.LENGTH, BigDecimal("201.168")),
        Unit("Fathom", listOf("ftm", "fathom"), UnitCategory.LENGTH, BigDecimal("1.8288")),
        Unit("Nautical Mile", listOf("NM", "nmi"), UnitCategory.LENGTH, BigDecimal("1852.0")),
        Unit("Light Year", listOf("ly"), UnitCategory.LENGTH, BigDecimal("9.4607304725808e15")),
        Unit("Angstrom", listOf("Å", "angstrom", "angstroms"), UnitCategory.LENGTH, BigDecimal("1e-10")),
        Unit("Picometer", listOf("pm", "picometer", "picometers"), UnitCategory.LENGTH, BigDecimal("1e-12")),
        Unit("Astronomical Unit", listOf("au", "AU", "astronomical unit", "astronomical units"), UnitCategory.LENGTH, BigDecimal("149597870700.0")),

        // --- AREA --- (Base: square meter)
        Unit("Square Nanometer", listOf("nm²", "nm2", "sqnm", "square nanometer", "square nanometers"), UnitCategory.AREA, BigDecimal("1e-18")),
        Unit("Square Micrometer", listOf("µm²","µm2", "um2", "squm", "square micrometer", "square micrometers"), UnitCategory.AREA, BigDecimal("1e-12")),
        Unit("Square Millimeter", listOf("mm²", "mm2", "sqmm", "square millimeter", "square millimeters"), UnitCategory.AREA, BigDecimal("1e-6")),
        Unit("Square Centimeter", listOf("cm²", "cm2", "sqcm", "square centimeter", "square centimeters"), UnitCategory.AREA, BigDecimal("1e-4")),
        Unit("Square Meter", listOf("m²", "m2", "sqm", "square meter", "square meters"), UnitCategory.AREA, BigDecimal.ONE),
        Unit("Square Kilometer", listOf("km²", "km2", "sqkm", "square kilometer", "square kilometers"), UnitCategory.AREA, BigDecimal("1e6")),
        Unit("Square Inch", listOf("in²", "in2", "sqin", "square inch", "square inches"), UnitCategory.AREA, BigDecimal("0.00064516")),
        Unit("Square Feet", listOf("ft²", "ft2", "sqft", "square foot", "square feet"), UnitCategory.AREA, BigDecimal("0.09290304")),
        Unit("Square Yard", listOf("yd²", "yd2", "sqyd", "square yard", "square yards"), UnitCategory.AREA, BigDecimal("0.83612736")),
        Unit("Square Mile", listOf("mi²", "mi2", "sqmi", "square mile", "square miles"), UnitCategory.AREA, BigDecimal("2589988.110336")),
        Unit("Acre", listOf("ac", "acre", "acres"), UnitCategory.AREA, BigDecimal("4046.8564224")),
        Unit("Hectare", listOf("ha", "hectare", "hectares"), UnitCategory.AREA, BigDecimal("10000.0")),

        // --- VOLUME --- (Base: Liter)
        Unit("Milliliter", listOf("mL", "ml", "milliliter", "milliliters"), UnitCategory.VOLUME, BigDecimal("0.001")),
        Unit("Liter", listOf("L", "l", "liter", "liters"), UnitCategory.VOLUME, BigDecimal.ONE),
        Unit("Kiloliter", listOf("kL", "kl", "kiloliter", "kiloliters"), UnitCategory.VOLUME, BigDecimal("1000.0")),
        Unit("Megaliter", listOf("ML", "megaliter", "megaliters"), UnitCategory.VOLUME, BigDecimal("1000000.0")),
        Unit("Cubic Centimeter", listOf("cm³", "cm3", "cc", "cubic centimeter", "cubic centimeters"), UnitCategory.VOLUME, BigDecimal("0.001")), // 1cm³ = 1mL
        Unit("Cubic Meter", listOf("m³", "m3", "cubic meter", "cubic meters"), UnitCategory.VOLUME, BigDecimal("1000.0")), // 1m³ = 1000L
        Unit("Deciliter", listOf("dL", "dl", "deciliter", "deciliters"), UnitCategory.VOLUME, BigDecimal("0.1")),
        Unit("Centiliter", listOf("cL", "cl", "centiliter", "centiliters"), UnitCategory.VOLUME, BigDecimal("0.01")),
        Unit("Microliter", listOf("µL", "uL", "µl", "ul", "microliter", "microliters"), UnitCategory.VOLUME, BigDecimal("1e-6")),
        Unit("Cubic Millimeter", listOf("mm³", "mm3", "cubic millimeter", "cubic millimeters"), UnitCategory.VOLUME, BigDecimal("1e-6")),

        Unit("Gallon", listOf("gal", "gallon", "gallons", "US gallon", "US gallons"), UnitCategory.VOLUME, BigDecimal("3.785411784")),
        Unit("Quart", listOf("qt", "quart", "quarts", "US quarts"), UnitCategory.VOLUME, BigDecimal("0.946352946")),
        Unit("Pint", listOf("pint", "pints", "US pints"), UnitCategory.VOLUME, BigDecimal("0.473176473")),
        Unit("Cup", listOf("cup", "cups", "US cups"), UnitCategory.VOLUME, BigDecimal("0.2365882365")),
        Unit("Fluid Ounce", listOf("fl oz", "floz", "fluid ounce", "fluid ounces", "US fluid ounces"), UnitCategory.VOLUME, BigDecimal("0.0295735295625")),

        Unit("Gallon (Imperial)", listOf("gal_imp", "imperial gallon", "imperial gallons"), UnitCategory.VOLUME, BigDecimal("4.54609")),
        Unit("Quart (Imperial)", listOf("qt_imp", "imperial quart", "imperial quarts"), UnitCategory.VOLUME, BigDecimal("1.1365225")),
        Unit("Pint (Imperial)", listOf("pint_imp", "imperial pint", "imperial pints"), UnitCategory.VOLUME, BigDecimal("0.56826125")),
        Unit("Fluid Ounce (Imperial)", listOf("fl_oz_imp", "imperial fluid ounce", "imperial fluid ounces"), UnitCategory.VOLUME, BigDecimal("0.0284130625")),
        Unit("Gill (US)", listOf("gi_us", "US gill", "US gills"), UnitCategory.VOLUME, BigDecimal("0.11829411825")),
        Unit("Gill (Imperial)", listOf("gi_imp", "imperial gill", "imperial gills"), UnitCategory.VOLUME, BigDecimal("0.1420653125")),
        Unit("Tablespoon", listOf("tbsp", "tablespoon", "tablespoons"), UnitCategory.VOLUME, BigDecimal("0.01478676478125")),
        Unit("Teaspoon", listOf("tsp", "teaspoon", "teaspoons"), UnitCategory.VOLUME, BigDecimal("0.00492892159375")),
        Unit("Cubic Inch", listOf("in³", "in3", "cubic inch", "cubic inches"), UnitCategory.VOLUME, BigDecimal("0.016387064")),
        Unit("Cubic Feet", listOf("ft³", "ft3", "cuft", "cubic foot", "cubic feet"), UnitCategory.VOLUME, BigDecimal("28.316846592")),

        // --- MASS --- (Base: Gram)
        Unit("Nanogram", listOf("ng", "nanogram", "nanograms"), UnitCategory.MASS, BigDecimal("1e-9")),
        Unit("Microgram", listOf("mcg", "µg", "ug", "microgram", "micrograms"), UnitCategory.MASS, BigDecimal("1e-6")),
        Unit("Milligram", listOf("mg", "milligram", "milligrams"), UnitCategory.MASS, BigDecimal("0.001")),
        Unit("Gram", listOf("g", "gram", "grams"), UnitCategory.MASS, BigDecimal.ONE),
        Unit("Kilogram", listOf("kg", "kgs", "kilograms"), UnitCategory.MASS, BigDecimal("1000.0")),
        Unit("Metric Ton", listOf("t", "tonne", "tonnes", "ton", "tons", "metric ton", "metric tons", "metric tonne", "metric tonnes"), UnitCategory.MASS, BigDecimal("1000000.0")),
        Unit("Ounce", listOf("oz", "ounce", "ounces"), UnitCategory.MASS, BigDecimal("28.349523125")),
        Unit("Pound", listOf("lb", "lbs", "pound", "pounds"), UnitCategory.MASS, BigDecimal("453.59237")),
        Unit("Stone", listOf("st", "stone", "stones"), UnitCategory.MASS, BigDecimal("6350.29318")),
        Unit("Short Ton", listOf("sh ton", "short ton", "short tons"), UnitCategory.MASS, BigDecimal("907184.74")),
        Unit("Troy Ounce", listOf("ozt", "oz t", "troy ounce", "troy ounces"), UnitCategory.MASS, BigDecimal("31.1034768")),
        Unit("Carat", listOf("ct", "carat", "carats"), UnitCategory.MASS, BigDecimal("0.2")),
        Unit("Ettogram", listOf("hg", "ettogram", "ettograms"), UnitCategory.MASS, BigDecimal("100.0")),
        Unit("Centigram", listOf("cg", "centigram", "centigrams"), UnitCategory.MASS, BigDecimal("0.01")),
        Unit("Quintal", listOf("q", "quintal", "quintals"), UnitCategory.MASS, BigDecimal("100000.0")),
        Unit("Pennyweight", listOf("dwt", "pennyweight"), UnitCategory.MASS, BigDecimal("1.55517384")),
        Unit("Unified atomic mass unit", listOf("u", "amu"), UnitCategory.MASS, BigDecimal("1.66053906660e-24")),

        // --- SPEED --- (Base: m/s)
        Unit("Meter per second", listOf("mps", "meters per second"), UnitCategory.SPEED, BigDecimal.ONE),
        Unit("Kilometer per hour", listOf("kmh", "kph", "kmph", "kilometers per hour"), UnitCategory.SPEED, BigDecimal.ONE.divide(BigDecimal("3.6"), JavaMathContext.DECIMAL128)),
        Unit("Miles per hour", listOf("mph", "miph", "miles per hour"), UnitCategory.SPEED, BigDecimal("0.44704")),
        Unit("Knot", listOf("kn", "knot", "knots"), UnitCategory.SPEED, BigDecimal("0.514444")),
        Unit("Feet per second", listOf("fps", "feet per second"), UnitCategory.SPEED, BigDecimal("0.3048")),
        Unit("Speed of light", listOf("speed of light"), UnitCategory.SPEED, BigDecimal("299792458.0")),

        // --- ANGLE --- (Base: Radian)
        Unit("Radian", listOf("rad", "radian", "radians"), UnitCategory.ANGLE, BigDecimal.ONE),
        Unit("Degree", listOf("deg", "degree", "degrees", "°"), UnitCategory.ANGLE, BigDecimal.valueOf(PI).divide(BigDecimal("180.0"), JavaMathContext.DECIMAL128)),
        Unit("Minute of arc", listOf("arcmin", "minute of arc"), UnitCategory.ANGLE, BigDecimal.valueOf(PI).divide(BigDecimal("10800.0"), JavaMathContext.DECIMAL128)),
        Unit("Second of arc", listOf("arcsec", "second of arc"), UnitCategory.ANGLE, BigDecimal.valueOf(PI).divide(BigDecimal("648000.0"), JavaMathContext.DECIMAL128)),

        // --- TEMPERATURE --- (Base: Kelvin)
        Unit("Celsius", listOf("°C", "C", "celsius", "degC", "degree celsius"), UnitCategory.TEMPERATURE, BigDecimal.ONE,
            customToBase = { v, _ -> v.add(BigDecimal("273.15")) },
            customFromBase = { v, _ -> v.subtract(BigDecimal("273.15")) }
        ),
        Unit("Fahrenheit", listOf("°F", "F", "fahrenheit", "degF", "degree fahrenheit"), UnitCategory.TEMPERATURE, BigDecimal.ONE,
            customToBase = { v, _ -> v.subtract(BigDecimal("32.0")).multiply(BigDecimal("5.0")).divide(BigDecimal("9.0"), JavaMathContext.DECIMAL128).add(BigDecimal("273.15")) },
            customFromBase = { v, _ -> v.subtract(BigDecimal("273.15")).multiply(BigDecimal("9.0")).divide(BigDecimal("5.0"), JavaMathContext.DECIMAL128).add(BigDecimal("32.0")) }
        ),
        Unit("Kelvin", listOf("K", "kelvin"), UnitCategory.TEMPERATURE, BigDecimal.ONE),
        Unit("Reaumur", listOf("°Re", "Re", "reaumur", "Réaumur"), UnitCategory.TEMPERATURE, BigDecimal.ONE,
            customToBase = { v, _ -> v.divide(BigDecimal("0.8"), JavaMathContext.DECIMAL128).add(BigDecimal("273.15")) },
            customFromBase = { v, _ -> v.subtract(BigDecimal("273.15")).multiply(BigDecimal("0.8")) }
        ),
        Unit("Rømer", listOf("°Rø", "Rø", "romer", "Rømer"), UnitCategory.TEMPERATURE, BigDecimal.ONE,
            customToBase = { v, _ -> v.subtract(BigDecimal("7.5")).multiply(BigDecimal("40.0")).divide(BigDecimal("21.0"), JavaMathContext.DECIMAL128).add(BigDecimal("273.15")) },
            customFromBase = { v, _ -> v.subtract(BigDecimal("273.15")).multiply(BigDecimal("21.0")).divide(BigDecimal("40.0"), JavaMathContext.DECIMAL128).add(BigDecimal("7.5")) }
        ),
        Unit("Delisle", listOf("°De", "De", "delisle"), UnitCategory.TEMPERATURE, BigDecimal.ONE,
            customToBase = { v, _ -> BigDecimal("373.15").subtract(v.multiply(BigDecimal("2.0")).divide(BigDecimal("3.0"), JavaMathContext.DECIMAL128)) },
            customFromBase = { v, _ -> BigDecimal("373.15").subtract(v).multiply(BigDecimal("1.5")) }
        ),
        Unit("Rankine", listOf("°Ra", "Ra", "rankine"), UnitCategory.TEMPERATURE, BigDecimal.ONE,
            customToBase = { v, _ -> v.divide(BigDecimal("1.8"), JavaMathContext.DECIMAL128) },
            customFromBase = { v, _ -> v.multiply(BigDecimal("1.8")) }
        ),

        // --- FREQUENCY --- (Base: Hertz)
        Unit("Hertz", listOf("Hz", "hertz"), UnitCategory.FREQUENCY, BigDecimal.ONE),
        Unit("Kilohertz", listOf("kHz", "kilohertz"), UnitCategory.FREQUENCY, BigDecimal("1000.0")),
        Unit("Megahertz", listOf("MHz", "megahertz"), UnitCategory.FREQUENCY, BigDecimal("1e6")),
        Unit("Gigahertz", listOf("GHz", "gigahertz"), UnitCategory.FREQUENCY, BigDecimal("1e9")),

        // --- ENERGY --- (Base: Joule)
        Unit("Joule", listOf("J", "joule", "joules"), UnitCategory.ENERGY, BigDecimal.ONE),
        Unit("Kilojoule", listOf("kJ", "kilojoule", "kilojoules"), UnitCategory.ENERGY, BigDecimal("1000.0")),
        Unit("Megajoule", listOf("MJ", "megajoule", "megajoules"), UnitCategory.ENERGY, BigDecimal("1e6")),
        Unit("Gigajoule", listOf("GJ", "gigajoule", "gigajoules"), UnitCategory.ENERGY, BigDecimal("1e9")),
        Unit("Calorie", listOf("cal", "calorie", "calories"), UnitCategory.ENERGY, BigDecimal("4.184")),
        Unit("Kilocalorie", listOf("kCal", "kcal", "kilocalorie", "kilocalories"), UnitCategory.ENERGY, BigDecimal("4184.0")),
        Unit("Watt hour", listOf("Wh", "watt hour", "watt hours"), UnitCategory.ENERGY, BigDecimal("3600.0")),
        Unit("Kilowatt hour", listOf("kWh", "kilowatt hour", "kilowatt hours"), UnitCategory.ENERGY, BigDecimal("3.6e6")),
        Unit("Electron volt", listOf("eV", "electronvolt", "electron volts"), UnitCategory.ENERGY, BigDecimal("1.602176634e-19")),
        Unit("Foot pound-force", listOf("ft lbf", "ft_lbf", "foot_pound"), UnitCategory.ENERGY, BigDecimal("1.3558179483314")),
        Unit("British thermal unit", listOf("BTU", "btu"), UnitCategory.ENERGY, BigDecimal("1055.05585262")),
        Unit("Tons of TNT equivalent", listOf("tTNT", "ton of TNT", "tons of TNT", "tonne of TNT", "tonnes of TNT"), UnitCategory.ENERGY, BigDecimal("4.184e9")),
        Unit("Kilotons of TNT equivalent", listOf("ktTNT", "kiloton of TNT", "kilotons of TNT", "kilotonne of TNT", "kilotonnes of TNT"), UnitCategory.ENERGY, BigDecimal("4.184e12")),
        Unit("Megatons of TNT equivalent", listOf("MtTNT", "megaton of TNT", "megatons of TNT", "megatonne of TNT", "megatonnes of TNT"), UnitCategory.ENERGY, BigDecimal("4.184e15")),

        // --- POWER --- (Base: Watt)
        Unit("Watt", listOf("W", "watt", "watts"), UnitCategory.POWER, BigDecimal.ONE),
        Unit("Milliwatt", listOf("mW", "milliwatt", "milliwatts"), UnitCategory.POWER, BigDecimal("0.001")),
        Unit("Kilowatt", listOf("kW", "kilowatt", "kilowatts"), UnitCategory.POWER, BigDecimal("1000.0")),
        Unit("Megawatt", listOf("MW", "megawatt", "megawatts"), UnitCategory.POWER, BigDecimal("1e6")),
        Unit("Horsepower", listOf("hp", "horsepower", "horsepowers"), UnitCategory.POWER, BigDecimal("745.7")),
        Unit("Gigawatt", listOf("GW", "gigawatt", "gigawatts"), UnitCategory.POWER, BigDecimal("1e9")),

        // --- DATA --- (Base: Byte)
        Unit("Bit", listOf("bit", "bits", "b"), UnitCategory.DATA, BigDecimal("0.125")),
        Unit("Nibble", listOf("nibble", "nibbles"), UnitCategory.DATA, BigDecimal("0.5")),
        Unit("Byte", listOf("B", "byte", "bytes"), UnitCategory.DATA, BigDecimal.ONE),
        Unit("Kilobyte", listOf("kB", "KB", "kilobyte", "kilobytes"), UnitCategory.DATA, BigDecimal("1000.0")),
        Unit("Megabyte", listOf("MB", "megabyte", "megabytes"), UnitCategory.DATA, BigDecimal("1000000.0")),
        Unit("Gigabyte", listOf("GB", "gigabyte", "gigabytes"), UnitCategory.DATA, BigDecimal("1000000000.0")),
        Unit("Terabyte", listOf("TB", "terabyte", "terabytes"), UnitCategory.DATA, BigDecimal("1e12")),
        Unit("Kibibyte", listOf("KiB", "kibibyte", "kibibytes"), UnitCategory.DATA, BigDecimal("1024.0")),
        Unit("Mebibyte", listOf("MiB", "mebibyte", "mebibytes"), UnitCategory.DATA, BigDecimal("1048576.0")),
        Unit("Gibibyte", listOf("GiB", "gibibyte", "gibibytes"), UnitCategory.DATA, BigDecimal("1073741824.0")),

        Unit("Tebibyte", listOf("TiB", "tebibyte", "tebibytes"), UnitCategory.DATA, BigDecimal("1099511627776")),
        Unit("Pebibyte", listOf("PiB", "pebibyte", "pebibytes"), UnitCategory.DATA, BigDecimal("1125899906842624")),
        Unit("Exbibyte", listOf("EiB", "exbibyte", "exbibytes"), UnitCategory.DATA, BigDecimal("1152921504606846976")),
        Unit("Petabyte", listOf("PB", "petabyte", "petabytes"), UnitCategory.DATA, BigDecimal("1e15")),
        Unit("Exabyte", listOf("EB", "exabyte", "exabytes"), UnitCategory.DATA, BigDecimal("1e18")),

        Unit("Kibibit", listOf("Kibit", "kibibit"), UnitCategory.DATA, BigDecimal("128")),
        Unit("Mebibit", listOf("Mibit", "mebibit"), UnitCategory.DATA, BigDecimal("131072")),
        Unit("Gibibit", listOf("Gibit", "gibibit"), UnitCategory.DATA, BigDecimal("134217728")),
        Unit("Tebibit", listOf("Tibit", "tebibit"), UnitCategory.DATA, BigDecimal("137438953472")),
        Unit("Pebibit", listOf("Pibit", "pebibit"), UnitCategory.DATA, BigDecimal("140737488355328")),
        Unit("Exbibit", listOf("Eibit", "exbibit"), UnitCategory.DATA, BigDecimal("144115188075855872")),

        Unit("Kilobit", listOf("kb", "kilobit"), UnitCategory.DATA, BigDecimal("125.0")),
        Unit("Megabit", listOf("Mb", "megabit"), UnitCategory.DATA, BigDecimal("125000.0")),
        Unit("Gigabit", listOf("Gb", "gigabit"), UnitCategory.DATA, BigDecimal("1.25e8")),
        Unit("Terabit", listOf("Tb", "terabit"), UnitCategory.DATA, BigDecimal("1.25e11")),
        Unit("Petabit", listOf("Pb", "petabit"), UnitCategory.DATA, BigDecimal("1.25e14")),
        Unit("Exabit", listOf("Eb", "exabit"), UnitCategory.DATA, BigDecimal("1.25e17")),

        // --- FORCE --- (Base: Newton)
        Unit("Newton", listOf("N", "newton", "newtons"), UnitCategory.FORCE, BigDecimal.ONE),
        Unit("Kilogram-force", listOf("kgf", "kg_f"), UnitCategory.FORCE, BigDecimal("9.80665")),
        Unit("Pound-force", listOf("lbf", "lb_f"), UnitCategory.FORCE, BigDecimal("4.448222")),
        Unit("Dyne", listOf("dyn", "dyne"), UnitCategory.FORCE, BigDecimal("1e-5")),
        Unit("Poundal", listOf("pdl"), UnitCategory.FORCE, BigDecimal("0.138255")),

        // --- FUEL CONSUMPTION --- (Base: Liters per 100 km)
        Unit("Liters per 100 km", listOf("l100km", "L100km", "liters per 100km", "liters per 100 km"), UnitCategory.FUEL_CONSUMPTION, BigDecimal.ONE),
        Unit("Kilometers per liter", listOf("kmpl", "kpl", "kilometers per liter"), UnitCategory.FUEL_CONSUMPTION, BigDecimal.ONE,
            customToBase = { v, _ -> if (v.compareTo(BigDecimal.ZERO) > 0) BigDecimal("100.0").divide(v, JavaMathContext.DECIMAL128) else BigDecimal.ZERO },
            customFromBase = { v, _ -> if (v.compareTo(BigDecimal.ZERO) > 0) BigDecimal("100.0").divide(v, JavaMathContext.DECIMAL128) else BigDecimal.ZERO }
        ),
        Unit("Miles per Gallon (US)", listOf("mpg", "mpg_us"), UnitCategory.FUEL_CONSUMPTION, BigDecimal.ONE,
            customToBase = { v, _ -> if (v.compareTo(BigDecimal.ZERO) > 0) BigDecimal("235.214583").divide(v, JavaMathContext.DECIMAL128) else BigDecimal.ZERO },
            customFromBase = { v, _ -> if (v.compareTo(BigDecimal.ZERO) > 0) BigDecimal("235.214583").divide(v, JavaMathContext.DECIMAL128) else BigDecimal.ZERO }
        ),
        Unit("Miles per Gallon (Imperial)", listOf("mpg_imp", "mpg_uk"), UnitCategory.FUEL_CONSUMPTION, BigDecimal.ONE,
            customToBase = { v, _ -> if (v.compareTo(BigDecimal.ZERO) > 0) BigDecimal("282.480936").divide(v, JavaMathContext.DECIMAL128) else BigDecimal.ZERO },
            customFromBase = { v, _ -> if (v.compareTo(BigDecimal.ZERO) > 0) BigDecimal("282.480936").divide(v, JavaMathContext.DECIMAL128) else BigDecimal.ZERO }
        ),

        // --- PRESSURE --- (Base: Pascal)
        Unit("Pascal", listOf("Pa", "pascal"), UnitCategory.PRESSURE, BigDecimal.ONE),
        Unit("Kilopascal", listOf("kPa", "kilopascal"), UnitCategory.PRESSURE, BigDecimal("1000.0")),
        Unit("Megapascal", listOf("MPa", "megapascal"), UnitCategory.PRESSURE, BigDecimal("1e6")),
        Unit("Gigapascal", listOf("GPa", "gigapascal"), UnitCategory.PRESSURE, BigDecimal("1e9")),
        Unit("Hectopascal", listOf("hPa", "hectopascal"), UnitCategory.PRESSURE, BigDecimal("100.0")),
        Unit("Bar", listOf("bar", "bars"), UnitCategory.PRESSURE, BigDecimal("100000.0")),
        Unit("Millibar", listOf("mbar", "millibar"), UnitCategory.PRESSURE, BigDecimal("100.0")),
        Unit("Atmosphere", listOf("atm", "atmosphere"), UnitCategory.PRESSURE, BigDecimal("101325.0")),
        Unit("Psi", listOf("psi", "pound per square inch"), UnitCategory.PRESSURE, BigDecimal("6894.757")),
        Unit("Ksi", listOf("ksi"), UnitCategory.PRESSURE, BigDecimal("6894757.0")),
        Unit("Torr", listOf("torr", "mmHg"), UnitCategory.PRESSURE, BigDecimal("133.3224")),
        Unit("Inches of Mercury", listOf("inHg"), UnitCategory.PRESSURE, BigDecimal("3386.388")),

        // --- NUMERAL SYSTEM ---
        Unit("Decimal", listOf("dec", "decimal"), UnitCategory.NUMERAL_SYSTEM, BigDecimal.TEN),
        Unit("Hexadecimal", listOf("hex", "hexadecimal"), UnitCategory.NUMERAL_SYSTEM, BigDecimal("16.0")),
        Unit("Octal", listOf("oct", "octal"), UnitCategory.NUMERAL_SYSTEM, BigDecimal("8.0")),
        Unit("Binary", listOf("bin", "binary"), UnitCategory.NUMERAL_SYSTEM, BigDecimal("2.0")),

        // --- SCALAR MULTIPLIERS ---
        Unit("Dozens", listOf("dozen", "dozens"), UnitCategory.SCALAR, BigDecimal("12.0")),
        Unit("Hundred", listOf("hundred", "hundreds"), UnitCategory.SCALAR, BigDecimal("100.0")),
        Unit("Thousand", listOf("thousand", "thousands"), UnitCategory.SCALAR, BigDecimal("1000.0")),
        Unit("Lakh", listOf("lakh", "lakhs"), UnitCategory.SCALAR, BigDecimal("100000.0")),
        Unit("Million", listOf("million", "millions"), UnitCategory.SCALAR, BigDecimal("1000000.0")),
        Unit("Crore", listOf("crore", "crores"), UnitCategory.SCALAR, BigDecimal("10000000.0")),
        Unit("Billion", listOf("billion", "billions"), UnitCategory.SCALAR, BigDecimal("1e9")),
        Unit("Trillion", listOf("trillion", "trillions"), UnitCategory.SCALAR, BigDecimal("1e12")),
        Unit("Quadrillion", listOf("quadrillion"), UnitCategory.SCALAR, BigDecimal("1e15")),
        Unit("Quintillion", listOf("quintillion"), UnitCategory.SCALAR, BigDecimal("1e18")),

        // --- CSS/SCREEN --- (Uses LENGTH category with dynamic factors)
        Unit("Pixel", listOf("px", "pixel", "pixels"), UnitCategory.LENGTH, BigDecimal.ONE,
            customToBase = { v, vars ->
                val ppiValue = vars["ppi"]?.value ?: BigDecimal("96.0")
                val ppi = if (ppiValue.compareTo(BigDecimal.ZERO) > 0) ppiValue else BigDecimal("96.0")
                v.multiply(BigDecimal.ONE.divide(ppi, JavaMathContext.DECIMAL128)).multiply(BigDecimal("0.0254"))
            },
            customFromBase = { v, vars ->
                val ppiValue = vars["ppi"]?.value ?: BigDecimal("96.0")
                val ppi = if (ppiValue.compareTo(BigDecimal.ZERO) > 0) ppiValue else BigDecimal("96.0")
                v.divide(BigDecimal("0.0254").divide(ppi, JavaMathContext.DECIMAL128), JavaMathContext.DECIMAL128)
            }
        ),
        Unit("Point", listOf("pt"), UnitCategory.LENGTH, BigDecimal.ONE.divide(BigDecimal("72.0"), JavaMathContext.DECIMAL128).multiply(BigDecimal("0.0254"))),
        Unit("Em", listOf("em"), UnitCategory.LENGTH, BigDecimal.ONE,
            customToBase = { v, vars ->
                val emResult = vars["em"]
                val ppiValue = vars["ppi"]?.value ?: BigDecimal("96.0")
                val ppi = if (ppiValue.compareTo(BigDecimal.ZERO) > 0) ppiValue else BigDecimal("96.0")
                if (emResult != null) {
                    if (emResult.unit != null) {
                        // em was assigned with a unit (e.g., em = 21px), so its value
                        // is already stored in base meters. 1 em = emResult.value meters.
                        val emValue = emResult.value ?: BigDecimal.ZERO
                        if (emValue.compareTo(BigDecimal.ZERO) > 0) {
                            v.multiply(emValue)
                        } else {
                            // Fall back to default 16px at ppi
                            v.multiply(BigDecimal("16.0")).multiply(BigDecimal.ONE.divide(ppi, JavaMathContext.DECIMAL128)).multiply(BigDecimal("0.0254"))
                        }
                    } else {
                        // em was assigned as a plain number (e.g., em = 20),
                        // treated as a pixel count at the current ppi.
                        val emValue = emResult.value ?: BigDecimal.ZERO
                        if (emValue.compareTo(BigDecimal.ZERO) > 0) {
                            v.multiply(emValue).multiply(BigDecimal.ONE.divide(ppi, JavaMathContext.DECIMAL128)).multiply(BigDecimal("0.0254"))
                        } else {
                            // Fall back to default 16px at ppi
                            v.multiply(BigDecimal("16.0")).multiply(BigDecimal.ONE.divide(ppi, JavaMathContext.DECIMAL128)).multiply(BigDecimal("0.0254"))
                        }
                    }
                } else {
                    // Default: 1em = 16px at 96ppi
                    v.multiply(BigDecimal("16.0")).multiply(BigDecimal.ONE.divide(ppi, JavaMathContext.DECIMAL128)).multiply(BigDecimal("0.0254"))
                }
            },
            customFromBase = { v, vars ->
                val emResult = vars["em"]
                val ppiValue = vars["ppi"]?.value ?: BigDecimal("96.0")
                val ppi = if (ppiValue.compareTo(BigDecimal.ZERO) > 0) ppiValue else BigDecimal("96.0")
                if (emResult != null) {
                    if (emResult.unit != null) {
                        val emValue = emResult.value ?: BigDecimal.ONE
                        if (emValue.compareTo(BigDecimal.ZERO) > 0) {
                            v.divide(emValue, JavaMathContext.DECIMAL128)
                        } else {
                            // Fall back to default 16px at ppi
                            v.divide(BigDecimal("16.0").multiply(BigDecimal.ONE.divide(ppi, JavaMathContext.DECIMAL128)).multiply(BigDecimal("0.0254")), JavaMathContext.DECIMAL128)
                        }
                    } else {
                        val emValue = emResult.value ?: BigDecimal("16.0")
                        if (emValue.compareTo(BigDecimal.ZERO) > 0) {
                            v.divide(emValue.multiply(BigDecimal.ONE.divide(ppi, JavaMathContext.DECIMAL128)).multiply(BigDecimal("0.0254")), JavaMathContext.DECIMAL128)
                        } else {
                            // Fall back to default 16px at ppi
                            v.divide(BigDecimal("16.0").multiply(BigDecimal.ONE.divide(ppi, JavaMathContext.DECIMAL128)).multiply(BigDecimal("0.0254")), JavaMathContext.DECIMAL128)
                        }
                    }
                } else {
                    v.divide(BigDecimal("16.0").multiply(BigDecimal.ONE.divide(ppi, JavaMathContext.DECIMAL128)).multiply(BigDecimal("0.0254")), JavaMathContext.DECIMAL128)
                }
            }
        )
    )

    private val EXCLUDED_DATA_SYMBOLS = listOf("B", "kB", "KB", "MB", "GB", "TB", "PB", "EB", "KiB", "MiB", "GiB", "TiB", "PiB", "EiB")

    private fun isLetterOrDegree(symbol: String): Boolean {
        return symbol.all { it.isLetter() } || symbol == "°"
    }

    /**
     * Set of all unit symbols/aliases that users must NOT reassign as variables
     * (except for the special dynamic units: `ppi` and `em`).
     */
    val RESERVED_UNIT_SYMBOLS: Set<String> by lazy {
        val result = mutableSetOf<String>()
        for (unit in UNITS) {
            for (symbol in unit.symbols) {
                // Only identifier-safe, multi-char symbols can meaningfully clash with variable names.
                // Single-char symbols (like 'g', 's', 'W') are too common as variable names to block.
                if (symbol.length >= 2 && symbol.all { it.isLetterOrDigit() || it == '_' }) {
                    result.add(symbol)
                    if (isLetterOrDegree(symbol) && symbol !in EXCLUDED_DATA_SYMBOLS) {
                        result.add(symbol.lowercase())
                    }
                }
            }
        }
        // ppi and em are intentionally allowed to be overridden
        result -= setOf("ppi", "em")
        result
    }

    private val symbolMap: Map<String, Unit> by lazy {
        val map = mutableMapOf<String, Unit>()
        for (unit in UNITS) {
            for (symbol in unit.symbols) {
                map[symbol] = unit
                if (isLetterOrDegree(symbol) && symbol !in EXCLUDED_DATA_SYMBOLS) {
                    val lower = symbol.lowercase()
                    if (!map.containsKey(lower)) {
                        map[lower] = unit
                    }
                }
            }
        }
        map
    }

    fun findUnit(token: String): Unit? {
        return symbolMap[token] ?: symbolMap[token.lowercase()]
    }

    fun toBase(value: BigDecimal, unit: Unit, variables: Map<String, EvaluationResult>): BigDecimal {
        return unit.customToBase?.invoke(value, variables) ?: value.multiply(unit.factor)
    }

    fun fromBase(value: BigDecimal, unit: Unit, variables: Map<String, EvaluationResult>): BigDecimal {
        return unit.customFromBase?.invoke(value, variables) ?: try {
            value.divide(unit.factor)
        } catch (e: ArithmeticException) {
            value.divide(unit.factor, JavaMathContext.DECIMAL128)
        }
    }

    fun convert(value: BigDecimal, from: Unit, to: Unit, variables: Map<String, EvaluationResult>): BigDecimal {
        if (from.category != to.category) {
            throw EvalException("Conversion of `${from.name}` to `${to.name}` is not supported")
        }
        val baseValue = toBase(value, from, variables)
        return fromBase(baseValue, to, variables)
    }

    fun convert(value: BigDecimal, fromToken: String, toToken: String, variables: Map<String, EvaluationResult>): BigDecimal {
        val from = findUnit(fromToken) ?: throw EvalException("Unknown unit `$fromToken`")
        val to = findUnit(toToken) ?: throw EvalException("Unknown unit `$toToken`")
        return convert(value, from, to, variables)
    }

    fun deriveUnit(left: Unit?, right: Unit?, op: TokenKind): String? {
        if (left == null && right == null) return null
        if (left?.category == UnitCategory.SCALAR || right?.category == UnitCategory.SCALAR) return null

        return when (op) {
            TokenKind.STAR -> deriveByRules(left, right, MULTIPLICATION_RULES)
            TokenKind.SLASH -> deriveByRules(left, right, DIVISION_RULES)
            else -> null
        }
    }

    fun deriveUnitScale(left: Unit?, right: Unit?, op: TokenKind): BigDecimal {
        if (left == null || right == null) return BigDecimal.ONE
        if (left.category == UnitCategory.SCALAR || right.category == UnitCategory.SCALAR) return BigDecimal.ONE

        return if (op == TokenKind.STAR &&
            ((left.category == UnitCategory.AREA && right.category == UnitCategory.LENGTH) ||
                (left.category == UnitCategory.LENGTH && right.category == UnitCategory.AREA))
        ) {
            CUBIC_METER_TO_LITER
        } else if (op == TokenKind.SLASH &&
            (left.category == UnitCategory.VOLUME &&
                (right.category == UnitCategory.LENGTH || right.category == UnitCategory.AREA))
        ) {
            BigDecimal.ONE.divide(CUBIC_METER_TO_LITER)
        } else {
            BigDecimal.ONE
        }
    }

    private fun deriveByRules(left: Unit?, right: Unit?, rules: List<UnitRule>): String? {
        if (left == null || right == null) return null
        if (left.category == UnitCategory.TEMPERATURE || right.category == UnitCategory.TEMPERATURE) return null
        return rules.firstOrNull { it.left == left.category && it.right == right.category }?.result?.invoke(left, right)
    }

    internal fun deriveForMultiplication(left: Unit?, right: Unit?): String? = deriveByRules(left, right, MULTIPLICATION_RULES)

    internal fun deriveForDivision(left: Unit?, right: Unit?): String? = deriveByRules(left, right, DIVISION_RULES)

    internal fun deriveForPower(left: Unit?, exponent: Int): String? {
        if (left == null) return null
        if (exponent == 0) return "unitless"
        if (left.category == UnitCategory.TEMPERATURE) return null
        return when (exponent) {
            1 -> left.symbols.first()
            2 -> squareSymbolForFamily(left.symbols.first())
            3 -> cubeSymbolForFamily(left.symbols.first())
            else -> null
        }
    }

    internal fun deriveForRoot(left: Unit?, rootDegree: Int): String? {
        if (left == null) return null
        if (rootDegree == 2) {
            val family = left.symbols.first().removeSuffix("²")
            return if (left.category == UnitCategory.AREA) lengthSymbolForFamily(family) else null
        }
        if (rootDegree == 3) {
            val family = left.symbols.first().removeSuffix("³")
            return if (left.category == UnitCategory.VOLUME) lengthSymbolForFamily(family) else null
        }
        return null
    }

    private fun sameSymbolFamily(left: Unit, right: Unit, resolve: (String) -> String?): String? {
        return if (left.symbols.first() == right.symbols.first()) resolve(left.symbols.first()) else null
    }

    private fun sameAreaLengthFamily(area: Unit, length: Unit): String? {
        val family = area.symbols.first().removeSuffix("²")
        return if (length.category == UnitCategory.LENGTH && length.symbols.first() == family) family else null
    }

    private fun sameVolumeLengthFamily(volume: Unit, length: Unit): String? {
        val family = volume.symbols.first().removeSuffix("³")
        return if (length.category == UnitCategory.LENGTH && length.symbols.first() == family) family else null
    }

    private fun sameVolumeAreaFamily(volume: Unit, area: Unit): String? {
        val family = volume.symbols.first().removeSuffix("³")
        return if (area.category == UnitCategory.AREA && area.symbols.first().removeSuffix("²") == family) family else null
    }

    private fun squareSymbolForFamily(family: String): String? = when (family) {
        "nm" -> findUnit("nm²")?.symbols?.first()
        "µm" -> findUnit("µm²")?.symbols?.first()
        "mm" -> findUnit("mm²")?.symbols?.first()
        "cm" -> findUnit("cm²")?.symbols?.first()
        "m" -> findUnit("m²")?.symbols?.first()
        "km" -> findUnit("km²")?.symbols?.first()
        "inch" -> findUnit("in²")?.symbols?.first()
        "ft" -> findUnit("ft²")?.symbols?.first()
        "yd" -> findUnit("yd²")?.symbols?.first()
        "mi" -> findUnit("mi²")?.symbols?.first()
        else -> null
    }

    private fun lengthSymbolForFamily(family: String): String? = when (family) {
        "nm" -> findUnit("nm")?.symbols?.first()
        "µm" -> findUnit("µm")?.symbols?.first()
        "mm" -> findUnit("mm")?.symbols?.first()
        "cm" -> findUnit("cm")?.symbols?.first()
        "m" -> findUnit("m")?.symbols?.first()
        "km" -> findUnit("km")?.symbols?.first()
        "inch" -> findUnit("inch")?.symbols?.first()
        "ft" -> findUnit("ft")?.symbols?.first()
        "yd" -> findUnit("yd")?.symbols?.first()
        "mi" -> findUnit("mi")?.symbols?.first()
        else -> null
    }

    private fun cubeSymbolForFamily(family: String): String? = when (family) {
        "mm" -> findUnit("mm³")?.symbols?.first()
        "cm" -> findUnit("cm³")?.symbols?.first()
        "m" -> findUnit("m³")?.symbols?.first()
        "inch" -> findUnit("in³")?.symbols?.first()
        "ft" -> findUnit("ft³")?.symbols?.first()
        else -> null
    }

    private fun speedAndTimeToLength(speed: String, time: String): String? {
        if(speed == "mps" && time == "s") return "m"
        if(speed == "kmh" && time == "h") return "km"
        if(speed == "mph" && time == "h") return "mi"
        if(speed == "fps" && time == "s") return "ft"
        return null;
    }

    private fun lengthAndTimeToSpeed(length: String, time: String): String? {
        if(length == "m" && time == "s") return "mps"
        if(length == "km" && time == "h") return "kmh"
        if(length == "mi" && time == "h") return "mph"
        if(length == "ft" && time == "s") return "fps"
        return null;
    }

    fun isNumeralSystemSymbol(symbol: String): Boolean {
        val unit = findUnit(symbol)
        return unit != null && unit.category == UnitCategory.NUMERAL_SYSTEM
    }
}
