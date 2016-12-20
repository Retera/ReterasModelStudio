import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.hiveworkshop.wc3.mdl.MDL;


public class MainPanel extends JPanel {
	//-------- UI COMPONENTS------------
	JLabel nameLabel = new JLabel("Name:");
	JTextField nameInput = new JTextField();
	JLabel baseMeshLabel = new JLabel("Base Mesh");
	JLabel animationLabel = new JLabel("Animation");
	JLabel mountsLabel = new JLabel("Mount");
	JLabel extrasLabel = new JLabel("Extra");
	JButton saveFile = new JButton("Save out a file!");
	JButton gearUp = new JButton("Gear Up");
	JFileChooser chooser = new JFileChooser();
	
	JCheckBox heroGlow, dissipate, decay;
	
	//--------- other field variables------------------
	Font bigBoldFont = new Font("Arial",Font.BOLD, 14);

	JList<MDL> listOfBaseMeshes = new JList<MDL>();
	DefaultListModel<MDL> baseMeshes = new DefaultListModel<MDL>();
	public void loadBaseMeshes() {
		File charactersFolder = new File("mesh/characters");
		for( File characterFile: charactersFolder.listFiles() ) {
			baseMeshes.addElement(MDL.read(characterFile));
		}
	}

	JList<MDL> listOfAnimMeshes = new JList<MDL>();
	DefaultListModel<MDL> animationMeshes = new DefaultListModel<MDL>();
	public void loadAnimationMeshes() {
		File animsFolder = new File("mesh/animations");
		for( File animsFile: animsFolder.listFiles() ) {
			animationMeshes.addElement(MDL.read(animsFile));
		}
	}

	JList<MDL> listOfMountMeshes = new JList<MDL>();
	DefaultListModel<MDL> mountMeshes = new DefaultListModel<MDL>();
	public void loadMountMeshes() {
		File animsFolder = new File("mesh/mounts");
		for( File animsFile: animsFolder.listFiles() ) {
			mountMeshes.addElement(MDL.read(animsFile));
		}
	}
	
	public MainPanel() {
		nameLabel.setFont(bigBoldFont);
		add(nameLabel);
		nameInput.setMinimumSize(new Dimension(400,10));
		nameInput.setPreferredSize(new Dimension(400,20));
		add(nameInput);

		// Setup list of mounts
		JPanel mounts = new JPanel();
		mounts.setBorder(BorderFactory.createLineBorder(Color.black));
		mounts.setLayout(new BoxLayout(mounts, BoxLayout.Y_AXIS));
		
		mountsLabel.setFont(bigBoldFont);
		mounts.add(mountsLabel);

		loadMountMeshes();
		listOfMountMeshes.setModel(mountMeshes);
		listOfMountMeshes.setCellRenderer(new MDLListCellRenderer());
		mounts.add(listOfMountMeshes);
		add(mounts);
		
		// turn off mounts due to not-programmed
		listOfMountMeshes.setEnabled(false);
		
		// Setup list of base meshes
		JPanel bases = new JPanel();
		bases.setBorder(BorderFactory.createLineBorder(Color.black));
		bases.setLayout(new BoxLayout(bases, BoxLayout.Y_AXIS));
		
		baseMeshLabel.setFont(bigBoldFont);
		bases.add(baseMeshLabel);

		loadBaseMeshes();
		listOfBaseMeshes.setModel(baseMeshes);
		listOfBaseMeshes.setCellRenderer(new MDLListCellRenderer());
		bases.add(listOfBaseMeshes);
		add(bases);

		// Setup list of animations
		JPanel anims = new JPanel();
		anims.setBorder(BorderFactory.createLineBorder(Color.black));
		anims.setLayout(new BoxLayout(anims, BoxLayout.Y_AXIS));
		
		animationLabel.setFont(bigBoldFont);
		anims.add(animationLabel);

		loadAnimationMeshes();
		listOfAnimMeshes.setModel(animationMeshes);
		listOfAnimMeshes.setCellRenderer(new MDLListCellRenderer());
		anims.add(listOfAnimMeshes);
		add(anims);
		
		// Setup the extras
		JPanel extras = new JPanel();
		extras.setBorder(BorderFactory.createLineBorder(Color.black));
		extras.setLayout(new BoxLayout(extras, BoxLayout.Y_AXIS));
		extras.add(extrasLabel);
		extras.add(heroGlow = new JCheckBox("Hero Glow", false));
		extras.add(dissipate = new JCheckBox("Dissipate", false));
		dissipate.setEnabled(false);
		extras.add(decay = new JCheckBox("Decay", false));
		decay.setEnabled(false);

		add(extras);
		
		setPreferredSize(new Dimension(500,200));
		
		//make file save btn

		
		saveFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				chooser.setSelectedFile(new File(nameInput.getText()+".mdl"));
				int x = chooser.showSaveDialog(MainPanel.this);
				if( x == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile() != null ) {
					makeModel().printTo(chooser.getSelectedFile());
				}
			}
		});
		add(saveFile);
	}
	
	public MDL makeModel() {
		MDL base = listOfBaseMeshes.getSelectedValue();
		MDL animationModel = listOfAnimMeshes.getSelectedValue();
		
		base = MDL.deepClone(base, nameInput.getText());
		// get rid of base model animations!
		base.deleteAllAnimation(true);
		base.addAnimationsFrom(animationModel);
		
		if( heroGlow.isSelected() ) {
			MDL heroGlowModel = MDL.read(new File("mesh/extras/heroglow.mdl"));
			base.add(heroGlowModel.getGeoset(0));
			base.add(heroGlowModel.getIdObject(0));
		}
		
		return base;
	}
}
