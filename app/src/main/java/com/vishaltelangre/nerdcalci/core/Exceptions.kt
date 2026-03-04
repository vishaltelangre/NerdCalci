package com.vishaltelangre.nerdcalci.core

/**
 * Typed exceptions for the expression engine, providing specific error messages.
 */
open class EvalException(message: String) : Exception(message)
class ParseException(message: String, val position: Int = -1) : EvalException(message)
class UndefinedVariableException(val variableName: String) : EvalException("Undefined variable '$variableName'")
class DivisionByZeroException : EvalException("Division by zero")
class UnknownFunctionException(val functionName: String) : EvalException("Unknown function '$functionName'")
class ArityMismatchException(val functionName: String, val expected: Int, val actual: Int)
    : EvalException("$functionName expects $expected argument(s), got $actual")
