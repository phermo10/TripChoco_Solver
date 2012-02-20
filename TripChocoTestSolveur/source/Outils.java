package source;
import java.text.Normalizer;


public class Outils {
	
	static String separateur = "\t";
	static String stringPasDesservi = "Cet arret n'est pas desservi";
	/**
	 * Permet d'ajouter des minutes a un horaire
	 * @param debut l'horaire
	 * @param duree les minutes a ajouter
	 * @return l'addition des deux
	 */
	public static int[] additionMinutes(int[] debut, int duree){
		int[] fin = new int[2];
		int minutesRestantes = duree;
		fin[0]=debut[0];
		fin[1]=debut[1];
		while(minutesRestantes>0){
			fin[1]++;
			if(fin[1]==60){
				fin[1]=0;
				fin[0]++;
				if(fin[0]==24){
					fin[0]=0;
				}
			}
			minutesRestantes--;
		}
		return fin;
	}
	
	/**
	 * Retourne la duree entre deux horaires
	 * @param debut l'horaire sense etre anterieur
	 * @param fin l'horaire sense etre posterieur
	 * @return la duree, RMQ : rajoute un jour si la fin est anterieure au debut
	 */
	public static int duree(int[] debut, int[] fin){
		int duree = estApres(debut, fin)?0:24*60;
		int delta = (fin[0]-debut[0])*60 + fin[1]-debut[1];
		duree = duree + delta;
		return duree;
	}
	
	/**
	 * Teste la posteriorite de deux horaires
	 * @param avant l'horaire sense etre avant
	 * @param apres l'horaire sense etre apres
	 * @return true si c'est vrai
	 */
	public static boolean estApres(int[] avant, int[] apres){
		return apres[0]>avant[0]||(apres[0]==avant[0]&&apres[1]>=avant[1]);
	}
	
	/**
	 * Teste le remplissage d'une chaine
	 * @param s la chaine a tester
	 * @return true si la chaine est null, vide ou ne contient que des espaces
	 */
	public static boolean isNullEmptyOrWhiteSpace(String s){
		if(s == null){return true;}
		if(s.isEmpty()){return true;}
		if(s.toLowerCase().startsWith(" ")||s.toLowerCase().startsWith(separateur)||s.toLowerCase().startsWith("\n")){
			int i = 1;
			boolean test = true;
			while(i<s.length()&&test){
				s = s.substring(1);
				test = test&&(s.toLowerCase().startsWith(" ")||s.toLowerCase().startsWith(separateur)||s.toLowerCase().startsWith("\n"));
				i++;
			}
			return test;
		}
		return false;
	}
	
	/**
	 * Permet une plus grande souplesse du programme en negligeant les espaces, accents et majuscules de certaines string (ex : recherches de l'utilisateur)
	 * @param avecAccentsMajusculesEtEspaces la chaine a neutraliser
	 * @param enleverEspaces sil faut aussi enlever les espaces
	 * @return la chaine neutralisee cad sans majuscules, sans accents et eventuellement sans espaces
	 */
	public static String neutraliser(String avecAccentsMajusculesEtEspaces, boolean enleverEspaces) {
		String sansAccentsNiMajusculesNiEspaces = Normalizer.normalize(avecAccentsMajusculesEtEspaces.toLowerCase(), Normalizer.Form.NFD).replaceAll("[\u0300-\u036F]", "");
		if(enleverEspaces){
			sansAccentsNiMajusculesNiEspaces = sansAccentsNiMajusculesNiEspaces.replaceAll(" ", "");
		}
		return sansAccentsNiMajusculesNiEspaces;
	}

}
