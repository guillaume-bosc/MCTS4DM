# MCTS4DM ReadMe

## Introduction
*MCTS4DM* is a anytime pattern mining algorithm that is able to consider tabular data with numerical and categorical attributes and to extracting patterns that distingishe a target (subgroup discovery, exceptional model mining, ...). 

## Repository contents
To run *MCTS4DM*, you need to download the projet from this GitHub repository. Below, we detail the content of each folder and file contained in this repository.
- The "src" folder contains the source code of the *MCTS4DM* algorithm.
- The "datasets" folder contains the benchmark datasets used in our experiments. Each folder in the "dataBenchmark" folder is related to a dataset, and it contains three files: the files corresponding to the targets and the attributes in the CSV format, and the arff format of the dataset (to compare with other existing algorithms).
- The "results" folder contains the result of the runs.
- The "lib" folder contains the required libraries.
- The "MCTS4DM.jar" file that is the jar of the *MCTS4DM* algorithm.
- The "parameters.conf" file that is the configuration file for parameters that has to be passed as argument of the *MCTS4DM* algorithm.
- The "RunExperiments" folder contains the program that can launch automatically a batch of experiments. In this folder there are (i) the "results" folder containing the results of the batchs of experiments given with a defined hierarchy, (ii) the "src" folder containing the source codes of this program, (iii) the "RunExperiments.jar" file, and (iv) the "paramGen.conf" file generating during the program. To run this program, execute `java -jar RunExperiment.jar [Options]`, where the options are chosen among `{ucb, expand, rollout, memory, update, iter}` that enables to run the experiment of a defined strategy of MCTS.
- The "ComparisonsWithCompetitors" folder contains the program that compare `MCTS4DM` with existing approaches, namely, `SD-Map*` (exhaustive search), beam search with the `Cortana`tool, `SSDP` (evolutionary algorithm) and `Misere`(sampling approach).
- The "GenerateDataPlot" folder contains the program to generate the data file from the results of *MCTS4DM* (located in `RunExperiments/results` with the specific hierarchy). For that, run the "GenerateDataPlot.jar" file by `java -jar GenerateDataPlot.jar`. To plot the graphes with *GnuPlot*, run the different script files `./launchQuality.sh`, `./launchRuntime.sh` and `./launchLength.sh` located in the `RunExperiments/results/` folder. The graphes will be created in each `RunExperiments/results/<NameExperiment>/<Dataset>/` folder.

## How to launch *MCTS4DM*
This part details the process to run *MCTS4DM*.

1. **Format the dataset.** A dataset is represented by two files : (i) the "properties.csv" file that contains the table of attributes where each line is an object and each column is an attribute; and (ii) the "qualities.csv" file that contains the table of labels where each line is an object (in the same order that in the "properties.csv") and each line is a target attribute. These file are in the CSV format where the separator character between columns is "\t".

2. **Fill out the parameters configuration file.** Create and fill out a parameters configuration file exactly respecting the format of the "parameters.conf" file.

3. **Run *MCTS4DM*.** Once the dataset files and the parameters configuration file are completed, run *MCTS4DM* by: `java -jar MCTS4DM.jar <Path to the parameters configuration file>`. For example: `java -jar MCTS4DM.jar parameters.conf`

4. **Get the results.** Once the execution is over, the results are available and in the `resultFolderName` (provided in the parameters configuration file) within the "results" folder. Four files are created: (i) the "info.log" that contains the values taken by the parameters, the runtime and the date of the run; (ii) the "results.log" file that where each line contains a subgroup. The format is : `<Description>\t<Targets>\t<Measure>\t<E11>\t<E10>\t<E01>` where `E11` is the number of objects that respect the description and that are associated to the targets, `E10` is the number of objects that respect the description and that are not associated to the targets, `E01` is the number of objects that do not respect the description and that are associated to the targets. (iii) the "support.log" file that contains the support of each subgroup (the ith line correspond to the support of the ith subgroups of the "result.log" file), and (iv) the "supportE11.log" file that only contains the IDs of the objects in E11.

For any questions/remarks, contact Guillaume BOSC: guillaume.bosc@insa-lyon.fr
