package source;
import java.util.HashMap;

/**
 * 
 * L'interface TRANSPORT modelise un ligne de transport en commun. 
 * Un transport est caracterise par son nom exemple 'Line-1', son type (cf EnumTypeTransport) et L'ensemble de ces ARRETS.
 * @author alebre
 *
 */
public interface TRANSPORT {
	/** 
	 * 	@return l'identifiant de la ligne
	 */
	public String getNom(); 
	
	/**
	 * @return le type de transport (bus, tram, ...)
	 */
	public EnumTypeTransport getType(); 
	
	/**
	 * Permet definir le type d'un transport 
	 * A sa creation, le type initialise est 'UNDEFINED'
	 * @param type
	 */
	public void  setType(EnumTypeTransport type);
	
	/**
	 * Retourne la liste des tuples (position,arrets) associes ˆ la ligne selon la direction donnee
	 * @param direction prend les valeurs 0 ou 1
	 * @return La liste ordonnee des arrets selon la direction donnee.
	 */
	public HashMap<Integer,ARRET> getListeArret(); 

	/**
	 * Ajoute un nouvel arret en position 'positionA'  
	 * @param newA le nouvel arret a associer au transport
	 * @param positionA la position de cet arret au sein de la ligne
	 */
	public void ajouterArret(ARRET newA, int positionA); 
	
	/**
	 * Surcharge de la mtehode toString()
	 * @return
	 */
	public String toString();
	
	public boolean equals(Object o);
	
	/**
	 * Retourne le numéro de ligne : 72, 76, C, 99, ...
	 * @return
	 */
	public String getLigne();
	
	/**
	 * toString() plus court qui renvoie juste "la ligne 76" ou "le bus 76" si le type a pu etre initialisé
	 * @return
	 */
	public String toStringCourt();
	
}
