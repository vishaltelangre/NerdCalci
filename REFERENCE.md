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

Trigonometric functions expect angles in **radians**.

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
