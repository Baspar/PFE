#ifndef MARKOVNODE_HPP
#define MARKOVNODE_HPP

#include <iostream>
#include <vector>
#include <map>

using namespace std;

class MarkovNode{
    private:
        vector<int> maChaineDoc;
        map<MarkovNode*,int> child;
        int total;
    public:
        MarkovNode(const vector<int> chaine);
        MarkovNode();
        void renforcerChaine(MarkovNode* nextChaine);
        vector<pair<int,float>> guessNextDocs();
        vector<int> getMaChaineDoc();
        void setTotal(int tot);
        void setChild(MarkovNode* markovNode, int nb);
        int getNbChild();
        map<MarkovNode*, int> getChild();
        int getTotal();
        void addChild(MarkovNode* markovNode, int poids);
};
#endif
