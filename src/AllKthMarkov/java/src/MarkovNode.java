import java.util.Vector;
import java.util.Hashtable;

public class MarkovNode{
    //Parametres
    private Vector<Document> maChaineDoc;
    private Hashtable<MarkovNode,Integer> child;
    private int total;

    //Constructeur
    public MarkovNode(Vector<Document> chaine){//DONE
        child=new Hashtable<MarkovNode, Integer>();
        maChaineDoc=new Vector<Document>();

        total=0;
        maChaineDoc.addAll(chaine);
    }

    //Methodes
    private Document getLastDoc(){//DONE
        return maChaineDoc.get(maChaineDoc.size()-1);
    }
    public void renforcerChaine(MarkovNode nextChaine){//DONE
        //Augmentation nombre de passage
        total++;

        //Récupération et augmentation passage nextChaine
        Integer old=child.get(nextChaine);
        if(old==null)
            child.put(nextChaine, 1);
        else
            child.put(nextChaine, old+1);
    }
    public Vector<Pair<Document,Float>> guessNextDocs(){//DONE
        Vector<Pair<Document,Float>> out=new Vector<Pair<Document,Float>>();

        for(MarkovNode node:child.keySet()){
            //Récupération ID document suivant
            Document idLastDoc=node.getLastDoc();

            //Calcul probabilité
            Float prob=((float)child.get(node))/(float)total;

            //Ajout
            out.add(new Pair<Document, Float>(idLastDoc, prob));
        }

        return out;
    }
    public void addChild(MarkovNode node, int poids){//DONE
        child.put(node, poids);
    }

    //Getter
    public Vector<Document> getMaChaineDoc(){//DONE
        return maChaineDoc;
    }
    public int getNbChild(){//DONE
        return child.size();
    }
    public int getTotal(){//DONE
        return this.total;
    }
    public Hashtable<MarkovNode,Integer> getChild(){//DONE
        return this.child;
    }

    //Setter
    public void setChild(MarkovNode markovNode, Integer nb){//DONE
        child.put(markovNode, nb);
    }
    public void setTotal(int total){//DONE
        this.total=total;
    }
}
