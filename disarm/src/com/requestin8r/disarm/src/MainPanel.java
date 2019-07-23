package com.requestin8r.disarm.src;

import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.rtf.RTFEditorKit;

import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.modeledit.BoneShell;
import com.hiveworkshop.wc3.gui.modeledit.ModelScale;
import com.hiveworkshop.wc3.gui.modelviewer.AnimationViewer;
import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.Attachment;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetAnim;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.Helper;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.MDL;
import com.hiveworkshop.wc3.mdl.Triangle;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.ModelViewManager;
import com.hiveworkshop.wc3.mpq.MpqCodebase;
import com.hiveworkshop.wc3.units.GameObject;
import com.hiveworkshop.wc3.units.ModelOptionPane;
import com.hiveworkshop.wc3.units.UnitOptionPane;
import com.requestin8r.src.IconGet;
import com.requestin8r.src.Project;

public class MainPanel extends JPanel implements ActionListener {

	String[] keywords = { "weapon", "weap", "axe", "sword", "bow", "knife", "gun", "spear" };
	JButton custom, unit, model, disarm, scalify, undo, save;
	AnimationViewer viewer;
	JFileChooser jfc = new JFileChooser();
	MainFrame frame;

	Project current = null;
	Project backup = null;
	int viewerSize = 300;
	JMenu options, help, edit, addFrom;
	JMenuBar menuBar;
	JMenuItem newDirectory, about, aboutMXE, instructions, removeComponent, removeGroup, listOpts, stripTo, fromUnit,
			fromModel, fromCustom;

	public JMenuBar createJMenuBar() {
		menuBar = new JMenuBar();

		options = new JMenu("Options");
		options.setMnemonic(KeyEvent.VK_O);
		menuBar.add(options);

		help = new JMenu("Help");
		help.setMnemonic(KeyEvent.VK_O);
		menuBar.add(help);

		edit = new JMenu("Edit");
		edit.setMnemonic(KeyEvent.VK_E);
		menuBar.add(edit);

		newDirectory = new JMenuItem("Change Game Directory");
		newDirectory.setAccelerator(KeyStroke.getKeyStroke("control shift D"));
		newDirectory.setToolTipText("Changes the directory from which to load necessary game files for Disarm.");
		newDirectory.setMnemonic(KeyEvent.VK_D);
		newDirectory.addActionListener(this);
		options.add(newDirectory);

		instructions = new JMenuItem("Instructions");
		instructions.setMnemonic(KeyEvent.VK_I);
		instructions.addActionListener(this);
		help.add(instructions);

		about = new JMenuItem("About");
		about.setMnemonic(KeyEvent.VK_A);
		about.addActionListener(this);
		help.add(about);

		removeComponent = new JMenuItem("Remove Component");
		removeComponent.setMnemonic(KeyEvent.VK_R);
		removeComponent.addActionListener(this);
		edit.add(removeComponent);

		removeGroup = new JMenuItem("Remove Group");
		removeGroup.setMnemonic(KeyEvent.VK_G);
		removeGroup.addActionListener(this);
		edit.add(removeGroup);

		edit.add(new JSeparator());

		stripTo = new JMenuItem("Isolate Part");
		stripTo.setMnemonic(KeyEvent.VK_I);
		stripTo.addActionListener(this);
		edit.add(stripTo);

		addFrom = new JMenu("Add From");
		addFrom.setMnemonic(KeyEvent.VK_A);
		edit.add(addFrom);

		fromUnit = new JMenuItem("Unit");
		fromUnit.setMnemonic(KeyEvent.VK_U);
		fromUnit.addActionListener(this);
		addFrom.add(fromUnit);

		fromModel = new JMenuItem("Model");
		fromModel.setMnemonic(KeyEvent.VK_M);
		fromModel.addActionListener(this);
		addFrom.add(fromModel);

		fromCustom = new JMenuItem("Custom");
		fromCustom.setMnemonic(KeyEvent.VK_C);
		fromCustom.addActionListener(this);
		addFrom.add(fromCustom);

		edit.add(new JSeparator());

		listOpts = new JMenuItem("List Known Model Parts");
		listOpts.setMnemonic(KeyEvent.VK_W);
		listOpts.addActionListener(this);
		edit.add(listOpts);

		aboutMXE = new JMenuItem("MatrixEater original Credits");
		aboutMXE.setMnemonic(KeyEvent.VK_M);
		aboutMXE.addActionListener(this);
		help.add(aboutMXE);

		return menuBar;
	}

