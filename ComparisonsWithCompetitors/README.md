# ComparisonsWithCompetitors ReadMe

## Introduction
*ComparisonsWithCompetitors* empirically compares some existing approaches for Subgroup Discovery with our algorithm `MCTS4DM`.
This program includes the exhaustive search `SD-Map*` implemented in the `VIKAMINE` application (http://www.vikamine.org), the beam search implemented in the `Cortana` tool (http://datamining.liacs.nl/cortana.html), the sampling algorithm `Misere` (http://misere.co.nf) and the evolutionary algorithm `SSDP` (https://github.com/tarcisiodpl/ssdp).

## Repository contents
This folder contains several folders and files that are required to perform the comparisons:
- The "src" folder contains the source code of the existing approaches and the main class to launch the different tasks.
- The "data" folder contains both the benchmark datasets and the artificial data used in our experiments. Each folder in the "data" folder is related to a dataset, and it contains several files: the files corresponding to the targets and the attributes in the CSV format, and other formats to be used with other existing algorithms.
- The "lib" folder contains the required libraries.
- The "MCTS4DM.conf" is the configuration file that is used by our algorithm `MCTS4DM`. It is automatically updated by the *ComparisonsWithCompetitors* program.
- The "Misere.conf" is the configuration file that is used by the sampling algorithm `Misere`. It is automatically updated by the *ComparisonsWithCompetitors* program.

## How to perform the comparison?
To launch the comparisons, it is required to run the main method in the `liris.cnrs.fr.dm2l.mcts4dm.Main` class. 
The user can also modify this method to perform other comparisons.
The program launches some tasks related to specific approaches and it generates some data file to draw figures (here, we used Gnuplot to generate the figures from the data files).

For any questions/remarks, contact Guillaume BOSC: guillaume.bosc@insa-lyon.fr
