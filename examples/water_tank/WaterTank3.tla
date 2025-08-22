----------------------------- MODULE WaterTank3 -----------------------------
EXTENDS Integers

VARIABLES pumpOn

vars == <<pumpOn>>

min(x,y) == IF x<y THEN x ELSE y
max(x,y) == IF x>y THEN x ELSE y

Init ==
    /\ pumpOn = FALSE

TurnPumpOn ==
    /\ pumpOn' = TRUE

TurnPumpOff ==
    /\ pumpOn' = FALSE

Next ==
    \/ TurnPumpOn
    \/ TurnPumpOff

Spec == Init /\ [][Next]_vars

=============================================================================
