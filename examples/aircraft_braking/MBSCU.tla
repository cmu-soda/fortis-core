----------------------------- MODULE MBSCU -----------------------------

(* 
 * FASR Source Code
 * 
 * Copyright 2025 Carnegie Mellon University.
 * 
 * NO WARRANTY. THIS CARNEGIE MELLON UNIVERSITY AND SOFTWARE ENGINEERING
 * INSTITUTE MATERIAL IS FURNISHED ON AN "AS-IS" BASIS. CARNEGIE MELLON 
 * UNIVERSITY MAKES NO WARRANTIES OF ANY KIND, EITHER EXPRESSED OR IMPLIED, AS
 * TO ANY MATTER INCLUDING, BUT NOT LIMITED TO, WARRANTY OF FITNESS FOR PURPOSE
 * OR MERCHANTABILITY, EXCLUSIVITY, OR RESULTS OBTAINED FROM USE OF THE 
 * MATERIAL. CARNEGIE MELLON UNIVERSITY DOES NOT MAKE ANY WARRANTY OF ANY KIND
 * WITH RESPECT TO FREEDOM FROM PATENT, TRADEMARK, OR COPYRIGHT INFRINGEMENT.
 * 
 * Licensed under a MIT (SEI)-style license, please see license.txt or contact
 * permission@sei.cmu.edu for full terms.
 * 
 * [DISTRIBUTION STATEMENT A] This material has been approved for public 
 * release and unlimited distribution.  Please see Copyright notice for non-US
 * Government use and distribution.
 * 
 * DM25-0946
 */
 *)
EXTENDS Integers
VARIABLES state, abnormality_happened, touchdown_happened
vars == <<state, abnormality_happened, touchdown_happened>>

Init ==
    /\ state = "bscu_off"
    /\ abnormality_happened = FALSE
    /\ touchdown_happened = FALSE

TurnBSCUOn ==
    /\ state = "bscu_off"
    /\ state' = "bscu_on"
    /\ UNCHANGED <<abnormality_happened, touchdown_happened>>

TurnBSCUOff == 
    /\ state = "bscu_on"
    /\ state' = "bscu_off"
    /\ UNCHANGED <<abnormality_happened, touchdown_happened>>

ArmAutobrake ==
    /\ state = "bscu_on"
    /\ state' = "bscu_armed"
    /\ UNCHANGED <<abnormality_happened, touchdown_happened>>

DeArmAutobrake ==
    /\ state = "bscu_armed"
    /\ state' = "bscu_on"
    /\ UNCHANGED <<abnormality_happened, touchdown_happened>>

SetManualMode ==
    /\ state = "bscu_off"
    /\ state' = "manual_mode"
    /\ UNCHANGED <<abnormality_happened, touchdown_happened>>

AbnormalDetected ==
    /\ abnormality_happened' = TRUE \* mark that abnormality has happened, will be used to decide braking at touchdown
    /\ UNCHANGED <<state, touchdown_happened>>

Touchdown ==
    /\ state' = CASE (state = "manual_mode") -> "brake" \* can brake if in manual mode
                [] (state = "bscu_armed" /\ abnormality_happened=FALSE) -> "brake" \* can brake if in armed mode and no abnormality
                [] OTHER -> state \* otherwise state remains the same state
    /\ touchdown_happened' = TRUE \* mark that touchdown has happened, will be used to check invariant
    /\ UNCHANGED <<abnormality_happened>>

Wait ==
    /\ UNCHANGED <<state, abnormality_happened, touchdown_happened>>

Next == 
    \/ TurnBSCUOn
    \/ TurnBSCUOff
    \/ ArmAutobrake
    \/ DeArmAutobrake
    \/ SetManualMode
    \/ AbnormalDetected
    \/ Touchdown
    \/ Wait

TypeOK ==
/\ state \in {"bscu_off", "bscu_on", "bscu_armed", "manual_mode", "brake"}

Spec == Init /\ [][Next]_vars

IntimeBrakeAfterTouchdown == 
    /\ touchdown_happened => state = "brake"

=============================================================================