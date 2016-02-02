#ifndef ALLKTHMARKOV_HPP
#define ALLKTHMARKOV_HPP

#include <iostream>
#include <vector>

#include "KthMarkov.hpp"

using namespace std;

class AllKthMarkov{
    private:
        int K;
        vector<KthMarkov> markovs;
        void ajouterDocs(int K, vector<int> docs, int nextDoc);
    public:
        AllKthMarkov(string fileName);
        void ajouterSession(vector<int> session);
        vector<vector<pair<int,float>>> guessNextDocs(vector<int> session);
        KthMarkov* getMarkov(int i);
};

#endif
