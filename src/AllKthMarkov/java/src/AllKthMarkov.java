import java.util.Vector;

public class AllKthMarkov{
    private int K;
    private Vector<KthMarkov> markovs;

    public AllKthMarkov(String fileName){//TODO
    }
    public KthMarkov getMarkov(int i){//DONE
        return markovs.get(i);
    }
    private void ajouterDocs(int K, Vector<Document> oldDocs, Document nextDoc){//DONE
        markovs.get(K-1).renforcerChaine(oldDocs, nextDoc);
    }
    public void ajouterSession(Vector<Document> session){//DONE
        Vector<Vector<Document>> oldDocs = new Vector<Vector<Document>>(K);
        Vector<Document> nextDocs = new Vector<Document>();

        for(int i=0; i<K; i++)
            nextDocs.add(new Document(-1, -1));

        for(Document doc: session){
            for(int k=1; k<K+1; k++){
                if(nextDocs.get(k-1).getId() != -1)
                    oldDocs.get(k-1).add(nextDocs.get(k-1));

                nextDocs.set(k-1, doc);

                if(oldDocs.get(k-1).size() > k)
                    oldDocs.get(k-1).remove(0);//TODO

                if(oldDocs.get(k-1).size() == k)
                    ajouterDocs(k, oldDocs.get(k-1), nextDocs.get(k-1));
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
}
