package source;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
public class FICHE_HORAIRE_IMP implements FICHE_HORAIRE {

	private String chemin;
	private boolean[][] horaires;
	private String horairesMisEnForme;
	private boolean desservi;
	private ARRET arret;
	private String ligne;
	private int dir;
	private int size;



	public FICHE_HORAIRE_IMP(ARRET a, String ligne, String cheminRep, int dir) {
		this.chemin = cheminRep + "LINE-" + ligne + java.io.File.separator + "dir" + dir +"-"+a.getIdA()+".text";
		this.horaires = new boolean[24][60];
		for(int i=0;i<horaires.length;i++){
			for(int j=0;j<horaires[0].length;j++){
				horaires[i][j]=false;
			}
		}
		this.arret = a;
		this.ligne = ligne;
		this.dir = dir;
		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(chemin), "UTF-8"));
			String lignelue="";
			for(int i=0; i<5;i++){
				lignelue=br.readLine();
			}
			desservi = !lignelue.contains(Outils.stringPasDesservi);
			horairesMisEnForme = lignelue + "\n";
			String heuresBrutes = br.readLine();
			if(desservi){
				ArrayList<Integer> heures = new ArrayList<Integer>();
				int index = heuresBrutes.indexOf(Outils.separateur);
				while(index!=-1){
					heuresBrutes = heuresBrutes.substring(1);
					index = heuresBrutes.indexOf(Outils.separateur);
					String lheure = index==-1?heuresBrutes:heuresBrutes.substring(0,index);
					//heures.add(Integer.parseInt(lheure)==0?24:Integer.parseInt(lheure));
					heures.add(Integer.parseInt(lheure));
					horairesMisEnForme = horairesMisEnForme + "\t" + lheure;
					heuresBrutes = heuresBrutes.substring(lheure.length());
					index = heuresBrutes.indexOf(Outils.separateur);
				}
				ArrayList<ArrayList<Integer>> minutes = new ArrayList<ArrayList<Integer>>();
				for(int i=0;i<heures.size();i++){minutes.add(new ArrayList<Integer>());}
				lignelue = br.readLine();
				int indexHeure = 0; // indique la position dans "heures" de l'heure correspondant e la minute lue 
				while(!(Outils.isNullEmptyOrWhiteSpace(lignelue))){
					horairesMisEnForme = horairesMisEnForme + "\n" + lignelue;
					String minute = "-";
					for(indexHeure=0; indexHeure<heures.size(); indexHeure++){
						lignelue = lignelue.substring(1);
						if(lignelue.contains(Outils.separateur)){
							minute = lignelue.substring(0,lignelue.indexOf(Outils.separateur));
						}
						else{
							minute = lignelue;
						}
						lignelue = lignelue.substring(minute.length());
						if(!(minute.equals("-"))){
							minutes.get(indexHeure).add(Integer.parseInt(minute));
						}
					}
					lignelue=br.readLine();
				}
				size = 0;
				for(int i=0; i<heures.size();i++){
					for(int j=0;j<minutes.get(i).size();j++){
						int h = (int)heures.get(i);
						int m =(int)minutes.get(i).get(j);
						horaires[h][m]=true;
						size++;
					}
				}
			}
			

		} catch (Exception e) {e.printStackTrace();}
	}


	public int size(){
		return size;
	}

	public boolean isValide(){return desservi;}
	
	public int getAttenteAvantNextStop(int[] dateDepart) {
		// on suppose que les horaires sont deje classes
		int[] nextStop = getNextStop(dateDepart); // retournera null si dateDepart == null OU !desservi
		if(nextStop!=null){
			int nextHour = (nextStop[0]==0)?24:nextStop[0];
			boolean demain = (nextHour<dateDepart[0]||((nextHour)==dateDepart[0])&&nextStop[1]<dateDepart[1]);
			int attente = -1;
			if(demain){ 
				int[] premierHoraireLendemain = getHoraireByIndex(0);
				attente =  premierHoraireLendemain[1] + premierHoraireLendemain[0]*60 + (23 - dateDepart[0])*60 + 60 - dateDepart[1];} // si il n'y a plus d'arret aujourd'hui, ie le prochain arret est le premier du lendemain
			else{
				if(nextStop[0]==dateDepart[0]){
					attente = nextStop[1]-dateDepart[1];
				}
				else{
					attente = nextStop[1] +60*((nextStop[0]==0?24:nextStop[0])-dateDepart[0]-1) + 60 - dateDepart[1];
				}
			}
			return attente;
		}
		else{return -1;}
	}

	public ARRET getArret(){return arret;}
	
	public int[] getNextStop(int[] dateDepart){
		//		on suppose que les horaires sont deja classes
		if(dateDepart!=null&&isValide()){
			int[] dernierHoraire = getHoraireByIndex(size()-1);
			int[] nextStop = dernierHoraire;
			int derniereHeure = (dernierHoraire[0])==0?24:dernierHoraire[0];
			boolean demain = (derniereHeure<dateDepart[0]||((derniereHeure)==dateDepart[0])&&dernierHoraire[1]<dateDepart[1]);
			if(!demain){
				int i = 0;
				int[] lHoraireLu;
				while(i<size()){
					lHoraireLu = getHoraireByIndex(size()-1-i);
					boolean lHoraireLuEstApres = lHoraireLu[0]>dateDepart[0];
					lHoraireLuEstApres = lHoraireLuEstApres||(lHoraireLu[0]==dateDepart[0]&&lHoraireLu[1]>=dateDepart[1]);
					if(lHoraireLuEstApres){
						nextStop = lHoraireLu;
						i++;}
					else{i = size()+1;}
				}
			}
			return nextStop;
		}
		else{return null;}
	}

	public String toString(){
		return "Arret " + getArret().getIdA() + " de la ligne " + getLigne() + " dans la direction " + getDir() + (isValide()?"\n" + horairesMisEnForme:" : n'est actuellement pas desservi."); 
	}

	public boolean equals(Object o){
		if(o instanceof FICHE_HORAIRE){
			FICHE_HORAIRE f = (FICHE_HORAIRE)o;
			return getHoraires().equals(f.getHoraires());
		}else{return false;}
	}

	public boolean[][] getHoraires(){return horaires;}

	public int getDir() {
		return dir;
	}

	public int[] getHoraireByIndex(int i){
		int[] horaire = new int[2];
		if(isValide()&&i<size()){
			int index = -1;
			for(int h=0; h<getHoraires().length;h++){
				for(int m=0; m<getHoraires()[h].length;m++){
					if(getHoraires()[h][m]){index++;}
					if(index==i){horaire[0]=h;horaire[1]=m;m=getHoraires()[h].length;}
				}
				if(index==i){h=getHoraires().length;}
			}
		}
		else{
			horaire=null;
		}
		return horaire;
	}

	public String getIdA() {
		return arret.getIdA();
	}

	public String getLigne() {
		return ligne;
	}

	public int hashCode(){
		return getLigne().hashCode()*getDir()*getHoraires().hashCode();
	}


}
