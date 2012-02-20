package source;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.*;
import javax.swing.JOptionPane;
public class Solveur {

	public static final String FICHIER_COORDONNEES = "coordonnees.txt";

	private int maxScore;
	private final int dureeMaxDesVisites = 50; //minutes

	public Solveur(String emplacementArretId, RESEAU graph){
		System.out.println("Calcul du score max :");
		Date t1 = new Date();
		maxScore = computeMaxScore(graph.getNbNoeuds());
		Date t2 = new Date();
		System.out.println("Scoremax = " + maxScore);
		System.out.println("Durée : " + (t2.getTime() - t1.getTime()) + "ms");
		System.out.println("Génération aléatoire des scores et durées de visite :");
		t1 = new Date();
		generateScoresAndTimes(graph);
		t2 = new Date();
		System.out.println("Durée : " + (t2.getTime() - t1.getTime()) + "ms");

		boolean cheminsDejaCalcules = chargerPlusCourtsChemins(graph);

		System.out.println("Chargement des coordonnées...");
		t1 = new Date();
		loadCoords(graph);
		t2 = new Date();
		System.out.println("Durée : " + (t2.getTime() - t1.getTime()) + "ms");

		if(!cheminsDejaCalcules){
			System.out.println("Echec de chargement du fichier des plus courts chemins.");
			System.out.println("Calcul des plus court chemins");
			boolean sauvegardeReussie = false;
			t1 = new Date();
			sauvegardeReussie = computeShortestPaths(graph);
			t2 = new Date();
			System.out.println("Durée : " + (t2.getTime() - t1.getTime()) + "ms");
			if(sauvegardeReussie){
				System.out.println("Les plus courts chemins ont étés sauvegardés avec succès.");
			}else{
				System.out.println("La sauvegarde des plus courts chemin à échoué.");
			}
		}else{
			System.out.println("Plus courts chemins précalculés chargés.");
		}
		//showNetwork();
		DialogMap dm = new DialogMap("Saisie des stations", null, graph, 0, maxScore);
		dm.setVisible(true);
		if(dm.getResult()!=null){
			STATION source = dm.getResult()[0];
			STATION destination = dm.getResult()[1];
			if(source!=null&&destination!=null){
				JOptionPane.showMessageDialog(null,"Vous souhaitez aller de " + source + " vers " + destination,"Information",JOptionPane.INFORMATION_MESSAGE);
				System.out.println("Vitesse de déplacement : 4km/h");
				System.out.println("Temps dispo : 180 minutes = 3 heures");
				System.out.println("Calcul de l'itineraire maximisant le score");
				t1 = new Date();
				ITIN best = computeBestPath(source, destination, 4000, 180, graph);
				t2 = new Date();
				System.out.println("Durée : " + (t2.getTime() - t1.getTime()) + "ms");
				if(best!=null){
					DialogMap displayBestResult = new DialogMap("Votre itineraire", best, graph, 4000, maxScore);
					displayBestResult.setVisible(true);
					displayBestResult.dispose();
				}else{JOptionPane.showMessageDialog(null,"Aucun itineraire possible entre ces deux stations dans le temps imparti","Information",JOptionPane.INFORMATION_MESSAGE);}

			}
			else{JOptionPane.showMessageDialog(null,"Vous n'avez sélectionné aucune station","Information",JOptionPane.INFORMATION_MESSAGE);}
		}
		dm.dispose();


	}

	private void showNetwork(RESEAU graph){
		for(STATION s : graph.getListeStations()){
			//System.out.println(s.getNom() + " " + s.getCoords());
			if(s.getCoords()==null){System.out.println(s.getNom());}
		}
		DialogMap dm = new DialogMap("Nantes", null, graph, 0, maxScore);
		dm.setVisible(true);
		dm.dispose();
	}

	private void generateScoresAndTimes(RESEAU graph){
		Random generator = new Random();
		for(STATION poi : graph.getListeStations()){
			poi.setScore(generator.nextInt(maxScore));
			poi.setDureeVisite(generator.nextInt(dureeMaxDesVisites));
		}
	}

