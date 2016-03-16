package test;

import java.util.Vector;
import java.util.ArrayList;
import java.util.List;

import dao.DAO;

public class testDAO {
    public static void main(String[] args){
        DAO dao = new DAO();
        try{
            dao.clearDB();
            System.out.print("Ajout User1");
            dao.addUserIfNotPresent("User1");
            System.out.println(" DONE");
            System.out.print("Ajout User2...");
            dao.addUserIfNotPresent("User2");
            System.out.println(" DONE");
            System.out.print("Ajout User3...");
            dao.addUserIfNotPresent("User3");
            System.out.println(" DONE");
            System.out.print("Ajout User4...");
            dao.addUserIfNotPresent("User4");
            System.out.println(" DONE");
            System.out.print("Ajout User5...");
            dao.addUserIfNotPresent("User5");
            System.out.println(" DONE");
            System.out.print("Ajout User6...");
            dao.addUserIfNotPresent("User6");
            System.out.println(" DONE");
            System.out.print("Ajout User7...");
            dao.addUserIfNotPresent("User7");
            System.out.println(" DONE");
            System.out.print("Ajout User8...");
            dao.addUserIfNotPresent("User8");
            System.out.println(" DONE");
            System.out.print("Ajout User9...");
            dao.addUserIfNotPresent("User9");
            System.out.println(" DONE");


            System.out.println();


            System.out.print("Ajout de 5 groupes...");
            dao.changeNumberOfGroup(5);
            System.out.println(" DONE");


            System.out.println();


            System.out.print("Ajout de 2 catégories...");
            dao.addCategorie("Chasse");
            dao.addCategorie("Peche");
            System.out.println(" DONE");

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


            System.out.println();


            System.out.print("Ajout session à user1...");
            dao.addSession("User1", session);
            System.out.println("DONE");
            System.out.print("Ajout session2 à user2...");
            dao.addSession("User2", session2);
            System.out.println("DONE");
            System.out.print("Ajout session1 à user1...");
            dao.addSession("User1", session1);
            System.out.println("DONE");


            System.out.println();


            System.out.println("Debut requete...");
            System.out.println(dao.guessNextDocs("User1", session2));
            System.out.println("DONE");
        } catch (Exception e){}
    }
}
