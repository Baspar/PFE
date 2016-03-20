import java.util.Vector;

public class AllKthMarkov{
    private int K;
    private Vector<KthMarkov> markovs;

    //Constructeur
    public AllKthMarkov(int K){//DONE
        this.K = K;
        markovs = new Vector<KthMarkov>();
        for(int i=0; i<K; i++)
            markovs.add(new KthMarkov());
    }

    //Getter
    public int getK(){//DONE
        return K;
    }
    public KthMarkov getMarkov(int i){//DONE
        return markovs.get(i);
    }

    //Methodes
    private void ajouterDocs(int K, Vector<Document> oldDocs, Document nextDoc){//DONE
        markovs.get(K-1).renforcerChaine(oldDocs, nextDoc);
    }
    public void add(AllKthMarkov all){//DONE
        for(int i=0; i<K; i++)
            markovs.get(i).add(all.getMarkov(i));
    }
    public void ajouterSession(Vector<Document> session){//DONE
        for(int i=1; i<=K; i++)
            if(session.size()>i){
                //Initialisation des old/next documents
                Vector<Document> oldDocs = new Vector<Document>();
                for(int j=0; j<i; j++)
                    oldDocs.add(session.get(j));
                Document nextDoc = session.get(i);

                ajouterDocs(i, oldDocs, nextDoc);

                for(int j=i+1; j<session.size(); j++){
                    oldDocs.remove(0);
                    oldDocs.add(nextDoc);
                    nextDoc = session.get(j);

                    ajouterDocs(i, oldDocs, nextDoc);
                }
            }
    }
    public Vector<Vector<Pair<Document, Float>>> guessNextDocs(Vector<Document> session){//DONE
        Vector<Vector<Pair<Document, Float>>> out = new Vector<Vector<Pair<Document, Float>>>();
        Vector<Document> usedSession = new Vector<Document>();

        for(int i=0; i<Math.min(K, session.size()); i++)
            usedSession.add(session.get(i));

        for(int longueurSession=usedSession.size()-1; longueurSession>=0; longueurSession--){
            MarkovNode markovNode = markovs.get(longueurSession).getDocs(usedSession);
            if(markovNode != null)
                out.add(markovNode.guessNextDocs());
            else
                out.add(new Vector<Pair<Document, Float>>());
        }

        return out;
    }
    public String toSave(){//WIP
        String out="";
        return out;
    }
    public String toUML(){//WIP
        String out="";
        return out;
    }
}
