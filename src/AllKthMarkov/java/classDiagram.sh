#!/bin/bash
constr=""
meth=""
getter=""
setter=""
attribut=""

compute() {
    line="$1"
    isAFunction=$(echo "$line" | grep "(" | sed '/^$/d')
    if ! [[ "$isAFunction" ]]
    then
        attrType=$(getAttrType "$line")

        attrOut=$(echo "$line" | cut -d ' ' -f 2)
        attrName=$(echo "$line" | cut -d ' ' -f 3)

        attribut="$attribut        $attrType $attrName : $attrOut\n"
    else
        state=$(echo $line |grep "//" | sed "s/.*\/\/\(.*\)/\1/g")
        ligne=$(echo $line | sed "s/\/\/.*//g")

        #Constructor case
        etat="+"
        out=$(echo "$ligne"  | cut -d "(" -f 1 | tr ' ' '\n' | tail -n 2 | head -n 1)
        nomFonction=$(echo "$ligne"  | cut -d "(" -f 1 | tr ' ' '\n' | tail -n 1)
        fonction=$(echo "$ligne" | sed "s/.*$out \(.*\)/\1/1")

        #"Abstract function" case
        if [[ $(echo "$ligne" | sed "s/$fonction//g" | grep abstract) != "" ]]
        then
            fonction="{abstract}$fonction"
        fi

        #Progress detection
        color=$(getFunctionColor "$state" "$fonction")
        fonction="<color:$color>$fonction"
        out="$out</color>"

        #Append function to its correct list
        sortFunction "$etat" "$fonction" "$out" "$name"
    fi
}
printFile() {
    classType="$1"
    name="$2"
    attribut="$3"
    contr="$4"
    setter="$5"
    getter="$6"
    meth="$7"
    md5sumJava="$8"

    echo "@startuml" > uml/Classe$name.uml
    echo "$classType $name{" >> uml/Classe$name.uml

    if [ "$attribut" != "" ]
    then
        if [[ $classType != "enum" ]]
        then
            echo "    ==<b>Attributes</b>==" >> uml/Classe$name.uml
            echo -en "$attribut" | sed 's/\\\*/\*/g'|sort >> uml/Classe$name.uml
        else
            echo -en "$attribut" | sed 's/\\\*/\*/g'>> uml/Classe$name.uml
        fi
    fi
    if [ "$constr" != "" ]
    then
        echo "    ==<b>Constructors</b>==" >> uml/Classe$name.uml
        echo -en "$constr" | sed 's/\\\*/\*/g'|sort >> uml/Classe$name.uml
    fi
    if [ "$setter" != "" ]
    then
        echo "    ==<b>Setters</b>==" >> uml/Classe$name.uml
        echo -en "$setter" | sed 's/\\\*/\*/g'|sort >> uml/Classe$name.uml
    fi
    if [ "$getter" != "" ]
    then
        echo "    ==<b>Getters</b>==" >> uml/Classe$name.uml
        echo -en "$getter" | sed 's/\\\*/\*/g'|sort >> uml/Classe$name.uml
    fi
    if [ "$meth" != "" ]
    then
        echo "    ==<b>Methods</b>==" >> uml/Classe$name.uml
        echo -en "$meth" | sed 's/\\\*/\*/g'|sort >> uml/Classe$name.uml
    fi

    echo "}" >> uml/Classe$name.uml
    echo "@enduml" >> uml/Classe$name.uml
    echo "'$md5sumJava" >> uml/Classe$name.uml
}
sortFunction() {
    nomFonction=$(echo "$fonction" | sed "s/.*>\(.*\)(.*/\1/g")
    if [ "$(echo $nomFonction | egrep "^$4")" ]
    then
        constr="$constr        $1$2</color>\n"
    elif [ "$(echo $nomFonction | grep -e "^set")" ]
    then
        setter="$setter        $1$2 : $3\n"
    elif [ "$(echo $nomFonction| grep -e "^get")" ]
    then
        getter="$getter        $1$2 : $3\n"
    else
        meth="$meth        $1$2 : $3\n"
    fi
}
getFunctionColor() {
    if [[ $(echo "$2" | grep abstract ) != "" ]] 
    then
        color="grey"
    elif [[ "$1" == "" ]]
    then
        echoerr "  No progress for $2"
        color="black"
    elif [[ "$1" == "TODO" ]]
    then
        color="red"
    elif [[ "$1" == "WIP" ]]
    then
        color="orange"
    elif [[ "$1" == "DONE" ]]
    then
        color="green"
    elif [[ "$1" == "CHK" ]]
    then
        color="brown"
    else
        color="white"
        echoerr "    Error with method $2"
    fi
    echo $color
}
echoerr() { echo "$@" 1>&2; }

