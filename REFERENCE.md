- [1. Basic math and operators](#1-basic-math-and-operators)
  - [Addition](#addition)
  - [Subtraction](#subtraction)
  - [Multiplication](#multiplication)
  - [Division](#division)
  - [Modulo](#modulo)
  - [Exponentiation](#exponentiation)
  - [Grouping](#grouping)
- [2. Variables and state](#2-variables-and-state)
  - [Assignment](#assignment)
  - [Compound assignment](#compound-assignment)
  - [Increment and decrement](#increment-and-decrement)
- [3. Percentages](#3-percentages)
  - [Percentage of](#percentage-of)
  - [Percentage off (discounting)](#percentage-off-discounting)
  - [Addition / subtraction](#addition--subtraction)
- [4. Built-in mathematical functions](#4-built-in-mathematical-functions)
  - [Power \& Roots](#power--roots)
  - [Rounding \& Signs](#rounding--signs)
  - [Logarithmic \& Exponential](#logarithmic--exponential)
  - [Trigonometry](#trigonometry)
- [5. Built-in constants](#5-built-in-constants)
- [6. Comments](#6-comments)
- [7. Dynamic variables](#7-dynamic-variables)
  - [Sum / Total](#sum--total)
  - [Average](#average)
  - [Reference to previous line result](#reference-to-previous-line-result)
  - [Reference to current line number](#reference-to-current-line-number)
- [8. User-defined functions](#8-user-defined-functions)
  - [Basic definition](#basic-definition)
  - [Multiple statements](#multiple-statements)
  - [Scope isolation](#scope-isolation)
  - [Recursion](#recursion)
- [9. Cross-file references](#9-cross-file-references)
  - [Referencing a file](#referencing-a-file)
  - [Accessing variables from other files](#accessing-variables-from-other-files)
  - [Accessing functions from other files](#accessing-functions-from-other-files)
  - [Circular references](#circular-references)
- [10. Unit conversions](#10-unit-conversions)
  - [Creating quantities](#creating-quantities)
  - [Dimension-safe arithmetic](#dimension-safe-arithmetic)
  - [Conversion operations](#conversion-operations)
  - [The `convert()` function](#the-convert-function)
  - [Quantities inside Trigonometry](#quantities-inside-trigonometry)
  - [Time](#time)
  - [Length](#length)
  - [Area](#area)
  - [Volume](#volume)
  - [Mass](#mass)
  - [Speed](#speed)
  - [Angle](#angle)
  - [Temperature](#temperature)
  - [Frequency](#frequency)
  - [Energy](#energy)
  - [Power](#power)
  - [Data](#data)
  - [Force](#force)
  - [Fuel consumption](#fuel-consumption)
  - [Pressure](#pressure)
- [11. Numeral systems](#11-numeral-systems)
  - [Radix base conversions](#radix-base-conversions)

## 1. Basic math and operators

All standard arithmetic operations are supported and follow the standard mathematical order of operations.

### Addition

Adds two numbers.

```text
2 + 3       # evaluates to 5
```

### Subtraction

Subtracts right from left.

```text
10 - 4      # evaluates to 6
```

### Multiplication

Multiplies two numbers.

```text
5 * 4       # evaluates to 20
5 × 4       # evaluates to 20
```

### Division

Divides left by right.

```text
10 / 2      # evaluates to 5
10 ÷ 2      # evaluates to 5
```

### Modulo

Returns the remainder of division.

```text
10 % 3      # evaluates to 1
```

### Exponentiation

Raises left to the power of right.

```text
2 ^ 3       # evaluates to 8
2 ^ 3 ^ 4   # evaluates to 2 ^ (3 ^ 4) = 2 ^ 81
```

### Grouping

Overrides standard precedence.

```text
(2 + 3) * 4 # evaluates to 20
```

## 2. Variables and state

NerdCalci maintains variable state seamlessly across multiple lines. You can assign a value on one line and use it in subsequent lines.

### Assignment

Use the `=` operator to create or update a variable. Variable names must start with a letter or underscore, and can contain numbers (`price`, `tax_rate`, `taxRate2`).

```text
price = 100
tax = 15
total = price + tax     # evaluates to 115
```

### Compound assignment

Modify an existing variable in-place using compound operators: `+=`, `-=`, `*=`, `×=`, `/=`, `÷=`, `%=`.

```text
score = 10
score += 5              # score is now 15
score *= 2              # score is now 30
```

### Increment and decrement

Quickly add or subtract 1 from an existing variable using `++` and `--`.

```text
count = 5
count++                 # count is now 6
count--                 # count is now 5
```

## 3. Percentages

### Percentage of

Calculate a slice of a number using `% of`.

```text
20% of 500  # evaluates to 100
```

### Percentage off (discounting)

Subtract a percentage from a base number using `% off`.

```text
15% off 1000 # evaluates to 850
```

### Addition / subtraction

When adding or subtracting a bare percentage to/from a number, it applies to that specific number.

```text
100 + 20%   # evaluates to 120 (adds 20% of 100)
100 - 15%   # evaluates to 85  (subtracts 15% of 100)
```

_(**Note**: Bare percentages isolated from numbers evaluate to their decimal equivalent, e.g., `20%` alone is `0.20`)_

## 4. Built-in mathematical functions

NerdCalci includes a wide array of built-in math functions.

### Power & Roots

| Function    | Example     | Result | Description |
| ----------- | ----------- | ------ | ----------- |
| `sqrt(x)`   | `sqrt(16)`  | 4      | Square root |
| `cbrt(x)`   | `cbrt(27)`  | 3      | Cube root   |
| `pow(x, y)` | `pow(2, 8)` | 256    | Power       |

### Rounding & Signs

| Function    | Example       | Result | Description                    |
| ----------- | ------------- | ------ | ------------------------------ |
| `abs(x)`    | `abs(-42)`    | 42     | Absolute value                 |
| `floor(x)`  | `floor(3.7)`  | 3      | Floor                          |
| `ceil(x)`   | `ceil(3.2)`   | 4      | Ceiling                        |
| `signum(x)` | `signum(-42)` | -1     | Sign of a number (1, -1, or 0) |

### Logarithmic & Exponential

| Function   | Example      | Result      | Description                  |
| ---------- | ------------ | ----------- | ---------------------------- |
| `log(x)`   | `log(E)`     | 1           | Natural logarithm            |
| `log10(x)` | `log10(100)` | 2           | Base 10 logarithm            |
| `log2(x)`  | `log2(8)`    | 3           | Base 2 logarithm             |
| `log1p(x)` | `log1p(x)`   | `ln(1 + x)` | Natural logarithm of 1 + x   |
| `exp(x)`   | `exp(1)`     | 2.72        | Exponential function         |
| `expm1(x)` | `expm1(x)`   | `e^x - 1`   | Exponential function minus 1 |

### Trigonometry

Trigonometric functions expect angles in **radians** by default, but also natively support explicit angle quantities (e.g., `sin(90°)`, `cos(45 deg)`).

| Function  | Example     | Result | Description        |
| --------- | ----------- | ------ | ------------------ |
| `sin(x)`  | `sin(PI/2)` | 1      | Sine               |
| `cos(x)`  | `cos(0)`    | 1      | Cosine             |
| `tan(x)`  | `tan(PI/4)` | 1      | Tangent            |
| `asin(x)` | `asin(1)`   | 1.57   | Arc sine           |
| `acos(x)` | `acos(1)`   | 0      | Arc cosine         |
| `atan(x)` | `atan(1)`   | 0.79   | Arc tangent        |
| `sinh(x)` | `sinh(1)`   | 1.18   | Hyperbolic sine    |
| `cosh(x)` | `cosh(1)`   | 1.54   | Hyperbolic cosine  |
| `tanh(x)` | `tanh(1)`   | 0.76   | Hyperbolic tangent |

## 5. Built-in constants

NerdCalci provides common mathematical constants.

| Constant | Value | Description                                                     |
| -------- | ----- | --------------------------------------------------------------- |
| `PI`     | 3.14  | The ratio of a circle's circumference to its diameter, i.e. `π` |
| `E`      | 2.72  | Euler's number, i.e. `e`                                        |

## 6. Comments

You can add comments to your calculations using the `#` symbol.
Everything after the `#` on that line is ignored.

```text
# Monthly expenses

rent = 1200      # Base rent
utilities = 150
rent + utilities # Total housing cost
```

## 7. Dynamic variables

### Sum / Total

Use `sum` or `total` to get the sum of all line results above, up to the nearest blank/comment/error line.

```text
groceries = 45.50
utilities = 120
rent = 950
total               # evaluates to 1115.50
```

Blank/comment/error lines create blocks — `sum`/`total` only sums within the current block:

```text
a = 10
b = 20
total               # evaluates to 30

c = 5
total               # evaluates to 5
```

Use `sum` or `total` inside expressions:

```text
item1 = 25
item2 = 75
tax = sum * 0.10    # evaluates to 10
```

Assigning to `total` or `sum` overrides the aggregate meaning from that point onward:

```text
a = 1
b = 2
total               # evaluates to 3

total = 4
total / 2           # evaluates to 2
total               # evaluates to 4
# i.e. after assigning, total no longer aggregates
```

### Average

Use `avg` or `average` to get the mathematical average of all line results above, up to the nearest blank/comment/error line.

```text
jan = 100
feb = 200
mar = 300
average             # evaluates to 200
```

Blank/comment/error lines create blocks — `avg`/`average` only averages within the current block:

```text
a = 10
b = 20
avg                 # evaluates to 15

c = 5
avg                 # evaluates to 5
```

Use `avg` or `average` inside expressions:

```text
item1 = 25
item2 = 75
half_avg = avg / 2  # evaluates to 25
```

Assigning to `avg` or `average` overrides the aggregate meaning from that point onward:

```text
a = 10
b = 20
avg                 # evaluates to 15

avg = 100
avg / 2             # evaluates to 50
avg                 # evaluates to 100
# i.e. after assigning, avg no longer aggregates
```

### Reference to previous line result

Use `last`, `prev`, `previous`, `above`, or `_` to reference the result of the immediately preceding line.

```text
10 + 5
last * 2            # evaluates to 30
_ / 3               # evaluates to 10
```

If the immediately preceding line is empty, a comment, or resulted in an error, these keywords evaluate to `0`.

```text
100
# some notes

prev + 5            # evaluates to 5

10
{                   # evaluates to Err
last                # evaluates to 0
```

These keywords are **strictly reserved** and cannot be reassigned.

### Reference to current line number

Use `lineno`, `linenumber`, or `currentLineNumber` to reference the current line number.

```text
lineno              # evaluates to 1 on the first line
linenumber          # evaluates to 2 on the second line
currentLineNumber   # evaluates to 3 on the third line
```

These keywords are **strictly reserved** and cannot be reassigned.

## 8. User-defined functions

You can define your own functions to reuse complex formulas.

### Basic definition

Define a function using the syntax `name(arg1, arg2, ...) = expression`.

```text
sq(x) = x ^ 2
sq(5)               # evaluates to 25

area(r) = PI * r^2
area(10)            # evaluates to 314.16
```

### Multiple statements

Functions can contain multiple expressions separated by semicolons (`;`). The result of the **last** expression is automatically returned.

```text
# Calculate total salary after tax and bonus
salary(hours) = base = hours * 50; bonus = base * 0.1; tax = base * 0.2; base + bonus - tax

salary(160)         # evaluates to 7200
```

### Scope isolation

Variables defined inside a function (like `base`, `bonus`, `tax` above) are **local** to that function. They do not exist outside and will not overwrite global variables with the same name.

### Recursion

For safety, NerdCalci prevents functions from calling themselves, either directly or indirectly.

## 9. Cross-file references

You can access variables and functions from other calculation files using the `file()` function and dot notation.

### Referencing a file

Use `file("FileName")` to load the context of another file. The file name must be inside quotes. Alternatively, you can use the file name directly if it doesn't contain spaces or special characters.

```text
f = file("Summary")
```

### Accessing variables from other files

Use dot notation (`.`) to read variables from the referenced file. These variables are **read-only**, meaning you cannot modify them from another file.

For example, if you have a file named "Summary" with the following content:

```text
item1 = 10
item2 = 20
item1 + item2
```

Then in another file, you can:

```text
f = file("Summary")
f.item1             # 10
r = f.total * 1.05  # 31.50
```

### Accessing functions from other files

You can call functions defined in the referenced file just like variables, passing any required arguments.

```text
calcs = file("Calculations")
result = calcs.calculateTax(1000)
```

### Circular references

To prevent system crashes or freezes, NerdCalci strictly enforces circular dependency guards against files referring back to one another.

A **circular reference** happens when File A loads File B, while File B (or its functions) concurrently resolves references back into File A, creating a closed loop.

## 10. Unit conversions

NerdCalci supports dimensionally-safe arithmetic and conversions out of the box for over 100 units across various categories like Length, Mass, Time, Temperature, Data, and more.

### Creating quantities

Simply attach the unit name or symbol suffix to any number:

```text
10km
5kg
3 hr
32°
```

### Dimension-safe arithmetic

You can mix units in operators, NerdCalci will automatically normalize and preserve dimensions securely using the left-hand side unit for the result:

```text
10km + 500m                        # 10.5 km
35 °C + 10 degF                    # 105 °F
35 °C + 10 degF in degC            # 40.56 °C

# Chained arithmetic with mixed scaling
10km + 500m + 200cm                # 10502 m

# Unit arithmetic
(12 months in days + 48h) in years # 1.01 y

# Units with variables
dist = 10km
dist + 500m                        # 10.5 km
```

### Conversion operations

Use standard connectors `to`, `in`, or `as` to scale values securely:

```text
10 km to m          # 10000 m
5 kg in g           # 5000 g
100 °C as °F        # 212 °F

# Combination with Large Numeral Multipliers
5 million m to km   # 5000 km
1.5 lakh g in kg    # 150 kg
```

### The `convert()` function

Alternatively, you can use the built-in `convert()` function for programmatic conversions.

**Syntax**: `convert(value, "fromUnit", "toUnit")`

The second and third arguments MUST be quoted strings.

```text
convert(10, "km", "m")       # 10000 m
convert(100, "°C", "°F")     # 212 °F
```

### Quantities inside Trigonometry

You can pass quantities with explicit angle units directly into trigonometric functions:

```text
sin(90)        # 0.89 (defaults to radians)
sin(90°)       # 1.0
sin(30 deg)    # 0.5
cos(60 degree) # 0.5
```

### Time

| Unit        | Symbols (Aliases)                         | Example         |
| :---------- | :---------------------------------------- | :-------------- |
| Nanosecond  | `ns`, `nanosecond`, `nanoseconds`         | `10 ns`         |
| Microsecond | `µs`, `us`, `microsecond`, `microseconds` | `10 µs`         |
| Millisecond | `ms`, `millisecond`, `milliseconds`       | `10 ms`         |
| Second      | `s`, `sec`, `secs`, `second`, `seconds`   | `10 s`          |
| Minute      | `min`, `mins`, `minute`, `minutes`        | `10 min`        |
| Hour        | `h`, `hr`, `hrs`, `hour`, `hours`         | `10 h`          |
| Day         | `d`, `day`, `days`                        | `10 d`          |
| Week        | `wk`, `wks`, `week`, `weeks`              | `10 wk`         |
| Month       | `mo`, `mnth`, `mnths`, `month`, `months`  | `10 mo`         |
| Year        | `y`, `yr`, `yrs`, `year`, `years`         | `10 y`          |
| Lustrum     | `lustrum`, `lustrums`                     | `10 lustrum`    |
| Decade      | `decade`, `decades`                       | `10 decade`     |
| Century     | `century`, `centuries`                    | `10 century`    |
| Millennium  | `millennium`, `millennia`, `millenniums`  | `10 millennium` |
| Decisecond  | `ds`, `decisecond`, `deciseconds`         | `10 ds`         |
| Centisecond | `cs`, `centisecond`, `centiseconds`       | `10 cs`         |

### Length

| Unit              | Symbols (Aliases)                                     | Example   |
| :---------------- | :---------------------------------------------------- | :-------- |
| Nanometer         | `nm`, `nanometer`, `nanometers`                       | `10 nm`   |
| Micrometer        | `µm`, `um`, `micrometer`, `micrometers`               | `10 µm`   |
| Millimeter        | `mm`, `millimeter`, `millimeters`                     | `10 mm`   |
| Centimeter        | `cm`, `centimeter`, `centimeters`                     | `10 cm`   |
| Decimeter         | `dm`, `decimeter`, `decimeters`                       | `10 dm`   |
| Meter             | `m`, `meter`, `meters`                                | `10 m`    |
| Kilometer         | `km`, `kms`, `kilometer`, `kilometers`                | `10 km`   |
| Inch              | `inch`, `inches`                                      | `10 inch` |
| Foot              | `ft`, `foot`, `feet`                                  | `10 ft`   |
| Yard              | `yd`, `yard`, `yards`                                 | `10 yd`   |
| Mile              | `mi`, `mile`, `miles`                                 | `10 mi`   |
| Furlong           | `fur`, `furlong`                                      | `10 fur`  |
| Fathom            | `ftm`, `fathom`                                       | `10 ftm`  |
| Nautical Mile     | `NM`, `nmi`                                           | `10 NM`   |
| Light Year        | `ly`                                                  | `10 ly`   |
| Angstrom          | `Å`, `angstrom`, `angstroms`                          | `10 Å`    |
| Picometer         | `pm`, `picometer`, `picometers`                       | `10 pm`   |
| Astronomical Unit | `au`, `AU`, `astronomical unit`, `astronomical units` | `10 au`   |

### Area

| Unit              | Symbols (Aliases)                                                              | Example  |
| :---------------- | :----------------------------------------------------------------------------- | :------- |
| Square Nanometer  | `nm²`, `nm^2`, `nm2`, `sqnm`, `square nanometer`, `square nanometers`          | `10 nm²` |
| Square Micrometer | `µm²`, `µm^2`, `µm2`, `um2`, `squm`, `square micrometer`, `square micrometers` | `10 µm²` |
| Square Millimeter | `mm²`, `mm^2`, `mm2`, `sqmm`, `square millimeter`, `square millimeters`        | `10 mm²` |
| Square Centimeter | `cm²`, `cm^2`, `cm2`, `sqcm`, `square centimeter`, `square centimeters`        | `10 cm²` |
| Square Meter      | `m²`, `m^2`, `m2`, `sqm`, `square meter`, `square meters`                      | `10 m²`  |
| Square Kilometer  | `km²`, `km^2`, `km2`, `sqkm`, `square kilometer`, `square kilometers`          | `10 km²` |
| Square Inch       | `in²`, `in^2`, `in2`, `sqin`, `square inch`, `square inches`                   | `10 in²` |
| Square Feet       | `ft²`, `ft^2`, `ft2`, `sqft`, `square foot`, `square feet`                     | `10 ft²` |
| Square Yard       | `yd²`, `yd^2`, `yd2`, `sqyd`, `square yard`, `square yards`                    | `10 yd²` |
| Square Mile       | `mi²`, `mi^2`, `mi2`, `sqmi`, `square mile`, `square miles`                    | `10 mi²` |
| Acre              | `ac`, `acre`, `acres`                                                          | `10 ac`  |
| Hectare           | `ha`, `hectare`, `hectares`                                                    | `10 ha`  |

### Volume

| Unit                   | Symbols (Aliases)                                                   | Example        |
| :--------------------- | :------------------------------------------------------------------ | :------------- |
| Milliliter             | `mL`, `ml`, `milliliter`, `milliliters`                             | `10 mL`        |
| Liter                  | `L`, `l`, `liter`, `liters`                                         | `10 L`         |
| Kiloliter              | `kL`, `kl`, `kiloliter`, `kiloliters`                               | `10 kL`        |
| Megaliter              | `ML`, `megaliter`, `megaliters`                                     | `10 ML`        |
| Cubic Centimeter       | `cm³`, `cm^3`, `cm3`, `cc`, `cubic centimeter`, `cubic centimeters` | `10 cm³`       |
| Cubic Meter            | `m³`, `m^3`, `m3`, `cubic meter`, `cubic meters`                    | `10 m³`        |
| Deciliter              | `dL`, `dl`, `deciliter`, `deciliters`                               | `10 dL`        |
| Centiliter             | `cL`, `cl`, `centiliter`, `centiliters`                             | `10 cL`        |
| Microliter             | `µL`, `uL`, `µl`, `ul`, `microliter`, `microliters`                 | `10 µL`        |
| Cubic Millimeter       | `mm³`, `mm^3`, `mm3`, `cubic millimeter`, `cubic millimeters`       | `10 mm³`       |
| Gallon                 | `gal`, `gallon`, `gallons`, `US gallon`, `US gallons`               | `10 gal`       |
| Quart                  | `qt`, `quart`, `quarts`, `US quarts`                                | `10 qt`        |
| Pint                   | `pint`, `pints`, `US pints`                                         | `10 pint`      |
| Cup                    | `cup`, `cups`, `US cups`                                            | `10 cup`       |
| Fluid Ounce            | `fl oz`, `floz`, `fluid ounce`, `fluid ounces`, `US fluid ounces`   | `10 fl oz`     |
| Gallon (Imperial)      | `gal-imp`, `imperial gallon`, `imperial gallons`                    | `10 gal-imp`   |
| Quart (Imperial)       | `qt-imp`, `imperial quart`, `imperial quarts`                       | `10 qt-imp`    |
| Pint (Imperial)        | `pint-imp`, `imperial pint`, `imperial pints`                       | `10 pint-imp`  |
| Fluid Ounce (Imperial) | `fl-oz-imp`, `imperial fluid ounce`, `imperial fluid ounces`        | `10 fl-oz-imp` |
| Gill (US)              | `gi-us`, `US gill`, `US gills`                                      | `10 gi-us`     |
| Gill (Imperial)        | `gi-imp`, `imperial gill`, `imperial gills`                         | `10 gi-imp`    |
| Tablespoon             | `tbsp`, `tablespoon`, `tablespoons`                                 | `10 tbsp`      |
| Teaspoon               | `tsp`, `teaspoon`, `teaspoons`                                      | `10 tsp`       |
| Cubic Inch             | `in³`, `in^3`, `in3`, `cubic inch`, `cubic inches`                  | `10 in³`       |
| Cubic Feet             | `ft³`, `ft^3`, `ft3`, `cuft`, `cubic foot`, `cubic feet`            | `10 ft³`       |

### Mass

| Unit                     | Symbols (Aliases)                                                                                   | Example     |
| :----------------------- | :-------------------------------------------------------------------------------------------------- | :---------- |
| Nanogram                 | `ng`, `nanogram`, `nanograms`                                                                       | `10 ng`     |
| Microgram                | `mcg`, `µg`, `ug`, `microgram`, `micrograms`                                                        | `10 mcg`    |
| Milligram                | `mg`, `milligram`, `milligrams`                                                                     | `10 mg`     |
| Gram                     | `g`, `gram`, `grams`                                                                                | `10 g`      |
| Kilogram                 | `kg`, `kgs`, `kilograms`                                                                            | `10 kg`     |
| Metric Ton               | `t`, `tonne`, `tonnes`, `ton`, `tons`, `metric ton`, `metric tons`, `metric tonne`, `metric tonnes` | `10 t`      |
| Ounce                    | `oz`, `ounce`, `ounces`                                                                             | `10 oz`     |
| Pound                    | `lb`, `lbs`, `pound`, `pounds`                                                                      | `10 lb`     |
| Stone                    | `st`, `stone`, `stones`                                                                             | `10 st`     |
| Short Ton                | `sh ton`, `short ton`, `short tons`                                                                 | `10 sh ton` |
| Troy Ounce               | `ozt`, `oz t`, `troy ounce`, `troy ounces`                                                          | `10 ozt`    |
| Carat                    | `ct`, `carat`, `carats`                                                                             | `10 ct`     |
| Ettogram                 | `hg`, `ettogram`, `ettograms`                                                                       | `10 hg`     |
| Centigram                | `cg`, `centigram`, `centigrams`                                                                     | `10 cg`     |
| Quintal                  | `q`, `quintal`, `quintals`                                                                          | `10 q`      |
| Pennyweight              | `dwt`, `pennyweight`                                                                                | `10 dwt`    |
| Unified atomic mass unit | `u`, `amu`                                                                                          | `10 u`      |

### Speed

| Unit               | Symbols (Aliases)                           | Example             |
| :----------------- | :------------------------------------------ | :------------------ |
| Meter per second   | `m/s`, `mps`, `meters per second`           | `10 m/s`            |
| Kilometer per hour | `km/h`, `kmh`, `kph`, `kilometers per hour` | `10 km/h`           |
| Miles per hour     | `mi/h`, `mph`, `miles per hour`             | `10 mi/h`           |
| Knot               | `kn`, `knot`, `knots`                       | `10 kn`             |
| Feet per second    | `ft/s`, `fps`, `feet per second`            | `10 ft/s`           |
| Speed of light     | `speed of light`                            | `10 speed of light` |

### Angle

| Unit          | Symbols (Aliases)               | Example     |
| :------------ | :------------------------------ | :---------- |
| Radian        | `rad`, `radian`, `radians`      | `10 rad`    |
| Degree        | `deg`, `degree`, `degrees`, `°` | `10 deg`    |
| Minute of arc | `arcmin`, `minute of arc`       | `10 arcmin` |
| Second of arc | `arcsec`, `second of arc`       | `10 arcsec` |

### Temperature

| Unit       | Symbols (Aliases)                                    | Example  |
| :--------- | :--------------------------------------------------- | :------- |
| Celsius    | `°C`, `C`, `celsius`, `degC`, `degree celsius`       | `10 °C`  |
| Fahrenheit | `°F`, `F`, `fahrenheit`, `degF`, `degree fahrenheit` | `10 °F`  |
| Kelvin     | `K`, `kelvin`                                        | `10 K`   |
| Reaumur    | `°Re`, `Re`, `reaumur`, `Réaumur`                    | `10 °Re` |
| Rømer      | `°Rø`, `Rø`, `romer`, `Rømer`                        | `10 °Rø` |
| Delisle    | `°De`, `De`, `delisle`                               | `10 °De` |
| Rankine    | `°Ra`, `Ra`, `rankine`                               | `10 °Ra` |

### Frequency

| Unit      | Symbols (Aliases)  | Example  |
| :-------- | :----------------- | :------- |
| Hertz     | `Hz`, `hertz`      | `10 Hz`  |
| Kilohertz | `kHz`, `kilohertz` | `10 kHz` |
| Megahertz | `MHz`, `megahertz` | `10 MHz` |
| Gigahertz | `GHz`, `gigahertz` | `10 GHz` |

### Energy

| Unit                 | Symbols (Aliases)                             | Example     |
| :------------------- | :-------------------------------------------- | :---------- |
| Joule                | `J`, `joule`, `joules`                        | `10 J`      |
| Kilojoule            | `kJ`, `kilojoule`, `kilojoules`               | `10 kJ`     |
| Megajoule            | `MJ`, `megajoule`, `megajoules`               | `10 MJ`     |
| Calorie              | `cal`, `calorie`, `calories`                  | `10 cal`    |
| Kilocalorie          | `kCal`, `kcal`, `kilocalorie`, `kilocalories` | `10 kCal`   |
| Watt hour            | `Wh`, `watt hour`, `watt hours`               | `10 Wh`     |
| Kilowatt hour        | `kWh`, `kilowatt hour`, `kilowatt hours`      | `10 kWh`    |
| Electron volt        | `eV`, `electronvolt`, `electron volts`        | `10 eV`     |
| Foot pound-force     | `ft lbf`, `ft-lbf`, `foot-pound`              | `10 ft lbf` |
| British thermal unit | `BTU`, `btu`                                  | `10 BTU`    |

### Power

| Unit       | Symbols (Aliases)                 | Example |
| :--------- | :-------------------------------- | :------ |
| Watt       | `W`, `watt`, `watts`              | `10 W`  |
| Milliwatt  | `mW`, `milliwatt`, `milliwatts`   | `10 mW` |
| Kilowatt   | `kW`, `kilowatt`, `kilowatts`     | `10 kW` |
| Megawatt   | `MW`, `megawatt`, `megawatts`     | `10 MW` |
| Horsepower | `hp`, `horsepower`, `horsepowers` | `10 hp` |
| Gigawatt   | `GW`, `gigawatt`, `gigawatts`     | `10 GW` |

### Data

| Unit     | Symbols (Aliases)                   | Example     |
| :------- | :---------------------------------- | :---------- |
| Bit      | `bit`, `bits`, `b`                  | `10 bit`    |
| Nibble   | `nibble`, `nibbles`                 | `10 nibble` |
| Byte     | `B`, `byte`, `bytes`                | `10 B`      |
| Kilobyte | `kB`, `KB`, `kilobyte`, `kilobytes` | `10 kB`     |
| Megabyte | `MB`, `megabyte`, `megabytes`       | `10 MB`     |
| Gigabyte | `GB`, `gigabyte`, `gigabytes`       | `10 GB`     |
| Terabyte | `TB`, `terabyte`, `terabytes`       | `10 TB`     |
| Kibibyte | `KiB`, `kibibyte`, `kibibytes`      | `10 KiB`    |
| Mebibyte | `MiB`, `mebibyte`, `mebibytes`      | `10 MiB`    |
| Gibibyte | `GiB`, `gibibyte`, `gibibytes`      | `10 GiB`    |
| Tebibyte | `TiB`, `tebibyte`, `tebibytes`      | `10 TiB`    |
| Pebibyte | `PiB`, `pebibyte`, `pebibytes`      | `10 PiB`    |
| Exbibyte | `EiB`, `exbibyte`, `exbibytes`      | `10 EiB`    |
| Petabyte | `PB`, `petabyte`, `petabytes`       | `10 PB`     |
| Exabyte  | `EB`, `exabyte`, `exabytes`         | `10 EB`     |
| Kibibit  | `Kibit`, `kibibit`                  | `10 Kibit`  |
| Mebibit  | `Mibit`, `mebibit`                  | `10 Mibit`  |
| Gibibit  | `Gibit`, `gibibit`                  | `10 Gibit`  |
| Tebibit  | `Tibit`, `tebibit`                  | `10 Tibit`  |
| Pebibit  | `Pibit`, `pebibit`                  | `10 Pibit`  |
| Exbibit  | `Eibit`, `exbibit`                  | `10 Eibit`  |
| Kilobit  | `kb`, `kilobit`                     | `10 kb`     |
| Megabit  | `Mb`, `megabit`                     | `10 Mb`     |
| Gigabit  | `Gb`, `gigabit`                     | `10 Gb`     |
| Terabit  | `Tb`, `terabit`                     | `10 Tb`     |
| Petabit  | `Pb`, `petabit`                     | `10 Pb`     |
| Exabit   | `Eb`, `exabit`                      | `10 Eb`     |

### Force

| Unit           | Symbols (Aliases)        | Example  |
| :------------- | :----------------------- | :------- |
| Newton         | `N`, `newton`, `newtons` | `10 N`   |
| Kilogram-force | `kgf`, `kg-f`            | `10 kgf` |
| Pound-force    | `lbf`, `lb-f`            | `10 lbf` |
| Dyne           | `dyn`, `dyne`            | `10 dyn` |
| Poundal        | `pdl`                    | `10 pdl` |

### Fuel consumption

| Unit                        | Symbols (Aliases)    | Example      |
| :-------------------------- | :------------------- | :----------- |
| Liters per 100 km           | `L/100km`, `l/100km` | `10 L/100km` |
| Kilometers per liter        | `km/L`, `km/l`       | `10 km/L`    |
| Miles per Gallon (US)       | `mpg`, `mpg_us`      | `10 mpg`     |
| Miles per Gallon (Imperial) | `mpg_imp`, `mpg_uk`  | `10 mpg_imp` |

### Pressure

| Unit              | Symbols (Aliases)              | Example   |
| :---------------- | :----------------------------- | :-------- |
| Pascal            | `Pa`, `pascal`                 | `10 Pa`   |
| Kilopascal        | `kPa`, `kilopascal`            | `10 kPa`  |
| Megapascal        | `MPa`, `megapascal`            | `10 MPa`  |
| Gigapascal        | `GPa`, `gigapascal`            | `10 GPa`  |
| Hectopascal       | `hPa`, `hectopascal`           | `10 hPa`  |
| Bar               | `bar`, `bars`                  | `10 bar`  |
| Millibar          | `mbar`, `millibar`             | `10 mbar` |
| Atmosphere        | `atm`, `atmosphere`            | `10 atm`  |
| Psi               | `psi`, `pound per square inch` | `10 psi`  |
| Ksi               | `ksi`                          | `10 ksi`  |
| Torr              | `torr`, `mmHg`                 | `10 torr` |
| Inches of Mercury | `inHg`                         | `10 inHg` |

## 11. Numeral systems

NerdCalci expands numeric representations supporting standard numeral naming conventions efficiently with scalable word suffixes:

| Word       | Multiplier          | Example      | Evaluates to |
| :--------- | :------------------ | :----------- | :----------- |
| `hundred`  | `100`               | `5 hundred`  | `500`        |
| `thousand` | `1,000`             | `2 thousand` | `2000`       |
| `lakh`     | `100,000`           | `10 lakh`    | `1000000`    |
| `million`  | `1,000,000`         | `5 million`  | `5000000`    |
| `crore`    | `10,000,000`        | `1.5 crore`  | `15000000`   |
| `billion`  | `1,000,000,000`     | `1 billion`  | `1000000000` |
| `trillion` | `1,000,000,000,000` | `1 trillion` | `1E12`       |

### Radix base conversions

NerdCalci supports converting decimal numbers to/from other number bases using the `in`/`to` operators.

| System      | Keywords             | Base | Example         | Result   |
| :---------- | :------------------- | :--- | :-------------- | :------- |
| Hexadecimal | `hex`, `hexadecimal` | 16   | `15 dec in hex` | `0xF`    |
| Binary      | `bin`, `binary`      | 2    | `10 in bin`     | `0b1010` |
| Octal       | `oct`, `octal`       | 8    | `64 in oct`     | `0o100`  |
| Decimal     | `dec`, `decimal`     | 10   | `10.5 in dec`   | `10`     |

> **💡 Note**: Conversion to other bases casts the value to an Integer (`Long`) before formatting. Floating point fractions are truncated.
>
> **⚠️ Important**: NerdCalci currently only supports decimal literals as numerical inputs in expressions. While you can convert **to** Hex/Binary representations for display, you cannot directly use hex literals like `0x10` or `0b1101` in calculation inputs.
