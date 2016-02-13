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
    public void ajouterSession(Vector<Document> session){//TODO
    }
    public Vector<Vector<Pair<Integer, Float>>> guessNextDocs(Vector<Document> session){//TODO
        Vector<Vector<Pair<Integer, Float>>> out = new Vector<Vector<Pair<Integer, Float>>>();
        return out;
    }
}
