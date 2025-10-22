---- MODULE Sys ----
EXTENDS Integers

VARIABLES phase, takeoffStep, speed, acceleration
vars == <<phase, takeoffStep, speed, acceleration>>

Crew == INSTANCE Crew WITH
    phase <- phase,
    takeoffStep <- takeoffStep

Plane == INSTANCE Plane WITH
    speed <- speed,
    acceleration <- acceleration

Init ==
/\ Crew!Init
/\ Plane!Init

Gas ==
/\ Crew!Gas
/\ Plane!Gas

Brake ==
/\ Crew!Brake
/\ Plane!Brake

Wait ==
/\ Crew!Wait
/\ Plane!Wait

TypeOK ==
/\ Crew!TypeOK
/\ Plane!TypeOK

Next ==
    \/ Gas
    \/ Brake
    \/ Wait

Spec == Init /\ [][Next]_vars

Safety == Plane!Safety
====