	public MainPanel(final MainFrame parent) {
		frame = parent;
		final ModelViewManager blank = new ModelViewManager(new MDL());
		viewer = new AnimationViewer(blank, new ProgramPreferences(), true);
		viewer.setModel(blank);// , viewerSize);

		final Font smallFont = new Font("Arial", Font.BOLD, 16);
		final Font medFont = new Font("Arial", Font.BOLD, 28);
		final Font bigFont = new Font("Arial", Font.BOLD, 46);
		final JLabel title = new JLabel("MDXScale");
		title.setIcon(new ImageIcon(IconGet.get("Curse", 64)));
		title.setFont(bigFont);
		final JLabel desc = new JLabel("Scale the model!");
		desc.setFont(smallFont);

		add(title);
		add(desc);

		unit = new JButton("Unit", new ImageIcon(IconGet.get("Footman", 48)));
		unit.setFont(medFont);
		unit.addActionListener(this);
		unit.setToolTipText("Choose a base model by selecting a unit type from Warcraft III.");
		// unitTip.setFont(smallFont);
		model = new JButton("Model", new ImageIcon(new ImageIcon(getClass().getResource("BTNfootmanBro.png")).getImage()
				.getScaledInstance(48, 48, Image.SCALE_SMOOTH)));
		model.setFont(medFont);
		model.addActionListener(this);
		model.setToolTipText("Choose a base model by selecting a model from the Warcraft III Object Editor.");
		// modelTip.setFont(smallFont);
		custom = new JButton("Custom", new ImageIcon(IconGet.get("Temp", 48)));
		custom.setFont(medFont);
		custom.addActionListener(this);
		custom.setToolTipText("Choose a base model by selecting a custom file on your PC.");

		disarm = new JButton("Disarm Now!", new ImageIcon(IconGet.get("Glove", 48)));
		disarm.setFont(medFont);
		disarm.addActionListener(this);
		disarm.setToolTipText("Remove the weapon from this model!");

		scalify = new JButton("Scale Now!", new ImageIcon(IconGet.get("Bloodlust", 48)));
		scalify.setFont(medFont);
		scalify.addActionListener(this);
		scalify.setToolTipText("Scale the model!");

		undo = new JButton("Undo", new ImageIcon(IconGet.get("Cancel", 48)));
		undo.setFont(medFont);
		undo.addActionListener(this);
		undo.setToolTipText("Return to last model state.");

		save = new JButton("Save", new ImageIcon(IconGet.get("GoldMine", 48)));
		save.setFont(medFont);
		save.addActionListener(this);
		save.setToolTipText("Save the model.");

		add(unit);
		add(model);
		add(custom);
		add(viewer);
		add(disarm);
		add(undo);

		final GroupLayout layout = new GroupLayout(this);
		//
		layout.setHorizontalGroup(layout.createSequentialGroup().addGap(16)
				.addGroup(layout.createParallelGroup().addComponent(title).addComponent(desc)
						.addGroup(layout.createSequentialGroup().addComponent(unit).addGap(8).addComponent(model)
								.addGap(8).addComponent(custom))
						.addComponent(viewer)
						.addGroup(layout.createSequentialGroup().addComponent(scalify).addGap(8).addComponent(undo))
						.addComponent(save))
				.addGap(16));

		layout.setVerticalGroup(
				layout.createSequentialGroup().addGap(16).addComponent(title).addGap(4).addComponent(desc).addGap(32)
						.addGroup(layout.createParallelGroup().addComponent(unit).addGap(32).addComponent(model)
								.addGap(32).addComponent(custom))
						.addGap(16).addComponent(viewer).addGap(16)
						.addGroup(layout.createParallelGroup().addComponent(scalify).addGap(32).addComponent(undo))
						.addGap(16).addComponent(save).addGap(16));

		setLayout(layout);

		jfc.addChoosableFileFilter(new FileNameExtensionFilter("Warcraft III Binary Model \"*.mdx\"", "mdx"));
		jfc.addChoosableFileFilter(new FileNameExtensionFilter("Warcraft III Text-based Model \"*.mdl\"", "mdl"));
		jfc.setAcceptAllFileFilterUsed(false);
		jfc.setFileFilter(jfc.getChoosableFileFilters()[0]);
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		if (e.getSource() == unit) {
			final GameObject choice = UnitOptionPane.show(this);
			if (choice != null) {
			} else {
				return;
			}

			String filepath = choice.getField("file");
			final String name = choice.getName();
			final Image icon = choice.getImage();

			MDL toLoad;
			ModelViewManager modelDisp = null;
			try {
				if (filepath.endsWith(".mdl")) {
					filepath = filepath.replace(".mdl", ".mdx");
				} else if (!filepath.endsWith(".mdx")) {
					filepath = filepath.concat(".mdx");
				}
				toLoad = MDL.read(MpqCodebase.get().getFile(filepath));
				modelDisp = new ModelViewManager(toLoad);
			} catch (final Exception exc) {
				exc.printStackTrace();
				// bad model!
				JOptionPane.showMessageDialog(frame, "The chosen model could not be used.", "Program Error",
						JOptionPane.ERROR_MESSAGE);
			}

			if (modelDisp != null) {
				current = new Project(modelDisp, icon, name);
				viewer.setModel(modelDisp);// , viewerSize);
				// WorkPanel workPanel = new WorkPanel(, frame);
				// frame.setContentPane(workPanel);
				// frame.revalidate();
				// frame.pack();
			}
		} else if (e.getSource() == model) {
			String filepath = ModelOptionPane.show(this);
			if (filepath != null) {
				// JOptionPane.showMessageDialog(null, filepath + ". Good
				// choice.");
			} else {
				return;
			}

			String name = filepath;
			if (name.contains("\\")) {
				name = name.substring(name.lastIndexOf("\\") + 1);
			}
			if (name.contains(".")) {
				name = name.substring(0, name.indexOf("."));
			}
			final Image icon = IconGet.get("Temp", 64);

			MDL toLoad;
			ModelViewManager modelDisp = null;
			try {
				if (filepath.endsWith(".mdl")) {
					filepath = filepath.replace(".mdl", ".mdx");
				} else if (!filepath.endsWith(".mdx")) {
					filepath = filepath.concat(".mdx");
				}
				toLoad = MDL.read(MpqCodebase.get().getFile(filepath));
				modelDisp = new ModelViewManager(toLoad);
			} catch (final Exception exc) {
				exc.printStackTrace();
				// bad model!
				JOptionPane.showMessageDialog(frame, "The chosen model could not be used.", "Program Error",
						JOptionPane.ERROR_MESSAGE);
			}

			if (modelDisp != null) {
				// WorkPanel workPanel = new WorkPanel(new Project(modelDisp,
				// icon, name), frame);
				// frame.setContentPane(workPanel);
				// frame.revalidate();
				// frame.pack();
				current = new Project(modelDisp, icon, name);
				viewer.setModel(modelDisp);// , viewerSize);
			}
		} else if (e.getSource() == custom) {
			final int x = jfc.showOpenDialog(frame);
			if ((x == JFileChooser.APPROVE_OPTION) && (jfc.getSelectedFile() != null)) {
				// JOptionPane.showMessageDialog(null, jfc.getSelectedFile() +
				// ". Good choice.");
			} else {
				return;
			}

			final String filepath = jfc.getSelectedFile().getPath();
			String name = filepath;
			if (name.contains("\\")) {
				name = name.substring(name.lastIndexOf("\\") + 1);
			}
			if (name.contains(".")) {
				name = name.substring(0, name.indexOf("."));
			}
			final Image icon = IconGet.get("Temp", 64);

			MDL toLoad;
			ModelViewManager modelDisp = null;
			try {
				toLoad = MDL.read(new File(filepath));
				modelDisp = new ModelViewManager(toLoad);
			} catch (final Exception exc) {
				exc.printStackTrace();
				// bad model!
				JOptionPane.showMessageDialog(frame, "The chosen model could not be used.", "Program Error",
						JOptionPane.ERROR_MESSAGE);
			}

			if (modelDisp != null) {
				current = new Project(modelDisp, icon, name);
				viewer.setModel(modelDisp);// , viewerSize);
				// WorkPanel workPanel = new WorkPanel(new Project(modelDisp,
				// icon, name), frame);
				// frame.setContentPane(workPanel);
				// frame.revalidate();
				// frame.pack();
			}
		} else if (e.getSource() == fromUnit) {
			final GameObject choice = UnitOptionPane.show(this);
			if (choice != null) {
			} else {
				return;
			}

			String filepath = choice.getField("file");
			final String name = choice.getName();
			final Image icon = choice.getImage();

			MDL toLoad;
			ModelViewManager modelDisp = null;
			try {
				if (filepath.endsWith(".mdl")) {
					filepath = filepath.replace(".mdl", ".mdx");
				} else if (!filepath.endsWith(".mdx")) {
					filepath = filepath.concat(".mdx");
				}
				toLoad = MDL.read(MpqCodebase.get().getFile(filepath));
				modelDisp = new ModelViewManager(toLoad);
			} catch (final Exception exc) {
				exc.printStackTrace();
				// bad model!
				JOptionPane.showMessageDialog(frame, "The chosen model could not be used.", "Program Error",
						JOptionPane.ERROR_MESSAGE);
			}

			if (modelDisp != null) {
				final String str = JOptionPane.showInputDialog(frame,
						"What parts of the model would you like to add?\nEnter them as a comma-separated list with no spaces. \nThe \"Disarm Now!\" button runs on the list \"weapon,weap,axe,sword,bow,knife,gun,spear\", for example.");
				final Project source = new Project(modelDisp, icon, name);
				if (str != null) {
					final int wantsGroup = JOptionPane.showConfirmDialog(frame,
							"Use the more generous group-based isolation for grabbing parts to add?",
							"Isolate for Add: Confirmation", JOptionPane.YES_NO_OPTION);
					final String strs2 = JOptionPane.showInputDialog(frame,
							"What part of the model do you want the new stuff to be attached onto?");
					addFrom(source, str.split(","), strs2.split(","), wantsGroup == JOptionPane.YES_OPTION);
				}
			}
		} else if (e.getSource() == fromModel) {
			String filepath = ModelOptionPane.show(this);
			if (filepath != null) {
				// JOptionPane.showMessageDialog(null, filepath + ". Good
				// choice.");
			} else {
				return;
			}

			String name = filepath;
			if (name.contains("\\")) {
				name = name.substring(name.lastIndexOf("\\") + 1);
			}
			if (name.contains(".")) {
				name = name.substring(0, name.indexOf("."));
			}
			final Image icon = IconGet.get("Temp", 64);

			MDL toLoad;
			ModelViewManager modelDisp = null;
			try {
				if (filepath.endsWith(".mdl")) {
					filepath = filepath.replace(".mdl", ".mdx");
				} else if (!filepath.endsWith(".mdx")) {
					filepath = filepath.concat(".mdx");
				}
				toLoad = MDL.read(MpqCodebase.get().getFile(filepath));
				modelDisp = new ModelViewManager(toLoad);
			} catch (final Exception exc) {
				exc.printStackTrace();
				// bad model!
				JOptionPane.showMessageDialog(frame, "The chosen model could not be used.", "Program Error",
						JOptionPane.ERROR_MESSAGE);
			}

			if (modelDisp != null) {
				final String str = JOptionPane.showInputDialog(frame,
						"What parts of the model would you like to add?\nEnter them as a comma-separated list with no spaces. \nThe \"Disarm Now!\" button runs on the list \"weapon,weap,axe,sword,bow,knife,gun,spear\", for example.");
				final Project source = new Project(modelDisp, icon, name);
				if (str != null) {
					final int wantsGroup = JOptionPane.showConfirmDialog(frame,
							"Use the more generous group-based isolation for grabbing parts to add?",
							"Isolate for Add: Confirmation", JOptionPane.YES_NO_OPTION);
					final String strs2 = JOptionPane.showInputDialog(frame,
							"What part of the model do you want the new stuff to be attached onto?");
					addFrom(source, str.split(","), strs2.split(","), wantsGroup == JOptionPane.YES_OPTION);
				}
			}
		} else if (e.getSource() == fromCustom) {
			final int x = jfc.showOpenDialog(frame);
			if ((x == JFileChooser.APPROVE_OPTION) && (jfc.getSelectedFile() != null)) {
				// JOptionPane.showMessageDialog(null, jfc.getSelectedFile() +
				// ". Good choice.");
			} else {
				return;
			}

			final String filepath = jfc.getSelectedFile().getPath();
			String name = filepath;
			if (name.contains("\\")) {
				name = name.substring(name.lastIndexOf("\\") + 1);
			}
			if (name.contains(".")) {
				name = name.substring(0, name.indexOf("."));
			}
			final Image icon = IconGet.get("Temp", 64);

			MDL toLoad;
			ModelViewManager modelDisp = null;
			try {
				toLoad = MDL.read(new File(filepath));
				modelDisp = new ModelViewManager(toLoad);
			} catch (final Exception exc) {
				exc.printStackTrace();
				// bad model!
				JOptionPane.showMessageDialog(frame, "The chosen model could not be used.", "Program Error",
						JOptionPane.ERROR_MESSAGE);
			}

			if (modelDisp != null) {
				final String str = JOptionPane.showInputDialog(frame,
						"What parts of the model would you like to add?\nEnter them as a comma-separated list with no spaces. \nThe \"Disarm Now!\" button runs on the list \"weapon,weap,axe,sword,bow,knife,gun,spear\", for example.");
				final Project source = new Project(modelDisp, icon, name);
				if (str != null) {
					final int wantsGroup = JOptionPane.showConfirmDialog(frame,
							"Use the more generous group-based isolation for grabbing parts to add?",
							"Isolate for Add: Confirmation", JOptionPane.YES_NO_OPTION);
					final String strs2 = JOptionPane.showInputDialog(frame,
							"What part of the model do you want the new stuff to be attached onto?");
					addFrom(source, str.split(","), strs2.split(","), wantsGroup == JOptionPane.YES_OPTION);
				}
			}
		} else if (e.getSource() == disarm) {
			disarm(keywords);
		} else if (e.getSource() == scalify) {
			final JSpinner xSpin = new JSpinner(new SpinnerNumberModel(1, 0, 100000000, 0.001));
			final JSpinner ySpin = new JSpinner(new SpinnerNumberModel(1, 0, 100000000, 0.001));
			final JSpinner zSpin = new JSpinner(new SpinnerNumberModel(1, 0, 100000000, 0.001));
			JOptionPane.showMessageDialog(this, new Object[] { new JLabel("X Scale"), xSpin, new JLabel("Y Scale"),
					ySpin, new JLabel("Z Scale"), zSpin }, "Scale Now!", JOptionPane.PLAIN_MESSAGE);
			ModelScale.scale(this.current.model.getModel(), ((Number) xSpin.getValue()).doubleValue(),
					((Number) ySpin.getValue()).doubleValue(), ((Number) zSpin.getValue()).doubleValue());
		} else if (e.getSource() == removeComponent) {
			final String str = JOptionPane.showInputDialog(frame,
					"What parts of the model would you like to remove?\nEnter them as a comma-separated list with no spaces. \nThe \"Disarm Now!\" button runs on the list \"weapon,weap,axe,sword,bow,knife,gun,spear\", for example.");
			if (str != null) {
				disarm(str.split(","), false);
			}
		} else if (e.getSource() == removeGroup) {
			final String str = JOptionPane.showInputDialog(frame,
					"What parts of the model would you like to remove?\nEnter them as a comma-separated list with no spaces. \nThe \"Disarm Now!\" button runs on the list \"weapon,weap,axe,sword,bow,knife,gun,spear\", for example.");
			if (str != null) {
				disarm(str.split(","));
			}
		} else if (e.getSource() == stripTo) {
			final String str = JOptionPane.showInputDialog(frame,
					"What parts of the model would you like to isolate?\nEnter them as a comma-separated list with no spaces. \nThe \"Disarm Now!\" button runs on the list \"weapon,weap,axe,sword,bow,knife,gun,spear\", for example.");

			if (str != null) {
				isolateParts(str);
			}
		} else if (e.getSource() == undo) {
			if (current != null) {
				throw new UnsupportedOperationException("didn't fix code yet");
				// current.model.undo();
			}
		} else if (e.getSource() == save) {
			if (current == null) {
				JOptionPane.showMessageDialog(frame, "Nothing to save.", "ERROR", JOptionPane.ERROR_MESSAGE);

				return;
			}
			final int returnValue = jfc.showSaveDialog(this);
			File temp = jfc.getSelectedFile();
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				if (temp != null) {
					final FileFilter ff = jfc.getFileFilter();
					final String ext = ff.accept(new File("junk.mdl")) ? ".mdl" : ".mdx";
					final String name = temp.getName();
					if (name.lastIndexOf('.') != -1) {
						if (!name.substring(name.lastIndexOf('.'), name.length()).equals(ext)) {
							temp = (new File(
									temp.getAbsolutePath().substring(0, temp.getAbsolutePath().lastIndexOf('.'))
											+ ext));
						}
					} else {
						temp = (new File(temp.getAbsolutePath() + ext));
					}
					final File currentFile = temp;
					if (temp.exists()) {
						final Object[] options = { "Overwrite", "Cancel" };
						final int n = JOptionPane.showOptionDialog(frame, "Selected file already exists.", "Warning",
								JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[1]);
						if (n == 1) {
							jfc.setSelectedFile(null);
							return;
						}
					}
					// profile.setPath(currentFile.getParent());
					current.model.getModel().printTo(currentFile);
					current.model.getModel().setFile(currentFile);
					// tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(),currentFile.getName().split("\\.")[0]);
					// tabbedPane.setToolTipTextAt(tabbedPane.getSelectedIndex(),currentFile.getPath());
				} else {
					JOptionPane.showMessageDialog(this,
							"You tried to save, but you somehow didn't select a file.\nThat is unfortunate.");
				}
			}
			jfc.setSelectedFile(null);
			// refreshController();
		} else if (e.getSource() == newDirectory) {
			JOptionPane.showMessageDialog(MainPanel.this,
					"Disarm built from source is no longer supported and needs an update to fix this. If you live in my future and want disarm from source, see branch history, download 'prod' branch.");
//			final DirectorySelector selector = new DirectorySelector(SaveProfile.get().getGameDirectory(), "");
//			JOptionPane.showMessageDialog(null, selector, "Locating Warcraft III Directory",
//					JOptionPane.QUESTION_MESSAGE);
//			String wcDirectory = selector.getDir();
//			if (!(wcDirectory.endsWith("/") || wcDirectory.endsWith("\\"))) {
//				wcDirectory = wcDirectory + "\\";
//			}
//			SaveProfile.get().setGameDirectory(wcDirectory);
//
//			AnimationViewer pdp;
//			pdp = viewer;
//			pdp.reloadAllTextures();
//			MpqCodebase.get().refresh();
		} else if (e.getSource() == listOpts) {
			if (current == null) {
				return;
			}
			final DefaultStyledDocument panel = new DefaultStyledDocument();
			final JTextPane epane = new JTextPane();
			try {
				for (final IdObject obj : current.model.getModel().sortedIdObjects(Bone.class)) {
					// String caseless = obj.getName().toLowerCase();
					// if( !caseless.contains("object")
					// && !caseless.contains("mesh")
					// && !caseless.contains("cone"))
					panel.insertString(panel.getLength(), obj.getName() + "\n", null);
				}
				for (final IdObject obj : current.model.getModel().sortedIdObjects(Helper.class)) {
					// String caseless = obj.getName().toLowerCase();
					// if( !caseless.contains("object")
					// && !caseless.contains("mesh")
					// && !caseless.contains("cone"))
					panel.insertString(panel.getLength(), obj.getName() + "\n", null);
				}
				for (final IdObject obj : current.model.getModel().sortedIdObjects(Attachment.class)) {
					// String caseless = obj.getName().toLowerCase();
					// if( !caseless.contains("object")
					// && !caseless.contains("mesh")
					// && !caseless.contains("cone"))
					panel.insertString(panel.getLength(), obj.getName() + "\n", null);
				}
			} catch (final BadLocationException e1) {
				e1.printStackTrace();
			}
			epane.setDocument(panel);
			epane.setEditable(false);
			final JFrame frame = new JFrame(String.format("%s: List Knowm Model Parts", current.name));
			frame.setContentPane(new JScrollPane(epane));
			frame.setSize(650, 500);
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
			// JOptionPane.showMessageDialog(this,new JScrollPane(epane));
		} else if (e.getSource() == about) {
			final DefaultStyledDocument panel = new DefaultStyledDocument();
			final JTextPane epane = new JTextPane();
			final RTFEditorKit rtfk = new RTFEditorKit();
			try {
				rtfk.read(MainPanel.class.getResourceAsStream("credits.rtf"), panel, 0);
			} catch (final MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (final IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (final BadLocationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			epane.setDocument(panel);
			epane.setEditable(false);
			final JFrame frame = new JFrame("About");
			frame.setContentPane(new JScrollPane(epane));
			frame.setSize(650, 500);
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
			// JOptionPane.showMessageDialog(this,new JScrollPane(epane));
		} else if (e.getSource() == instructions) {
			final DefaultStyledDocument panel = new DefaultStyledDocument();
			final JTextPane epane = new JTextPane();
			final RTFEditorKit rtfk = new RTFEditorKit();
			try {
				rtfk.read(MainPanel.class.getResourceAsStream("instr.rtf"), panel, 0);
			} catch (final MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (final IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (final BadLocationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			epane.setDocument(panel);
			epane.setEditable(false);
			final JFrame frame = new JFrame("Instructions");
			frame.setContentPane(new JScrollPane(epane));
			frame.setSize(650, 500);
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
			// JOptionPane.showMessageDialog(this,new JScrollPane(epane));
		} else if (e.getSource() == aboutMXE) {
			final DefaultStyledDocument panel = new DefaultStyledDocument();
			final JTextPane epane = new JTextPane();
			final RTFEditorKit rtfk = new RTFEditorKit();
			try {
				rtfk.read(com.matrixeater.src.MainPanel.class.getResourceAsStream("credits.rtf"), panel, 0);
			} catch (final MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (final IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (final BadLocationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			epane.setDocument(panel);
			epane.setEditable(false);
			final JFrame frame = new JFrame("About MatrixEater");
			frame.setContentPane(new JScrollPane(epane));
			frame.setSize(650, 500);
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
			// JOptionPane.showMessageDialog(this,new JScrollPane(epane));
		}
	}

	private void isolateParts(final String str) {
		final int wantsGroup = JOptionPane.showConfirmDialog(frame, "Use the more generous group-based isolation?",
				"Isolate: Confirmation", JOptionPane.YES_NO_OPTION);
		disarm(str.split(","), wantsGroup == JOptionPane.YES_OPTION, true);
		// model still has animations and stuff
	}

	public void addFrom(final Project source, final String[] keywords, final String[] atchKeys, final boolean group) {
		if (current != null) {
			final ArrayList<Vertex> selection = selectGroup(source, keywords, group);
			// ArrayList<Vertex> mainGroup = selectGroup(current, keywords,
			// group);
			if (selection == null) {
				JOptionPane.showMessageDialog(frame, "Aborting script, not enough model data was detected.", "ERROR",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (true) {
				throw new UnsupportedOperationException("didn't fix code yet");
				// source.model.selectVerteces(selection, 0);
				// source.model.invertSelection();
				// source.model.delete();
			}

			final Bone yoBone = getBoneFromKeywords(source, keywords);

			final MDL model = current.model.getModel();
			final MDL sourceModel = source.model.getModel();
			// for( Bone bone: sourceModel.sortedIdObjects(Bone.class) ) {
			// boolean match = false;
			// for( String str: keywords ) {
			// if( bone.getName().toLowerCase().contains(str.toLowerCase()) ) {
			// match = true;
			// }
			// }
			// if( match )
			// yoBone = bone;
			// }
			//
			// if( yoBone == null ) {
			// for( Bone bone: sourceModel.sortedIdObjects(Bone.class) ) {
			// boolean match = false;
			// if( bone.getParent() == null ) {
			// continue;
			// }
			// for( String str: keywords ) {
			// if(
			// bone.getParent().getName().toLowerCase().contains(str.toLowerCase())
			// ) {
			// match = true;
			// }
			// }
			// if( match )
			// yoBone = bone;
			// }
			// }

			if (yoBone == null) {
				JOptionPane.showMessageDialog(frame,
						"Aborting script, there was no node found in the imported model model.\n\n(Unusual error -- seems like it should never come up.)",
						"ERROR", JOptionPane.ERROR_MESSAGE);
				return;
			}

			final Bone myBone = getBoneFromKeywords(current, atchKeys);

			// for( Bone bone: model.sortedIdObjects(Bone.class) ) {
			// boolean match = false;
			// for( String str: atchKeys ) {
			// if( bone.getName().toLowerCase().contains(str.toLowerCase()) ) {
			// match = true;
			// }
			// }
			// if( match )
			// myBone = bone;
			// }
			//
			// if( myBone == null ) {
			// for( Bone bone: model.sortedIdObjects(Bone.class) ) {
			// boolean match = false;
			// if( bone.getParent() == null ) {
			// continue;
			// }
			// for( String str: atchKeys ) {
			// if(
			// bone.getParent().getName().toLowerCase().contains(str.toLowerCase())
			// ) {
			// match = true;
			// }
			// }
			// if( match )
			// myBone = bone;
			// }
			// }

			if (myBone == null) {
				JOptionPane.showMessageDialog(frame,
						"Aborting script, there was no matching node found in the current model.", "ERROR",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			// JOptionPane.showMessageDialog(frame, "This is a test statement
			// for code development:\nModel data is being ripped from
			// "+yoBone.getName() +" and put onto "+myBone.getName()+".",
			// "ERROR", JOptionPane.ERROR_MESSAGE);

			Geoset myBaseGeoset = null;

			for (int i = 0; (i < model.getGeosetsSize()) && (myBaseGeoset == null); i++) {
				final Geoset geo = model.getGeoset(i);
				for (int v = 0; (v < geo.numVerteces()) && (myBaseGeoset == null); v++) {
					final GeosetVertex gv = geo.getVertex(v);
					if (gv.getBoneAttachments().contains(myBone)
							|| gv.getBoneAttachments().contains(myBone.getParent())) {
						myBaseGeoset = geo;
					}
				}
			}

			if (myBaseGeoset == null) {
				JOptionPane.showMessageDialog(frame,
						"No matching geoset found in current model. Generic data will be used (Always visible).",
						"WARNING", JOptionPane.WARNING_MESSAGE);

			}

			final DefaultListModel<BoneShell> shellList = new DefaultListModel<>();
			shellList.addElement(new BoneShell(myBone));
			source.editor.selectByVertices(selection);
			// source.model.setMatrix(shellList);
			if (true) {
				throw new UnsupportedOperationException("didn't fix code yet");
			}

			final Vertex startPivot = myBone.getPivotPoint();
			final Vertex endPivot = yoBone.getPivotPoint();
			// if( myBone.getParent() != null ) {
			// myBone = myBone.getParent();
			// }
			final Vertex modelShift = new Vertex(startPivot.x - endPivot.x, startPivot.y - endPivot.y,
					startPivot.z - endPivot.z);

			for (int i = 0; i < sourceModel.getGeosetsSize(); i++) {
				final Geoset geo = sourceModel.getGeoset(i);

				model.add(geo);
				for (int v = 0; v < geo.numVerteces(); v++) {
					final GeosetVertex gv = geo.getVertex(v);
					gv.x += modelShift.x;
					gv.y += modelShift.y;
					gv.z += modelShift.z;
				}

				final GeosetAnim geoanim = geo.forceGetGeosetAnim();
				model.add(geoanim);
				final AnimFlag oldFlag = geoanim.getVisibilityFlag();
				AnimFlag flag = null;
				if (oldFlag != null) {
					flag = AnimFlag.buildEmptyFrom(oldFlag);
				} else if ((myBaseGeoset != null) && (myBaseGeoset.getVisibilityFlag() != null)) {
					flag = new AnimFlag(myBaseGeoset.getVisibilityFlag());
				}
				geoanim.setVisibilityFlag(flag);
				if ((myBaseGeoset != null) && (flag != null) && (myBaseGeoset.getVisibilityFlag() != null)) {
					flag.copyFrom(myBaseGeoset.getVisibilityFlag());
				}

				current.model.makeGeosetEditable(geo);
				current.model.makeGeosetVisible(geo);
			}
			// model.updateObjectIds();
			// model.doSavePreps();
			// current.viewer.reloadTextures();

			throw new UnsupportedOperationException("didn't fix code yet");
		}
	}

	public void disarm(final String[] keywords) {
		disarm(keywords, true);
	}

	public void disarm(final String[] keywords, final boolean group) {
		disarm(keywords, group, false);
	}

	public void disarm(final String[] keywords, final boolean group, final boolean inverse) {
		if (current != null) {
			final ArrayList<Vertex> selection = selectGroup(keywords, group);
			if (selection != null) {
				current.editor.selectByVertices(selection);
				if (inverse) {
					current.editor.invertSelection();
				}
				current.editor.deleteSelectedComponents();// new DoNothingModelChangeListener()
			}
		}
	}

	public Bone getBoneFromKeywords(final Project current, final String[] keywords) {
		if (current != null) {
			final MDL model = current.model.getModel();
			if (model == null) {
				// JOptionPane.showMessageDialog(frame, "No suitable model
				// loaded!", "ERROR", JOptionPane.ERROR_MESSAGE);
				return null;
			}
			final List<IdObject> weaps = new ArrayList<>();
			for (int i = 0; i < model.getIdObjectsSize(); i++) {
				final IdObject atch = model.getIdObject(i);
				boolean match = false;
				for (final String str : keywords) {
					if (atch.getName().toLowerCase().contains(str.toLowerCase())) {
						match = true;
					}
				}
				if (match) {
					weaps.add(atch);
				}
			}
			// for(IdObject weap: weaps)
			// System.err.println(weap.getName() + ": " + (weap == null ?
			// "nothing trololol" : (weap.getParent() == null ? "no parent" :
			// weap.getParent().getName())));
			if (weaps.size() < 1) {
				// JOptionPane.showMessageDialog(frame, "No suitable weapon was
				// found in the model to remove.", "ERROR",
				// JOptionPane.ERROR_MESSAGE);
				return null;
			}

			for (final IdObject weap : weaps) {
				IdObject weapHeadNode = weap.getParent();
				weapHeadNode = weap;
				boolean valid = true;
				final HashMap<IdObject, Boolean> isValid = new HashMap<>();
				for (final Bone b : model.sortedIdObjects(Bone.class)) {
					if (isValid.get(b) == null) {
						IdObject currentBone = b;
						boolean boneValid = false;
						while (currentBone != null) {
							if (currentBone == weapHeadNode) {
								boneValid = true;
								break;
							}
							currentBone = currentBone.getParent();
						}
						isValid.put(b, boneValid);
						valid = boneValid;
					} else if (!isValid.get(b)) {
						valid = false;
					}
					if (valid) {
						return b;
					}
				}
				// if( boneList.size() == 1 && (boneList.contains(weapHeadNode)
				// || boneList.contains(weap)) ) {
				// selection.add(gv);
				// }
			}
			for (final IdObject weap : weaps) {
				final IdObject weapHeadNode = weap.getParent();
				boolean valid = true;
				final HashMap<IdObject, Boolean> isValid = new HashMap<>();
				for (final Bone b : model.sortedIdObjects(Bone.class)) {
					if (isValid.get(b) == null) {
						IdObject currentBone = b;
						boolean boneValid = false;
						while (currentBone != null) {
							if (currentBone == weapHeadNode) {
								boneValid = true;
								break;
							}
							currentBone = currentBone.getParent();
						}
						isValid.put(b, boneValid);
						valid = boneValid;
					} else if (!isValid.get(b)) {
						valid = false;
					}
					if (valid) {
						return b;
					}
				}
				// if( boneList.size() == 1 && (boneList.contains(weapHeadNode)
				// || boneList.contains(weap)) ) {
				// selection.add(gv);
				// }
			}
		}
		return null;
	}

	public ArrayList<Vertex> selectGroup(final String[] keywords, final boolean group) {
		return selectGroup(current, keywords, group);
	}

	public ArrayList<Vertex> selectGroup(final Project current, final String[] keywords, final boolean group) {
		for (final String str : keywords) {
			System.err.println(str);
		}
		if (current != null) {
			final MDL model = current.model.getModel();
			if (model == null) {
				JOptionPane.showMessageDialog(frame, "No suitable model loaded!", "ERROR", JOptionPane.ERROR_MESSAGE);
				return null;
			}
			final List<IdObject> weaps = new ArrayList<>();
			for (int i = 0; i < model.getIdObjectsSize(); i++) {
				final IdObject atch = model.getIdObject(i);
				boolean match = false;
				for (final String str : keywords) {
					if (atch.getName().toLowerCase().contains(str.toLowerCase())) {
						match = true;
					}
				}
				if (match) {
					weaps.add(atch);
				}
			}
			for (final IdObject weap : weaps) {
				System.err.println(weap.getName() + ": " + (weap == null ? "nothing trololol"
						: (weap.getParent() == null ? "no parent" : weap.getParent().getName())));
			}
			if (weaps.size() < 1) {
				JOptionPane.showMessageDialog(frame, "No suitable weapon was found in the model to remove.", "ERROR",
						JOptionPane.ERROR_MESSAGE);
				return null;
			}

			final ArrayList<Vertex> selection = new ArrayList<>();
			for (final IdObject weap : weaps) {
				IdObject weapHeadNode = weap.getParent();
				if (!group) {
					weapHeadNode = weap;
				}
				for (int i = 0; i < model.getGeosetsSize(); i++) {
					final Geoset geo = model.getGeoset(i);
					for (int vertId = 0; vertId < geo.numVerteces(); vertId++) {
						final GeosetVertex gv = geo.getVertex(vertId);
						final List<Bone> boneList = gv.getBoneAttachments();
						boolean valid = true;
						final HashMap<IdObject, Boolean> isValid = new HashMap<>();
						for (final Bone b : boneList) {
							if (isValid.get(b) == null) {
								IdObject currentBone = b;
								boolean boneValid = false;
								while (currentBone != null) {
									if (currentBone == weapHeadNode) {
										boneValid = true;
										break;
									}
									currentBone = currentBone.getParent();
								}
								isValid.put(b, boneValid);
								valid = boneValid;
							} else if (!isValid.get(b)) {
								valid = false;
							}
						}
						if (valid && !selection.contains(gv)) {
							selection.add(gv);
						}
						// if( boneList.size() == 1 &&
						// (boneList.contains(weapHeadNode) ||
						// boneList.contains(weap)) ) {
						// selection.add(gv);
						// }
					}
				}
			}
			if (selection.size() < 1) {
				JOptionPane.showMessageDialog(frame,
						"No suitable weapon mesh was found in the model to remove (Already removed?).", "ERROR",
						JOptionPane.ERROR_MESSAGE);
				return null;
			}

			// Backcheck

			final ArrayList<Vertex> badPool = new ArrayList<>();
			for (final Vertex v : selection) {
				for (int i = 0; i < model.getGeosetsSize(); i++) {
					final Geoset geo = model.getGeoset(i);
					for (int vertId = 0; vertId < geo.numVerteces(); vertId++) {
						final GeosetVertex gv = geo.getVertex(vertId);
						if (!selection.contains(gv) && v.equalLocs(gv)) {
							badPool.add(v);
						}
					}
				}
				if (!badPool.contains(v)) {
					final GeosetVertex gv = (GeosetVertex) v;
					for (final Triangle tri : gv.getTriangles()) {
						for (final GeosetVertex other : tri.getAll()) {
							if (!selection.contains(other)) {
								badPool.add(v);
								break;
							}
						}
						if (badPool.contains(v)) {
							break;
						}
					}
				}
			}
			final ArrayList<Vertex> enlargedBadPool = new ArrayList<>();
			for (final Vertex v : badPool) {
				final GeosetVertex gv = (GeosetVertex) v;
				addNeighbors(selection, enlargedBadPool, gv, model, true);
				// for( GeosetVertex)
			}
			final ArrayList<Vertex> allConnectionsPool = new ArrayList<>();
			for (final Vertex v : badPool) {
				final GeosetVertex gv = (GeosetVertex) v;
				addNeighbors(selection, allConnectionsPool, gv, model, false);
				// for( GeosetVertex)
			}
			if ((enlargedBadPool.size() > 0) || (badPool.size() > 0)) {
				final String[] opts = { "Yes", "No", "Delete all connected" };
				final int x = JOptionPane.showOptionDialog(frame,
						"Some parts of the \"weapon\" were found to be connected to other parts of the model.\nThis could mean they are a part of a hand, or an arm. Delete them anyway?",
						"Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, opts, opts[1]);
				if (x == 1) {
					selection.removeAll(enlargedBadPool);
				} else if (x == 2) {
					for (final Vertex v : allConnectionsPool) {
						if (!selection.contains(v)) {
							selection.add(v);
						}
					}
				}
			}
			return selection;
		}
		return null;
	}

	public void addNeighbors(final ArrayList<Vertex> selection, final ArrayList<Vertex> enlargedBadPool,
			final GeosetVertex gv, final MDL model, final boolean checkSel) {
		enlargedBadPool.add(gv);
		for (int i = 0; i < model.getGeosetsSize(); i++) {
			final Geoset geo = model.getGeoset(i);
			for (int k = 0; k < geo.numTriangles(); k++) {
				final Triangle tri = geo.getTriangle(k);
				if (tri.contains(gv) || tri.containsRef(gv)) {
					for (final GeosetVertex other : tri.getAll()) {
						if ((!checkSel || selection.contains(other)) && !enlargedBadPool.contains(other)) {
							addNeighbors(selection, enlargedBadPool, other, model, checkSel);
						}
					}
				}
			}
		}
	}
}
