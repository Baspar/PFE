package test;

import java.util.Vector;
import java.util.ArrayList;
import java.util.List;

import dao.DAO;

public class testDAO {
    public static void main(String[] args){
        DAO dao = new DAO();
        if(!dao.isConnected())
            return;

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
        isOk = dao.addDocument("Un chateau", "Peche");
        System.out.println(isOk==0?" DONE":isOk==1?" ERROR ["+isOk+"]  (Categorie inexistante)":" ERROR ["+isOk+"]  (Document deja present)");
        System.out.print("Ajout document \"Un pied\"...");
        isOk = dao.addDocument("Un pied", "Peche");
        System.out.println(isOk==0?" DONE":isOk==1?" ERROR ["+isOk+"]  (Categorie inexistante)":" ERROR ["+isOk+"]  (Document deja present)");
        System.out.print("Ajout document \"14 Vaches\"...");
        isOk = dao.addDocument("14 Vaches", "Peche");
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

        Vector<String> sessionTest = new Vector<String>();
        sessionTest.add("Un chateau");
        sessionTest.add("Livre idiot");
        sessionTest.add("Ma passion");
        sessionTest.add("Un pied");

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
        System.out.print("Ajout session2 à user2...");
        isOk = dao.addSession("User2", session2);
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


        System.out.println("Debut requete...");
        System.out.println(dao.guessNextDocs("User6", sessionTest));
        System.out.println("DONE");
    }
}
