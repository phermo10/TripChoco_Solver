package source;
import java.util.ArrayList;

public class ITINERAIRE_IMP implements ITINERAIRE {

	private ArrayList<ARRET> arrets; 
	private ArrayList<int[]> horaires;

	public ITINERAIRE_IMP(){
		arrets = new ArrayList<ARRET>();
		horaires = new ArrayList<int[]>();
	}

	public String toHtml() {
		String h = 
			"<html><head><title>" 
			+ "Itineraire de " + getArrets().get(0).getStation().getNom() + " a " + getArrets().get(size()-1).getStation().getNom()
			+ "</title></head><body>" + toString().replaceAll("\n", "<br>").replaceAll("\t", "") +"</body></html>";
		return h;
	}

	public String toString(){
		if(size()>0){
			String s="Itineraire de " + getArrets().get(0).getStation().getNom() + " a " + getArrets().get(size()-1).getStation().getNom() + " {\n";
			TRANSPORT transportActuel = getArrets().get(0).getTransport();
			s = s + "\t" + getHoraires().get(0)[0] + "h" + getHoraires().get(0)[1]+"m : Prendre " + transportActuel.toStringCourt() + " a " + getArrets().get(0).getStation().getNom() + "\n";
			for(int i=1; i<size()-1;i++){
				if(!getArrets().get(i).getTransport().equals(transportActuel)){
						s = s + "\t" + getHoraires().get(i-1)[0] + "h" + getHoraires().get(i-1)[1] + "m : Descendre a " + getArrets().get(i-1).getStation().getNom() + "\n";
						int duree = Outils.duree(getHoraires().get(i-1), getHoraires().get(i));
						s = s + "Attendre " + duree + " minutes\n"
						+ "\t" + getHoraires().get(i)[0] + "h" + getHoraires().get(i)[1]+"m : Prendre " + getArrets().get(i).getTransport().toStringCourt() + " a " + getArrets().get(i).getStation().getNom() + "\n";
						transportActuel = getArrets().get(i).getTransport();
					}
				}
			s = s + "\t" + getHoraires().get(size()-1)[0] + "h" + getHoraires().get(size()-1)[1] + "m : Descendre a " + getArrets().get(size()-1).getStation().getNom() + "\n}";
			return s;
			}
		return "Impossible de calculer cet itineraire.";
		}
	

	public int getDureeTotale(){
		return Outils.duree(getHoraires().get(0), getHoraires().get(size() - 1));
	}
	
	public int getNombreChangements(){
		int n = 0;
		TRANSPORT transportPrecedent = getArrets().get(0).getTransport();
		for(int i=1; i<size();i++){
			TRANSPORT transportActuel = getArrets().get(i).getTransport();
			n=transportPrecedent.equals(transportActuel)?n:n+1;
		}
		return n;
	}

	public ArrayList<String> getListeStations() {
		ArrayList<String> stations = new ArrayList<String>();
		for(ARRET a:getArrets()){
			String laStation = a.getStation().getNom();
			if(!stations.contains(laStation)){
				stations.add(laStation);
			}
		}
		return stations;
	}

	public void addEtape(ARRET a, int[] horaire) {
		arrets.add(0,a);
		horaires.add(0,horaire);
	}

	public ArrayList<ARRET> getArrets() {
		return arrets;
	}

	public ArrayList<int[]> getHoraires() {
		return horaires;
	}

	public int size() {
		return arrets.size();
	}
	
}
