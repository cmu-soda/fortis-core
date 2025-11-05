----------------------------- MODULE Sys -----------------------------

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

VARIABLES state_crew, time_crew, abnormality_flag_crew, state_bscu, abnormality_happened_bscu, touchdown_happened_bscu

vars == <<state_crew, time_crew, abnormality_flag_crew, state_bscu, abnormality_happened_bscu, touchdown_happened_bscu>>

BSCU == INSTANCE MBSCU WITH
    state <- state_bscu,
    abnormality_happened <- abnormality_happened_bscu,
    touchdown_happened <- touchdown_happened_bscu

Env == INSTANCE EnvFlightCrew WITH
    state <- state_crew,
    time <- time_crew,
    abnormality_flag <- abnormality_flag_crew

Init ==
    /\ BSCU!Init
    /\ Env!Init

TurnBSCUOn == 
    /\ BSCU!TurnBSCUOn
    /\ Env!TurnBSCUOn

TurnBSCUOff == 
    /\ BSCU!TurnBSCUOff
    /\ Env!TurnBSCUOff

ArmAutobrake == 
    /\ BSCU!ArmAutobrake
    /\ Env!ArmAutobrake

DeArmAutobrake == 
    /\ BSCU!DeArmAutobrake
    /\ Env!DeArmAutobrake

SetManualMode == 
    /\ BSCU!SetManualMode
    /\ Env!SetManualMode

AbnormalDetected == 
    /\ BSCU!AbnormalDetected
    /\ Env!AbnormalDetected

Touchdown == 
    /\ BSCU!Touchdown
    /\ Env!Touchdown

Wait ==
    /\ BSCU!Wait
    /\ Env!Wait

Next ==
    \/ TurnBSCUOn
    \/ TurnBSCUOff
    \/ ArmAutobrake
    \/ DeArmAutobrake
    \/ SetManualMode
    \/ AbnormalDetected
    \/ Touchdown
    \/ Wait


Spec == Init /\ [][Next]_vars

IntimeBrakeAfterTouchdown == BSCU!IntimeBrakeAfterTouchdown


=============================================================================