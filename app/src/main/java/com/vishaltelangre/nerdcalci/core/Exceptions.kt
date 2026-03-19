package com.vishaltelangre.nerdcalci.core

/**
 * Typed exceptions for the expression engine, providing specific error messages.
 */
open class EvalException(message: String) : Exception(message)
class ParseException(message: String, val position: Int = -1) : EvalException(message)
class UndefinedVariableException(val variableName: String) : EvalException("Unknown variable `$variableName`")
private fun generateCircularMessage(chain: List<String>): String {
    if (chain.size == 2 && chain[0] == chain[1]) {
        return "File `${chain[0]}` references itself, causing an endless loop"
    }
    if (chain.size >= 3 && chain[0] == chain[chain.size - 1]) {
        val root = chain[0]
        val secondary = chain[chain.size - 2]
        return "File `$secondary` also references file `$root`, causing an endless loop"
    }
    return "Endless loop: " + chain.joinToString(" -> ")
}

class CircularReferenceException(val fileName: String, val loadingStack: Set<String> = emptySet())
    : EvalException(generateCircularMessage(loadingStack.toList() + fileName))
class DivisionByZeroException : EvalException("Cannot divide by zero")
class UnknownFunctionException(val functionName: String) : EvalException("Unknown function `$functionName()`")
class ArityMismatchException(val functionName: String, val expected: Int, val actual: Int)
    : EvalException("Function `$functionName()` expects $expected ${if (expected == 1) "argument" else "arguments"}, but got $actual")
