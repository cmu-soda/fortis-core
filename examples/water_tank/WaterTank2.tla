----------------------------- MODULE WaterTank2 -----------------------------
EXTENDS Integers

VARIABLES waterLevel, pumpOn

vars == <<waterLevel, pumpOn>>

min(x,y) == IF x<y THEN x ELSE y
max(x,y) == IF x>y THEN x ELSE y

Init ==
    /\ waterLevel = 0
    /\ pumpOn = FALSE

TurnPumpOn ==
    /\ pumpOn' = TRUE
    /\ UNCHANGED waterLevel

TurnPumpOff ==
    /\ pumpOn' = FALSE
    /\ UNCHANGED waterLevel

Wait ==
    /\ pumpOn => waterLevel' = min(waterLevel + 1, 3)  \* fill
    /\ ~pumpOn => waterLevel' = max(waterLevel - 1, 0)  \* drain
    /\ UNCHANGED pumpOn

Next ==
    \/ TurnPumpOn
    \/ TurnPumpOff
    \/ Wait

Spec == Init /\ [][Next]_vars

NoOverflow == waterLevel <= 2

=============================================================================
