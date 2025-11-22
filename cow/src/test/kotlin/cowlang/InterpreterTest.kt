package cowlang

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class InterpreterTest {

    @Test
    fun `MoO and MOo`() {
        val interpreter = Main()
        interpreter.run("MoO MoO MoO MOo")
        assertEquals(2, interpreter.memory[0])
    }

    @Test
    fun `moO and mOo`() {
        val interpreter = Main()
        interpreter.run("MoO moO MoO MoO mOo MOo")
        assertEquals(0, interpreter.memory[0])
        assertEquals(2, interpreter.memory[1])
        assertEquals(0, interpreter.memoryPointer)
    }

    @Test
    fun `OOO`() {
        val interpreter = Main()
        interpreter.run("MoO MoO MoO OOO")
        assertEquals(0, interpreter.memory[0])
    }

    @Test
    fun `MOO-moo`() {
        val code = "MoO MoO MoO " + "MOO " + "moO MoO mOo " + "MOo " + "moo"
        val interpreter = Main()
        interpreter.run(code)
        assertEquals(0, interpreter.memory[0])
        assertEquals(3, interpreter.memory[1])
    }

    @Test
    fun `MMM`() {
        val interpreter = Main()
        interpreter.run("MoO MoO MoO MoO MoO MMM moO MMM")
        assertEquals(5, interpreter.memory[0])
        assertEquals(5, interpreter.memory[1])
        assertNull(interpreter.register)
    }

    @Test
    fun `вложенные циклы`() {
        val interpreter = Main()
        val code = "MoO MoO " + "MOO " + "moO MoO MoO MoO " + "MOO " + "moO MoO mOo " + "MOo " + "moo " + "mOo MOo " + "moo"
        interpreter.run(code)
        assertEquals(0, interpreter.memory[0])
        assertEquals(0, interpreter.memory[1])
        assertEquals(6, interpreter.memory[2])
    }

    @Test
    fun `OOM`() {
        val interpreter = Main()
        repeat(65) { interpreter.run("MoO") }
        interpreter.run("OOM")
        assertEquals("65", interpreter.getOutput())
    }

    @Test
    fun `Moo`() {
        val interpreter = Main()
        repeat(67) { interpreter.run("MoO") }
        interpreter.run("Moo")
        assertEquals("C", interpreter.getOutput())
    }

    @Test
    fun `пропуск цикла когда ячейка = 0`() {
        val interpreter = Main()
        interpreter.run("MOO MoO MoO MoO moo")
        assertEquals(0, interpreter.memory[0])
    }

}