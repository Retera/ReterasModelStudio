package com.hiveworkshop.wc3.gui.modeledit.wizards;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.hiveworkshop.wc3.gui.modeledit.ModelPanel;
import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.componenttree.ModelComponentCopier;
import com.hiveworkshop.wc3.gui.modeledit.componenttree.actions.AddComponentsAction;
import com.hiveworkshop.wc3.gui.modelviewer.ViewerCamera;
import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.Animation;
import com.hiveworkshop.wc3.mdl.Attachment;
import com.hiveworkshop.wc3.mdl.Bitmap;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.CollisionShape;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.EventObject;
import com.hiveworkshop.wc3.mdl.ExtLog;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.ParticleEmitter2;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.util.IconUtils;

import net.miginfocom.swing.MigLayout;

/**
 * The Add menu: friendly, undoable wizards for the things people add most.
 * Each wizard collects its inputs in one dialog, builds fully initialised
 * components, pushes a single {@link AddComponentsAction}, and shows the
 * result in the Model tab. The technical "New" (bare component, no dialog)
 * stays in the Model tab's popup.
 */
public final class AddComponentWizards {
	public static final String[] ATTACHMENT_NAMES = { "Origin Ref", "Overhead Ref", "Head Ref", "Chest Ref",
			"Hand Left Ref", "Hand Right Ref", "Foot Left Ref", "Foot Right Ref", "Weapon Ref", "Weapon Left Ref",
			"Weapon Right Ref", "Sprite First Ref", "Sprite Second Ref", "Sprite Third Ref", "Sprite Fourth Ref",
			"Sprite Rallypoint Ref", "Mount Ref", "Head Mount Ref", "Chest Mount Ref", "Left Foot Mount Ref",
			"Right Foot Mount Ref", "Large Ref", "Medium Ref", "Small Ref", "Turret Ref", "Chest Alternate Ref",
			"Head Alternate Ref", "Hand Left Alternate Ref", "Hand Right Alternate Ref", "Origin Alternate Ref" };
	public static final String[] SEQUENCE_NAMES = { "Stand", "Stand - 2", "Stand Ready", "Stand Hit", "Stand Channel",
			"Stand Victory", "Walk", "Walk Fast", "Attack", "Attack - 2", "Attack Slam", "Attack Alternate", "Spell",
			"Spell Slam", "Spell Channel", "Spell Throw", "Death", "Decay Flesh", "Decay Bone", "Dissipate", "Birth",
			"Morph", "Morph Alternate", "Portrait", "Portrait Talk", "Stand Work", "Stand Upgrade", "Stand Lumber",
			"Stand Gold" };

	private final WizardHost host;

	public AddComponentWizards(final WizardHost host) {
		this.host = host;
	}

	/**
	 * Finds the stock particle library: next to the working directory when run
	 * from the repository, or under the installed application's home folder.
	 */
	public static File findStockParticleFolder() {
		final List<File> candidates = new ArrayList<>();
		candidates.add(new File("stock/particles"));
		candidates.add(new File("matrixeater/stock/particles"));
		try {
			final java.net.URL location = AddComponentWizards.class.getProtectionDomain().getCodeSource()
					.getLocation();
			if (location != null) {
				File dir = new File(location.toURI());
				if (dir.isFile()) {
					dir = dir.getParentFile();
				}
				for (int up = 0; (up < 3) && (dir != null); up++) {
					candidates.add(new File(dir, "stock/particles"));
					dir = dir.getParentFile();
				}
			}
		} catch (final Exception e) {
			// fall through to the relative candidates
		}
		for (final File candidate : candidates) {
			if (candidate.isDirectory()) {
				return candidate;
			}
		}
		return candidates.get(0);
	}

	/** Fills the given Add menu with the wizards; the caller appends Animation after. */
	public void populate(final JMenu addMenu, final File stockParticleFolder) {
		addMenu.add(item("Attachment Point...", this::addAttachment));
		addMenu.add(item("Event Object...", this::addEventObject));
		addMenu.add(item("Collision Shape from Selection...", this::addCollisionShape));
		addMenu.add(item("Camera from Current View...", this::addCamera));
		addMenu.add(item("Sequence...", this::addSequence));
		addMenu.add(item("Global Sequence...", this::addGlobalSequence));
		addMenu.add(buildParticleMenu(stockParticleFolder));
		addMenu.addSeparator();
	}

