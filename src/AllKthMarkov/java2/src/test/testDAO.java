package test;

import java.util.Vector;
import java.util.ArrayList;
import java.util.List;

import dao.DAO;

public class testDAO {
    private static DAO dao;
    private static void test(){
        int isOk;

        System.out.print("Nettoyage Base de données...");
        dao.clearDB();
        System.out.println(" DONE");


        System.out.println();


        System.out.print("Ajout User1...");
        isOk = dao.addUserIfNotPresent("User1");
        System.out.println(isOk==0?" DONE":" ERROR ["+isOk+"]  (Utilisateur déjà présent)");
        System.out.print("Ajout User2...");
        isOk = dao.addUserIfNotPresent("User2");
        System.out.println(isOk==0?" DONE":" ERROR ["+isOk+"]  (Utilisateur déjà présent)");
        System.out.print("Ajout User3...");
        isOk = dao.addUserIfNotPresent("User3");
        System.out.println(isOk==0?" DONE":" ERROR ["+isOk+"]  (Utilisateur déjà présent)");
        System.out.print("Ajout User4...");
        isOk = dao.addUserIfNotPresent("User4");
        System.out.println(isOk==0?" DONE":" ERROR ["+isOk+"]  (Utilisateur déjà présent)");
        System.out.print("Ajout User5...");
        isOk = dao.addUserIfNotPresent("User5");
        System.out.println(isOk==0?" DONE":" ERROR ["+isOk+"]  (Utilisateur déjà présent)");
        System.out.print("Ajout User6...");
        isOk = dao.addUserIfNotPresent("User6");
        System.out.println(isOk==0?" DONE":" ERROR ["+isOk+"]  (Utilisateur déjà présent)");
        System.out.print("Ajout User7...");
        isOk = dao.addUserIfNotPresent("User7");
        System.out.println(isOk==0?" DONE":" ERROR ["+isOk+"]  (Utilisateur déjà présent)");
        System.out.print("Ajout User8...");
        isOk = dao.addUserIfNotPresent("User8");
        System.out.println(isOk==0?" DONE":" ERROR ["+isOk+"]  (Utilisateur déjà présent)");
        System.out.print("Ajout User9...");
        isOk = dao.addUserIfNotPresent("User9");
        System.out.println(isOk==0?" DONE":" ERROR ["+isOk+"]  (Utilisateur déjà présent)");
        System.out.print("Ajout User9...");
        isOk = dao.addUserIfNotPresent("User9");
        System.out.println(isOk==0?" DONE":" ERROR ["+isOk+"]  (Utilisateur déjà présent)");
        for(int i=10; i<100; i++){
            System.out.print("Ajout User"+i+"...");
            isOk = dao.addUserIfNotPresent("User"+i);
            System.out.println(isOk==0?" DONE":" ERROR ["+isOk+"]  (Utilisateur déjà présent)");
        }


        System.out.println();


        System.out.print("Passage à 5 groupes...");
        dao.changeNumberOfGroup(5);
        System.out.println(" DONE");


        System.out.println();


        System.out.print("Ajout catégorie Chasse...");
        isOk = dao.addCategorie("Chasse");
        System.out.println(isOk==0?" DONE":" ERROR ["+isOk+"]  (Catégorie déjà presente)");
        System.out.print("Ajout catégorie Peche...");
        isOk = dao.addCategorie("Peche");
        System.out.println(isOk==0?" DONE":" ERROR ["+isOk+"]  (Catégorie déjà presente)");
        System.out.print("Ajout catégorie Peche...");
        isOk = dao.addCategorie("Peche");
        System.out.println(isOk==0?" DONE":" ERROR ["+isOk+"]  (Catégorie déjà presente)");


        System.out.println();


        System.out.print("Ajout document \"Ma passion\"...");
        isOk = dao.addDocument("Ma passion", "Peche");
        System.out.println(isOk==0?" DONE":isOk==1?" ERROR ["+isOk+"]  (Categorie inexistante)":" ERROR ["+isOk+"]  (Document deja present)");
        System.out.print("Ajout document \"Livre idiot\"...");
        isOk = dao.addDocument("Livre idiot", "Peche");
        System.out.println(isOk==0?" DONE":isOk==1?" ERROR ["+isOk+"]  (Categorie inexistante)":" ERROR ["+isOk+"]  (Document deja present)");
        System.out.print("Ajout document \"Un chateau\"...");
        isOk = dao.addDocument("Un chateau", "Chasse");
        System.out.println(isOk==0?" DONE":isOk==1?" ERROR ["+isOk+"]  (Categorie inexistante)":" ERROR ["+isOk+"]  (Document deja present)");
        System.out.print("Ajout document \"Un pied\"...");
        isOk = dao.addDocument("Un pied", "Chasse");
        System.out.println(isOk==0?" DONE":isOk==1?" ERROR ["+isOk+"]  (Categorie inexistante)":" ERROR ["+isOk+"]  (Document deja present)");
        System.out.print("Ajout document \"14 Vaches\"...");
        isOk = dao.addDocument("14 Vaches", "Chasse");
        System.out.println(isOk==0?" DONE":isOk==1?" ERROR ["+isOk+"]  (Categorie inexistante)":" ERROR ["+isOk+"]  (Document deja present)");
        System.out.print("Ajout document \"15 Vaches\"...");
        isOk = dao.addDocument("15 Vaches", "AAA");
        System.out.println(isOk==0?" DONE":isOk==1?" ERROR ["+isOk+"]  (Categorie inexistante)":" ERROR ["+isOk+"]  (Document deja present)");
        System.out.print("Ajout document \"14 Vaches\"...");
        isOk = dao.addDocument("14 Vaches", "Peche");
        System.out.println(isOk==0?" DONE":isOk==1?" ERROR ["+isOk+"]  (Categorie inexistante)":" ERROR ["+isOk+"]  (Document deja present)");

        List<String> session = new ArrayList<String>();
        session.add("Ma passion");
        session.add("Livre idiot");
        session.add("Un chateau");
        session.add("Un pied");
        session.add("14 Vaches");

        Vector<String> session2 = new Vector<String>();
        session2.add("Un chateau");
        session2.add("Livre idiot");
        session2.add("Ma passion");
        session2.add("Un pied");
        session2.add("14 Vaches");
        session2.add("Ma passion");

        List<String> session1 = new ArrayList<String>();
        session1.add("Un chateau");
        session1.add("Livre idiot");
        session1.add("Ma passion");
        session1.add("Un pied");
        session1.add("14 Vaches");
        session1.add("Un pied");
        session1.add("Ma passion");


        List<String> sessionBug = new ArrayList<String>();
        sessionBug.add("Un chateau");
        sessionBug.add("Livre idiot");
        sessionBug.add("Ma passion");
        sessionBug.add("Un pied");
        sessionBug.add("16 Vaches");


        System.out.println();


        System.out.print("Ajout session à user1...");
        isOk = dao.addSession("User1", session);
        System.out.println(isOk==0?" DONE":isOk==1?" ERROR ["+isOk+"]  (Utilisateur inexistant)":" ERROR ["+isOk+"]  (Document #"+(isOk-2)+" -"+session.get(isOk-2)+"- inexistant)");
        System.out.print("Ajout session2 à user6...");
        isOk = dao.addSession("User6", session2);
        System.out.println(isOk==0?" DONE":isOk==1?" ERROR ["+isOk+"]  (Utilisateur inexistant)":" ERROR ["+isOk+"]  (Document #"+(isOk-2)+" -"+session.get(isOk-2)+"- inexistant)");
        System.out.print("Ajout session1 à user1...");
        isOk = dao.addSession("User1", session1);
        System.out.println(isOk==0?" DONE":isOk==1?" ERROR ["+isOk+"]  (Utilisateur inexistant)":" ERROR ["+isOk+"]  (Document #"+(isOk-2)+" -"+session.get(isOk-2)+"- inexistant)");
        System.out.print("Ajout sessionBug à user1...");
        isOk = dao.addSession("User1", sessionBug);
        System.out.println(isOk==0?" DONE":isOk==1?" ERROR ["+isOk+"]  (Utilisateur inexistant)":" ERROR ["+isOk+"]  (Document #"+(isOk-2)+" -"+session.get(isOk-2)+"- inexistant)");
        System.out.print("Ajout session1 à userBug...");
        isOk = dao.addSession("UserBug", session1);
        System.out.println(isOk==0?" DONE":isOk==1?" ERROR ["+isOk+"]  (Utilisateur inexistant)":" ERROR ["+isOk+"]  (Document #"+(isOk-2)+" -"+session.get(isOk-2)+"- inexistant)");


        System.out.println();


        Vector<String> sessionTest = new Vector<String>();
        sessionTest.add("Un chateau");
        sessionTest.add("Livre idiot");
        sessionTest.add("Ma passion");
        sessionTest.add("Un pied");

        System.out.println("Debut requete...");
        System.out.println(dao.guessNextDocs("User6", sessionTest));
        System.out.println("DONE");

        dao.calculVectorGroupes();

        dao.recompute();
    }
    private static void testRandom(boolean justRequest){
        int nbUsers = 10;
        int nbCategories = 20;
        int nbDocs = 50;
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

        if(!justRequest){
            //Clear
            System.out.print("Nettoyage Base de données...");
            dao.clearDB();
            System.out.println(" DONE");


            System.out.println();


            //Users
            for(int i=0; i<nbUsers; i++){
                System.out.print(i+"/"+nbUsers+"> Ajout User"+i+"...");
                isOk = dao.addUserIfNotPresent("User"+i);
                System.out.println(isOk==0?" DONE":" ERROR ["+isOk+"]  (Utilisateur déjà présent)");
            }


            System.out.println();


            //Categories
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
        }
        //Sessions
        for(int i=0; i<2; i++){
            int user = (int) Math.floor(Math.random() * nbUsers);
            int tailleSession = (int) Math.floor( tailleSessionMin + Math.random() * (tailleSessionMax - tailleSessionMin));

            Vector<String> session = new Vector<String>();
            for(int j=0; j<tailleSession; j++)
                session.add("Doc"+(int) Math.floor( Math.random() * nbDocs));

            System.out.print(i+"/"+nbSessions+"> Ajout "+session+" à user"+user+"...");
            isOk = dao.addSession("User"+user, session);
            System.out.println(isOk==0?" DONE":isOk==1?" ERROR ["+isOk+"]  (Utilisateur inexistant)":" ERROR ["+isOk+"]  (Document #"+(isOk-2)+" -"+session.get(isOk-2)+"- inexistant)");
        }

        System.out.println("Avant:");
        System.out.println(dao.getUsersGroups());

        dao.recompute();

        System.out.println("Après:");
        System.out.println(dao.getUsersGroups());


    }
    public static void main(String[] args){
        dao = new DAO();
        if(!dao.isConnected())
            return;

        //test();
        //testRandom(false);
        testRandom(true);

    }
}
