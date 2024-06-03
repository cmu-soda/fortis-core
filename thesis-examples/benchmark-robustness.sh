#!/bin/bash

#################################
# ABP
#################################
echo -e "run perfect protocol..."
cd ./abp
timeout 10m java -jar ../../bin/fortis.jar robustness -s perfect.lts -e abp_env.lts -p p.lts -d abp_env_lossy.lts

echo -e "\n\nrun ABP protocol..."
timeout 10m java -jar ../../bin/fortis.jar robustness -s abp.lts -e abp_env.lts -p p.lts -d abp_env_lossy.lts

#################################
# Therac25
#################################
echo -e "\n\nrun therapy..."
cd ../therac25
timeout 10m java -jar ../../bin/fortis.jar robustness -s sys.lts -e env0.lts -p p.lts -d dev.lts

#################################
# Voting
#################################
echo -e "\n\nrun voting..."
cd ../voting
timeout 10m java -jar ../../bin/fortis.jar robustness -s sys.lts -e env0.lts -p p.lts -d dev.lts

echo -e "\n\nrun voting-2..."
cd ../voting2
python generator2.py 2 2
timeout 10m java -jar ../../bin/fortis.jar robustness -s sys.lts -e env.lts -p p.lts -d dev.lts

echo -e "\n\nrun voting-3..."
cd ../voting2
python generator2.py 3 3
timeout 10m java -jar ../../bin/fortis.jar robustness -s sys.lts -e env.lts -p p.lts -d dev.lts

echo -e "\n\nrun voting-4..."
cd ../voting2
python generator2.py 4 4
timeout 10m java -jar ../../bin/fortis.jar robustness -s sys.lts -e env.lts -p p.lts -d dev.lts

#################################
# Infusion Pump
#################################
echo -e "\n\nrun infusion pump..."
cd ../pump
timeout 10m java -jar ../../bin/fortis.jar robustness --jsons config-robustness.json

echo -e "\n\nrun infusion pump-2..."
cd ../pump2
timeout 10m java -jar ../../bin/fortis.jar robustness --jsons config-robustness.json

echo -e "\n\nrun infusion pump-3..."
cd ../pump3
timeout 10m java -Xmx16g -jar ../../bin/fortis.jar robustness --jsons config-robustness.json

#################################
# Oyster
#################################
echo -e "\n\nrun oyster..."
cd ../oyster
python generator.py 1
timeout 10m java -jar ../../bin/fortis.jar robustness --jsons config-robustness.json

echo -e "\n\nrun oyster-2..."
python generator.py 2
timeout 10m java -jar ../../bin/fortis.jar robustness --jsons config-robustness.json

echo -e "\n\nrun oyster-3..."
python generator.py 3
timeout 10m java -jar ../../bin/fortis.jar robustness --jsons config-robustness.json

echo -e "\n\nrun oyster-4..."
python generator.py 4
timeout 10m java -jar ../../bin/fortis.jar robustness --jsons config-robustness.json

echo -e "\n\nrun oyster-5..."
python generator.py 5
timeout 10m java -jar ../../bin/fortis.jar robustness --jsons config-robustness.json