	private JMenuItem item(final String text, final Runnable action) {
		final JMenuItem item = new JMenuItem(text);
		item.addActionListener(e -> {
			if (host.currentModelPanel() == null) {
				JOptionPane.showMessageDialog(host.dialogParent(), "Open a model first.", "Add",
						JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			action.run();
		});
		return item;
	}

	private EditableModel model() {
		return host.currentModelPanel().getModel();
	}

	private void commit(final List<Object> components, final String actionName, final Object showInModelTab) {
		final AddComponentsAction action = new AddComponentsAction(model(), components, host.structureListener(),
				actionName);
		push(action);
		if (showInModelTab != null) {
			host.navigation().openInModelTab(showInModelTab);
		}
	}

	private void push(final UndoAction action) {
		action.redo();
		host.currentModelPanel().getUndoManager().pushAction(action);
	}

	private static Vertex pivotOf(final IdObject parent) {
		return (parent == null) || (parent.getPivotPoint() == null) ? new Vertex(0, 0, 0)
				: new Vertex(parent.getPivotPoint());
	}

	private static boolean nameTaken(final EditableModel model, final String name) {
		for (final IdObject node : model.getIdObjects()) {
			if (name.equals(node.getName())) {
				return true;
			}
		}
		return false;
	}

	// ---- Attachment Point ----------------------------------------------

	private void addAttachment() {
		final EditableModel model = model();
		final WizardForm form = new WizardForm();
		String suggested = null;
		for (final String candidate : ATTACHMENT_NAMES) {
			if (!nameTaken(model, candidate)) {
				suggested = candidate;
				break;
			}
		}
		final JComboBox<String> name = form.row("Name", WizardForm.editableCombo(
				suggested == null ? "Attachment Ref" : suggested, ATTACHMENT_NAMES));
		final JComboBox<IdObject> parent = form.nodeChooser("Parent", model, null);
		final JSpinner[] offset = form.xyz("Offset from parent", new Vertex(0, 0, 0));
		final JLabel hint = form.wide(new JLabel("Standard names let the game and the World Editor find the point."));
		hint.setFont(hint.getFont().deriveFont(java.awt.Font.ITALIC));
		if (!form.showOkCancel(host.dialogParent(), "Add Attachment Point")) {
			return;
		}
		final String text = WizardForm.textOf(name);
		final Attachment attachment = new Attachment(text.isEmpty() ? "Attachment Ref" : text);
		final IdObject parentNode = (IdObject) parent.getSelectedItem();
		final Vertex pivot = pivotOf(parentNode);
		final Vertex delta = WizardForm.vertex(offset);
		attachment.setPivotPoint(new Vertex(pivot.x + delta.x, pivot.y + delta.y, pivot.z + delta.z));
		attachment.setParent(parentNode);
		commit(Collections.singletonList(attachment), "add attachment point", attachment);
	}

	// ---- Event Object --------------------------------------------------

	private void addEventObject() {
		final EditableModel model = model();
		final WizardForm form = new WizardForm();
		final JComboBox<EventObjectCatalog.Kind> kind = form.row("Type",
				new JComboBox<>(EventObjectCatalog.Kind.values()));
		kind.setRenderer(new javax.swing.DefaultListCellRenderer() {
			@Override
			public java.awt.Component getListCellRendererComponent(final JList<?> list, final Object value,
					final int index, final boolean isSelected, final boolean cellHasFocus) {
				final String text = value == null ? "" : ((EventObjectCatalog.Kind) value).displayName;
				return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
			}
		});
		final JTextField filter = form.text("Filter", "");
		final DefaultListModel<EventObjectCatalog.Entry> listModel = new DefaultListModel<>();
		final JList<EventObjectCatalog.Entry> list = new JList<>(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setVisibleRowCount(12);
		form.row("Entry", new JScrollPane(list), "growx, hmin 200");
		final JTextField id = form.text("ID (4 characters)", "");
		final JComboBox<IdObject> parent = form.nodeChooser("Parent", model, null);
		final JComboBox<String> when = form.row("Fire",
				new JComboBox<>(new String[] { "At the start of every sequence", "Once, at time:" }));
		final JSpinner time = form.intSpinner("Time (ms)", 0, 0, Integer.MAX_VALUE, 100);
		final Runnable refill = () -> {
			listModel.clear();
			final String needle = filter.getText().trim().toLowerCase();
			for (final EventObjectCatalog.Entry entry : EventObjectCatalog
					.entries((EventObjectCatalog.Kind) kind.getSelectedItem())) {
				if (needle.isEmpty() || entry.toString().toLowerCase().contains(needle)) {
					listModel.addElement(entry);
				}
			}
		};
		kind.addActionListener(e -> refill.run());
		filter.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(final DocumentEvent e) {
				refill.run();
			}

			@Override
			public void removeUpdate(final DocumentEvent e) {
				refill.run();
			}

			@Override
			public void changedUpdate(final DocumentEvent e) {
				refill.run();
			}
		});
		list.addListSelectionListener(e -> {
			final EventObjectCatalog.Entry selected = list.getSelectedValue();
			if (selected != null) {
				id.setText(selected.id);
			}
		});
		refill.run();
		if (listModel.isEmpty()) {
			form.wide(new JLabel("No game data loaded for this table; type the 4 character ID by hand."));
		}
		if (!form.showOkCancel(host.dialogParent(), "Add Event Object")) {
			return;
		}
		final String code = id.getText().trim();
		if (code.length() != 4) {
			JOptionPane.showMessageDialog(host.dialogParent(), "The event object ID must be exactly 4 characters.",
					"Add Event Object", JOptionPane.WARNING_MESSAGE);
			return;
		}
		final EventObjectCatalog.Kind chosen = (EventObjectCatalog.Kind) kind.getSelectedItem();
		final EventObject eventObject = new EventObject(chosen.prefix + code);
		final IdObject parentNode = (IdObject) parent.getSelectedItem();
		eventObject.setPivotPoint(pivotOf(parentNode));
		eventObject.setParent(parentNode);
		final ArrayList<Integer> track = new ArrayList<>();
		if (when.getSelectedIndex() == 0) {
			for (final Animation animation : model.getAnims()) {
				track.add(animation.getStart());
			}
			if (track.isEmpty()) {
				track.add(0);
			}
		} else {
			track.add(WizardForm.integer(time));
		}
		Collections.sort(track);
		eventObject.setEventTrack(track);
		commit(Collections.singletonList(eventObject), "add event object", eventObject);
	}

	// ---- Collision Shape from selection --------------------------------

	private void addCollisionShape() {
		final ModelPanel panel = host.currentModelPanel();
		final EditableModel model = model();
		Collection<? extends Vertex> selected = panel.getModelEditorManager().getSelectionView()
				.getSelectedVertices();
		boolean wholeModel = false;
		if ((selected == null) || selected.isEmpty()) {
			final List<Vertex> all = new ArrayList<>();
			for (final Geoset geoset : model.getGeosets()) {
				for (final GeosetVertex vertex : geoset.getVertices()) {
					all.add(vertex);
				}
			}
			selected = all;
			wholeModel = true;
		}
		if (selected.isEmpty()) {
			JOptionPane.showMessageDialog(host.dialogParent(), "Select some vertices first (or add a geoset).",
					"Add Collision Shape", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		final Vertex min = new Vertex(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
		final Vertex max = new Vertex(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);
		for (final Vertex v : selected) {
			min.x = Math.min(min.x, v.x);
			min.y = Math.min(min.y, v.y);
			min.z = Math.min(min.z, v.z);
			max.x = Math.max(max.x, v.x);
			max.y = Math.max(max.y, v.y);
			max.z = Math.max(max.z, v.z);
		}
		final Vertex center = new Vertex((min.x + max.x) / 2, (min.y + max.y) / 2, (min.z + max.z) / 2);
		double radius = 0;
		for (final Vertex v : selected) {
			radius = Math.max(radius, center.distance(v));
		}
		final WizardForm form = new WizardForm();
		final JTextField name = form.text("Name", nextFreeName(model, "Collision"));
		final JComboBox<String> type = form.row("Shape", new JComboBox<>(new String[] { "Box", "Sphere" }));
		final JComboBox<IdObject> parent = form.nodeChooser("Parent", model, null);
		final JSpinner padding = form.doubleSpinner("Padding", 0.0, 1.0);
		form.wide(new JLabel(String.format("Bounds of %s: %d vertices, %.1f x %.1f x %.1f",
				wholeModel ? "the whole model" : "the selection", selected.size(), max.x - min.x, max.y - min.y,
				max.z - min.z)));
		if (!form.showOkCancel(host.dialogParent(), "Add Collision Shape")) {
			return;
		}
		final double pad = WizardForm.number(padding);
		final CollisionShape shape = new CollisionShape(name.getText().trim().isEmpty() ? "Collision"
				: name.getText().trim());
		shape.getFlags().clear();
		shape.getVertices().clear();
		shape.setPivotPoint(new Vertex(center));
		if (type.getSelectedIndex() == 1) {
			shape.add("Sphere");
			shape.getVertices().add(new Vertex(center));
			shape.setExtents(new ExtLog(radius + pad));
		} else {
			shape.add("Box");
			shape.getVertices().add(new Vertex(min.x - pad, min.y - pad, min.z - pad));
			shape.getVertices().add(new Vertex(max.x + pad, max.y + pad, max.z + pad));
		}
		final IdObject parentNode = (IdObject) parent.getSelectedItem();
		shape.setParent(parentNode);
		commit(Collections.singletonList(shape), "add collision shape", shape);
	}

	private static String nextFreeName(final EditableModel model, final String base) {
		if (!nameTaken(model, base)) {
			return base;
		}
		for (int i = 2;; i++) {
			final String candidate = base + i;
			if (!nameTaken(model, candidate)) {
				return candidate;
			}
		}
	}

	// ---- Camera from current view --------------------------------------

	private void addCamera() {
		final ModelPanel panel = host.currentModelPanel();
		final EditableModel model = model();
		final ViewerCamera viewer = panel.getPerspArea().getViewport().getViewerCamera();
		final WizardForm form = new WizardForm();
		String suggested = "Camera01";
		for (int i = 1; i < 100; i++) {
			suggested = String.format("Camera%02d", i);
			boolean taken = false;
			for (final Camera camera : model.getCameras()) {
				taken |= suggested.equals(camera.getName());
			}
			if (!taken) {
				break;
			}
		}
		final JTextField name = form.text("Name", suggested);
		final JSpinner fov = form.doubleSpinner("Field of view (deg)", Math.toDegrees(viewer.getFov() * 4.0 / 3.0),
				1.0);
		final JSpinner near = form.doubleSpinner("Near clip", viewer.getNearClipPlane(), 1.0);
		final JSpinner far = form.doubleSpinner("Far clip", viewer.getFarClipPlane(), 100.0);
		form.wide(new JLabel(String.format("Eye (%.1f, %.1f, %.1f) from the Edit perspective viewport",
				viewer.location.x, viewer.location.y, viewer.location.z)));
		if (!form.showOkCancel(host.dialogParent(), "Add Camera from Current View")) {
			return;
		}
		final Vertex position = new Vertex(viewer.location.x, viewer.location.y, viewer.location.z);
		final Vertex target = new Vertex(viewer.location.x - viewer.directionZ.x,
				viewer.location.y - viewer.directionZ.y, viewer.location.z - viewer.directionZ.z);
		final Camera camera = new Camera(name.getText().trim().isEmpty() ? suggested : name.getText().trim(),
				position, target, Math.toRadians(WizardForm.number(fov)), WizardForm.number(far),
				WizardForm.number(near));
		commit(Collections.singletonList(camera), "add camera", camera);
	}

	// ---- Sequence ------------------------------------------------------

	private void addSequence() {
		final EditableModel model = model();
		final WizardForm form = new WizardForm();
		final JComboBox<String> name = form.row("Name", WizardForm.editableCombo("Stand", SEQUENCE_NAMES));
		final JSpinner duration = form.intSpinner("Duration (ms)", 1000, 1, Integer.MAX_VALUE, 100);
		final JCheckBox nonLooping = form.check("Non looping", false);
		final JSpinner rarity = form.doubleSpinner("Rarity", 0.0, 1.0);
		final JSpinner moveSpeed = form.doubleSpinner("Move speed", 0.0, 10.0);
		final List<Animation> sources = new ArrayList<>();
		sources.add(null);
		sources.addAll(model.getAnims());
		final JComboBox<Animation> copyFrom = form.row("Copy keyframes from", new JComboBox<>(sources.toArray(
				new Animation[0])));
		copyFrom.setRenderer(new javax.swing.DefaultListCellRenderer() {
			@Override
			public java.awt.Component getListCellRendererComponent(final JList<?> list, final Object value,
					final int index, final boolean isSelected, final boolean cellHasFocus) {
				final String text = value == null ? "(none, empty sequence)" : ((Animation) value).getName();
				return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
			}
		});
		final int start = ModelComponentCopier.nextFreeSequenceStart(model);
		form.wide(new JLabel("The new sequence starts at " + start + " ms, after every existing sequence."));
		if (!form.showOkCancel(host.dialogParent(), "Add Sequence")) {
			return;
		}
		final String text = WizardForm.textOf(name);
		final Animation animation = new Animation(text.isEmpty() ? "Stand" : text, start,
				start + WizardForm.integer(duration));
		animation.setNonLooping(nonLooping.isSelected());
		animation.setRarity((float) WizardForm.number(rarity));
		animation.setMoveSpeed((float) WizardForm.number(moveSpeed));
		final Animation source = (Animation) copyFrom.getSelectedItem();
		final AddComponentsAction action = new AddComponentsAction(model, Collections.singletonList(animation),
				host.structureListener(), "add sequence");
		if (source != null) {
			action.withKeyframesFrom(source, animation);
		}
		push(action);
		host.navigation().openInModelTab(animation);
	}

	// ---- Global Sequence -----------------------------------------------

	private void addGlobalSequence() {
		final WizardForm form = new WizardForm();
		final JSpinner length = form.intSpinner("Length (ms)", 1000, 1, Integer.MAX_VALUE, 100);
		form.wide(new JLabel("Tracks assigned to a global sequence loop on this clock, ignoring sequences."));
		if (!form.showOkCancel(host.dialogParent(), "Add Global Sequence")) {
			return;
		}
		final Integer globalSequence = ModelComponentCopier.newGlobalSequence(WizardForm.integer(length));
		commit(Collections.singletonList(globalSequence), "add global sequence", globalSequence);
	}

	// ---- Particle (stock library) --------------------------------------

	private JMenu buildParticleMenu(final File stockFolder) {
		final JMenu menu = new JMenu("Particle");
		final File[] stockFiles = stockFolder == null ? null : stockFolder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(final File dir, final String name) {
				return name.endsWith(".mdx");
			}
		});
		if ((stockFiles == null) || (stockFiles.length == 0)) {
			final JMenuItem none = new JMenuItem("(no stock particles found in " + stockFolder + ")");
			none.setEnabled(false);
			menu.add(none);
			return menu;
		}
		java.util.Arrays.sort(stockFiles);
		for (final File file : stockFiles) {
			final String basicName = file.getName().split("\\.")[0];
			final File pngImage = new File(file.getParent() + File.separatorChar + basicName + ".png");
			Image image = null;
			if (pngImage.exists()) {
				try {
					image = ImageIO.read(pngImage);
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
			final Image icon = image;
			final JMenuItem particleItem = icon == null ? new JMenuItem(basicName)
					: new JMenuItem(basicName, new ImageIcon(icon.getScaledInstance(28, 28, Image.SCALE_DEFAULT)));
			particleItem.addActionListener(e -> {
				if (host.currentModelPanel() == null) {
					return;
				}
				addStockParticle(file, basicName, icon);
			});
			menu.add(particleItem);
		}
		return menu;
	}

	private void addStockParticle(final File file, final String basicName, final Image image) {
		final EditableModel model = model();
		final EditableModel stock = EditableModel.read(file);
		final List<ParticleEmitter2> emitters = stock.sortedIdObjects(ParticleEmitter2.class);
		if (emitters.isEmpty()) {
			JOptionPane.showMessageDialog(host.dialogParent(), file.getName() + " holds no ParticleEmitter2.",
					"Add Particle", JOptionPane.WARNING_MESSAGE);
			return;
		}
		final ParticleEmitter2 particle = emitters.get(0);
		final WizardForm form = new WizardForm();
		if (image != null) {
			form.wide(new JLabel(new ImageIcon(image.getScaledInstance(128, 128, Image.SCALE_SMOOTH))), "span 2, alignx center");
		}
		final JTextField name = form.text("Name", nextFreeName(model, basicName));
		final JComboBox<IdObject> parent = form.nodeChooser("Parent", model, null);
		final JSpinner[] position = form.xyz("Position", new Vertex(0, 0, 0));
		parent.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Vertex pivot = pivotOf((IdObject) parent.getSelectedItem());
				position[0].setValue(pivot.x);
				position[1].setValue(pivot.y);
				position[2].setValue(pivot.z);
			}
		});
		final JPanel colorRow = new JPanel(new MigLayout("insets 0", "[][][]", ""));
		final Color[] colors = new Color[3];
		for (int i = 0; i < 3; i++) {
			final Vertex c = particle.getSegmentColor(i);
			colors[i] = c == null ? Color.WHITE
					: new Color(clamp(c.z), clamp(c.y), clamp(c.x));
			final int index = i;
			final JButton button = new JButton("Color " + (i + 1),
					new ImageIcon(IconUtils.createBlank(colors[i], 24, 24)));
			button.addActionListener(e -> {
				final Color chosen = JColorChooser.showDialog(form, "Segment color " + (index + 1), colors[index]);
				if (chosen != null) {
					colors[index] = chosen;
					button.setIcon(new ImageIcon(IconUtils.createBlank(chosen, 24, 24)));
				}
			});
			colorRow.add(button);
		}
		form.row("Segment colors", colorRow);
		final List<Animation> anims = model.getAnims();
		final JPanel animPanel = new JPanel(new MigLayout("insets 0, wrap 3", "", ""));
		final JCheckBox[] boxes = new JCheckBox[anims.size()];
		for (int i = 0; i < anims.size(); i++) {
			boxes[i] = new JCheckBox(anims.get(i).getName(), true);
			animPanel.add(boxes[i]);
		}
		if (!anims.isEmpty()) {
			final JScrollPane scroll = new JScrollPane(animPanel);
			scroll.setBorder(javax.swing.BorderFactory.createTitledBorder("Visible during"));
			form.wide(scroll, "span 2, growx, hmax 160");
		}
		if (!form.showOkCancel(host.dialogParent(), "Add " + basicName)) {
			return;
		}
		particle.setName(name.getText().trim().isEmpty() ? basicName : name.getText().trim());
		particle.setPivotPoint(WizardForm.vertex(position));
		for (int i = 0; i < 3; i++) {
			particle.setSegmentColor(i, new Vertex(colors[i].getBlue() / 255.0, colors[i].getGreen() / 255.0,
					colors[i].getRed() / 255.0));
		}
		particle.setParent((IdObject) parent.getSelectedItem());
		boolean anyHidden = false;
		for (final JCheckBox box : boxes) {
			anyHidden |= !box.isSelected();
		}
		if (anyHidden) {
			AnimFlag oldFlag = particle.getVisibilityFlag();
			if (oldFlag == null) {
				oldFlag = new AnimFlag("Visibility");
			}
			final AnimFlag visibility = AnimFlag.buildEmptyFrom(oldFlag);
			for (int i = 0; i < anims.size(); i++) {
				visibility.addEntry(anims.get(i).getStart(), Integer.valueOf(boxes[i].isSelected() ? 1 : 0));
			}
			particle.setVisibilityFlag(visibility);
		}
		final List<Object> components = new ArrayList<>();
		final Bitmap texture = particle.getTexture();
		if (texture != null) {
			Bitmap existing = null;
			for (final Bitmap candidate : model.getTextures()) {
				if (candidate.getPath().equals(texture.getPath())
						&& (candidate.getReplaceableId() == texture.getReplaceableId())) {
					existing = candidate;
					break;
				}
			}
			if (existing != null) {
				particle.setTexture(existing);
			} else {
				components.add(texture);
			}
		}
		components.add(particle);
		commit(components, "add " + basicName, particle);
	}

	private static int clamp(final double channel) {
		return (int) Math.max(0, Math.min(255, Math.round(channel * 255)));
	}
}
