package source;

public class StationNotFoundException extends Exception{
	
	private static final long serialVersionUID = 1L;

	public StationNotFoundException() {
		super("L'une des stations demandées n'a pas été trouvée dans le réseau");
	}

}
