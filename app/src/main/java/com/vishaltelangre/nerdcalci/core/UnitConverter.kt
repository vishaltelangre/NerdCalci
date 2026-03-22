package com.vishaltelangre.nerdcalci.core

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
    val factor: Double = 1.0,
    val customToBase: ((Double, Map<String, EvaluationResult>) -> Double)? = null,
    val customFromBase: ((Double, Map<String, EvaluationResult>) -> Double)? = null
)

object UnitConverter {

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
        Unit("Nanosecond", listOf("ns", "nanosecond", "nanoseconds"), UnitCategory.TIME, 1e-9),
        Unit("Microsecond", listOf("µs", "us", "microsecond", "microseconds"), UnitCategory.TIME, 1e-6),
        Unit("Millisecond", listOf("ms", "millisecond", "milliseconds"), UnitCategory.TIME, 0.001),
        Unit("Second", listOf("s", "sec", "secs", "second", "seconds"), UnitCategory.TIME, 1.0),
        Unit("Minute", listOf("min", "mins", "minute", "minutes"), UnitCategory.TIME, 60.0),
        Unit("Hour", listOf("h", "hr", "hrs", "hour", "hours"), UnitCategory.TIME, 3600.0),
        Unit("Day", listOf("d", "day", "days"), UnitCategory.TIME, 86400.0),
        Unit("Week", listOf("wk", "wks", "week", "weeks"), UnitCategory.TIME, 604800.0),
        Unit("Month", listOf("mo", "mnth", "mnths", "month", "months"), UnitCategory.TIME, 2629746.0), // 30.436875 days
        Unit("Year", listOf("y", "yr", "yrs", "year", "years"), UnitCategory.TIME, 31556952.0), // 365.2425 days
        Unit("Lustrum", listOf("lustrum", "lustrums"), UnitCategory.TIME, 157784760.0),
        Unit("Decade", listOf("decade", "decades"), UnitCategory.TIME, 315569520.0),
        Unit("Century", listOf("century", "centuries"), UnitCategory.TIME, 3155695200.0),
        Unit("Millennium", listOf("millennium", "millennia", "millenniums"), UnitCategory.TIME, 31556952000.0),
        Unit("Decisecond", listOf("ds", "decisecond", "deciseconds"), UnitCategory.TIME, 0.1),
        Unit("Centisecond", listOf("cs", "centisecond", "centiseconds"), UnitCategory.TIME, 0.01),

        // --- LENGTH --- (Base: meter)
        Unit("Nanometer", listOf("nm", "nanometer", "nanometers"), UnitCategory.LENGTH, 1e-9),
        Unit("Micrometer", listOf("µm", "um", "micrometer", "micrometers"), UnitCategory.LENGTH, 1e-6),
        Unit("Millimeter", listOf("mm", "millimeter", "millimeters"), UnitCategory.LENGTH, 0.001),
        Unit("Centimeter", listOf("cm", "centimeter", "centimeters"), UnitCategory.LENGTH, 0.01),
        Unit("Decimeter", listOf("dm", "decimeter", "decimeters"), UnitCategory.LENGTH, 0.1),
        Unit("Meter", listOf("m", "meter", "meters"), UnitCategory.LENGTH, 1.0),
        Unit("Kilometer", listOf("km", "kms", "kilometer", "kilometers"), UnitCategory.LENGTH, 1000.0),
        // Imperial Length
        Unit("Inch", listOf("inch", "inches"), UnitCategory.LENGTH, 0.0254),
        Unit("Foot", listOf("ft", "foot", "feet"), UnitCategory.LENGTH, 0.3048),
        Unit("Yard", listOf("yd", "yard", "yards"), UnitCategory.LENGTH, 0.9144),
        Unit("Mile", listOf("mi", "mile", "miles"), UnitCategory.LENGTH, 1609.344),
        Unit("Furlong", listOf("fur", "furlong"), UnitCategory.LENGTH, 201.168),
        Unit("Fathom", listOf("ftm", "fathom"), UnitCategory.LENGTH, 1.8288),
        Unit("Nautical Mile", listOf("NM", "nmi"), UnitCategory.LENGTH, 1852.0),
        Unit("Light Year", listOf("ly"), UnitCategory.LENGTH, 9.4607304725808e15),
        Unit("Angstrom", listOf("Å", "angstrom", "angstroms"), UnitCategory.LENGTH, 1e-10),
        Unit("Picometer", listOf("pm", "picometer", "picometers"), UnitCategory.LENGTH, 1e-12),
        Unit("Astronomical Unit", listOf("au", "AU", "astronomical unit", "astronomical units"), UnitCategory.LENGTH, 149597870700.0),

