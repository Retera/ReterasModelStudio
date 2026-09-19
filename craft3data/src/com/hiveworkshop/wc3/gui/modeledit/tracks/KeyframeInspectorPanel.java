package com.hiveworkshop.wc3.gui.modeledit.tracks;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.DefaultListCellRenderer;

import com.hiveworkshop.wc3.gui.modeledit.activity.UndoActionListener;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.animation.SetKeyframeAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.animation.SlideKeyframeByIndexAction;
import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.Bitmap;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.Named;
import com.hiveworkshop.wc3.mdl.QuaternionRotation;
import com.hiveworkshop.wc3.mdl.Vertex;

/**
 * Shows the selected keyframe and lets its time, value and tangents be edited
 * with controls that fit the value type: a number for alpha and other scalars,
 * red/green/blue plus a swatch for colors, X/Y/Z for translation and scaling,
 * quaternion components plus Euler degrees for rotation, and a texture picker
 * for texture ids. Every edit is one undo step.
 */
public final class KeyframeInspectorPanel extends JPanel {
	private final UndoActionListener undoActionListener;
	private final Runnable repaintTracks;
	private EditableModel model;
	private final JLabel header = new JLabel(" ");
	private final JLabel trackLabel = new JLabel(" ");
	private final JLabel interpLabel = new JLabel(" ");
	private final JPanel fields = new JPanel(new GridBagLayout());
	private KeyframeRef current;
	private boolean updating;
	private JSpinner firstValueField;

