public class Document{
    //Parametre
    private int id;
    private int categorie;

    //Constructeur
    public Document(int id, int categorie){//DONE
        this.id=id;
        this.categorie=categorie;
    }

    //Getters
    public int getCategorie(){//DONE
        return this.categorie;
    }
    public int getId(){//DONE
        return this.id;
    }
}
