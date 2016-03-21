package dao;

import dao.*;

import java.sql.ResultSet;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Properties;

import org.neo4j.jdbc.Driver;
import org.neo4j.jdbc.Neo4jConnection;

public class DAOKMeans{
    private DAOUser daoUser;
    private DAOVector daoVector;
    private DAOGroup daoGroup;
    private DAOCategory daoCategory;
    private Neo4jConnection connect;

    public DAOKMeans(DAOUser daoUser, DAOVector daoVector, DAOGroup daoGroup, DAOCategory daoCategory, Neo4jConnection connect){
        this.daoUser = daoUser;
        this.daoVector = daoVector;
        this.daoGroup = daoGroup;
        this.daoCategory = daoCategory;
        this.connect = connect;
    }

    /**
     * @author Laine B.
     * Remise à zéro des centres de groupe
     *
     * Cette méthode remet les centres des groupes à zéro.
     * Pour chaque groupe, on essaye de le placer sur un utilisateur tiré au hasard, tout en évitant que les groupes ne se superpose.
     * Si il n'y a pas assez d'utilisateur, les autres positions sont tirées au hasard, entre 0 et 1 pour chaque coordonnée, et de manière à ce que la somme soit égale à 1.
     */
    public void initialise(){
        List<String> users = daoUser.getUserNames();
        HashMap<String, Vector<Double>> userVector = daoVector.getVector("User");
        int nbGroups = daoGroup.getNbGroup();
        int nbCat = daoCategory.getNbCategories();

        List<String> userscp = new ArrayList<String>(users);

        for(int i=0; i<nbGroups; i++){
            if(userscp.size() > 0){
                int rand = (int)Math.floor(Math.random()*userscp.size());
                String user = userscp.get(rand);
                userscp.remove(rand);
                daoVector.setVectorGroupe("Group"+i, userVector.get(user));
            } else {
                List<Double> vect = new ArrayList<Double>();
                double tot=0;
                for(int j=0; j<nbCat; j++){
                    double rand = Math.random();
                    vect.add(rand);
                    tot+=rand;
                }
                for(int j=0; j<nbCat; j++)
                    vect.set(j, vect.get(j)/tot);
                daoVector.setVectorGroupe("Group"+i, vect);
            }
        }


        int i=0;
        for(String user : users){
            daoGroup.link(user, "Group"+i);
            i=(i+1)%nbGroups;
        }
    }
}