        // --- AREA --- (Base: square meter)
        Unit("Square Nanometer", listOf("nm²", "nm^2", "nm2", "sqnm", "square nanometer", "square nanometers"), UnitCategory.AREA, 1e-18),
        Unit("Square Micrometer", listOf("µm²", "µm^2", "µm2", "um2", "squm", "square micrometer", "square micrometers"), UnitCategory.AREA, 1e-12),
        Unit("Square Millimeter", listOf("mm²", "mm^2", "mm2", "sqmm", "square millimeter", "square millimeters"), UnitCategory.AREA, 1e-6),
        Unit("Square Centimeter", listOf("cm²", "cm^2", "cm2", "sqcm", "square centimeter", "square centimeters"), UnitCategory.AREA, 1e-4),
        Unit("Square Meter", listOf("m²", "m^2", "m2", "sqm", "square meter", "square meters"), UnitCategory.AREA, 1.0),
        Unit("Square Kilometer", listOf("km²", "km^2", "km2", "sqkm", "square kilometer", "square kilometers"), UnitCategory.AREA, 1e6),
        Unit("Square Inch", listOf("in²", "in^2", "in2", "sqin", "square inch", "square inches"), UnitCategory.AREA, 0.00064516),
        Unit("Square Feet", listOf("ft²", "ft^2", "ft2", "sqft", "square foot", "square feet"), UnitCategory.AREA, 0.09290304),
        Unit("Square Yard", listOf("yd²", "yd^2", "yd2", "sqyd", "square yard", "square yards"), UnitCategory.AREA, 0.83612736),
        Unit("Square Mile", listOf("mi²", "mi^2", "mi2", "sqmi", "square mile", "square miles"), UnitCategory.AREA, 2589988.110336),
        Unit("Acre", listOf("ac", "acre", "acres"), UnitCategory.AREA, 4046.8564224),
        Unit("Hectare", listOf("ha", "hectare", "hectares"), UnitCategory.AREA, 10000.0),

        // --- VOLUME --- (Base: Liter)
        Unit("Milliliter", listOf("mL", "ml", "milliliter", "milliliters"), UnitCategory.VOLUME, 0.001),
        Unit("Liter", listOf("L", "l", "liter", "liters"), UnitCategory.VOLUME, 1.0),
        Unit("Kiloliter", listOf("kL", "kl", "kiloliter", "kiloliters"), UnitCategory.VOLUME, 1000.0),
        Unit("Megaliter", listOf("ML", "megaliter", "megaliters"), UnitCategory.VOLUME, 1000000.0),
        Unit("Cubic Centimeter", listOf("cm³", "cm^3", "cm3", "cc", "cubic centimeter", "cubic centimeters"), UnitCategory.VOLUME, 0.001), // 1cm³ = 1mL
        Unit("Cubic Meter", listOf("m³", "m^3", "m3", "cubic meter", "cubic meters"), UnitCategory.VOLUME, 1000.0), // 1m³ = 1000L
        Unit("Deciliter", listOf("dL", "dl", "deciliter", "deciliters"), UnitCategory.VOLUME, 0.1),
        Unit("Centiliter", listOf("cL", "cl", "centiliter", "centiliters"), UnitCategory.VOLUME, 0.01),
        Unit("Microliter", listOf("µL", "uL", "µl", "ul", "microliter", "microliters"), UnitCategory.VOLUME, 1e-6),
        Unit("Cubic Millimeter", listOf("mm³", "mm^3", "mm3", "cubic millimeter", "cubic millimeters"), UnitCategory.VOLUME, 1e-6),

        Unit("Gallon", listOf("gal", "gallon", "gallons", "US gallon", "US gallons"), UnitCategory.VOLUME, 3.785411784),
        Unit("Quart", listOf("qt", "quart", "quarts", "US quarts"), UnitCategory.VOLUME, 0.946352946),
        Unit("Pint", listOf("pint", "pints", "US pints"), UnitCategory.VOLUME, 0.473176473),
        Unit("Cup", listOf("cup", "cups", "US cups"), UnitCategory.VOLUME, 0.2365882365),
        Unit("Fluid Ounce", listOf("fl oz", "floz", "fluid ounce", "fluid ounces", "US fluid ounces"), UnitCategory.VOLUME, 0.0295735295625),

