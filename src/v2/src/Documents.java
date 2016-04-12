import java.util.Vector;
import java.util.Hashtable;

public class Documents{
    private Hashtable<String,Document> documentsByChemin;
    private Hashtable<Integer,Vector<Document>> documentsByCategory;

    //Constructeur
    public Documents(){//DONE
        documentsByChemin = new Hashtable<String, Document>();
        documentsByCategory = new Hashtable<Integer, Vector<Document>>();
    }

    //Getter
    public Hashtable<String,Document> getDocumentsByChemin(){//DONE
        return documentsByChemin;
    }
    public int getNbCategories(){//DONE
        return documentsByCategory.keySet().size();
    }
    public Document getDocument(String chemin){//DONE
        return documentsByChemin.get(chemin);
    }

    //Methodes
    public void createNewDoc(String chemin, int categorie){//DONE
        Document doc = new Document(chemin, categorie);
        documentsByChemin.put(chemin, doc);

        if(!documentsByCategory.containsKey(categorie))
            documentsByCategory.put(categorie, new Vector<Document>());
        documentsByCategory.get(categorie).add(doc);
    }
}
