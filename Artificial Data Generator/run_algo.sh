cd graphs
rm *.txt *.pdf
cd ..
#cp ../../Algorithm/MCTS4SD/MCTS4SD.jar .
javac DataGen.java ProcessWithTimeOut.java
java DataGen
cd data && rm -fr * && cd ..
cd results && rm -fr * && cd ..
