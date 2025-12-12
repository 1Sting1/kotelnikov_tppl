package org.example.pascal

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream

class InterpreterTest {

    private fun runInterpreter(source: String): Map<String, Int> {
        val lexer = Lexer(source)
        val parser = Parser(lexer)
        val interpreter = Interpreter(parser)
        return interpreter.interpret()
    }

    // ==========================================
    // 1. HAPPY PATH (Основная логика)
    // ==========================================

    @Test
    fun testEmptyProgram() {
        val result = runInterpreter("BEGIN ;; END.")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testComplexMath() {
        val source = """
            BEGIN
                x:= 2 + 3 * (2 + 3);
                y:= 2 / 2 - 2 + 3 * ((1 + 1) + (1 + 1));
            END.
        """
        val result = runInterpreter(source)
        assertEquals(17, result["x"])
        assertEquals(11, result["y"])
    }

    @Test
    fun testScopesAndVariables() {
        val result = runInterpreter("BEGIN x := 10; y := x * 2; END.")
        assertEquals(10, result["x"])
        assertEquals(20, result["y"])
    }

    @Test
    fun testUnaryOps() {
        val result = runInterpreter("BEGIN x := -5; y := +3; z := -x + y; END.")
        assertEquals(8, result["z"])
    }

    // ==========================================
    // 2. LEXER COVERAGE (Branch Busters)
    // ==========================================

    @Test
    fun testLexerBranchCoverage() {
        // 1. Покрытие циклов WHILE (выход по EOF и выход по символу)
        // Число -> EOF
        val l1 = Lexer("123")
        assertEquals(TokenType.INTEGER, l1.getNextToken().type)
        assertEquals(TokenType.EOF, l1.getNextToken().type)

        // Число -> Пробел (выход из цикла while(isDigit))
        val l2 = Lexer("123 ")
        assertEquals(TokenType.INTEGER, l2.getNextToken().type)

        // ID -> EOF
        val l3 = Lexer("abc")
        assertEquals(TokenType.ID, l3.getNextToken().type)
        assertEquals(TokenType.EOF, l3.getNextToken().type)

        // ID -> Пробел (выход из цикла while(isLetterOrDigit))
        val l4 = Lexer("abc ")
        assertEquals(TokenType.ID, l4.getNextToken().type)

        // 2. Покрытие IF (isLetter || '_')
        val l5 = Lexer("x") // Letter
        assertEquals(TokenType.ID, l5.getNextToken().type)

        val l6 = Lexer("_") // Underscore
        assertEquals(TokenType.ID, l6.getNextToken().type)

        // 3. Покрытие IF (':' && peek == '=')
        val l8 = Lexer(":=") // True && True
        assertEquals(TokenType.ASSIGN, l8.getNextToken().type)

        // True && False (EOF)
        val l10 = Lexer(":")
        val ex = assertThrows<IllegalArgumentException> { l10.getNextToken() }
        assertTrue(ex.message!!.contains("Unexpected") || ex.message!!.contains("did you mean"))

        // 4. Покрытие всех символов (для WHEN)
        val symbols = listOf('+', '-', '*', '/', '(', ')', '.', ';')
        for (char in symbols) {
            val lex = Lexer(char.toString())
            assertEquals(char.toString(), lex.getNextToken().value)
        }
    }

    @Test
    fun testLexerUnderscoreInMiddle() {
        val l = Lexer("a_b")
        assertEquals("a_b", l.getNextToken().value)
    }

    @Test
    fun testLexerLoopTermination() {
        val lexer = Lexer("abc@")
        assertEquals("abc", lexer.getNextToken().value)
        assertThrows<IllegalArgumentException> { lexer.getNextToken() }
    }

    // ==========================================
    // 3. ОШИБКИ
    // ==========================================

    @Test
    fun testCaseInsensitive() {
        val result = runInterpreter("BeGiN x := 1; eNd.")
        assertEquals(1, result["x"])
    }

    @Test
    fun testLexerUnknownChar() {
        val ex = assertThrows<IllegalArgumentException> { runInterpreter("BEGIN @ END.") }
        assertTrue(ex.message!!.contains("Unknown character"))
    }

    @Test
    fun testParserErrors() {
        assertThrows<IllegalArgumentException> { runInterpreter("BEGIN x := END; END.") }
        assertThrows<IllegalArgumentException> { runInterpreter("BEGIN x := 1 y := 2 END.") }
    }

    @Test
    fun testRuntimeErrors() {
        val ex = assertThrows<IllegalArgumentException> { runInterpreter("BEGIN x := z; END.") }
        assertTrue(ex.message!!.contains("not found"))

        assertThrows<ArithmeticException> { runInterpreter("BEGIN x := 1/0; END.") }
    }

    // ==========================================
    // 4. MOCK PARSER
    // ==========================================

    @Test
    fun testInterpreterUnreachableBranches() {
        val badBin = BinOp(Num(Token(TokenType.INTEGER,"1")), Token(TokenType.EOF), Num(Token(TokenType.INTEGER,"1")))
        val mockParserBin = object : Parser(Lexer("")) { override fun parse() = badBin }
        assertThrows<IllegalArgumentException> { Interpreter(mockParserBin).interpret() }

        val badUnary = UnaryOp(Token(TokenType.MUL), Num(Token(TokenType.INTEGER,"1")))
        val mockParserUnary = object : Parser(Lexer("")) { override fun parse() = badUnary }
        assertThrows<IllegalArgumentException> { Interpreter(mockParserUnary).interpret() }
    }

    // ==========================================
    // 5. GENERATED METHODS
    // ==========================================

    @Test
    fun testGeneratedCodeCoverage() {
        TokenType.values()
        TokenType.valueOf("INTEGER")

        val t = Token(TokenType.ID, "v")
        assertEquals(t, t.copy())
        t.hashCode(); t.toString(); val (tt, tv) = t

        val n = Num(t)
        n.hashCode(); n.toString(); val (nt) = n

        val v = Var(t)
        v.hashCode(); v.toString(); val (vt) = v

        val u = UnaryOp(t, n)
        u.hashCode(); u.toString(); val (ut, ue) = u

        val b = BinOp(n, t, n)
        b.hashCode(); b.toString(); val (bl, bo, br) = b

        val a = Assign(v, t, n)
        a.hashCode(); a.toString(); val (al, ao, ar) = a

        val c = Compound(listOf(a))
        c.hashCode(); c.toString(); val (cl) = c

        assertNotNull(NoOp.toString())
        assertNull(Token(TokenType.EOF).value)
    }

    private val originalIn = System.`in`
    private val originalOut = System.out
    private val outCaptor = ByteArrayOutputStream()

    @BeforeEach
    fun setup() { System.setOut(PrintStream(outCaptor)) }

    @AfterEach
    fun cleanup() {
        System.setIn(originalIn)
        System.setOut(originalOut)
    }

    @Test
    fun testMainHappyPath() {
        val f = File.createTempFile("valid_prog", ".pas")
        f.writeText("BEGIN x:=1; END.")
        f.deleteOnExit()

        System.setIn(ByteArrayInputStream("${f.absolutePath}\n".toByteArray()))
        main()

        assertTrue(outCaptor.toString().contains("x = 1"))
    }

    @Test
    fun testMainFileNotFound() {
        System.setIn(ByteArrayInputStream("missing.pas\n".toByteArray()))
        main()

        val output = outCaptor.toString()
        assertTrue(output.contains("не найден")) { "Actual output: $output" }
    }

    @Test
    fun testMainRuntimeError() {
        val f = File.createTempFile("error_prog", ".pas")
        f.writeText("BEGIN x:=unknown; END.")
        f.deleteOnExit()

        System.setIn(ByteArrayInputStream("${f.absolutePath}\n".toByteArray()))
        main()

        val output = outCaptor.toString()
        assertTrue(output.contains("Ошибка")) { "Actual output: $output" }
    }

    @Test
    fun testMainEmptyInput() {
        System.setIn(ByteArrayInputStream("\n".toByteArray()))
        main()

        val output = outCaptor.toString()
        assertTrue(output.contains("не введен")) { "Actual output: $output" }
    }
}