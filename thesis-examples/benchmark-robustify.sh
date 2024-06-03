#!/bin/bash

#################################
# ABP
#################################
echo -e "run perfect protocol..."
cd ./abp
echo -e "\nrun with pareto search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-pareto.json
echo -e "\nrun with pareto-non-opt search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-pareto-non-opt.json
echo -e "\nrun with fast search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-fast.json
echo -e "\nrun with oasis search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-oasis.json
echo -e "\nrun with simple search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-simple.json

#################################
# Therac25
#################################
echo -e "\n\nrun therapy..."
cd ../therac25
echo -e "\nrun with pareto search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-pareto.json
echo -e "\nrun with pareto-non-opt search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-pareto-non-opt.json
echo -e "\nrun with fast search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-fast.json
echo -e "\nrun with oasis search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-oasis.json
echo -e "\nrun with simple search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-simple.json

#################################
# Voting
#################################
echo -e "\n\nrun voting..."
cd ../voting
echo -e "\nrun with pareto search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-pareto.json
echo -e "\nrun with pareto-non-opt search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-pareto-non-opt.json
echo -e "\nrun with fast search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-fast.json
echo -e "\nrun with oasis search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-oasis.json
echo -e "\nrun with simple search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-simple.json

echo -e "\n\nrun voting-2..."
cd ../voting2
python generator.py 2 2
echo -e "\nrun with pareto search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-pareto.json
echo -e "\nrun with pareto-non-opt search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-pareto-non-opt.json
echo -e "\nrun with fast search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-fast.json
echo -e "\nrun with oasis search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-oasis.json
echo -e "\nrun with simple search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-simple.json

echo -e "\n\nrun voting-3..."
cd ../voting2
python generator.py 3 3
echo -e "\nrun with pareto search..."
timeout 10m java -Xmx16g -jar ../../bin/fortis.jar robustify config-pareto.json
echo -e "\nrun with pareto-non-opt search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-pareto-non-opt.json
echo -e "\nrun with fast search..."
timeout 10m java -Xmx16g -jar ../../bin/fortis.jar robustify config-fast.json
echo -e "\nrun with oasis search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-oasis.json
echo -e "\nrun with simple search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-simple.json

echo -e "\n\nrun voting-4..."
cd ../voting2
python generator.py 4 4
echo -e "\nrun with pareto search..."
timeout 10m java -Xmx16g -jar ../../bin/fortis.jar robustify config-pareto.json
echo -e "\nrun with pareto-non-opt search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-pareto-non-opt.json
echo -e "\nrun with fast search..."
timeout 10m java -Xmx16g -jar ../../bin/fortis.jar robustify config-fast.json
echo -e "\nrun with oasis search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-oasis.json
echo -e "\nrun with simple search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-simple.json

#################################
# Infusion Pump
#################################
echo -e "\n\nrun infusion pump..."
cd ../pump
echo -e "\nrun with pareto search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-pareto.json
echo -e "\nrun with pareto-non-opt search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-pareto-non-opt.json
echo -e "\nrun with fast search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-fast.json
echo -e "\nrun with oasis search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-oasis.json
echo -e "\nrun with simple search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-simple.json

echo -e "\n\nrun infusion pump-2..."
cd ../pump2
echo -e "\nrun with pareto search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-pareto.json
echo -e "\nrun with pareto-non-opt search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-pareto-non-opt.json
echo -e "\nrun with fast search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-fast.json
echo -e "\nrun with oasis search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-oasis.json
echo -e "\nrun with simple search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-simple.json

echo -e "\n\nrun infusion pump-3..."
cd ../pump3
echo -e "\nrun with pareto search..."
timeout 10m java -Xmx16g -jar ../../bin/fortis.jar robustify config-pareto.json
echo -e "\nrun with pareto-non-opt search..."
timeout 10m java -Xmx16g -jar ../../bin/fortis.jar robustify config-pareto-non-opt.json
echo -e "\nrun with fast search..."
timeout 10m java -Xmx16g -jar ../../bin/fortis.jar robustify config-fast.json
echo -e "\nrun with oasis search..."
timeout 10m java -Xmx16g -jar ../../bin/fortis.jar robustify config-oasis.json
echo -e "\nrun with simple search..."
timeout 10m java -Xmx16g -jar ../../bin/fortis.jar robustify config-simple.json

#################################
# Oyster
#################################
echo -e "\n\nrun oyster 1..."
cd ../oyster
python generator.py 1
echo -e "\nrun with pareto search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-pareto.json
echo -e "\nrun with pareto-non-opt search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-pareto-non-opt.json
echo -e "\nrun with fast search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-fast.json
echo -e "\nrun with oasis search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-oasis.json
echo -e "\nrun with simple search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-simple.json

echo -e "\n\nrun oyster 2..."
cd ../oyster
python generator.py 2
echo -e "\nrun with pareto search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-pareto.json
echo -e "\nrun with pareto-non-opt search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-pareto-non-opt.json
echo -e "\nrun with fast search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-fast.json
echo -e "\nrun with oasis search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-oasis.json
echo -e "\nrun with simple search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-simple.json

echo -e "\n\nrun oyster 3..."
cd ../oyster
python generator.py 3
echo -e "\nrun with pareto search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-pareto.json
echo -e "\nrun with pareto-non-opt search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-pareto-non-opt.json
echo -e "\nrun with fast search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-fast.json
echo -e "\nrun with oasis search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-oasis.json
echo -e "\nrun with simple search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-simple.json

echo -e "\n\nrun oyster 4..."
cd ../oyster
python generator.py 4
echo -e "\nrun with pareto search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-pareto.json
echo -e "\nrun with pareto-non-opt search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-pareto-non-opt.json
echo -e "\nrun with fast search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-fast.json
echo -e "\nrun with oasis search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-oasis.json
echo -e "\nrun with simple search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-simple.json

echo -e "\n\nrun oyster 5..."
cd ../oyster
python generator.py 5
echo -e "\nrun with pareto search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-pareto.json
echo -e "\nrun with pareto-non-opt search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-pareto-non-opt.json
echo -e "\nrun with fast search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-fast.json
echo -e "\nrun with oasis search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-oasis.json
echo -e "\nrun with simple search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-simple.json
