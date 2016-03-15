package dao;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
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
        System.out.println("    User "+name);
        ResultSet out = execQuery("create ("+name+":User {name: \""+name+"\" })");
        try{
            out.close();
        } catch(Exception e){
            System.out.println(e);
        }
    }
    private boolean userIsPresent(String name){
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
    public void addUserIfNotPresent(String name){
        if(!userIsPresent(name))
            addUser(name);
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
            String query = "match (n:Markov"+K+")  where n.doc0=\""+docs.get(0)+"\"";
            for(int i=1; i<docs.size(); i++)
                query += " AND n.doc"+i+"=\""+docs.get(i)+"\"";
            query += " return count(n)";

            ResultSet set = execQuery(query);
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
    public void link(int K, String user, List<String> docs){
        String query = "MATCH (u:User {name:\""+user+"\"}), (m:Markov"+K+" {doc0: \""+docs.get(0)+"\"";
        for(int i=1; i<docs.size(); i++)
            query += ", doc"+i+": \""+docs.get(i)+"\"";
        query += "}) CREATE (u)-[:HAS]->(m)";
        ResultSet out = execQuery(query);
        try {
            out.close();
        } catch (Exception e ) {
            e.printStackTrace();
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
    public int getCptLink(int K, String user, List<String> oldDocs, List<String> newDocs){
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
        String query = "create (:Markov"+K+" {doc0: \""+docs.get(0)+"\"";
        for(int i=1; i<docs.size(); i++)
            query += ", doc"+i+": \""+docs.get(i)+"\"";
        query += "})";
        ResultSet out = execQuery(query);
        try {
            out.close();
        } catch (Exception e ) {
            e.printStackTrace();
        }
        link(K, user, docs);
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

        int cptActuel = getCptLink(K, user, oldDocs, newDocs);
        if(cptActuel < 1)
            lier(K, user, oldDocs, newDocs);
        else
            updateLien(K, user, oldDocs, newDocs, cptActuel);

    }
    public void addSession(String user, List<String> session){
        for(int i=1; i<session.size(); i++){
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
    }

    //Catégories
    public int getCategorieId(String cat){
        try{
            String query = "match (n:Categorie)  where n.name=\""+cat+"\" RETURN n.cpt";
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
    public void addCategorie(String categorie){
        int nbCategories = getNbCategories();
        ResultSet out = execQuery("create ("+categorie+":Categorie {name:\""+categorie+"\", cpt:\""+nbCategories+"\"})");
        try{
            out.close();
        } catch (Exception e){}
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
