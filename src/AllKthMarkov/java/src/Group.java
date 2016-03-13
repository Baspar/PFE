import java.util.Vector;

public class Group{
    private Vector<User> users;
    private Vector<Double> center;
    private AllKthMarkov kThMarkov;
    private int nbCategories;

    //Constructeur
    public Group(int nbCategories){//DONE
        this.nbCategories = nbCategories;
        this.kThMarkov = new AllKthMarkov(4);
        this.center = new Vector<Double>();
        this.users = new Vector<User>();

        //Création Centre
        for(int i=0; i<nbCategories; i++)
            center.add(0.);
    }

    //Getter
    public Vector<Double> getCenter(){//DONE
        return this.center;
    }
    public Vector<User> getUsers(){//DONE
        return users;
    }
    public AllKthMarkov getMarkovs(){//DONE
        return kThMarkov;
    }

    //Methode
    public double distance(User user){//CHK
        double out=0;
        for(int i=0; i<nbCategories; i++)
            out += Math.pow( (user.getUserVector().get(i) - center.get(i)), 2);
        return Math.sqrt(out);
    }
    public void updateCenter(){//DONE
        //Reset center
        for(int i=0; i<nbCategories; i++)
            center.set(i, 0.);

        //Ajout coordonnées user
        for(User user : users)
            for(int i=0; i<nbCategories; i++)
                center.set(i, center.get(i)+user.getUserVector().get(i));

        //normalisation
        for(int i=0; i<nbCategories; i++)
            center.set(i, center.get(i)/users.size());
    }
    public void updateMarkovs(){//DONE
        kThMarkov = new AllKthMarkov(4);
        for(User user : users)
            this.kThMarkov.add(user.getMarkovs());
    }
    public void addUser(User user){//DONE
        users.add(user);
    }
    public void removeUser(User user){//DONE
        users.remove(user);
    }
    public void afficher(){//DONE
        System.out.println("  Centre: "+center);
        System.out.println("  Composition:");
        for(User user : users)
            System.out.println("    "+user.getName()+" "+user.getUserVector());
    }
}
