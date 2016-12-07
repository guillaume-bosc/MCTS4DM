clear
reset

fileData = folder.'/length.data'
fileResult = folder.'/length_'.XP.'_'.dataset.'.pdf'
header = system('head -1 '.fileData);
N=words(header)

set terminal pdfcairo font "Arial,13.5" size 2.8,2 transparent
set out fileResult
set key inside right top

set linestyle 1 lw 1 pt 4 lc 3 lt 0

set bmarg 3
set tmarg 2
set xlabel "Strategies" offset 0,0.6
set ylabel "Description length"  offset 8,6.8 rotate by 0
set yrange [0:5.5]
set ytics nomirror out
set xtics nomirror out offset 0,0.3

if (N>10) {
	set xtics 0,5,N
	set for [i=0:N:5] xtics add ("(".(i+1).")" i)
} else {
	set xtics 0,1,N
	set for [i=0:N:1] xtics add ("(".(i+1).")" i)
}

set style fill solid 0.25 border lt -1
set style boxplot outliers pointtype 7
set style boxplot nooutliers
set pointsize 0.2


plot for [i=1:N] fileData using (i-1):i with boxplot notitle, \
	folder.'/outliersLength.data' with labels point pt 7 notitle
