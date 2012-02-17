package source;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
/**
 * Programme de gestion des deplacements en transport en commun dans le reseau nantais
 * @author alebre
 *
 */
public class MinesTan {

	/**
	 * ponderations du changement de transport selon la vitesse de marche de lutilisateur
	 */
	private static final int[] dureeChangementTransport = {0,2,5,15};
	
	/**
	 * Si useConsoleMenu=true, si debugMode = true, le main lance les 3 itineraires de test
	 */
	private static final boolean debugMode = false;
	
	/**
	 * Si useConsoleMenu = false, le main lance le menu IHM
	 * Si useConsoleMenu = true, le main lance le menu console
	 */
	private static final boolean useConsoleMenu = true;

	/**
	 * Charge le reseau, puis attend que l'utilisateur précise ce qu'il veut faire
	 * Si useConsoleMenu = false, lance le menu IHM
	 * Si useConsoleMenu = true, lance le menu console
	 * 		Si en plus debugMode==true, se contente de calculer des itineraires test
	 * @param args
	 */
	public static void main (String[] args){

		System.out.println("Veuillez patienter, chargement du reseau...");
		Date t1 = new Date(); // le constructeur par defaut de Date retourne l'instant actuel, voir javadoc
		RESEAU tan = Fabrics.creerReseau("." + java.io.File.separator + "ressources" + java.io.File.separator); // RESEAU_IMP affichera ses propres messages d'erreurs 
		Date t2 = new Date();
		System.out.println("Duree de l'initialisation : " + (t2.getTime() - t1.getTime()) + "ms");

		/*if(useConsoleMenu){menuConsole(tan);}
		else{new menuIHM(tan, dureeChangementTransport);}*/
		new Solveur("." + java.io.File.separator + "ressources" + java.io.File.separator+ "arretid.txt",tan);

	}

