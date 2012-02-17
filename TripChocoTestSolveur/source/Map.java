package source;
//---------------------------------------------
//Nom    : HERMOUET
//Prenom : Pierre
//---------------------------------------------
import java.awt.*;

import javax.swing.*;
import java.awt.event.*;
import java.awt.MediaTracker;
import java.io.*;
import java.util.ArrayList;

//OK //- completez mouseDragged afin de modifier this.x et this.y en consequence

//OK //- Completez mouseWheelMoved afin de modifier le facteur de zoom en consequence. Vous pouvez appeler this.repaint(); afin de faire un appel indirecte a la methode paint

//OK //- Completez componentResized afin de prendre en compte le redimensionnement de la fenetre.

//OK //- Completer charger 

//OK //- Completer paintComponent afin d'afficher des cercles rouges sur les stations de l'itineraire si l'itineraire ne vaut pas null.

//OK // - Completer mouseMoved afin que si on est en mode selection de stations on affiche dans la barre de tite de la boite de dialogue (methode setTitle) le nom de la station la plus proche de la souris

//OK //- Completer mouseClicked afin qu'on ajoute la station la plus proche a la liste des stations selectionnes
//OK //et appeler la methode setResult de notre boite de dialogue si d'aventure on a deja selectionne deux stations et qu'on venait deja de cliquer sur cette station en dernier. 

//OK //- passage d'une linkedList<String> a un ITINERAIRE

//OK //- les setToolTip et donc un acces au reseau pour récupérer le code html a afficher pour visualiser les fiches horaires.
//OK //--> clic droit == activier/desactiver les tooltips

public class Map extends JPanel implements   MouseListener, ActionListener, MouseMotionListener, MouseWheelListener, ComponentListener{

	// CONSTANTES
	static final long serialVersionUID=1;

	// >>>> Voir methode ajuster <<<<
	/**
	 * niveau de zoom minimal
	 */
	private static final double leZoomMin = 0;
	/**
	 * niveau de zoom maximal
	 */
	private static final double leZoomMax = 20; 
	/**
	 * coefficient multiplicateur de la variation de zoom
	 */
	private static final double vitesseDeZoom = 5; 


	// constantes pour l'affichage
	/**
	 * rayon du cercle a dessiner autour des stations
	 */
	private static final int rayonCercleStation = 20;

	/**
	 * epaisseur du cercle
	 */
	private static final int epaisseurCercleStation = 10;

	/**
	 * taille des caracteres d'affichage des horaires de l'itineraire
	 */
	private static final int tailleCaracteresHeures=30;

	/**
	 * couleur d'affichage des horaires de l'itineraire
	 */
	private static final Color couleurDesTextes = java.awt.Color.black;


	public static final String FICHIER_IMAGE = "map.jpg"; // fichier contenant l'image du reseau
	public static final String FICHIER_COORDONNEES = "coordonnees.txt"; // fichier contenant les coordonnees des stations

	// VARIABLES D'INSTANCE
	private MediaTracker tracker; // pour atteindre la fin du chargement des images
	private Image offscreen;  // pour le double buffering : on n'affiche pas directement sur l'ecran, mais sur une image et une fois l'image mis a jour c'est elle qu'on affiche sur l'ecran
	private Graphics bufferGraphics; // le Graphics du double buffering : c'est sur lui qu'on trace. 

	private boolean charge=false; // vrai si les images sont chargees, false sinon.

	private int colDepart, ligDepart; // dernieres positions connues de la souris depuis que le bouton de la souris a ete enfonce

	private Image carte; // la carte du reseau
	private int largeur, hauteur; // largeur et hauteur de l'image (carte) en prenant en compte le zoom
	private int x,y; // position du coin haut gauche de l'image
	private int minX, maxX, minY, maxY; // positions limites pour le coin haut gauche de l'image

	/**
	 * le niveau de zoom actuel
	 */
	private double leZoom;

	private ArrayList<String> stations;    // les noms des stations
	private ArrayList<Point> coordonnees ; // les coordonnees des stations
	public final ITIN itineraire; // la liste des noms de station de l'itineraire a afficher

	private DialogMap dialog; // la dialogMap dont on fait partie

	/**
	 * le menu contextuel
	 */
	private JPopupMenu popup; 

	/**
	 * l'item du menu contextuel
	 */
	private JMenuItem menuItem;

	/**
	 * la selection de noms de stations
	 */
	private String[] result;

	/**
	 * true si la dialogMap est utilisee pour selectionner des stations de depart et d'arrivee
	 * false si la dialogMap est utilisee pour afficher un itineraire
	 */
	private boolean jeSersASelectionnerLesStations; 
	private RESEAU network;


