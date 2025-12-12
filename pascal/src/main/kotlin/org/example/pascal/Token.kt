package org.example.pascal

enum class TokenType {
    INTEGER, PLUS, MINUS, MUL, DIV,
    LPAREN, RPAREN,
    BEGIN, END, DOT, SEMI, ASSIGN,
    ID, EOF
}

data class Token(val type: TokenType, val value: String? = null)