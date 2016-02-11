#include "MarkovNode.hpp"

MarkovNode::MarkovNode(){//DONE
}
MarkovNode::MarkovNode(const vector<int> chaine){//DONE
    total=0;
    maChaineDoc=chaine;
}
void MarkovNode::renforcerChaine(MarkovNode* nextChaine){//DONE
   //Incrementation total
    total++;

    //Incrementation occurence
    child[nextChaine]++;
}
vector<pair<int,float>> MarkovNode::guessNextDocs(){//DONE
    vector<pair<int, float> > out;

    for(pair<MarkovNode*, int> p : child){
        //Recuperation ID document suivant
        vector<int> chaineMarkovChild =p.first->getMaChaineDoc();
        int idDoc=chaineMarkovChild.back();

        //Calcul probabilite document suivant
        float prob=(float)p.second/(float)total;

        //Ajout a la liste
        out.push_back(make_pair(idDoc, prob));
    }
    return out;
}
vector<int> MarkovNode::getMaChaineDoc(){//DONE
    return maChaineDoc;
}
void MarkovNode::setTotal(int tot){//DONE
    total=tot;
}
void MarkovNode::setChild(MarkovNode* markovNode, int nb){//DONE
    child[markovNode]=nb;
}
int MarkovNode::getNbChild(){//DONE
    return child.size();
}
map<MarkovNode*, int> MarkovNode::getChild(){//DONE
    return child;
}
int MarkovNode::getTotal(){//DONE
    return total;
}
void MarkovNode::addChild(MarkovNode* markovNode, int poids){//DONE
    child[markovNode]=poids;
}