        Unit("Gallon (Imperial)", listOf("gal_imp", "imperial gallon", "imperial gallons"), UnitCategory.VOLUME, 4.54609),
        Unit("Quart (Imperial)", listOf("qt_imp", "imperial quart", "imperial quarts"), UnitCategory.VOLUME, 1.1365225),
        Unit("Pint (Imperial)", listOf("pint_imp", "imperial pint", "imperial pints"), UnitCategory.VOLUME, 0.56826125),
        Unit("Fluid Ounce (Imperial)", listOf("fl_oz_imp", "imperial fluid ounce", "imperial fluid ounces"), UnitCategory.VOLUME, 0.0284130625),
        Unit("Gill (US)", listOf("gi_us", "US gill", "US gills"), UnitCategory.VOLUME, 0.11829411825),
        Unit("Gill (Imperial)", listOf("gi_imp", "imperial gill", "imperial gills"), UnitCategory.VOLUME, 0.1420653125),
        Unit("Tablespoon", listOf("tbsp", "tablespoon", "tablespoons"), UnitCategory.VOLUME, 0.01478676478125),
        Unit("Teaspoon", listOf("tsp", "teaspoon", "teaspoons"), UnitCategory.VOLUME, 0.00492892159375),
        Unit("Cubic Inch", listOf("in³", "in^3", "in3", "cubic inch", "cubic inches"), UnitCategory.VOLUME, 0.016387064),
        Unit("Cubic Feet", listOf("ft³", "ft^3", "ft3", "cuft", "cubic foot", "cubic feet"), UnitCategory.VOLUME, 28.316846592),

        // --- MASS --- (Base: Gram)
        Unit("Nanogram", listOf("ng", "nanogram", "nanograms"), UnitCategory.MASS, 1e-9),
        Unit("Microgram", listOf("mcg", "µg", "ug", "microgram", "micrograms"), UnitCategory.MASS, 1e-6),
        Unit("Milligram", listOf("mg", "milligram", "milligrams"), UnitCategory.MASS, 0.001),
        Unit("Gram", listOf("g", "gram", "grams"), UnitCategory.MASS, 1.0),
        Unit("Kilogram", listOf("kg", "kgs", "kilograms"), UnitCategory.MASS, 1000.0),
        Unit("Metric Ton", listOf("t", "tonne", "tonnes", "ton", "tons", "metric ton", "metric tons", "metric tonne", "metric tonnes"), UnitCategory.MASS, 1000000.0),
        Unit("Ounce", listOf("oz", "ounce", "ounces"), UnitCategory.MASS, 28.349523125),
        Unit("Pound", listOf("lb", "lbs", "pound", "pounds"), UnitCategory.MASS, 453.59237),
        Unit("Stone", listOf("st", "stone", "stones"), UnitCategory.MASS, 6350.29318),
        Unit("Short Ton", listOf("sh ton", "short ton", "short tons"), UnitCategory.MASS, 907184.74),
        Unit("Troy Ounce", listOf("ozt", "oz t", "troy ounce", "troy ounces"), UnitCategory.MASS, 31.1034768),
        Unit("Carat", listOf("ct", "carat", "carats"), UnitCategory.MASS, 0.2),
        Unit("Ettogram", listOf("hg", "ettogram", "ettograms"), UnitCategory.MASS, 100.0),
        Unit("Centigram", listOf("cg", "centigram", "centigrams"), UnitCategory.MASS, 0.01),
        Unit("Quintal", listOf("q", "quintal", "quintals"), UnitCategory.MASS, 100000.0),
        Unit("Pennyweight", listOf("dwt", "pennyweight"), UnitCategory.MASS, 1.55517384),
        Unit("Unified atomic mass unit", listOf("u", "amu"), UnitCategory.MASS, 1.66053906660e-24),

