package cmu.s3d.fortis.weakening

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class UtilsTests {
    @Test
    fun testParseConjunction() {
        val conjunction = "a && b && !c".parseConjunction()
        assert(conjunction == Conjunctions(listOf("a" to true, "b" to true, "c" to false)))
    }

    @Test
    fun testParseDisjunction() {
        val disjunction = "a || b || !c".parseDisjunction()
        assert(disjunction == Disjunctions(listOf("a" to true, "b" to true, "c" to false)))
    }

    @Test
    fun testParseCNF() {
        val cnf = "(a || b || !c) && (d || e || !f) && g".parseCNF()
        assert(cnf == CNF(listOf(
            Disjunctions(listOf("a" to true, "b" to true, "c" to false)),
            Disjunctions(listOf("d" to true, "e" to true, "f" to false)),
            Disjunctions(listOf("g" to true))
        )))
    }

    @Test
    fun testParseCNF2() {
        val cnf = "a".parseCNF()
        assert(cnf == CNF(listOf(
            Disjunctions(listOf("a" to true))
        )))
    }

    @Test
    fun testParseDNF() {
        val dnf = "(a && b && !c) || (d && e && !f) || g".parseDNF()
        assert(dnf == DNF(listOf(
            Conjunctions(listOf("a" to true, "b" to true, "c" to false)),
            Conjunctions(listOf("d" to true, "e" to true, "f" to false)),
            Conjunctions(listOf("g" to true))
        )))
    }

    @Test
    fun testParseDNF2() {
        val dnf = "a".parseDNF()
        assert(dnf == DNF(listOf(
            Conjunctions(listOf("a" to true))
        )))
    }

    @Test
    fun testGR1Invariant1() {
        val invString = "[](a && b -> c)"
        val expected = SimpleGR1Invariant(
            CNF(listOf(
                Disjunctions(listOf("a" to true)),
                Disjunctions(listOf("b" to true))
            )),
            DNF(listOf(
                Conjunctions(listOf("c" to true))
            ))
        )
        assertEquals(expected, SimpleGR1Invariant.oneFromString(invString))
        assertEquals(invString, expected.toString())
        assertEquals("G(->(&(x0,x1),x2))", expected.toLTL2String(listOf("a", "b", "c")))
    }

    @Test
    fun testGR1Invariant2() {
        val invString = "[](a && (b || c) -> d)"
        val expected = SimpleGR1Invariant(
            CNF(listOf(
                Disjunctions(listOf("a" to true)),
                Disjunctions(listOf("b" to true, "c" to true))
            )),
            DNF(listOf(
                Conjunctions(listOf("d" to true))
            ))
        )
        assertEquals(expected, SimpleGR1Invariant.oneFromString(invString))
        assertEquals(invString, expected.toString())
        assertEquals("G(->(&(x0,|(x1,x2)),x3))", expected.toLTL2String(listOf("a", "b", "c", "d")))
    }

    @Test
    fun testGR1Invariant3() {
        val invString = "[](a && b -> c || d)"
        val expected = SimpleGR1Invariant(
            CNF(listOf(
                Disjunctions(listOf("a" to true)),
                Disjunctions(listOf("b" to true))
            )),
            DNF(listOf(
                Conjunctions(listOf("c" to true)),
                Conjunctions(listOf("d" to true))
            ))
        )
        assertEquals(expected, SimpleGR1Invariant.oneFromString(invString))
        assertEquals(invString, expected.toString())
        assertEquals("G(->(&(x0,x1),|(x2,x3)))", expected.toLTL2String(listOf("a", "b", "c", "d")))
    }

    @Test
    fun testGR1Invariant4() {
        val invString = "[](a && b -> (c && d) || e)"
        val expected = SimpleGR1Invariant(
            CNF(listOf(
                Disjunctions(listOf("a" to true)),
                Disjunctions(listOf("b" to true))
            )),
            DNF(listOf(
                Conjunctions(listOf("c" to true, "d" to true)),
                Conjunctions(listOf("e" to true))
            ))
        )
        assertEquals(expected, SimpleGR1Invariant.oneFromString(invString))
        assertEquals(invString, expected.toString())
        assertEquals("G(->(&(x0,x1),|(&(x2,x3),x4)))", expected.toLTL2String(listOf("a", "b", "c", "d", "e")))
    }

    @Test
    fun testGR1Invariant5() {
        val invString = "[](a && !b -> c)"
        val expected = SimpleGR1Invariant(
            CNF(listOf(
                Disjunctions(listOf("a" to true)),
                Disjunctions(listOf("b" to false))
            )),
            DNF(listOf(
                Conjunctions(listOf("c" to true))
            ))
        )
        assertEquals(expected, SimpleGR1Invariant.oneFromString(invString))
        assertEquals(invString, expected.toString())
        assertEquals("G(->(&(x0,!(x1)),x2))", expected.toLTL2String(listOf("a", "b", "c")))
    }

    @Test
    fun testGR1ToSimple1() {
        val invString = "[](a && b -> c)"
        val inv = SimpleGR1Invariant.oneFromString(invString)
        val expected = SimpleInvariant(
            Conjunctions(listOf("a" to true, "b" to true)),
            Conjunctions(listOf("c" to true))
        )
        assertEquals(expected, inv.toSimpleInvariant())
    }

    @Test
    fun testGR1ToSimple2() {
        val invString = "[](a -> b && c)"
        val inv = SimpleGR1Invariant.oneFromString(invString)
        val expected = SimpleInvariant(
            Conjunctions(listOf("a" to true)),
            Conjunctions(listOf("b" to true, "c" to true))
        )
        assertEquals(expected, inv.toSimpleInvariant())
    }

    @Test
    fun testGR1ToSimple3() {
        val invString = "[](a && !b -> c)"
        val inv = SimpleGR1Invariant.oneFromString(invString)
        val expected = SimpleInvariant(
            Conjunctions(listOf("a" to true, "b" to false)),
            Conjunctions(listOf("c" to true))
        )
        assertEquals(expected, inv.toSimpleInvariant())
    }
}