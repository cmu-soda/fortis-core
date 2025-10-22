---- MODULE Crew ----
EXTENDS Integers

VARIABLES phase, takeoffStep
vars == <<phase, takeoffStep>>

MAX_SPEED == 5

Init ==
/\ phase = "idle"
/\ takeoffStep = 0

Gas ==
/\ phase \in {"idle", "takeoff"}
/\ takeoffStep < MAX_SPEED
/\ (phase = "idle") => (phase' = "taxiing" /\ takeoffStep' = takeoffStep)
/\ (phase = "takeoff") => (phase' = phase /\ takeoffStep' = takeoffStep + 1)

Brake ==
/\ phase = "taxiing"
/\ phase' = "waitingAtRunway"
/\ UNCHANGED<<takeoffStep>>

Wait ==
/\ takeoffStep < 5
/\ phase \in {"waitingAtRunway", "takeoff"}
/\ (phase = "waitingAtRunway") => (phase' = "takeoff" /\ takeoffStep' = takeoffStep)
/\ (phase = "takeoff") => (phase' = phase /\ takeoffStep' = takeoffStep + 1)

TypeOK ==
/\ phase \in {"idle", "taxiing", "waitingAtRunway", "takeoff"}
/\ takeoffStep \in 0..MAX_SPEED

Next ==
    \/ Gas
    \/ Brake
    \/ Wait

Spec == Init /\ [][Next]_vars
====
