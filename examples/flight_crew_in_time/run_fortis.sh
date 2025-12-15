#!/bin/bash

java -jar ../../out/artifacts/fortis_core_jar/fortis-core.jar robustness --stpa --tla-sys BSCU.tla --cfg-sys BSCU.cfg --tla-env FlightCrew.tla --cfg-env FlightCrew.cfg --unique-good
