package source;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

public class RESEAU_IMP implements RESEAU {

	private ArrayList<STATION> stations;
	private ArrayList<TRANSPORT> transports;
	private String CheminRep;
	
	public RESEAU_IMP(String CheminRep) {
		this.CheminRep = CheminRep;
		this.stations = new ArrayList<STATION>();
		this.transports = new ArrayList<TRANSPORT>();
		try{
			if (initialiser(CheminRep)) {
				System.out.println("Initialisation reussie !");
			}else{System.out.println("Fichier arretid.txt corrompu, l'initialisation a echoue. Fin du programme.");System.exit(0);}
		}
		catch(FileNotFoundException e){
			System.out.println("Fichier arretid.txt introuvable ! Fin du programme.");
			System.exit(0);
		}

	}

	public ITINERAIRE calculItineraire(String sour, String dest, int[] dateDepart, int dureeChangementTransport, boolean leMoinsDeChangement) throws StationNotFoundException, DepartPasDesserviException, ItineraireNotPossibleException {
		ITINERAIRE result = null;
		// recherche de stations correspondantes
		// d'abord par egalite de nom
		STATION source = null;
		STATION destination = null;
		for(int i = 0; i <getListeStations().size();i++){
			STATION st = getListeStations().get(i);
			if(Outils.neutraliser(st.getNom(),true).equals(Outils.neutraliser(sour,true))){source = st;}
			if(Outils.neutraliser(st.getNom(),true).equals(Outils.neutraliser(dest,true))){destination = st;}
			if(source!=null&&destination!=null){i=getListeStations().size();}
		}
		// deuxieme chance avec des contains (exemple si la station demandee etait gare sncf, il faudrai trouver gare sncf nord
		if(source==null){
			for(int i = 0; i <getListeStations().size();i++){
				STATION st = getListeStations().get(i);
				if(Outils.neutraliser(st.getNom(),true).contains(Outils.neutraliser(sour,true))){source = st;i=getListeStations().size();}
			}
		}
		if(destination==null){
			for(int i = 0; i <getListeStations().size();i++){
				STATION st = getListeStations().get(i);
				if(Outils.neutraliser(st.getNom(),true).contains(Outils.neutraliser(dest,true))){destination = st;i=getListeStations().size();}
			}
		}
		if(source!=null&&destination!=null){ // on a trouvé les stations

			//Variables pour dijkstra
			ArrayList<ARRET> lesArrets = getListeArrets(); // pour ne pas rappeller la méthode à chaque fois (performance)
			HashMap<ARRET, Integer> distances = new HashMap<ARRET, Integer>(lesArrets.size()); 
			ArrayList<ARRET> nonVisites = new ArrayList<ARRET>(lesArrets); // au début aucun arret n'a été visité
			ArrayList<ARRET> visites = new ArrayList<ARRET>(lesArrets.size());
			HashMap<ARRET,ARRET> predecesseurs = new HashMap<ARRET, ARRET>(lesArrets.size());
			
			// Pour ne pas avoir à le refaire plus tard, on va stocker au fur et à mesure l'heure à laquelle
			// on atteint chaque arret, de la meme maniere qu'on stocke la distance.
			HashMap<ARRET, int[]> heuresLocales = new HashMap<ARRET, int[]>(lesArrets.size());
			
			// Permettra d'arreter dijkstra quand on aura visite tous les arrets de la destination
			ArrayList<ARRET> destinationsPossiblesVisitees = new ArrayList<ARRET>(destination.getListeArrets().size());
			
			// Representera l'heure a laquelle on arriverai a l'arret dont on parcourera les voisins
			int[] heureLocale = new int[2];
			heureLocale[0]=dateDepart[0];
			heureLocale[1]=dateDepart[1];

			// initialisation
			// les arrets de la station de depart ont pour distance l'attente avant leur prochain depart
			// les autres arrets ont pour distance l'infinie
			// En fait, d'un point de vue graphe theorique, vu qu'il y a plusieurs points de depart possibles
			// on fait comme si on avait cree un sommet de depart virtuel qui leur est connecté
			ARRET plusProche = null;
			int attMin = -1;
			for(ARRET a:lesArrets){
				if(!source.getListeArrets().contains(a)){
					distances.put(a, -1); // -1 implémente l'infini
					heuresLocales.put(a, null); // null implémente le "jamais"
				}
				else{
					// le plusProche de notre sommet virtuel sera l'arret de depart qui part le plus tot
					int attente = a.getFicheHoraire().getAttenteAvantNextStop(dateDepart);
					distances.put(a,attente);
					heuresLocales.put(a,attente>-1?Outils.additionMinutes(dateDepart, attente):null);
					if(attMin==-1||attente<attMin){
						attMin = attente;
						plusProche = a;
					}
				}
			}
			// Si attMin==-1 c'est qu'aucun arret de la station de depart n'est desservi
			if(attMin==-1){throw new DepartPasDesserviException();}

			majDistances(distances, predecesseurs, visites, heuresLocales, plusProche, plusProche, attMin);

			// >>DIJKSTRA<<
			do{
				nonVisites.remove(plusProche);
				visites.add(plusProche);
				heureLocale = heuresLocales.get(plusProche);
				for(ARRET vers : plusProche.getStation().getListeArrets()){
					if(!vers.equals(plusProche)){
						int[] heureApresChangement = Outils.additionMinutes(heureLocale, dureeChangementTransport);
						int attente = vers.getFicheHoraire().getAttenteAvantNextStop(heureApresChangement);
						majDistances(distances, predecesseurs, visites, heuresLocales, plusProche, vers, dureeChangementTransport + attente);
					}
				}
				ARRET vers = plusProche;
				int attns = -1;
				while(attns==-1&&vers!=null){
					vers=vers.getSuiv();
					attns=vers!=null?vers.getFicheHoraire().getAttenteAvantNextStop(heureLocale):-1;
				}
				if(vers!=null){
					majDistances(distances, predecesseurs, visites, heuresLocales, plusProche, vers, attns);
				}


				plusProche = getPlusProche(distances, nonVisites);
				
				if(plusProche!=null&&plusProche.getStation().equals(destination)){
					destinationsPossiblesVisitees.add(plusProche);
				}
			}while(nonVisites.size()>0 && plusProche!=null && !destinationsPossiblesVisitees.equals(destination.getListeArrets()));
			// >>FIN DE DIJKSTRA<<
			
			
			ARRET plusProcheDansDest = null;
			
			if(leMoinsDeChangement){
				// nombre minimal de changements nécessaires actuel
				int nbMin = -1; 
				// tableau representant les itineraires possibles qu'a trouve dijkstra
				ITINERAIRE[] lesItPossibles = new ITINERAIRE[destination.getListeArrets().size()];
				// index du meilleur itineraire dans lesItPossibles
				int indexDuMeilleur = -1; 
				
				// On reconstruit tous les itineraires possibles 
				for(int i=0; i<destination.getListeArrets().size();i++){
					ARRET a = destination.getListeArrets().get(i);
					if(distances.get(a)>-1){ // (ie si dijkstra a trouvé un chemin vers cet arret)
						lesItPossibles[i] = construireItineraire(a, source, heuresLocales, predecesseurs);
						
						// Pour chaque itineraire, on regarde si il est celui avec le moins de changement
						int nb = lesItPossibles[i].getNombreChangements();
						if(nbMin==-1||(nb<nbMin)){
							plusProcheDansDest = a;
							nbMin = nb;
							indexDuMeilleur = i;
						}else{if(nb==nbMin){
							// Si jamais il a le meme nb de changements que le meilleur itineraire actuel
							// on regarde lequel des deux arrive le plus vite
							boolean estMeilleur = distances.get(a)<distances.get(plusProcheDansDest);
							plusProcheDansDest=estMeilleur?a:plusProcheDansDest;
							nbMin=estMeilleur?nb:nbMin;
							indexDuMeilleur=estMeilleur?i:indexDuMeilleur;
						}}
					}
				}
				//Si aucun itineraire n'est possible on throw une exception
				if(indexDuMeilleur==-1){throw new ItineraireNotPossibleException();}
				else{result=lesItPossibles[indexDuMeilleur];}
			}
			else{// si on veut juste le plus court chemin
				// algo classique, on cherche le meilleur parmi les itineraires possibles
				
				// duree du meilleur itineraire actuel
				int dMinVersDest = -1;
				// pour chaque arret de destinatin on regarde si l'itineraire associé arrive plus tot
				for(ARRET a: destination.getListeArrets()){ 
					int dParA = distances.get(a);
					if(dParA>-1&&(dParA<dMinVersDest||dMinVersDest==-1)){
						dMinVersDest = dParA;
						plusProcheDansDest = a;
					}
				}
				System.out.println(plusProcheDansDest);
				result = construireItineraire(plusProcheDansDest, source, heuresLocales, predecesseurs);
				if(result==null){throw new ItineraireNotPossibleException();}
			}
			return result;
		}else{throw new StationNotFoundException();}
	}

	
	/**
	 * Reconstruit l'itineraire aboutissant a l'arret terminus en parcourant la structure predecesseurs.
	 * Il n'y a aucune gestion des incohérences (l'algo ne marche que si les predecesseurs sont cohérents)
	 * car si les predecesseurs ne forment pas un itineraire, ça ne peut être que parceque
	 * l'algo dijkstra est faux
	 * @param terminus
	 * @param source
	 * @param heuresLocales
	 * @param predecesseurs
	 * @return null si terminus==null
	 */
	private ITINERAIRE construireItineraire(ARRET terminus, STATION source, HashMap<ARRET, int[]> heuresLocales, HashMap<ARRET,ARRET> predecesseurs){
		ITINERAIRE it = null;
		ARRET a = terminus;
		if(a!=null){
			it=new ITINERAIRE_IMP();
			ARRET pred = null;
			boolean fini=false;
			do{
				it.addEtape(a, heuresLocales.get(a));
				pred = predecesseurs.get(a);
				if(a.equals(pred)||a.getStation().equals(source)){fini=true;}
				else{a=pred;}
			}while(!fini);
		}
		return it;
	}
	
