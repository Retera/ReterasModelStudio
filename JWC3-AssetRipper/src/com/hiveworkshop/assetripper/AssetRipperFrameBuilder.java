package com.hiveworkshop.assetripper;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Paths;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.hiveworkshop.wc3.gui.ExceptionPopup;
import com.hiveworkshop.wc3.units.UnitOptionPanel;

public final class AssetRipperFrameBuilder {
	private final String windowTitle;

	public AssetRipperFrameBuilder(final String windowTitle) {
		this.windowTitle = windowTitle;
	}

	public JFrame build() {
		final JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		final JPanel panel = buildPanel();
		frame.setContentPane(panel);
		frame.setTitle(windowTitle);
		frame.pack();
		frame.setLocationRelativeTo(null);
		return frame;
	}

	private JPanel buildPanel() {
		final JPanel jPanel = new JPanel();
		final GroupLayout layout = new GroupLayout(jPanel);
		jPanel.setLayout(layout);

		final JLabel chooseArchiveLabel = new JLabel("Choose Archive");
		final JTextField chooseArchiveField = new JTextField();
		final JButton chooseArchiveButton = new JButton("Browse...");
		chooseArchiveButton.addActionListener(new ActionListener() {
			JFileChooser jfc = new JFileChooser();

			@Override
			public void actionPerformed(final ActionEvent e) {
				jfc.setSelectedFile(new File(chooseArchiveField.getText()));
				final int result = jfc.showOpenDialog(jPanel);
				if (result == JFileChooser.APPROVE_OPTION && jfc.getSelectedFile() != null) {
					chooseArchiveField.setText(jfc.getSelectedFile().getAbsolutePath());
				}
			}
		});

		final JLabel chooseDestinationLabel = new JLabel("Choose Destination Folder");
		final JTextField chooseDestinationField = new JTextField();
		final JButton chooseDestinationButton = new JButton("Browse...");
		chooseDestinationButton.addActionListener(new ActionListener() {
			JFileChooser jfc = new JFileChooser();
			{
				jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			}

			@Override
			public void actionPerformed(final ActionEvent e) {
				jfc.setSelectedFile(new File(chooseDestinationField.getText()));
				final int result = jfc.showOpenDialog(jPanel);
				if (result == JFileChooser.APPROVE_OPTION && jfc.getSelectedFile() != null) {
					chooseDestinationField.setText(jfc.getSelectedFile().getAbsolutePath());
				}
			}
		});

		final JCheckBox includeInternalBox = new JCheckBox("Include Internal");
		final JComboBox flattenSetting = new JComboBox<>(
				new String[] { "Retain all paths", "Retain texture paths", "Flatten all paths" });
		final JButton chooseUnitIdButton = new JButton("Extract unit(s)!");
		chooseUnitIdButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final String archivePath = chooseArchiveField.getText();
				final String destinationPath = chooseDestinationField.getText();
				try (MapAssetRipper ripper = new MapAssetRipper(Paths.get(archivePath))) {
					UnitOptionPanel.dropRaceCache();
					final UnitOptionPanel uop = new UnitOptionPanel(ripper.getUnitData(), false);
					while (true) {
						final int x = JOptionPane.showConfirmDialog(jPanel, uop, "Choose Unit Type",
								JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
						if (x == JOptionPane.OK_OPTION) {
							if (uop.getSelection() != null) {
								ripper.ripObject(uop.getSelection().getId(), Paths.get(destinationPath),
										new AssetRipperSettings(includeInternalBox.isSelected(),
												flattenSetting.getSelectedIndex()));
							} else {
								System.out.println("nullifieed");
							}
						} else {
							break;
						}
					}
					ripper.close();
				} catch (final Exception exc) {
					ExceptionPopup.display(exc);
					exc.printStackTrace();
				}
			}
		});

		layout.setHorizontalGroup(layout.createSequentialGroup()

				.addGap(8)
				.addGroup(layout.createParallelGroup().addComponent(chooseArchiveLabel)
						.addGroup(layout.createSequentialGroup().addComponent(chooseArchiveField)
								.addComponent(chooseArchiveButton))
						.addComponent(chooseDestinationLabel)
						.addGroup(layout.createSequentialGroup().addComponent(chooseDestinationField)
								.addComponent(chooseDestinationButton))
						.addGroup(layout.createSequentialGroup().addComponent(includeInternalBox)
								.addComponent(flattenSetting))
						.addComponent(chooseUnitIdButton))
				.addGap(8));
		layout.setVerticalGroup(layout.createSequentialGroup()

				.addGap(8).addComponent(chooseArchiveLabel)
				.addGroup(
						layout.createParallelGroup().addComponent(chooseArchiveField).addComponent(chooseArchiveButton))
				.addComponent(chooseDestinationLabel)
				.addGroup(layout.createParallelGroup().addComponent(chooseDestinationField)
						.addComponent(chooseDestinationButton))
				.addGroup(layout.createParallelGroup().addComponent(includeInternalBox).addComponent(flattenSetting))
				.addComponent(chooseUnitIdButton).addGap(8));

		return jPanel;
	}

	public static void main(final String[] args) {
		EditorDisplayManager.setupLookAndFeel();

		final JFrame frame = new AssetRipperFrameBuilder("Asset Ripper").build();
		frame.setVisible(true);
	}
}
