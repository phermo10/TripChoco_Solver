package source;

import java.util.ArrayList;

public class ITIN {
	private ArrayList<STATION> etapes;
	private STATION depart;
	private STATION arrivee;
	public int lastSize;
	private double distanceTotale;
	private boolean isImpossible;

	public ITIN(STATION depart, STATION arrivee){
		isImpossible = false;
		etapes = new ArrayList<STATION>();
		this.depart = depart;
		this.arrivee = arrivee;
		lastSize = 0;
		distanceTotale = 0;
		etapes.add(depart);
		etapes.add(arrivee);
	}
	public boolean addEtape(STATION s){
		if(!etapes.contains(s)){etapes.add(s);return false;}{return true;}
	}

	private void majDistanceTotale(){
		lastSize = etapes.size();
		if(!isImpossible){
			ordonnerEtapes();
			distanceTotale = 0;
			for(int i=1; i<etapes.size();i++){
				distanceTotale = distanceTotale + etapes.get(i-1).getCoords().distance(etapes.get(i).getCoords());
			}
		}else{distanceTotale = Double.POSITIVE_INFINITY;}
	}

	private void ordonnerEtapes(){
		ArrayList<STATION> etapesTriees = new ArrayList<STATION>();
		etapes.remove(depart);
		etapesTriees.add(depart);
		double dMin = -1;
		STATION pp = depart;
		while(etapes.size()>0){
			dMin = -1;
			for(STATION s : etapes){
				double d = s.getCoords().distance(pp.getCoords());
				if(dMin == -1 || dMin > d){
					dMin = d;
					pp = s;
				}
			}
			etapes.remove(pp);
			etapesTriees.add(pp);
		}
		etapesTriees.add(arrivee);
		etapes = etapesTriees;
	}


	public double getDistTot(){if(!isImpossible){if(lastSize!=etapes.size()){majDistanceTotale();}}return distanceTotale;}
	
	public int getDureeTot(int vitesse){
		int dureeDesEtapes = 0;
		for(STATION etape : etapes){
			dureeDesEtapes+=etape.getDureeVisite();
		}
		return ((int)Math.floor(getDistTot()/vitesse))+1+dureeDesEtapes;
	}


	public void makeImpossible(){
		distanceTotale = Double.POSITIVE_INFINITY;
		isImpossible = true;
	}


	public ITIN prolonger(STATION newArrivee){
		ITIN clone = new ITIN(depart, newArrivee);
		for(STATION s : etapes){
			clone.addEtape(s);
		}	
		return clone;
	}

	public boolean goesBy(STATION s){
		return etapes.contains(s);
	}

	public boolean isPossible(){
		return !isImpossible;
	}

	/**
	 * On recherche le meilleur endroit où inserer cette nouvelle étape pour minimiser la distance parcourue
	 * Si aucune des étapes actuelle ne permet de rejoindre la nouvelle étape, retourne false. True si c'est possible.
	 * @param newEtape l'étape à ajouter
	 * @param newPath l'itineraire résultant de l'addition (modification via reference)
	 * @return
	 */
	public boolean tryToGoBy(STATION newEtape, ITIN result){
		boolean ok = false;
		STATION bestPrec = null; // candidat meilleur precedent
		double bestDistTot = -1;
		for(int i = 0;i<etapes.size()&&!ok;i++){
			STATION prec = etapes.get(i);
			for(int j = 0;i<etapes.size()&&!ok;i++){
				STATION suiv = etapes.get(j);
				double distPrecNew = prec.getPlusCourtsChemins().get(newEtape).getDistTot();
				double distNewSuiv = newEtape.getPlusCourtsChemins().get(suiv).getDistTot();
				if(distPrecNew!=Double.POSITIVE_INFINITY && distNewSuiv!=Double.POSITIVE_INFINITY){
					ok = true;
					if(distPrecNew + distNewSuiv < bestDistTot){
						bestPrec = prec;
						bestDistTot = distPrecNew + distNewSuiv;
					}
				}

			}
		}
		if(ok){
			result = new ITIN(depart, arrivee);
			for(int i=0; i<etapes.size();i++){
				result.addEtape(etapes.get(i));
				if(etapes.get(i)==bestPrec){
					result.addEtape(newEtape);
				}
			}
		}
		return ok;
	}

	public ArrayList<STATION> getEtapes(){
		return etapes;
	}
	
	public String toString(){
		String s = "";
		s = "Pour aller de " + depart.getNom() + " à " + arrivee.getNom();
		if(isPossible()){
		s+= " il faut passer par : ";
		for(STATION e : etapes){
			s+="\n" + e.getNom(); 
		}
		s+="\n Distance à vol d'oiseau : " + depart.getCoords().distance(arrivee.getCoords());
		}
		else{ s+=" il n'existe pas de chemin.";}
		return s;
	}
	
	public String toBeautifulString(int vitesse){
		String s = "Votre itineraire de " + depart.getNom() + " à " + arrivee.getNom() + " : ";
		int dureeActuelle = 0;
		double distanceActuelle = 0;
		double distanceTmp = 0;
		ordonnerEtapes();
		dureeActuelle = depart.getDureeVisite();
		s+="Visite de " + depart.getNom() + " : " + depart.getDureeVisite() + " Minutes";;
		int i;
		for(i=1; i<etapes.size()-1;i++){
			distanceTmp = etapes.get(i-1).getCoords().distance(etapes.get(i).getCoords());
			distanceActuelle += distanceTmp;
			s+="\nMarchez " + distanceTmp + " km";
			dureeActuelle += 60*distanceTmp/vitesse + etapes.get(i).getDureeVisite();
			s+="\nVisite de " + etapes.get(i).getNom() + " : " + etapes.get(i).getDureeVisite() + " Minutes";;
		}
		s+="\nMarchez " + etapes.get(etapes.size()-2).getCoords().distance(arrivee.getCoords())
		+"\nVisite de " + arrivee.getNom() + " : " + arrivee.getDureeVisite() + " Minutes"
		+"\nVous êtes arrivés à destination."
		+"\nDistance parcourue : " + distanceActuelle
		+"\nTemps écoulé : " + dureeActuelle;
		return s;
	}

}
