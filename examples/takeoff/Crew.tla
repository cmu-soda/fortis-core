---- MODULE Crew ----
EXTENDS Integers

VARIABLES phase
vars == <<phase>>

MAX_SPEED == 5

Init ==
/\ phase = "idle"

Gas ==
/\ phase \in {"idle", "startTakeoff", "takeoff"}
/\ (phase = "idle") => (phase' = "beginTaxi")
/\ (phase = "startTakeoff") => (phase' = "takeoff")
/\ (phase = "takeoff") => (phase' = phase)

Brake ==
/\ phase = "taxiing"
/\ phase' = "waitingAtRunway"

Wait ==
/\ phase \in {"beginTaxi", "waitingAtRunway", "takeoff"}
/\ (phase = "beginTaxi") => (phase' = "taxiing")
/\ (phase = "waitingAtRunway") => (phase' = "startTakeoff")
/\ (phase = "takeoff") => (phase' = phase)

TypeOK ==
/\ phase \in {"idle", "beginTaxi", "taxiing", "waitingAtRunway", "startTakeoff", "takeoff"}

Next ==
    \/ Gas
    \/ Brake
    \/ Wait

Spec == Init /\ [][Next]_vars
====