	public KeyframeInspectorPanel(final UndoActionListener undoActionListener, final Runnable repaintTracks) {
		super(new BorderLayout(0, 4));
		this.undoActionListener = undoActionListener;
		this.repaintTracks = repaintTracks;
		setBorder(BorderFactory.createTitledBorder("Keyframe"));
		final JPanel top = new JPanel(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		top.add(header, c);
		c.gridy++;
		top.add(trackLabel, c);
		c.gridy++;
		top.add(interpLabel, c);
		add(top, BorderLayout.NORTH);
		final JScrollPane scroll = new JScrollPane(fields);
		scroll.setBorder(null);
		add(scroll, BorderLayout.CENTER);
		showNothing();
	}

	public void setModel(final EditableModel model) {
		this.model = model;
	}

	/** Puts keyboard focus into the first value field (double-click on a key). */
	public void focusValue() {
		if (firstValueField != null) {
			SwingUtilities.invokeLater(() -> {
				final Component editor = firstValueField.getEditor();
				if (editor instanceof JSpinner.DefaultEditor) {
					((JSpinner.DefaultEditor) editor).getTextField().requestFocusInWindow();
					((JSpinner.DefaultEditor) editor).getTextField().selectAll();
				}
			});
		}
	}

	public void setSelection(final List<KeyframeRef> refs) {
		KeyframeRef first = null;
		for (final KeyframeRef ref : refs) {
			if (ref.isValid()) {
				first = ref;
				break;
			}
		}
		current = first;
		fields.removeAll();
		firstValueField = null;
		if (first == null) {
			showNothing();
		} else {
			final String owner = first.container instanceof Named ? ((Named) first.container).getName()
					: first.container.getClass().getSimpleName();
			header.setText(refs.size() > 1 ? refs.size() + " keys selected, showing first" : "1 key selected");
			trackLabel.setText(owner + "  >  " + first.track.getName());
			interpLabel.setText("Interpolation: " + first.track.getInterpTypeAsEnum()
					+ (first.track.hasGlobalSeq() ? ", global sequence " + first.track.getGlobalSeq() : ""));
			buildFields(first);
		}
		fields.revalidate();
		fields.repaint();
	}

	private void showNothing() {
		header.setText("No keyframe selected");
		trackLabel.setText("Click a key in the timeline, or right-click a track to insert one.");
		interpLabel.setText(" ");
	}

	// ---- field construction ----

	private GridBagConstraints constraints(final int row) {
		final GridBagConstraints c = new GridBagConstraints();
		c.gridy = row;
		c.insets = new Insets(1, 2, 1, 2);
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		return c;
	}

	private int row;

	private void addRow(final String label, final JComponentSupplier supplier) {
		final GridBagConstraints c = constraints(row++);
		c.gridx = 0;
		c.weightx = 0;
		fields.add(new JLabel(label), c);
		c.gridx = 1;
		c.weightx = 1;
		fields.add(supplier.get(), c);
	}

	private interface JComponentSupplier {
		Component get();
	}

	private void buildFields(final KeyframeRef ref) {
		row = 0;
		final AnimFlag track = ref.track;
		// time
		final SpinnerNumberModel timeModel = new SpinnerNumberModel(ref.time(), -1000000, 10000000, 1);
		final JSpinner timeSpinner = new JSpinner(timeModel);
		timeSpinner.addChangeListener(e -> {
			if (updating || (current == null) || !current.isValid()) {
				return;
			}
			final int newTime = timeModel.getNumber().intValue();
			final int delta = newTime - current.time();
			if (delta != 0) {
				final SlideKeyframeByIndexAction action = new SlideKeyframeByIndexAction(current.track, current.index,
						delta, repaintTracks);
				action.redo();
				undoActionListener.pushAction(action);
			}
		});
		addRow("Time", () -> timeSpinner);
		final Object value = ref.value();
		buildValueEditors("Value", value, ref, 0);
		if (track.tans()) {
			buildValueEditors("In Tangent", ref.inTan(), ref, 1);
			buildValueEditors("Out Tangent", ref.outTan(), ref, 2);
		}
	}

	/**
	 * @param slot 0 value, 1 in tangent, 2 out tangent
	 */
	private void buildValueEditors(final String title, final Object value, final KeyframeRef ref, final int slot) {
		final GridBagConstraints heading = constraints(row++);
		heading.gridx = 0;
		heading.gridwidth = 2;
		final JLabel titleLabel = new JLabel(title);
		titleLabel.setForeground(Color.DARK_GRAY);
		fields.add(titleLabel, heading);
		if (value instanceof Double) {
			final JSpinner spinner = numberSpinner((Double) value, 0.01);
			spinner.addChangeListener(e -> commit(ref, slot, ((Number) spinner.getValue()).doubleValue()));
			rememberFirst(spinner);
			addRow("", () -> spinner);
		} else if (value instanceof Integer) {
			final JSpinner spinner = new JSpinner(new SpinnerNumberModel(((Integer) value).intValue(), -1, 100000, 1));
			final JLabel nameLabel = new JLabel(textureName((Integer) value));
			spinner.addChangeListener(e -> {
				commit(ref, slot, Integer.valueOf(((Number) spinner.getValue()).intValue()));
				nameLabel.setText(textureName(((Number) spinner.getValue()).intValue()));
			});
			rememberFirst(spinner);
			addRow("Id", () -> spinner);
			if (ref.track.getName().endsWith("TextureID") && (model != null)) {
				addRow("", () -> nameLabel);
				final JButton choose = new JButton("Choose texture...");
				choose.addActionListener(e -> {
					final Integer picked = chooseTexture(((Number) spinner.getValue()).intValue());
					if (picked != null) {
						spinner.setValue(picked);
					}
				});
				addRow("", () -> choose);
			}
		} else if (value instanceof Vertex) {
			final Vertex vertex = (Vertex) value;
			final boolean color = ref.track.getName().equals("Color") || ref.track.getName().endsWith("Color");
			final String[] labels = color ? new String[] { "Red", "Green", "Blue" } : new String[] { "X", "Y", "Z" };
			final JSpinner x = numberSpinner(vertex.x, color ? 0.01 : 1.0);
			final JSpinner y = numberSpinner(vertex.y, color ? 0.01 : 1.0);
			final JSpinner z = numberSpinner(vertex.z, color ? 0.01 : 1.0);
			final Runnable apply = () -> commit(ref, slot, new Vertex(((Number) x.getValue()).doubleValue(),
					((Number) y.getValue()).doubleValue(), ((Number) z.getValue()).doubleValue()));
			x.addChangeListener(e -> apply.run());
			y.addChangeListener(e -> apply.run());
			z.addChangeListener(e -> apply.run());
			rememberFirst(x);
			addRow(labels[0], () -> x);
			addRow(labels[1], () -> y);
			addRow(labels[2], () -> z);
			if (color && (slot == 0)) {
				final JButton swatch = new JButton("Pick color...");
				swatch.setBackground(clampColor(vertex));
				swatch.setOpaque(true);
				swatch.addActionListener(e -> {
					final Color chosen = JColorChooser.showDialog(this, "Keyframe color", clampColor(vertex));
					if (chosen != null) {
						updating = true;
						x.setValue(chosen.getRed() / 255.0);
						y.setValue(chosen.getGreen() / 255.0);
						z.setValue(chosen.getBlue() / 255.0);
						updating = false;
						apply.run();
						swatch.setBackground(chosen);
					}
				});
				addRow("", () -> swatch);
			}
		} else if (value instanceof QuaternionRotation) {
			final QuaternionRotation q = (QuaternionRotation) value;
			final JSpinner qx = numberSpinner(q.a, 0.01);
			final JSpinner qy = numberSpinner(q.b, 0.01);
			final JSpinner qz = numberSpinner(q.c, 0.01);
			final JSpinner qw = numberSpinner(q.d, 0.01);
			final Vertex euler = q.toEuler();
			final JSpinner ex = numberSpinner(Math.toDegrees(euler.x), 1.0);
			final JSpinner ey = numberSpinner(Math.toDegrees(euler.y), 1.0);
			final JSpinner ez = numberSpinner(Math.toDegrees(euler.z), 1.0);
			final Runnable applyQuaternion = () -> {
				if (updating) {
					return;
				}
				final QuaternionRotation rotation = new QuaternionRotation(((Number) qx.getValue()).doubleValue(),
						((Number) qy.getValue()).doubleValue(), ((Number) qz.getValue()).doubleValue(),
						((Number) qw.getValue()).doubleValue());
				commit(ref, slot, rotation);
				final Vertex e = rotation.toEuler();
				updating = true;
				ex.setValue(Math.toDegrees(e.x));
				ey.setValue(Math.toDegrees(e.y));
				ez.setValue(Math.toDegrees(e.z));
				updating = false;
			};
			final Runnable applyEuler = () -> {
				if (updating) {
					return;
				}
				final QuaternionRotation rotation = new QuaternionRotation(
						new Vertex(Math.toRadians(((Number) ex.getValue()).doubleValue()),
								Math.toRadians(((Number) ey.getValue()).doubleValue()),
								Math.toRadians(((Number) ez.getValue()).doubleValue())));
				commit(ref, slot, rotation);
				updating = true;
				qx.setValue(rotation.a);
				qy.setValue(rotation.b);
				qz.setValue(rotation.c);
				qw.setValue(rotation.d);
				updating = false;
			};
			for (final JSpinner s : new JSpinner[] { qx, qy, qz, qw }) {
				s.addChangeListener(e -> applyQuaternion.run());
			}
			for (final JSpinner s : new JSpinner[] { ex, ey, ez }) {
				s.addChangeListener(e -> applyEuler.run());
			}
			rememberFirst(ex);
			addRow("Euler X (deg)", () -> ex);
			addRow("Euler Y (deg)", () -> ey);
			addRow("Euler Z (deg)", () -> ez);
			addRow("Quat X", () -> qx);
			addRow("Quat Y", () -> qy);
			addRow("Quat Z", () -> qz);
			addRow("Quat W", () -> qw);
		} else {
			addRow("", () -> new JLabel(String.valueOf(value)));
		}
	}

	private void rememberFirst(final JSpinner spinner) {
		if (firstValueField == null) {
			firstValueField = spinner;
		}
	}

	private static Color clampColor(final Vertex v) {
		return new Color(clamp(v.x), clamp(v.y), clamp(v.z));
	}

	private static float clamp(final double d) {
		return (float) Math.max(0, Math.min(1, d));
	}

	private static JSpinner numberSpinner(final double value, final double step) {
		final JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, -1e9, 1e9, step));
		spinner.setEditor(new JSpinner.NumberEditor(spinner, "0.####"));
		return spinner;
	}

	private String textureName(final int id) {
		if ((model == null) || (id < 0) || (id >= model.getTextures().size())) {
			return "(no texture " + id + ")";
		}
		return model.getTextures().get(id).getName();
	}

	private Integer chooseTexture(final int currentId) {
		final List<Bitmap> textures = new ArrayList<>(model.getTextures());
		final JList<Bitmap> list = new JList<>(textures.toArray(new Bitmap[0]));
		list.setCellRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(final JList<?> l, final Object v, final int index,
					final boolean isSelected, final boolean cellHasFocus) {
				return super.getListCellRendererComponent(l, index + ": " + ((Bitmap) v).getName(), index,
						isSelected, cellHasFocus);
			}
		});
		if ((currentId >= 0) && (currentId < textures.size())) {
			list.setSelectedIndex(currentId);
		}
		final int choice = JOptionPane.showConfirmDialog(this, new JScrollPane(list), "Texture for this key",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if ((choice != JOptionPane.OK_OPTION) || (list.getSelectedIndex() < 0)) {
			return null;
		}
		return list.getSelectedIndex();
	}

	// ---- committing ----

	/**
	 * Writes one slot (value, in tangent or out tangent) of the current key as an
	 * undoable SetKeyframeAction that carries the full old and new triple.
	 */
	private void commit(final KeyframeRef ref, final int slot, final Object newValue) {
		if (updating || !ref.isValid()) {
			return;
		}
		final AnimFlag track = ref.track;
		final Object oldValue = AnimFlag.cloneValue(ref.value());
		final Object oldIn = AnimFlag.cloneValue(ref.inTan());
		final Object oldOut = AnimFlag.cloneValue(ref.outTan());
		Object value = AnimFlag.cloneValue(ref.value());
		Object in = AnimFlag.cloneValue(ref.inTan());
		Object out = AnimFlag.cloneValue(ref.outTan());
		if (slot == 0) {
			value = newValue;
		} else if (slot == 1) {
			in = newValue;
		} else {
			out = newValue;
		}
		final SetKeyframeAction action;
		if (track.tans()) {
			action = new SetKeyframeAction(ref.container, track, ref.time(), value, in, out, oldValue, oldIn, oldOut,
					repaintTracks);
		} else {
			action = new SetKeyframeAction(ref.container, track, ref.time(), value, oldValue, repaintTracks);
		}
		action.redo();
		undoActionListener.pushAction(action);
	}
}
