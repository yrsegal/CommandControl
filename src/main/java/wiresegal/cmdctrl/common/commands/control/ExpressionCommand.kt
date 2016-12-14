package wiresegal.cmdctrl.common.commands.control

import com.udojava.evalex.Expression
import java.math.BigDecimal

/**
 * @author WireSegal
 * Created at 5:22 PM on 12/14/16.
 */
class ExpressionCommand(str: String) : Expression(str) {
    init {
        addOperator(object : Operator("and", 2, false) {
            override fun eval(v1: BigDecimal, v2: BigDecimal): BigDecimal {
                return BigDecimal(v1.toBigInteger().and(v2.toBigInteger()))
            }
        })
        addOperator(object : Operator("or", 2, false) {
            override fun eval(v1: BigDecimal, v2: BigDecimal): BigDecimal {
                return BigDecimal(v1.toBigInteger().or(v2.toBigInteger()))
            }
        })
        addOperator(object : Operator("xor", 2, false) {
            override fun eval(v1: BigDecimal, v2: BigDecimal): BigDecimal {
                return BigDecimal(v1.toBigInteger().xor(v2.toBigInteger()))
            }
        })
        addOperator(object : Operator("shl", 2, false) {
            override fun eval(v1: BigDecimal, v2: BigDecimal): BigDecimal {
                return BigDecimal(v1.toBigInteger().shiftLeft(v2.toInt()))
            }
        })
        addOperator(object : Operator("shr", 2, false) {
            override fun eval(v1: BigDecimal, v2: BigDecimal): BigDecimal {
                return BigDecimal(v1.toBigInteger().shiftRight(v2.toInt()))
            }
        })
        addFunction(object : Function("bitnot", 1) {
            override fun eval(parameters: List<BigDecimal>): BigDecimal {
                return BigDecimal(parameters[0].toBigInteger().not())
            }
        })
    }
}
