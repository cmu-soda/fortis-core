package cmu.s3d.fortis.service.impl

import cmu.s3d.fortis.common.*
import net.automatalib.word.Word
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class RobustnessComputationServiceImplTests {
    private val service = RobustnessComputationServiceImpl()

    @Test
    fun testTherac() {
        val expected = listOf(
            listOf(
                RepWithExplain(
                    RepTrace(Word.fromList("x,up".split(',')), deadlock = false), Word.fromList("x,commission,up".split(','))
                )
            ),
            listOf(
                RepWithExplain(
                    RepTrace(Word.fromList("e,up".split(',')), deadlock = false), Word.fromList("e,commission,up".split(','))
                )
            ),
            listOf(
                RepWithExplain(
                    RepTrace(Word.fromList("x,enter,up".split(',')), deadlock = false), Word.fromList("x,enter,commission,up".split(','))
                )
            ),
            listOf(
                RepWithExplain(
                    RepTrace(Word.fromList("e,enter,up".split(',')), deadlock = false), Word.fromList("e,enter,commission,up".split(','))
                )
            ),
            listOf(
                RepWithExplain(
                    RepTrace(Word.fromList("x,enter,b,enter,e,up".split(',')), deadlock = false), Word.fromList("x,enter,b,enter,e,commission,up".split(','))
                )
            ),
            listOf(
                RepWithExplain(
                    RepTrace(Word.fromList("e,enter,b,enter,x,up".split(',')), deadlock = false), Word.fromList("e,enter,b,enter,x,commission,up".split(','))
                )
            ),
            listOf(
                RepWithExplain(
                    RepTrace(Word.fromList("x,enter,b,enter,e,enter,up".split(',')), deadlock = false), Word.fromList("x,enter,b,enter,e,enter,commission,up".split(','))
                )
            ),
            listOf(
                RepWithExplain(
                    RepTrace(Word.fromList("e,enter,b,enter,x,enter,up".split(',')), deadlock = false), Word.fromList("e,enter,b,enter,x,enter,commission,up".split(','))
                )
            ),
        )
        val result = service.computeRobustness(
            listOf(
                Spec(SpecType.FSP, ClassLoader.getSystemResource("specs/therac25/sys.lts").readText())
            ),
            listOf(
                Spec(SpecType.FSP, ClassLoader.getSystemResource("specs/therac25/env_perfect.lts").readText())
            ),
            listOf(
                Spec(SpecType.FSP, ClassLoader.getSystemResource("specs/therac25/p.lts").readText())
            ),
            listOf(
                Spec(SpecType.FSP, ClassLoader.getSystemResource("specs/therac25/env_with_err.lts").readText())
            ),
            RobustnessOptions()
        )
        assertEquals(expected, result)
    }

    @Test
    fun testTheracMinimized() {
        val expected = listOf(
            listOf(
                RepWithExplain(
                    RepTrace(Word.fromList("x,up".split(',')), deadlock = false), Word.fromList("x,commission,up".split(','))
                )
            ),
            listOf(
                RepWithExplain(
                    RepTrace(Word.fromList("e,up".split(',')), deadlock = false), Word.fromList("e,commission,up".split(','))
                )
            ),
            listOf(
                RepWithExplain(
                    RepTrace(Word.fromList("x,enter,up".split(',')), deadlock = false), Word.fromList("x,enter,commission,up".split(','))
                )
            ),
            listOf(
                RepWithExplain(
                    RepTrace(Word.fromList("e,enter,up".split(',')), deadlock = false), Word.fromList("e,enter,commission,up".split(','))
                )
            ),
            listOf(
                RepWithExplain(
                    RepTrace(Word.fromList("x,enter,b,enter,e,up".split(',')), deadlock = false), Word.fromList("x,enter,b,enter,e,commission,up".split(','))
                )
            ),
            listOf(
                RepWithExplain(
                    RepTrace(Word.fromList("x,enter,b,enter,e,enter,up".split(',')), deadlock = false), Word.fromList("x,enter,b,enter,e,enter,commission,up".split(','))
                )
            ),
        )
        val result = service.computeRobustness(
            listOf(
                Spec(SpecType.FSP, ClassLoader.getSystemResource("specs/therac25/sys.lts").readText())
            ),
            listOf(
                Spec(SpecType.FSP, ClassLoader.getSystemResource("specs/therac25/env_perfect.lts").readText())
            ),
            listOf(
                Spec(SpecType.FSP, ClassLoader.getSystemResource("specs/therac25/p.lts").readText())
            ),
            listOf(
                Spec(SpecType.FSP, ClassLoader.getSystemResource("specs/therac25/env_with_err.lts").readText())
            ),
            RobustnessOptions(minimized = true)
        )
        assertEquals(expected, result)
    }

    @Test
    fun testTheracCompare() {
        val expected = emptyList<EquivClassRep>()
        val result = service.compareRobustnessOfTwoSystems(
            listOf(
                Spec(SpecType.FSP, ClassLoader.getSystemResource("specs/therac25/sys.lts").readText())
            ),
            listOf(
                Spec(SpecType.FSP, ClassLoader.getSystemResource("specs/therac25/sys.lts").readText()),
                Spec(SpecType.FSP, ClassLoader.getSystemResource("specs/therac25/sol1.lts").readText())
            ),
            listOf(
                Spec(SpecType.FSP, ClassLoader.getSystemResource("specs/therac25/env_perfect.lts").readText())
            ),
            listOf(
                Spec(SpecType.FSP, ClassLoader.getSystemResource("specs/therac25/p.lts").readText())
            ),
            listOf(
                Spec(SpecType.FSP, ClassLoader.getSystemResource("specs/therac25/env_with_err.lts").readText())
            ),
            RobustnessOptions(minimized = true)
        )
        assertEquals(expected, result)
    }

    @Test
    fun testTheracCompare2() {
        val expected = listOf(
            listOf(
                RepWithExplain(
                    RepTrace(Word.fromList("x,up,e,enter,b".split(',')), deadlock = false), Word.fromList("x,commission,up,e,enter,b".split(','))
                )
            ),
        )
        val result = service.compareRobustnessOfTwoSystems(
            listOf(
                Spec(SpecType.FSP, ClassLoader.getSystemResource("specs/therac25/sys.lts").readText()),
                Spec(SpecType.FSP, ClassLoader.getSystemResource("specs/therac25/sol1.lts").readText())
            ),
            listOf(
                Spec(SpecType.FSP, ClassLoader.getSystemResource("specs/therac25/sys.lts").readText())
            ),
            listOf(
                Spec(SpecType.FSP, ClassLoader.getSystemResource("specs/therac25/env_perfect.lts").readText())
            ),
            listOf(
                Spec(SpecType.FSP, ClassLoader.getSystemResource("specs/therac25/p.lts").readText())
            ),
            listOf(
                Spec(SpecType.FSP, ClassLoader.getSystemResource("specs/therac25/env_with_err.lts").readText())
            ),
            RobustnessOptions(minimized = true)
        )
        assertEquals(expected, result)
    }

    @Test
    fun testTheracWA() {
        val expected =
            "S0 = (x -> S1 | e -> S2),\n" +
            "S1 = (up -> S5 | enter -> S6),\n" +
            "S2 = (up -> S0 | enter -> S3),\n" +
            "S5 = (x -> S1 | e -> S8),\n" +
            "S6 = (up -> S1 | b -> S7),\n" +
            "S3 = (up -> S2 | b -> S4),\n" +
            "S8 = (up -> S5 | enter -> S9),\n" +
            "S7 = (enter -> S5),\n" +
            "S4 = (enter -> S0),\n" +
            "S9 = (up -> S8).\n"
        val result = service.computeWeakestAssumption(
            listOf(
                Spec(SpecType.FSP, ClassLoader.getSystemResource("specs/therac25/sys.lts").readText())
            ),
            listOf(
                Spec(SpecType.FSP, ClassLoader.getSystemResource("specs/therac25/env_perfect.lts").readText())
            ),
            listOf(
                Spec(SpecType.FSP, ClassLoader.getSystemResource("specs/therac25/p.lts").readText())
            ),
            RobustnessOptions(minimized = true),
            SpecType.FSP
        )
        assertEquals(expected, result)
    }

    @Test
    fun testTheracUnsafe() {
        val expected = listOf(
            listOf(
                RepWithExplain(
                    RepTrace(Word.fromList("x,up,e,enter,b".split(',')), deadlock = false), Word.fromList("x,commission,up,e,enter,b".split(','))
                )
            ),
        )
        val result = service.computeIntolerableBeh(
            listOf(
                Spec(SpecType.FSP, ClassLoader.getSystemResource("specs/therac25/sys.lts").readText())
            ),
            listOf(
                Spec(SpecType.FSP, ClassLoader.getSystemResource("specs/therac25/env_perfect.lts").readText())
            ),
            listOf(
                Spec(SpecType.FSP, ClassLoader.getSystemResource("specs/therac25/p.lts").readText())
            ),
            listOf(
                Spec(SpecType.FSP, ClassLoader.getSystemResource("specs/therac25/env_with_err.lts").readText())
            ),
            RobustnessOptions(minimized = true)
        )
        assertEquals(expected, result)
    }
}