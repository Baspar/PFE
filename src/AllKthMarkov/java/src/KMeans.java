import java.util.Vector;
import java.util.Hashtable;

public class KMeans{
    private Vector<Group> groups;
    private Vector<User> users;
    private Hashtable<User,Group> appartenanceGroup;
    private int K;

    //Constructeur
    public KMeans(Vector<User> users, int K, int nbCategories){//DONE
        this.K=K;

        this.groups = new Vector<Group>();
        this.users = new Vector<User>();
        this.appartenanceGroup = new Hashtable<User, Group>();

        for(int i=0; i<K; i++)
            this.groups.add(new Group(nbCategories));

        for(int i=0; i<users.size(); i++){
            this.users.add(users.get(i));
            this.groups.get(i%K).addUser(users.get(i));
            this.appartenanceGroup.put(users.get(i), groups.get(i%K));
        }

        for(Group group : groups)
            group.updateCenter();

        compute();
    }


    //Methodes
    public void afficher(){//DONE
        for(int i=0; i<K; i++){
            System.out.println("Group #"+i);
            groups.get(i).afficher();
        }

        System.out.println();
        System.out.println("User:");
        for(User user : users){
            System.out.println("  "+user.getName()+" ["+appartenanceGroup.get(user).distance(user)+"]");
        }
    }
    public void compute(){//WIP
        boolean changes=true;
        while(changes){
            changes=false;

            for(User user : users){
                Group groupMin = appartenanceGroup.get(user);
                Group initialGroup = appartenanceGroup.get(user);
                double distanceMin = groupMin.distance(user);
                boolean localChanges = false;

                //Recherche group plus proche
                for(Group group : groups){
                    double newDistance = group.distance(user);
                    if(newDistance < distanceMin){
                        localChanges = true;
                        distanceMin = newDistance;
                        groupMin = group;
                    }
                }

                //Changement de groupe si nécessaire
                if(localChanges){
                    initialGroup.removeUser(user);
                    groupMin.addUser(user);
                    appartenanceGroup.put(user, groupMin);
                    changes = true;
                }
            }

            for(Group group : groups)
                group.updateCenter();
        }
    }
}
