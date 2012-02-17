package source;

public class ItineraireNotPossibleException extends Exception{

	private static final long serialVersionUID = 1L;

	public ItineraireNotPossibleException() {
		super("Cet itineraire est impossible dans l'�tat actuel du r�seau. Il est possible que votre destination ne soit pas desservie dans cette direction.");
	}

}
