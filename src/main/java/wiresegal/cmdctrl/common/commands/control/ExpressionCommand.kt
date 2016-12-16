package wiresegal.cmdctrl.common.commands.control

import com.udojava.evalex.Expression
import java.math.BigDecimal

/**
 * @author WireSegal
 * Created at 5:22 PM on 12/14/16.
 */
class ExpressionCommand(str: String) : Expression(str) {
    init {
        addOperator("and", 2, false) { v1, v2 -> BigDecimal(v1.toBigInteger().and(v2.toBigInteger())) }
        addOperator("or", 2, false) { v1, v2 -> BigDecimal(v1.toBigInteger().or(v2.toBigInteger())) }
        addOperator("xor", 2, false) { v1, v2 -> BigDecimal(v1.toBigInteger().xor(v2.toBigInteger())) }
        addOperator("shl", 2, false) { v1, v2 -> BigDecimal(v1.toBigInteger().shiftLeft(v2.toInt())) }
        addOperator("shr", 2, false) { v1, v2 -> BigDecimal(v1.toBigInteger().shiftRight(v2.toInt())) }
        addFunction("bitnot", 1) { BigDecimal(it[0].toBigInteger().not()) }
    }

    fun addOperator(name: String, precedence: Int, leftAssoc: Boolean, impl: (v1: BigDecimal, v2: BigDecimal) -> BigDecimal) {
        addOperator(OperatorCommand(name, precedence, leftAssoc, impl))
    }

    fun addFunction(name: String, paramCount: Int, impl: (List<BigDecimal>) -> BigDecimal) {
        addFunction(FunctionCommand(name, paramCount, impl))
    }

    inner class OperatorCommand(name: String, precedence: Int, leftAssoc: Boolean, val impl: (v1: BigDecimal, v2: BigDecimal) -> BigDecimal) : Operator(name, precedence, leftAssoc) {
        override fun eval(v1: BigDecimal, v2: BigDecimal) = impl(v1, v2)
    }

    inner class FunctionCommand(name: String, paramCount: Int, val impl: (List<BigDecimal>) -> BigDecimal) : Function(name, paramCount) {
        override fun eval(parameters: List<BigDecimal>) = impl(parameters)
    }
}
