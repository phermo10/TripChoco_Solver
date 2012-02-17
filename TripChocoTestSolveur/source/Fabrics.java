package source;
/**
 * Fabrique pour les differentes entites composant l'application
 * @author alebre
 *
 */
public class Fabrics {
	
	/**
	 * Permet de creer un reseau
	 * @param CheminRep le repertoire ou les differents donnees sont disponibles
	 * @return un reseau (le reseau a ete initialise a partir des donnees disponibles dans 'cheminRep').
	 */
	static RESEAU creerReseau(String CheminRep){
		//return new reseauModele(CheminRep); 
		return new RESEAU_IMP(CheminRep);
	}

	/**
	 * Permet de creer un transport (une ligne de transport en commun)
	 * @param identifiantT, l'identifiant du transport (son nom egalement)
	 * @return un transport (a la sortie de cette fonction, le transport ne possede pas d'arrets)
	 */
	public static TRANSPORT creerTransport(String identifiantT) {
		//return new transportModele(identifiantT);
		return new TRANSPORT_IMP(identifiantT);
	}

	/**
	 * Permet de creer une station (une station est un emplacement ''geographique'')
	 * @param nomS le nom (servant d'identifiant unique: ex. ''commerce'')
	 * @return une station
	 */
	public static STATION creerStation(String nomS) {
		//return new stationModele(nomS);
		return new STATION_IMP(nomS);
	}
	
	/**
	 * Permet de creer un arret et de l'initialiser a partir des donnees presentes dans ''cheminRep''.
	 * @param t, le transport auquel appartient l'arret
	 * @param s, la station de cet arret.
	 * @param idA, l'identifiant de l'arret (ex. ''COME3'', ''BDOU1'', ...)
	 * @param cheminRep, le repertoire ou les differents donnees sont disponibles
	 * @return
	 */
	public static ARRET creerArret(TRANSPORT t, STATION s, String idA, String cheminRep, int dir) {
		//return new arretModele(t,s,idA, cheminRep);
		return new ARRET_IMP(t,s,idA,cheminRep, dir);
	}

	
}
