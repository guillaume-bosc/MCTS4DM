# MCTS4SD ReadMe

## Introduction
*MCTS4SD* is an instant mining algorithm designed to extract frequent subgroups from a dataset.

## Repository contents
To run *MCTS4SD*, you need to download the projet from this GitHub repository. Below, we detail the content of each folder and file contained in this repository.
- The "src" folder contains the source code of the *MCTS4SD* algorithm.
- The "dataBenchmark" folder contains the benchmark datasets used in our experiments. Each folder in the "dataBenchmark" folder is related to a dataset, and it contains three files: the files corresponding to the targets and the attributes in the CSV format, and the arff format of the dataset (to compare with other existing algorithms).
- The "results" folder contains the result of the runs.
- The "lib" folder contains the required libraries.
- The "MCTS4SD.jar" file that is the jar of the *MCTS4SD* algorithm.
- The "parameters.conf" file that is the configuration file for parameters that has to be passed as argument of the *MCTS4SD* algorithm.

## How to launch *MCTS4SD*
This part details the process to run *MCTS4SD*.

1. **Format the dataset.** A dataset is represented by two files : (i) the "properties.csv" file that contains the table of attributes where each line is an object and each column is an attribute; and (ii) the "qualities.csv" file that contains the table of labels where each line is an object (in the same order that in the "properties.csv") and each line is a target attribute. These file are in the CSV format where the separator character between columns is "\t".

2. **Fill out the parameters configuration file.** Create and fill out a parameters configuration file exactly respecting the format of the "parameters.conf" file.

3. **Run *MCTS4SD*.** Once the dataset files and the parameters configuration file are completed, run *MCTS4SD* by: `java -jar MCTS4SD.jar <Path to the parameters configuration file>`. For example: `java -jar MCTS4SD.jar parameters.conf`

4. **Get the results.** Once the execution is over, the results are available and in the `resultFolderName` (provided in the parameters configuration file) within the "results" folder. Four files are created: (i) the "info.log" that contains the values taken by the parameters, the runtime and the date of the run; (ii) the "results.log" file that where each line contains a subgroup. The format is : `<Description>\t<Targets>\t<Measure>\t<E11>\t<E10>\t<E01>` where `E11` is the number of objects that respect the description and that are associated to the targets, `E10` is the number of objects that respect the description and that are not associated to the targets, `E01` is the number of objects that do not respect the description and that are associated to the targets. (iii) the "support.log" file that contains the support of each subgroup (the ith line correspond to the support of the ith subgroups of the "result.log" file), and (iv) the "supportE11.log" file that only contains the IDs of the objects in E11.

For any questions/remarks, contact Guillaume BOSC: guillaume.bosc@insa-lyon.fr