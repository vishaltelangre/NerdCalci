package com.vishaltelangre.nerdcalci.core

/**
 * Typed exceptions for the expression engine, providing specific error messages.
 */
open class EvalException(message: String) : Exception(message)
class ParseException(message: String, val position: Int = -1) : EvalException(message)
class UndefinedVariableException(val variableName: String) : EvalException("Unknown variable `$variableName`")
class CircularReferenceException(val fileName: String) : EvalException("Endless loop: File `$fileName` refers to a file that refers back to it")
class DivisionByZeroException : EvalException("Cannot divide by zero")
class UnknownFunctionException(val functionName: String) : EvalException("Unknown function `$functionName()`")
class ArityMismatchException(val functionName: String, val expected: Int, val actual: Int)
    : EvalException("Function `$functionName()` expects $expected ${if (expected == 1) "argument" else "arguments"}, but got $actual")
