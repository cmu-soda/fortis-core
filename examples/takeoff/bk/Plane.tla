---- MODULE Plane ----
EXTENDS Integers

VARIABLES speed, acceleration
vars == <<speed, acceleration>>

\* minimum acceleration and speed are both 0
MAX_ACCELERATION == 1
MAX_SPEED == 3

Max(a,b) == IF a>b THEN a ELSE b
Min(a,b) == IF a<b THEN a ELSE b

InV1(s) == s = 2


Init ==
/\ speed = 0
/\ acceleration = 0

Gas ==
/\ acceleration < MAX_ACCELERATION
/\ acceleration' = acceleration + 1
/\ UNCHANGED<<speed>>

Brake ==
/\ acceleration > 0
/\ acceleration' = acceleration - 1
/\ UNCHANGED<<speed>>

Wait ==
/\ acceleration' = Max(acceleration - 1, 0)
/\ speed' = Min(speed + acceleration, MAX_SPEED)

Next ==
    \/ Gas
    \/ Brake
    \/ Wait

Spec == Init /\ [][Next]_vars

TypeOK ==
/\ speed \in 0..MAX_SPEED
/\ acceleration \in 0..MAX_ACCELERATION

\* V1 is the point of no return, so acceleration should be positive
Safety == InV1(speed) => acceleration > 0

====