getAttrType() {
    out=$(echo "$1" | cut -d ' ' -f 1)
    if [[ $out == "protected" ]]
    then
        out='#'
    elif [[ $out == "private" ]]
    then
        out='-'
    else
        out='+'
    fi
    echo $out
}
getClassType() {
    isAbstract=$(echo "$1"| grep "abstract")
    isInterface=$(echo "$1" | grep "interface")
    isEnum=$(echo "$1" | grep "enum")
    if [[ $isAbstract ]]
    then
        out="abstract"
    elif [[ $isInterface ]]
    then
        out="interface"
    elif [[ $isEnum ]]
    then
        out="enum"
    else
        out="class"
    fi
    echo "$out"
}
preprocessTxt() {
    newFile="$1"

    #Main preprocess
    newFile="$(echo "$newFile"  | sed "s/\/\*[.\n]*?\*\///g;
                                        s/^[\t ]*//g;"\
                                | grep "^public\|^private\|^protected" \
                                | sed " s/{//g;
                                        s/\/\/[^\(TODO\|CHK\|WIP\|DONE\)].*//g;
                                        s/;//g;"
                                        )"

    echo -e "$newFile"
}
isAlreadyTested() {
    name=$(basename -s .java $1)
    if [ -e uml/Classe$name.uml ]
    then
        dejaFait=$(cat uml/Classe$name.uml | tail -n 2 | tr "\n" " " | grep "'$2")
    else
        dejaFait=""
    fi
    echo "$dejaFait"
}


rm -rf uml/Classes.uml Classes.png
[ "$1" == "clean" ] && rm -f uml/Classe*.uml
[ "$1" == "clear" ] && rm -f uml/Classe*.uml && echo "UML files deleted" && exit 0

echo "@startuml" >> uml/Classes.uml
echo "!include skin.uml" >> uml/Classes.uml
echo "!include links.uml" >> uml/Classes.uml


listeFichier=$(ls src/*.java | grep -v "Pair.java")

echo $listeFichier

nbFichier=$(echo $listeFichier | wc -w)
indic=0

echo "Java file analysis in progress"
for javaFile in $listeFichier
do
    name=$(basename -s .java $javaFile) # Nom du fichier sans extenstion
    myFile=$(preprocessTxt "$(cat "$javaFile")")
    md5sumJava=$(echo "$myFile" | md5sum)
    echo "!include Classe$name.uml"  >> uml/Classes.uml

    indic=$(($indic + 1)) # Numero du fichier traité
    if [ $indic -lt 10 ]
    then
        echoerr -n "[ $indic/$nbFichier]" #debug
    else
        echoerr -n "[$indic/$nbFichier]" #debug
    fi

    dejaFait=$(isAlreadyTested "$javaFile" "$md5sumJava")


    if [[ "$dejaFait" ]]
    then
        echoerr "  (-)  Class $name"
    else
        echoerr "  (O)  Class $name" # Debug
        rm -f uml/Classe$name.uml

        #Get the file content, and get its header and its declarations
        header=$(cat "$javaFile" | grep "class \+$name\|enum \+$name" | sed "s/\(class \)\?\+$name.*//g")
        myFile=$(echo "$myFile" | grep -v "class\|enum"  | sed '/^}$/d' )

        #Is the class abstract/an interface ?
        classType=$(getClassType "$header")

        constr=""
        meth=""
        getter=""
        setter=""
        attribut=""

        [[ "$myFile" ]] && while read line
        do
            #Sort every line(=Attribute/Method) in its corresponding group
            if [[ $classType == "enum" ]]
            then
                attribut="$attribut        $line\n"
            else
                compute "$line"
            fi
        done <<< "$(echo "$myFile")"

        printFile "$classType" "$name" "$attribut" "$contr" "$setter" "$getter" "$meth" "$md5sumJava"
    fi
done

echo "@enduml" >> uml/Classes.uml
echo "UML created. PNG rendering in progress"
java -jar uml/plantuml.jar uml/Classes.uml
mv uml/Classes.png .

[ -e Classes.png ] && eog Classes.png || xdg-open Classes.png
