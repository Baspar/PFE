#include <iostream>
#include <fstream>
#include <vector>

#include "AllKthMarkov.hpp"
#include "KthMarkov.hpp"
#include "MarkovNode.hpp"

using namespace std;

void affichageNextDocs(vector<vector<pair<int, float>>> out, vector<int> session){
    while(session.size()>4)
        session.erase(session.begin());

    for(int k=session.size()-1; k>=0; k--){
        vector<pair<int, float>> listeDocs = out[4-1-k];
        cout << "|=> Markov d'odre " << k << " (" ;
        for(int doc : session)
            cout << doc << ",";
        cout << "):" << endl;
        session.erase(session.begin());

        for(pair<int, float> doc:listeDocs)
            cout << "    |=> Document #" << doc.first << " [P=" << doc.second << "]" << endl;

    }
}
int main(int argc, char* argv[]){
    int K=4;

    bool verbose=true;
    if(argc>1)
        if(argv[1][0]=='-' && argv[1][1]=='v')
            verbose=false;


    AllKthMarkov markovs("save");

    vector<int> session;
    int nbDoc;
    char operation=' ';
    do{
        if(verbose)
            cout << endl << "Rentrez une operation ([r]equete, [s]ession, [p]lantuml, [q]uitter): ";
        cin >> operation;
        switch (operation) {
            case 's':
                        if(verbose)
                            cout << "Rentrez la longueur de votre session: ";
                        cin >> nbDoc;
                        session.resize(nbDoc);
                        if(verbose)
                            cout << "Rentrez les differents documents de votre sessions:" << endl;
                        for(int i=0; i<nbDoc; i++){
                            if(verbose)
                                cout << "  "<< i << ": ";
                            cin >> session[i];
                        }
                        markovs.ajouterSession(session);
                        break;
            case 'p':
                        if(verbose)
                            cout << "Generation des diagrammes UML..." << endl;
                        for(int i=1; i<=K; i++){
                            if(verbose)
                                cout << "  Diagramme markov " << i << "...";
                            ofstream fichierUML("../diagrammes/diag"+to_string(i)+".uml", ios::out | ios::trunc);
                            if(fichierUML){
                                fichierUML << "digraph markov"+to_string(i)+"{" << endl;
                                    fichierUML<< markovs.getMarkov(i)->toUML() << endl;
                                fichierUML << "}" << endl;
                                fichierUML.close();
                            }
                            if(verbose)
                                cout << " OK" << endl;
                        }
                        break;
            case 'r':
                        if(verbose)
                            cout << "Rentrez la longueur de votre session: ";
                        cin >> nbDoc;
                        session.resize(nbDoc);
                        if(verbose)
                            cout << "Rentrez les differents documents de votre sessions:" << endl;
                        for(int i=0; i<nbDoc; i++){
                            if(verbose)
                                cout << "  "<< i << ": ";
                            cin >> session[i];
                        }
                        vector<vector<pair<int, float>>> out=markovs.guessNextDocs(session);
                        affichageNextDocs(out, session);

                        break;
        }
    } while(operation!='q');

    ofstream fichierSave("save", ios::out | ios::trunc);
    fichierSave << "Format du fichier:" << endl;
    fichierSave << ">> ordreDeLaAllKthMarkovUtilisee" << endl;
    fichierSave << ">> nombreDeNoeudMarkov0 nombreDeLienMarkov0" << endl;
    fichierSave << ">> noeud1 poidsNoeud1" << endl;
    fichierSave << ">> noeud2 poidsNoeud2" << endl;
    fichierSave << ">> ..." << endl;
    fichierSave << ">> noeudI poidsLiaison1 noeudJ" << endl;
    fichierSave << ">> noeudK poidsLiaison2 noeudL" << endl;
    fichierSave << ">> ..." << endl;
    fichierSave << ">> nombreDeNoeudMarkov1 nombreDeLienMarkov1" << endl;
    fichierSave << ">> ..." << endl;
    fichierSave << ">> " << endl;
    fichierSave << endl;
    fichierSave << endl;

    for(int i=1; i<=K; i++)
        fichierSave << markovs.getMarkov(i)->toSave() << endl;
    fichierSave.close();

    return 0;
}
