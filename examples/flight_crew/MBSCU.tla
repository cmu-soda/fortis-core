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

EXTENDS Integers, Sequences

VARIABLES power, fault, mode, abarmed, decelrate, finished

vars == <<power, fault, mode, abarmed, decelrate, finished>>

Init ==
    /\ power = FALSE
    /\ fault = "Unset"
    /\ mode = "Unset"
    /\ abarmed = FALSE
    /\ decelrate = 0
    /\ finished = FALSE

TurnBSCUOn ==
    /\ power = FALSE
    /\ power' = TRUE
    /\ UNCHANGED <<fault, mode, abarmed, decelrate, finished>>

SelfCheck ==
    /\ power = TRUE
    /\ fault = "Unset"
    /\ fault' \in {"Fault", "NoFault"}
    /\ UNCHANGED <<power, mode, abarmed, decelrate, finished>>

SetMode == 
    /\ fault = "NoFault"
    /\ mode = "Unset"
    /\ mode' \in {"Auto", "Normal", "Manual", "Reject"}
    /\ UNCHANGED <<power, fault, abarmed, decelrate, finished>>

ArmAutobrake ==
    /\ mode = "Auto"
    /\ abarmed = FALSE
    /\ abarmed' = TRUE
    /\ UNCHANGED <<power, fault, mode, decelrate, finished>>

SetDecelRate ==
    /\ abarmed = TRUE
    /\ decelrate = 0
    /\ decelrate' \in 1 .. 10
    /\ UNCHANGED <<power, fault, mode, abarmed, finished>>

Wait ==
    /\ finished = FALSE
    /\ finished' = TRUE
    /\ UNCHANGED <<power, fault, mode, abarmed, decelrate>>

Next == 
    \/ TurnBSCUOn
    \/ SelfCheck
    \/ SetMode
    \/ ArmAutobrake
    \/ SetDecelRate
    \/ Wait

Spec == Init /\ [][Next]_vars

AdequateDecel == finished = TRUE => decelrate > 0
=============================================================================
