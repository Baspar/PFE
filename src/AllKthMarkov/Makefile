GCC=g++ --std=c++11

all: main pseudoclean

main: src/AllKthMarkov.o src/KthMarkov.o src/MarkovNode.o
	$(GCC) src/Main.cpp $^ -o bin/Main

%.o:%.cpp
	$(GCC) -c $< && mv *.o src/

pseudoclean:
	rm -f src/*.o

clean: pseudoclean
	rm -f bin/Main bin/d bin/save diagrammes/diag*
