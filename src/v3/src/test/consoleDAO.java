package test;

import java.util.Map;
import java.util.HashMap;
import java.util.Vector;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import dao.DAO;

public class consoleDAO {
    private static DAO dao;
    private static void console(){
        String instruction="";
        String userConnected="";
        Scanner in = new Scanner(System.in);
        for(int i=0; i<200; i++)
            System.out.println();
        System.out.println("Bienvenue, tapez HELP pour afficher l'aide.");
        while(!instruction.equals("EXIT") && !instruction.equals("Q")){
            System.out.println();

            System.out.print((userConnected.equals("")?"#####":userConnected)+"> ");
            String[] line = in.nextLine().split("\\s+");

            System.out.println();

            if(line.length > 0){
                instruction = line[0].toUpperCase();
                if(instruction.equals("EXIT") || instruction.equals("Q")){
                    System.out.println("   Fermeture du programme");
                } else if (instruction.equals("HELP")){
                    System.out.println("   Liste des commandes:");
                    System.out.println("     HELP => Afficher l'aide");
                    System.out.println("     LIST DOC/CAT/USER => Lister les documents, catégories, utilisateurs");
                    System.out.println("     ADD DOC/CAT/USER/SESSION => Ajouter des documents, catégories, utilisateurs, sessions");
                    System.out.println("     GUESS => Faire une demande de prédiction");
                    System.out.println("     CHGRP <nombre> => Changer le nombre de groupes");
                    System.out.println("     TEST => Lancer un environnement de test");
                    System.out.println("     UNIT => Lancer un environnement de test unitaire");
                    System.out.println("     CONNECT <user> => Connexion");
                    System.out.println("     KMEANS => Effectuer un K-Means");
                    System.out.println("     DISCONNECT <user> => Déconnexion");
                    System.out.println("     CLEARDB => Effacer la base de donnée");
                    System.out.println("     C => Effacer l'écran");
                    System.out.println("     EXIT => Quitter");
                } else if (instruction.equals("UNIT")){//OK
                    System.out.println("   Lancement du test unitaire...:");
                    testUnit();
                } else if (instruction.equals("TEST")){//OK
                    System.out.println("   Lancement du test...:");
                    testRandom();
                } else if (instruction.equals("GUESS")){//OK
                    if(userConnected.equals("")){
                        System.out.println("   Connectez vous avant de faire une demande prédiction");
                    } else {
                        int i=0;
                        Vector<String> session = new Vector<String>();
                        String doc="";
                        System.out.println("   Rentrez les noms des documents. Valider='.' et quitter='x' ");
                        while(!doc.equals(".") && !doc.equals("x")){
                            System.out.print("   Rentrez le nom du document #"+i+": ");
                            doc = in.nextLine();
                            if(!doc.equals(".") && !doc.equals("x")){
                                if(dao.docExists(doc)){
                                    session.add(doc);
                                    i++;
                                } else {
                                    System.out.println("     Le document \""+doc+"\" n'existe pas ");
                                }
                            }
                        }
                        if(doc.equals(".")){
                            Vector<HashMap<String, Double>> results = dao.guessNextDocs(userConnected, session);
                            System.out.println("   Resultats:");
                            for(int k=0; k<results.size(); k++){
                                System.out.println("     Markov d'ordre "+(k+1));
                                for(Map.Entry<String, Double> ent : results.get(k).entrySet())
                                    System.out.println("       \""+ent.getKey()+"\" ["+ent.getValue()+"] ");
                            }
                        } else if(doc.equals("x")){
                            System.out.println("   Prediction annulée");
                        }
                    }
                } else if (instruction.equals("ADD")){//OK
                    if(line.length > 1){
                        String type = line[1].toUpperCase();
                        if(type.equals("CAT")){
                            String cat;
                            System.out.print("   Choisissez la catégorie du document: ");
                            cat = in.nextLine();
                            if(dao.addCategorie(cat)==0){
                                System.out.println("   Succès de l'ajout de la catégorie");
                            } else {
                                System.out.println("   Une catégorie portant ce nom existe déjà)");
                            }
                        }else if(type.equals("SESSION")){
                            if(userConnected.equals("")){
                                System.out.println("   Connectez vous avant d'ajouter une session ");
                            } else {
                                int i=0;
                                Vector<String> session = new Vector<String>();
                                String doc="";
                                System.out.println("   Rentrez les noms des documents. Valider='.' et quitter='x' ");
                                while(!doc.equals(".") && !doc.equals("x")){
                                    System.out.print("   Rentrez le nom du document #"+i+": ");
                                    doc = in.nextLine();
                                    if(!doc.equals(".") && !doc.equals("x")){
                                        if(dao.docExists(doc)){
                                            session.add(doc);
                                            i++;
                                        } else {
                                            System.out.println("     Le document \""+doc+"\" n'existe pas ");
                                        }
                                    }
                                }
                                if(doc.equals(".")){
                                    dao.addSession(userConnected, session);
                                    System.out.println("   Session ajoutée");
                                } else if(doc.equals("x")){
                                    System.out.println("   Session annulée");
                                }
                            }
                        }else if(type.equals("DOC")){
                            String doc, cat;
                            System.out.print("   Choisissez le nom du document: ");
                            doc = in.nextLine();
                            System.out.print("   Choisissez la catégorie du document: ");
                            cat = in.nextLine();
                            switch(dao.addDocument(doc, cat)){
                                case 1: System.out.println("   Catégorie inexistante");
                                    break;
                                case 2: System.out.println("   Un document portant ce nom existe déjà");
                                    break;
                                default: System.out.println("   Succès de l'ajout du document");
                                    break;
                            }
                        }else if(type.equals("USER")){
                            String user;
                            if(dao.getNbGroup() > 0){
                                System.out.print("   Choisissez le nom d'utilisateur: ");
                                user = in.nextLine();
                                if(dao.addUser(user)==0){
                                    System.out.println("   Succès de l'ajout d'utilisateur");
                                }else{
                                    System.out.println("   Un utilisateur portant ce nom existe déjà)");
                                }
                            } else {
                                System.out.println("   Il n'y a pas de groupe. Ajoutez-en en premier");
                            }
                        }else{
                            System.out.println("   Type \""+type+"\" non reconnu");
                        }
                    } else {
                        System.out.println("   Type d'ajout manquant");
                    }
                } else if (instruction.equals("C") || instruction.equals("L")){//OK
                    for(int i=0; i<200; i++)
                        System.out.println();
                } else if (instruction.equals("KMEANS")){//OK
                    dao.recomputeKMeans();
                } else if (instruction.equals("CHGRP")){//OK
                    if(line.length > 1){
                        int nb = Integer.parseInt(line[1]);
                        if(nb > 0){
                            dao.changeNumberOfGroup(nb);
                            System.out.println("   Passage à "+nb+" groupe(s) effectué");
                        }else{
                            System.out.println("   Veuillez rentrer un chiffre correct");
                        }
                    } else {
                        System.out.println("   Nombre de groupe manquant");
                    }
                } else if (instruction.equals("CONNECT")){//OK
                    if(line.length > 1){
                        String user = line[1];
                        if(dao.userExists(user)){
                            userConnected=user;
                            System.out.println("   Connexion OK");
                        }else{
                            System.out.println("   Nom d'utilisateur incorrect");
                        }
                    } else {
                        System.out.println("   Nom d'utilisateur manquant");
                    }
                } else if (instruction.equals("LIST")){//OK
                    if(line.length > 1){
                        String type = line[1].toUpperCase();
                        if(type.equals("DOC")){
                            System.out.println("   Liste documents | Catégorie");
                            for(Map.Entry<String, String> ent : dao.getDocsCategories().entrySet())
                                System.out.println("     "+ent.getKey()+"   | "+ent.getValue());
                        } else if (type.equals("CAT")){
                            System.out.println("   Liste catégories");
                            for(String cat : dao.getCategories())
                                System.out.println("     "+cat);
                        } else if (type.equals("USER")){
                            System.out.println("   Liste utilisateur | Groupe");
                            for(Map.Entry<String, String> ent : dao.getUsersGroups().entrySet())
                                System.out.println("     "+ent.getKey()+"   | "+ent.getValue());
                        }else{
                            System.out.println("   Type \""+type+"\" non reconnu");
                        }
                    } else {
                        System.out.println("   Type manquant (DOC, CAT, USER)");
                    }
                } else if (instruction.equals("CLEARDB")){//OK
                    if(userConnected.equals("")){
                        dao.clearDB();
                        System.out.println("   Effacement OK");
                    }else{
                        System.out.println("   Veuillez vous déconnecter pour effacer");
                    }
                } else if (instruction.equals("DISCONNECT")){//OK
                    if(!userConnected.equals("")){
                        userConnected="";
                        System.out.println("   Déconnexion réussie");
                    }else{
                        System.out.println("   Vous n'êtes pas connecté");
                    }
                } else {
                    System.out.println("   Commande \""+instruction+"\" non reconnue");
                }
            }
        }
    }
    private static void testUnit(){
        int isOk;

        System.out.print("Nettoyage Base de données...");
        dao.clearDB();
        System.out.println(" DONE");


        System.out.println();


        System.out.print("Ajout User1...");
        isOk = dao.addUser("User1");
        System.out.println(isOk==0?" DONE":" ERROR ["+isOk+"]  (Utilisateur déjà présent)");
        System.out.print("Ajout User2...");
        isOk = dao.addUser("User2");
        System.out.println(isOk==0?" DONE":" ERROR ["+isOk+"]  (Utilisateur déjà présent)");
        System.out.print("Ajout User3...");
        isOk = dao.addUser("User3");
        System.out.println(isOk==0?" DONE":" ERROR ["+isOk+"]  (Utilisateur déjà présent)");
        System.out.print("Ajout User4...");
        isOk = dao.addUser("User4");
        System.out.println(isOk==0?" DONE":" ERROR ["+isOk+"]  (Utilisateur déjà présent)");
        System.out.print("Ajout User5...");
        isOk = dao.addUser("User5");
        System.out.println(isOk==0?" DONE":" ERROR ["+isOk+"]  (Utilisateur déjà présent)");
        System.out.print("Ajout User6...");
        isOk = dao.addUser("User6");
        System.out.println(isOk==0?" DONE":" ERROR ["+isOk+"]  (Utilisateur déjà présent)");
        System.out.print("Ajout User7...");
        isOk = dao.addUser("User7");
        System.out.println(isOk==0?" DONE":" ERROR ["+isOk+"]  (Utilisateur déjà présent)");
        System.out.print("Ajout User8...");
        isOk = dao.addUser("User8");
        System.out.println(isOk==0?" DONE":" ERROR ["+isOk+"]  (Utilisateur déjà présent)");
        System.out.print("Ajout User9...");
        isOk = dao.addUser("User9");
        System.out.println(isOk==0?" DONE":" ERROR ["+isOk+"]  (Utilisateur déjà présent)");
        System.out.print("Ajout User9...");
        isOk = dao.addUser("User9");
        System.out.println(isOk==0?" DONE":" ERROR ["+isOk+"]  (Utilisateur déjà présent)");


        System.out.println();


        System.out.print("Passage à 5 groupes...");
        dao.changeNumberOfGroup(5);
        System.out.println(" DONE");


        System.out.println();


        System.out.print("Ajout catégorie Contes...");
        isOk = dao.addCategorie("Contes");
        System.out.println(isOk==0?" DONE":" ERROR ["+isOk+"]  (Catégorie déjà presente)");
        System.out.print("Ajout catégorie Recits...");
        isOk = dao.addCategorie("Recits");
        System.out.println(isOk==0?" DONE":" ERROR ["+isOk+"]  (Catégorie déjà presente)");
        System.out.print("Ajout catégorie Recits...");
        isOk = dao.addCategorie("Recits");
        System.out.println(isOk==0?" DONE":" ERROR ["+isOk+"]  (Catégorie déjà presente)");


        System.out.println();


        System.out.print("Ajout document \"Germinal\"...");
        isOk = dao.addDocument("Germinal", "Recits");
        System.out.println(isOk==0?" DONE":isOk==1?" ERROR ["+isOk+"]  (Categorie inexistante)":" ERROR ["+isOk+"]  (Document deja present)");
        System.out.print("Ajout document \"1984\"...");
        isOk = dao.addDocument("1984", "Recits");
        System.out.println(isOk==0?" DONE":isOk==1?" ERROR ["+isOk+"]  (Categorie inexistante)":" ERROR ["+isOk+"]  (Document deja present)");
        System.out.print("Ajout document \"Don Quichote\"...");
        isOk = dao.addDocument("Don Quichote", "Contes");
        System.out.println(isOk==0?" DONE":isOk==1?" ERROR ["+isOk+"]  (Categorie inexistante)":" ERROR ["+isOk+"]  (Document deja present)");
        System.out.print("Ajout document \"Hamlet\"...");
        isOk = dao.addDocument("Hamlet", "Contes");
        System.out.println(isOk==0?" DONE":isOk==1?" ERROR ["+isOk+"]  (Categorie inexistante)":" ERROR ["+isOk+"]  (Document deja present)");
        System.out.print("Ajout document \"Les fleurs du mal\"...");
        isOk = dao.addDocument("Les fleurs du mal", "Contes");
        System.out.println(isOk==0?" DONE":isOk==1?" ERROR ["+isOk+"]  (Categorie inexistante)":" ERROR ["+isOk+"]  (Document deja present)");
        System.out.print("Ajout document \"1001 nuits\"...");
        isOk = dao.addDocument("1001 nuits", "Categorie");
        System.out.println(isOk==0?" DONE":isOk==1?" ERROR ["+isOk+"]  (Categorie inexistante)":" ERROR ["+isOk+"]  (Document deja present)");
        System.out.print("Ajout document \"Les fleurs du mal\"...");
        isOk = dao.addDocument("Les fleurs du mal", "Recits");
        System.out.println(isOk==0?" DONE":isOk==1?" ERROR ["+isOk+"]  (Categorie inexistante)":" ERROR ["+isOk+"]  (Document deja present)");

        List<String> session = new ArrayList<String>();
        session.add("Germinal");
        session.add("1984");
        session.add("Don Quichote");
        session.add("Hamlet");
        session.add("Les fleurs du mal");

        Vector<String> session2 = new Vector<String>();
        session2.add("Don Quichote");
        session2.add("1984");
        session2.add("Germinal");
        session2.add("Hamlet");
        session2.add("Les fleurs du mal");
        session2.add("Germinal");

        List<String> session1 = new ArrayList<String>();
        session1.add("Don Quichote");
        session1.add("1984");
        session1.add("Germinal");
        session1.add("Hamlet");
        session1.add("Les fleurs du mal");
        session1.add("Hamlet");
        session1.add("Germinal");


        List<String> sessionBug = new ArrayList<String>();
        sessionBug.add("Don Quichote");
        sessionBug.add("1984");
        sessionBug.add("Germinal");
        sessionBug.add("Hamlet");
        sessionBug.add("1001 nuits");


        System.out.println();


        System.out.print("Ajout session à user1...");
        isOk = dao.addSession("User1", session);
        System.out.println(isOk==0?" DONE":isOk==-1?" ERROR ["+isOk+"]  (Utilisateur inexistant)":" ERROR ["+isOk+"]  (Document #"+(isOk)+" -"+session.get(isOk)+"- inexistant)");
        System.out.print("Ajout session2 à user6...");
        isOk = dao.addSession("User6", session2);
        System.out.println(isOk==0?" DONE":isOk==-1?" ERROR ["+isOk+"]  (Utilisateur inexistant)":" ERROR ["+isOk+"]  (Document #"+(isOk)+" -"+session2.get(isOk)+"- inexistant)");
        System.out.print("Ajout session1 à user1...");
        isOk = dao.addSession("User1", session1);
        System.out.println(isOk==0?" DONE":isOk==-1?" ERROR ["+isOk+"]  (Utilisateur inexistant)":" ERROR ["+isOk+"]  (Document #"+(isOk)+" -"+session1.get(isOk)+"- inexistant)");
        System.out.print("Ajout sessionBug à user1...");
        isOk = dao.addSession("User1", sessionBug);
        System.out.println(isOk==0?" DONE":isOk==-1?" ERROR ["+isOk+"]  (Utilisateur inexistant)":" ERROR ["+isOk+"]  (Document #"+(isOk)+" -"+sessionBug.get(isOk)+"- inexistant)");
        System.out.print("Ajout session1 à userBug...");
        isOk = dao.addSession("UserBug", session1);
        System.out.println(isOk==0?" DONE":isOk==-1?" ERROR ["+isOk+"]  (Utilisateur inexistant)":" ERROR ["+isOk+"]  (Document #"+(isOk)+" -"+session1.get(isOk)+"- inexistant)");


        System.out.println();


        Vector<String> sessionTest = new Vector<String>();
        sessionTest.add("Don Quichote");
        sessionTest.add("1984");
        sessionTest.add("Germinal");
        sessionTest.add("Hamlet");

        System.out.println("Debut requete...");
        System.out.println(dao.guessNextDocs("User6", sessionTest));
        System.out.println("DONE");

        dao.recomputeKMeans();
    }
    private static void testRandom(){
        int nbUsers = 10;
        int nbCategories = 5;
        int nbDocs = 10;
        int nbSessions = 50;
        int nbGroups = 4;
        int tailleSessionMin = 3;
        int tailleSessionMax = 7;
        //int nbUsers = 50;
        //int nbCategories = 20;
        //int nbDocs = 100;
        //int nbSessions = 200;
        //int nbGroups = 10;
        //int tailleSessionMin = 4;
        //int tailleSessionMax = 8;

        int isOk;

        //Clear
        System.out.print("Nettoyage Base de données...");
        dao.clearDB();
        System.out.println(" DONE");


        System.out.println();


        //Users
        for(int i=0; i<nbUsers; i++){
            System.out.print(i+"/"+nbUsers+"> Ajout User"+i+"...");
            isOk = dao.addUser("User"+i);
            System.out.println(isOk==0?" DONE":" ERROR ["+isOk+"]  (Utilisateur déjà présent)");
        }


        System.out.println();


        //Catégories
        for(int i=0; i<nbCategories; i++){
            System.out.print(i+"/"+nbCategories+"> Ajout catégorie Cat"+i+"...");
            isOk = dao.addCategorie("Cat"+i);
            System.out.println(isOk==0?" DONE":" ERROR ["+isOk+"]  (Catégorie déjà présente)");
        }


        System.out.println();


        //Documents
        for(int i=0; i<nbDocs; i++){
            int categorie = (int)Math.floor(Math.random() * nbCategories);
            System.out.print(i+"/"+nbDocs+"> Ajout document \"Doc"+i+"\" [Cat"+categorie+"]...");
            isOk = dao.addDocument("Doc"+i, "Cat"+categorie);
            System.out.println(isOk==0?" DONE":isOk==1?" ERROR ["+isOk+"]  (Categorie inexistante)":" ERROR ["+isOk+"]  (Document deja present)");
        }


        System.out.println();


        //Sessions
        for(int i=0; i<nbSessions; i++){
            int user = (int) Math.floor(Math.random() * nbUsers);
            int tailleSession = (int) Math.floor( tailleSessionMin + Math.random() * (tailleSessionMax - tailleSessionMin));

            Vector<String> session = new Vector<String>();
            for(int j=0; j<tailleSession; j++)
                session.add("Doc"+(int) Math.floor( Math.random() * nbDocs));

            System.out.print(i+"/"+nbSessions+"> Ajout "+session+" à user"+user+"...");
            isOk = dao.addSession("User"+user, session);
            System.out.println(isOk==0?" DONE":isOk==1?" ERROR ["+isOk+"]  (Utilisateur inexistant)":" ERROR ["+isOk+"]  (Document #"+(isOk-2)+" -"+session.get(isOk-2)+"- inexistant)");
        }


        System.out.println();


        System.out.print("Passage à "+nbGroups+" groupes...");
        dao.changeNumberOfGroup(nbGroups);
        System.out.println(" DONE");


        System.out.println();


        System.out.println("Execution du K-MEANS");
        dao.recomputeKMeans();
        System.out.println("DONE");
    }
    public static void main(String[] args){
        dao = new DAO();
        if(!dao.isConnected())
            return;

        console();
    }
}
