---- MODULE Plane ----
EXTENDS Integers

VARIABLES speed, acceleration
vars == <<speed, acceleration>>

MAX_ACCELERATION == 1
MIN_ACCELERATION == -1
MAX_SPEED == 5
MIN_SPEED == 0

InV1(s) == 3 <= s /\ s <= 4


Init ==
/\ speed = 0
/\ acceleration = 0

Gas ==
/\ acceleration < MAX_ACCELERATION
/\ acceleration' = acceleration + 1
/\ UNCHANGED<<speed>>

Brake ==
/\ acceleration > MIN_ACCELERATION
/\ acceleration' = acceleration - 1
/\ UNCHANGED<<speed>>

Wait ==
/\ speed + acceleration <= MAX_SPEED
/\ speed + acceleration >= MIN_SPEED
/\ speed' = speed + acceleration
/\ UNCHANGED<<acceleration>>

Next ==
    \/ Gas
    \/ Brake
    \/ Wait

Spec == Init /\ [][Next]_vars

TypeOK ==
/\ speed \in MIN_SPEED..MAX_SPEED
/\ acceleration \in MIN_ACCELERATION..MAX_ACCELERATION

\* V1 is the point of no return, so acceleration should be positive
Safety == InV1(speed) => acceleration > 0

====
