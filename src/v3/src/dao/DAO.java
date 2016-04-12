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

    //DAO
    private DAOGroup daoGroup;
    private DAOCategory daoCategory;
    private DAOKMeans daoKMeans;
    private DAOMarkov daoMarkov;
    private DAOOther daoOther;
    private DAOUser daoUser;
    private DAOVector daoVector;

    //Constructeur
    /**
     * @author Laine B.
     * Constructeur DAO.
     *
     * Cette méthode construit une DAO.
     */
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

        daoMarkov = new DAOMarkov(connect, dureeDeVie);
        daoOther = new DAOOther(connect);
        daoCategory = new DAOCategory(daoOther, connect);
        daoUser = new DAOUser(daoCategory, connect);
        daoGroup = new DAOGroup(daoOther, daoUser, daoVector, connect);
        daoVector = new DAOVector(daoUser, daoCategory, daoGroup, connect, pourcentageMinUserVector);
        daoGroup.setDaoVector(daoVector);
        daoUser.setDaoGroup(daoGroup);
        daoKMeans = new DAOKMeans(daoUser, daoVector, daoGroup, daoCategory, connect);
    }

    //Groups
    /**
     * @author Laine B.
     * Changement du nombre de groupe.
     *
     * Cette méthode change le nombre de groupes présents en base de donnée.
     * Une fois le nombre changé, elle réinitialise leur position.
     * @param n Nouveau nombre de groupes.
     */
    public void changeNumberOfGroup(int n){
        try(ResultSet set = connect.createStatement().executeQuery("match (n:Group) detach delete n")){
        } catch(Exception e){
            System.out.println(e);
        }

        int nbCategories = daoCategory.getNbCategories();

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

        daoKMeans.initialise();
    }
    /**
     * @author Laine B.
     * Récupération du nombre de groupes.
     *
     * Cette méthode retourne le nombre de groupes présents en base de donnée.
     * @return Nombre de groupes.
     */
    public Integer getNbGroup(){
        return daoGroup.getNbGroup();
    }

    //K-means
    /**
     * @author Laine B.
     * Calcul nouveaux groupes avec K-Means.
     *
     * Cette méthode calcule les nouveaux vecteurs de groupe, et les affectations avec les utilisateurs.
     * Cette méthode utilise l'algorithme K-Means.
     * L'initialisation des centres est fait via la méthode initialise.
     */
    public void recomputeKMeans(){
        daoKMeans.initialise();

        HashMap<String, Vector<Double>> userVector = daoVector.getVector("User");
        HashMap<String, Vector<Double>> groupVector = daoVector.getVector("Group");
        HashMap<String, String> usersGroups = getUsersGroups();
        HashMap<String, Integer> nbUsersInGroups = daoUser.getNbUsersInGroups();

        boolean hasChanged = true;

        while(hasChanged){
            hasChanged = false;
            System.out.println("\n===============================");

            for(String user : userVector.keySet()){
                double distMin = daoGroup.distance(userVector.get(user), groupVector.get(usersGroups.get(user)));
                String groupMin = usersGroups.get(user);

                System.err.println("  "+user+": "+groupMin+"["+distMin+"]");

                for(Map.Entry<String, Vector<Double>> ent : groupVector.entrySet()){
                    double newDist = daoGroup.distance(userVector.get(user), ent.getValue());
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

        daoGroup.deleteEveryContains();
        for(Map.Entry<String, String> ent : usersGroups.entrySet())
            daoGroup.link(ent.getKey(), ent.getValue());
        daoVector.calculVectorGroupes();
    }

    //Users
    /**
     * @author Laine B.
     * Récupération couple Utilisateur - Groupe.
     *
     * Cette méthode retourne une table contenant pour chaque utilisateur le groupe qui lui est affecté.
     * @return Table Utilisateur - Groupe
     */
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
    /**
     * @author Laine B.
     * Existence de l'utilisateur
     *
     * Cette méthode retourne true si l'utilisateur existe, false sinon.
     * @param name Nom de l'utilisateur
     * @return true si l'utilisateur existe, false sinon.
     */
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
    /**
     * @author Laine B.
     * Ajout d'un utilisateur
     *
     * Cette méthode va ajouter un utilisateur si ce dernier n'est pas présent.
     * Le cas échéant, rien ne sera changé, et la méthode renverra 1.
     * @param name Nom de l'utilisateur
     * @return 0 si l'utilisateur n'existait pas déjà, 1 sinon.
     */
    public int addUser(String name){
        if(!userExists(name)){
            daoUser.addUserNotPresent(name);
            return 0;
        }
        return 1;
    }
    /**
     * @author Laine B.
     * Récupération du nom du groupe où est l'utilisateur
     *
     * Cette méthode revoie le nom du groupe où est situé l'utilisateur.
     * Dans le cas où  n'existerait pas, la méthode reverra une chaine vide.
     * @param name Nom de l'utilisateur
     * @return Une chaine vide si l'utilisateur n'existe pas, sinon le nom de son groupe.
     */
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
    /**
     * @author Laine B.
     * Récupération du nombre d'utilisateurs
     *
     * Cette méthode retourne le nombre d'utilisateurs présents en base de donnée.
     * @return Le nombre d'utilisateurs.
     */
    public Integer getNbUser(){
        return daoOther.getNbClass("User");
    }
    /**
     * @author Laine B.
     * Récupération liste d'utilisateur
     *
     * Cette méthode revoit une liste contenant les noms de tout les utilisateurs mis en mémoire.
     * @return Liste des noms d'utilisateur
     */
    public List<String> getUserNames(){
        return daoUser.getUserNames();
    }

    //Markovs
    /**
     * @author Laine B.
     * Ajout d'une session
     *
     * Cette méthode va ajouter une session à un utilisateur donné.
     * Si un document, ou l'utilisateur n'existe pas, une erreur sera renvoyée et rien ne sera modifié.
     * @param user Nom de l'utilisateur
     * @param session Session/liste de document
     * @return -1 si l'utilisateur n'existe pas, X si le document numéro X de la session n'existe pas, 0 sinon.
     */
    public int addSession(String user, List<String> session){
        if(userExists(user)){
            for(int i=0; i<session.size(); i++)
                if(!docExists(session.get(i)))
                    return i;

            daoMarkov.removeLiensPerimes(user);
            daoMarkov.removeLiensVides(user);
            daoMarkov.removeFeuillesMarkov(user);

            for(int i=1; i<Math.min(5, session.size()); i++){
                Vector<String> oldDocs = new Vector<String>();
                for(int j=0; j<i; j++)
                    oldDocs.add(session.get(j));
                String newDoc = session.get(i);

                daoMarkov.ajouterSequence(i, user, oldDocs, newDoc);

                for(int j=i+1; j<session.size(); j++){
                    oldDocs.remove(0);
                    oldDocs.add(session.get(j-1));
                    newDoc = session.get(j);
                    daoMarkov.ajouterSequence(i, user, oldDocs, newDoc);
                }
            }

            daoVector.updateUserVector(user, session);

            daoGroup.linkToClosestGroup(user);

            daoUser.incrementNbSessionsUser(user);
            return 0;
        } else {
            return -1;
        }
    }
    /**
     * @author Laine B.
     * Prédiction d'un ensemble de documents pertinents
     *
     * Cette méthode va retourner une liste (Dont l'indice correspond à la markov utilisée) de documents pertinents.
     * Chacune des cellules de la liste contiendra une table faisant lien enter un document, et sa probabilité d'être pertinent.
     * @param user Nom de l'utilisateur
     * @param session Session/liste de document
     * @return Liste de table Document - Probabilité
     */
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

    //Documents
    /**
     * @author Laine B.
     * Ajout document
     *
     * Cette méthode ajoute un document en base de donnée, et le lie à une catégorie.
     * @param doc Nom du document
     * @param categorie Nom de la catégorie
     * @return 1 si la catégorie n'existe pas, 2 si un document ayant le même nom est déjà présent, 0 sinon.
     */
    public int addDocument(String doc, String categorie){
        if(daoCategory.getCategorieId(categorie) != -1){
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
    /**
     * @author Laine B.
     * Existence de document
     *
     * Cette méthode retourne true si le document existe, false sinon.
     * @param doc
     * @return true si le document existe, false sinon
     */
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
    /**
     * @author Laine B.
     * Récupération table Document - Catégorie
     *
     * Cette méthode retourne une table qui fait correspondre à chaque document la catégorie à laquelle il appartient.
     * @return Table de documents - catégorie
     */
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
    /**
     * @author Laine B.
     * Récupération catégorie d'un document
     *
     * Cette méthode retourne pour un document donné l'ID de sa catégorie
     * @param doc Nom du document
     * @return ID de la catégorie
     */
    public int getCategorie(String doc){
        return daoCategory.getCategorie(doc);
    }

    //Catégories
    /**
     * @author Laine B.
     * Récupération catégories
     *
     * Cette méthode retourne la liste de catégorie présentes en base de donnée.
     * @return Liste de catégories
     */
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
    /**
     * @author Laine B.
     * Ajout catégorie
     *
     * Cette méthode ajoute une catégorie en base de donnée
     * @param categorie
     * @return 1 si la catégorie est déjà présente, 0 sinon
     */
    public int addCategorie(String categorie){
        int nbCategories = daoCategory.getNbCategories();
        if(daoCategory.getCategorieId(categorie)==-1){
            try(ResultSet set = connect.createStatement().executeQuery("create ("+categorie+":Categorie {name:\""+categorie+"\", cpt:"+nbCategories+"})")){
                daoVector.resizeVectors();
                return 0;
            } catch(Exception e){
                System.out.println(e);
                return 1;
            }
        }
        return 1;
    }

    //DB
    /**
     * @author Laine B.
     * Effacement base de donnée
     *
     * Cette méthode retire tout les nœuds et les liens de la base de donnée.
     */
    public void clearDB(){
        try(ResultSet out = connect.createStatement().executeQuery("match (n) detach delete n")){
        } catch(Exception e){
            System.out.println(e);
        }
    }
    /**
     * @author Laine B.
     * Vérification connexion
     *
     * Cette méthode vérifie si l'application est bien connectée à la base de donnée.
     * @return true si la base de donnée est accessible, false sinon
     */
    public boolean isConnected(){
        return isConnected;
    }
}