	private static void menuConsole(RESEAU tan){
		System.out.println("Nb d'arcs : " + tan.getNbArcs());
		System.out.println("Nb de noeuds : " + tan.getNbNoeuds());
		if(debugMode){
			System.out.println("Vous utilisez le mode debug en console (voir debugMode et useConsoleMenu dans MinesTan.java pour plus d'informations) : Affichage de trois itineraires test en console:");
			int[] dateDepart = new int[2];
			dateDepart[0]=9;
			dateDepart[1]=00;
			ITINERAIRE result;
			try {
				// aller de la gare à l'hopital Bellier en partant à 9:00
				result = tan.calculItineraire("gare sncf", "hopital bellier", dateDepart, dureeChangementTransport[0],false);
				System.out.println(result.toString());
			} catch (StationNotFoundException e) {
				System.out.println(e.getMessage());
			} catch (DepartPasDesserviException e) {
				System.out.println(e.getMessage());
			} catch (ItineraireNotPossibleException e){
				System.out.println(e.getMessage());
			}
			dateDepart[0]=9;
			dateDepart[1]=55;
			try {
				// aller de la gare à l'hopital Bellier en partant à 9:55
				result = tan.calculItineraire("gare sncf", "hopital bellier", dateDepart, dureeChangementTransport[0],false);
				System.out.println(result.toString());
			} catch (StationNotFoundException e) {
				System.out.println(e.getMessage());
			} catch (DepartPasDesserviException e) {
				System.out.println(e.getMessage());
			} catch (ItineraireNotPossibleException e){
				System.out.println(e.getMessage());
			}
			dateDepart[0]=7;
			dateDepart[1]=00;
			try {
				// aller de Christian Pauc à Facultes en partant à 7:00
				result = tan.calculItineraire("Christian pauc", "facultés", dateDepart, dureeChangementTransport[0],false);
				System.out.println(result.toString());
			} catch (StationNotFoundException e) {
				System.out.println(e.getMessage());
			} catch (DepartPasDesserviException e) {
				System.out.println(e.getMessage());
			} catch (ItineraireNotPossibleException e){
				System.out.println(e.getMessage());
			}

		}
		else{	
			System.out.println("Vous utilisez le mode console. (voir debugMode et useConsoleMenu dans MinesTan.java pour plus d'informations)");
			
			System.out.println("Bienvenue, que voulez-vous faire ? Entrez h pour de l'aide");
			String input = lireString();
			while(!(input.equals("quitter"))){

				if(input.equals("h")){
					help();
				}
				if(input.equals("toutafficher")){
					System.out.println("Affichage du reseau :\n" + tan);
				}
				if(input.equals("horaires")){ // Pour le test des fiches horaires
					System.out.println("quel arret ?");
					input = lireString();
					for(ARRET a:tan.getListeArrets()){
						if(a.getIdA().equals(input)||Outils.neutraliser(a.getStation().getNom(),true).equals(input)){
							System.out.println(a.getFicheHoraire());
						}
					}
				}
				if(input.equals("itineraire")){
					System.out.println("Vitesse de marche ? 0 = yamakasi (pas de ponderation changement), 1 = rapide (2min), 2 = normale(5min), 3=lente(15min)");
					String strSpeed = lireString();
					try{

						int speed = Integer.parseInt(strSpeed);
						System.out.println("Mode de voyage ? 0 = plus court chemin, 1 = le moins de changements");
						String strMode = lireString();
						int mode = Integer.parseInt(strMode);
						boolean leMoinsDeChangement = (mode==1);
						System.out.println("Station de depart ?");
						String source = lireString();
						System.out.println("Station d'arrivee ?");
						String destination = lireString();
						System.out.println("Heure de depart ?");
						int h = (int)Integer.parseInt(lireString());
						System.out.println("Minutes ?");
						int m = (int)Integer.parseInt(lireString());
						int[] dateDepart = new int[2];
						dateDepart[0]=h;
						dateDepart[1]=m;
						Date t1 = new Date();
						try{
							ITINERAIRE itin = tan.calculItineraire(source, destination, dateDepart, dureeChangementTransport[speed], leMoinsDeChangement);
							System.out.println(itin.toString());
						} catch (StationNotFoundException e) {
							System.out.println(e.getMessage());
						} catch (DepartPasDesserviException e) {
							System.out.println(e.getMessage());
						} catch (ItineraireNotPossibleException e){
							System.out.println(e.getMessage());
						}
						Date t2 = new Date();
						System.out.println("Duree du calcul : " + (t2.getTime() - t1.getTime()) + "ms");
					}
					catch(NumberFormatException e){
						System.out.println("Vous avez entré une valeur incorrecte.");
					}
				}
				System.out.println("OK ! Et maintenant, que voulez-vous faire ? Entrez h pour de l'aide");
				input = lireString();
			}
			System.out.println("Fin du programme.");
		}
	}


	/**
	 * Lit la chaine tapee au clavier par l'utilisateur et la retourne sans majuscules ni espaces ni accents
	 * @return la chaine tapee par l'utilisateur sans majuscules ni espaces ni accents
	 */
	private static String lireString(){
		String ligne_lue=null;
		try{ 
			InputStreamReader lecteur=new InputStreamReader(System.in); 
			BufferedReader entree=new BufferedReader(lecteur); 
			ligne_lue=Outils.neutraliser(entree.readLine(),true); 
		} 
		catch(IOException e){e.printStackTrace();}
		return ligne_lue;
	}

	/**
	 * Affiche simplement les commandes disponibles
	 */
	private static void help(){
		System.out.println("Commandes disponibles :\ntout afficher : affiche l'integralite du reseau de la base de donnees\nhoraires : pour afficher la fiche horaire d'un arret connaissant son identifiant\nitineraire : permet de calculer l'itineraire le plus court d'un arret a un autre\nquitter : quitte le programme" + "\n\n(insensible a la casse, aux accents et aux espaces)");
	}



}
