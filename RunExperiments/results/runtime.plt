clear
reset

fileData = folder.'/runtime.data'
fileResult = folder.'/runtime_'.XP.'_'.dataset.'.pdf'
N=system('wc -l '.fileData);

set terminal pdfcairo font "Arial,13.5" size 2.8,2 transparent
set out fileResult
set key inside right top

set linestyle 1 lw 1 pt 4 lc 3 lt 0

set bmarg 3
set tmarg 2
set xlabel "Strategies" offset 0,0.6
set ylabel "Runtime (ms)"  offset 7,6.8 rotate by 0
set yrange [0:]
set ytics nomirror out
set xtics nomirror out offset 0,0.3

if (N>10) {
	set xtics 0,5,N
	set for [i=0:N:5] xtics add ("(".(i+1).")" i)
}

set boxwidth 0.8 relative
set style fill solid 0.25 border

if (N>10) {
	plot fileData using 2 w boxes notitle linecolor rgb "#0060ad"
} else {
	plot fileData using 2:xticlabels(3) w boxes notitle linecolor rgb "#0060ad"
}


