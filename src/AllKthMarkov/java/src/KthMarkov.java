import java.util.Enumeration;
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
        if(!nodes.containsKey(docs))
            nodes.put(docs, new MarkovNode(docs));
        return nodes.get(docs);
    }
    public Enumeration<MarkovNode> getNodes(){//DONE
        return nodes.elements();
    }

    //Methodes
    public void add(KthMarkov markov){//TODO
        Enumeration<MarkovNode> markovNodes = markov.getNodes();
        while(markovNodes.hasMoreElements()){
            MarkovNode mn = markovNodes.nextElement();
            MarkovNode pere = getDocs(mn.getMaChaineDoc());
            for(Map.Entry<MarkovNode, Integer> ent : mn.getChild().entrySet()){
                MarkovNode child = getDocs(ent.getKey().getMaChaineDoc());
                pere.renforcerChaine(child, ent.getValue());
            }
        }
    }
    public void renforcerChaine(Vector<Document> oldDocs, Document nextDoc){//DONE
        //Creation du vecteur nouveau document
        Vector<Document> newDocs= new Vector<Document>();
        Vector<Document> oldDocsCp= new Vector<Document>();
        for(Document doc:oldDocs){
            newDocs.add(doc);
            oldDocsCp.add(doc);
        }
        newDocs.remove(0);
        newDocs.add(nextDoc);

        //Insertion de noeuds si ces derniers n'existent pas
        MarkovNode pere = getDocs(oldDocsCp);
        MarkovNode child = getDocs(newDocs);

        //Renforcement de la chaine
        pere.renforcerChaine(child);
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
                out += "\""+doc.getChemin()+"\" ";
            out += "["+node.getValue().getTotal()+"]\n";
        }

        //Affichage liens
        for(Map.Entry<Vector<Document>, MarkovNode> node : nodes.entrySet()){
            for(Map.Entry<MarkovNode, Integer> child : node.getValue().getChild().entrySet()){
                for(Document doc : node.getKey())
                    out += "\""+doc.getChemin()+"\" ";
                out += "-("+child.getValue()+")- ";
                for(Document doc : child.getKey().getMaChaineDoc())
                    out += "\""+doc.getChemin()+"\" ";
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
                    out += doc.getChemin()+",";

                out += "> -> <";

                for(Document doc: child.getKey().getMaChaineDoc())
                    out += doc.getChemin()+",";

                out += "> [label=\"";
                out += ((float)child.getValue()/(float)node.getValue().getTotal());
                out += "\"]\n";
            }
        }
        return out;
    }
}
