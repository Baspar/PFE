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
    private String username = "neo4j";
    private String password = "Ch3va|e";

    //Constructeur
    public DAO(){
        properties = new Properties();
        properties.put("user", username);
        properties.put("password", password);
        try{
            connect = new Driver().connect("jdbc:neo4j://localhost:7474", properties);
        } catch (Exception e){};
    }

    //Groups
    public void changeNumberOfGroup(int n){
        ResultSet out = execQuery("match (g:Group) detach delete g");
        for(int i=0; i<n; i++)
            execQuery("merge (group"+i+":Group {name: \"group"+i+"\"})");
        try{
            out.close();
        } catch(Exception e){
            System.out.println(e);
        }
        initialise();
    }
    public Integer getNbGroup(){
        return getNbClass("Group");
    }
    public void link(String userName, int group){
        ResultSet out = execQuery("MATCH (g:Group {name:\"group"+group+"\"}), (u:User {name:\""+userName+"\"}) CREATE (g)-[:CONTAINS]->(u)");
        try{
            out.close();
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
    public void recompute(){
        initialise();
    }

    //Users
    private void addUser(String name){
        ResultSet out = execQuery("create ("+name+":User {name: \""+name+"\" })");
        try{
            out.close();
        } catch(Exception e){
            System.out.println(e);
        }
    }
    private boolean userExists(String name){
        try{
            ResultSet set = execQuery("match (n:User)  where n.name=\""+name+"\" return count(n)");
            int i=-1;
            if(set.next())
                i=set.getInt("count(n)");
            set.close();
            return (i==1);
        } catch (Exception e){
            System.out.print(e);
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
        try{
            List<String> out = new ArrayList<String>();

            ResultSet set = execQuery("match (n:User) return n.name");
            while(set.next()){
                out.add(set.getString("n.name"));
            }

            set.close();
            return out;
        } catch (Exception e){
            System.out.print(e);
            return new ArrayList<String>();
        }
    }

    //Markovs
    public boolean nodeExists(int K, String user, List<String> docs){
        try{
            String query = "match (u:User {name:\""+user+"\"})-[:HAS]->(n:Markov"+K+" {doc0:\""+docs.get(0)+"\"";
            for(int i=1; i<docs.size(); i++)
                query += ", doc"+i+":\""+docs.get(i)+"\"";
            query += "}) return count(n)";

            ResultSet set = execQuery(query);
            int i=0;
            while(set.next())
                i+=set.getInt("count(n)");
            set.close();
            return (i>=1);
        } catch (Exception e){
            System.out.print(e);
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
        query += "})<-[:HAS]-(:User {name:\""+user+"\"}) CREATE (m1)-[:NEXT {cpt:1}]->(m2)";
        ResultSet out = execQuery(query);
        try {
            out.close();
        } catch (Exception e ) {
            e.printStackTrace();
        }
    }
    public int getCptLien(int K, String user, List<String> oldDocs, List<String> newDocs){
        try {
            String query = "MATCH (:User {name:\""+user+"\"})-[:HAS]->(:Markov"+K+" {doc0: \""+oldDocs.get(0)+"\"";
            for(int i=1; i<oldDocs.size(); i++)
                query += ", doc"+i+": \""+oldDocs.get(i)+"\"";
            query += "})-[rel:NEXT]->(:Markov"+K+" {doc0: \""+newDocs.get(0)+"\"";
            for(int i=1; i<newDocs.size(); i++)
                query += ", doc"+i+": \""+newDocs.get(i)+"\"";
            query += "})<-[:HAS]-(:User {name:\""+user+"\"}) return rel.cpt";
            ResultSet out = execQuery(query);
            int i=-1;
            if(out.next())
                i=out.getInt("rel.cpt");
            out.close();
            return i;
        } catch (Exception e ) {
            e.printStackTrace();
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
        ResultSet out = execQuery(query);
        try {
            out.close();
        } catch (Exception e ) {
            e.printStackTrace();
        }
    }
    public void addMarkovNode(int K, String user, List<String> docs){
        String query = "MATCH (u:User {name:\""+user+"\"}) CREATE (u)-[:HAS]->(:Markov"+K+" {doc0: \""+docs.get(0)+"\"";
        for(int i=1; i<docs.size(); i++)
            query += ", doc"+i+": \""+docs.get(i)+"\"";
        query += "})";
        ResultSet out = execQuery(query);
        try {
            out.close();
        } catch (Exception e ) {
            e.printStackTrace();
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

        int cptActuel = getCptLien(K, user, oldDocs, newDocs);
        if(cptActuel < 1){
            lier(K, user, oldDocs, newDocs);
        }else{
            updateLien(K, user, oldDocs, newDocs, cptActuel);
        }

    }
    public int addSession(String user, List<String> session){
        if(userExists(user)){
            for(int i=0; i<session.size(); i++)
                if(!docExists(session.get(i)))
                    return (i+2);

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
            query += "RETURN rel.cpt, d.doc"+(i-1)+" as doc ";
            query += "UNION ALL ";

            // GEstion de nos noeud
            query += "MATCH (:User {name:\""+user+"\"})-[:HAS]->(:Markov"+i+" {doc0: \""+session.get(session.size()-i)+"\"";
            for(int j=session.size()-i+1; j<session.size(); j++)
                query += ", doc"+(j-session.size()+i)+": \""+session.get(j)+"\"";
            query += "})-[rel:NEXT]->(d:Markov"+i+") ";
            query += "RETURN rel.cpt, d.doc"+(i-1)+" as doc ";
            ResultSet rs = execQuery(query);
            try{
                int total=0;
                while(rs.next()){
                    String doc = rs.getString("doc");
                    Double cpt = rs.getDouble("rel.cpt");
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
            } catch (Exception e){ System.out.println(e);}
        }
        return out;
    }

    //Documents
    public int addDocument(String doc, String categorie){
        if(getCategorieId(categorie) != -1){
            if(!docExists(doc)){
                ResultSet out = execQuery("MATCH (c:Categorie {name:\""+categorie+"\"}) CREATE (c)<-[:HASCATEGORIE]-(:Doc {name: \""+doc+"\" })");
                try{
                    out.close();
                } catch(Exception e){
                    System.out.println(e);
                } finally {
                    return 0;
                }
            } else {
                return 2;
            }
        }
        return 1;
    }
    public boolean docExists(String doc){
        try{
            ResultSet set = execQuery("match (d:Doc {name:\""+doc+"\"}) return count(d)");
            int i=0;
            while(set.next())
                i+=set.getInt("count(d)");
            set.close();
            return (i>=1);
        } catch (Exception e){
            System.out.print(e);
            return false;
        }
    }

    //Catégories
    public int getCategorieId(String cat){
        try{
            String query = "match (n:Categorie {name:\""+cat+"\"}) RETURN n.cpt";
            ResultSet set = execQuery(query);
            int i=-1;
            if(set.next())
                i=set.getInt("n.cpt");
            set.close();
            return i;
        } catch (Exception e){
            System.out.print(e);
            return -1;
        }
    }
    public Integer getNbCategories(){
        return getNbClass("Categorie");
    }
    public List<String> getCategories(){
        try{
            List<String> out = new ArrayList<String>();

            ResultSet set = execQuery("match (n:Category) return n.name");
            while(set.next()){
                out.add(set.getString("n.name"));
            }

            set.close();
            return out;
        } catch (Exception e){
            System.out.print(e);
            return new ArrayList<String>();
        }
    }
    public int addCategorie(String categorie){
        int nbCategories = getNbCategories();
        if(getCategorieId(categorie)==-1){
            ResultSet out = execQuery("create ("+categorie+":Categorie {name:\""+categorie+"\", cpt:"+nbCategories+"})");
            try{
                out.close();
            } catch (Exception e){
                System.out.println(e);
            } finally {
                return 0;
            }
        }
        return 1;
    }

    //Autre
    private ResultSet execQuery(String query){
        try{
            return connect.createStatement().executeQuery(query);
        } catch (Exception e){
            System.out.println(e);
            return null;
        }
    }
    private Integer getNbClass(String className){
        try{
            ResultSet set = execQuery("match (n:"+className+") return count(n)");
            int i=-1;
            if(set.next())
                i=set.getInt("count(n)");
            set.close();
            return i;
        } catch (Exception e){
            System.out.print(e);
            return -1;
        }
    }
    public void clearDB(){
        ResultSet out = execQuery("match (n) detach delete n");
        try{
            out.close();
        } catch(Exception e){
            System.out.println(e);
        }
    }
}
