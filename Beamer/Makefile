UML=$(wildcard src/graph/*.uml)
PNG=$(UML:.uml=.png)

TEX=$(wildcard src/*.tex)
PDF=$(TEX:.tex=.pdf,src/=doc/)

all: $(PNG) doc/Beamer.pdf pseudoclean

%.png: %.uml
	java -jar src/graph/plantuml.jar $<

doc/Beamer.pdf: src/Beamer.tex
	cd src;pdflatex Beamer.tex && pdflatex Beamer.tex && mv Beamer.pdf ../doc; cd ..

cleangraph:
	rm -rf src/graph/*.png

pseudoclean:
	rm -rf src/*.log src/*.aux src/*.toc src/*.out src/*.nav src/*.snm

clean: pseudoclean cleangraph
	rm -rf doc/*.pdf
