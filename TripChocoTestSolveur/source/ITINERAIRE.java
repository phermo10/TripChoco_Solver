package source;
import java.util.ArrayList;

/**
 * L'interface ITINERAIRE sera necessaire pour le calcul du plus court chermin dans un second temps
 * @author alebre
 */

public interface ITINERAIRE {
	
		
	/**
	 * @return retourne une version sous forme de chaine de caracteres de l'itineraire
	 */
	public String toString(); 
	/**
	 * @return retourne une version sous forme html de l'itineraire
	 */
	public String toHtml();
	
	
	/**
	 * Surcharge de la methode equals
	 * @param o l'objet a comparer
	 * @return true si o est la meme station, false dans le cas contraire
	 */
	public boolean equals(Object o);
	
	/**
	 * ajoute une etape a l'itineraire
	 * la position est ajoute au debut 
	 * @param a l'arret
	 * @param horaire, l'horaire auquel on arriv a l'arret
	 */
	public void addEtape(ARRET a, int[] horaire);
	
	/**
	 * calcule la duree totale de l'itineraire
	 * @return la duree totale de l'itineraire
	 */
	public int getDureeTotale();
	
	/**
	 * calcule le nombre de changements
	 */
	public int getNombreChangements();
	
	/**
	 * retourne les stations de l'itineraire
	 * @return
	 */
	public ArrayList<String> getListeStations();
	
	/**
	 * retourne les horaires de passage aux arrets de l'itineraire
	 * @return
	 */
	public ArrayList<int[]> getHoraires();
	
	/**
	 * retourne les arrets de l'itineraire
	 * @return
	 */
	public ArrayList<ARRET> getArrets();
	
	/**
	 * le nombre d'arrets de l'itineraire
	 * @return
	 */
	public int size();
}
