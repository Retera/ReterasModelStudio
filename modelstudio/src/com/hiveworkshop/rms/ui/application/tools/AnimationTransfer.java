package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.actionfunctions.ActionFunction;
import com.hiveworkshop.rms.ui.application.model.editors.TwiTextField;
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
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AnimationTransfer extends JPanel {
	//	private final JTextField baseFileField;
//	private final JTextField animFileField;
//  private final JTextField outFileField;
//	private final TwiTextField baseFileField;
//	private final TwiTextField animFileField;
	private final TwiTextField outFileField;
	//	private String baseFilePath;
//	private String animFilePath;
	private String outFilePath;
	private final JComboBox<Animation> pickAnimBox;
	private final JComboBox<Animation> visFromBox;

	private final JFileChooser fc = new JFileChooser();
	private EditableModel sourceModel;
	private EditableModel animModel;
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

		JPanel fileFieldsPanel = new JPanel(new MigLayout("gap 0, wrap 3", "[grow]"));

		fileFieldsPanel.add(getFieldPanel("Base file:", m -> sourceModel = m, () -> sourceModel), "wrap, growx");
		fileFieldsPanel.add(getFieldPanel("Animation file:", m -> animModel = m, () -> animModel), "wrap, growx");

		outFileField = getFileField(s -> outFilePath = s);
		fileFieldsPanel.add(getFieldPanel("Output file:", e -> saveAction(outFileField), outFileField), "wrap, growx");
		// TODO: remove save field and make "transfer" (and finished in ImportPanel) open save dialog

		add(fileFieldsPanel, "growx, spanx, wrap");

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
		transferDonePanel.add(getSpecialButton("Transfer", e -> doTransfer(false, transferSingleAnimation.isSelected()), new Dimension(200, 35), KeyEvent.VK_T));
		transferDonePanel.add(getSpecialButton("Done", e -> done(), new Dimension(80, 35), KeyEvent.VK_D));
//		transferDonePanel.add(getSpecialButton("Save", e -> done(), new Dimension(80, 35), KeyEvent.VK_D));

		add(transferDonePanel, "spanx, align center, wrap");

		JButton goAdvanced = getSpecialButton("Go Advanced", e -> doTransfer(true, transferSingleAnimation.isSelected()), null, KeyEvent.VK_G);
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
		Consumer<String> setFieldText = textField::setText;
		panel.add(getBrowseButton(actionListener));
		return panel;
	}

	private JPanel getFieldPanel(String text, Consumer<EditableModel> modelConsumer, Supplier<EditableModel> model) {
		JPanel panel = new JPanel(new MigLayout("fill, gap 0, ins 0, wrap 3", "[grow]8[align right]8[align right]"));
		panel.add(new JLabel(text), "growx");
		TwiTextField textField = new TwiTextField(24, fp -> updateModel(null, modelConsumer, model.get(), fp));
		panel.add(textField);
		Consumer<String> filePathConsumer = fp -> updateModel(textField, modelConsumer, model.get(), fp);
		panel.add(getBrowseButton(e -> filePathConsumer.accept(openAction())));
		return panel;
	}

	private void updateModel(TwiTextField textField, Consumer<EditableModel> modelConsumer, EditableModel model, String filePath) {
		if (textField != null) {
			textField.setText(filePath);
		}
		modelConsumer.accept(getModel(model, filePath));
		updateBoxes();
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

	public TwiTextField getFileField(Consumer<String> stringConsumer) {
		TwiTextField baseFileInput = new TwiTextField(24, stringConsumer);
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

	private DefaultComboBoxModel<Animation> getAnimBoxModel(List<Animation> anims) {
		DefaultComboBoxModel<Animation> model = new DefaultComboBoxModel<>();

		for (Animation animation : anims){
			model.addElement(animation);
		}
		return model;
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

	private void updateBoxes() {
		if (animModel != null) {
			pickAnimBox.setModel(getAnimBoxModel(animModel.getAnims()));
		}
		if (sourceModel != null) {
			visFromBox.setModel(getAnimBoxModel(sourceModel.getAnims()));
		}
	}

	private EditableModel getModel(EditableModel modelToSet, String newPath) {
		if (newPath.length() > 0
				&& (modelToSet == null
				|| modelToSet.getFile() == null
				|| !newPath.equals(modelToSet.getFile().getPath()))) {
			try {
				return MdxUtils.loadEditable(new File(newPath));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return modelToSet;
	}

	private void doTransfer(boolean show, boolean transferSingleAnim) {
		EditableModel receivingModel = sourceModel;
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

		Animation animation = pickAnimBox.getItemAt(pickAnimBox.getSelectedIndex());
		Animation visibility = visFromBox.getItemAt(visFromBox.getSelectedIndex());
		importPanel2.animTransferPartTwo(animation, visibility, show);

//		waitWhileVisible(importPanel2);

//		if (importPanel2.importStarted()) {
//			waitForPanel(importPanel2);
////			if (importPanel2.importSuccessful()) {
//////				saveModel(receivingModel);
////			}
//		}
	}
//	private void doImportThings2(boolean show, EditableModel receivingModel) {
//		EditableModel newDonatingModel = getEditableModel(receivingModel.getFile().getPath());
//		ImportPanel importPanel2 = new ImportPanel(receivingModel, newDonatingModel, show);
//
//		Animation animation = pickAnimBox.getItemAt(pickAnimBox.getSelectedIndex());
//		Animation visibility = visFromBox.getItemAt(visFromBox.getSelectedIndex());
//		importPanel2.animTransferPartTwo(animation, visibility, show);
//
//		waitWhileVisible(importPanel2);
//
//		if (importPanel2.importStarted()) {
//			waitForPanel(importPanel2);
//			if (importPanel2.importSuccessful()) {
//				saveModel(receivingModel);
//			}
//		}
//	}

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
