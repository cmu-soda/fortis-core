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

VARIABLES BSCUPower, BSCUFault, ENVStep, BrakingMode, AutobrakeArmed, DecelRate, TaskComplete

vars == <<BSCUPower, BSCUFault, ENVStep, BrakingMode, AutobrakeArmed, DecelRate, TaskComplete>>

BSCU == INSTANCE MBSCU WITH
    power <- BSCUPower,
    fault <- BSCUFault,
    mode <- BrakingMode,
    abarmed <- AutobrakeArmed,
    decelrate <- DecelRate,
    finished <- TaskComplete

Env == INSTANCE EnvFlightCrew WITH
    step <- ENVStep

Init ==
    /\ BSCU!Init
    /\ Env!Init

TurnBSCUOn == 
    /\ BSCU!TurnBSCUOn
    /\ Env!TurnBSCUOn

SelfCheck == 
    /\ BSCU!SelfCheck
    /\ Env!SelfCheck

SetMode ==
    /\ BSCU!SetMode
    /\ Env!SetMode

ArmAutobrake == 
    /\ BSCU!ArmAutobrake
    /\ Env!ArmAutobrake

SetDecelRate == 
    /\ BSCU!SetDecelRate
    /\ Env!SetDecelRate

Wait ==
    /\ BSCU!Wait
    /\ Env!Wait

Next ==
    \/ TurnBSCUOn
    \/ SelfCheck
    \/ SetMode
    \/ ArmAutobrake
    \/ SetDecelRate
    \/ Wait

Spec == Init /\ [][Next]_vars

AdequateDecel == BSCU!AdequateDecel

=============================================================================