        // --- SPEED --- (Base: m/s)
        Unit("Meter per second", listOf("mps", "meters per second"), UnitCategory.SPEED, 1.0),
        Unit("Kilometer per hour", listOf("kmh", "kph", "kmph", "kilometers per hour"), UnitCategory.SPEED, 1.0 / 3.6),
        Unit("Miles per hour", listOf("mph", "miph", "miles per hour"), UnitCategory.SPEED, 0.44704),
        Unit("Knot", listOf("kn", "knot", "knots"), UnitCategory.SPEED, 0.514444),
        Unit("Feet per second", listOf("fps", "feet per second"), UnitCategory.SPEED, 0.3048),
        Unit("Speed of light", listOf("speed of light"), UnitCategory.SPEED, 299792458.0),

        // --- ANGLE --- (Base: Radian)
        Unit("Radian", listOf("rad", "radian", "radians"), UnitCategory.ANGLE, 1.0),
        Unit("Degree", listOf("deg", "degree", "degrees", "°"), UnitCategory.ANGLE, PI / 180.0),
        Unit("Minute of arc", listOf("arcmin", "minute of arc"), UnitCategory.ANGLE, (PI / 180.0) / 60.0),
        Unit("Second of arc", listOf("arcsec", "second of arc"), UnitCategory.ANGLE, (PI / 180.0) / 3600.0),

        // --- TEMPERATURE --- (Base: Kelvin)
        Unit("Celsius", listOf("°C", "C", "celsius", "degC", "degree celsius"), UnitCategory.TEMPERATURE,
            customToBase = { v, _ -> v + 273.15 },
            customFromBase = { v, _ -> v - 273.15 }
        ),
        Unit("Fahrenheit", listOf("°F", "F", "fahrenheit", "degF", "degree fahrenheit"), UnitCategory.TEMPERATURE,
            customToBase = { v, _ -> (v - 32.0) * 5.0 / 9.0 + 273.15 },
            customFromBase = { v, _ -> (v - 273.15) * 9.0 / 5.0 + 32.0 }
        ),
        Unit("Kelvin", listOf("K", "kelvin"), UnitCategory.TEMPERATURE, 1.0),
        Unit("Reaumur", listOf("°Re", "Re", "reaumur", "Réaumur"), UnitCategory.TEMPERATURE,
            customToBase = { v, _ -> v / 0.8 + 273.15 },
            customFromBase = { v, _ -> (v - 273.15) * 0.8 }
        ),
        Unit("Rømer", listOf("°Rø", "Rø", "romer", "Rømer"), UnitCategory.TEMPERATURE,
            customToBase = { v, _ -> (v - 7.5) * 40.0 / 21.0 + 273.15 },
            customFromBase = { v, _ -> (v - 273.15) * 21.0 / 40.0 + 7.5 }
        ),
        Unit("Delisle", listOf("°De", "De", "delisle"), UnitCategory.TEMPERATURE,
            customToBase = { v, _ -> 373.15 - v * 2.0 / 3.0 },
            customFromBase = { v, _ -> (373.15 - v) * 1.5 }
        ),
        Unit("Rankine", listOf("°Ra", "Ra", "rankine"), UnitCategory.TEMPERATURE,
            customToBase = { v, _ -> v / 1.8 },
            customFromBase = { v, _ -> v * 1.8 }
        ),

        // --- FREQUENCY --- (Base: Hertz)
        Unit("Hertz", listOf("Hz", "hertz"), UnitCategory.FREQUENCY, 1.0),
        Unit("Kilohertz", listOf("kHz", "kilohertz"), UnitCategory.FREQUENCY, 1000.0),
        Unit("Megahertz", listOf("MHz", "megahertz"), UnitCategory.FREQUENCY, 1e6),
        Unit("Gigahertz", listOf("GHz", "gigahertz"), UnitCategory.FREQUENCY, 1e9),

