package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.actionfunctions.ActionFunction;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.ImportPanel;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.ui.preferences.SaveProfile;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class AnimationTransfer extends JPanel {
	JTextField baseFileInput;
	JTextField animFileInput;
	JTextField outFileInput;
	JComboBox<Animation> pickAnimBox;
	JComboBox<Animation> visFromBox;

	JFileChooser fc = new JFileChooser();

	EditableModel sourceFile;
	EditableModel animFile;
	private JFrame parentFrame;

	public AnimationTransfer(final JFrame parentFrame) {
		this();
		this.parentFrame = parentFrame;
	}

	public AnimationTransfer() {
		setLayout(new MigLayout("gap 0"));
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null && modelPanel.getModel() != null && modelPanel.getModel().getFile() != null) {
			fc.setCurrentDirectory(modelPanel.getModel().getFile().getParentFile());
		} else if (SaveProfile.get().getPath() != null) {
			fc.setCurrentDirectory(new File(SaveProfile.get().getPath()));
		}

//		JPanel filePanel = new JPanel(new MigLayout("gap 0, wrap 3", "[grow]8[align right]8[align right]"));
		JPanel filePanel = new JPanel(new MigLayout("gap 0, wrap 3", "[grow]"));

		baseFileInput = getFileField();
		filePanel.add(getFieldPanel("Base file:", e -> openAction(baseFileInput), baseFileInput), "wrap, growx");

		animFileInput = getFileField();
		filePanel.add(getFieldPanel("Animation file:", e -> openAction(animFileInput), animFileInput), "wrap, growx");

		outFileInput = getFileField();
		filePanel.add(getFieldPanel("Output file:", e -> saveAction(outFileInput), outFileInput), "wrap, growx");
		// TODO: remove save field and make "transfer" (and finished in ImportPanel) open save dialog

		add(filePanel, "growx, spanx, wrap");

		JCheckBox transferSingleAnimation = new JCheckBox("Transfer single animation:", false);
		transferSingleAnimation.addActionListener(e -> transferSingleAnimation(transferSingleAnimation.isSelected()));
		transferSingleAnimation.setHorizontalTextPosition(SwingConstants.LEADING);
		add(transferSingleAnimation, "spanx, wrap");
//		transSingleLabel = new JLabel("Transfer single animation:");

		JPanel animTransferPanel = new JPanel(new MigLayout("gap 0, wrap 2", "20[]8[grow,align right]"));

		animTransferPanel.add(new JLabel("Animation to transfer:"));
		pickAnimBox = new JComboBox<>();
		pickAnimBox.setEnabled(false);
		animTransferPanel.add(pickAnimBox, "growx");

		animTransferPanel.add(new JLabel("Get visibility from:"));
		visFromBox = new JComboBox<>();
		visFromBox.setEnabled(false);
		animTransferPanel.add(visFromBox, "growx");

		add(animTransferPanel, "growx, spanx, wrap");

		JPanel transferDonePanel = new JPanel(new MigLayout());
		transferDonePanel.add(getSpecialButton("Transfer", e -> transfer(false, transferSingleAnimation.isSelected()), new Dimension(200, 35), KeyEvent.VK_T));
		transferDonePanel.add(getSpecialButton("Done", e -> done(), new Dimension(80, 35), KeyEvent.VK_D));
//		transferDonePanel.add(getSpecialButton("Save", e -> done(), new Dimension(80, 35), KeyEvent.VK_D));

		add(transferDonePanel, "spanx, align center, wrap");

		JButton goAdvanced = getSpecialButton("Go Advanced", e -> transfer(true, transferSingleAnimation.isSelected()), null, KeyEvent.VK_G);
		goAdvanced.setToolTipText(
				"Opens the traditional MatrixEater Import window responsible for this Simple Import, " +
						"so that you can micro-manage particular settings before finishing the operation.");

		add(goAdvanced, "spanx, align center");
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

	private JPanel getFieldPanel(String text, ActionListener actionListener, JTextField textField) {
		JPanel panel = new JPanel(new MigLayout("fill, gap 0, ins 0, wrap 3", "[grow]8[align right]8[align right]"));
		panel.add(new JLabel(text), "growx");
		panel.add(textField);
		panel.add(getBrowseButton(actionListener));
		return panel;
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
		new AnimationTransfer().showWindow();
	}

	public void showWindow() {
		parentFrame = new JFrame("Animation Transferer");
		parentFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		parentFrame.setContentPane(new AnimationTransfer(parentFrame));
		parentFrame.setIconImage(RMSIcons.AnimIcon.getImage());
		parentFrame.pack();
		parentFrame.setLocationRelativeTo(null);
		parentFrame.setVisible(true);
	}

	public JTextField getFileField() {
		JTextField baseFileInput = new JTextField("");
		baseFileInput.setMinimumSize(new Dimension(200, 18));
		return baseFileInput;
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

	public void refreshModels() throws IOException {
		if (baseFileInput.getText().length() > 0) {
			if (sourceFile == null
					|| sourceFile.getFile() == null
					|| !baseFileInput.getText().equals(sourceFile.getFile().getPath())) {
				sourceFile = MdxUtils.loadEditable(new File(baseFileInput.getText()));
			}
		}
		if (animFileInput.getText().length() > 0) {
			if (animFile == null
					|| animFile.getFile() == null
					|| !animFileInput.getText().equals(animFile.getFile().getPath())) {
				animFile = MdxUtils.loadEditable(new File(animFileInput.getText()));
			}
		}
	}


	public void updateBoxes() {
		if (animFile != null) {
			pickAnimBox.setModel(getAnimBoxModel(animFile.getAnims()));
		}
		if (sourceFile != null) {
			visFromBox.setModel(getAnimBoxModel(sourceFile.getAnims()));
		}
	}

	private DefaultComboBoxModel<Animation> getAnimBoxModel(List<Animation> anims) {
		DefaultComboBoxModel<Animation> model = new DefaultComboBoxModel<>();

		for (Animation animation : anims){
			model.addElement(animation);
		}
		return model;
	}

	private void transfer(boolean advancedTransfer, boolean transferSingleAnim) {
		try {
			doTransfer(advancedTransfer, transferSingleAnim);
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void transferSingleAnimation(boolean transferSingleAnim) {
		updateBoxes();
		pickAnimBox.setEnabled(transferSingleAnim);
		visFromBox.setEnabled(transferSingleAnim);
	}

	private void saveAction(JTextField jTextField) {
		fc.setDialogTitle("Save");
		int returnValue = fc.showSaveDialog(this);

		if (returnValue == JFileChooser.APPROVE_OPTION) {
			String filepath = fc.getSelectedFile().getPath();
			if (!filepath.toLowerCase().endsWith(".mdl") && !filepath.toLowerCase().endsWith(".mdx")) {
				filepath += ".mdl";
			}
			jTextField.setText(filepath);
		}
	}

	private void openAction(JTextField jTextField) {
		fc.setDialogTitle("Open");
		int returnValue = fc.showOpenDialog(this);

		if (returnValue == JFileChooser.APPROVE_OPTION) {
			String filepath = fc.getSelectedFile().getPath();
			if (!filepath.toLowerCase().endsWith(".mdl") && !filepath.toLowerCase().endsWith(".mdx")) {
				filepath += ".mdl";
			}
			jTextField.setText(filepath);
			try {
				refreshModels();
			} catch (final IOException e1) {
				e1.printStackTrace();
			}
			updateBoxes();
		}
	}

	public void doTransfer(boolean show, boolean transferSingleAnim) throws IOException {
		EditableModel receivingModel = getEditableModel(baseFileInput.getText());
		EditableModel donatingModel = getEditableModel(animFileInput.getText());

		if(receivingModel != null) {
			if (!transferSingleAnim) {
				new Thread(() -> doImportIngStuff(show, false, receivingModel, donatingModel, () -> saveModel(receivingModel))).start();
			} else {
				new Thread(() -> doImportIngStuff(show, true, receivingModel, donatingModel, () -> doImportThings2(show, receivingModel))).start();
			}
		}
	}

	private void doImportIngStuff(boolean show, boolean transferSingleAnim, EditableModel receivingModel, EditableModel donatingModel, Runnable runnable) {
		ImportPanel importPanel = new ImportPanel(receivingModel, donatingModel, show);
		Animation animation = pickAnimBox.getItemAt(pickAnimBox.getSelectedIndex());
		Animation visibility = visFromBox.getItemAt(visFromBox.getSelectedIndex());
		importPanel.animTransfer(transferSingleAnim, animation, visibility, show);
		waitWhileVisible(importPanel);

		if (importPanel.importStarted()) {
			waitForPanel(importPanel);

			if (importPanel.importSuccessful()) {
				runnable.run();
			}
		}
	}
	private void doImportIngStuff(boolean show, EditableModel receivingModel, EditableModel donatingModel) {
		ImportPanel importPanel = new ImportPanel(receivingModel, donatingModel, show);
		Animation animation = pickAnimBox.getItemAt(pickAnimBox.getSelectedIndex());
		Animation visibility = visFromBox.getItemAt(visFromBox.getSelectedIndex());
		importPanel.animTransfer1(true, animation, visibility, show);
		waitWhileVisible(importPanel);

		if (importPanel.importStarted()) {
			waitForPanel(importPanel);

			if (importPanel.importSuccessful()) {
				doImportThings2(show, receivingModel);
			}
		}
	}
	private void doImportIngStuff2(boolean show, EditableModel receivingModel, EditableModel donatingModel) {
		ImportPanel importPanel = new ImportPanel(receivingModel, donatingModel, show);
		Animation animation = pickAnimBox.getItemAt(pickAnimBox.getSelectedIndex());
		Animation visibility = visFromBox.getItemAt(visFromBox.getSelectedIndex());
		importPanel.animTransfer1(true, animation, visibility, show);
		waitWhileVisible(importPanel);

		if (importPanel.importStarted()) {
			waitForPanel(importPanel);

			if (importPanel.importSuccessful()) {
				EditableModel newDonatingModel = getEditableModel(receivingModel.getFile().getPath());
				ImportPanel importPanel2 = new ImportPanel(receivingModel, newDonatingModel, show);
				importPanel2.animTransferPartTwo(animation, visibility, show);

				waitWhileVisible(importPanel2);

				if (importPanel2.importStarted()) {
					waitForPanel(importPanel2);
					if (importPanel2.importSuccessful()) {
						saveModel(receivingModel);
					}
				}
			}
		}
	}

	private EditableModel getEditableModel(String filePath) {
		try {
			File modelFile = new File(filePath);
			EditableModel model = MdxUtils.loadEditable(modelFile);
			model.setFileRef(modelFile);
			return model;
		} catch (IOException e){
			e.printStackTrace();
			return null;
		}
	}

	private void doImportThings2(boolean show, EditableModel receivingModel) {
		EditableModel newDonatingModel = getEditableModel(receivingModel.getFile().getPath());
		ImportPanel importPanel2 = new ImportPanel(receivingModel, newDonatingModel, show);

		Animation animation = pickAnimBox.getItemAt(pickAnimBox.getSelectedIndex());
		Animation visibility = visFromBox.getItemAt(visFromBox.getSelectedIndex());
		importPanel2.animTransferPartTwo(animation, visibility, show);

		waitWhileVisible(importPanel2);

		if (importPanel2.importStarted()) {
			waitForPanel(importPanel2);
			if (importPanel2.importSuccessful()) {
				saveModel(receivingModel);
			}
		}
	}

	private void saveModel(EditableModel model) {
//		trySave(model, outFileInput.getText());
		FileDialog fileDialog = new FileDialog();
		fileDialog.onClickSaveAs(model, FileDialog.SAVE_MODEL, false);
		JOptionPane.showMessageDialog(null, "Animation transfer done!");
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
			super(TextKey.IMPORT_ANIM, () -> new AnimationTransfer().showWindow());
			setMenuItemMnemonic(KeyEvent.VK_I);
		}
	}

	public static JMenuItem getMenuItem(){
		return new AnimTransfer().getMenuItem();
	}
}