	private void loadCoords(RESEAU graph){
		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(FICHIER_COORDONNEES), "UTF-8"));
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
			graph.setCoordsLoaded(true);
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

	private boolean computeShortestPaths(RESEAU graph){
		int count = 0;
		int tot = graph.getListeStations().size();
		for(STATION depart : graph.getListeStations()){
			count++;
			System.out.println("" + count + "/" + tot);
			/*if(!Outils.neutraliser(depart.getNom(), true).equals("tertre")){
				continue;
			}

			System.out.println("DEPART = " + depart);*/
			HashMap<STATION,STATION> predecesseurs = new HashMap<STATION, STATION>();
			HashMap<STATION, Double> distances = new HashMap<STATION, Double>();
			HashMap<STATION, ITIN> plusCourtsChemins = new HashMap<STATION, ITIN>();
			ArrayList<STATION> nonVisites = new ArrayList<STATION>(graph.getListeStations());
			ArrayList<STATION> visites = new ArrayList<STATION>();

			for(STATION s : graph.getListeStations()){
				if(s!=depart){
					distances.put(s,Double.POSITIVE_INFINITY);
					ITIN it = new ITIN(depart, s);
					it.makeImpossible();
					plusCourtsChemins.put(s, it);
				}
			}
			distances.put(depart, 0.0);
			STATION pp=null;
			while(nonVisites.size()>0){
				pp = getPlusProche(distances, nonVisites);
				if(pp!=null){
					//System.out.println("Plus proche : " + pp);
					// Collections.sort trie par ordre croissant (plus petit en 0)
					Collections.sort(visites, new ComparateurPOI(pp, true));
					boolean precNotFound = true;
					int i=0;
					STATION prec;
					// On parcours les points visites tries par distance relative à pp
					// jusqu'à en trouver un qui soit dans les sommets atteignables de pp
					// (il y en a forcément un, sinon pp serait à une distance infinie et getplusproche retournerai null)
					// c'est ce point qui sera le prédecesseur de pp
					while(precNotFound&&i<visites.size()){
						prec = visites.get(i);
						if(prec!=null&&prec.getSommetsAtteignables(graph).contains(pp)){
							precNotFound = false;
							ITIN shortestToPrec = plusCourtsChemins.get(prec);
							ITIN shortestToPP;
							if(shortestToPrec!=null)
								shortestToPP = shortestToPrec.prolonger(pp);
							else
								shortestToPP = new ITIN(depart, pp);
							plusCourtsChemins.put(pp, shortestToPP);
						}
						i++;
					}

					for(STATION s2 : pp.getSommetsAtteignables(graph)){
						double ds2 = distances.get(s2);
						double dpp = distances.get(pp);
						double ds2pp = pp.getCoords().distance(s2.getCoords());
						if(ds2>dpp+ds2pp){
							distances.put(s2, ds2pp + dpp);
							predecesseurs.put(s2, pp);
						}
					}
					nonVisites.remove(pp);
					visites.add(pp);
				}
				else{
					nonVisites.clear();
				}
			}	
			depart.setPlusCourtsChemins(plusCourtsChemins);
			predecesseurs.clear();
			distances.clear();
			nonVisites.clear();
			nonVisites.trimToSize();
			visites.clear();
			visites.trimToSize();
		}
		return sauvegarderPlusCourtsChemins(graph);
	}

	private STATION getPlusProche(HashMap<STATION, Double> distances, ArrayList<STATION> nonVisites){
		double dMin = Double.POSITIVE_INFINITY;
		STATION pp = null;
		for(STATION s : nonVisites){
			double d = distances.get(s);
			if(d!=Double.POSITIVE_INFINITY){
				boolean dMinIsInf = dMin==Double.POSITIVE_INFINITY;
				if(dMinIsInf||(!dMinIsInf&&d<dMin)){
					dMin = d;
					pp = s;
				}
			}
		}
		return pp;

	}

	/**
	 * 
	 * @param dep
	 * @param arr
	 * @param vitesse En unité de coordonnée par heure (1 unité = 1 mètre ==> equivalent a du Mètre Par Heure, exemple pour 4km/h mettre 4000)
	 * @param minutesDispo
	 * @return
	 */
	public ITIN computeBestPath(STATION depart, STATION arrivee, int vitesse, int minutesDispo, RESEAU graph){
		ITIN bestPath = null;

		// ----------------------------
		System.out.println("Simplification...");
		Date t1 = new Date();
		// Simplification du graphe
		RESEAU g = simplifyGraph(depart, vitesse, minutesDispo, graph);
		Date t2 = new Date();
		System.out.println("Durée : " + (t2.getTime() - t1.getTime()) + "ms");
		//	----------------------------


		//System.out.println("Recherche du meilleur chemin");
		//Date t3 = new Date();
		double vitesseNecessaire = depart.getPlusCourtsChemins().get(arrivee).getDistTot()/(minutesDispo/60);
		if(!g.getListeStations().contains(arrivee)||vitesse<vitesseNecessaire){
			// Si l'arrivee n'est pas dans le rayon atteignable
			// OU
			// Si le plus court chemin entre depart et arrivee a une durée plus longue que le tps dispo
			// A améliorer : Ce calcul ne prend pas en compte les temps de visite du départ et de l'arrivee

			System.out.println("L'arrivee demandée n'est pas atteignable dans le temps imparti. Vitesse nécessaire = " + vitesseNecessaire + " > " + vitesse);
		}else{
			System.out.println("Recherche du meilleur chemin...");
			bestPath = new ITIN(depart, arrivee);
			//Classement des stations par score décroissant, classement des stations par duree de visite croissante.
			ArrayList<STATION> classementParScore = new ArrayList<STATION>(g.getListeStations());
			ArrayList<STATION> classementParDuree = new ArrayList<STATION>(g.getListeStations());
			ComparateurPOI comparateurScore = new ComparateurPOI(POIproperties.SCORE, false);
			ComparateurPOI comparateurDuree = new ComparateurPOI(POIproperties.DUREEVISITE, true);
			Collections.sort(classementParScore, comparateurScore);
			Collections.sort(classementParDuree, comparateurDuree);		

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
							if(indexToDeleteFound){
								indexToDelete = i;
								i--;
							}
						}
					}
				}
			}
		}
		//Date t4 = new Date();
