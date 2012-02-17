package source;

public class DepartPasDesserviException extends Exception {

	private static final long serialVersionUID = 1L;

	public DepartPasDesserviException() {
		super("Aucun transport ne dessert votre station de d�part dans l'�tat actuel du r�seau");
	}

}
