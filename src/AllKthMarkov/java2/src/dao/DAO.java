package dao;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Map;
import java.util.List;
import java.util.Properties;

import org.neo4j.jdbc.Driver;
import org.neo4j.jdbc.Neo4jConnection;

public class DAO {
    private Properties properties;
    private Neo4jConnection connect;
    private boolean isConnected;

    //Paramètres Neo4j
    private String username = "neo4j";
    private String password = "neo4j";

    //Paramètres modèle
    private int dureeDeVie = 100;
    private double pourcentageMinUserVector = 0.05;

    //Constructeur
    public DAO(){
        properties = new Properties();
        properties.put("user", username);
        properties.put("password", password);
        try{
            connect = new Driver().connect("jdbc:neo4j://localhost:7474", properties);
            isConnected = true;
        } catch (Exception e){
            System.out.println("Connexion au serveur impossible.\nVérifiez votre username/password.");
            isConnected = false;
        }
    }

    //Groups
    public void changeNumberOfGroup(int n){
        try(ResultSet set = connect.createStatement().executeQuery("match (n:Group) detach delete n")){
        } catch(Exception e){
            System.out.println(e);
        }

        int nbCategories = getNbCategories();

        for(int i=0; i<n; i++){
            String query ="CREATE (:Group {name: \"Group"+i+"\", vector:[";
            if(nbCategories>0){
                query += "toFloat(0)";
                for(int j=1; j<nbCategories; j++)
                    query += ", toFloat(0)";
            }
            query += "]})";

            try(ResultSet set = connect.createStatement().executeQuery(query)){
            } catch(Exception e){
                System.out.println(e);
            }
        }

        initialise();
        recompute();
    }
    public Integer getNbGroup(){
        return getNbClass("Group");
    }
    public void link(String userName, int group){
        try(ResultSet set = connect.createStatement().executeQuery("MATCH (g:Group {name:\"Group"+group+"\"}), (u:User {name:\""+userName+"\"}) CREATE (g)-[:CONTAINS]->(u)")){
        } catch(Exception e){
            System.out.println(e);
        }
    }

    //K-means
    public void initialise(){
        List<String> users = getUserNames();
        int max = getNbGroup();
        int i=0;
        for(String user : users){
            link(user, i);
            i=(i+1)%max;
        }
    }
    public void recompute(){//TODO
    }

    //Users
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
    private void addUser(String name){
        int nbCategories = getNbCategories();
        String query = "create ("+name+":User {name: \""+name+"\" , nbSessions:0, vector: [";
        if(nbCategories>0){
            query += "toFloat(0)";
            for(int i=1; i<nbCategories; i++)
                query += ", toFloat(0)";
        }
        query += "]})";

        try(ResultSet set = connect.createStatement().executeQuery(query)){
        } catch(Exception e){
            System.out.println(e);
        }
    }
    private boolean userExists(String name){
        try(ResultSet set = connect.createStatement().executeQuery("match (n:User)  where n.name=\""+name+"\" return count(n)")){
            int i=-1;
            if(set .next())
                i=set.getInt("count(n)");
            return (i==1);
        } catch(Exception e){
            System.out.println(e);
            return false;
        }
    }
    public int addUserIfNotPresent(String name){
        if(!userExists(name)){
            addUser(name);
            return 0;
        }
        return 1;
    }
    public Integer getNbUser(){
        return getNbClass("User");
    }
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
    private void incrementNbSessionsUser(String user){
        String query = "MATCH (u:User {name:\""+user+"\"}) WITH u, u.nbSessions+1 AS nbSessions SET u.nbSessions = nbSessions";

        try(ResultSet set = connect.createStatement().executeQuery(query)){
        } catch(Exception e){
            System.out.println(e);
        }
    }