	/**
	 * Met à jour le graphe des distances sur le principe classique de dijkstra
	 * Si n2.parcouru > n1.parcouru + distance(n1, n2)   // distance correspond au poids de l'arc reliant n1 et n2
     *           n2.parcouru = n1.parcouru + distance(n1, n2)
     *           n2.précédent = n1   // Dit que pour aller à n2, il faut passer par n1
     * Fin si
	 * @param distances
	 * @param predecesseurs
	 * @param visites
	 * @param heuresLocales
	 * @param plusProche
	 * @param a2
	 * @param cout
	 */
	private void majDistances(HashMap<ARRET, Integer> distances, HashMap<ARRET,ARRET> predecesseurs, ArrayList<ARRET> visites, HashMap<ARRET, int[]> heuresLocales, ARRET plusProche, ARRET a2, int cout){
		if(!visites.contains(a2)){
			if(cout>-1){
				if(distances.get(a2) == -1 || distances.get(a2) > distances.get(plusProche) + cout){
					distances.put(a2, distances.get(plusProche) + cout);
					predecesseurs.put(a2, plusProche);
					heuresLocales.put(a2, Outils.additionMinutes(heuresLocales.get(plusProche), cout));
				}
			}else{				
				distances.put(a2, -1);
				predecesseurs.put(a2, null);
				heuresLocales.put(a2, null);
			}
		}
	}

