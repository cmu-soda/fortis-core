----------------------------- MODULE EnvFlightCrew -----------------------------

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

VARIABLES state

vars == <<state>>

Init ==
    /\ state = "bscu_off"

TurnBSCUOn ==
    /\ state = "bscu_off"
    /\ state' = "bscu_on"

TurnBSCUOff ==
    /\ state = "bscu_on_abnormal"
    /\ state' = "bscu_off_abnormal"

ArmAutobrake ==
    /\ state = "bscu_on"
    /\ state' = "bscu_armed"

DeArmAutobrake ==
    /\ state = "bscu_armed_abnormal"
    /\ state' = "bscu_on_abnormal"

SetManualMode ==
    /\ state = "bscu_off_abnormal"
    /\ state' = "manual_mode"

AbnormalDetected ==
    /\ state = "bscu_armed"
    /\ state' = "bscu_armed_abnormal"

Touchdown ==
    /\ state \in {"bscu_armed", "manual_mode"}
    /\ state' = "touchdown_happened"

TypeOK ==
/\ state \in {"bscu_off", "bscu_on", "bscu_armed", "bscu_armed_abnormal", "bscu_on_abnormal", "bscu_off_abnormal", "manual_mode", "touchdown_happened"}

Next == 
    \/ TurnBSCUOn
    \/ TurnBSCUOff
    \/ SetManualMode
    \/ ArmAutobrake
    \/ DeArmAutobrake
    \/ AbnormalDetected
    \/ Touchdown

Spec == Init /\ [][Next]_vars

=============================================================================