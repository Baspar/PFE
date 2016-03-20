package dao;

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
    }
    public Integer getNbGroup(){
        return getNbClass("Group");
    }
    private void link(String userName, String group){
        try(ResultSet set = connect.createStatement().executeQuery("MATCH (g:Group {name:\""+group+"\"}), (u:User {name:\""+userName+"\"}) CREATE (g)-[:CONTAINS]->(u)")){
        } catch(Exception e){
            System.out.println(e);
        }
    }
    private void deleteEveryContains(){
        String query = "MATCH ()-[rel:CONTAINS]-() DELETE rel";
        try(ResultSet set = connect.createStatement().executeQuery(query)){
        } catch(Exception e){
            System.out.println(e);
        }
    }
    public HashMap<String, Integer> getNbUsersInGroups(){
        HashMap<String, Integer> out = new HashMap<String, Integer>();
        int nbGroups = getNbGroup();

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
    public void linkToClosestGroup(String user){
        HashMap<String, Vector<Double>> groupVector = getVector("Group");
        HashMap<String, Integer> nbUsersInGroups = getNbUsersInGroups();
        Vector<Double> userVector = getVector("User").get(user);


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

    //K-means
    public void initialise(){
        List<String> users = getUserNames();
        List<String> userscp = new ArrayList<String>(users);
        HashMap<String, Vector<Double>> userVector = getVector("User");
        int nbGroups = getNbGroup();
        int nbCat = getNbCategories();

        for(int i=0; i<nbGroups; i++){
            if(userscp.size() > 0){
                int rand = (int)Math.floor(Math.random()*userscp.size());
                String user = userscp.get(rand);
                userscp.remove(rand);
                setVectorGroupe("Group"+i, userVector.get(user));
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
                setVectorGroupe("Group"+i, vect);
            }
        }


        int i=0;
        for(String user : users){
            link(user, "Group"+i);
            i=(i+1)%nbGroups;
        }
    }
    public void recompute(){
        initialise();

        HashMap<String, Vector<Double>> userVector = getVector("User");
        HashMap<String, Vector<Double>> groupVector = getVector("Group");
        HashMap<String, String> usersGroups = getUsersGroups();
        HashMap<String, Integer> nbUsersInGroups = getNbUsersInGroups();

        boolean hasChanged = true;

        while(hasChanged){
            hasChanged = false;
            System.out.println("\n===============================");

            for(String user : userVector.keySet()){
                double distMin = distance(userVector.get(user), groupVector.get(usersGroups.get(user)));
                String groupMin = usersGroups.get(user);

                System.err.println("  "+user+": "+groupMin+"["+distMin+"]");

                for(Map.Entry<String, Vector<Double>> ent : groupVector.entrySet()){
                    double newDist = distance(userVector.get(user), ent.getValue());
                    System.err.print("       : "+ent.getKey()+"["+newDist+"]");
                    if(newDist < distMin){
                        nbUsersInGroups.put(groupMin, nbUsersInGroups.get(groupMin)-1);
                        nbUsersInGroups.put(ent.getKey(), nbUsersInGroups.get(ent.getKey())+1);
                        System.err.println(" =>");
                        hasChanged = true;
                        distMin = newDist;
                        groupMin = ent.getKey();
                    } else {
                        System.err.println(" X");
                    }
                }

                usersGroups.put(user, groupMin);
            }


            if(hasChanged){
                for(String group : groupVector.keySet()){
                    int nbCat = groupVector.get(group).size();
                    for(int i=0; i<nbCat; i++)
                        groupVector.get(group).set(i, 0.);
                }
                for(String user : userVector.keySet()){
                    int nbCat = userVector.get(user).size();
                    String group = usersGroups.get(user);
                    for(int i=0; i<nbCat; i++)
                        groupVector.get(group).set(i, groupVector.get(group).get(i)+userVector.get(user).get(i));
                }
                for(String group : groupVector.keySet()){
                    if(nbUsersInGroups.containsKey(group))
                        for(int i=0; i<groupVector.get(group).size(); i++)
                            groupVector.get(group).set(i, groupVector.get(group).get(i)/nbUsersInGroups.get(group));
                }


            }
        }

        deleteEveryContains();
        for(Map.Entry<String, String> ent : usersGroups.entrySet())
            link(ent.getKey(), ent.getValue());
        calculVectorGroupes();
    }

    //Users
    public HashMap<String, String> getUsersGroups(){
        HashMap<String, String> out = new HashMap<String, String>();

        String query = "MATCH (g:Group)--(u:User) return g.name AS group, u.name AS user";
        try(ResultSet set = connect.createStatement().executeQuery(query)){
            while(set.next()){
                String user = set.getString("user");
                String group = set.getString("group");
                out.put(user, group);
            }
            return out;
        } catch(Exception e){
            System.out.println(e);
            return new HashMap<String, String>();
        }
    }
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
        String query = "create (:User {name: \""+name+"\" , nbSessions:0, vector: [";
        if(nbCategories>0){
            query += "toFloat(0)";
            for(int i=1; i<nbCategories; i++)
                query += ", toFloat(0)";
        }
        query += "]})";

        try(ResultSet set = connect.createStatement().executeQuery(query)){
            linkToClosestGroup(name);
        } catch(Exception e){
            System.out.println(e);
        }
    }
    public boolean userExists(String name){
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
    public String getUserGroup(String user){
        String out ="";
        String query = "MATCH (g:Group)--(u:User {name:\""+user+"\"}) RETURN g.name";
        try(ResultSet set = connect.createStatement().executeQuery(query)){
            if(set.next())
                out = set.getString("g.name");
        } catch(Exception e){
            System.out.println(e);
        } finally {
            return out;
        }
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

             calculVectorGroupe(getUserGroup(user));

            return 0;
        } else {
            return 1;
        }
    }
    public Vector<HashMap<String, Double>> guessNextDocs(String user, Vector<String> session){
        Vector<HashMap<String, Double>> out = new Vector<HashMap<String, Double>>();
        for(int i=1; i<Math.min(5, session.size()+1); i++){
            out.add(new HashMap<String, Double>());

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
    private HashMap<String, Vector<Double>> getVector(String cat){
        HashMap<String, Vector<Double>> out = new HashMap<String, Vector<Double>>();
        int nbCat = getNbCategories();

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
        private void setVectorGroupe(String group, List<Double> vector){
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
        private void calculVectorGroupe(String group){
            int nbCategories = getNbCategories();

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
        private void calculVectorGroupe(int g){
            int nbCategories = getNbCategories();

            String query = "MATCH (g:Group {name:\"Group"+g+"\"})-[]-(u:User) WITH g";
            if(nbCategories > 0){
                query += ", avg(u.vector[0]) AS a0";
                for(int i=1; i<nbCategories; i++)
                    query += ", avg(u.vector["+i+"]) AS a"+i;
            }
            query += " SET g.vector = [";
            if(nbCategories > 0){
                query+="a0";
                for(int i=1; i<nbCategories; i++)
                    query += ", a"+i;
            }
            query += "]";

            try(ResultSet set = connect.createStatement().executeQuery(query)){
            } catch(Exception e){
                System.out.println(e);
            }
        }
    public void calculVectorGroupes(){
        int nbGroup = getNbGroup();
        for(int i=0; i<nbGroup; i++)
            calculVectorGroupe(i);
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
    public HashMap<String, String> getDocsCategories(){
        HashMap<String, String> out = new HashMap<String, String>();
        String query = "MATCH (d:Doc)--(c:Categorie) RETURN d.name AS doc, c.name AS cat";
        try(ResultSet set = connect.createStatement().executeQuery(query)){
            while(set.next()){
                String cat = set.getString("cat");
                String doc = set.getString("doc");
                out.put(doc, cat);
            }
            return out;
        } catch(Exception e){
            System.out.println(e);
            return new HashMap<String, String>();
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
        try(ResultSet set = connect.createStatement().executeQuery("match (n:Categorie) return n.name")){
            List<String> out = new ArrayList<String>();

            while(set.next())
                out.add(set.getString("n.name"));

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
    private double distance(Vector<Double> i1, Vector<Double> i2){
        double out = 0.;
        for(int i=0; i<i1.size(); i++)
            out += Math.pow( (i1.get(i)-i2.get(i)), 2);
        return out;
    }
}
