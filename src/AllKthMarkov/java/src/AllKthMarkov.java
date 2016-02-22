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
    public void ajouterSession(Vector<Document> session){//CHK
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
                    oldDocs.get(k-1).erase(oldDocs.get(k-1).begin());//TODO

                if(oldDocs.get(k-1).size() == k)
                    ajouterDocs(k, oldDocs.get(k-1), nextDocs.get(k-1));
            }
        }
    }
    public Vector<Vector<Pair<Integer, Float>>> guessNextDocs(Vector<Document> session){//TODO
        Vector<Vector<Pair<Integer, Float>>> out = new Vector<Vector<Pair<Integer, Float>>>();
        return out;
    }
}
