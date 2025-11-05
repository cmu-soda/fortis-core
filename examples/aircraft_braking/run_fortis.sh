#!/bin/bash

java -jar ../../out/artifacts/fortis_core_jar/fortis-core.jar robustness --stpa --tla-sys MBSCU.tla --cfg-sys MBSCU.cfg --tla-env EnvFlightCrew.tla --cfg-env EnvFlightCrew.cfg
