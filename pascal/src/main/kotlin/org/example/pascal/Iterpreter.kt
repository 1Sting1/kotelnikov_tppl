package org.example.pascal

class Interpreter(private val parser: Parser) {
    private val globalScope = mutableMapOf<String, Int>()

    fun interpret(): Map<String, Int> {
        val tree = parser.parse()
        visit(tree)
        return globalScope
    }

    private fun visit(node: AST): Int {
        return when (node) {
            is BinOp -> visitBinOp(node)
            is UnaryOp -> visitUnaryOp(node)
            is Num -> node.token.value!!.toInt()
            is Compound -> visitCompound(node)
            is Assign -> visitAssign(node)
            is Var -> visitVar(node)
            is NoOp -> 0
        }
    }

    private fun visitBinOp(node: BinOp): Int {
        val left = visit(node.left)
        val right = visit(node.right)
        return when (node.op.type) {
            TokenType.PLUS -> left + right
            TokenType.MINUS -> left - right
            TokenType.MUL -> left * right
            TokenType.DIV -> left / right
            else -> throw IllegalArgumentException("Unknown operator: ${node.op.type}")
        }
    }

    private fun visitUnaryOp(node: UnaryOp): Int {
        val exprVal = visit(node.expr)
        return when (node.op.type) {
            TokenType.PLUS -> +exprVal
            TokenType.MINUS -> -exprVal
            else -> throw IllegalArgumentException("Unknown unary operator: ${node.op.type}")
        }
    }

    private fun visitCompound(node: Compound): Int {
        for (child in node.children) {
            visit(child)
        }
        return 0
    }

    private fun visitAssign(node: Assign): Int {
        val varName = node.left.token.value!!.lowercase()
        val value = visit(node.right)
        globalScope[varName] = value
        return value
    }

    private fun visitVar(node: Var): Int {
        val varName = node.token.value!!.lowercase()
        return globalScope[varName] ?: throw IllegalArgumentException("Variable '$varName' not found")
    }
}