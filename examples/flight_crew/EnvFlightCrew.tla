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

VARIABLES step

vars == <<step>>

Init ==
    /\ step = 0

TurnBSCUOn ==
    /\ step = 0
    /\ step' = 1

SelfCheck ==
    /\ step = 1
    /\ step' = 2

SetMode == 
    /\ step = 2
    /\ step' = 3

ArmAutobrake ==
    /\ step = 3
    /\ step' = 4

SetDecelRate ==
    /\ step = 4
    /\ step' = 5

Wait ==
    /\ step \in {5}
    /\ step' = step + 1

Next == 
    \/ TurnBSCUOn
    \/ SelfCheck
    \/ SetMode
    \/ ArmAutobrake
    \/ SetDecelRate
    \/ Wait

Spec == Init /\ [][Next]_vars

=============================================================================