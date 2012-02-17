package source;

import java.util.HashMap;

/**
 * L'interface ARRET modelise un ''stop'' d'un transport sur une station. 
 * Pour chaque arret, UNE SEULE fiche horaire est associee
 * @author PH
 */

public interface ARRET {
	/**
	 * Retourne la fiche horaire
	 * @return la fiche horaire de l'arret
	 */
	public FICHE_HORAIRE getFicheHoraire();
	
	/**
	 * @return la station a laquelle l'arret appartient
	 */
	public STATION getStation();
	
	/**
	 * @return le transport associe a cet arret. 
	 */
	TRANSPORT getTransport(); 
	

	/**
	 * Surcharge de la methode toString()
	 * @return
	 */
	public String toString();
	
	/**
	 * Surcharge de la methode equals
	 * @param o l'objet a comparer
	 * @return true ssi o est le meme arret (meme idA, meme dir, meme fiche horaire), false dans le cas contraire
	 */
	public boolean equals(Object o);
	
	/**
	 * l'id de l'arret
	 * @return l'id de l'arret
	 */
	public String getIdA();
	
	/**
	 * le cheminRep
	 * @return le cheminRep
	 */
	public String getCheminRep();
	
	/**
	 * set le transport de this a t
	 * @param t le transport
	 */
	public void setTransport(TRANSPORT t);
	
	/**
	 * set la station de this a s
	 * @param s la station
	 */
	public void setStation(STATION s);
	
	/**
	 * set la position de this dans les arrets de t
	 * pos est uniquement initialise lorsque l'arret est ajoute a son transport
	 * @param pos la position
	 */
	public void setPos(int pos);
	
	
	/**
	 * null si dernier arret dans sa direction,
	 * pour rappel, dir 1 = chantrerie vers cpauc
	 * @return l'arret suivant du transport
	 */
	public ARRET getSuiv();
	
	/**
	 * position dans la liste d'arrets du transport
	 * @return position dans la liste d'arrets du transport
	 */
	public int getPos();
	
	/**
	 * surcharge de la methode hashCode necessaire pour les hashmap
	 * @return identifiant unique representant this
	 */	
	public int hashCode();
	
	/**
	 * la direction
	 * @return la direction
	 */
	public int getDir();
	
}
