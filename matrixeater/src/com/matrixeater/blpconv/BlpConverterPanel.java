package com.matrixeater.blpconv;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader;
import com.hiveworkshop.wc3.gui.ExceptionPopup;
import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.modeledit.PerspDisplayPanel;
import com.hiveworkshop.wc3.gui.mpqbrowser.MPQBrowser;
import com.hiveworkshop.wc3.mdl.MDL;
import com.hiveworkshop.wc3.mdl.RenderModel;
import com.hiveworkshop.wc3.mdl.v2.ModelViewManager;
import com.hiveworkshop.wc3.mdx.MdxUtils;
import com.hiveworkshop.wc3.mpq.MpqCodebase;
import com.hiveworkshop.wc3.util.Callback;
import com.matrixeater.src.EditorDisplayManager;

import de.wc3data.image.BlpFile;
import de.wc3data.image.TgaFile;
import de.wc3data.stream.BlizzardDataInputStream;

public class BlpConverterPanel extends JPanel {
	private final JButton loadFile;
	private final JLabel preview;
	private final JButton saveFile;
	private final JFileChooser fileChooser;
	private BufferedImage currentImage;

	private File lastFileFilterFile;

	public BlpConverterPanel() {
		fileChooser = new JFileChooser();
		fileChooser.setAcceptAllFileFilterUsed(false);
		final FileNameExtensionFilter blpFilter = new FileNameExtensionFilter("BLP image", "blp");
		final FileNameExtensionFilter tgaFilter = new FileNameExtensionFilter("TGA image", "tga");
		fileChooser.addChoosableFileFilter(blpFilter);
		fileChooser.addChoosableFileFilter(tgaFilter);
		for (final String ext : ImageIO.getReaderFileSuffixes()) {
			fileChooser.addChoosableFileFilter(new FileNameExtensionFilter(ext.toUpperCase() + " image", ext));
		}
		loadFile = new JButton("Load File");
		preview = new JLabel();
		preview.setMinimumSize(new Dimension(512, 512));
		preview.setPreferredSize(new Dimension(512, 512));
		saveFile = new JButton("Save File");

		setLayout(new BorderLayout());

		final JPanel top = new JPanel();
		top.setLayout(new BorderLayout());
		top.add(loadFile, BorderLayout.LINE_START);
		top.add(saveFile, BorderLayout.LINE_END);
		add(top, BorderLayout.NORTH);
		add(new JScrollPane(preview), BorderLayout.CENTER);

		fileChooser.addPropertyChangeListener(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY,
				new PropertyChangeListener() {
					@Override
					public void propertyChange(final PropertyChangeEvent evt) {
						final File oldFile = (File) evt.getOldValue();
						final File newFile = (File) evt.getNewValue();
						if (oldFile != null && newFile == null) {
							lastFileFilterFile = oldFile;
						}
					}
				});

		fileChooser.addPropertyChangeListener(JFileChooser.FILE_FILTER_CHANGED_PROPERTY, new PropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent evt) {
				final FileNameExtensionFilter newFilter = (FileNameExtensionFilter) evt.getNewValue();
				if (newFilter != null && lastFileFilterFile != null) {
					final File newFile = new File(
							lastFileFilterFile.getPath().substring(0, lastFileFilterFile.getPath().lastIndexOf('.') + 1)
									+ newFilter.getExtensions()[0]);
					fileChooser.setSelectedFile(newFile);
				}
			}
		});

		loadFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				try {
					final int result = fileChooser.showOpenDialog(BlpConverterPanel.this);
					if (result == JFileChooser.APPROVE_OPTION) {
						final File selectedFile = fileChooser.getSelectedFile();
						if (selectedFile != null) {
							final FileFilter fileFilter = fileChooser.getFileFilter();
							if (fileFilter == blpFilter || selectedFile.getName().toLowerCase().endsWith("blp")) {
								final BufferedImage image = BlpFile.read(selectedFile);
								if (image == null) {
									throw new RuntimeException("unable to load: " + selectedFile);
								}
								setCurrentImage(image);
							} else if (fileFilter == tgaFilter
									|| selectedFile.getName().toLowerCase().endsWith("tga")) {
								final BufferedImage image = TgaFile.readTGA(selectedFile);
								if (image == null) {
									throw new RuntimeException("unable to load: " + selectedFile);
								}
								setCurrentImage(image);
							} else {
								final BufferedImage image = ImageIO.read(selectedFile);
								if (image == null) {
									throw new RuntimeException("Unable to load (bad format?): " + selectedFile);
								}
								setCurrentImage(image);
							}
						}
					}
				} catch (final Exception exc) {
					ExceptionPopup.display(exc);
				}
			}
		});
		saveFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				try {
					final int result = fileChooser.showSaveDialog(BlpConverterPanel.this);
					if (result == JFileChooser.APPROVE_OPTION) {
						final File selectedFile = fileChooser.getSelectedFile();
						if (selectedFile != null) {
							final FileFilter fileFilter = fileChooser.getFileFilter();
							if (fileFilter == blpFilter || selectedFile.getName().toLowerCase().endsWith("blp")) {
								final String[] types = { "Jpg", "Paletted" };
								final JComboBox<String> type = new JComboBox<>(types);
								type.setEditable(false);
								final JCheckBox useAlpha = new JCheckBox("Use Alpha", true);
								final JSpinner quality = new JSpinner(new SpinnerNumberModel(100, 0, 100, 0.01));

								final JCheckBox generateMipMaps = new JCheckBox("Generate Mip Maps", true);
								final JCheckBox antiDither = new JCheckBox("Anti Dither", true);

								type.addActionListener(new ActionListener() {
									@Override
									public void actionPerformed(final ActionEvent e) {
										final boolean isJpg = type.getSelectedItem() == types[0];
										quality.setEnabled(isJpg);
										generateMipMaps.setEnabled(!isJpg);
										antiDither.setEnabled(!isJpg);
									}
								});
								generateMipMaps.setEnabled(false);
								antiDither.setEnabled(false);
								JOptionPane.showMessageDialog(BlpConverterPanel.this,
										new Object[] { type, useAlpha, new JLabel("Quality: "), quality,
												generateMipMaps, antiDither },
										"BLP Export Options", JOptionPane.PLAIN_MESSAGE);

								final boolean isJpg = type.getSelectedItem() == types[0];
								if (isJpg) {
									BlpFile.writeJpgBLP(currentImage, selectedFile, useAlpha.isSelected(),
											((Number) quality.getValue()).floatValue() / 100f);
								} else {
									BlpFile.writePalettedBLP(currentImage, selectedFile, useAlpha.isSelected(),
											generateMipMaps.isSelected(), antiDither.isSelected());
								}
							} else if (fileFilter == tgaFilter
									|| selectedFile.getName().toLowerCase().endsWith("tga")) {
								TgaFile.writeTGA(currentImage, selectedFile);
							} else {
								ImageIO.write(currentImage, ((FileNameExtensionFilter) fileFilter).getExtensions()[0],
										selectedFile);
							}
						}
					}
				} catch (final Exception exc) {
					exc.printStackTrace();
					ExceptionPopup.display(exc);
				}
			}
		});

	}

	public void setCurrentImage(final BufferedImage currentImage) {
		this.currentImage = currentImage;
		preview.setIcon(new ImageIcon(currentImage));
		preview.setPreferredSize(new Dimension(currentImage.getWidth(), currentImage.getHeight()));
	}

	public static void main(final String[] args) {
		LwjglNativesLoader.load();
		EditorDisplayManager.setupLookAndFeel();
		final JFrame frame = new JFrame("MACgos Browser for Hayate on Mac");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		final JPanel leftHandCardPanel = new JPanel();
		final CardLayout cardLayout = new CardLayout();
		leftHandCardPanel.setLayout(cardLayout);
		final BlpConverterPanel converterPanel = new BlpConverterPanel();
		leftHandCardPanel.add("tex", converterPanel);
		final MDL emptyModel = new MDL();
		final PerspDisplayPanel modelViewport = new PerspDisplayPanel("", new ModelViewManager(emptyModel),
				new ProgramPreferences(), new RenderModel(emptyModel));
		leftHandCardPanel.add("model", modelViewport);
		final MpqCodebase mpqCodebase = MpqCodebase.get();
		cardLayout.show(leftHandCardPanel, "tex");
		final MPQBrowser mpqBrowser = new MPQBrowser(mpqCodebase, new Callback<String>() {
			@Override
			public void run(final String object) {
				try {
					if (object.toLowerCase().endsWith("mdx")) {
						cardLayout.show(leftHandCardPanel, "model");
						modelViewport.setViewport(new ModelViewManager(
								MdxUtils.loadModel(new BlizzardDataInputStream(mpqCodebase.getResourceAsStream(object)))
										.toMDL()));
					} else {
						cardLayout.show(leftHandCardPanel, "tex");
						converterPanel.setCurrentImage(BlpFile.read(object, mpqCodebase.getResourceAsStream(object)));
					}
				} catch (final IOException e) {
					e.printStackTrace();
					ExceptionPopup.display(e);
				}
			}
		});
		final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftHandCardPanel, mpqBrowser);

		frame.setContentPane(splitPane);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

}
