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

public class DAOMarkov{
    private Neo4jConnection connect;
    int dureeDeVie;

    public DAOMarkov(Neo4jConnection connect, int dureeDeVie){
        this.connect = connect;
        this.dureeDeVie = dureeDeVie;
    }

    /**
     * @author Laine B.
     * Test de l'existence d'un nœud de Markov
     *
     * Cette méthode vérifie si un nœud existe déjà dans le All-Kth-Markov d'un Utilisateur
     * @param K Ordre de la chaîne de Markov
     * @param user Nom de l'utilisateur
     * @param docs Liste des documents du nœud
     * @return Existence du nœud
     */
    public boolean nodeExists(int K, String user, List<String> docs){
        String query = "match (u:User {name:\""+user+"\"})-[:HAS]->(n:Markov"+K+" {docs:[\""+docs.get(0)+"\"";
        for(int i=1; i<docs.size(); i++)
            query += ", \""+docs.get(i)+"\"";
        query += "]}) return count(n)";

        try(ResultSet set = connect.createStatement().executeQuery(query)){
            int i=0;
            while(set.next())
                i+=set.getInt("count(n)");
            return (i>=1);
        } catch(Exception e){
            System.out.println(e);
            return false;
        }
    }
    /**
     * @author Laine B.
     * Liaison de deux nœuds de Markov
     *
     * Cette méthode crée un lien [:NEXT] entre deux Markov pour l'utilisateur mentionné.
     * La valeur de "fins" sera initialisée au nombre de sessions de l'utilisateur additionnée à la durée de vie d'une session.
     * Aucune vérification n'est faite sur l'existence des noeuds, ou de l'utilisateur.
     * Si ces derniers ne sont pas présents, rien n'est modifié.
     * @param K Ordre de la chaîne de Markov
     * @param user Nom de l'utilisateur
     * @param oldDocs Liste des documents de l'ancien nœud.
     * @param newDocs Liste des documents du nouveau nœud.
     */
    public void lier(int K, String user, List<String> oldDocs, List<String> newDocs){
        String query = "MATCH (u:User {name:\""+user+"\"})-[:HAS]->(m1:Markov"+K+" {docs: [\""+oldDocs.get(0)+"\"";
        for(int i=1; i<oldDocs.size(); i++)
            query += ", \""+oldDocs.get(i)+"\"";
        query += "]}), (m2:Markov"+K+" {docs: [\""+newDocs.get(0)+"\"";
        for(int i=1; i<newDocs.size(); i++)
            query += ", \""+newDocs.get(i)+"\"";
        query += "]})<-[:HAS]-(:User {name:\""+user+"\"}) CREATE (m1)-[:NEXT {fins:[u.nbSessions+"+dureeDeVie+"]}]->(m2)";

        try(ResultSet set = connect.createStatement().executeQuery(query)){
        } catch(Exception e){
            System.out.println(e);
        }
    }
    /**
     * @author Laine B.
     * Récupération nombre de passages pour un lien.
     *
     * Cette méthode récupère le nombre de passage entre deux nœuds.
     * Aucune vérification n'est faite sur l'existence des nœuds, ou de l'utilisateur.
     * S'ils n'existent pas, la méthode retourne -1.
     * @param K Ordre de la chaîne de Markov
     * @param user Nom de l'utilisateur
     * @param oldDocs Liste des documents de l'ancien nœud.
     * @param newDocs Liste des documents du nouveau nœud.
     * @return -1 si l'utilisateur ou l'un des deux nœuds n'existe pas, le nombre de passage sinon.
     */
    public int getCptLien(int K, String user, List<String> oldDocs, List<String> newDocs){
        String query = "MATCH (:User {name:\""+user+"\"})-[:HAS]->(:Markov"+K+" {docs: [\""+oldDocs.get(0)+"\"";
        for(int i=1; i<oldDocs.size(); i++)
            query += ", \""+oldDocs.get(i)+"\"";
        query += "]})-[rel:NEXT]->(:Markov"+K+" {docs: [\""+newDocs.get(0)+"\"";
        for(int i=1; i<newDocs.size(); i++)
            query += ", \""+newDocs.get(i)+"\"";
        query += "]})<-[:HAS]-(:User {name:\""+user+"\"}) return size(rel.fins) AS cpt";

        try(ResultSet set = connect.createStatement().executeQuery(query)){
            int i=-1;
            if(set.next())
                i=set.getInt("cpt");
            return i;
        } catch(Exception e){
            System.out.println(e);
            return -1;
        }
    }
    /**
     * @author Laine B.
     * Renforce un lien entre deux nœuds déjà existant.
     *
     * Cette méthode renforce un lien entre deux nœuds déjà existant.
     * Aucune vérification n'est faite sur l'existence des nœuds ou, de l'utilisateur.
     * S'ils n'existent pas, rien n'est modifié.
     * @param K Ordre de la chaîne de Markov
     * @param user Nom de l'utilisateur
     * @param oldDocs Liste des documents de l'ancien nœud.
     * @param newDocs Liste des documents du nouveau nœud.
     */
    public void renforcerLien(int K, String user, List<String> oldDocs, List<String> newDocs){
        String query = "MATCH (u:User {name:\""+user+"\"})-[:HAS]->(m1:Markov"+K+" {docs: [\""+oldDocs.get(0)+"\"";
        for(int i=1; i<oldDocs.size(); i++)
            query += ", \""+oldDocs.get(i)+"\"";
        query += "]})-[rel:NEXT]->(m2:Markov"+K+" {docs: [\""+newDocs.get(0)+"\"";
        for(int i=1; i<newDocs.size(); i++)
            query += ", \""+newDocs.get(i)+"\"";
        query += "]})<-[:HAS]-(:User {name:\""+user+"\"}) SET rel.fins = rel.fins + (u.nbSessions + "+dureeDeVie+")";

        try(ResultSet set = connect.createStatement().executeQuery(query)){
        } catch(Exception e){
            System.out.println(e);
        }
    }
    /**
     * @author Laine B.
     * Ajout d'un nœud de Markov.
     *
     * Cette méthode ajoute à l'utilisateur donné un nœud de Markov correspondant à la liste de documents donnée
     * Aucune vérification n'est faite sur l'existence des documents ou de l'utilisateur
     * Si l'utilisateur n'existe pas, rien ne sera fait.
     * Si le document n'existe pas, le nœud sera malgré tout ajouté.
     * @param K Ordre de la chaîne de Markov
     * @param user Nom de l'utilisateur
     * @param docs Liste des documents du nœud.
     */
    public void addMarkovNode(int K, String user, List<String> docs){
        String query = "MATCH (u:User {name:\""+user+"\"}) CREATE (u)-[:HAS]->(:Markov"+K+":Markov {k:"+K+", docs: [\""+docs.get(0)+"\"";
        for(int i=1; i<docs.size(); i++)
            query += ", \""+docs.get(i)+"\"";
        query += "]})";
        try(ResultSet set = connect.createStatement().executeQuery(query)){
        } catch(Exception e){
            System.out.println(e);
        }
    }
    /**
     * @author Laine B.
     * Traitement d'une séquence de document.
     *
     * Cette méthode va traiter en deux temps une séquence de documents donnée:
     * <p><ul>
     *   <li> Passage du format (doc1, doc2, doc3)=>doc4 en (doc1, doc2, doc3)=>(doc2, doc3, doc4)
     *   <li> Gestion de la mise en mémoire en fonction de si:<ul>
     *     <li> un lien est déjà présent, auquel cas un renforcement du lien sera effectué.
     *     <li> il n'existe pas de lien de ce type, auquel cas il est crée.
     *   </ul>
     * </p></ul>
     * @param K Ordre de la chaîne de Markov
     * @param user Nom de l'utilisateur
     * @param oldDocs Liste des documents précédents
     * @param newDoc Document suivant
     */
    public void ajouterSequence(int K, String user, List<String> oldDocs, String newDoc){
        List<String> newDocs = new ArrayList<String>();
        for(int i=1; i<oldDocs.size(); i++)
            newDocs.add(oldDocs.get(i));
        newDocs.add(newDoc);

        if(!nodeExists(K, user, oldDocs))
            addMarkovNode(K, user, oldDocs);

        if(!nodeExists(K, user, newDocs))
            addMarkovNode(K, user, newDocs);

        int cptActuel = getCptLien(K, user, oldDocs, newDocs);
        if(cptActuel < 1){
            lier(K, user, oldDocs, newDocs);
        }else{
            renforcerLien(K, user, oldDocs, newDocs);
        }

    }
    /**
     * @author Laine B.
     * Suppression des nœuds inutiles.
     *
     * Cette méthode va supprimer les nœuds de Markov qui ne sont plus atteints par des liens [:NEXT].
     * @param user Nom de l'utilisateur
     */
    public void removeFeuillesMarkov(String user){
        if(dureeDeVie > 0){
            String query = "MATCH (:User {name:\""+user+"\"})-[:HAS]->(n) WHERE NOT (n)-[:NEXT]-() DETACH DELETE (n)";
            try(ResultSet set = connect.createStatement().executeQuery(query)){
            } catch(Exception e){
                System.out.println(e);
            }
        }
    }
    /**
     * @author Laine B.
     * Suppression liens vides.
     *
     * Cette méthode va supprimer les liens [:NEXT] ne contenant plus aucune fins.
     * @param user Nom de l'utilisateur
     */
    public void removeLiensVides(String user){
        if(dureeDeVie > 0){
            String query = "MATCH (:User {name:\""+user+"\"})-[:HAS]->()-[rel:NEXT]->() WHERE SIZE(rel.fins) = 0 DELETE rel";
            try(ResultSet set = connect.createStatement().executeQuery(query)){
            } catch(Exception e){
                System.out.println(e);
            }
        }
    }
    /**
     * @author Laine B.
     * Retrait sessions périmées.
     *
     * Cette méthode va retirer les sessions périmées des liens [:NEXT].
     * @param user Nom de l'utilisateur
     */
    public void removeLiensPerimes(String user){
        if(dureeDeVie > 0){
            String query = "MATCH (u:User {name:\""+user+"\"})-[:HAS]->()-[rel:NEXT]->() WHERE HEAD(rel.fins) = u.nbSessions SET rel.fins = TAIL(rel.fins)";
            try(ResultSet set = connect.createStatement().executeQuery(query)){
            } catch(Exception e){
                System.out.println(e);
            }
        }
    }
}
