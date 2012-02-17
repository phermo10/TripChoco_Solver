package source;
/**
 * implementation de l'interface ARRET
 * contient une fiche horaire
 * une station
 * un transport
 * un idA
 * un cheminRep
 * une position
 * une direction
 * @author PH
 *
 */
public class ARRET_IMP implements ARRET {

	private FICHE_HORAIRE f;
	private STATION s;
	private TRANSPORT t;
	private String idA;
	private String cheminRep;
	private int pos; // est initialisé lorsque l'arret est ajouté à son transport
	private int dir;

	public ARRET_IMP(TRANSPORT t, STATION s, String idA, String cheminRep, int dir){
		pos = -1;
		this.t = t;
		this.s = s;
		this.idA = Outils.neutraliser(idA,false);
		this.dir = dir;
		this.f = new FICHE_HORAIRE_IMP(this,t.getLigne(),cheminRep,dir);
		this.cheminRep = cheminRep;
	}

	public ARRET_IMP(ARRET a){
		this(a.getTransport(), a.getStation(), a.getIdA(), a.getCheminRep(), a.getDir());
	}

	public int getDir(){return dir;}
	
	public String getCheminRep(){
		return cheminRep;
	}

	public String getIdA(){return idA;}

	public FICHE_HORAIRE getFicheHoraire() {
		return f;
	}

	public STATION getStation() {
		return s;
	}

	public TRANSPORT getTransport() {
		return t;
	}

	public String toString(){
		return "Arret " + this.idA.toString() + " (" + getTransport().getNom()+" dir " + getDir() + ")" + (getFicheHoraire().isValide()?"":"(pas desservi)");
	}

	public boolean equals(Object o){
		if(o instanceof ARRET_IMP){
			ARRET a = (ARRET)o;
			return a.getIdA().equals(getIdA())&&a.getDir()==getDir()&&a.getFicheHoraire().equals(getFicheHoraire());
		}else{return false;}
	}

	public void setStation(STATION s) {
		this.s = s;
	}

	public void setTransport(TRANSPORT t) {
		this.t = t;

	}

	public ARRET getSuiv(){
		if(dir==2){
			if(getPos()<getTransport().getListeArret().size()-1){
				return getTransport().getListeArret().get(getPos()+1);
			}else{return null;}
		}
		else{
			if(getPos()>0){
				return getTransport().getListeArret().get(getPos()-1);
			}else{return null;}
		}
	}


	public void setPos(int pos) {
		this.pos = pos;
	}

	public int getPos() {
		return pos;
	}

	public int hashCode(){
		return getIdA().hashCode()*getFicheHoraire().hashCode();
	}

	
	
}
