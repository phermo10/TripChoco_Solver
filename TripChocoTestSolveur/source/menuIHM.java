package source;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;


public class menuIHM extends JFrame implements ActionListener{

	private static final long serialVersionUID = 1L;
	private JButton btnChoixStations;
	private JLabel iciCEstLheure;
	private JTextField tBoxHeureDepart;
	private JLabel iciCEstLesMinutes;
	private JTextField tBoxMinuteDepart;
	private JLabel iciCEstLaVitesse;
	private JComboBox cBoxVitesse;
	private JLabel iciCEstLeMode;
	private JComboBox cBoxMode;
	private String source;
	private String destination;
	private JButton okButton;
	private RESEAU tan;
	private int[] dureeChangementTransport;

	menuIHM(RESEAU tan, int[] dureeChangementTransport){
		this.dureeChangementTransport = dureeChangementTransport;
		this.tan = tan;
		source = null;
		destination = null;
		JFrame frame = new JFrame("MinesTan");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(new Dimension(400,400));
		frame.getContentPane().setLayout(new GridLayout(10,1));
		btnChoixStations = new JButton("Cliquez-ici pour choisir votre départ puis votre arrivée");
		btnChoixStations.addActionListener(this);
		frame.getContentPane().add(btnChoixStations);
		iciCEstLheure = new JLabel("Partir à partir de... Heures :");
		frame.getContentPane().add(iciCEstLheure);
		tBoxHeureDepart = new JTextField();
		frame.getContentPane().add(tBoxHeureDepart);
		iciCEstLesMinutes = new JLabel("Minutes :");
		frame.getContentPane().add(iciCEstLesMinutes);
		tBoxMinuteDepart = new JTextField();
		frame.getContentPane().add(tBoxMinuteDepart);
		iciCEstLaVitesse = new JLabel("Vitesse de marche ?");
		frame.getContentPane().add(iciCEstLaVitesse);
		cBoxVitesse = new JComboBox();
		cBoxVitesse.addItem("Yamakasi (Pas d'attente minimale entre les changements");
		cBoxVitesse.addItem("Rapide (2 min)");
		cBoxVitesse.addItem("Normale (5 min)");
		cBoxVitesse.addItem("Lente (15 min)");
		cBoxVitesse.setSelectedIndex(0);
		frame.getContentPane().add(cBoxVitesse);
		iciCEstLeMode = new JLabel("Votre priorité pour le voyage?");
		frame.getContentPane().add(iciCEstLeMode);
		cBoxMode=new JComboBox();
		cBoxMode.addItem("Plus court chemin");
		cBoxMode.addItem("Le moins de changements");
		cBoxMode.setSelectedIndex(0);
		frame.getContentPane().add(cBoxMode);
		okButton = new JButton("Calculer l'itineraire !");
		okButton.addActionListener(this);
		frame.getContentPane().add(okButton);
		frame.pack();
		frame.setVisible(true);

	}

	public void actionPerformed(ActionEvent e) {

		if(e.getSource()!=null&&e.getSource().equals(btnChoixStations)){
			/*DialogMap dm = new DialogMap( "Saisie des stations");
			dm.setVisible(true);
			if(dm.getResult()!=null){
				source = dm.getResult()[0];
				destination = dm.getResult()[1];
				if(source!=null&&destination!=null){JOptionPane.showMessageDialog(null,"Vous souhaitez aller de " + source + " vers " + destination,"Information",JOptionPane.INFORMATION_MESSAGE);}
				else{JOptionPane.showMessageDialog(null,"Vous n'avez sélectionné aucune station","Information",JOptionPane.INFORMATION_MESSAGE);}
			}
			dm.dispose();*/
		}
		else{if(e.getSource()!=null&&e.getSource().equals(okButton)){
			if(source!=null&&destination!=null){
				try{
					int h = (int)Integer.parseInt(tBoxHeureDepart.getText());
					int m = (int)Integer.parseInt(tBoxMinuteDepart.getText());
					int[] time = new int[2];
					time[0] = h;
					time[1] = m;
					try{
						ITINERAIRE itin = tan.calculItineraire(source, destination, time, dureeChangementTransport[cBoxVitesse.getSelectedIndex()], cBoxMode.getSelectedIndex()==0);
						/*DialogMap dm = new DialogMap("Votre itineraire de " + source + " vers " + destination, itin);
						dm.setVisible(true);
						dm.dispose();*/
					} catch (StationNotFoundException ex) {
						JOptionPane.showMessageDialog(null,ex.getMessage(),"Erreur",JOptionPane.ERROR_MESSAGE);
					} catch (DepartPasDesserviException ex) {
						JOptionPane.showMessageDialog(null,ex.getMessage(),"Erreur",JOptionPane.ERROR_MESSAGE);
					} catch (ItineraireNotPossibleException ex){
						JOptionPane.showMessageDialog(null,ex.getMessage(),"Erreur",JOptionPane.ERROR_MESSAGE);
					}
				}catch(NumberFormatException nEx){
					JOptionPane.showMessageDialog(null,"Le texte entré n'est pas un nombre","Erreur",JOptionPane.ERROR_MESSAGE);
				}
			}else{JOptionPane.showMessageDialog(null,"Vous devez sélectionner une station de départ et d'arrivée à l'aide de la carte","Erreur",JOptionPane.ERROR_MESSAGE);}

		}}
	}

}