package cmu.s3d.fortis.weakening

import org.junit.jupiter.api.Test

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
}