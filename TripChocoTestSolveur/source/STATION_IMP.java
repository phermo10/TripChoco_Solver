package source;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;


public class STATION_IMP implements STATION {

	private String nom;
	private ArrayList<ARRET> arrets;

	public STATION_IMP(String nom) {
		this.nom = Outils.neutraliser(nom,false);
		this.arrets = new ArrayList<ARRET>();
	}

	public void ajouterArret(ARRET newA) {
		arrets.add(newA);
		newA.setStation(this);
	}

	public ArrayList<ARRET> getListeArrets() {
		return arrets;
	}

	public String getNom() {
		return nom;
	}

	public String toString(){
		return "Station " + getNom() + ". Arr�ts : " + arrets.toString();
	}

	public boolean equals(Object o){
		if(o instanceof STATION_IMP){
			STATION_IMP s = (STATION_IMP)o;
			boolean testCoords = true;
			if(s.getCoords()!=null && getCoords()!=null){
				testCoords = s.getCoords().equals(getCoords());
			}
			return s.getNom().equals(this.getNom())&&testCoords;
		}else{return false;}
	}

	private int score;
	public void setScore(int score){this.score = score;updateModifs();}
	public int getScore(){
		return score;
	}
	private HashMap<STATION,ITIN> itineraires;
	public void setPlusCourtsChemins(HashMap<STATION,ITIN> plusCourtsChemins){
		this.itineraires = plusCourtsChemins;updateModifs();
	}
	public HashMap<STATION,ITIN> getPlusCourtsChemins(){
		return this.itineraires;
	}

	private Point coords;
	public void setCoords(Point p){this.coords = p;updateModifs();}
	public Point getCoords(){return this.coords;}
	public ArrayList<STATION> getSommetsAtteignables(RESEAU graph){
		updateModifs();
		ArrayList<STATION> att = new ArrayList<STATION>();
		for(ARRET a:this.arrets){
			ARRET suiv = a.getSuiv();
			if(suiv!=null){
				STATION s = suiv.getStation();
				if(!att.contains(s)&&!s.equals(this)){
					boolean ok = false;
					for(int i = 0; i<graph.getListeStations().size()&&!ok;i++){
						if(graph.getListeStations().get(i).equals(s)){
							ok = true;
							att.add(graph.getListeStations().get(i));
						}	
					}
				}
			}
		}
		return att;
	}

	private void updateModifs(){
		for(ARRET a : this.arrets){
			a.setStation(this);
		}
	}
	
	public int hashCode(){
		return 3*getNom().hashCode() + 7 * getCoords().x + 11 * getCoords().y;
	}
	
	private int dureeVisite;
	public void setDureeVisite(int duree){this.dureeVisite = duree;}
	public int getDureeVisite(){return this.dureeVisite;}
	
}
