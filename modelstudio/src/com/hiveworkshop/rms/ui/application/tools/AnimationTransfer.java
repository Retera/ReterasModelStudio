package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.ui.application.actionfunctions.ActionFunction;
import com.hiveworkshop.rms.ui.application.model.editors.TwiTextField;
import com.hiveworkshop.rms.ui.browsers.jworldedit.RMSFileChooser;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.ImportPanel;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.ui.preferences.SaveProfile;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.TwiComboBox;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AnimationTransfer extends JPanel {
	private String outFilePath;
	private final List<Animation> animsToTransfer = new ArrayList<>();
	private final List<Animation> orgAnims = new ArrayList<>();
	private Animation animToTransfer;
	private Animation visAnim;

	private final RMSFileChooser fc = new RMSFileChooser();
	private EditableModel receivingModel;
	private EditableModel animModel;
	private JFrame parentFrame;

	private boolean singleAnimation;

	public AnimationTransfer(String path, final JFrame parentFrame) {
		this(path);
		this.parentFrame = parentFrame;
	}

	public AnimationTransfer(String path) {
		setLayout(new MigLayout("gap 0"));
		setCurrentPath(path);

		add(getFileFieldsPanel(), "growx, spanx, wrap");

		add(getAnimTransferPanel(), "growx, spanx, wrap");

		add(getButtonPanel(), "spanx, align center, wrap");
	}

	private JPanel getButtonPanel() {
		JPanel buttonPanel = new JPanel(new MigLayout());
		buttonPanel.add(getSpecialButton("Transfer", e -> doTransfer(false, singleAnimation), new Dimension(200, 35), KeyEvent.VK_T));
		buttonPanel.add(getSpecialButton("Close", e -> done(), new Dimension(80, 35), KeyEvent.VK_D), "wrap");
//		buttonPanel.add(getSpecialButton("Save", e -> done(), new Dimension(80, 35), KeyEvent.VK_D));

		JButton goAdvanced = getSpecialButton("Go Advanced", e -> doTransfer(true, singleAnimation), null, KeyEvent.VK_G);
		goAdvanced.setToolTipText(
				"Opens the traditional MatrixEater Import window responsible for this Simple Import, " +
						"so that you can micro-manage particular settings before finishing the operation.");

		buttonPanel.add(goAdvanced, "spanx, align center");
		return buttonPanel;
	}
	private JPanel getFileFieldsPanel() {
		JPanel fileFieldsPanel = new JPanel(new MigLayout("gap 0, wrap 3", "[grow]8[align right]8[align right]"));

		fileFieldsPanel.add(new JLabel("Base file:"), "growx");
		TwiTextField modelField = new TwiTextField(24, fp -> setReceivingModel(getModel(receivingModel, fp)));
		fileFieldsPanel.add(modelField);
		fileFieldsPanel.add(getBrowseButton(e -> modelField.setTextAndRun(openAction())));


		fileFieldsPanel.add(new JLabel("Animation file:"), "growx");
		TwiTextField animField = new TwiTextField(24, fp -> setAnimModel(getModel(animModel, fp)));
		fileFieldsPanel.add(animField);
		fileFieldsPanel.add(getBrowseButton(e -> animField.setTextAndRun(openAction())));

//		fileFieldsPanel.add(new JLabel("Output file:"), "growx");
//		TwiTextField outField = new TwiTextField(24, fp -> outFilePath = fp);
//		fileFieldsPanel.add(outField);
//		fileFieldsPanel.add(getBrowseButton(e -> outField.setTextAndRun(openAction())));
//		// TODO: remove save field and make "transfer" (and finished in ImportPanel) open save dialog
		return fileFieldsPanel;
	}

	private void setReceivingModel(EditableModel model){
		orgAnims.clear();
		receivingModel = model;
		if (receivingModel != null) {
			orgAnims.addAll(receivingModel.getAnims());
		}
	}
	private void setAnimModel(EditableModel model){
		animsToTransfer.clear();
		animModel = model;
		if (animModel != null) {
			animsToTransfer.addAll(animModel.getAnims());
		}
	}

	private JPanel getAnimTransferPanel() {
		JPanel animTransferPanel = new JPanel(new MigLayout("gap 0, wrap 2", "20[]8[grow,align right]"));

		JLabel animLabel = new JLabel("Animation to transfer:");
		animLabel.setEnabled(false);
		TwiComboBox<Animation> pickAnimBox = new TwiComboBox<>(animsToTransfer, new Animation("PrototypePrototypePrototype", 0, 1));
		pickAnimBox.addOnSelectItemListener(a -> animToTransfer = a);
		pickAnimBox.setEnabled(false);

		JLabel visLabel = new JLabel("Get visibility from:");
		visLabel.setEnabled(false);
		TwiComboBox<Animation> visFromBox = new TwiComboBox<>(orgAnims, new Animation("PrototypePrototypePrototype", 0, 1));
		visFromBox.addOnSelectItemListener(a -> visAnim = a);
		visFromBox.setEnabled(false);

		JCheckBox transferSingleAnimation = new JCheckBox("Transfer single animation:", false);
		transferSingleAnimation.addActionListener(e -> {
			singleAnimation = transferSingleAnimation.isSelected();
			pickAnimBox.setEnabled(singleAnimation);
			animLabel.setEnabled(singleAnimation);
			visFromBox.setEnabled(singleAnimation);
			visLabel.setEnabled(singleAnimation);
		});
//		transferSingleAnimation.setHorizontalTextPosition(SwingConstants.LEADING);
		animTransferPanel.add(transferSingleAnimation, "spanx, wrap");

		animTransferPanel.add(animLabel);
		animTransferPanel.add(pickAnimBox, "growx");

		animTransferPanel.add(visLabel);
		animTransferPanel.add(visFromBox, "growx");
		return animTransferPanel;
	}
	private void setCurrentPath(String path) {
		fc.setCurrentDirectory(new File(path));
	}

	private JButton getSpecialButton(String text, ActionListener actionListener, Dimension minimumSize, int mnem) {
		JButton button = new JButton(text);
		button.setMnemonic(mnem);
		if(minimumSize != null){
			button.setMinimumSize(minimumSize);
		}
		button.addActionListener(actionListener);
		return button;
	}

	public static void main(final String[] args) {
		try {
			// Set cross-platform Java L&F (also called "Metal")
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (final UnsupportedLookAndFeelException
				| ClassNotFoundException
				| InstantiationException
				| IllegalAccessException e) {
			// handle exception
		}
		AnimationTransfer.showWindow();

	}

	public static void showWindow() {
		JFrame parentFrame = new JFrame("Animation Transferer");
		parentFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		AnimationTransfer contentPane = new AnimationTransfer(SaveProfile.get().getPath(), parentFrame);
		parentFrame.setContentPane(contentPane);
		parentFrame.setIconImage(RMSIcons.AnimIcon.getImage());
		parentFrame.pack();
		parentFrame.setLocationRelativeTo(null);
		parentFrame.setVisible(true);
	}

	public JButton getBrowseButton(ActionListener actionListener) {
		final Dimension dim = new Dimension(28, 18);
		JButton baseBrowse = new JButton("...");
		baseBrowse.setMaximumSize(dim);
		baseBrowse.setMinimumSize(dim);
		baseBrowse.setPreferredSize(dim);
		baseBrowse.addActionListener(actionListener);
		return baseBrowse;
	}

	private String openAction() {
		fc.setDialogTitle("Open");
		int returnValue = fc.showOpenDialog(this);

		if (returnValue == JFileChooser.APPROVE_OPTION) {
			String filepath = fc.getSelectedFile().getPath();
			if (!filepath.toLowerCase().endsWith(".mdl") && !filepath.toLowerCase().endsWith(".mdx")) {
				filepath += ".mdl";
			}
			return filepath;
		}
		return "";
	}

	private EditableModel getModel(EditableModel modelToSet, String newPath) {
		if (newPath.length() > 0
				&& (modelToSet == null
				|| modelToSet.getFile() == null
				|| !newPath.equals(modelToSet.getFile().getPath()))) {
			try {
				File file = new File(newPath);
				EditableModel model = MdxUtils.loadEditable(file);
				model.setFileRef(file);
				return model;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return modelToSet;
	}

	private void doTransfer(boolean show, boolean transferSingleAnim) {
		EditableModel receivingModel = this.receivingModel;
		EditableModel donatingModel = animModel;

		if (receivingModel != null) {
			if (!transferSingleAnim) {
				new Thread(() -> doImportIngStuff(show, false, receivingModel, donatingModel, () -> saveModel(receivingModel))).start();
			} else {
				new Thread(() -> doImportIngStuff(show, true, receivingModel, donatingModel, () -> doImportThings2(show, receivingModel))).start();
			}
		}
	}

	private void doImportIngStuff(boolean show, boolean transferSingleAnim, EditableModel receivingModel, EditableModel donatingModel, Runnable runnable) {
		ImportPanel importPanel = new ImportPanel(receivingModel, donatingModel, show);
		importPanel.animTransfer(transferSingleAnim, animToTransfer, visAnim, show);
		waitWhileVisible(importPanel);

		if (importPanel.importStarted()) {
			waitForPanel(importPanel);

			if (importPanel.importSuccessful()) {
				runnable.run();
			}
		}
	}


	private EditableModel getEditableModel(String filePath) {
		try {
			File modelFile = new File(filePath);
			EditableModel model = MdxUtils.loadEditable(modelFile);
			model.setFileRef(modelFile);
			return model;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private void doImportThings2(boolean show, EditableModel receivingModel) {
		EditableModel newDonatingModel = getEditableModel(receivingModel.getFile().getPath());
		ImportPanel importPanel2 = new ImportPanel(receivingModel, newDonatingModel, show);

		importPanel2.animTransferPartTwo(animToTransfer, visAnim, show);
		saveModel(receivingModel);
	}

	private void saveModel(EditableModel model) {
		if(saveAction(model)) {
			JOptionPane.showMessageDialog(null, "Animation transfer done!");
		}
	}

	private boolean saveAction(EditableModel model) {
		fc.setDialogTitle("Save");
		fc.setSelectedFile(model.getFile());
		int returnValue = fc.showSaveDialog(this);

		if (returnValue == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			if (file != null) {
				String filepath = file.getPath();
				String ext = filepath.substring(filepath.lastIndexOf('.') + 1);

				try {
					if (ext.equals("mdl")) {
						MdxUtils.saveMdl(model, file);
					} else {
						MdxUtils.saveMdx(model, file);
					}
					model.setFileRef(file);
					SaveProfile.get().addRecent(file.getPath());
					return true;
				} catch (final Exception exc) {
					exc.printStackTrace();
					ExceptionPopup.display(exc);
				}
			}
		}
		return false;
	}

	private void waitForPanel(ImportPanel importPanel) {
		while (!importPanel.importEnded()) {
			trySleep();
		}
	}

	private void waitWhileVisible(ImportPanel importPanel) {
		while (importPanel.getParentFrame().isVisible()
				&& (!importPanel.importStarted()
				|| importPanel.importEnded())) {
			trySleep();
		}
	}

	private void trySleep() {
		try {
			Thread.sleep(1);
		} catch (final Exception e) {
			ExceptionPopup.display("MatrixEater detected error with Java's wait function", e);
		}
	}

	private void done() {
		parentFrame.setVisible(false);
		parentFrame.dispose();
	}

	private static class AnimTransfer extends ActionFunction {
		AnimTransfer(){
			super(TextKey.IMPORT_ANIM, AnimationTransfer::showWindow);
			setMenuItemMnemonic(KeyEvent.VK_I);
		}
	}

	public static JMenuItem getMenuItem(){
		return new AnimTransfer().getMenuItem();
	}
}
