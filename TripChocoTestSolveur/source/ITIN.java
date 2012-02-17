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
		ArrayList<STATION> e = new ArrayList<STATION>();
		etapes.remove(depart);
		e.add(depart);
		while(etapes.size()>0){
			double dMin = -1;
			STATION pp = depart;
			for(STATION s : etapes){
				if(!s.equals(arrivee)){
					double d = s.getCoords().distance(pp.getCoords());
					if(dMin == -1 || dMin > d){
						dMin = d;
						pp = s;
					}
				}
			}
			etapes.remove(pp);
			e.add(pp);
		}
		etapes.remove(arrivee);
		e.add(arrivee);
		etapes = e;
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
	
	public ArrayList<String> getEtapes(){
		ArrayList<String> lesEtapes = new ArrayList<String>();
		ordonnerEtapes();
		for(STATION etape : etapes){
			lesEtapes.add(etape.getNom());
		}
		return lesEtapes;
	}

}
