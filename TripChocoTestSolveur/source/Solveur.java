package source;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import javax.swing.JOptionPane;
public class Solveur {

	private RESEAU graph;
	private int maxScore;
	private final int dureeMaxDesVisites = 50; //minutes

	public Solveur(String emplacementArretId, RESEAU graph){
		this.graph = graph;
		System.out.println("Calcul du score max :");
		Date t1 = new Date();
		maxScore = computeMaxScore(graph.getNbNoeuds());
		Date t2 = new Date();
		System.out.println("Scoremax = " + maxScore);
		System.out.println("Durée : " + (t2.getTime() - t1.getTime()) + "ms");
		System.out.println("Génération aléatoire des scores et durées de visite :");
		Date t3 = new Date();
		generateScoresAndTimes();
		Date t4 = new Date();
		System.out.println("Durée : " + (t4.getTime() - t3.getTime()) + "ms");
		System.out.println("Chargement des coordonnées...");
		Date t5 = new Date();
		loadCoords();
		Date t6 = new Date();
		System.out.println("Durée : " + (t6.getTime() - t5.getTime()) + "ms");
		System.out.println("Calcul des plus court chemins");
		Date t7 = new Date();
		computeItineraries();
		Date t8 = new Date();
		System.out.println("Durée : " + (t8.getTime() - t7.getTime()) + "ms");
		//showNetwork();
		DialogMap dm = new DialogMap( "Saisie des stations", null, graph);
		dm.setVisible(true);
		if(dm.getResult()!=null){
			String source = dm.getResult()[0];
			String destination = dm.getResult()[1];
			if(source!=null&&destination!=null){
				JOptionPane.showMessageDialog(null,"Vous souhaitez aller de " + source + " vers " + destination,"Information",JOptionPane.INFORMATION_MESSAGE);
				ITIN best = computeBestPath(source, destination, 4, 180);
			}
			else{JOptionPane.showMessageDialog(null,"Vous n'avez sélectionné aucune station","Information",JOptionPane.INFORMATION_MESSAGE);}
		}
		dm.dispose();


	}

	private void showNetwork(){
		for(STATION s : this.graph.getListeStations()){
			//System.out.println(s.getNom() + " " + s.getCoords());
			if(s.getCoords()==null){System.out.println(s.getNom());}
		}
		DialogMap dm = new DialogMap("Nantes", null, graph);
		dm.setVisible(true);
		dm.dispose();
	}

	private void generateScoresAndTimes(){
		Random generator = new Random();
		for(STATION poi : this.graph.getListeStations()){
			poi.setScore(generator.nextInt(maxScore));
			poi.setDureeVisite(generator.nextInt(dureeMaxDesVisites));
		}
	}

