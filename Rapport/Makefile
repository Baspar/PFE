all: texPFE mvdoc pseudoclean

texPFE:
	cd src; pdflatex PFE.tex;pdflatex PFE.tex; cd ..

mvdoc:
	mv src/*.pdf doc/

pseudoclean:
	rm -rf src/*.log src/*.aux src/*.toc src/*.out

clean: pseudoclean
	rm -rf doc/*.pdf