        // --- ENERGY --- (Base: Joule)
        Unit("Joule", listOf("J", "joule", "joules"), UnitCategory.ENERGY, 1.0),
        Unit("Kilojoule", listOf("kJ", "kilojoule", "kilojoules"), UnitCategory.ENERGY, 1000.0),
        Unit("Megajoule", listOf("MJ", "megajoule", "megajoules"), UnitCategory.ENERGY, 1e6),
        Unit("Calorie", listOf("cal", "calorie", "calories"), UnitCategory.ENERGY, 4.184),
        Unit("Kilocalorie", listOf("kCal", "kcal", "kilocalorie", "kilocalories"), UnitCategory.ENERGY, 4184.0),
        Unit("Watt hour", listOf("Wh", "watt hour", "watt hours"), UnitCategory.ENERGY, 3600.0),
        Unit("Kilowatt hour", listOf("kWh", "kilowatt hour", "kilowatt hours"), UnitCategory.ENERGY, 3.6e6),
        Unit("Electron volt", listOf("eV", "electronvolt", "electron volts"), UnitCategory.ENERGY, 1.602176634e-19),
        Unit("Foot pound-force", listOf("ft lbf", "ft_lbf", "foot_pound"), UnitCategory.ENERGY, 1.3558179483314),
        Unit("British thermal unit", listOf("BTU", "btu"), UnitCategory.ENERGY, 1055.05585262),

        // --- POWER --- (Base: Watt)
        Unit("Watt", listOf("W", "watt", "watts"), UnitCategory.POWER, 1.0),
        Unit("Milliwatt", listOf("mW", "milliwatt", "milliwatts"), UnitCategory.POWER, 0.001),
        Unit("Kilowatt", listOf("kW", "kilowatt", "kilowatts"), UnitCategory.POWER, 1000.0),
        Unit("Megawatt", listOf("MW", "megawatt", "megawatts"), UnitCategory.POWER, 1e6),
        Unit("Horsepower", listOf("hp", "horsepower", "horsepowers"), UnitCategory.POWER, 745.7),
        Unit("Gigawatt", listOf("GW", "gigawatt", "gigawatts"), UnitCategory.POWER, 1e9),

        // --- DATA --- (Base: Byte)
        Unit("Bit", listOf("bit", "bits", "b"), UnitCategory.DATA, 0.125),
        Unit("Nibble", listOf("nibble", "nibbles"), UnitCategory.DATA, 0.5),
        Unit("Byte", listOf("B", "byte", "bytes"), UnitCategory.DATA, 1.0),
        Unit("Kilobyte", listOf("kB", "KB", "kilobyte", "kilobytes"), UnitCategory.DATA, 1000.0),
        Unit("Megabyte", listOf("MB", "megabyte", "megabytes"), UnitCategory.DATA, 1000000.0),
        Unit("Gigabyte", listOf("GB", "gigabyte", "gigabytes"), UnitCategory.DATA, 1000000000.0),
        Unit("Terabyte", listOf("TB", "terabyte", "terabytes"), UnitCategory.DATA, 1e12),
        Unit("Kibibyte", listOf("KiB", "kibibyte", "kibibytes"), UnitCategory.DATA, 1024.0),
        Unit("Mebibyte", listOf("MiB", "mebibyte", "mebibytes"), UnitCategory.DATA, 1048576.0),
        Unit("Gibibyte", listOf("GiB", "gibibyte", "gibibytes"), UnitCategory.DATA, 1073741824.0),

        Unit("Tebibyte", listOf("TiB", "tebibyte", "tebibytes"), UnitCategory.DATA, 1099511627776.0),
        Unit("Pebibyte", listOf("PiB", "pebibyte", "pebibytes"), UnitCategory.DATA, 1125899906842624.0),
        Unit("Exbibyte", listOf("EiB", "exbibyte", "exbibytes"), UnitCategory.DATA, 1.1529215046e18),
        Unit("Petabyte", listOf("PB", "petabyte", "petabytes"), UnitCategory.DATA, 1e15),
        Unit("Exabyte", listOf("EB", "exabyte", "exabytes"), UnitCategory.DATA, 1e18),

        Unit("Kibibit", listOf("Kibit", "kibibit"), UnitCategory.DATA, 128.0),
        Unit("Mebibit", listOf("Mibit", "mebibit"), UnitCategory.DATA, 131072.0),
        Unit("Gibibit", listOf("Gibit", "gibibit"), UnitCategory.DATA, 1.34217728e8),
        Unit("Tebibit", listOf("Tibit", "tebibit"), UnitCategory.DATA, 1.37438953472e11),
        Unit("Pebibit", listOf("Pibit", "pebibit"), UnitCategory.DATA, 1.40737488355e14),
        Unit("Exbibit", listOf("Eibit", "exbibit"), UnitCategory.DATA, 1.44115188075e17),

