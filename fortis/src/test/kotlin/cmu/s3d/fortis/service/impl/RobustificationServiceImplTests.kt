package cmu.s3d.fortis.service.impl

import cmu.s3d.fortis.common.*
import net.automatalib.word.Word
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class RobustificationServiceImplTests {
    private val service = RobustificationServiceImpl()

    @Test
    fun testTherac() {
        val sols = service.robustify(
            listOf(
                Spec(SpecType.FSP, ClassLoader.getSystemResource("specs/therac25/sys.lts").readText())
            ),
            listOf(
                Spec(SpecType.FSP, ClassLoader.getSystemResource("specs/therac25/env.lts").readText())
            ),
            listOf(
                Spec(SpecType.FSP, ClassLoader.getSystemResource("specs/therac25/p.lts").readText())
            ),
            SupervisoryOptions(
                progress = listOf("fire_xray", "fire_ebeam"),
                preferredBeh = mapOf(
                    Priority.P3 to listOf(
                        Word.fromList("x,up,e,enter,b".split(',')),
                        Word.fromList("e,up,x,enter,b".split(',')),
                    ),
                    Priority.P2 to listOf(
                        Word.fromList("x,enter,up,up,e,enter,b".split(',')),
                        Word.fromList("e,enter,up,up,x,enter,b".split(',')),
                    ),
                ),
                controllable = mapOf(
                    Priority.P0 to listOf("fire_xray", "fire_ebeam", "setMode"),
                    Priority.P1 to listOf("x", "e", "enter", "up", "b"),
                ),
                observable = mapOf(
                    Priority.P0 to listOf("x", "e", "enter", "up", "b", "fire_xray", "fire_ebeam", "setMode"),
                ),
                algorithm = Algorithms.Fast,
                maxIter = 1
            ),
            SpecType.FSP
        )
        assert(sols.size == 1)

        val expected =
            "S0 = (e -> S1 | x -> S2),\n" +
            "S1 = (enter -> S4 | up -> S3),\n" +
            "S2 = (enter -> S12 | up -> S9),\n" +
            "S4 = (b -> S44 | up -> S1),\n" +
            "S3 = (e -> S1 | x -> S8),\n" +
            "S12 = (b -> S5 | up -> S2),\n" +
            "S9 = (e -> S16 | x -> S2),\n" +
            "S44 = (fire_ebeam -> S13),\n" +
            "S8 = (enter -> S10 | setMode -> S2 | up -> S7),\n" +
            "S5 = (fire_xray -> S6),\n" +
            "S16 = (setMode -> S1 | up -> S15),\n" +
            "S13 = (enter -> S14),\n" +
            "S10 = (b -> S11 | setMode -> S12 | up -> S8),\n" +
            "S7 = (e -> S1 | setMode -> S9 | x -> S8),\n" +
            "S6 = (enter -> S17),\n" +
            "S15 = (e -> S16 | setMode -> S3 | x -> S2),\n" +
            "S14 = (e -> S19 | x -> S20),\n" +
            "S11 = (fire_xray -> S18 | setMode -> S5),\n" +
            "S17 = (e -> S21 | x -> S22),\n" +
            "S19 = (enter -> S24 | up -> S14),\n" +
            "S20 = (enter -> S26 | setMode -> S27 | up -> S25),\n" +
            "S18 = (enter -> S23 | setMode -> S6),\n" +
            "S21 = (setMode -> S29 | up -> S28),\n" +
            "S22 = (enter -> S30 | up -> S17),\n" +
            "S24 = (b -> S32 | up -> S19),\n" +
            "S26 = (b -> S34 | setMode -> S35 | up -> S20),\n" +
            "S27 = (enter -> S35 | up -> S33),\n" +
            "S25 = (e -> S19 | setMode -> S33 | x -> S20),\n" +
            "S23 = (e -> S29 | setMode -> S17 | x -> S31),\n" +
            "S29 = (enter -> S37 | up -> S36),\n" +
            "S28 = (e -> S21 | setMode -> S36 | x -> S22),\n" +
            "S30 = (b -> S38 | up -> S22),\n" +
            "S32 = (fire_ebeam -> S13),\n" +
            "S34 = (fire_xray -> S41 | setMode -> S42),\n" +
            "S35 = (b -> S42 | up -> S27),\n" +
            "S33 = (e -> S40 | x -> S27),\n" +
            "S31 = (enter -> S39 | setMode -> S22 | up -> S23),\n" +
            "S37 = (b -> S43 | up -> S29),\n" +
            "S36 = (e -> S29 | x -> S31),\n" +
            "S38 = (fire_xray -> S6),\n" +
            "S41 = (enter -> S47 | setMode -> S48),\n" +
            "S42 = (fire_xray -> S48),\n" +
            "S40 = (setMode -> S19 | up -> S46),\n" +
            "S39 = (b -> S45 | setMode -> S30 | up -> S31),\n" +
            "S43 = (fire_ebeam -> S49),\n" +
            "S47 = (e -> S50 | setMode -> S52 | x -> S51),\n" +
            "S48 = (enter -> S52),\n" +
            "S46 = (e -> S40 | setMode -> S14 | x -> S27),\n" +
            "S45 = (fire_xray -> S18 | setMode -> S38),\n" +
            "S49 = (enter -> S53),\n" +
            "S50 = (enter -> S54 | up -> S53),\n" +
            "S52 = (e -> S57 | x -> S56),\n" +
            "S51 = (enter -> S55 | setMode -> S56 | up -> S47),\n" +
            "S53 = (e -> S50 | x -> S51),\n" +
            "S54 = (b -> S58 | up -> S50),\n" +
            "S57 = (setMode -> S50 | up -> S61),\n" +
            "S56 = (enter -> S60 | up -> S52),\n" +
            "S55 = (b -> S59 | setMode -> S60 | up -> S51),\n" +
            "S58 = (fire_ebeam -> S49),\n" +
            "S61 = (e -> S57 | setMode -> S53 | x -> S56),\n" +
            "S60 = (b -> S62 | up -> S56),\n" +
            "S59 = (fire_xray -> S41 | setMode -> S62),\n" +
            "S62 = (fire_xray -> S48).\n"
        assertEquals(expected, sols[0])
    }
}