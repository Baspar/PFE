#ifndef KTHMARKOV_HPP
#define KTHMARKOV_HPP

#include <iostream>
#include <vector>
#include <map>

#include "MarkovNode.hpp"

using namespace std;

class KthMarkov{
    private:
        map<vector<int>, MarkovNode> nodes;

    public:
        MarkovNode* getPDocs(vector<int> docs);
        void renforcerChaine(vector<int> oldDocs, int nextDoc);
        string toSave();
        string toUML();
        void addNewNode(vector<int> node, int poidsNode);
};
#endif
