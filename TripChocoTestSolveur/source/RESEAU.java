package source;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * L'interface RESEAU modelise le reseau en tant que tel. 
 * Un reseau est consitue d'un ensemble de transports et d'un ensemble de station.
 * Chaque transport s'arrete a plusieurs stations. l'association d'un arret ˆ une station est modelise par l'interface ARRET.
 * @author alebre
 *
 */

public interface RESEAU {

	/**
	 * Initialise le reseau :   
	 * 1./ en parcourant le fichier arretsid.txt present dans le repertoire ''cheminRep''. 
	 * Ce fichier indique l'ensemble des arrets pour chacun des transports. 
	 * 2./ en chargeant l'ensemble des fiches horaires presentes dans les sous repertoires correspondant a chacun des transports. 
	 * @param cheminRep le chemin du repertoire contenant les differents fichiers/repertoires necessaires pour initialiser le reseau
	 * @return true si l'initialisation s'est correctement deroule, false dans le cas contraire.
	 * @throws FileNotFoundException  l'appel du service genere une exception si le chemin passe en parametre n'est pas correcte
	 */
	public boolean initialiser(String cheminRep) throws FileNotFoundException;
	
	/**
	 * Retourne l'ensemble des transports constituant le reseau
	 * @return une ArrayList d'objets de type TRANSPORT
	 */
	public ArrayList<TRANSPORT> getListeTransports(); 
	
	/**
	 * Retourne l'ensemble des stations presentes dans le reseau
	 * @return une ArrayList d'objets de type STATION.
	 */
	public ArrayList<STATION> getListeStations(); 
	
	/**
	 * Calcule le meilleur itineraire possible entre source et destination. Cad le plus court si leMoinsDeChangements==false, celui avec le moins de changement sinon.
	 * @param source : le nom ou une partie du nom de la station de depart souhaitee
	 * @param destination : le nom ou une partie du nom de la station d'arrivee souhaitee
	 * @param time : l'horaire a partir de laquelle on peut partir. time[0] étant l'heure, time[1] étant les minutes
	 * @param dureeChangementTransport : la ponderation en minutes qu'il faut appliquer a un changement de transport
	 * (ie le temps minimal a avoir entre la descente d'un transport et la montee dans le suivant) 
	 * @param leMoinsDeChangements : s'il faut chercher en priorite un itineraire avec le moins de changement possibles
	 * @return un ITINERAIRE_IMP representant le meilleur itineraire possible satisfaisant ces conditions
	 */
	public ITINERAIRE calculItineraire(String source, String destination, int[] time, int dureeChangementTransport, boolean leMoinsDeChangements)  throws StationNotFoundException, DepartPasDesserviException, ItineraireNotPossibleException; 
	
	/**
	 * surcharge de la methode toString
	 * @return
	 */
	public String toString();
	
	/**
	 * retourne la liste des arrets du reseau
	 * !!-> a ne pas confondre avec un simple getteur car il est ici necessaire de parcourir toutes les stations <-!!
	 * @return la liste des arrets du reseau
	 */
	public ArrayList<ARRET> getListeArrets();
	
	public int getNbNoeuds();
	public int getNbArcs();
	public void setStations(ArrayList<STATION> stations);
	public void setTransports(ArrayList<TRANSPORT> transports);
	public String getCheminRep();
	public boolean isCoordsLoaded();
	public void setCoordsLoaded(boolean loaded);


}
