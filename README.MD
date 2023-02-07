This is an implementation of our robustness computation and behavioral robustification tool *Fortis*.

To compute robustness, the tool takes the specifications of a machine, its normative environment, and a property as inputs, and generate a set of traces where each represents an execution scenario where the machine is robust against but not defined in the normative environment.

To robustify a machine, in addition to the above models, the user needs to provide the deviated environment w.r.t. some deviation and priorities for a set of preferred behavior and controllable/observable events. Fortis automatically synthesizes new designs that satisfy the properties and maximize an objective function w.r.t. the common behavior between the original design and the new design and the cost of changes.

We assume all the specifications of the models are specified in FSP, the modeling language used by [the LTSA tool](https://www.doc.ic.ac.uk/ltsa/).

## System Requirements
This program requires Java version >= 11 and Python 3.8. The program has been tested under
```
openjdk version "11.0.14" 2022-01-18
OpenJDK Runtime Environment (build 11.0.14+9-post-Debian-1deb11u1)
OpenJDK 64-Bit Server VM (build 11.0.14+9-post-Debian-1deb11u1, mixed mode, sharing)

Python 3.8.12
```

## Package Structure
- bin: includes all the executable jars of our tool.
- examples: includes the case study models and scripts to run the benchmark.
- fortis: includes the source code of Fortis.

## Robustness Computation Example
We will use Therac-25 to show an example of our tool.
```
cd examples/therac25
java -jar ../../bin/fortis.jar robustness -s sys.lts -e env0.lts -p p.lts -d env.lts
```
The results would be like:
```
2023-02-02 14:49:28.398 [INFO] BaseCalculator - Generating robust behavior representation traces by equivalence 
classes...
2023-02-02 14:49:28.400 [INFO] BaseCalculator - Generating the weakest assumption...
2023-02-02 14:49:28.400 [INFO] SubsetConstructionGenerator - Compose System and Property...        
2023-02-02 14:49:28.403 [INFO] SubsetConstructionGenerator - System: #states = 22, #transitions: 44
2023-02-02 14:49:28.414 [INFO] SubsetConstructionGenerator - S||P: #states = 20, #transitions: 40
2023-02-02 14:49:28.414 [INFO] SubsetConstructionGenerator - Pruning and determinising the model...
2023-02-02 14:49:28.438 [INFO] Robustness - Equivalence class 'EquivClass(s=1, a=up)':
2023-02-02 14:49:28.443 [INFO] Robustness -     RepTrace(word=x,up, deadlock=false) => x,up
2023-02-02 14:49:28.443 [INFO] Robustness - Equivalence class 'EquivClass(s=8, a=up)':
2023-02-02 14:49:28.445 [INFO] Robustness -     RepTrace(word=e,up, deadlock=false) => e,up
2023-02-02 14:49:28.445 [INFO] Robustness - Equivalence class 'EquivClass(s=3, a=up)':
2023-02-02 14:49:28.448 [INFO] Robustness -     RepTrace(word=x,enter,up, deadlock=false) => x,enter,up
2023-02-02 14:49:28.449 [INFO] Robustness - Equivalence class 'EquivClass(s=9, a=up)':
2023-02-02 14:49:28.451 [INFO] Robustness -     RepTrace(word=e,enter,up, deadlock=false) => e,enter,up
2023-02-02 14:49:28.451 [INFO] Robustness - Equivalence class 'EquivClass(s=6, a=up)':
2023-02-02 14:49:28.453 [INFO] Robustness -     RepTrace(word=x,enter,b,enter,e,up, deadlock=false) => x,enter,b,fire_xray,enter,e,up
2023-02-02 14:49:28.454 [INFO] Robustness - Equivalence class 'EquivClass(s=12, a=up)':
2023-02-02 14:49:28.456 [INFO] Robustness -     RepTrace(word=e,enter,b,enter,x,up, deadlock=false) => e,enter,b,fire_ebeam,enter,x,up
2023-02-02 14:49:28.457 [INFO] Robustness - Equivalence class 'EquivClass(s=7, a=up)':
2023-02-02 14:49:28.458 [INFO] Robustness -     RepTrace(word=x,enter,b,enter,e,enter,up, deadlock=false) => x,enter,b,fire_xray,enter,e,enter,up
2023-02-02 14:49:28.459 [INFO] Robustness - Equivalence class 'EquivClass(s=13, a=up)':
2023-02-02 14:49:28.461 [INFO] Robustness -     RepTrace(word=e,enter,b,enter,x,enter,up, deadlock=false) => e,enter,b,fire_ebeam,enter,x,enter,up
2023-02-02 14:49:28.462 [INFO] Robustness - Total time: 00:00:00:065
```

## Robustification Example
We will use the Voting Machine to show an example of our tool.
```
cd examples/voting
java -jar ../../bin/fortis.jar robustify config-pareto.json
```
This will run the Voting machine example with the SmartPareto search method. You should get output like:
```
2022-09-01 11:54:45.831 [INFO] SupervisoryRobustifier - Number of states of the system: 5
2022-09-01 11:54:45.832 [INFO] SupervisoryRobustifier - Number of states of the environment: 4
2022-09-01 11:54:45.833 [INFO] SupervisoryRobustifier - Number of states of the plant (S || E): 12
2022-09-01 11:54:45.833 [INFO] SupervisoryRobustifier - Number of transitions of the plant: 27
2022-09-01 11:54:45.835 [INFO] SolutionIterator - ==============================>
2022-09-01 11:54:45.835 [INFO] SolutionIterator - Initializing search by using Pareto search...
2022-09-01 11:54:45.835 [INFO] SolutionIterator - Number of preferred behaviors: 1
2022-09-01 11:54:45.836 [INFO] SolutionIterator - Number of controllable events with cost: 9
2022-09-01 11:54:45.837 [INFO] SolutionIterator - Number of observable events with cost: 4
2022-09-01 11:54:45.964 [INFO] SolutionIterator - Maximum fulfilled preferred behavior:
2022-09-01 11:54:45.964 [INFO] SolutionIterator -       select,back,select,vote,confirm
2022-09-01 11:54:45.978 [INFO] SolutionIterator - Initialization completes, time: 00:00:00:142
2022-09-01 11:54:45.978 [INFO] SolutionIterator - Start search from events:
2022-09-01 11:54:45.978 [INFO] SolutionIterator - Controllable: [back, confirm, password, select, vote]
2022-09-01 11:54:45.979 [INFO] SolutionIterator - Observable: [back, confirm, eo.enter, eo.exit, password, select, v.enter, v.exit, vote]
2022-09-01 11:54:45.979 [INFO] SolutionIterator - ==============================>
2022-09-01 11:54:45.980 [INFO] SolutionIterator - Start iteration 1...
2022-09-01 11:54:45.980 [INFO] SolutionIterator - Try to weaken the preferred behavior by one of the 0 behavior sets:
2022-09-01 11:54:49.135 [INFO] SolutionIterator - This iteration completes, time: 00:00:03:156
2022-09-01 11:54:49.135 [INFO] SolutionIterator - Number of controller synthesis process invoked: 194
2022-09-01 11:54:49.136 [INFO] SolutionIterator - New pareto-optimal found:
2022-09-01 11:54:49.136 [INFO] SolutionIterator -       Size of the controller: 40 states and 88 transitions
2022-09-01 11:54:49.136 [INFO] SolutionIterator -       Number of controllable events: 1
2022-09-01 11:54:49.137 [INFO] SolutionIterator -       Controllable: [vote]
2022-09-01 11:54:49.137 [INFO] SolutionIterator -       Number of observable events: 7
2022-09-01 11:54:49.137 [INFO] SolutionIterator -       Observable: [back, confirm, password, select, v.enter, v.exit, vote]
2022-09-01 11:54:49.137 [INFO] SolutionIterator -       Number of preferred behavior: 1
2022-09-01 11:54:49.137 [INFO] SolutionIterator -       Preferred Behavior:
2022-09-01 11:54:49.138 [INFO] SolutionIterator -               select,back,select,vote,confirm
2022-09-01 11:54:49.138 [INFO] SolutionIterator - Utility Preferred Behavior: 30
2022-09-01 11:54:49.138 [INFO] SolutionIterator - Utility Cost: -13

...

2022-09-01 11:54:49.180 [INFO] Robustify - Total number of controller synthesis invoked: 195
2022-09-01 11:54:49.180 [INFO] Robustify - Total number of solutions: 16
2022-09-01 11:54:49.223 [INFO] Robustify - Robustification completes, total time: 00:00:03:575
```

The suffix of the JSON config files indicates the different searching methods. For example, to run the Voting example with LocalSearch, run
```
java -jar ../../bin/fortis.jar robustify config-fast.json
```

## Run the benchmark
We also provide the script to run the benchmark.
```
cd examples
bash benchmark-robustness.sh
bash benchmark-robustify.sh
```
