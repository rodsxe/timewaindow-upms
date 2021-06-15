# TIME WINDOW-UPMS
Project destinated to the paper "Weighted earliness and tardiness minimization with idle times: An analysis of time window influence on scheduling over unrelated parallel machines environments"

#EXPERIMENTS CONFIGURATIONS

Every configuration of the experiments, e.g. the directory of instances, number of executions without improvement, etc., must be done in the file ExperimentConfig.java;

#COMPILE AND EXECUTE

To compile goes to ./src directory and execute in command line javac -cp lib/tools.jar -encoding ISO-8859-1 @sources.txt

To execute the algorithm goes to ./src directory and execute in command line java -cp lib/tools.jar:. ExperimentRunner The path and name of the result files must be configurated in ExperimentConfig.java

#RESULTS

The analytical results showed in the paper to the benchmark instances are avaliable in the directory ./results.

#INSTANCES

The instances used in the paper are avaliable in the directory ./instances.

#MIP

The proposed MIP are avaliable in ./MIP

#CONTACT

If you have any doubt or question about the source code, please contact us in rodneyoliveira@dppg.cefetmg.br or sergio@dppg.cefetmg.br
