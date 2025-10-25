#!/bin/bash

#java -jar ../../out/artifacts/fortis_core_jar/fortis-core.jar robustness --stpa --explore-env --tla-sys Plane.tla --cfg-sys Plane.cfg --tla-env Crew.tla --cfg-env Crew.cfg
java -jar ../../out/artifacts/fortis_core_jar/fortis-core.jar robustness --stpa --tla-sys Plane.tla --cfg-sys Plane.cfg --tla-env Crew.tla --cfg-env Crew.cfg
