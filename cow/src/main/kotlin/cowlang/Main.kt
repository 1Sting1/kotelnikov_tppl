package cowlang

import java.io.File
import java.nio.charset.StandardCharsets

class Main {
    val memory = IntArray(10000) { 0 }
    var memoryPointer = 0
    var instructionPointer = 0
    var register: Int? = null

    var inputProvider: (() -> Char)? = null

    private val outputBuffer = StringBuilder()

    fun reset() {
        memory.fill(0)
        memoryPointer = 0
        instructionPointer = 0
        register = null
        outputBuffer.clear()
    }

    fun getOutput(): String = outputBuffer.toString()

    fun run(code: String) {
        val instructions = "MoO|MOo|moO|mOo|moo|MOO|OOM|oom|mOO|Moo|OOO|MMM".toRegex()
            .findAll(code)
            .map { it.value }
            .toList()

        instructionPointer = 0

        while (instructionPointer < instructions.size) {
            val command = instructions[instructionPointer]
            executeCommand(command, instructions)
            instructionPointer++
        }
    }

    private fun executeCommand(command: String, contextInstructions: List<String>) {
        when (command) {
            "MoO" -> {
                memory[memoryPointer] = (memory[memoryPointer] + 1) and 0xFF
            }
            "MOo" -> {
                memory[memoryPointer] = (memory[memoryPointer] - 1) and 0xFF
            }
            "moO" -> {
                memoryPointer = (memoryPointer + 1) % memory.size
            }
            "mOo" -> {
                memoryPointer--
                if (memoryPointer < 0) memoryPointer = memory.size - 1
            }

            "MOO" -> {
                if (memory[memoryPointer] == 0) {
                    var depth = 1
                    while (depth > 0 && instructionPointer < contextInstructions.size - 1) {
                        instructionPointer++
                        when (contextInstructions[instructionPointer]) {
                            "MOO" -> depth++
                            "moo" -> depth--
                        }
                    }
                }
            }

            "moo" -> {
                if (memory[memoryPointer] != 0) {
                    var depth = 1
                    while (depth > 0 && instructionPointer > 0) {
                        instructionPointer--
                        when (contextInstructions[instructionPointer]) {
                            "moo" -> depth++
                            "MOO" -> depth--
                        }
                    }
                }
            }

            "OOM" -> {
                val value = memory[memoryPointer]
                outputBuffer.append(value)
                print(value)
            }
            "oom" -> {
                if (inputProvider != null) {
                    try {
                        val charCode = inputProvider!!.invoke().code
                        memory[memoryPointer] = charCode and 0xFF
                    } catch (e: Exception) {
                        memory[memoryPointer] = 0
                    }
                }
            }
            "Moo" -> {
                if (memory[memoryPointer] == 0) {
                    if (inputProvider != null) {
                        try {
                            val charCode = inputProvider!!.invoke().code
                            memory[memoryPointer] = charCode and 0xFF
                        } catch (e: Exception) {
                            memory[memoryPointer] = 0
                        }
                    }
                } else {
                    val char = memory[memoryPointer].toChar()
                    outputBuffer.append(char)
                    print(char)
                }
            }
            "OOO" -> {
                memory[memoryPointer] = 0
            }
            "MMM" -> {
                if (register == null) {
                    register = memory[memoryPointer]
                } else {
                    memory[memoryPointer] = register!!
                    register = null
                }
            }
            "mOO" -> {
                val commandCode = memory[memoryPointer]
                val commandToExecute = getCommandByCode(commandCode)
                if (commandToExecute != null) {

                    if (commandToExecute != "moo" && commandToExecute != "MOO") {
                        executeCommand(commandToExecute, contextInstructions)
                    }
                }
            }
        }
    }

    private fun getCommandByCode(code: Int): String? {
        return when(code) {
            0 -> "MoO"
            1 -> "MOo"
            2 -> "moO"
            3 -> "mOo"
            4 -> "moo"
            5 -> "MOO"
            6 -> "OOM"
            7 -> "oom"
            8 -> "MMM"
            9 -> "OOO"
            10 -> "mOO"
            11 -> "Moo"
            else -> null
        }
    }
}

fun main(args: Array<String>) {
    System.setOut(java.io.PrintStream(System.out, true, StandardCharsets.UTF_8))
    val filenames = if (args.isEmpty()) {
        listOf("cow_examples/hello.cow", "cow_examples/fib.cow")
    } else {
        args.toList()
    }

    val interpreter = Main()

    for (filename in filenames) {
        println("\n $filename ")
        interpreter.reset()
        try {
            val file = File(filename)
            if (file.exists()) {
                val code = file.readText(StandardCharsets.UTF_8)
                interpreter.run(code)
            } else {
                println("Файл не найден: ${file.absolutePath}")
            }
        } catch (e: Exception) {
            println("ОШИБКА: ${e.message}")
            e.printStackTrace()
        }
    }
}