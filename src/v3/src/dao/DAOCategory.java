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

public class DAOCategory{
    private DAOOther daoOther;
    private Neo4jConnection connect;

    public DAOCategory(DAOOther daoOther, Neo4jConnection connect){
        this.daoOther = daoOther;
        this.connect = connect;
    }

    /**
     * @author Laine B.
     * Récupération catégorie d'un document
     *
     * Cette méthode retourne pour un document donné l'ID de sa catégorie
     * @param doc Nom du document
     * @return ID de la catégorie
     */
    public int getCategorie(String doc){
        String query = "MATCH (c:Categorie)-[]-(:Doc {name: \""+doc+"\"}) RETURN c.cpt";
        int out = -1;
        try(ResultSet set = connect.createStatement().executeQuery(query)){
            if(set.next())
                out=set.getInt("c.cpt");
        } catch(Exception e){
            System.out.println(e);
        } finally {
            return out;
        }
    }
    /**
     * @author Laine B.
     * Récupération de l'ID d'une catégorie.
     *
     * Cette méthode retourne l'ID en base de donnée d'une catégorie.
     * @param cat Nom de la catégorie
     * @return -1 s'il n'existe pas de catégorie portant ce nom, sinon son ID.
     */
    public int getCategorieId(String cat){
        String query = "match (n:Categorie {name:\""+cat+"\"}) RETURN n.cpt";
        try(ResultSet set = connect.createStatement().executeQuery(query)){
            int i=-1;
            if(set.next())
                i=set.getInt("n.cpt");
            return i;
        } catch(Exception e){
            System.out.println(e);
            return -1;
        }
    }
    /**
     * @author Laine B.
     * Récupération nombre de catégorie.
     *
     * Cette méthode retourne le nombre de catégorie présentes en la base de donnée.
     * @return Le nombre de catégories.
     */
    public Integer getNbCategories(){
        return daoOther.getNbClass("Categorie");
    }
}
