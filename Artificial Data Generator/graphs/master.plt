clear
reset

set terminal pdfcairo font "Arial,13.5" size 2.8,2 transparent
set key inside right top
set pointsize 0.5
set linestyle 1 lw 1 pt 4 lc 3 lt 0
set linestyle 2 lw 1 pt 2 lc 2 lt 0
#set bmarg 3.5
#set tmarg 2
#set ylabel "Jaccard"  offset 8,6 rotate by 0
set ytics nomirror
set xtics nomirror rotate by 45 right

set datafile separator " "
set yrange [0:1]

 
#set xlabel "out factor"
set out "outfact.pdf"
plot './out-fact-data.txt' using 1:2   axes x1y1 w linespoints pt 4 lc rgb '#0060ad' title '1 iteration', \
	'./out-fact-data.txt' using 1:3 axes x1y1 w linespoints pt 2 lc rgb '#118700' title '10 iterations', \
	'./out-fact-data.txt' using 1:4 axes x1y1 w linespoints pt 8 lc rgb '#F5EA25' title '100 iterations', \
	'./out-fact-data.txt' using 1:5 axes x1y1 w linespoints pt 6 lc rgb '#BD0002' title '1000 iterations', \
	'./out-fact-data.txt' using 1:6 axes x1y1 w linespoints pt 3 lc rgb '#AA0002' title '10000 iterations', \
	'./out-fact-data.txt' using 1:7 axes x1y1 w linespoints pt 3 lc rgb '#AA0002' title '100000 iterations'
	


#set xlabel "nb patterns"
set out "nbpatterns.pdf"
plot './nb-patterns-data.txt' using 1:2   axes x1y1 w linespoints pt 4 lc rgb '#0060ad' title '1 iteration', \
	'./nb-patterns-data.txt' using 1:3 axes x1y1 w linespoints pt 2 lc rgb '#118700' title '10 iterations', \
	'./nb-patterns-data.txt' using 1:4 axes x1y1 w linespoints pt 8 lc rgb '#F5EA25' title '100 iterations', \
	'./nb-patterns-data.txt' using 1:5 axes x1y1 w linespoints pt 6 lc rgb '#BD0002' title '1000 iterations', \
	'./nb-patterns-data.txt' using 1:6 axes x1y1 w linespoints pt 3 lc rgb '#AA0002' title '10000 iterations', \
	'./nb-patterns-data.txt' using 1:7 axes x1y1 w linespoints pt 3 lc rgb '#AA0002' title '100000 iterations'



#set xlabel "pattern size"	
set out "patternsize.pdf"
plot './pattern-size-data.txt' using 1:2   axes x1y1 w linespoints pt 4 lc rgb '#0060ad' title '1 iteration', \
	'./pattern-size-data.txt' using 1:3 axes x1y1 w linespoints pt 2 lc rgb '#118700' title '10 iterations', \
	'./pattern-size-data.txt' using 1:4 axes x1y1 w linespoints pt 8 lc rgb '#F5EA25' title '100 iterations', \
	'./pattern-size-data.txt' using 1:5 axes x1y1 w linespoints pt 6 lc rgb '#BD0002' title '1000 iterations', \
	'./pattern-size-data.txt' using 1:6 axes x1y1 w linespoints pt 3 lc rgb '#AA0002' title '10000 iterations', \
	'./pattern-size-data.txt' using 1:7 axes x1y1 w linespoints pt 3 lc rgb '#AA0002' title '100000 iterations'
	


#set xlabel "nb trans"
set out "nbtrans.pdf"
plot './nbtrans-data.txt' using 1:2   axes x1y1 w linespoints pt 4 lc rgb '#0060ad' title '1 iteration', \
	'./nbtrans-data.txt' using 1:3 axes x1y1 w linespoints pt 2 lc rgb '#118700' title '10 iterations', \
	'./nbtrans-data.txt' using 1:4 axes x1y1 w linespoints pt 8 lc rgb '#F5EA25' title '100 iterations', \
	'./nbtrans-data.txt' using 1:5 axes x1y1 w linespoints pt 6 lc rgb '#BD0002' title '1000 iterations', \
	'./nbtrans-data.txt' using 1:6 axes x1y1 w linespoints pt 3 lc rgb '#AA0002' title '10000 iterations', \
	'./nbtrans-data.txt' using 1:7 axes x1y1 w linespoints pt 3 lc rgb '#AA0002' title '100000 iterations'


#set xlabel "nb attr"
set out "nbattr.pdf"
plot './nb_att-data.txt' using 1:2   axes x1y1 w linespoints pt 4 lc rgb '#0060ad' title '1 iteration', \
	'./nb_att-data.txt' using 1:3 axes x1y1 w linespoints pt 2 lc rgb '#118700' title '10 iterations', \
	'./nb_att-data.txt' using 1:4 axes x1y1 w linespoints pt 8 lc rgb '#F5EA25' title '100 iterations', \
	'./nb_att-data.txt' using 1:5 axes x1y1 w linespoints pt 6 lc rgb '#BD0002' title '1000 iterations', \
	'./nb_att-data.txt' using 1:6 axes x1y1 w linespoints pt 3 lc rgb '#AA0002' title '10000 iterations', \
	'./nb_att-data.txt' using 1:7 axes x1y1 w linespoints pt 3 lc rgb '#AA0002' title '100000 iterations'
	

#set xlabel "attr domain"
set out "attrdomain.pdf"
plot './attdomain-data.txt' using 1:2   axes x1y1 w linespoints pt 4 lc rgb '#0060ad' title '1 iteration',   \
	'./attdomain-data.txt' using 1:3 axes x1y1 w linespoints pt 2 lc rgb '#118700' title '10 iterations',    \
	'./attdomain-data.txt' using 1:4 axes x1y1 w linespoints pt 8 lc rgb '#F5EA25' title '100 iterations',   \
	'./attdomain-data.txt' using 1:5 axes x1y1 w linespoints pt 6 lc rgb '#BD0002' title '1000 iterations',  \
	'./attdomain-data.txt' using 1:6 axes x1y1 w linespoints pt 3 lc rgb '#AA0002' title '10000 iterations', \
	'./attdomain-data.txt' using 1:7 axes x1y1 w linespoints pt 3 lc rgb '#AA0002' title '100000 iterations'
		
