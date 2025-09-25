----------------------------- MODULE SmallCtrl -----------------------------
EXTENDS Integers, Sequences

VARIABLES power, fault, mode, abarmed, decelrate, step

vars == <<power, fault, mode, abarmed, decelrate, step>>

MAX == 4

Init ==
    /\ power = FALSE
    /\ fault = "Unset"
    /\ mode = "Unset"
    /\ abarmed = FALSE
    /\ decelrate = 0
    /\ step = 0

TurnBSCUOn ==
    /\ step < MAX
    /\ power = FALSE
    /\ step' = step + 1
    /\ power' = TRUE
    /\ UNCHANGED <<fault, mode, abarmed, decelrate>>

SelfCheck ==
    /\ step < MAX
    /\ power = TRUE
    /\ fault = "Unset"
    /\ fault' \in {"Fault", "NoFault"}
    /\ step' = step + 1
    /\ UNCHANGED <<power, mode, abarmed, decelrate>>

SetDecelRate ==
    /\ step < MAX
    /\ abarmed = TRUE
    /\ decelrate = 0
    /\ step' = step + 1
    /\ decelrate' = 9
    /\ UNCHANGED <<power, fault, mode, abarmed>>

Wait ==
    /\ step < MAX
    /\ step' = step + 1
    /\ UNCHANGED <<power, fault, mode, abarmed, decelrate>>

Next == 
    \/ TurnBSCUOn
    \/ SelfCheck
    \*\/ SetDecelRate
    \/ Wait

Spec == Init /\ [][Next]_vars

AdequateDecel == step >= 3 => decelrate > 8

=============================================================================
