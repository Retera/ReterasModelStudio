package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.ModelLoader;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Consumer;

public class ImportPanelGui extends JTabbedPane {
	public static final ImageIcon animIcon = RMSIcons.animIcon;// new ImageIcon(ImportPanel.class.getClassLoader().getResource("ImageBin/anim_small.png"));
	public static final ImageIcon boneIcon = RMSIcons.boneIcon;// new ImageIcon(ImportPanel.class.getClassLoader().getResource("ImageBin/Bone_small.png"));
	public static final ImageIcon geoIcon = RMSIcons.geoIcon;// new ImageIcon(ImportPanel.class.getClassLoader().getResource("ImageBin/geo_small.png"));
	public static final ImageIcon objIcon = RMSIcons.objIcon;// new ImageIcon(ImportPanel.class.getClassLoader().getResource("ImageBin/Obj_small.png"));
	public static final ImageIcon greenIcon = RMSIcons.greenIcon;// new ImageIcon(ImportPanel.class.getClassLoader().getResource("ImageBin/Blank_small.png"));
	public static final ImageIcon redIcon = RMSIcons.redIcon;// new ImageIcon(ImportPanel.class.getClassLoader().getResource("ImageBin/BlankRed_small.png"));
	public static final ImageIcon orangeIcon = RMSIcons.orangeIcon;// new ImageIcon(ImportPanel.class.getClassLoader().getResource("ImageBin/BlankOrange_small.png"));
	public static final ImageIcon cyanIcon = RMSIcons.cyanIcon;// new ImageIcon(ImportPanel.class.getClassLoader().getResource("ImageBin/BlankCyan_small.png"));
	public static final ImageIcon redXIcon = RMSIcons.redXIcon;// new ImageIcon(ImportPanel.class.getClassLoader().getResource("ImageBin/redX.png"));
	public static final ImageIcon greenArrowIcon = RMSIcons.greenArrowIcon;// new ImageIcon(ImportPanel.class.getClassLoader().getResource("ImageBin/greenArrow.png"));
	public static final ImageIcon moveUpIcon = RMSIcons.moveUpIcon;// new ImageIcon(ImportPanel.class.getClassLoader().getResource("ImageBin/moveUp.png"));
	public static final ImageIcon moveDownIcon = RMSIcons.moveDownIcon;// new ImageIcon(ImportPanel.class.getClassLoader().getResource("ImageBin/moveDown.png"));

	private JFrame frame;
	private final ModelHolderThing mht;

	private final ImportPanelNoGui2 importPanel2;
	Consumer<EditableModel> modelConsumer;

	public ImportPanelGui(final EditableModel receivingModel, final EditableModel donatingModel, Consumer<EditableModel> modelConsumer) {
		this(new ModelHolderThing(receivingModel, donatingModel), modelConsumer);
	}

	public ImportPanelGui(ModelHolderThing mht, Consumer<EditableModel> modelConsumer) {
		this.mht = mht;
		this.modelConsumer = modelConsumer;
		importPanel2 = new ImportPanelNoGui2(mht);
		makeTabs();

		String receivingModelName = mht.receivingModel.getName();
		String donatingModelName = mht.donatingModel.getName();

		if (receivingModelName.equals(donatingModelName)) {
			frame = getFrame(receivingModelName, "itself");
		} else {
			frame = getFrame(donatingModelName, receivingModelName);
		}
		frame.setVisible(true);
	}


	private JFrame getFrame(String name1, String name2) {
		JFrame frame = new JFrame("Importing " + name1 + " into " + name2);
		try {
			frame.setIconImage(RMSIcons.MDLIcon.getImage());
		} catch (final Exception e) {
			JOptionPane.showMessageDialog(null, "Error: Image files were not found! Due to bad programming, this might break the program!");
		}

		JPanel containerPanel = new JPanel(new MigLayout("gap 0, fill", "[grow]", "[grow][]"));
		containerPanel.add(this, "growx, growy, wrap");
		containerPanel.add(getFooterPanel(frame));
		frame.setContentPane(containerPanel);

		frame.setBounds(0, 0, 1024, 780);
		frame.setLocationRelativeTo(null);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				cancelImport(frame);
			}
		});
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		return frame;
	}


	private void makeTabs() {
		// Geoset Panel
		GeosetEditPanel geosetEditPanel = new GeosetEditPanel(mht);
		addTab("Geosets", geoIcon, geosetEditPanel, "Controls which geosets will be imported.");

		// Animation Panel
		AnimEditPanel animEditPanel = new AnimEditPanel(mht);
		addTab("Animation", animIcon, animEditPanel, "Controls which animations will be imported.");

		// Bone Panel
		BoneEditPanel boneEditPanel = new BoneEditPanel(mht);
		addTab("Bones", boneIcon, boneEditPanel, "Controls which bones will be imported.");

		// Matrices Panel // Build the geosetAnimTabs list of GeosetPanels
		BoneAttachmentEditPanel boneAttachmentEditPanel = new BoneAttachmentEditPanel(mht);
		addTab("Matrices", greenIcon, boneAttachmentEditPanel, "Controls which bones geosets are attached to.");

		// Objects Panel
		ObjectEditPanel objectEditPanel = new ObjectEditPanel(mht);
		addTab("Objects", objIcon, objectEditPanel, "Controls which objects are imported.");

		// Objects Panel
		CameraEditPanel cameraEditPanel = new CameraEditPanel(mht);
		addTab("Cameras", objIcon, cameraEditPanel, "Controls which cameras are imported.");

		// Visibility Panel
		VisibilityEditPanel visibilityEditPanel = new VisibilityEditPanel(mht);
		addTab("Visibility", orangeIcon, visibilityEditPanel, "Controls the visibility of portions of the model.");
	}


	private JPanel getFooterPanel(JFrame frame) {
		JPanel footerPanel = new JPanel(new MigLayout("gap 0", "[grow, left]8[grow, right]"));
		JButton okayButton = new JButton("Finish");
		okayButton.addActionListener(e -> applyImport(frame));
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(e -> cancelImport(frame));

		footerPanel.add(okayButton);
		footerPanel.add(cancelButton);

		return footerPanel;
	}


	private void applyImport(JFrame frame) {
		EditableModel editableModel = importPanel2.doImport();
		frame.setVisible(false);
		if(modelConsumer != null){
			modelConsumer.accept(editableModel);
		} else {
			ModelPanel modelPanel = new ModelPanel(new ModelHandler(editableModel));
			ModelLoader.loadModel(true, true, modelPanel);
		}
		frame.dispose();
	}

	private void cancelImport(JFrame frame) {
		final Object[] options = {"Yes", "No"};
		final int n = JOptionPane.showOptionDialog(frame, "Really cancel this import?", "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
		if (n == 0) {
			frame.setVisible(false);
			frame.dispose();
			frame = null;
		}
	}
}
