package com.hiveworkshop.rms.ui.application.scripts;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.ui.application.MainFrame;
import com.hiveworkshop.rms.ui.application.MainPanel;
import com.hiveworkshop.rms.ui.application.MenuBar;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.ImportPanel;
import com.hiveworkshop.rms.ui.preferences.SaveProfile;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

public class AnimationTransfer extends JPanel {
	JTextField baseFileInput, animFileInput, outFileInput;
	JCheckBox transferSingleAnimation, useCurrentModel;
	JComboBox<Animation> pickAnimBox, visFromBox;
	DefaultComboBoxModel<Animation> baseAnims;
	DefaultComboBoxModel<Animation> animAnims;

	JFileChooser fc = new JFileChooser();

	EditableModel sourceFile;
	EditableModel animFile;
	private final JFrame parentFrame;

	public AnimationTransfer(final JFrame parentFrame) {
		setLayout(new MigLayout("gap 0"));
		this.parentFrame = parentFrame;
		final MainPanel panel = MainFrame.getPanel();
		final EditableModel current;// ;
		if (panel != null && (current = panel.currentMDL()) != null && current.getFile() != null) {
			fc.setCurrentDirectory(current.getFile().getParentFile());
		} else if (SaveProfile.get().getPath() != null) {
			fc.setCurrentDirectory(new File(SaveProfile.get().getPath()));
		}

		JPanel filePanel = new JPanel(new MigLayout("gap 0, wrap 3", "[grow]8[align right]8[align right]"));

		baseFileInput = getFileField();
		filePanel.add(new JLabel("Base file:"));
		filePanel.add(baseFileInput);
		filePanel.add(getBrowseButton(e -> openAction(baseFileInput)));

		animFileInput = getFileField();
		filePanel.add(new JLabel("Animation file:"));
		filePanel.add(animFileInput);
		filePanel.add(getBrowseButton(e -> openAction(animFileInput)));

		outFileInput = getFileField();
		filePanel.add(new JLabel("Output file:"));
		filePanel.add(outFileInput);
		filePanel.add(getBrowseButton(e -> saveAction(outFileInput)));
		// TODO: remove save field and make "transfer" (and finished in ImportPanel) open save dialog

		add(filePanel, "growx, spanx, wrap");

		transferSingleAnimation = new JCheckBox("Transfer single animation:", false);
		transferSingleAnimation.addActionListener(e -> transferSingleAnimation());
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

		JButton transfer = new JButton("Transfer");
		transfer.setMnemonic(KeyEvent.VK_T);
		transfer.setMinimumSize(new Dimension(200, 35));
		transfer.addActionListener(e -> transfer(false));
		transferDonePanel.add(transfer);

		JButton done = new JButton("Done");
		done.setMnemonic(KeyEvent.VK_D);
		done.setMinimumSize(new Dimension(80, 35));
		done.addActionListener(e -> done());
		transferDonePanel.add(done);

		add(transferDonePanel, "spanx, align center, wrap");

		JButton goAdvanced = new JButton("Go Advanced");
		goAdvanced.setMnemonic(KeyEvent.VK_G);
		goAdvanced.addActionListener(e -> transfer(true));
		goAdvanced.setToolTipText(
				"Opens the traditional MatrixEater Import window responsible for this Simple Import, " +
						"so that you can micro-manage particular settings before finishing the operation.");

		add(goAdvanced, "spanx, align center");
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

	public void forceRefreshModels() throws IOException {
		// if( (sourceFile == null && !baseFileInput.getText().equals("")) ||
		// !baseFileInput.getText().equals(sourceFile.getFile().getPath()) ) {
		sourceFile = MdxUtils.loadEditable(new File(baseFileInput.getText()));
		// JOptionPane.showMessageDialog(null,"Reloaded base model");
		// }
		// if( (animFile == null && !animFileInput.getText().equals("")) ||
		// !animFileInput.getText().equals(animFile.getFile().getPath()) ) {
		animFile = MdxUtils.loadEditable(new File(animFileInput.getText()));
		// JOptionPane.showMessageDialog(null,"Reloaded anim model");
		// }
		updateBoxes();
	}


	public void updateBoxes() {
		if (animFile != null) {
			updateBox(animFile, pickAnimBox);
		}
		if (sourceFile != null) {
			updateBox(sourceFile, visFromBox);
		}
	}

	private void updateBox(EditableModel animFile, JComboBox<Animation> pickAnimBox) {
		final DefaultComboBoxModel<Animation> model = new DefaultComboBoxModel<>();

		for (int i = 0; i < animFile.getAnimsSize(); i++) {
			final Animation anim = animFile.getAnim(i);
			model.addElement(anim);
		}
		final ComboBoxModel<Animation> oldModel = pickAnimBox.getModel();
		boolean equalModels = oldModel.getSize() > 0;
		for (int i = 0; i < oldModel.getSize() && i < model.getSize() && equalModels; i++) {
			if (oldModel.getElementAt(i) != model.getElementAt(i)) {
				equalModels = false;
			}
		}
		if (!equalModels) {
			pickAnimBox.setModel(model);
		}
	}

	private void done() {
		parentFrame.setVisible(false);
		parentFrame.dispose();
	}

	private void transfer(boolean advancedTransfer) {
		try {
			doTransfer(advancedTransfer);
		} catch (final IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private void transferSingleAnimation() {
		updateBoxes();
		pickAnimBox.setEnabled(transferSingleAnimation.isSelected());
		visFromBox.setEnabled(transferSingleAnimation.isSelected());
	}

	private void saveAction(JTextField jTextField) {
		fc.setDialogTitle("Save");
		final int returnValue = fc.showSaveDialog(this);

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
		final int returnValue = fc.showOpenDialog(this);

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

	public void doTransfer(final boolean show) throws IOException {
		File baseFile = new File(baseFileInput.getText());
		final EditableModel receivingModel = MdxUtils.loadEditable(baseFile);
		receivingModel.setFileRef(baseFile);
		File animFile = new File(animFileInput.getText());
		final EditableModel donatingModel = MdxUtils.loadEditable(animFile);
		donatingModel.setFileRef(animFile);

		if (!transferSingleAnimation.isSelected()) {
			new Thread(() -> {
				final ImportPanel importPanel = new ImportPanel(receivingModel, donatingModel, show);
				importPanel.animTransfer(transferSingleAnimation.isSelected(), pickAnimBox.getItemAt(pickAnimBox.getSelectedIndex()), visFromBox.getItemAt(visFromBox.getSelectedIndex()), show);
				waitWhileVisible(importPanel);

				if (importPanel.importStarted()) {
					waitForPanel(importPanel);

					if (importPanel.importSuccessful()) {
						String filepath = outFileInput.getText();
						if (!filepath.toLowerCase().endsWith(".mdl") && !filepath.toLowerCase().endsWith(".mdx")) {
							filepath += ".mdl";
						}
						trySave(receivingModel, filepath);
						JOptionPane.showMessageDialog(null, "Animation transfer done!");
					}
				}

                // forceRefreshModels();
            }).start();
		} else {
			final Thread watcher = new Thread(() -> {
				final ImportPanel importPanel = new ImportPanel(receivingModel, donatingModel, show);
				importPanel.animTransfer(transferSingleAnimation.isSelected(), pickAnimBox.getItemAt(pickAnimBox.getSelectedIndex()), visFromBox.getItemAt(visFromBox.getSelectedIndex()), show);
				waitWhileVisible(importPanel);

				if (importPanel.importStarted()) {
					waitForPanel(importPanel);

					if (importPanel.importSuccessful()) {
						final ImportPanel importPanel2;
						try {
							File newFile = receivingModel.getFile();
							EditableModel newDonatingModel = MdxUtils.loadEditable(newFile);
							newDonatingModel.setFileRef(newFile);
							importPanel2 = new ImportPanel(receivingModel, newDonatingModel, show);
						} catch (final IOException e) {
							e.printStackTrace();
							return;
						}
						importPanel2.animTransferPartTwo(transferSingleAnimation.isSelected(),
								pickAnimBox.getItemAt(pickAnimBox.getSelectedIndex()),
								visFromBox.getItemAt(visFromBox.getSelectedIndex()), show);

						waitWhileVisible(importPanel2);

						if (importPanel2.importStarted()) {
							waitForPanel(importPanel2);
							if (importPanel2.importSuccessful()) {
								JOptionPane.showMessageDialog(null, "Animation transfer done!");
								trySave(receivingModel, outFileInput.getText());
							}
						}
					}
				}
			});
			watcher.start();
		}
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

	private void trySave(EditableModel sourceFile, String text) {
		try {
			File file = new File(text);
			MdxUtils.saveMdx(sourceFile, file);
			SaveProfile.get().addRecent(file.getPath());
			MenuBar.updateRecent();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void trySleep() {
		try {
			Thread.sleep(1);
		} catch (final Exception e) {
			ExceptionPopup.display("MatrixEater detected error with Java's wait function", e);
		}
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

		final JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setIconImage((new ImageIcon(MainFrame.class.getResource("ImageBin/Anim.png"))).getImage());
		final AnimationTransfer transfer = new AnimationTransfer(frame);
		frame.setContentPane(transfer);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
