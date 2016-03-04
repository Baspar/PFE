import java.util.Vector;
import java.util.Hashtable;

public class X{
    private Vector<User> users;
    private Vector<Pair<Integer, Integer>> centers;
    private int nbCenter;
    private Vector<Vector<User>> groups;
    private Documents documents;

    public X(int nbCenter){//DONE
        this.nbCenter = nbCenter;
        users = new Vector<User>();
        centers = new Vector<Pair<Integer, Integer>>();
        groups = new Vector<Vector<User>>();
        documents = new Documents();
    }
    public void createNewDoc(int cat){//DONE
        documents.createNewDoc(cat);
    }
    public void createNewUser(){//DONE
        users.add(new User(-1, documents));
    }
    public void addNewSession(int idUser, Vector<Document> session){//DONE
        users.get(idUser).ajouterSession(session);
    }
}
