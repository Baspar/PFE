public class Document{
    //Parametre
    private String chemin;
    private int categorie;

    //Constructeur
    public Document(String chemin, int categorie){//DONE
        this.chemin = chemin;
        this.categorie=categorie;
    }

    //Methodes
    public int getCategorie(){//DONE
        return this.categorie;
    }
    public String getChemin(){//DONE
        return this.chemin;
    }
}
