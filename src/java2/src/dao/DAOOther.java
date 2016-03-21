package dao;

import dao.*;

import java.sql.ResultSet;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Properties;

import org.neo4j.jdbc.Driver;
import org.neo4j.jdbc.Neo4jConnection;

public class DAOOther{
    private Neo4jConnection connect;

    public DAOOther(Neo4jConnection connect){
        this.connect = connect;
    }

    /**
     * @author Laine B.
     * Récupération du nombre de nœuds d'une catégorie.
     *
     * Cette méthode retourne le nombre de nœuds d'une catégorie.
     * Elle est appelée génériquement par d'autres méthodes de comptage.
     * @param className Nom de la catégorie.
     * @return -1 si le nom de catégorie donné n'existe pas, le nombre de nœuds sinon.
     */
    public int getNbClass(String className){
        try(ResultSet set = connect.createStatement().executeQuery("match (n:"+className+") return count(n)")){
            int i=-1;
            if(set.next())
                i=set.getInt("count(n)");
            return i;
        } catch(Exception e){
            System.out.println(e);
            return -1;
        }
    }
}
