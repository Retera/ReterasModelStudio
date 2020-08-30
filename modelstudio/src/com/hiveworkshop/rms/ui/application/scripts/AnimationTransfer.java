package com.hiveworkshop.rms.ui.application.scripts;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.ui.application.MainFrame;
import com.hiveworkshop.rms.ui.application.MainPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ImportPanel;
import com.hiveworkshop.rms.ui.preferences.SaveProfile;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

public class AnimationTransfer extends JPanel implements ActionListener {
	JLabel baseFileLabel, animFileLabel, outFileLabel, transSingleLabel, pickAnimLabel, visFromLabel;
	JTextField baseFileInput, animFileInput, outFileInput;
	JCheckBox transferSingleAnimation, useCurrentModel;
	JButton baseBrowse, animBrowse, outBrowse, transfer, done, goAdvanced;
	JComboBox<Animation> pickAnimBox, visFromBox;
	DefaultComboBoxModel<Animation> baseAnims;
	DefaultComboBoxModel<Animation> animAnims;

	JFileChooser fc = new JFileChooser();

	EditableModel sourceFile;
	EditableModel animFile;
	private final JFrame parentFrame;

	public AnimationTransfer(final JFrame parentFrame) {
		this.parentFrame = parentFrame;
		final MainPanel panel = MainFrame.getPanel();
		final EditableModel current;// ;
		if (panel != null && (current = panel.currentMDL()) != null && current.getFile() != null) {
			fc.setCurrentDirectory(current.getFile().getParentFile());
		} else if (SaveProfile.get().getPath() != null) {
			fc.setCurrentDirectory(new File(SaveProfile.get().getPath()));
		}

		baseFileLabel = new JLabel("Base file:");
		baseFileInput = new JTextField("");
		baseFileInput.setMinimumSize(new Dimension(200, 18));
		baseBrowse = new JButton("...");
		final Dimension dim = new Dimension(28, 18);
		baseBrowse.setMaximumSize(dim);
		baseBrowse.setMinimumSize(dim);
		baseBrowse.setPreferredSize(dim);
		baseBrowse.addActionListener(this);

		animFileLabel = new JLabel("Animation file:");
		animFileInput = new JTextField("");
		animFileInput.setMinimumSize(new Dimension(200, 18));
		animBrowse = new JButton("...");
		animBrowse.setMaximumSize(dim);
		animBrowse.setMinimumSize(dim);
		animBrowse.setPreferredSize(dim);
		animBrowse.addActionListener(this);

		outFileLabel = new JLabel("Output file:");
		outFileInput = new JTextField("");
		outFileInput.setMinimumSize(new Dimension(200, 18));
		outBrowse = new JButton("...");
		outBrowse.setMaximumSize(dim);
		outBrowse.setMinimumSize(dim);
		outBrowse.setPreferredSize(dim);
		outBrowse.addActionListener(this);

		transferSingleAnimation = new JCheckBox("", false);
		transferSingleAnimation.addActionListener(this);
		transSingleLabel = new JLabel("Transfer single animation:");

		pickAnimLabel = new JLabel("Animation to transfer:");
		pickAnimBox = new JComboBox<>();
		pickAnimBox.setEnabled(false);

		visFromLabel = new JLabel("Get visibility from:");
		visFromBox = new JComboBox<>();
		visFromBox.setEnabled(false);

		transfer = new JButton("Transfer");
		transfer.setMnemonic(KeyEvent.VK_T);
		transfer.setMinimumSize(new Dimension(200, 35));
		transfer.addActionListener(this);

		done = new JButton("Done");
		done.setMnemonic(KeyEvent.VK_D);
		done.setMinimumSize(new Dimension(80, 35));
		done.addActionListener(this);

		goAdvanced = new JButton("Go Advanced");
		goAdvanced.setMnemonic(KeyEvent.VK_G);
		goAdvanced.addActionListener(this);
		goAdvanced.setToolTipText(
				"Opens the traditional MatrixEater Import window responsible for this Simple Import, so that you can micro-manage particular settings before finishing the operation.");

		final GroupLayout layout = new GroupLayout(this);
		layout.setHorizontalGroup(layout.createSequentialGroup().addGap(12).addGroup(layout
				.createParallelGroup(
						GroupLayout.Alignment.CENTER)
				.addGroup(
						layout.createParallelGroup()
								.addGroup(
										layout.createSequentialGroup()
												.addGroup(layout.createParallelGroup().addComponent(baseFileLabel)
														.addComponent(animFileLabel).addComponent(outFileLabel))
												.addGap(16)
												.addGroup(layout.createParallelGroup().addComponent(baseFileInput)
														.addComponent(animFileInput).addComponent(outFileInput))
												.addGap(16)
												.addGroup(layout.createParallelGroup().addComponent(baseBrowse)
														.addComponent(animBrowse).addComponent(outBrowse)))
								.addGroup(layout.createSequentialGroup().addComponent(transSingleLabel).addComponent(
										transferSingleAnimation)))
				.addGroup(layout.createSequentialGroup().addGap(48)
						.addGroup(layout.createParallelGroup().addComponent(pickAnimLabel).addComponent(visFromLabel))
						.addGap(16)
						.addGroup(layout.createParallelGroup().addComponent(pickAnimBox).addComponent(visFromBox)))
				.addGroup(layout.createSequentialGroup().addComponent(transfer).addComponent(done))
				.addComponent(goAdvanced)).addGap(12));
		layout.setVerticalGroup(layout.createSequentialGroup().addGap(12)
				.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(baseFileLabel)
								.addComponent(baseFileInput).addComponent(baseBrowse))
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(animFileLabel)
								.addComponent(animFileInput).addComponent(animBrowse))
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(outFileLabel)
								.addComponent(outFileInput).addComponent(outBrowse))
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(transSingleLabel).addComponent(transferSingleAnimation))
						.addGap(8)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(pickAnimLabel)
								.addComponent(pickAnimBox))
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(visFromLabel)
								.addComponent(visFromBox))
						.addGap(24).addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(transfer).addComponent(done))
						.addGap(12).addComponent(goAdvanced)

				).addGap(12));
		setLayout(layout);
	}

	public void refreshModels() throws IOException {
		if (baseFileInput.getText().length() > 0) {
			if (sourceFile == null || sourceFile.getFile() == null
					|| !baseFileInput.getText().equals(sourceFile.getFile().getPath())) {
				sourceFile = MdxUtils.loadEditable(new File(baseFileInput.getText()));
			}
		}
		if (animFileInput.getText().length() > 0) {
			if (animFile == null || animFile.getFile() == null
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

	// public void refreshSource() {
	// sourceFile = MDL.read(new File(baseFileInput.getText()));
	// }
	//
	// public void refreshAnim() {
	// animFile = MDL.read(new File(animFileInput.getText()));
	// }

	public void updateBoxes() {
		if (animFile != null) {
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
		if (sourceFile != null) {
			final DefaultComboBoxModel<Animation> model = new DefaultComboBoxModel<>();

			for (int i = 0; i < sourceFile.getAnimsSize(); i++) {
				final Animation anim = sourceFile.getAnim(i);
				model.addElement(anim);
			}
			final ComboBoxModel<Animation> oldModel = visFromBox.getModel();
			boolean equalModels = oldModel.getSize() > 0;
			for (int i = 0; i < oldModel.getSize() && i < model.getSize() && equalModels; i++) {
				if (oldModel.getElementAt(i) != model.getElementAt(i)) {
					equalModels = false;
				}
			}
			if (!equalModels) {
				visFromBox.setModel(model);
			}
		}
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		if (e.getSource() == baseBrowse) {
			fc.setDialogTitle("Open");
			final int returnValue = fc.showOpenDialog(this);

			if (returnValue == JFileChooser.APPROVE_OPTION) {
				String filepath = fc.getSelectedFile().getPath();
				if (!filepath.toLowerCase().endsWith(".mdl") && !filepath.toLowerCase().endsWith(".mdx")) {
					filepath += ".mdl";
				}
				baseFileInput.setText(filepath);
				try {
					refreshModels();
				} catch (final IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				updateBoxes();
			}
		} else if (e.getSource() == animBrowse) {
			fc.setDialogTitle("Open");
			final int returnValue = fc.showOpenDialog(this);

			if (returnValue == JFileChooser.APPROVE_OPTION) {
				String filepath = fc.getSelectedFile().getPath();
				if (!filepath.toLowerCase().endsWith(".mdl") && !filepath.toLowerCase().endsWith(".mdx")) {
					filepath += ".mdl";
				}
				animFileInput.setText(filepath);
				try {
					refreshModels();
				} catch (final IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				updateBoxes();
			}
		} else if (e.getSource() == outBrowse) {
			fc.setDialogTitle("Save");
			final int returnValue = fc.showSaveDialog(this);

			if (returnValue == JFileChooser.APPROVE_OPTION) {
				String filepath = fc.getSelectedFile().getPath();
				if (!filepath.toLowerCase().endsWith(".mdl") && !filepath.toLowerCase().endsWith(".mdx")) {
					filepath += ".mdl";
				}
				outFileInput.setText(filepath);
			}
		} else if (e.getSource() == transferSingleAnimation) {
			updateBoxes();
			pickAnimBox.setEnabled(transferSingleAnimation.isSelected());
			visFromBox.setEnabled(transferSingleAnimation.isSelected());
		} else if (e.getSource() == transfer) {
			// refreshModels();
			//
			// if( !transferSingleAnimation.isSelected() ) {
			// ImportPanel host = new ImportPanel(sourceFile,animFile,false);
			// host.animTransfer(transferSingleAnimation.isSelected(),
			// pickAnimBox.getItemAt(pickAnimBox.getSelectedIndex()),
			// visFromBox.getItemAt(visFromBox.getSelectedIndex()),false);
			//
			// sourceFile.printTo(new File(outFileInput.getText()));
			// JOptionPane.showMessageDialog(null, "Animation transfer done!");
			//
			// forceRefreshModels();
			// }
			// else
			// {
			// Thread watcher = new Thread(new Runnable() {
			// public void run()
			// {
			// final ImportPanel importPanel = new
			// ImportPanel(sourceFile,animFile,false);
			// importPanel.animTransfer(transferSingleAnimation.isSelected(),
			// pickAnimBox.getItemAt(pickAnimBox.getSelectedIndex()),
			// visFromBox.getItemAt(visFromBox.getSelectedIndex()),false);
			//
			//
			// ImportPanel importPanel2 = new
			// ImportPanel(sourceFile,MDL.read(sourceFile.getFile()),false);
			// importPanel2.animTransferPartTwo(transferSingleAnimation.isSelected(),
			// pickAnimBox.getItemAt(pickAnimBox.getSelectedIndex()),
			// visFromBox.getItemAt(visFromBox.getSelectedIndex()),false);
			//
			// sourceFile.printTo(new File(outFileInput.getText()));
			// JOptionPane.showMessageDialog(null, "Animation transfer done!");
			//
			// forceRefreshModels();
			//
			// }
			// });
			// watcher.start();
			// }
			try {
				doTransfer(false);
			} catch (final IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} else if (e.getSource() == goAdvanced) {
			try {
				doTransfer(true);
			} catch (final IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} else if (e.getSource() == done) {
			parentFrame.setVisible(false);
			parentFrame.dispose();
		}
	}

	public void doTransfer(final boolean show) throws IOException {
		final EditableModel sourceFile = MdxUtils.loadEditable(new File(baseFileInput.getText()));
		final EditableModel animFile = MdxUtils.loadEditable(new File(animFileInput.getText()));

		if (!transferSingleAnimation.isSelected()) {
			final ImportPanel importPanel = new ImportPanel(sourceFile, animFile, show);
			new Thread(() -> {
                importPanel.animTransfer(transferSingleAnimation.isSelected(),
                        pickAnimBox.getItemAt(pickAnimBox.getSelectedIndex()),
                        visFromBox.getItemAt(visFromBox.getSelectedIndex()), show);
                while (importPanel.getParentFrame().isVisible()
                        && (!importPanel.importStarted() || importPanel.importEnded())) {
                    // JOptionPane.showMessageDialog(null, "check 1!");
                    try {
                        Thread.sleep(1);
                    } catch (final Exception e) {
                        ExceptionPopup.display("MatrixEater detected error with Java's wait function", e);
                    }
                }
                // if( !importPanel.getParentFrame().isVisible() &&
                // !importPanel.importEnded() )
                // JOptionPane.showMessageDialog(null,"bad voodoo
                // "+importPanel.importSuccessful());
                // else
                // JOptionPane.showMessageDialog(null,"good voodoo
                // "+importPanel.importSuccessful());
                // if( importPanel.importSuccessful() )
                // {
                // newModel.saveFile();
                // loadFile(newModel.getFile());
                // }

                if (importPanel.importStarted()) {
                    while (!importPanel.importEnded()) {
                        // JOptionPane.showMessageDialog(null, "check 2!");
                        try {
                            Thread.sleep(1);
                        } catch (final Exception e) {
                            ExceptionPopup.display("MatrixEater detected error with Java's wait function", e);
                        }
                    }

                    // JOptionPane.showMessageDialog(null, "Animation
                    // transfer 99% done!");

                    if (importPanel.importSuccessful()) {
                        String filepath = outFileInput.getText();
                        if (!filepath.toLowerCase().endsWith(".mdl") && !filepath.toLowerCase().endsWith(".mdx")) {
                            filepath += ".mdl";
                        }
                        try {
                            MdxUtils.saveMdx(sourceFile, new File(filepath));
                        } catch (final IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        JOptionPane.showMessageDialog(null, "Animation transfer done!");
                    }
                }

                // forceRefreshModels();
            }).start();
		} else {
			final Thread watcher = new Thread(() -> {
                final ImportPanel importPanel = new ImportPanel(sourceFile, animFile, show);
                importPanel.animTransfer(transferSingleAnimation.isSelected(),
                        pickAnimBox.getItemAt(pickAnimBox.getSelectedIndex()),
                        visFromBox.getItemAt(visFromBox.getSelectedIndex()), show);

                // while(importPanel.getParentFrame().isVisible() &&
                // (!importPanel.importStarted() ||
                // importPanel.importEnded()) )
                while (importPanel.getParentFrame().isVisible()
                        && (!importPanel.importStarted() || importPanel.importEnded())) {
                    // JOptionPane.showMessageDialog(null, "check 1!");
                    try {
                        Thread.sleep(1);
                    } catch (final Exception e) {
                        ExceptionPopup.display("MatrixEater detected error with Java's wait function", e);
                    }
                }
                // if( !importPanel.getParentFrame().isVisible() &&
                // !importPanel.importEnded() )
                // JOptionPane.showMessageDialog(null,"bad voodoo
                // "+importPanel.importSuccessful());
                // else
                // JOptionPane.showMessageDialog(null,"good voodoo
                // "+importPanel.importSuccessful());
                // if( importPanel.importSuccessful() )
                // {
                // newModel.saveFile();
                // loadFile(newModel.getFile());
                // }

                if (importPanel.importStarted()) {
                    while (!importPanel.importEnded()) {
                        // JOptionPane.showMessageDialog(null, "check 2!");
                        try {
                            Thread.sleep(1);
                        } catch (final Exception e) {
                            ExceptionPopup.display("MatrixEater detected error with Java's wait function", e);
                        }
                    }

                    // JOptionPane.showMessageDialog(null, "Animation
                    // transfer 99% done!");

                    if (importPanel.importSuccessful()) {
                        final ImportPanel importPanel2;
                        try {
                            importPanel2 = new ImportPanel(sourceFile,
                                    MdxUtils.loadEditable(sourceFile.getFile()), show);
                        } catch (final IOException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                            return;
                        }
                        importPanel2.animTransferPartTwo(transferSingleAnimation.isSelected(),
                                pickAnimBox.getItemAt(pickAnimBox.getSelectedIndex()),
                                visFromBox.getItemAt(visFromBox.getSelectedIndex()), show);

                        while (importPanel2.getParentFrame().isVisible()
                                && (!importPanel2.importStarted() || importPanel2.importEnded())) {
                            // JOptionPane.showMessageDialog(null, "check
                            // 1!");
                            try {
                                Thread.sleep(1);
                            } catch (final Exception e) {
                                ExceptionPopup.display("MatrixEater detected error with Java's wait function", e);
                            }
                        }
                        // if( !importPanel.getParentFrame().isVisible() &&
                        // !importPanel.importEnded() )
                        // JOptionPane.showMessageDialog(null,"bad voodoo
                        // "+importPanel.importSuccessful());
                        // else
                        // JOptionPane.showMessageDialog(null,"good voodoo
                        // "+importPanel.importSuccessful());
                        // if( importPanel.importSuccessful() )
                        // {
                        // newModel.saveFile();
                        // loadFile(newModel.getFile());
                        // }

                        if (importPanel2.importStarted()) {
                            while (!importPanel2.importEnded()) {
                                // JOptionPane.showMessageDialog(null,
                                // "check 2!");
                                try {
                                    Thread.sleep(1);
                                } catch (final Exception e) {
                                    ExceptionPopup.display("MatrixEater detected error with Java's wait function",
                                            e);
                                }
                            }

                            // JOptionPane.showMessageDialog(null,
                            // "Animation transfer 99% done!");

                            if (importPanel2.importSuccessful()) {
                                JOptionPane.showMessageDialog(null, "Animation transfer done!");
                                try {
                                    MdxUtils.saveMdx(sourceFile, new File(outFileInput.getText()));
                                } catch (final IOException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }

                                // forceRefreshModels();
                            }
                        }
                    }
                }
            });
			watcher.start();
		}
	}

	public static void main(final String[] args) {
		try {
			// Set cross-platform Java L&F (also called "Metal")
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (final UnsupportedLookAndFeelException e) {
			// handle exception
		} catch (final ClassNotFoundException e) {
			// handle exception
		} catch (final InstantiationException e) {
			// handle exception
		} catch (final IllegalAccessException e) {
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
