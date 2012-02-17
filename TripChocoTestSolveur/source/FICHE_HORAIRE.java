package source;
/**
 * L'interface FICHE_HORAIRE permet de manipuler les differentes
 * horaires correspondant aux ''stops'' quotidien que fait un transport dans une station.
 * Une fiche horaire est associee a un arret.
 * @author alebre
 *
 */

public interface FICHE_HORAIRE {
	
		/** 
	 * Retourne l'heure du prochain passage du transport
	 * @return l'heure du prochain passage.
	 */
	public int getAttenteAvantNextStop(int[] dateDepart);
	
	/**
	 * Retourne le prochain passage du transport apres la date de depart indiquee
	 * @param dateDepart
	 * @return le prochain passage du transport apres la date de depart indiquee
	 */
	public int[] getNextStop(int[] dateDepart);
	
	/**
	 * Surcharge de la methode toString()
	 * @return
	 */
	public String toString();
	
	/**
	 * Surcharge de la methode equals
	 * @param o l'objet a comparer
	 * @return true si o est la meme station, false dans le cas contraire
	 */
	public boolean equals(Object o);
	
	/**
	 * getter des horaires
	 * @return
	 */
	public boolean[][] getHoraires();
	
	/**
	 * surcharge de la methode hashcode
	 * @return
	 */
	public int hashCode();
	
	/**
	 * retourne le ième horaire de la fiche sachant qu'ils sont par ordre chronologique
	 * @param i
	 * @return
	 */
	public int[] getHoraireByIndex(int i);
	
	/**
	 * verifie que la fiche horaire est valide ie que l'arret correspondant est desservi
	 * @return
	 */
	public boolean isValide();
	
	/**
	 * getter de l'arret
	 * @return
	 */
	public ARRET getArret();
	
	/**
	 * getter de la ligne
	 * @return
	 */
	public String getLigne();
	
	/**
	 * getter de la direction
	 * @return
	 */
	public int getDir();
}
