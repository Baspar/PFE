#include "AllKthMarkov.hpp"

#include <fstream>

AllKthMarkov::AllKthMarkov(string fileName){//DONE
    ifstream fichierSave(fileName, ios::in);

    markovs.resize(4);
    K=4;

    if(fichierSave){
        for(int i=0; i<14; i++)
            fichierSave.ignore(256, '\n');

        for(int i=0; i<K; i++){
            //Lecture nbNode/Liens
            int nbNodes, nbLiens;
            fichierSave >> nbNodes >> nbLiens;

            //Creation des noeuds
            for(int j=0; j<nbNodes; j++){
                vector<int> node(i+1);
                int poidsNode;

                //Lecture Noeud
                for(int k=0; k<node.size(); k++)
                    fichierSave >> node[k];

                //Lecture Poids du noeud
                fichierSave.ignore(2);
                fichierSave >> poidsNode;
                fichierSave.ignore(2);

                //Insertion du noeud
                markovs[i].addNewNode(node, poidsNode);
            }

            //Creation des liens
            for(int j=0; j<nbLiens; j++){
                vector<int> node1(i+1);
                int poids;
                vector<int> node2(i+1);

                //Lecture noeud initial
                for(int k=0; k<node1.size(); k++)
                    fichierSave >> node1[k];

                //Lecture poids arc
                fichierSave.ignore(3);
                fichierSave >> poids;
                fichierSave.ignore(3);

                //Lecture noeud final
                for(int k=0; k<node2.size(); k++)
                    fichierSave >> node2[k];

                //Recuperation adresses noeuds
                MarkovNode* pNode1=markovs[i].getPDocs(node1);
                MarkovNode* pNode2=markovs[i].getPDocs(node2);

                //Insertion lien
                pNode1->addChild(pNode2, poids);
            }
        }

        fichierSave.close();
    }
}
void AllKthMarkov::ajouterDocs(int K, vector<int> docs, int nextDoc){//DONE
    markovs[K-1].renforcerChaine(docs, nextDoc);
}
void AllKthMarkov::ajouterSession(vector<int> session){//DONE
    //La session sera de plus grande taille que K
    vector<vector<int> > oldDocs(K);
    vector<int> nextDocs(K, -1);


    for(int doc:session){
        for(int k=1; k<K+1; k++){
            //Le "nextDoc" precedent devient le dernier "oldDoc"
            if(nextDocs[k-1]!=-1)
                oldDocs[k-1].push_back(nextDocs[k-1]);

            //On met le nouveau "nextDoc"
            nextDocs[k-1]=doc;

            //Si "oldDocs" est trop grand, on retire le premier
            if(oldDocs[k-1].size() > k)
                oldDocs[k-1].erase(oldDocs[k-1].begin());

            //Si "oldDocs" est suffisament grand, on l'ajoute aux markovs
            if(oldDocs[k-1].size() == k)
                ajouterDocs(k, oldDocs[k-1], nextDocs[k-1]);
        }
    }
}
vector<vector<pair<int,float>>> AllKthMarkov::guessNextDocs(vector<int> session){//DONE
    //Debug
    cout << "Votre session est (";
    for(int i:session)
        cout << i << ",";
    cout << ")" << endl;

    vector<vector<pair<int,float> > > out;

    //On ne recupère que les K+1 derniers documents
    while(session.size()>K)
        session.erase(session.begin());

    //Boucle sur les documents restants
    //for(int i=session.size()-1; i>=0; i--){
    for(int i=K-1; i>=0; i--){
        if(i<session.size()){
            MarkovNode* markovNode = markovs[i].getPDocs(session);
            if(markovNode != nullptr)
                out.push_back(markovNode->guessNextDocs());
            else
                out.push_back(vector<pair<int,float>>());
            session.erase(session.begin());
        } else {
            out.push_back(vector<pair<int,float>>());
        }
    }

    return out;
}
KthMarkov* AllKthMarkov::getMarkov(int i){//DONE
    return &(markovs[i-1]);
}
