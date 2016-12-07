#/bin/sh

SEARCH_FOLDER="./*"

for f in $SEARCH_FOLDER
do
	if [[ -d "$f" ]]
	then
		XPName=$(basename "$f")	
		echo "processing $f"
		for ff in $f/*
		do      
			if [[ -d "$ff" ]]
			then
				DatasetName=$(basename "$ff")
				echo "\tprocessing $ff"
				gnuplot -e "folder='$ff'; dataset='$DatasetName'; XP='$XPName'" runtime.plt
			fi
		done
	fi
done