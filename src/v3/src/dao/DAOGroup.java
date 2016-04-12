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

public class DAOGroup{
    private DAOOther daoOther;
    private DAOUser daoUser;
    private DAOVector daoVector;
    private Neo4jConnection connect;

    public DAOGroup(DAOOther daoOther, DAOUser daoUser, DAOVector daoVector, Neo4jConnection connect){
        this.daoOther = daoOther;
        this.daoUser = daoUser;
        this.daoVector = daoVector;
        this.connect = connect;
    }

    public void setDaoVector(DAOVector daoVector){
        this.daoVector = daoVector;
    }

    /**
     * @author Laine B.
     * Création de lien Groupe -> User
     *
     * Cette méthode crée un lien, mais ne vérifie pas si un lien identique est déjà existant.
     * Aucune vérification n'est faite sur l'existence de l'utilisateur et du groupe
     * @param userName Nom de l'utilisateur
     * @param group Nom du groupe ("Groupe<num>")
     */
    public void link(String userName, String group){
        try(ResultSet set = connect.createStatement().executeQuery("MATCH (g:Group {name:\""+group+"\"}), (u:User {name:\""+userName+"\"}) CREATE (g)-[:CONTAINS]->(u)")){
        } catch(Exception e){
            System.out.println(e);
        }
    }
    /**
     * @author Laine B.
     * Supprime tous les arcs de type [:CONTAINS]
     *
     * Cette méthode supprime tout les arcs liants deux nœuds de Markov.
     */
    public void deleteEveryContains(){
        String query = "MATCH ()-[rel:CONTAINS]-() DELETE rel";
        try(ResultSet set = connect.createStatement().executeQuery(query)){
        } catch(Exception e){
            System.out.println(e);
        }
    }
    /**
     * @author Laine B.
     * Deconnecte l'utilisateur de tout groupe
     * 
     * Cette méthode deconnecte l'utilisaterur de tout les groupe
     * @param user Nom de l'utilisateur
     */
    public void unlink(String user){
        String query = "MATCH (:User{name:\""+user+"\"})-[rel]-(:Group) DELETE rel";
        try(ResultSet set = connect.createStatement().executeQuery(query)){
        } catch(Exception e){
            System.out.println(e);
        }
    }

    /** TODO
     * @author Laine B.
     * Liaison de l'utilisateur à son groupe le plus proche
     *
     * Cette méthode trouve le groupe le plus proche de l'utilisateur et le relie.
     * Aucune vérification n'est faite sur l'existence de l'utilisateur
     * @param user Nom de l'utilisateur
     */
    public void linkToClosestGroup(String user){
        HashMap<String, Vector<Double>> groupVector = daoVector.getVector("Group");
        HashMap<String, Integer> nbUsersInGroups = daoUser.getNbUsersInGroups();
        Vector<Double> userVector = daoVector.getVector("User").get(user);

        unlink(user);


        String groupMin = "";
        double distMin = -1.;
        int nbUsers = 0;

        for(Map.Entry<String, Vector<Double>> ent : groupVector.entrySet()){
            double newDist = distance(userVector, ent.getValue());
            int newNbUsers = nbUsersInGroups.get(ent.getKey());
            if      (distMin == -1 // Premier groupe
                    || newDist < distMin // Nouveau groupe plus proche
                    || (newDist == distMin && newNbUsers < nbUsers) //Groupe aussi proche mais moins rempli
                    ){
                distMin = newDist;
                groupMin = ent.getKey();
                nbUsers = newNbUsers;
                    }
        }

        link(user, groupMin);
    }
    /**
     * @author Laine B.
     * Récupération du nombre de groupes.
     *
     * Cette méthode retourne le nombre de groupes présents en base de donnée.
     * @return Nombre de groupes.
     */
    public Integer getNbGroup(){
        return daoOther.getNbClass("Group");
    }
    /**
     * @author Laine B.
     * Calcul de distance entre deux vecteurs
     *
     * Retourne la distance entre deux vecteurs de Double.
     * @param i1 Vecteur 1
     * @param i2 Vecteur 2
     * @return Distance entre les deux vecteurs.
     */
    public double distance(Vector<Double> i1, Vector<Double> i2){
        double out = 0.;
        for(int i=0; i<i1.size(); i++)
            out += Math.pow( (i1.get(i)-i2.get(i)), 2);
        return out;
    }
}