        Unit("Kilobit", listOf("kb", "kilobit"), UnitCategory.DATA, 125.0),
        Unit("Megabit", listOf("Mb", "megabit"), UnitCategory.DATA, 125000.0),
        Unit("Gigabit", listOf("Gb", "gigabit"), UnitCategory.DATA, 1.25e8),
        Unit("Terabit", listOf("Tb", "terabit"), UnitCategory.DATA, 1.25e11),
        Unit("Petabit", listOf("Pb", "petabit"), UnitCategory.DATA, 1.25e14),
        Unit("Exabit", listOf("Eb", "exabit"), UnitCategory.DATA, 1.25e17),

        // --- FORCE --- (Base: Newton)
        Unit("Newton", listOf("N", "newton", "newtons"), UnitCategory.FORCE, 1.0),
        Unit("Kilogram-force", listOf("kgf", "kg_f"), UnitCategory.FORCE, 9.80665),
        Unit("Pound-force", listOf("lbf", "lb_f"), UnitCategory.FORCE, 4.448222),
        Unit("Dyne", listOf("dyn", "dyne"), UnitCategory.FORCE, 1e-5),
        Unit("Poundal", listOf("pdl"), UnitCategory.FORCE, 0.138255),

        // --- FUEL CONSUMPTION --- (Base: Liters per 100 km)
        Unit("Liters per 100 km", listOf("l100km", "L100km", "liters per 100km", "liters per 100 km"), UnitCategory.FUEL_CONSUMPTION, 1.0),
        Unit("Kilometers per liter", listOf("kmpl", "kpl", "kilometers per liter"), UnitCategory.FUEL_CONSUMPTION,
            customToBase = { v, _ -> if (v > 0) 100.0 / v else 0.0 },
            customFromBase = { v, _ -> if (v > 0) 100.0 / v else 0.0 }
        ),
        Unit("Miles per Gallon (US)", listOf("mpg", "mpg_us"), UnitCategory.FUEL_CONSUMPTION,
            customToBase = { v, _ -> if (v > 0) 235.214583 / v else 0.0 },
            customFromBase = { v, _ -> if (v > 0) 235.214583 / v else 0.0 }
        ),
        Unit("Miles per Gallon (Imperial)", listOf("mpg_imp", "mpg_uk"), UnitCategory.FUEL_CONSUMPTION,
            customToBase = { v, _ -> if (v > 0) 282.480936 / v else 0.0 },
            customFromBase = { v, _ -> if (v > 0) 282.480936 / v else 0.0 }
        ),

        // --- PRESSURE --- (Base: Pascal)
        Unit("Pascal", listOf("Pa", "pascal"), UnitCategory.PRESSURE, 1.0),
        Unit("Kilopascal", listOf("kPa", "kilopascal"), UnitCategory.PRESSURE, 1000.0),
        Unit("Megapascal", listOf("MPa", "megapascal"), UnitCategory.PRESSURE, 1e6),
        Unit("Gigapascal", listOf("GPa", "gigapascal"), UnitCategory.PRESSURE, 1e9),
        Unit("Hectopascal", listOf("hPa", "hectopascal"), UnitCategory.PRESSURE, 100.0),
        Unit("Bar", listOf("bar", "bars"), UnitCategory.PRESSURE, 100000.0),
        Unit("Millibar", listOf("mbar", "millibar"), UnitCategory.PRESSURE, 100.0),
        Unit("Atmosphere", listOf("atm", "atmosphere"), UnitCategory.PRESSURE, 101325.0),
        Unit("Psi", listOf("psi", "pound per square inch"), UnitCategory.PRESSURE, 6894.757),
        Unit("Ksi", listOf("ksi"), UnitCategory.PRESSURE, 6894757.0),
        Unit("Torr", listOf("torr", "mmHg"), UnitCategory.PRESSURE, 133.3224),
        Unit("Inches of Mercury", listOf("inHg"), UnitCategory.PRESSURE, 3386.388),

        // --- NUMERAL SYSTEM ---
        Unit("Decimal", listOf("dec", "decimal"), UnitCategory.NUMERAL_SYSTEM, 10.0),
        Unit("Hexadecimal", listOf("hex", "hexadecimal"), UnitCategory.NUMERAL_SYSTEM, 16.0),
        Unit("Octal", listOf("oct", "octal"), UnitCategory.NUMERAL_SYSTEM, 8.0),
        Unit("Binary", listOf("bin", "binary"), UnitCategory.NUMERAL_SYSTEM, 2.0),