//		System.out.println("Durée : " + (t4.getTime() - t3.getTime()) + "ms");
		return bestPath;
	}

	/**
	 * 
	 * @param depart
	 * @param vitesse En unité de coordonnée par heure (1 unité = 1 mètre ==> equivalent a du Mètre Par Heure, exemple pour 4km/h mettre 4000)
	 * @param full
	 * @return
	 */
	private RESEAU simplifyGraph(STATION depart, int vitesse, int minutesDispo, RESEAU full){
		RESEAU simple = new RESEAU_IMP(full.getCheminRep());
		ArrayList<STATION> stations = new ArrayList<STATION>();
		ArrayList<TRANSPORT> transports = new ArrayList<TRANSPORT>();

		// Tri par rayon parcourable
		double distanceMaxParcourable = vitesse * minutesDispo/60;
		System.out.println(distanceMaxParcourable);
		for(STATION s : full.getListeStations()){
			if(depart.getCoords().distance(s.getCoords())<distanceMaxParcourable/2){
				stations.add(s);
				for(ARRET a : s.getListeArrets()){
					TRANSPORT t = a.getTransport();
					if(!transports.contains(t)){transports.add(t);}
				}
			}
		}
		simple.setStations(stations);
		simple.setTransports(transports);
		// -------------------------
		return simple;
	}


	public enum POIproperties {
		SCORE(0),
		DUREEVISITE(1),
		DISTANCERELATIVE(2);
		private final int propertyCode;
		private POIproperties(int propertyCode) {
			this.propertyCode = propertyCode;
		}	
	}


	private class ComparateurPOI implements Comparator<STATION> {

		private POIproperties propertyCode;
		private STATION origin;
		private boolean ordreCroissant;

		public ComparateurPOI(POIproperties propertyCode, boolean ordreCroissant){
			this.propertyCode = propertyCode;
			this.origin = null;
			this.ordreCroissant = ordreCroissant;
		}

		public ComparateurPOI(STATION origin, boolean ordreCroissant){
			this.propertyCode = POIproperties.DISTANCERELATIVE;
			this.origin = origin;
			this.ordreCroissant = ordreCroissant;
		}

		public int compare(STATION s1, STATION s2) {
			int result;
			double property1;
			double property2;
			switch (propertyCode)
			{
			case SCORE:property1 = s1.getScore();property2 = s2.getScore();break;
			case DISTANCERELATIVE:
				property1 = origin.getCoords().distance(s1.getCoords());
				property2 = origin.getCoords().distance(s2.getCoords());
			default:property1 = s1.getDureeVisite();property2 = s2.getDureeVisite();break;	
			}
			if(property1==property2){result=0;}
			else{

				if(ordreCroissant){
					result=property1<property2?-1:1;
				}
				else{
					result=property1<property2?1:-1;
				}
			}			
			return result;
		}
	}

	private class Balises{
		public static final String FROM_Begin = "<FROM=";
		public static final String TO_Begin = "\t<TO=";
		public static final String FROM_End = "</FROM>";
		public static final String TO_End = "\t</TO>";
		public static final String BY = "\t\t<BY=";
	}

	private boolean sauvegarderPlusCourtsChemins(RESEAU graph){
		boolean success = true;	
		PrintWriter output=null;
		System.out.println("Sauvegarde des plus courts chemin...");
		try {
			output = new PrintWriter(new BufferedWriter(new FileWriter("plusCourtsChemins.txt")));
			String line;
			for(STATION from : graph.getListeStations()){
				output.println(Balises.FROM_Begin +from.getNom()+">");

				for(STATION to : graph.getListeStations()){
					if(!from.equals(to)){
						output.println(Balises.TO_Begin+to.getNom()+">");

						ArrayList<STATION> etapes = from.getPlusCourtsChemins().get(to).getEtapes();
						for(STATION etape : etapes){
							if(!(etape.equals(to)||etape.equals(from)))
								output.println(Balises.BY + etape.getNom()+">");
						}
						output.println(Balises.TO_End);
					}
				}
				output.println(Balises.FROM_End);
			}
			System.out.println("OK.");			
		}	    
		catch (Exception e) {
			System.out.println("Echec.");
			success = false;
		}finally {
			try{
				output.flush();
				output.close();
			}catch(Exception e){}
		}
		return success;
	}

	private boolean chargerPlusCourtsChemins(RESEAU graph){
		if(graph.isCoordsLoaded()) throw new IllegalArgumentException("Graph coords must not be loaded before calling this method.");
		boolean success = true;

		ArrayList<String> lines = new ArrayList<String>();
		try {
			BufferedReader input =  new BufferedReader(new FileReader("plusCourtsChemins.txt"));
			try {
				String line = null; 

				while (( line = input.readLine()) != null){
					lines.add(line);
				}
			}
			finally {
				input.close();
			}
		}
		catch (Exception ex){
			success = false;
		}

		String line;
		HashMap<STATION,ITIN> plusCourtsChemins = null;
		STATION from=null;
		int fromIndex=-1;
		STATION to=null;
		int toIndex=-1;
		STATION by=null;
		int byIndex=-1;
		ITIN chem = null;

		for(int i=0;i<lines.size()&&success; i++){
			line = lines.get(i);
			if(line.startsWith(Balises.FROM_Begin)){
				String fromName = line.substring(Balises.FROM_Begin.length(),line.length()-1);
				from = new STATION_IMP(fromName);
				fromIndex = graph.getListeStations().indexOf(from);
				if(fromIndex > -1){
					plusCourtsChemins = new HashMap<STATION, ITIN>();
				}else{
					plusCourtsChemins = null;
				}
			}
			if(line.startsWith(Balises.TO_Begin)&&fromIndex > -1){
				String toName = line.substring(Balises.TO_Begin.length(),line.length()-1);
				to = new STATION_IMP(toName);
				toIndex = graph.getListeStations().indexOf(to);
				if(toIndex>-1){
					chem = new ITIN(from,to);
				}else{
					chem = null;
				}
			}
			if(line.startsWith(Balises.BY)&&fromIndex > -1&&toIndex>-1){
				String byName = line.substring(Balises.BY.length(),line.length()-1);
				by = new STATION_IMP(byName);
				byIndex = graph.getListeStations().indexOf(by);
				if(byIndex>-1){
					chem.addEtape(by);
				}else{
					// Le fichier est corrompu (peut etre que le graphe a changé) 
					//il faut recalculer les itineraire
					success = false; 
				}
			}
			if(line.startsWith(Balises.TO_End)&&fromIndex > -1&&toIndex>-1&&byIndex>-1){
				plusCourtsChemins.put(to, chem);
			}
			if(line.startsWith(Balises.FROM_End)&&fromIndex > -1&&toIndex>-1&&byIndex>-1){
				from.setPlusCourtsChemins(plusCourtsChemins);
				plusCourtsChemins = null;
				from=null;
				fromIndex=-1;
				to=null;
				toIndex=-1;
				by=null;
				byIndex=-1;
				chem = null;
			}
		}
		return success;
	}

}


