package com.hiveworkshop.assetextractor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.hiveworkshop.wc3.gui.BLPHandler;
import com.hiveworkshop.wc3.gui.ExceptionPopup;
import com.hiveworkshop.wc3.resources.Resources;
import com.hiveworkshop.wc3.resources.WEString;
import com.hiveworkshop.wc3.units.DataTable;
import com.hiveworkshop.wc3.units.Element;
import com.hiveworkshop.wc3.units.ModelOptionPane;
import com.hiveworkshop.wc3.units.ModelOptionPanel;
import com.hiveworkshop.wc3.units.StandardObjectData;
import com.hiveworkshop.wc3.units.UnitOptionPanel;

public final class AssetExtractorFrameBuilder {
	private final String windowTitle;

	public AssetExtractorFrameBuilder(final String windowTitle) {
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
		final JComboBox flattenSetting = new JComboBox<>(new String[] { "Retain all paths", "Retain texture paths",
				"Flatten all paths", "Retain texture and icon paths" });
		final JButton chooseUnitIdButton = new JButton("Extract unit(s)!");
		chooseUnitIdButton.addActionListener(new ExtractorAction(chooseArchiveField, flattenSetting,
				chooseDestinationField, jPanel, includeInternalBox, new ExtractionAction() {
					@Override
					public void doAction(final String destinationPath, final MapAssetExtractor extractor)
							throws IOException {
						final UnitOptionPanel uop = new UnitOptionPanel(extractor.getUnitData(), false);
						while (true) {
							final int x = JOptionPane.showConfirmDialog(jPanel, uop, "Choose Unit Type",
									JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
							if (x == JOptionPane.OK_OPTION) {
								if (uop.getSelection() != null) {
									extractor.extractObject(uop.getSelection().getId(), Paths.get(destinationPath),
											new AssetExtractorSettings(includeInternalBox.isSelected(),
													flattenSetting.getSelectedIndex()));
								} else {
									System.out.println("nullifieed");
								}
							} else {
								break;
							}
						}
					}
				}));
		final JButton chooseObjectButton = new JButton("Extract object(s)!");
		chooseObjectButton.addActionListener(new ExtractorAction(chooseArchiveField, flattenSetting,
				chooseDestinationField, jPanel, includeInternalBox, new ExtractionAction() {
					@Override
					public void doAction(final String destinationPath, final MapAssetExtractor extractor)
							throws IOException {
						String model = ModelOptionPane.show(jPanel);
						while (model != null) {
							AssetSourceObject.extractModel(extractor.getCodebase(), Paths.get(destinationPath),
									AssetSourceObject.asMdxExtension(model), new AssetExtractorSettings(
											includeInternalBox.isSelected(), flattenSetting.getSelectedIndex()));
							model = ModelOptionPane.show(jPanel, model);
						}
					}
				}));
		final JButton chooseTerrainButton = new JButton("Extract terrain(s)!");
		chooseTerrainButton.addActionListener(new ExtractorAction(chooseArchiveField, flattenSetting,
				chooseDestinationField, jPanel, includeInternalBox, new ExtractionAction() {
					@Override
					public void doAction(final String destinationPath, final MapAssetExtractor extractor)
							throws IOException {
						final DataTable terrainTable = DataTable.getTerrain();
						final DataTable worldEditData = StandardObjectData.getWorldEditData();
						final Set<String> terrainSet = terrainTable.keySet();
						final Terrain[] terrainsArray = new Terrain[terrainSet.size()];
						int terrainIndex = 0;
						final Element tileSets = worldEditData.get("TileSets");
						for (final String key : terrainSet) {
							final Element terrainElement = terrainTable.get(key);
							terrainsArray[terrainIndex++] = new Terrain(key,
									WEString.getStringCaseSensitive("WESTRING_TERRAINTYPE_" + terrainElement.getId()),
									terrainElement.getField("dir") + File.separatorChar
											+ terrainElement.getField("file") + ".blp",
									terrainElement);
						}
						Arrays.sort(terrainsArray, new Comparator<Terrain>() {
							@Override
							public int compare(final Terrain a, final Terrain b) {
								return a.getId().compareTo(b.getId());
							}
						});
						final JList<Terrain> terrains = new JList<>(terrainsArray);
						terrains.setCellRenderer(new TerrainListCellRenderer());
						int option;
						while ((option = JOptionPane.showConfirmDialog(jPanel, new JScrollPane(terrains),
								"Choose Terrain", JOptionPane.OK_CANCEL_OPTION)) == JOptionPane.OK_OPTION) {
							for (final Terrain selectedItem : terrains.getSelectedValuesList()) {
								AssetSourceObject.extract(extractor.getCodebase(), Paths.get(destinationPath),
										selectedItem.getTexturePath(),
										new AssetExtractorSettings(includeInternalBox.isSelected(),
												flattenSetting.getSelectedIndex()),
										false);
							}
						}

						// String model = ModelOptionPane.show(jPanel);
						// while (model != null) {
						// AssetSourceObject.extractModel(extractor.getCodebase(),
						// Paths.get(destinationPath),
						// AssetSourceObject.asMdxExtension(model), new
						// AssetExtractorSettings(
						// includeInternalBox.isSelected(),
						// flattenSetting.getSelectedIndex()));
						// model = ModelOptionPane.show(jPanel, model);
						// }
					}
				}));

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
						.addGroup(layout.createSequentialGroup().addComponent(chooseUnitIdButton).addGap(4)
								.addComponent(chooseObjectButton).addGap(4).addComponent(chooseTerrainButton)))
				.addGap(8));
		layout.setVerticalGroup(layout.createSequentialGroup()

				.addGap(8).addComponent(chooseArchiveLabel)
				.addGroup(
						layout.createParallelGroup().addComponent(chooseArchiveField).addComponent(chooseArchiveButton))
				.addComponent(chooseDestinationLabel)
				.addGroup(layout.createParallelGroup().addComponent(chooseDestinationField)
						.addComponent(chooseDestinationButton))
				.addGroup(layout.createParallelGroup().addComponent(includeInternalBox).addComponent(flattenSetting))
				.addGroup(layout.createParallelGroup().addComponent(chooseUnitIdButton).addComponent(chooseObjectButton)
						.addComponent(chooseTerrainButton))
				.addGap(8));