        // --- SCALAR MULTIPLIERS ---
        Unit("Hundred", listOf("hundred", "hundreds"), UnitCategory.SCALAR, 100.0),
        Unit("Thousand", listOf("thousand", "thousands"), UnitCategory.SCALAR, 1000.0),
        Unit("Lakh", listOf("lakh", "lakhs"), UnitCategory.SCALAR, 100000.0),
        Unit("Million", listOf("million", "millions"), UnitCategory.SCALAR, 1000000.0),
        Unit("Crore", listOf("crore", "crores"), UnitCategory.SCALAR, 10000000.0),
        Unit("Billion", listOf("billion", "billions"), UnitCategory.SCALAR, 1e9),
        Unit("Trillion", listOf("trillion", "trillions"), UnitCategory.SCALAR, 1e12),

        // --- CSS/SCREEN --- (Uses LENGTH category with dynamic factors)
        Unit("Pixel", listOf("px", "pixel", "pixels"), UnitCategory.LENGTH,
            customToBase = { v, vars ->
                val ppi = vars["ppi"]?.value ?: 96.0
                v * (1.0 / ppi) * 0.0254
            },
            customFromBase = { v, vars ->
                val ppi = vars["ppi"]?.value ?: 96.0
                v / (0.0254 / ppi)
            }
        ),
        Unit("Point", listOf("pt"), UnitCategory.LENGTH, (1.0 / 72.0) * 0.0254),
        Unit("Em", listOf("em"), UnitCategory.LENGTH,
            customToBase = { v, vars ->
                val emResult = vars["em"]
                val ppi = vars["ppi"]?.value ?: 96.0
                if (emResult != null) {
                    if (emResult.unit != null) {
                        // em was assigned with a unit (e.g., em = 21px), so its value
                        // is already stored in base meters. 1 em = emResult.value meters.
                        v * (emResult.value ?: 0.0)
                    } else {
                        // em was assigned as a plain number (e.g., em = 20),
                        // treated as a pixel count at the current ppi.
                        v * (emResult.value ?: 0.0) * (1.0 / ppi) * 0.0254
                    }
                } else {
                    // Default: 1em = 16px at 96ppi
                    v * 16.0 * (1.0 / ppi) * 0.0254
                }
            },
            customFromBase = { v, vars ->
                val emResult = vars["em"]
                val ppi = vars["ppi"]?.value ?: 96.0
                if (emResult != null) {
                    if (emResult.unit != null) {
                        v / (emResult.value ?: 1.0)
                    } else {
                        v / ((emResult.value ?: 16.0) * (1.0 / ppi) * 0.0254)
                    }
                } else {
                    v / (16.0 * (1.0 / ppi) * 0.0254)
                }
            }
        )
    )

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
                    result.add(symbol.lowercase())
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
                val excludedDataSymbols = listOf("B", "kB", "KB", "MB", "GB", "TB", "PB", "EB", "KiB", "MiB", "GiB", "TiB", "PiB", "EiB")
                val isLetterOrDegree = symbol.all { it.isLetter() } || symbol == "°"
                if (isLetterOrDegree && symbol !in excludedDataSymbols) {
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

    fun toBase(value: Double, unit: Unit, variables: Map<String, EvaluationResult>): Double {
        return unit.customToBase?.invoke(value, variables) ?: (value * unit.factor)
    }

    fun fromBase(value: Double, unit: Unit, variables: Map<String, EvaluationResult>): Double {
        return unit.customFromBase?.invoke(value, variables) ?: (value / unit.factor)
    }

    fun convert(value: Double, from: Unit, to: Unit, variables: Map<String, EvaluationResult>): Double {
        if (from.category != to.category) {
            throw EvalException("Cannot convert `${from.name}` to `${to.name}`: dimension mismatch")
        }
        val baseValue = toBase(value, from, variables)
        return fromBase(baseValue, to, variables)
    }

    fun convert(value: Double, fromToken: String, toToken: String, variables: Map<String, EvaluationResult>): Double {
        val from = findUnit(fromToken) ?: throw EvalException("Unknown unit `$fromToken`")
        val to = findUnit(toToken) ?: throw EvalException("Unknown unit `$toToken`")
        return convert(value, from, to, variables)
    }
}
