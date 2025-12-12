package org.example.pascal

open class Parser(private val lexer: Lexer) {
    private var currentToken: Token = lexer.getNextToken()

    private fun eat(type: TokenType) {
        if (currentToken.type == type) {
            currentToken = lexer.getNextToken()
        } else {
            throw IllegalArgumentException("Expected token $type but found ${currentToken.type}")
        }
    }

    private fun factor(): AST {
        val token = currentToken
        return when (token.type) {
            TokenType.PLUS, TokenType.MINUS -> {
                eat(token.type)
                UnaryOp(token, factor())
            }
            TokenType.INTEGER -> {
                eat(TokenType.INTEGER)
                Num(token)
            }
            TokenType.LPAREN -> {
                eat(TokenType.LPAREN)
                val node = expr()
                eat(TokenType.RPAREN)
                node
            }
            TokenType.ID -> variable()
            else -> throw IllegalArgumentException("Unexpected token in factor: ${token.type}")
        }
    }

    private fun term(): AST {
        var node = factor()

        while (currentToken.type == TokenType.MUL || currentToken.type == TokenType.DIV) {
            val token = currentToken
            eat(token.type)
            node = BinOp(node, token, factor())
        }
        return node
    }

    private fun expr(): AST {
        var node = term()

        while (currentToken.type == TokenType.PLUS || currentToken.type == TokenType.MINUS) {
            val token = currentToken
            eat(token.type)
            node = BinOp(node, token, term())
        }
        return node
    }

    private fun variable(): Var {
        val node = Var(currentToken)
        eat(TokenType.ID)
        return node
    }

    private fun assignment(): AST {
        val left = variable()
        val token = currentToken
        eat(TokenType.ASSIGN)
        val right = expr()
        return Assign(left, token, right)
    }

    private fun statement(): AST {
        return when (currentToken.type) {
            TokenType.BEGIN -> compoundStatement()
            TokenType.ID -> assignment()
            else -> empty()
        }
    }

    private fun statementList(): List<AST> {
        val results = mutableListOf<AST>()
        val node = statement()
        results.add(node)

        while (currentToken.type == TokenType.SEMI) {
            eat(TokenType.SEMI)
            results.add(statement())
        }
        return results
    }

    private fun compoundStatement(): AST {
        eat(TokenType.BEGIN)
        val nodes = statementList()
        eat(TokenType.END)
        return Compound(nodes)
    }

    private fun empty(): AST {
        return NoOp
    }

    open fun parse(): AST {
        val node = compoundStatement()
        eat(TokenType.DOT)
        return node
    }
}