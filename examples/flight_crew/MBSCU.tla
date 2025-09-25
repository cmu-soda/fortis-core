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

VARIABLES power, fault, mode, abarmed, decelrate, step

vars == <<power, fault, mode, abarmed, decelrate, step>>

Init ==
    /\ power = FALSE
    /\ fault = "Unset"
    /\ mode = "Unset"
    /\ abarmed = FALSE
    /\ decelrate = 0
    /\ step = 0

TurnBSCUOn ==
    /\ step < 7
    /\ power = FALSE
    /\ step' = step + 1
    /\ power' = TRUE
    /\ UNCHANGED <<fault, mode, abarmed, decelrate>>

SelfCheck ==
    /\ step < 7
    /\ power = TRUE
    /\ fault = "Unset"
    /\ fault' \in {"Fault", "NoFault"}
    /\ step' = step + 1
    /\ UNCHANGED <<power, mode, abarmed, decelrate>>

SetMode == 
    /\ step < 7
    /\ fault = "NoFault"
    /\ mode = "Unset"
    /\ step' = step + 1
    /\ mode' \in {"Auto", "Normal", "Manual", "Reject"}
    /\ UNCHANGED <<power, fault, abarmed, decelrate>>

ArmAutobrake ==
    /\ step < 7
    /\ mode = "Auto"
    /\ abarmed = FALSE
    /\ step' = step + 1
    /\ abarmed' = TRUE
    /\ UNCHANGED <<power, fault, mode, decelrate>>

SetDecelRate ==
    /\ step < 7
    /\ abarmed = TRUE
    /\ decelrate = 0
    /\ step' = step + 1
    /\ decelrate' = 9
    /\ UNCHANGED <<power, fault, mode, abarmed>>

Wait ==
    /\ step < 7
    /\ step' = step + 1
    /\ UNCHANGED <<power, fault, mode, abarmed, decelrate>>

Next == 
    \/ TurnBSCUOn
    \/ SelfCheck
    \/ SetMode
    \/ ArmAutobrake
    \/ SetDecelRate
    \/ Wait

Spec == Init /\ [][Next]_vars

AdequateDecel == step >= 5 => decelrate > 8

=============================================================================