	private void loadCoords(){
		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(Map.FICHIER_COORDONNEES), "UTF-8"));
			String nom = br.readLine();
			while(nom!=null){
				boolean trouvee = false;
				for(int i =0; i<graph.getListeStations().size()-1&&!trouvee;i++){
					if(Outils.neutraliser(graph.getListeStations().get(i).getNom(),true).equals(Outils.neutraliser(nom,true))){
						trouvee = true;
						int x = (int)Integer.parseInt(br.readLine());
						int y = (int)Integer.parseInt(br.readLine());
						graph.getListeStations().get(i).setCoords(new Point(x,y));
					}
				}
				nom = br.readLine();
			}
			ArrayList<STATION> stationsTriees = new ArrayList<STATION>();
			for(STATION s : graph.getListeStations()){
				if(s.getCoords()!=null){stationsTriees.add(s);}
			}
			graph.setStations(stationsTriees);
		} catch (IOException e) {
			e.printStackTrace();
		}
	} 

	private int computeMaxScore(int nbPOI){
		int ms = 0;
		for(int i=1;i<nbPOI;i++){
			ms = ms + i*nbPOI;
		}
		return ms;
	}

	private void computeItineraries(){
		for(STATION depart : graph.getListeStations()){
			HashMap<STATION,STATION> predecesseurs = new HashMap<STATION, STATION>();
			HashMap<STATION, Double> distances = new HashMap<STATION, Double>();
			HashMap<STATION, ITIN> plusCourtsChemins = new HashMap<STATION, ITIN>();
			for(STATION s : graph.getListeStations()){
				if(s!=depart){
					distances.put(s,Double.valueOf(Double.POSITIVE_INFINITY));
					ITIN it = new ITIN(depart, s);
					it.makeImpossible();
					plusCourtsChemins.put(s, it);
				}
			}
			distances.put(depart, Double.parseDouble("0"));
			ArrayList<STATION> nonVisites = new ArrayList<STATION>(graph.getListeStations());
			STATION pp=null;
			while(nonVisites.size()>0){
				STATION prec1 = pp;
				pp = getPlusProche(distances, nonVisites);
				nonVisites.remove(pp);
				if(prec1!=null){
					STATION prec2 = predecesseurs.get(prec1);
					if(prec1.getSommetsAtteignables(graph).contains(pp)){
						ITIN pccPrec1 = plusCourtsChemins.get(prec1);
						if(pccPrec1!=null){plusCourtsChemins.put(pp, pccPrec1.prolonger(pp));}
						else{plusCourtsChemins.put(pp, new ITIN(depart, pp));}
					}
					else{
						ITIN pccPrec2 = plusCourtsChemins.get(prec2);
						if(pccPrec2!=null){plusCourtsChemins.put(pp, pccPrec2.prolonger(pp));}
						else{plusCourtsChemins.put(pp, new ITIN(depart, pp));}
					}
				}
				if(pp!=null){
					for(STATION s2 : pp.getSommetsAtteignables(graph)){
						double ds2 = distances.get(s2);
						double dpp = distances.get(pp);
						double ds2pp = pp.getCoords().distance(s2.getCoords());
						if(ds2>dpp+ds2pp){
							distances.put(s2, ds2pp + dpp);
							predecesseurs.put(s2, pp);
						}
					}
				}
				else{
					nonVisites.clear();
				}
			}	
			depart.setPlusCourtsChemins(plusCourtsChemins);
		}
	}

	private STATION getPlusProche(HashMap<STATION, Double> distances, ArrayList<STATION> nonVisites){
		double dMin = Double.POSITIVE_INFINITY;;
		STATION pp = null;
		for(STATION s : nonVisites){
			double d = (double)distances.get(s);
			if(d<Double.POSITIVE_INFINITY){
				if(dMin==Double.POSITIVE_INFINITY||d<dMin){
					dMin = d;
					pp = s;
				}
			}
		}
		return pp;

	}


	public ITIN computeBestPath(String dep, String arr, int vitesse, int minutesDispo){
		ITIN bestPath = null;

		STATION depart = null;
		STATION arrivee = null;

		// Recherche des stations demandées
		for(int i=0; i<graph.getListeStations().size()&&(depart==null||arrivee==null);i++){
			if(depart==null){
				if(Outils.neutraliser(dep, true).equals(Outils.neutraliser(graph.getListeStations().get(i).getNom(), true))){
					depart = graph.getListeStations().get(i); 
				}
			}
			if(arrivee==null){
				if(Outils.neutraliser(arr, true).equals(Outils.neutraliser(graph.getListeStations().get(i).getNom(), true))){
					arrivee = graph.getListeStations().get(i); 
				}
			}
		}
		if(depart!=null&&arrivee!=null){

			// ----------------------------

			System.out.println("Simplification du graphe");
			Date t1 = new Date();
			// Simplification du graphe
			RESEAU g = simplifyGraph(depart, vitesse, minutesDispo, graph);
			Date t2 = new Date();
			System.out.println("Durée : " + (t2.getTime() - t1.getTime()) + "ms");
			System.out.println("Taille du nouveau graphe : " + g.getNbNoeuds() + " stations");
			//	----------------------------


			System.out.println("Recherche du meilleur chemin");
			Date t3 = new Date();
			double vitesseNecessaire = depart.getPlusCourtsChemins().get(arrivee).getDistTot()/(minutesDispo/60);
			if(g.getListeStations().contains(arrivee)||vitesse<vitesseNecessaire){
				// Si l'arrivee n'est pas dans le rayon atteignable
				// OU
				// Si le plus court chemin entre depart et arrivee a une durée plus longue que le tps dispo
				// A améliorer : Ce calcul ne prend pas en compte les temps de visite du départ et de l'arrivee

				System.out.println("L'arrivee demandée n'est pas atteignable dans le temps imparti. Vitesse nécessaire = " + vitesseNecessaire + " > " + vitesse);
			}else{
				bestPath = new ITIN(depart, arrivee);
				//Classement des stations par score décroissant, classement des stations par duree de visite croissante.
				ArrayList<STATION> classementParScore = new ArrayList<STATION>();
				ArrayList<STATION> classementParDuree = new ArrayList<STATION>();
				for(STATION s : g.getListeStations()){
					int scoreRanking = 0;
					while(scoreRanking<classementParScore.size()&&classementParScore.get(scoreRanking).getScore()>s.getScore()){
						scoreRanking++;
					}
					classementParScore.add(scoreRanking,s);

					int timeRanking = 0;
					while(timeRanking<classementParDuree.size()&&classementParDuree.get(timeRanking).getDureeVisite()>s.getDureeVisite()){
						timeRanking++;
					}
					classementParDuree.add(timeRanking, s);
//					----------------------------
				}

				// Tant qu'il reste des points qui sont potentiellement visitables
				while(classementParScore.size() > 0){
					// 6) Sélectionner le point ayant le meilleur score
					STATION etapePotentielle = classementParScore.get(0);

					// 7) Vérifier que la durée minimale de la visite de ce point est inférieure au temps restant
					if(etapePotentielle.getDureeVisite()+bestPath.getDureeTot(vitesse)>minutesDispo){

						//8) Calcul du temps minimal que prendrait un trajet ne visitant que ces points, en utilisant la base des distances précalculées.
						ITIN newPath = null;
						// s'il est possible de passer par cette etape potentielle et que le temps que cela prendrait est inferieur au temps dispo
						if(bestPath.tryToGoBy(etapePotentielle, newPath)&&newPath.getDureeTot(vitesse)<=minutesDispo){
							bestPath = newPath;					
						}// Dans tous les cas on a vérifié cette option donc on la supprime
						classementParDuree.remove(etapePotentielle);
						classementParScore.remove(etapePotentielle);

					}else{
						/*9) Si la durée est OK, on retourne à 6) en gardant ce point et en sélectionnant le point suivant dans l’ordre des meilleurs scores.
					Sinon, on note ce point comme étant invisitable et on retourne à 6)*/

						// sinon, tout les points de temps de visite superieure seront invisitables aussi
						boolean indexToDeleteFound = false;
						int size = classementParDuree.size();
						int indexToDelete = -1;
						for(int i = 0; i<size; i++){
							if(indexToDeleteFound){
								classementParScore.remove(classementParDuree.get(indexToDelete));
								classementParDuree.remove(indexToDelete);
							}else{
								indexToDeleteFound = classementParDuree.get(i) == etapePotentielle;
								indexToDelete = i;
								i--;
							}
						}
					}
				}
			}
			Date t4 = new Date();
			System.out.println("Durée : " + (t4.getTime() - t3.getTime()) + "ms");
		}else{
			JOptionPane.showMessageDialog(null,"Les stations demandées n'ont pas été trouvées dans la base","Information",JOptionPane.INFORMATION_MESSAGE);
			System.out.println("Les stations demandées n'ont pas été trouvées dans la base");
		}
		return bestPath;
	}

	/**
	 * 
	 * @param depart
	 * @param vitesse En millier d'unité de coordonnée par heure (1 unité = 1 mètre ==> equivalent a du km/h)
	 * @param full
	 * @return
	 */
	private RESEAU simplifyGraph(STATION depart, int vitesse, int minutesDispo, RESEAU full){
		RESEAU simple = new RESEAU_IMP(full.getCheminRep());
		ArrayList<STATION> stations = new ArrayList<STATION>();
		ArrayList<TRANSPORT> transports = new ArrayList<TRANSPORT>();

		// Tri par rayon parcourable
		double distanceMaxParcourable = vitesse * minutesDispo/60;
		for(STATION s : full.getListeStations()){
			if(depart.getCoords().distance(s.getCoords())<distanceMaxParcourable/2){
				stations.add(s);
				for(ARRET a : s.getListeArrets()){
					TRANSPORT t = a.getTransport();
					if(!transports.contains(t)){transports.add(t);}
				}
			}
		}
		// -------------------------



		return simple;
	}



}
