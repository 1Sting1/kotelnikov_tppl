package org.example.pascal

class Lexer(private val text: String) {
    private var pos = 0
    private var currentChar: Char? = if (text.isNotEmpty()) text[0] else null

    private fun advance() {
        pos++
        currentChar = if (pos < text.length) text[pos] else null
    }

    private fun skipWhitespace() {
        while (currentChar != null && currentChar!!.isWhitespace()) {
            advance()
        }
    }

    private fun integer(): String {
        val result = StringBuilder()
        while (currentChar != null && currentChar!!.isDigit()) {
            result.append(currentChar)
            advance()
        }
        return result.toString()
    }

    private fun id(): Token {
        val result = StringBuilder()
        while (currentChar != null && (currentChar!!.isLetterOrDigit() || currentChar == '_')) {
            result.append(currentChar)
            advance()
        }
        val str = result.toString()
        return when (str.uppercase()) {
            "BEGIN" -> Token(TokenType.BEGIN, str)
            "END" -> Token(TokenType.END, str)
            else -> Token(TokenType.ID, str)
        }
    }

    private fun peek(): Char? {
        val peekPos = pos + 1
        return if (peekPos < text.length) text[peekPos] else null
    }

    fun getNextToken(): Token {
        while (currentChar != null) {
            if (currentChar!!.isWhitespace()) {
                skipWhitespace()
                continue
            }

            if (currentChar!!.isDigit()) {
                return Token(TokenType.INTEGER, integer())
            }

            if (currentChar!!.isLetter() || currentChar == '_') {
                return id()
            }

            if (currentChar == ':' && peek() == '=') {
                advance()
                advance()
                return Token(TokenType.ASSIGN, ":=")
            }

            val token = when (currentChar) {
                '+' -> Token(TokenType.PLUS, "+")
                '-' -> Token(TokenType.MINUS, "-")
                '*' -> Token(TokenType.MUL, "*")
                '/' -> Token(TokenType.DIV, "/")
                '(' -> Token(TokenType.LPAREN, "(")
                ')' -> Token(TokenType.RPAREN, ")")
                '.' -> Token(TokenType.DOT, ".")
                ';' -> Token(TokenType.SEMI, ";")
                ':' -> throw IllegalArgumentException("Unexpected character ':' (did you mean ':=')?")
                else -> throw IllegalArgumentException("Unknown character: $currentChar")
            }
            advance()
            return token
        }
        return Token(TokenType.EOF, null)
    }
}