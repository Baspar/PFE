import java.util.Hashtable;
import java.util.Vector;
import java.util.Map;
public class KthMarkov{
    //Attributs
    private Hashtable<Vector<Document>,MarkovNode> nodes;

    //Constructeur
    public KthMarkov(){//DONE
        nodes=new Hashtable<Vector<Document>,MarkovNode>();
    }

    //Getter
    public MarkovNode getDocs(Vector<Document> docs){//DONE
        if(!nodes.contains(docs))
            return null;
        return nodes.get(docs);
    }

    //Methodes
    public void addNewNode(Vector<Document> node, int poidsNode){//DONE
        if(!nodes.contains(node)){
            nodes.put(node, new MarkovNode(node));
            nodes.get(node).setTotal(poidsNode);
        }
    }
    public void renforcerChaine(Vector<Document> oldDocs, Document nextDoc){//DONE
        //Creation du vecteur nouveau document
        Vector<Document> newDocs= new Vector<Document>();
        for(Document doc:oldDocs)
            newDocs.add(doc);
        newDocs.remove(0);
        newDocs.add(nextDoc);

        //Insertion de noeuds si ces derniers n'existent pas
        if(!nodes.contains(oldDocs))
            nodes.put(oldDocs, new MarkovNode(oldDocs));
        if(!nodes.contains(newDocs))
            nodes.put(newDocs, new MarkovNode(newDocs));

        //Renforcement de la chaine
        nodes.get(oldDocs).renforcerChaine(nodes.get(newDocs));
    }
    public String toSave(){//DONE
        String out="";

        //Nb noeud
        out+=nodes.size();
        out+=" ";

        //Nb Liens
        int nbLiens=0;
        for(MarkovNode m:nodes.values())
            nbLiens+=m.getNbChild();
        out+=nbLiens;
        out+="\n";

        //Affichage Noeuds
        for(Map.Entry<Vector<Document>, MarkovNode> node:nodes.entrySet()){
            for(Document doc:node.getKey())
                out += doc.getId()+" ";
            out += "["+node.getValue().getTotal()+"]\n";
        }

        //Affichage liens
        for(Map.Entry<Vector<Document>, MarkovNode> node : nodes.entrySet()){
            for(Map.Entry<MarkovNode, Integer> child : node.getValue().getChild().entrySet()){
                for(Document doc : node.getKey())
                    out += doc.getId()+" ";
                out += "-("+child.getValue()+")-";
                for(Document doc : child.getKey().getMaChaineDoc())
                    out += doc.getId()+" ";
                out += "\n";
            }
        }
        return out;
    }
public String toUML(){//DONE
        String out="";

        for(Map.Entry<Vector<Document>, MarkovNode> node:nodes.entrySet()){
            for(Map.Entry<MarkovNode, Integer> child : node.getValue().getChild().entrySet()){
                out += "  <";

                for(Document doc:node.getKey())
                    out += doc.getId()+",";

                out += "> -> <";

                for(Document doc: child.getKey().getMaChaineDoc())
                    out += doc.getId()+",";

                out += "> [label=\"";
                out += ((float)child.getValue()/(float)node.getValue().getTotal());
                out += "\"]\n";
            }
        }
        return out;
    }
}
