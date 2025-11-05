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

VARIABLES state, time, abnormality_flag

vars == <<state, time, abnormality_flag>>

TOUCHDOWN_TIME == 12
ABNORMALITY_DETECTION_TIME == 5
MAX_TIME == 15

Init ==
    /\ state = "bscu_off"
    /\ time = 0
    /\ abnormality_flag \in {TRUE, FALSE} \* init abnormality flag nondeterministically

TurnBSCUOn ==
    /\ state = "bscu_off"
    /\ state' = "bscu_on"
    /\ time /= TOUCHDOWN_TIME
    /\ time' = time + 1
    /\ UNCHANGED <<abnormality_flag>>

TurnBSCUOff ==
    /\ state = "bscu_on_abnormal" \* the crew will only turn off BSCU in abnormal state
    /\ state' = "bscu_off_abnormal"
    /\ time /= TOUCHDOWN_TIME
    /\ time' = time + 1
    /\ UNCHANGED <<abnormality_flag>>

ArmAutobrake ==
    /\ state = "bscu_on"
    /\ state' = "bscu_armed"
    /\ time /= TOUCHDOWN_TIME
    /\ time' = time + 1
    /\ UNCHANGED <<abnormality_flag>>

DeArmAutobrake ==
    /\ state = "bscu_armed_abnormal" \* the crew will only de-arm in abnormal state
    /\ state' = "bscu_on_abnormal"
    /\ time /= TOUCHDOWN_TIME
    /\ time' = time + 1
    /\ UNCHANGED <<abnormality_flag>>

SetManualMode ==
    /\ state = "bscu_off_abnormal" \* the crew will only set manual mode in abnormal state
    /\ state' = "manual_mode"
    /\ time /= TOUCHDOWN_TIME
    /\ time' = time + 1
    /\ UNCHANGED <<abnormality_flag>>

AbnormalDetected ==
    /\ state \in {"bscu_armed", "bscu_on"} \* only when BSCU is on, abnormality can be detected
    /\ abnormality_flag = TRUE
    /\ time >= ABNORMALITY_DETECTION_TIME
    /\ time /= TOUCHDOWN_TIME
    /\ (state = "bscu_armed") => (state' = "bscu_armed_abnormal")
    /\ (state = "bscu_on") => (state' = "bscu_on_abnormal")
    /\ UNCHANGED <<abnormality_flag, time>> \* no time elapse for abnormality detection

Touchdown ==
    /\ time = TOUCHDOWN_TIME \* this is the only transition that can happen at touchdown time
    /\ time' = time + 1
    /\ UNCHANGED <<state, abnormality_flag>>

Wait ==
    /\ state \in {"bscu_armed", "manual_mode"}
    /\ (state = "bscu_armed") => ((abnormality_flag = FALSE) \/ (time < ABNORMALITY_DETECTION_TIME)) \* make sure that AbnormalDetected can happen by preventing taking Wait when abnormality can be detected
    /\ time /= TOUCHDOWN_TIME
    /\ time' = time + 1
    /\ time <= MAX_TIME
    /\ UNCHANGED <<state, abnormality_flag>>

TypeOK ==
/\ state \in {"bscu_off", "bscu_on", "bscu_armed", "bscu_armed_abnormal", "bscu_on_abnormal", "bscu_off_abnormal", "manual_mode"}

Next == 
    \/ TurnBSCUOn
    \/ TurnBSCUOff
    \/ SetManualMode
    \/ ArmAutobrake
    \/ DeArmAutobrake
    \/ AbnormalDetected
    \/ Touchdown
    \/ Wait

Spec == Init /\ [][Next]_vars

=============================================================================