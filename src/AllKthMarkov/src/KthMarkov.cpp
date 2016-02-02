#include "KthMarkov.hpp"

void KthMarkov::addNewNode(vector<int> node, int poidsNode){//DONE
    if(getPDocs(node) == nullptr){
        nodes[node]=MarkovNode(node);
        nodes[node].setTotal(poidsNode);
    }
}
MarkovNode* KthMarkov::getPDocs(vector<int> docs){//DONE
    if(nodes.find(docs) == nodes.end())
        return nullptr;
    return &nodes[docs];
}
void KthMarkov::renforcerChaine(vector<int> oldDocs, int nextDoc){//DONE
    //Calcul chaine nouveaux documents
    vector<int> newDocs(oldDocs);
    newDocs.erase(newDocs.begin());
    newDocs.push_back(nextDoc);

    //Recuperation adresse oldMarkovNode
    if(getPDocs(oldDocs) == nullptr)
        nodes[oldDocs]=MarkovNode(oldDocs);
    MarkovNode* oldMarkovNode = &(nodes[oldDocs]);

    //Recuperation adresse newMarkovNode
    if(getPDocs(newDocs) == nullptr)
        nodes[newDocs]=MarkovNode(newDocs);
    MarkovNode* newMarkovNode = &(nodes[newDocs]);

    //Renforcement chaine
    oldMarkovNode->renforcerChaine(newMarkovNode);
}
string KthMarkov::toSave(){//DONE
    //Affiochage nombre de noeuds
    string out=to_string(nodes.size());
    out+=" ";

    //Affichage nombre de liens
    int nbLiens=0;
    for(pair<vector<int>, MarkovNode> node: nodes)
        nbLiens+=node.second.getNbChild();
    out+=to_string(nbLiens);
    out+="\n";

    //Affichage noeuds
    for(pair<vector<int>, MarkovNode> node: nodes){
        for(int doc:node.first)
            out += to_string(doc)+" ";
        out += "["+to_string(node.second.getTotal())+"]";
        //out += to_string(node.second.getTotal());
        out += "\n";
    }

    //Affichage liens
    for(pair<vector<int>, MarkovNode> node: nodes){
        for(pair<MarkovNode*, int> child: node.second.getChild()){
            for(int doc:node.first)
                out+=to_string(doc)+" ";
            out+="-("+to_string(child.second)+")- ";
            //out+=to_string(child.second)+" ";
            for(int doc:child.first->getMaChaineDoc())
                out+=to_string(doc)+" ";
            out+="\n";
        }
    }

    return out;
}
string KthMarkov::toUML(){//DONE
    //Affiochage nombre de noeuds
    string out="";

    //Affichage liens
    for(pair<vector<int>, MarkovNode> node: nodes){
        for(pair<MarkovNode*, int> child: node.second.getChild()){
            out += "  <";

            for(int doc:node.first)
                out+=to_string(doc)+",";

            out += "> -> <";

            for(int doc:child.first->getMaChaineDoc())
                out+=to_string(doc)+",";

            out += "> [label=\""+to_string((float)child.second/(float)node.second.getTotal())+"\"]\n";
        }
    }

    return out;
}
