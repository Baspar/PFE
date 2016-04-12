#!/bin/bash
[ "$1"=="" ] && nb=10 || nb=$1

i=0
while [ $i -lt $nb ]
do
    cat ex.tex | sed "s/<<I>>/$i/g" > "$i".tex
    pdflatex "$i".tex
    rm "$i".tex "$i".log "$i".aux
    i=$((i+1))
done
