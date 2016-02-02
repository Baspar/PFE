#!/bin/bash
i=0
file=$((RANDOM%10))
session="$file"

while [ "$file" != "" ]
do
    i=$((i+1))

    evince docs/"$file".pdf
    docs=$(echo "r $i $session q" | ./Main -v | tr ' ' '\n' | sort -r | sed 's/\(.*\)_\(.*\)_\(.*\)/\2 \1 \3/g' | tr '\n' ' ')
    file=$(zenity --list --text="Voici les document trouvé suite à votre session ($session)" --column="N° documents" --column="Probabilité" --column="Issu de la markov d'ordre" $(echo $docs))

    session="$session $file"
done
echo "s $i $session q" | ./Main -v
