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
            dao.addUserIfNotPresent("User1");
            dao.addUserIfNotPresent("User2");
            dao.addUserIfNotPresent("User3");
            dao.addUserIfNotPresent("User4");
            dao.addUserIfNotPresent("User5");
            dao.addUserIfNotPresent("User6");
            dao.addUserIfNotPresent("User7");
            dao.addUserIfNotPresent("User8");
            dao.addUserIfNotPresent("User9");
            dao.changeNumberOfGroup(5);
            dao.addCategorie("Chasse");
            dao.addCategorie("Peche");

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

            dao.addSession("User1", session);
            System.out.println("OK");
            dao.addSession("User2", session2);
            System.out.println("OK");
            dao.addSession("User1", session1);
            System.out.println("OK");
            //dao.guessNextDocs("User6", session2);
        } catch (Exception e){}
    }
}
