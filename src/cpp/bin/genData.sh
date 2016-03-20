#!/bin/bash

echo -n "" > d
docs="0 1 2 3 4 5 6 7 8 9"
for i in {1..100}
do
    docs=$(echo -n $docs | tr ' ' '\n' | sort -R | tr '\n' ' ')
    L=$((RANDOM%4+6))

    echo -n "s ">>d
    echo -n "$L " >>d
    echo -n $docs | tr ' ' '\n'  | head -n $L | tr '\n' ' '  >> d
    echo >> d
done
echo "q">> d
