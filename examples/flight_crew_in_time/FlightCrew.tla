----------------------------- MODULE FlightCrew -----------------------------

EXTENDS Integers

VARIABLES state, mode

vars == <<state, mode>>

Init == 
	/\ state = "prepareToTurnBSCUOn"
    /\ mode = "normal"

TurnBSCUOn == 
	/\ mode = "normal"
	/\ state = "prepareToTurnBSCUOn"
    /\ state' = "BSCUOn"
    /\ UNCHANGED<<mode>>

TurnBSCUOff ==
	/\ mode = "abnormal"
	/\ state \in {"BSCUOn", "prepareToArmAutoBrake"}
    /\ state' = "BSCUOff"
    /\ UNCHANGED<<mode>>

ArmAutoBrake == 
	/\ mode = "normal"
	/\ state = "prepareToArmAutoBrake"
	/\ state' = "AutoBrakeArmed"
    /\ UNCHANGED<<mode>>

DisarmAutoBrake ==
	/\ mode = "abnormal"
	/\ state = "AutoBrakeArmed"
	/\ state' = "BSCUOn"
    /\ UNCHANGED<<mode>>

SwitchToManual ==
	/\ mode = "abnormal"
    /\ state \in {"BSCUOff", "prepareToTurnBSCUOn"}
	/\ state' = "Manual"
    /\ UNCHANGED<<mode>>

Wait ==
    /\ mode = "normal"
    /\ state = "BSCUOn"
    /\ state' = "prepareToArmAutoBrake"
    /\ UNCHANGED<<mode>>

Touchdown == 
	/\ \/ (mode = "normal" /\ state = "AutoBrakeArmed")
	   \/ (mode = "abnormal" /\ state = "Manual")
    /\ state' = "landed"
    /\ UNCHANGED<<mode>>

ANON_ACT_13 == 
	/\ state # "landed"
	/\ mode = "normal"
    /\ mode' = "abnormal"
    /\ UNCHANGED<<state>>

Next ==
	\/ TurnBSCUOff
	\/ ArmAutoBrake
	\/ DisarmAutoBrake
	\/ Wait
	\/ Touchdown
	\/ TurnBSCUOn
	\/ ANON_ACT_13
	\/ SwitchToManual

Spec == Init /\ [][Next]_vars

=============================================================================