		return jPanel;
	}

	private final class ExtractorAction implements ActionListener {
		private final JTextField chooseArchiveField;
		private final JComboBox flattenSetting;
		private final JTextField chooseDestinationField;
		private final JPanel jPanel;
		private final JCheckBox includeInternalBox;
		private final ExtractionAction action;

		private ExtractorAction(final JTextField chooseArchiveField, final JComboBox flattenSetting,
				final JTextField chooseDestinationField, final JPanel jPanel, final JCheckBox includeInternalBox,
				final ExtractionAction action) {
			this.chooseArchiveField = chooseArchiveField;
			this.flattenSetting = flattenSetting;
			this.chooseDestinationField = chooseDestinationField;
			this.jPanel = jPanel;
			this.includeInternalBox = includeInternalBox;
			this.action = action;
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final String archivePath = chooseArchiveField.getText();
			final String destinationPath = chooseDestinationField.getText();
			try (MapAssetExtractor extractor = new MapAssetExtractor(Paths.get(archivePath))) {
				UnitOptionPanel.dropRaceCache();
				DataTable.dropCache();
				ModelOptionPanel.dropCache();
				WEString.dropCache();
				Resources.dropCache();
				BLPHandler.get().dropCache();
				if (extractor.getLoadedMPQ() != null && !extractor.getLoadedMPQ().hasListfile()) {
					JOptionPane.showMessageDialog(jPanel,
							"You are extracting data from an archive which contains no listfile.\nYou should consult the creator of the archive for ownership information,\nin case he or she did not want you to use their assets.");
				}
				action.doAction(destinationPath, extractor);
				extractor.close();
			} catch (final Throwable exc) {
				ExceptionPopup.display(exc);
				exc.printStackTrace();
			}

		}

	}

	public interface ExtractionAction {
		void doAction(String destinationPath, MapAssetExtractor extractor) throws IOException;
	}

	public static void main(final String[] args) {
		EditorDisplayManager.setupLookAndFeel();

		final JFrame frame = new AssetExtractorFrameBuilder("Asset Extractor").build();
		frame.setVisible(true);
	}
}