	/**
	 * Retourne l'arret le plus proche
	 * @param distances
	 * @param nonVisites
	 * @return null si tous les arrets sont a l'infini
	 */
	private ARRET getPlusProche(HashMap<ARRET, Integer> distances, ArrayList<ARRET> nonVisites){
		ARRET plusProche = null;
		int dMin = -1;
		for(ARRET a : nonVisites){
			if(distances.get(a)>-1&&(distances.get(a)<dMin||dMin==-1)){
				dMin = distances.get(a);
				plusProche = a;
			}
		}
		return plusProche;
	}

	public ArrayList<STATION> getListeStations() {
		return stations;
	}

	public ArrayList<TRANSPORT> getListeTransports() {
		return transports;
	}

	public boolean initialiser(String cheminRep) throws FileNotFoundException {
		boolean reussi = false;
		try{
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(cheminRep + "arretid.txt"), "UTF-8"));
			String ligneLue;
			try{
				ligneLue = br.readLine();
				TRANSPORT t1;
				TRANSPORT t2;
				while(ligneLue !=null){
					t1 = Fabrics.creerTransport(ligneLue.substring(3));
					t2 = Fabrics.creerTransport(ligneLue.substring(3));
					String station;
					STATION s;
					String arret;
					ARRET a1;
					ARRET a2;
					String position;
					int pos;
					while(((ligneLue = br.readLine())!=null)&&(!ligneLue.startsWith(("-- Line")))){
						station = ligneLue.substring(ligneLue.indexOf("-")+1);
						station = station.substring(0, station.lastIndexOf("-"));
						s = Fabrics.creerStation(station);

						arret = ligneLue.substring(0, ligneLue.indexOf("-"));
						a1 = Fabrics.creerArret(t1, s, arret, cheminRep, 1);
						a2 = Fabrics.creerArret(t2, s, arret, cheminRep, 2);
						position = ligneLue.substring(ligneLue.lastIndexOf("-")+1);
						pos = ((int)Integer.parseInt(position))-1;

						int indOf = stations.indexOf(s);
						if(indOf==-1){
							s.ajouterArret(a1);
							s.ajouterArret(a2);
							this.stations.add(s);
						}else{
							STATION s1 = stations.get(indOf);
							if (!(s1.getListeArrets().contains(a1))){
								stations.get(indOf).ajouterArret(a1);							
							}
							if (!(s1.getListeArrets().contains(a2))){
								stations.get(indOf).ajouterArret(a2);							
							}
						}
						a1.setPos(pos);
						t1.ajouterArret(a1, pos);
						a2.setPos(pos);
						t2.ajouterArret(a2, pos);
					}
					this.transports.add(t1);
					this.transports.add(t2);
				}
				reussi = true;
			}catch(IOException e){e.printStackTrace();}
		}
		catch(UnsupportedEncodingException encEx){encEx.printStackTrace();}
		return reussi;
	}

	public String toString(){
		String lesStations = "";
		for(int i=0;i<stations.size();i++){
			lesStations = lesStations + ((ArrayList)stations).get(i).toString() + "\n";
		}
		String lesTransports = "";
		for(int i=0;i<transports.size();i++){
			lesTransports = lesTransports + ((ArrayList)transports).get(i).toString() + "\n";
		}
		return "Stations du reseau :\n" + lesStations + "\n" + "Transports du reseau :\n" + lesTransports;
	}

	public ArrayList<ARRET> getListeArrets(){
		ArrayList<ARRET> lesArrets = new ArrayList<ARRET>();
		for(STATION s : getListeStations()){
			lesArrets.addAll(s.getListeArrets());
		}
		return lesArrets;
	}
	
	public int getNbNoeuds(){
		int n=0;
		for(STATION s : stations){
			n = n + s.getListeArrets().size();
		}
		return n;
	}
	public int getNbArcs(){
		int n= 0;
		for(TRANSPORT t:transports){
			n = n + t.getListeArret().size() - 1;
		}
		return n;
	}
	public void setStations(ArrayList<STATION> stations){this.stations = stations;}
	
	public String getCheminRep(){return this.CheminRep;}
	public void setTransports(ArrayList<TRANSPORT> transports){
		this.transports = transports;
	}
}
