----------------------------- MODULE BSCU -----------------------------

EXTENDS Integers

VARIABLES Main_state, wait_count, SelfCheck_state, Land_state

vars == <<Main_state, wait_count, SelfCheck_state, Land_state>>

Init == 
	/\ Main_state = "BSCUOff"
	/\ wait_count = 0
	/\ SelfCheck_state = "SystemNormal"
	/\ Land_state = "InAir"

TurnBSCUOn == 
	/\ Main_state = "BSCUOff"
	/\ Main_state' = "BSCUOn"
	/\ UNCHANGED <<wait_count, SelfCheck_state, Land_state>>

TurnBSCUOff == 
	/\ Main_state = "BSCUOn"
	/\ Main_state' = "BSCUOff"
	/\ UNCHANGED <<wait_count, SelfCheck_state, Land_state>>

ArmAutoBrake == 
	/\ Main_state = "BSCUOn"
	/\ Main_state' = "AutoBrakeOn"
	/\ UNCHANGED <<wait_count, SelfCheck_state, Land_state>>

DisarmAutoBrake == 
	/\ Main_state = "AutoBrakeOn"
	/\ Main_state' = "BSCUOn"
	/\ UNCHANGED <<wait_count, SelfCheck_state, Land_state>>

SwitchToManual == 
	/\ Main_state = "BSCUOff"
	/\ Main_state' = "ManualMode"
	/\ UNCHANGED <<wait_count, SelfCheck_state, Land_state>>

Touchdown == 
	/\ Land_state = "InAir"
	/\ Land_state' = "Landed"
	/\ UNCHANGED <<wait_count, SelfCheck_state, Main_state>>

ANON_ACT_13 == 
    /\ Land_state = "InAir"
	/\ SelfCheck_state = "SystemNormal"
	/\ SelfCheck_state' = "SystemAbnormal"
	/\ UNCHANGED <<wait_count, Main_state, Land_state>>

Next ==
	\/ TurnBSCUOff
	\/ ArmAutoBrake
	\/ DisarmAutoBrake
	\/ Touchdown
	\/ TurnBSCUOn
	\/ ANON_ACT_13
	\/ SwitchToManual

Spec == Init /\ [][Next]_vars

TouchdownInCorrectMode == 
	Land_state = "Landed" => 
        \/ /\ SelfCheck_state = "SystemNormal"
           /\ Main_state = "AutoBrakeOn"
		\/ /\ SelfCheck_state = "SystemAbnormal"
           /\ Main_state = "ManualMode"

=============================================================================
