#!/bin/bash

java -jar ../../out/artifacts/fortis_core_jar/fortis-core.jar robustness --stpa --tla-sys WaterTank.tla --cfg-sys WaterTank.cfg --tla-env WaterTankEnv.tla --cfg-env WaterTankEnv.cfg
