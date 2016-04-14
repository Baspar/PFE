UML=$(wildcard src/ressources/graph/*.uml)
PNG=$(UML:.uml=.png)

all: $(PNG) texPFE mvdoc pseudoclean

%.png: %.uml
	java -jar src/ressources/graph/plantuml.jar $<

texPFE:
	cd src; pdflatex PFE.tex;pdflatex PFE.tex; cd ..

mvdoc:
	mv src/*.pdf doc/

pseudoclean:
	rm -rf src/*.log src/*.aux src/*.toc src/*.out

clean: pseudoclean
	rm -rf doc/*.pdf
