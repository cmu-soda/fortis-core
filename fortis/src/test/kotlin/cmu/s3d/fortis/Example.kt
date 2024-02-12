package cmu.s3d.fortis

import cmu.s3d.fortis.ts.lts.toFluent
import cmu.s3d.fortis.weakening.SimpleInvariant
import cmu.s3d.fortis.weakening.SimpleInvariantWeakener
import cmu.s3d.fortis.weakening.parseConjunction
import net.automatalib.word.Word
import org.junit.jupiter.api.Test

class Example() {
    @Test
    fun testExample() {
        val invWeakener = SimpleInvariantWeakener.build(
            invariant = listOf(
                SimpleInvariant(
                    antecedent = "Confirmed".parseConjunction(),
                    consequent = "SelectByVoter && VoteByVoter".parseConjunction()
                )
            ),
            fluents = listOf(
                "fluent Confirmed = <confirm, password>".toFluent()!!,
                "fluent SelectByVoter = <v.select, {password, eo.select}>".toFluent()!!,
                "fluent VoteByVoter = <v.vote, {password, eo.vote}>".toFluent()!!,
            ),
            positiveExamples = listOf(
                Word.fromList(
                    "v.enter,password,v.password,select,v.select,v.exit,eo.enter,vote,eo.vote,confirm,eo.confirm".split(
                        ","
                    )
                ),
            ),
            negativeExamples = listOf(
                Word.fromList(
                    "v.enter,password,v.password,v.exit,eo.enter,select,eo.select,vote,eo.vote,confirm,eo.confirm".split(
                        ","
                    )
                ),
            )
        )
        var solution = invWeakener.learn()
        while (solution != null) {
            println(solution.getInvariant().joinToString(" && "))
            solution = solution.next()
        }
    }
}