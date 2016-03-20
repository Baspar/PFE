#ifndef USER_HPP
#define USER_HPP

#include <vector>

#include "AllKthMarkov.hpp"

class User{
    public:
        AllKthMarkov myMarkovs;
        vector<Session> sessions;
    private:
};

#endif
