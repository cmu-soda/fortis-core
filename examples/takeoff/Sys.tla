---- MODULE Sys ----
EXTENDS Integers

VARIABLES phase, speed, acceleration
vars == <<phase, speed, acceleration>>

Crew == INSTANCE Crew WITH
    phase <- phase

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
