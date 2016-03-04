import java.util.Vector;

public class User{
    private Vector<Vector<Document>> sessions;
    private int tailleSessionsMax; // -1 => pas de limite max
    private Vector<Double> userVector;
    private Documents documents;

    public User(int max, Documents documents){//DONE
        this.documents = documents;
        tailleSessionsMax = max;
        sessions = new Vector<Vector<Document>>();
        userVector = new Vector<Double>();
    }
    public void ajouterSession(Vector<Document> session){//DONE
        if(tailleSessionsMax != -1)
            if(sessions.size()<tailleSessionsMax)
                sessions.remove(0);

        sessions.add(session);
        recomputeUserVector();
    }
    private void recomputeUserVector(){//DONE
        int nbCat = documents.getNbCategories();

        userVector = new Vector<Double>();
        for(int i=0; i<nbCat; i++)
            userVector.add(0.);

        //Calcul du vector categorie pour chaque session
        Vector<Vector<Double>> sessionsVector = new Vector<Vector<Double>>();
        for(Vector<Document> session : sessions){
            //Initialisation vecteur de la session
            int id = sessionsVector.size();
            sessionsVector.add(new Vector<Double>());
            for(int i=0; i<nbCat; i++)
                sessionsVector.get(id).add(0.);

            //Ajout des documents de la session
            for(Document doc : session){
                int cat = doc.getCategorie();
                sessionsVector.get(id).set(cat, sessionsVector.get(id).get(cat)+1);
            }

            //Normalisation vector session
            for(int i=0; i<nbCat; i++)
                sessionsVector.get(id).set(i, sessionsVector.get(id).get(i)/sessions.size());
        }

        for(int i=0; i<nbCat; i++){
            for(Vector<Double> sessionVector : sessionsVector)
                userVector.set(i, userVector.get(i)+sessionVector.get(i));

            userVector.set(i, userVector.get(i)/sessionsVector.size());
        }
    }
}
