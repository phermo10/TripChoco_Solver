package source;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;


public class TRANSPORT_IMP implements TRANSPORT {

	private String identifiantT;
	private HashMap<Integer,ARRET> arrets;
	private EnumTypeTransport type;
	private int dir=-1;
	
	public TRANSPORT_IMP(String identifiantT) {
		this.identifiantT = Outils.neutraliser(identifiantT, false);
		this.type = EnumTypeTransport.UNDEFINED;
		arrets = new HashMap<Integer,ARRET>();
	}

	public void ajouterArret(ARRET a, int pos) {
		arrets.put(pos, a);
		a.setPos(pos);
		a.setTransport(this);
		/*
		 * Le code suivant obtient le type du transport et sa direction
     	 * car on ne peut pas le savoir avant d'avoir ajouté au moins un arret !
		 */
		if(type == EnumTypeTransport.UNDEFINED){
			String chemin = a.getCheminRep() + identifiantT + "/dir" + 1 +"-"+a.getIdA()+".text";
			try {
				BufferedReader br = new BufferedReader(new FileReader(chemin));
				String lignelue=br.readLine();
				lignelue = br.readLine(); // 2e ligne
				lignelue = lignelue.substring(lignelue.indexOf("|") + 2);
				lignelue = lignelue.substring(0, lignelue.indexOf("|") - 1);
				if(lignelue.equals("BUS")){this.type = EnumTypeTransport.BUS;}
				if(lignelue.equals("TRAMWAY")){this.type = EnumTypeTransport.TRAM;}
				if(lignelue.equals("BUSWAY")){this.type = EnumTypeTransport.NAVIBUS;}
			} catch (Exception e) {e.printStackTrace();}
			dir = a.getDir();
		}
	}

	public HashMap<Integer,ARRET> getListeArret() {
		return arrets;
	}

	public String getNom() {
		return identifiantT;
	}

	public EnumTypeTransport getType() {
		return type;
	}

	public void setType(EnumTypeTransport type) {
		this.type = type;
	}
	
	public String toString(){
		return "Transport " + getNom() + (dir>-1?" (direction " + dir + ")":"") + " de type " + getType() + ". Arrets : " + arrets.toString();
	}

	public boolean equals(Object o){
		if(o instanceof TRANSPORT){
			TRANSPORT t = (TRANSPORT)o;
			return t.getNom().equals(getNom());
		}else{return false;}
	}

	public String getLigne() {
		return getNom().substring(5);
	}

	public String toStringCourt() {
		return (getType()!=null)?"le " + Outils.neutraliser(getType().name(), false)+" " + getLigne():"la ligne " + getLigne();
	}
}