	public Map(DialogMap d, ITIN  itin, RESEAU network) {
		this.dialog = d;
		this.itineraire = itin;
		this.network = network;
		this.jeSersASelectionnerLesStations = itineraire==null;
		/*if(!jeSersASelectionnerLesStations){
			setToolTipText(itineraire.toHtml());
		}*/
		this.coordonnees = new ArrayList<Point>();
		this.stations = new ArrayList<String>();
		this.charger(); // consulte le fichier de coordonnees afin de mettre a jour les coordonnees et les noms des stations
		result = new String[2]; // car ne contiendra que la station de depart et d'arrivee
		result[0] = null;
		result[1]=null;

		this.leZoom = 0;

		tracker = new MediaTracker(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		addComponentListener(this);

		// creation du menu contextuel
		if(!jeSersASelectionnerLesStations){
			popup = new JPopupMenu();
			menuItem = new JMenuItem("Activer/Désactiver tooltip");
			menuItem.addActionListener(this);
			popup.add(menuItem);
			popup.addMouseListener(this);
			dialog.getContentPane().add(popup);
		}
	}



	/**
	 * Consulte le fichier de coordonnes afin de mettre a jour la liste des noms de station et leurs coordonnees
	 */
	private void charger() {

		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(FICHIER_COORDONNEES), "UTF-8"));
			String nom = br.readLine();
			while(nom!=null){
				stations.add(Outils.neutraliser(nom,false));
				int x = (int)Integer.parseInt(br.readLine());
				int y = (int)Integer.parseInt(br.readLine());
				coordonnees.add(new Point(x,y));
				nom = br.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void chargerImages() {
		// Creation de oofscreen pour le double buffering
		Dimension tailleEcran = java.awt.Toolkit.getDefaultToolkit().getScreenSize(); // dimensions de l'ecran
		offscreen = createImage((int)tailleEcran.getWidth(), (int)tailleEcran.getHeight());//

		// creation de l'image du reseau
		carte = Toolkit.getDefaultToolkit().getImage(FICHIER_IMAGE);

		// attente du chargement via un tracker
		tracker.addImage(offscreen, 0);
		tracker.addImage(carte, 0);
		try {
			tracker.waitForID(0);
		} catch (InterruptedException e) {
			System.out.println("erreur tracker "+e);
		}
		if (offscreen==null || offscreen.getWidth(null)<0 || carte==null || carte.getWidth(null)<0){
			System.out.println("images pas bien chargees");
			this.charge=false;
		}
		else {
			this.charge=true;
		}
		this.x=this.minX;
		this.y=this.minY;

		this.largeur = carte.getWidth(null);
		this.hauteur = carte.getHeight(null);
		this.ajuster();
	}

	/**
	 * met a jour zoomMin, largeur, hauteur, minX, ... et minY pour tenir compte des dimensions de la fenetre
	 * la strategie d'affichage est de ne pas deformer la carte tout en remplissant tout le cadre. D'où le fait que selon les proportions de
	 * la fenetre on ne peut pas toujours afficher toute la carte en dézoomant
	 */
	private void ajuster() {
		if(this.charge){
			// On calcule le coefficient multiplicateur a appliquer a la taille de l'image
			double coeffZoom =(1 + (this.leZoom / this.leZoomMax)*vitesseDeZoom);
			int ancienneLargeur = largeur;
			int ancienneHauteur = hauteur;
			boolean largeurSuperieureAHauteur = dialog.getWidth()>dialog.getHeight();

			// Ceci est ce qui fait garder les proportions tout en remplissant la fenetre
			// si la fenetre est en paysage on zoome sur la largeur puis on ajuste la hauteur pour garder les proportions
			// et inversement si on est en portrait
			if(largeurSuperieureAHauteur){ 
				largeur = (int)(((double)dialog.getWidth())*coeffZoom);
				hauteur = hauteur * largeur/ancienneLargeur;
			}else{
				hauteur = (int)(((double)dialog.getHeight())*coeffZoom);
				largeur = largeur * hauteur/ancienneHauteur;
			}

			// On deplace les x et y pour que le zoom se fasse en restant centré sur le centre de la fenetre
			this.x = this.x + (ancienneLargeur - largeur)/2; 
			this.y = this.y + (ancienneHauteur - hauteur)/2;


			minX=-(this.largeur-dialog.getWidth());
			maxX=0;
			minY=-(this.hauteur-dialog.getHeight());
			maxY=0;
			recadrer();
		}
	}

	/**
	 * Modifie this.x (resp. this.y) Si this.x (resp. this.y) est en dehors de [minX, maxX] (resp. [minY, maxY]) pour le remettre dans cette plage de validite 
	 */
	private void recadrer() {
		x=x>maxX?maxX:x;
		y=y>maxY?maxY:y;
		x=x<minX?minX:x;
		y=y<minY?minY:y;
	}


	/**
	 * methode appelee automatiquement chaque fois que le composant doit etre redessine
	 * @param g, la matrice de pixels du composant
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (!this.charge) {
			chargerImages();
		}
		else {
			// on va tout effectuer sur bufferGraphics avant d'afficher bufferGraphics sur g
			bufferGraphics = offscreen.getGraphics(); 
			bufferGraphics.drawImage(carte, this.x, this.y, largeur, hauteur, this);
			if(!jeSersASelectionnerLesStations){
				ArrayList<String> lesStations = itineraire.getEtapes();
				for(int j = 0; j<lesStations.size();j++){
					int index = stations.indexOf(lesStations.get(j));
					if(index>-1){
						Point A = mettreALEchelle(coordonnees.get(index));
						Point M = new Point(x+A.x,y+A.y);

						//Affichage de l'horaire
						bufferGraphics.setFont(new Font(bufferGraphics.getFont().getName(), Font.BOLD, tailleCaracteresHeures));
						bufferGraphics.setColor(couleurDesTextes);
						//bufferGraphics.drawString(itineraire.getHoraires().get(j)[0] + "h" + itineraire.getHoraires().get(j)[1] +"m", M.x - rayonCercleStation, M.y - rayonCercleStation + 2);

						// La boucle est là pour donner un simulacre d'épaisseur au cercle..... Même si ça rend plutot moche.
						for(int i = 0;i<=epaisseurCercleStation;i++){
							bufferGraphics.drawOval(M.x - rayonCercleStation+i/2, M.y - rayonCercleStation+i/2, 2*(rayonCercleStation - i/2), 2*(rayonCercleStation - i/2));
						}

						// Normalement ne devrait jamais arriver tant que le fichier coordonnees est a jour
					}else{System.out.println(lesStations.get(j) + " n'est pas dans les stations");System.out.println(stations);}
				}
			}else{
				for(STATION s1 : this.network.getListeStations()){
					Point p1 = mettreALEchelle(s1.getCoords());
					Point m1 = new Point(x+p1.x,y+p1.y);
					for(STATION s2 : s1.getSommetsAtteignables(this.network)){

						Point p2 = mettreALEchelle(s2.getCoords());
						Point m2 = new Point(x+p2.x,y+p2.y);
						((Graphics2D) bufferGraphics).setStroke(new BasicStroke(5));
						bufferGraphics.setColor(Color.RED);
						bufferGraphics.drawLine(m1.x, m1.y, m2.x, m2.y);
					}
					int r = 3;
					bufferGraphics.setColor(Color.BLUE);
					((Graphics2D) bufferGraphics).setStroke(new BasicStroke(3));
					bufferGraphics.drawOval(m1.x - r, m1.y-r, 2*r, 2*r);
				}
			}

			g.drawImage(offscreen,0, 0, this);
		}
	}

	public void mousePressed(MouseEvent e) {
		if(!jeSersASelectionnerLesStations&&e.isPopupTrigger()){
			popup.show(dialog, e.getX(), e.getY());
		}else{
			this.colDepart=(e.getX());
			this.ligDepart=(e.getY());
		}

	}

	public void mouseDragged(MouseEvent e) {

		this.x = this.x + e.getX()-this.colDepart;
		this.y = this.y + e.getY()-this.ligDepart;
		colDepart = e.getX();
		ligDepart = e.getY();
		ajuster();
		repaint();

	}

	public void  mouseClicked(MouseEvent e) {
		if(jeSersASelectionnerLesStations){
			String titre = dialog.getTitle();
			if(result[0]==null&&stations.contains(titre)){result[0]=titre;}
			else{
				if(!result[0].equals(titre)&&stations.contains(titre)){
					result[1]=titre;dialog.setResult(result);}
			}				
		}
	}


	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {
		if(!jeSersASelectionnerLesStations&&e.isPopupTrigger()){
			System.out.println("trigger");
			popup.show(dialog, e.getX(), e.getY());
		}
	}


	public void mouseMoved(MouseEvent e) {
		if(jeSersASelectionnerLesStations){
			double dMin = -1;
			String plusProche = "";
			for(int i=0; i<stations.size();i++){
				Point p = mettreALEchelle(coordonnees.get(i));
				p = new Point(p.x + x, p.y + y);
				double d = Math.abs(Point.distance(e.getX(), e.getY(), p.getX(), p.getY()));
				if(dMin==-1||d<dMin){
					dMin=d;
					plusProche=stations.get(i);
				}
			}
			dialog.setTitle(plusProche);
		}
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		if(!(this.leZoom<this.leZoomMin||this.leZoom>this.leZoomMax)){
			this.leZoom = this.leZoom - e.getWheelRotation();
			this.leZoom = this.leZoom < this.leZoomMin ? this.leZoomMin:this.leZoom;
			this.leZoom = this.leZoom > this.leZoomMax ? this.leZoomMax:this.leZoom;
		}
		ajuster();
		repaint();		
	}

	public void componentResized(ComponentEvent e) {
		ajuster();
		repaint();
	}

	public void componentHidden(ComponentEvent arg0) { }
	public void componentMoved(ComponentEvent arg0) { }
	public void componentShown(ComponentEvent arg0) { }

	/**
	 * Calcule les coordonnees d'une station dans le repère de l'image affichée selon le zoom actuel
	 * @param p
	 * @return
	 */
	private Point mettreALEchelle(Point p){
		return new Point(p.x*largeur/carte.getWidth(null),p.y*hauteur/carte.getHeight(null));
	}


	public void actionPerformed(ActionEvent ev) {
		/*if(!jeSersASelectionnerLesStations){
			Object src = ev.getSource() ;
			if(src == menuItem)
			{
				setToolTipText(getToolTipText()!=null?null:itineraire.toHtml());
			}
		}*/

	}
}
