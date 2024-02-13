package cmu.s3d.fortis.weakening.benchmark

import cmu.s3d.fortis.weakening.SimpleInvariant
import cmu.s3d.ltl.State
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ProblemTests {
    @Test
    fun testProblemToString() {
        val problem = Problem(
            literals = listOf("a", "b", "c"),
            oldInvariant = SimpleInvariant.oneFromString("[](a -> b)"),
            expected = SimpleInvariant.oneFromString("[](a && c -> b)"),
            positiveTraces = listOf(
                listOf(
                    State(mapOf("a" to true, "b" to true, "c" to true)),
                    State(mapOf("a" to true, "b" to false, "c" to true)),
                    State(mapOf("a" to false, "b" to true, "c" to false))
                )
            ),
            negativeTraces = listOf(
                listOf(
                    State(mapOf("a" to true, "b" to true, "c" to true)),
                    State(mapOf("a" to true, "b" to true, "c" to false)),
                    State(mapOf("a" to false, "b" to true, "c" to false))
                )
            ),
            3
        )
        val expected = """
            1,1,1;1,0,1;0,1,0
            ---
            1,1,1;1,1,0;0,1,0
            ---
            G,!,&,|,->
            ---
            3
            ---
            G(->(&(x0,x2),x1))
            ---
            [](a && c -> b)
            ---
            [](a -> b)
            ---
            a,b,c
        """.trimIndent()
        assertEquals(expected, problem.toString())
    }

    @Test
    fun testParseProblem() {
        val problem = ProblemParser.parseTask("""
            1,1,1;1,0,1;0,1,0
            ---
            1,1,1;1,1,0;0,1,0
            ---
            G,!,&,|,->
            ---
            3
            ---
            G(->(&(x0,x2),x1))
            ---
            [](a && c -> b)
            ---
            [](a -> b)
            ---
            a,b,c
        """.trimIndent())
        assertEquals(listOf("a", "b", "c"), problem.literals)
        assertEquals(SimpleInvariant.oneFromString("[](a -> b)"), problem.oldInvariant)
        assertEquals(SimpleInvariant.oneFromString("[](a && c -> b)"), problem.expected)
        assertEquals(
            listOf(
                listOf(
                    State(mapOf("a" to true, "b" to true, "c" to true)),
                    State(mapOf("a" to true, "b" to false, "c" to true)),
                    State(mapOf("a" to false, "b" to true, "c" to false))
                )
            ),
            problem.positiveTraces
        )
        assertEquals(
            listOf(
                listOf(
                    State(mapOf("a" to true, "b" to true, "c" to true)),
                    State(mapOf("a" to true, "b" to true, "c" to false)),
                    State(mapOf("a" to false, "b" to true, "c" to false))
                )
            ),
            problem.negativeTraces
        )
        assertEquals(3, problem.maxNumOfNode)
    }
}