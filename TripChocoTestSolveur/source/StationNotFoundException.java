package source;

public class StationNotFoundException extends Exception{
	
	private static final long serialVersionUID = 1L;

	public StationNotFoundException() {
		super("L'une des stations demand�es n'a pas �t� trouv�e dans le r�seau");
	}

}
