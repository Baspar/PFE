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

public class DAOVector{
    private DAOUser daoUser;
    private DAOCategory daoCategory;
    private DAOGroup daoGroup;
    private Neo4jConnection connect;
    private double pourcentageMinUserVector;

    public DAOVector(DAOUser daoUser, DAOCategory daoCategory, DAOGroup daoGroup, Neo4jConnection connect, double pourcentageMinUserVector){
        this.daoUser = daoUser;
        this.daoCategory = daoCategory;
        this.daoGroup = daoGroup;
        this.connect = connect;
        this.pourcentageMinUserVector = pourcentageMinUserVector;
    }

    /**
     * @author Laine B.
     * Récupération vecteurs d'une catégorie
     *
     * Cette méthode retourne une table qui a chaque entité de la catégorie fait correspondre son vecteur.
     * Les catégories peuvent être "User" ou "Group"
     * @param cat Le nom de la catégorie
     * @return Table de Nom - vecteur
     */
    public HashMap<String, Vector<Double>> getVector(String cat){
        HashMap<String, Vector<Double>> out = new HashMap<String, Vector<Double>>();
        int nbCat = daoCategory.getNbCategories();

        String query = "MATCH (n:"+cat+") RETURN ";
        for(int i=0; i<nbCat; i++)
            query += "n.vector["+i+"] AS vector"+i+", ";
        query += "n.name AS name";

        try(ResultSet set = connect.createStatement().executeQuery(query)){
            while(set.next()){
                String name = set.getString("name");

                out.put(name, new Vector<Double>());

                for(int i=0; i<nbCat; i++)
                    out.get(name).add(set.getDouble("vector"+i));
            }
        } catch(Exception e){
            System.out.println(e);
            return new HashMap<String, Vector<Double>>();
        }

        return out;
    }

    /**
     * @author Laine B.
     * Remise à niveau des vecteurs utilisateur et groupe.
     *
     * Cette méthode va ajouter une dimension supplémentaire aux vecteurs utilisateur et de chaque groupe.
     * Cette coordonnée sera mise par défaut à zéro.
     * Cette méthode est appelée à chaque ajout de catégorie.
     */
    public void resizeVectors(){
        String query = "MATCH (n) WHERE n:User OR n:Group SET n.vector = n.vector + toFloat(0)";

        try(ResultSet set = connect.createStatement().executeQuery(query)){
        } catch(Exception e){
            System.out.println(e);
        }
    }
    /**
     * @author Laine B.
     * Mise a jour du vecteur utilisateur.
     *
     * Cette méthode va mettre à jour le vecteur utilisateur en prenant en considération le pourcentageMinUserVector.
     * D'abord, elle calculera le pourcentage qu'est censé représenter la nouvelle session dans le vecteur (Si l'utilisateur a déjà fait X sessions, le pourcentage sera 1/(X+1)%)
     * Si ce pourcentage est inférieur au pourcentageMinUserVector, on prend ce dernier. Sinon, on garde le pourcentage classique.
     * @param user Nom de l'utilisateur
     * @param session Session/liste de documents
     */
    public void updateUserVector(String user, List<String> session){
        int nbSessions = daoUser.getNbSessions(user);
        int nbCategories = daoCategory.getNbCategories();

        int tailleSession = session.size();
        double pourcentageNew = Math.max( ((double)1)/(nbSessions+1), pourcentageMinUserVector);
        double pourcentageOld = 1-pourcentageNew;

        Vector<Double> sessionVector = new Vector<Double>();
        for(int i=0; i<nbCategories; i++)
            sessionVector.add(0.);

        for(String doc : session){
            int i=daoCategory.getCategorie(doc);
            sessionVector.set(i, sessionVector.get(i)+1);
        }

        for(int i=0; i<nbCategories; i++)
            sessionVector.set(i, sessionVector.get(i)/tailleSession);

        String query = "MATCH (u:User {name:\""+user+"\"}) SET u.vector = [ u.vector[0]*"+pourcentageOld+"+"+sessionVector.get(0)+"*"+pourcentageNew;
        for(int i=1; i<nbCategories; i++)
            query += ", u.vector["+i+"]*"+pourcentageOld+"+"+sessionVector.get(i)+"*"+pourcentageNew;
        query += "]";

        try(ResultSet set = connect.createStatement().executeQuery(query)){
        } catch(Exception e){
            System.out.println(e);
        }
    }
    /**
     * @author Laine B.
     * Setter du vecteur de groupe.
     *
     * Cette méthode remplace le vecteur du groupe par le vecteur souhaité.
     * @param group Nom du groupe
     * @param vector Vecteur du groupe
     */
    public void setVectorGroupe(String group, List<Double> vector){
        String query = "MATCH (g:Group {name:\""+group+"\"}) SET g.vector = [";
        if(vector.size() > 0){
            query += vector.get(0);
            for(int i=1; i<vector.size(); i++)
                query += ", "+vector.get(i);
        }
        query += "]";
        try(ResultSet set = connect.createStatement().executeQuery(query)){
        } catch(Exception e){
            System.out.println(e);
        }
    }
    /**
     * @author Laine B.
     * Calcul du vecteur de groupe.
     *
     * Cette méthode met à jour le vecteur du groupe, comme étant la moyenne des vecteur des utilisateurs inclus dans le groupe.
     * @param group Nom du groupe
     */
    public void calculVectorGroupe(String group){
        int nbCategories = daoCategory.getNbCategories();

        String query = "MATCH (g:Group {name:\""+group+"\"})-[]-(u:User) WITH g";
        if(nbCategories > 0){
            query += ", avg(u.vector[0]) AS a0";
            for(int i=1; i<nbCategories; i++)
                query += ", avg(u.vector["+i+"]) AS a"+i;
        }
        query += " SET g.vector = [";
        if(nbCategories > 0){
            query += "a0";
            for(int i=1; i<nbCategories; i++)
                query += ", a"+i;
        }
        query += "]";

        try(ResultSet set = connect.createStatement().executeQuery(query)){
        } catch(Exception e){
            System.out.println(e);
        }
    }
    /**
     * @author Laine B.
     * Calcul du vecteur des groupes.
     *
     * Cette méthode met à jour le vecteur de chacun des groupes.
     * Une boucle est faite avec la méthode calculVec(String);
     */
    public void calculVectorGroupes(){
        int nbGroup = daoGroup.getNbGroup();
        for(int i=0; i<nbGroup; i++)
            calculVectorGroupe("Group"+i);
    }
}
