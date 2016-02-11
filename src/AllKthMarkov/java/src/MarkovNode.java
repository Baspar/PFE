import java.util.Vector;
import java.util.Hashtable;

public class MarkovNode{
    //Parametres
    private Vector<Integer> maChaineDoc;
    private Hashtable<MarkovNode,Integer> child;
    private int total;

    //Constructeur
    public MarkovNode(Vector<Integer> chaine){//DONE
        child=new Hashtable<MarkovNode, Integer>();
        maChaineDoc=new Vector<Integer>();

        total=0;
        maChaineDoc.addAll(chaine);
    }

    //Methodes
    public Integer getLastDoc(){//DONE
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
    public Vector<Pair<Integer,Float>> guessNextDocs(){//DONE
        Vector<Pair<Integer,Float>> out=new Vector<Pair<Integer,Float>>();

        for(MarkovNode node:child.keySet()){
            //Récupération ID document suivant
            Integer idLastDoc=node.getLastDoc();

            //Calcul probabilité
            Float prob=((float)child.get(node))/(float)total;

            //Ajout
            out.add(new Pair<Integer, Float>(idLastDoc, prob));
        }

        return out;
    }
    public void addChild(MarkovNode node, int poids){//DONE
        child.put(node, poids);
    }

    //Getter
    public Vector<Integer> getMaChaineDoc(){//DONE
        return maChaineDoc;
    }
}
