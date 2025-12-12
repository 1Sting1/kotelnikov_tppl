package org.example.pascal

sealed interface AST

data class BinOp(val left: AST, val op: Token, val right: AST) : AST
data class UnaryOp(val op: Token, val expr: AST) : AST
data class Num(val token: Token) : AST
data class Var(val token: Token) : AST
data class Assign(val left: Var, val op: Token, val right: AST) : AST
data class Compound(val children: List<AST>) : AST
data object NoOp : AST