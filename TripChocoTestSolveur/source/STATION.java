package source;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Une station est un emplacement physique ou s'arrete un ou plusieurs transports. 
 * @author alebre
 *
 */

public interface STATION {

	/**
	 * @return le nom de la station
	 */
	public String getNom();
	
	/**
	 * 	@return La liste des arrets de la station (en d'autres termes la liste des lignes s'arretant a cette station).
	 */
	public ArrayList<ARRET> getListeArrets(); 


	/**
	 * Ajoute un arret a une station
	 * @param newA, le nouvel arret a associer a la station. 
	 */
	public void ajouterArret(ARRET newA);
	
	/**
	 * Surcharge de la mtehode toString()
	 * @return
	 */
	public String toString();
	
	/**
	 * Surcharge de la methode equals
	 * @param o l'objet a comparer
	 * @return true si o est la meme station, false dans le cas contraire
	 */
	public boolean equals(Object o);
	 
	
	public void setScore(int score);
	public int getScore();
	public void setDureeVisite(int duree);
	public int getDureeVisite();
	public void setPlusCourtsChemins(HashMap<STATION,ITIN> plusCourtsChemins);
	public HashMap<STATION,ITIN> getPlusCourtsChemins();
	public void setCoords(Point p);
	public Point getCoords();
	public ArrayList<STATION> getSommetsAtteignables(RESEAU graph);
}
