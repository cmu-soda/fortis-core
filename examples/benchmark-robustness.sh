#!/bin/bash

echo -e "run voting..."
cd voting
timeout 30m java -jar ../../bin/robustifier.jar robustness -s sys.lts -e env0.lts -p p.lts -d env1.lts

echo -e "\n\nrun voting-2..."
cd ../voting2
python generator2.py 2 2
timeout 30m java -jar ../../bin/robustifier.jar robustness -s sys.lts -e env.lts -p p.lts -d dev.lts

echo -e "\n\nrun voting-3..."
cd ../voting2
python generator2.py 3 3
timeout 30m java -jar ../../bin/robustifier.jar robustness -s sys.lts -e env.lts -p p.lts -d dev.lts

echo -e "\n\nrun voting-4..."
cd ../voting2
python generator2.py 4 4
timeout 30m java -jar ../../bin/robustifier.jar robustness -s sys.lts -e env.lts -p p.lts -d dev.lts

echo -e "\n\nrun perfect protocol..."
cd ../abp
timeout 30m java -jar ../../bin/robustifier.jar robustness -s perfect.lts -e abp_env.lts -p p.lts -d abp_env_lossy.lts

echo -e "\n\nrun ABP protocol..."
timeout 30m java -jar ../../bin/robustifier.jar robustness -s abp.lts -e abp_env.lts -p p.lts -d abp_env_lossy.lts

echo -e "\n\nrun therapy..."
cd ../therac25
timeout 30m java -jar ../../bin/robustifier.jar robustness -s sys.lts -e env0.lts -p p.lts -d env.lts

echo -e "\n\nrun infusion pump..."
cd ../pump
timeout 30m java -jar ../../bin/robustifier.jar robustness --jsons config-robustness.json

echo -e "\n\nrun infusion pump-2..."
cd ../pump2
timeout 30m java -jar ../../bin/robustifier.jar robustness --jsons config-robustness.json

echo -e "\n\nrun infusion pump-3..."
cd ../pump3
timeout 30m java -Xmx16g -jar ../../bin/robustifier.jar robustness --jsons config-robustness.json

echo -e "\n\nrun oyster..."
cd ../oyster
timeout 30m java -jar ../../bin/robustifier.jar robustness --jsons config-robustness.json
