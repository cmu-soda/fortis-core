----------------------------- MODULE Sys -----------------------------
EXTENDS Integers

VARIABLES waterLevel, pumpOn
VARIABLES step

vars == <<waterLevel, pumpOn>>

WT == INSTANCE WaterTank WITH
    waterLevel <- waterLevel,
    pumpOn <- pumpOn

Env == INSTANCE WaterTankEnv WITH
    step <- step

Init ==
    /\ WT!Init
    /\ Env!Init

TurnPumpOn ==
    /\ WT!TurnPumpOn
    /\ Env!TurnPumpOn

TurnPumpOff ==
    /\ WT!TurnPumpOff
    /\ Env!TurnPumpOff

Wait ==
    /\ WT!Wait
    /\ Env!Wait

Next ==
    \/ TurnPumpOn
    \/ TurnPumpOff
    \/ Wait

Spec == Init /\ [][Next]_vars

NoOverflow == WT!NoOverflow

=============================================================================