    //Markovs
    private boolean nodeExists(int K, String user, List<String> docs){
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
    private void lier(int K, String user, List<String> oldDocs, List<String> newDocs){
        String query = "MATCH (:User {name:\""+user+"\"})-[:HAS]->(m1:Markov"+K+" {docs: [\""+oldDocs.get(0)+"\"";
        for(int i=1; i<oldDocs.size(); i++)
            query += ", \""+oldDocs.get(i)+"\"";
        query += "]}), (m2:Markov"+K+" {docs: [\""+newDocs.get(0)+"\"";
        for(int i=1; i<newDocs.size(); i++)
            query += ", \""+newDocs.get(i)+"\"";
        query += "]})<-[:HAS]-(:User {name:\""+user+"\"}) CREATE (m1)-[:NEXT {fins:["+(getNbSessions(user)+dureeDeVie)+"]}]->(m2)";

        try(ResultSet set = connect.createStatement().executeQuery(query)){
        } catch(Exception e){
            System.out.println(e);
        }
    }
    private int getCptLien(int K, String user, List<String> oldDocs, List<String> newDocs){
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
    public void updateLien(int K, String user, List<String> oldDocs, List<String> newDocs){
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
    public void addMarkovNode(int K, String user, List<String> docs){
        String query = "MATCH (u:User {name:\""+user+"\"}) CREATE (u)-[:HAS]->(:Markov"+K+" {docs: [\""+docs.get(0)+"\"";
        for(int i=1; i<docs.size(); i++)
            query += ", \""+docs.get(i)+"\"";
        query += "]})";
        try(ResultSet set = connect.createStatement().executeQuery(query)){
        } catch(Exception e){
            System.out.println(e);
        }
    }
    private void renforcer(int K, String user, List<String> oldDocs, String newDoc){
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
            updateLien(K, user, oldDocs, newDocs);
        }

    }
    public int addSession(String user, List<String> session){
        if(userExists(user)){
            for(int i=0; i<session.size(); i++)
                if(!docExists(session.get(i)))
                    return (i+2);

            removeLiensPerimes(user);
            removeLiensVides(user);
            removeFeuillesMarkov(user);

            updateUserVector(user, session);

            for(int i=1; i<Math.min(5, session.size()); i++){
                Vector<String> oldDocs = new Vector<String>();
                for(int j=0; j<i; j++)
                    oldDocs.add(session.get(j));
                String newDoc = session.get(i);

                renforcer(i, user, oldDocs, newDoc);

                for(int j=i+1; j<session.size(); j++){
                    oldDocs.remove(0);
                    oldDocs.add(session.get(j-1));
                    newDoc = session.get(j);
                    renforcer(i, user, oldDocs, newDoc);
                }
            }

            incrementNbSessionsUser(user);

            return 0;
        } else {
            return 1;
        }
    }
    public Vector<Hashtable<String, Double>> guessNextDocs(String user, Vector<String> session){
        Vector<Hashtable<String, Double>> out = new Vector<Hashtable<String, Double>>();
        for(int i=1; i<Math.min(5, session.size()+1); i++){
            out.add(new Hashtable<String, Double>());

            //Gestion noeud autre groupe
            String query = "MATCH (:User {name:\""+user+"\"})<-[:CONTAINS]-(:Group)-[:CONTAINS]->(:User)-[:HAS]->(:Markov"+i+" {docs: [\""+session.get(session.size()-i)+"\"";
            for(int j=session.size()-i+1; j<session.size(); j++)
                query += ", \""+session.get(j)+"\"";
            query += "]})-[rel:NEXT]->(d:Markov"+i+") ";
            query += "RETURN LAST(d.docs) as doc, SIZE(rel.fins) AS cpt ";
            query += "UNION ALL ";

            // GEstion de nos noeud
            query += "MATCH (:User {name:\""+user+"\"})-[:HAS]->(:Markov"+i+" {docs: [\""+session.get(session.size()-i)+"\"";
            for(int j=session.size()-i+1; j<session.size(); j++)
                query += ", \""+session.get(j)+"\"";
            query += "]})-[rel:NEXT]->(d:Markov"+i+") ";
            query += "RETURN LAST(d.docs) as doc, SIZE(rel.fins) AS cpt";

            try(ResultSet set = connect.createStatement().executeQuery(query)){
                int total=0;
                while(set.next()){
                    String doc = set.getString("doc");
                    Double cpt = set.getDouble("cpt");
                    total+=cpt;
                    if(out.get(i-1).containsKey(doc)){
                        double old = out.get(i-1).get(doc);
                        out.get(i-1).put(doc, cpt+old);
                    } else {
                        out.get(i-1).put(doc, cpt);
                    }
                }
                for(String key : out.get(i-1).keySet()){
                    double old = out.get(i-1).get(key);
                    out.get(i-1).put(key, old/total);
                }
            } catch(Exception e){
                System.out.println(e);
            }
        }
        return out;
    }
    private void removeFeuillesMarkov(String user){
        if(dureeDeVie > 0){
            String query = "MATCH (:User {name:\""+user+"\"})-[:HAS]->(n) WHERE NOT (n)-[:NEXT]-() DETACH DELETE (n)";
            try(ResultSet set = connect.createStatement().executeQuery(query)){
            } catch(Exception e){
                System.out.println(e);
            }
        }
    }
    private void removeLiensVides(String user){
        if(dureeDeVie > 0){
            String query = "MATCH (:User {name:\""+user+"\"})-[:HAS]->()-[rel:NEXT]->() WHERE SIZE(rel.fins) = 0 DELETE rel";
            try(ResultSet set = connect.createStatement().executeQuery(query)){
            } catch(Exception e){
                System.out.println(e);
            }
        }
    }
    private void removeLiensPerimes(String user){
        if(dureeDeVie > 0){
            String query = "MATCH (u:User {name:\""+user+"\"})-[:HAS]->()-[rel:NEXT]->() WHERE HEAD(rel.fins) = u.nbSessions SET rel.fins = TAIL(rel.fins)";
            try(ResultSet set = connect.createStatement().executeQuery(query)){
            } catch(Exception e){
                System.out.println(e);
            }
        }
    }

    //Vectors
    private void resizeVectors(){
        String query = "MATCH (n) WHERE n:User OR n:Group SET n.vector = n.vector + toFloat(0)";

        try(ResultSet set = connect.createStatement().executeQuery(query)){
        } catch(Exception e){
            System.out.println(e);
        }
    }
    private void updateUserVector(String user, List<String> session){
        int nbSessions = getNbSessions(user);
        int nbCategories = getNbCategories();
        int tailleSession = session.size();
        double pourcentageNew = Math.max( ((double)1)/(nbSessions+1), pourcentageMinUserVector);
        double pourcentageOld = 1-pourcentageNew;

        Vector<Double> sessionVector = new Vector<Double>();
        for(int i=0; i<nbCategories; i++)
            sessionVector.add(0.);

        for(String doc : session){
            int i=getCategorie(doc);
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

    //Documents
    public int addDocument(String doc, String categorie){
        if(getCategorieId(categorie) != -1){
            if(!docExists(doc)){
                try(ResultSet set = connect.createStatement().executeQuery("MATCH (c:Categorie {name:\""+categorie+"\"}) CREATE (c)<-[:HASCATEGORIE]-(:Doc {name: \""+doc+"\" })")){
                    return 0;
                } catch(Exception e){
                    System.out.println(e);
                    return 1;
                }
            } else {
                return 2;
            }
        }
        return 1;
    }
    public boolean docExists(String doc){
        try(ResultSet set = connect.createStatement().executeQuery("match (d:Doc {name:\""+doc+"\"}) return count(d)")){
            int i=0;
            while(set.next())
                i+=set.getInt("count(d)");
            return (i>=1);
        } catch(Exception e){
            System.out.println(e);
            return false;
        }
    }
    public int getCategorie(String doc){
        String query = "MATCH (c:Categorie)-[]-(:Doc {name: \""+doc+"\"}) RETURN c.cpt";
        int out = -1;
        try(ResultSet set = connect.createStatement().executeQuery(query)){
            if(set.next())
                out=set.getInt("c.cpt");
        } catch(Exception e){
            System.out.println(e);
        } finally {
            return out;
        }
    }

    //Catégories
    public int getCategorieId(String cat){
        String query = "match (n:Categorie {name:\""+cat+"\"}) RETURN n.cpt";
        try(ResultSet set = connect.createStatement().executeQuery(query)){
            int i=-1;
            if(set.next())
                i=set.getInt("n.cpt");
            return i;
        } catch(Exception e){
            System.out.println(e);
            return -1;
        }
    }
    public Integer getNbCategories(){
        return getNbClass("Categorie");
    }
    public List<String> getCategories(){
        try(ResultSet set = connect.createStatement().executeQuery("match (n:Category) return n.name")){
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
    public int addCategorie(String categorie){
        int nbCategories = getNbCategories();
        if(getCategorieId(categorie)==-1){
            try(ResultSet set = connect.createStatement().executeQuery("create ("+categorie+":Categorie {name:\""+categorie+"\", cpt:"+nbCategories+"})")){
                resizeVectors();
                return 0;
            } catch(Exception e){
                System.out.println(e);
                return 1;
            }
        }
        return 1;
    }

    //Autre
    private int getNbClass(String className){
        try(ResultSet set = connect.createStatement().executeQuery("match (n:"+className+") return count(n)")){
            int i=-1;
            if(set.next())
                i=set.getInt("count(n)");
            return i;
        } catch(Exception e){
            System.out.println(e);
            return -1;
        }
    }
    public void clearDB(){
        try(ResultSet out = connect.createStatement().executeQuery("match (n) detach delete n")){
        } catch(Exception e){
            System.out.println(e);
        }
    }
    public boolean isConnected(){
        return isConnected;
    }
}
