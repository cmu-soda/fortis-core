----------------------------- MODULE WaterTankEnv -----------------------------
EXTENDS Integers

VARIABLES step

vars == <<step>>

Init ==
    /\ step = 0

TurnPumpOn ==
    /\ step = 0
    /\ step' = step + 1

TurnPumpOff ==
    /\ step = 3
    /\ step' = step + 1

Wait ==
    /\ step \in {1,2}
    /\ step' = step + 1

Next ==
    \/ TurnPumpOn
    \/ TurnPumpOff
    \/ Wait

Spec == Init /\ [][Next]_vars

=============================================================================
