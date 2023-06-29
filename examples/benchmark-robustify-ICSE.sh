#!/bin/bash

echo -e "run voting..."
cd voting
echo -e "\nrun with pareto search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-pareto.json
echo -e "\nrun with pareto-non-opt search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-pareto-non-opt.json
echo -e "\nrun with fast search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-fast.json
# echo -e "\nrun with oasis search..."
# timeout 10m java -jar ../../bin/fortis.jar robustify config-oasis.json
# echo -e "\nrun with simple search..."
# timeout 10m java -jar ../../bin/fortis.jar robustify config-simple.json

echo -e "\n\nrun voting-2..."
cd ../voting2
python generator.py 2 1
echo -e "\nrun with pareto search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-pareto.json
echo -e "\nrun with pareto-non-opt search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-pareto-non-opt.json
echo -e "\nrun with fast search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-fast.json
# echo -e "\nrun with oasis search..."
# timeout 10m java -jar ../../bin/fortis.jar robustify config-oasis.json
# echo -e "\nrun with simple search..."
# timeout 10m java -jar ../../bin/fortis.jar robustify config-simple.json

echo -e "\n\nrun voting-3..."
cd ../voting2
python generator.py 3 1
echo -e "\nrun with pareto search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-pareto.json
echo -e "\nrun with pareto-non-opt search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-pareto-non-opt.json
echo -e "\nrun with fast search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-fast.json
# echo -e "\nrun with oasis search..."
# timeout 10m java -jar ../../bin/fortis.jar robustify config-oasis.json
# echo -e "\nrun with simple search..."
# timeout 10m java -jar ../../bin/fortis.jar robustify config-simple.json

echo -e "\n\nrun voting-4..."
cd ../voting2
python generator.py 4 1
echo -e "\nrun with pareto search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-pareto.json
echo -e "\nrun with pareto-non-opt search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-pareto-non-opt.json
echo -e "\nrun with fast search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-fast.json
# echo -e "\nrun with oasis search..."
# timeout 10m java -jar ../../bin/fortis.jar robustify config-oasis.json
# echo -e "\nrun with simple search..."
# timeout 10m java -jar ../../bin/fortis.jar robustify config-simple.json

echo -e "\n\nrun therapy..."
cd ../therac25
echo -e "\nrun with pareto search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-pareto.json
echo -e "\nrun with pareto-non-opt search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-pareto-non-opt.json
echo -e "\nrun with fast search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-fast.json
# echo -e "\nrun with oasis search..."
# timeout 10m java -jar ../../bin/fortis.jar robustify config-oasis.json
# echo -e "\nrun with simple search..."
# timeout 10m java -jar ../../bin/fortis.jar robustify config-simple.json

echo -e "\n\nrun infusion pump..."
cd ../pump
echo -e "\nrun with pareto search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-pareto.json
echo -e "\nrun with pareto-non-opt search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-pareto-non-opt.json
echo -e "\nrun with fast search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-fast.json
# echo -e "\nrun with oasis search..."
# timeout 10m java -jar ../../bin/fortis.jar robustify config-oasis.json
# echo -e "\nrun with simple search..."
# timeout 10m java -jar ../../bin/fortis.jar robustify config-simple.json

echo -e "\n\nrun infusion pump-2..."
cd ../pump2
echo -e "\nrun with pareto search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-pareto.json
echo -e "\nrun with pareto-non-opt search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-pareto-non-opt.json
echo -e "\nrun with fast search..."
timeout 10m java -jar ../../bin/fortis.jar robustify config-fast.json
# echo -e "\nrun with oasis search..."
# timeout 10m java -jar ../../bin/fortis.jar robustify config-oasis.json
# echo -e "\nrun with simple search..."
# timeout 10m java -jar ../../bin/fortis.jar robustify config-simple.json

# echo -e "\n\nrun oyster..."
# cd ../oyster
# echo -e "\nrun with pareto search..."
# timeout 10m java -jar ../../bin/fortis.jar robustify config-pareto.json
# echo -e "\nrun with pareto-non-opt search..."
# timeout 10m java -jar ../../bin/fortis.jar robustify config-pareto-non-opt.json
# echo -e "\nrun with fast search..."
# timeout 10m java -jar ../../bin/fortis.jar robustify config-fast.json
# echo -e "\nrun with oasis search..."
# timeout 10m java -jar ../../bin/fortis.jar robustify config-oasis.json
# echo -e "\nrun with simple search..."
# timeout 10m java -jar ../../bin/fortis.jar robustify config-simple.json