import java.util.Vector;
import java.util.Hashtable;

public class Documents{
    private Vector<Document> documentsById;
    private Hashtable<Integer, Vector<Document>> documentsbyCategory;

    public Documents(){//DONE
        documentsById = new Vector<Document>();
        documentsbyCategory = new Hashtable<Integer, Vector<Document>>();
    }
    public int createNewDoc(int categorie){//DONE
        Document doc = new Document(documentsById.size(), categorie);
        documentsById.add(doc);

        if(!documentsbyCategory.contains(categorie))
            documentsbyCategory.put(categorie, new Vector<Document>());
        documentsbyCategory.get(categorie).add(doc);

        return doc.getId();
    }
    public Vector<Document> getDocumentsById(){//DONE
        return documentsById;
    }
    public int getNbCategories(){//DONE
        return documentsbyCategory.keySet().size();
    }
}
