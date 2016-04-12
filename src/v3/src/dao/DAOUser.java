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

public class DAOUser{
    private DAOCategory daoCategory;
    private DAOGroup daoGroup;
    private Neo4jConnection connect;

    public DAOUser(DAOCategory daoCategory, Neo4jConnection connect){
        this.daoCategory = daoCategory;
        this.daoGroup = null;
        this.connect = connect;
    }
    public void setDaoGroup(DAOGroup daoGroup){
        this.daoGroup = daoGroup;
    }

    /**
     * @author Laine B.
     * Récupération liste d'utilisateur
     *
     * Cette méthode revoit une liste contenant les noms de tout les utilisateurs mis en mémoire.
     * @return Liste des noms d'utilisateur
     */
    public List<String> getUserNames(){
        try(ResultSet set = connect.createStatement().executeQuery("match (n:User) return n.name")){
            List<String> out = new ArrayList<String>();

            while(set.next()){
                out.add(set.getString("n.name"));
            }

            return out;
        } catch(Exception e){
            System.out.println(e);
            return new ArrayList<String>();
        }
    }
    /**
     * @author Laine B.
     * Récupération couple Groupe - nombre d'utilisateur
     *
     * Cette méthode retourne une table contenant pour chaque groupe le nombre d'utilisateurs qui lui sont affecté.
     * @return Table Groupe - nombre d'utilisateur
     */
    public HashMap<String, Integer> getNbUsersInGroups(){
        HashMap<String, Integer> out = new HashMap<String, Integer>();
        int nbGroups = daoGroup.getNbGroup();

        String query = "MATCH (g:Group) OPTIONAL MATCH (g)--(u:User) return g.name AS name, COUNT(u) AS cpt";
        try(ResultSet set = connect.createStatement().executeQuery(query)){
            while(set.next())
                out.put(set.getString("name"), set.getInt("cpt"));
            return out;
        } catch(Exception e){
            System.out.println(e);
            return new HashMap<String, Integer>();
        }
    }
    /**
     * @author Laine B.
     * Récupération du nombre de sessions d'un utilisateur.
     *
     * Cette méthode retourne le nombre de sessions qu'un utilisateur a parcouru.
     * Ce chiffre inclut aussi les sessions qui ont été supprimées.
     * @param user Nom de l'utilisateur
     * @return Nombre de sessions de l'utilisateur
     */
    public int getNbSessions(String user){
        try(ResultSet set = connect.createStatement().executeQuery("MATCH (u:User {name:\""+user+"\"}) RETURN u.nbSessions")){
            if(set.next())
                return set.getInt("u.nbSessions");
            return -1;
        } catch(Exception e){
            System.out.println(e);
            return -1;
        }
    }
    /**
     * @author Laine B.
     * Création "brute" d'un utilisateur
     *
     * Cette méthode crée un utilisateur sans vérifier si un utilisateur portant le même nom est déjà existant.
     * @param name Nom de l'utilisateur
     */
    public void addUserNotPresent(String name){
        int nbCategories = daoCategory.getNbCategories();
        String query = "create (:User {name: \""+name+"\" , nbSessions:0, vector: [";
        if(nbCategories>0){
            query += "toFloat(0)";
            for(int i=1; i<nbCategories; i++)
                query += ", toFloat(0)";
        }
        query += "]})";

        try(ResultSet set = connect.createStatement().executeQuery(query)){
            daoGroup.linkToClosestGroup(name);
        } catch(Exception e){
            System.out.println(e);
        }
    }
    /**
     * @author Laine B.
     * Augmentation du nombre de sessions d'un utilisateur
     *
     * Cette méthode incrémente le nombre de session d'un utilisateur donné.
     * Si ce dernier n'existe pas, rien n'est modifié.
     * @param user Nom de l'utilisateur
     */
    public void incrementNbSessionsUser(String user){
        String query = "MATCH (u:User {name:\""+user+"\"}) WITH u, u.nbSessions+1 AS nbSessions SET u.nbSessions = nbSessions";

        try(ResultSet set = connect.createStatement().executeQuery(query)){
        } catch(Exception e){
            System.out.println(e);
        }
    }
}
