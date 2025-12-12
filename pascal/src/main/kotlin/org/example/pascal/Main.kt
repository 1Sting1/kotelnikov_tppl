package org.example.pascal

import java.io.File

fun main() {
    println("Pascal")
    print("Введите путь к файлу: ")
    val filePath = readlnOrNull()

    if (filePath.isNullOrBlank()) {
        println("Путь к файлу не введен.")
        return
    }

    val file = File(filePath)
    if (!file.exists()) {
        println("'$filePath' не найден.")
        return
    }

    try {
        val sourceCode = file.readText()
        println("\n ${sourceCode}")

        val lexer = Lexer(sourceCode)
        val parser = Parser(lexer)
        val interpreter = Interpreter(parser)
        val result = interpreter.interpret()

        println("\nРезультат")
        if (result.isEmpty()) {
            println("Переменных нет.")
        } else {
            result.forEach { (name, value) ->
                println("$name = $value")
            }
        }

    } catch (e: Exception) {
        println("Ошибка во время выполнения: ${e.message}")
    }
}