clear
reset

set terminal pdfcairo font "Arial,13.5" size 2.8,2 transparent
set key inside right top
set pointsize 0.5
set linestyle 1 lw 1 pt 4 lc 3 lt 0
set linestyle 2 lw 1 pt 2 lc 2 lt 0
set bmarg 3.5
set tmarg 2
set xlabel "noise rate"
set ylabel "Jaccard"  offset 8,6 rotate by 0
set ytics nomirror
set xtics nomirror rotate by 45 right

set datafile separator " "
set yrange [0:1]

#set y2label "Quality measure" offset -8,6.8 rotate by 0
#set y2range [0:0.5]
#set y2tics nomirror

set out "noise-1.pdf"
plot './data1.txt' using 1:2   axes x1y1 w linespoints pt 4 lc rgb '#0060ad' title '1 iteration', \
	'./data1.txt' using 1:3 axes x1y1 w linespoints pt 2 lc rgb '#118700' title '10 iterations', \
	'./data1.txt' using 1:4 axes x1y1 w linespoints pt 8 lc rgb '#F5EA25' title '100 iterations', \
	'./data1.txt' using 1:5 axes x1y1 w linespoints pt 6 lc rgb '#BD0002' title '1000 iterations', \
	'./data1.txt' using 1:6 axes x1y1 w linespoints pt 3 lc rgb '#AA0002' title '10000 iterations', \
	'./data1.txt' using 1:7 axes x1y1 w linespoints pt 6 lc rgb '#BD0002' title '100000 iterations'


set out "noise-5.pdf"
plot './data5.txt' using 1:2   axes x1y1 w linespoints pt 4 lc rgb '#0060ad' title '1 iteration', \
'./data5.txt' using 1:3 axes x1y1 w linespoints pt 2 lc rgb '#118700' title '10 iterations', \
'./data5.txt' using 1:4 axes x1y1 w linespoints pt 8 lc rgb '#F5EA25' title '100 iterations', \
'./data5.txt' using 1:5 axes x1y1 w linespoints pt 6 lc rgb '#BD0002' title '1000 iterations', \
'./data5.txt' using 1:6 axes x1y1 w linespoints pt 3 lc rgb '#AA0002' title '10000 iterations', \
'./data5.txt' using 1:7 axes x1y1 w linespoints pt 6 lc rgb '#BD0002' title '100000 iterations'


set out "noise-10.pdf"
plot './data10.txt' using 1:2   axes x1y1 w linespoints pt 4 lc rgb '#0060ad' title '1 iteration', \
'./data10.txt' using 1:3 axes x1y1 w linespoints pt 2 lc rgb '#118700' title '10 iterations', \
'./data10.txt' using 1:4 axes x1y1 w linespoints pt 8 lc rgb '#F5EA25' title '100 iterations', \
'./data10.txt' using 1:5 axes x1y1 w linespoints pt 6 lc rgb '#BD0002' title '1000 iterations', \
'./data10.txt' using 1:6 axes x1y1 w linespoints pt 3 lc rgb '#AA0002' title '10000 iterations', \
'./data10.txt' using 1:7 axes x1y1 w linespoints pt 6 lc rgb '#BD0002' title '100000 iterations'


set out "noise-25.pdf"
plot './data25.txt' using 1:2   axes x1y1 w linespoints pt 4 lc rgb '#0060ad' title '1 iteration', \
'./data25.txt' using 1:3 axes x1y1 w linespoints pt 2 lc rgb '#118700' title '10 iterations', \
'./data25.txt' using 1:4 axes x1y1 w linespoints pt 8 lc rgb '#F5EA25' title '100 iterations', \
'./data25.txt' using 1:5 axes x1y1 w linespoints pt 6 lc rgb '#BD0002' title '1000 iterations', \
'./data25.txt' using 1:6 axes x1y1 w linespoints pt 3 lc rgb '#AA0002' title '10000 iterations', \
'./data25.txt' using 1:7 axes x1y1 w linespoints pt 6 lc rgb '#BD0002' title '100000 iterations'


set out "noise-50.pdf"
plot './data50.txt' using 1:2   axes x1y1 w linespoints pt 4 lc rgb '#0060ad' title '1 iteration', \
'./data50.txt' using 1:3 axes x1y1 w linespoints pt 2 lc rgb '#118700' title '10 iterations', \
'./data50.txt' using 1:4 axes x1y1 w linespoints pt 8 lc rgb '#F5EA25' title '100 iterations', \
'./data50.txt' using 1:5 axes x1y1 w linespoints pt 6 lc rgb '#BD0002' title '1000 iterations', \
'./data50.txt' using 1:6 axes x1y1 w linespoints pt 3 lc rgb '#AA0002' title '10000 iterations', \
'./data50.txt' using 1:7 axes x1y1 w linespoints pt 6 lc rgb '#BD0002' title '100000 iterations'


set out "noise-100.pdf"
plot './data100.txt' using 1:2   axes x1y1 w linespoints pt 4 lc rgb '#0060ad' title '1 iteration', \
'./data100.txt' using 1:3 axes x1y1 w linespoints pt 2 lc rgb '#118700' title '10 iterations', \
'./data100.txt' using 1:4 axes x1y1 w linespoints pt 8 lc rgb '#F5EA25' title '100 iterations', \
'./data100.txt' using 1:5 axes x1y1 w linespoints pt 6 lc rgb '#BD0002' title '1000 iterations', \
'./data100.txt' using 1:6 axes x1y1 w linespoints pt 3 lc rgb '#AA0002' title '10000 iterations', \
'./data100.txt' using 1:7 axes x1y1 w linespoints pt 6 lc rgb '#BD0002' title '100000 iterations'


