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


    //Private
        //Groups
            /**
             * @author Laine B.
             * Création de lien Groupe -> User
             *
             * Cette méthode crée un lien, mais ne vérifie pas si un lien identique est déjà existant.
             * Aucune vérification n'est faite sur l'existence de l'utilisateur et du groupe
             * @param userName Nom de l'utilisateur
             * @param group Nom du groupe ("Groupe<num>")
             */
            private void link(String userName, String group){
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
            private void deleteEveryContains(){
                String query = "MATCH ()-[rel:CONTAINS]-() DELETE rel";
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
            private void linkToClosestGroup(String user){
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

        //K-Means
            /**
             * @author Laine B.
             * Remise à zéro des centres de groupe
             *
             * Cette méthode remet les centres des groupes à zéro.
             * Pour chaque groupe, on essaye de le placer sur un utilisateur tiré au hasard, tout en évitant que les groupes ne se superpose.
             * Si il n'y a pas assez d'utilisateur, les autres positions sont tirées au hasard, entre 0 et 1 pour chaque coordonnée, et de manière à ce que la somme soit égale à 1.
             */
            private void initialise(){
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

        //Users
            /**
             * @author Laine B.
             * Création "brute" d'un utilisateur
             *
             * Cette méthode crée un utilisateur sans vérifier si un utilisateur portant le même nom est déjà existant.
             * @param name Nom de l'utilisateur
             */
            private void addUserNotPresent(String name){
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
            /**
             * @author Laine B.
             * Augmentation du nombre de sessions d'un utilisateur
             *
             * Cette méthode incrémente le nombre de session d'un utilisateur donné.
             * Si ce dernier n'existe pas, rien n'est modifié.
             * @param user Nom de l'utilisateur
             */
        private void incrementNbSessionsUser(String user){
            String query = "MATCH (u:User {name:\""+user+"\"}) WITH u, u.nbSessions+1 AS nbSessions SET u.nbSessions = nbSessions";

            try(ResultSet set = connect.createStatement().executeQuery(query)){
            } catch(Exception e){
                System.out.println(e);
            }
        }

        //Markovs
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
            private void lier(int K, String user, List<String> oldDocs, List<String> newDocs){
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
            private void renforcerLien(int K, String user, List<String> oldDocs, List<String> newDocs){
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
            private void addMarkovNode(int K, String user, List<String> docs){
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
            private void ajouterSequence(int K, String user, List<String> oldDocs, String newDoc){
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
            private void removeFeuillesMarkov(String user){
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
            private void removeLiensVides(String user){
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
            /**
             * @author Laine B.
             * Remise à niveau des vecteurs utilisateur et groupe.
             *
             * Cette méthode va ajouter une dimension supplémentaire aux vecteurs utilisateur et de chaque groupe.
             * Cette coordonnée sera mise par défaut à zéro.
             * Cette méthode est appelée à chaque ajout de catégorie.
             */
            private void resizeVectors(){
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
            /**
             * @author Laine B.
             * Setter du vecteur de groupe.
             *
             * Cette méthode remplace le vecteur du groupe par le vecteur souhaité.
             * @param group Nom du groupe
             * @param vector Vecteur du groupe
             */
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
            /**
             * @author Laine B.
             * Calcul du vecteur de groupe.
             *
             * Cette méthode met à jour le vecteur du groupe, comme étant la moyenne des vecteur des utilisateurs inclus dans le groupe.
             * @param group Nom du groupe
             */
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
            /**
             * @author Laine B.
             * Calcul du vecteur des groupes.
             *
             * Cette méthode met à jour le vecteur de chacun des groupes.
             * Une boucle est faite avec la méthode calculVec(String);
             */
            private void calculVectorGroupes(){
                int nbGroup = getNbGroup();
                for(int i=0; i<nbGroup; i++)
                    calculVectorGroupe("Group"+i);
            }

        //Catégories
            /**
             * @author Laine B.
             * Récupération de l'ID d'une catégorie.
             *
             * Cette méthode retourne l'ID en base de donnée d'une catégorie.
             * @param cat Nom de la catégorie
             * @return -1 s'il n'existe pas de catégorie portant ce nom, sinon son ID.
             */
            private int getCategorieId(String cat){
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
            /**
             * @author Laine B.
             * Récupération nombre de catégorie.
             *
             * Cette méthode retourne le nombre de catégorie présentes en la base de donnée.
             * @return Le nombre de catégories.
             */
            private Integer getNbCategories(){
                return getNbClass("Categorie");
            }

        //Autre
            /**
             * @author Laine B.
             * Récupération du nombre de nœuds d'une catégorie.
             *
             * Cette méthode retourne le nombre de nœuds d'une catégorie.
             * Elle est appelée génériquement par d'autres méthodes de comptage.
             * @param className Nom de la catégorie.
             * @return -1 si le nom de catégorie donné n'existe pas, le nombre de nœuds sinon.
             */
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
            /**
             * @author Laine B.
             * Calcul de distance entre deux vecteurs
             *
             * Retourne la distance entre deux vecteurs de Double.
             * @param i1 Vecteur 1
             * @param i2 Vecteur 2
             * @return Distance entre les deux vecteurs.
             */
            private double distance(Vector<Double> i1, Vector<Double> i2){
                double out = 0.;
                for(int i=0; i<i1.size(); i++)
                    out += Math.pow( (i1.get(i)-i2.get(i)), 2);
                return out;
            }





    //Public
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
            /**
             * @author Laine B.
             * Récupération du nombre de groupes.
             *
             * Cette méthode retourne le nombre de groupes présents en base de donnée.
             * @return Nombre de groupes.
             */
            public Integer getNbGroup(){
                return getNbClass("Group");
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
                    addUserNotPresent(name);
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
                return getNbClass("User");
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

                    removeLiensPerimes(user);
                    removeLiensVides(user);
                    removeFeuillesMarkov(user);

                    updateUserVector(user, session);

                    for(int i=1; i<Math.min(5, session.size()); i++){
                        Vector<String> oldDocs = new Vector<String>();
                        for(int j=0; j<i; j++)
                            oldDocs.add(session.get(j));
                        String newDoc = session.get(i);

                        ajouterSequence(i, user, oldDocs, newDoc);

                        for(int j=i+1; j<session.size(); j++){
                            oldDocs.remove(0);
                            oldDocs.add(session.get(j-1));
                            newDoc = session.get(j);
                            ajouterSequence(i, user, oldDocs, newDoc);
                        }
                    }

                    incrementNbSessionsUser(user);
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

        //Vectors
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
