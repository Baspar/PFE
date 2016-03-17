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

        for(int i=0; i<n; i++){
            try(ResultSet set = connect.createStatement().executeQuery("merge (group"+i+":Group {name: \"group"+i+"\"})")){
            } catch(Exception e){
                System.out.println(e);
            }
        }

        initialise();
    }
    public Integer getNbGroup(){
        return getNbClass("Group");
    }
    public void link(String userName, int group){
        try(ResultSet set = connect.createStatement().executeQuery("MATCH (g:Group {name:\"group"+group+"\"}), (u:User {name:\""+userName+"\"}) CREATE (g)-[:CONTAINS]->(u)")){
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
        initialise();
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
        String query = "create ("+name+":User {name: \""+name+"\" , nbSessions:0, userVector: [";
        if(nbCategories>0){
            query += "0";
            for(int i=1; i<nbCategories; i++)
                query += ", 0";
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
    public void resizeUserVectors(){
        String query = "MATCH (u:User) SET u.userVector = u.userVector  + 0";

        try(ResultSet set = connect.createStatement().executeQuery(query)){
        } catch(Exception e){
            System.out.println(e);
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
    public boolean nodeExists(int K, String user, List<String> docs){
        String query = "match (u:User {name:\""+user+"\"})-[:HAS]->(n:Markov"+K+" {doc0:\""+docs.get(0)+"\"";
        for(int i=1; i<docs.size(); i++)
            query += ", doc"+i+":\""+docs.get(i)+"\"";
        query += "}) return count(n)";

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
    public void lier(int K, String user, List<String> oldDocs, List<String> newDocs){
        String query = "MATCH (:User {name:\""+user+"\"})-[:HAS]->(m1:Markov"+K+" {doc0: \""+oldDocs.get(0)+"\"";
        for(int i=1; i<oldDocs.size(); i++)
            query += ", doc"+i+": \""+oldDocs.get(i)+"\"";
        query += "}), (m2:Markov"+K+" {doc0: \""+newDocs.get(0)+"\"";
        for(int i=1; i<newDocs.size(); i++)
            query += ", doc"+i+": \""+newDocs.get(i)+"\"";
        query += "})<-[:HAS]-(:User {name:\""+user+"\"}) CREATE (m1)-[:NEXT {fin:"+(getNbSessions(user)+dureeDeVie)+"}]->(m2)";
        //MODIFICATION
        //query += "})<-[:HAS]-(:User {name:\""+user+"\"}) CREATE (m1)-[:NEXT {cpt:1}]->(m2)";

        try(ResultSet set = connect.createStatement().executeQuery(query)){
        } catch(Exception e){
            System.out.println(e);
        }
    }
    public int getCptLien(int K, String user, List<String> oldDocs, List<String> newDocs){
        String query = "MATCH (:User {name:\""+user+"\"})-[:HAS]->(:Markov"+K+" {doc0: \""+oldDocs.get(0)+"\"";
        for(int i=1; i<oldDocs.size(); i++)
            query += ", doc"+i+": \""+oldDocs.get(i)+"\"";
        query += "})-[rel:NEXT]->(:Markov"+K+" {doc0: \""+newDocs.get(0)+"\"";
        for(int i=1; i<newDocs.size(); i++)
            query += ", doc"+i+": \""+newDocs.get(i)+"\"";
        query += "})<-[:HAS]-(:User {name:\""+user+"\"}) return rel.cpt";

        try(ResultSet set = connect.createStatement().executeQuery(query)){
            int i=-1;
            if(set.next())
                i=set.getInt("rel.cpt");
            return i;
        } catch(Exception e){
            System.out.println(e);
            return -1;
        }
    }
    public void updateLien(int K, String user, List<String> oldDocs, List<String> newDocs, int cpt){
        String query = "MATCH (:User {name:\""+user+"\"})-[:HAS]->(m1:Markov"+K+" {doc0: \""+oldDocs.get(0)+"\"";
        for(int i=1; i<oldDocs.size(); i++)
            query += ", doc"+i+": \""+oldDocs.get(i)+"\"";
        query += "})-[rel:NEXT]->(m2:Markov"+K+" {doc0: \""+newDocs.get(0)+"\"";
        for(int i=1; i<newDocs.size(); i++)
            query += ", doc"+i+": \""+newDocs.get(i)+"\"";
        query += "})<-[:HAS]-(:User {name:\""+user+"\"}) SET rel.cpt = "+(cpt+1);

        try(ResultSet set = connect.createStatement().executeQuery(query)){
        } catch(Exception e){
            System.out.println(e);
        }
    }
    public void addMarkovNode(int K, String user, List<String> docs){
        String query = "MATCH (u:User {name:\""+user+"\"}) CREATE (u)-[:HAS]->(:Markov"+K+" {doc0: \""+docs.get(0)+"\"";
        for(int i=1; i<docs.size(); i++)
            query += ", doc"+i+": \""+docs.get(i)+"\"";
        query += "})";
        try(ResultSet set = connect.createStatement().executeQuery(query)){
        } catch(Exception e){
            System.out.println(e);
        }
    }
    public void renforcer(int K, String user, List<String> oldDocs, String newDoc){
        List<String> newDocs = new ArrayList<String>();
        for(int i=1; i<oldDocs.size(); i++)
            newDocs.add(oldDocs.get(i));
        newDocs.add(newDoc);

        if(!nodeExists(K, user, oldDocs))
            addMarkovNode(K, user, oldDocs);

        if(!nodeExists(K, user, newDocs))
            addMarkovNode(K, user, newDocs);

        //MODIFICATION
        //int cptActuel = getCptLien(K, user, oldDocs, newDocs);
        //if(cptActuel < 1){
            lier(K, user, oldDocs, newDocs);
        //}else{
            //updateLien(K, user, oldDocs, newDocs, cptActuel);
        //}

    }
    public int addSession(String user, List<String> session){
        if(userExists(user)){
            for(int i=0; i<session.size(); i++)
                if(!docExists(session.get(i)))
                    return (i+2);

            removeLiensPerimes(user);
            removeFeuillesMarkov(user);

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
        for(int i=1; i<5; i++){
            out.add(new Hashtable<String, Double>());

            //GEstion noeud autre groupe
            String query = "MATCH (:User {name:\""+user+"\"})<-[:CONTAINS]-(:Group)-[:CONTAINS]->(:User)-[:HAS]->(:Markov"+i+" {doc0: \""+session.get(session.size()-i)+"\"";
            for(int j=session.size()-i+1; j<session.size(); j++)
                query += ", doc"+(j-session.size()+i)+": \""+session.get(j)+"\"";
            query += "})-[rel:NEXT]->(d:Markov"+i+") ";
            //MODIFICATIOn
            //query += "RETURN rel.cpt, d.doc"+(i-1)+" as doc ";
            query += "RETURN d.doc"+(i-1)+" as doc ";
            query += "UNION ALL ";

            // GEstion de nos noeud
            query += "MATCH (:User {name:\""+user+"\"})-[:HAS]->(:Markov"+i+" {doc0: \""+session.get(session.size()-i)+"\"";
            for(int j=session.size()-i+1; j<session.size(); j++)
                query += ", doc"+(j-session.size()+i)+": \""+session.get(j)+"\"";
            query += "})-[rel:NEXT]->(d:Markov"+i+") ";
            //MODIFICATION
            //query += "RETURN rel.cpt, d.doc"+(i-1)+" as doc ";
            query += "RETURN d.doc"+(i-1)+" as doc ";

            try(ResultSet set = connect.createStatement().executeQuery(query)){
                int total=0;
                while(set.next()){
                    String doc = set.getString("doc");
                    //MODIFICATION
                    //Double cpt = set.getDouble("rel.cpt");
                    //total+=cpt;
                    total++;
                    if(out.get(i-1).containsKey(doc)){
                        double old = out.get(i-1).get(doc);
                        //MODIFICATION
                        //out.get(i-1).put(doc, cpt+old);
                        out.get(i-1).put(doc, old+1);
                    } else {
                        //MODIFICATION
                        //out.get(i-1).put(doc, cpt);
                        out.get(i-1).put(doc, 1.);
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
    private void removeLiensPerimes(String user){
        if(dureeDeVie > 0){
            String query = "MATCH (u:User {name:\""+user+"\"})-[:HAS]->()-[rel:NEXT]->() WHERE rel.fin = u.nbSessions DELETE rel";
            try(ResultSet set = connect.createStatement().executeQuery(query)){
            } catch(Exception e){
                System.out.println(e);
            }
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
                resizeUserVectors();
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
