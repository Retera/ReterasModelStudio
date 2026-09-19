package com.hiveworkshop.wc3.gui.modeledit.components.cards;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.SetComponentPropertyAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.activity.UndoActionListener;
import com.hiveworkshop.wc3.gui.modeledit.components.editors.ColorValuePanel;
import com.hiveworkshop.wc3.gui.modeledit.components.editors.ComponentEditorJSpinner;
import com.hiveworkshop.wc3.gui.modeledit.components.editors.ComponentEditorTextField;
import com.hiveworkshop.wc3.gui.modeledit.components.editors.FloatValuePanel;
import com.hiveworkshop.wc3.gui.modeledit.componenttree.ModelComponentNavigationListener;
import com.hiveworkshop.wc3.util.IconUtils;
import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.TimelineContainer;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.ModelViewManager;
import com.hiveworkshop.wc3.util.Callback;

import net.miginfocom.swing.MigLayout;

/**
 * Base class for the Model tab editor cards. A card is a two column MigLayout
 * form; subclasses declare their rows with the add* helpers, each of which
 * binds a getter (used on reload) and a setter (wrapped in an undoable
 * {@link SetComponentPropertyAction} when the user commits a change).
 *
 * Every committed change fires
 * {@link ModelStructureChangeListener#componentChanged(Object)} so the trees,
 * viewports and this card itself refresh from the model.
 */
public abstract class ComponentCard<T> extends JPanel {
	protected T item;
	protected ModelViewManager modelViewManager;
	protected UndoActionListener undoListener;
	protected ModelStructureChangeListener changeListener;
	protected ModelComponentNavigationListener navigationListener = ModelComponentNavigationListener.NONE;
	private boolean loading;
	private final List<Runnable> reloaders = new ArrayList<>();
	private JPanel target;

	protected ComponentCard() {
		setLayout(new MigLayout("fillx, wrap 2, insets 8", "[right]8[grow,fill]", ""));
		target = this;
	}

	public void setNavigationListener(final ModelComponentNavigationListener navigationListener) {
		this.navigationListener = navigationListener == null ? ModelComponentNavigationListener.NONE
				: navigationListener;
	}

	public void setItem(final T item, final ModelViewManager modelViewManager, final UndoActionListener undoListener,
			final ModelStructureChangeListener changeListener) {
		this.item = item;
		this.modelViewManager = modelViewManager;
		this.undoListener = undoListener;
		this.changeListener = changeListener;
		reload();
	}

	public T getItem() {
		return item;
	}

	protected EditableModel model() {
		return modelViewManager.getModel();
	}

	/** Re-reads every bound control from the model. */
	public void reload() {
		if (item == null) {
			return;
		}
		loading = true;
		try {
			for (final Runnable reloader : reloaders) {
				reloader.run();
			}
			onReload();
		} finally {
			loading = false;
		}
		revalidate();
		repaint();
	}

	/** Hook for subclasses with controls that the helpers do not cover. */
	protected void onReload() {
	}

	protected boolean isLoading() {
		return loading;
	}

	protected void notifyChanged() {
		changeListener.componentChanged(item);
	}

	/**
	 * Commits a change as an undoable action. No-op while loading or when the
	 * value did not change.
	 */
	protected <V> void apply(final String actionName, final V oldValue, final V newValue, final Callback<V> setter) {
		if (loading || Objects.equals(oldValue, newValue)) {
			return;
		}
		final T target = item;
		final ModelStructureChangeListener listener = changeListener;
		final SetComponentPropertyAction<V> action = new SetComponentPropertyAction<>(actionName, oldValue, newValue,
				setter, () -> listener.componentChanged(target));
		action.redo();
		undoListener.pushAction(action);
	}

	// ---- layout helpers -------------------------------------------------

	protected void addRow(final String label, final JComponent component) {
		addRow(label, component, "growx");
	}

	protected void addRow(final String label, final JComponent component, final String constraints) {
		if (label == null) {
			target.add(component, "span 2, " + constraints);
		} else {
			target.add(new JLabel(label));
			target.add(component, constraints);
		}
	}

	protected void addWide(final JComponent component) {
		target.add(component, "span 2, growx");
	}

	protected void addWide(final JComponent component, final String constraints) {
		target.add(component, "span 2, " + constraints);
	}

	/** Starts a titled group; rows added afterwards go into the group. */
	protected JPanel beginSection(final String title) {
		final JPanel section = new JPanel(new MigLayout("fillx, wrap 2, insets 4", "[right]8[grow,fill]", ""));
		section.setBorder(BorderFactory.createTitledBorder(title));
		add(section, "span 2, growx");
		target = section;
		return section;
	}

	protected void endSection() {
		target = this;
	}

	protected void addReloader(final Runnable reloader) {
		reloaders.add(reloader);
	}

	// ---- bound controls -------------------------------------------------

	protected JLabel addInfo(final String label, final Function<T, String> getter) {
		final JLabel value = new JLabel();
		addReloader(() -> value.setText(getter.apply(item)));
		addRow(label, value);
		return value;
	}

	protected ComponentEditorTextField addTextField(final String label, final Function<T, String> getter,
			final BiConsumer<T, String> setter) {
		final ComponentEditorTextField field = new ComponentEditorTextField(24);
		field.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final T target = item;
				apply(label, getter.apply(target), field.getText(), value -> setter.accept(target, value));
			}
		});
		addReloader(() -> field.reloadNewValue(nullToEmpty(getter.apply(item))));
		addRow(label, field);
		return field;
	}

	protected ComponentEditorJSpinner addIntSpinner(final String label, final int min, final int max,
			final Function<T, Integer> getter, final BiConsumer<T, Integer> setter) {
		final SpinnerNumberModel model = new SpinnerNumberModel(Math.max(min, Math.min(max, 0)), min, max, 1);
		final ComponentEditorJSpinner spinner = new ComponentEditorJSpinner(model);
		spinner.addActionListener(() -> {
			final T target = item;
			apply(label, getter.apply(target), ((Number) spinner.getValue()).intValue(),
					value -> setter.accept(target, value));
		});
		addReloader(() -> {
			final Integer value = getter.apply(item);
			// files in the wild hold values outside the sensible range; widen rather than throw
			if (value < ((Number) model.getMinimum()).intValue()) {
				model.setMinimum(value);
			}
			if (value > ((Number) model.getMaximum()).intValue()) {
				model.setMaximum(value);
			}
			spinner.reloadNewValue(value);
		});
		addRow(label, spinner, "wmax 220, alignx left");
		return spinner;
	}

	protected ComponentEditorJSpinner addDoubleSpinner(final String label, final double step,
			final Function<T, Double> getter, final BiConsumer<T, Double> setter) {
		final ComponentEditorJSpinner spinner = newDoubleSpinner(step);
		spinner.addActionListener(() -> {
			final T target = item;
			apply(label, getter.apply(target), ((Number) spinner.getValue()).doubleValue(),
					value -> setter.accept(target, value));
		});
		addReloader(() -> spinner.reloadNewValue(getter.apply(item)));
		addRow(label, spinner, "wmax 220, alignx left");
		return spinner;
	}

	protected JCheckBox addCheckBox(final String label, final Predicate<T> getter,
			final BiConsumer<T, Boolean> setter) {
		final JCheckBox box = new JCheckBox(label);
		box.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final T target = item;
				apply(label, getter.test(target), box.isSelected(), value -> setter.accept(target, value));
			}
		});
		addReloader(() -> box.setSelected(getter.test(item)));
		target.add(box, "span 2, alignx left");
		return box;
	}

	/**
	 * Check box bound to the presence of a string in a node's flag list.
	 */
	protected JCheckBox addFlagCheckBox(final String flag, final Function<T, List<String>> flags) {
		return addCheckBox(flag, target -> flags.apply(target).contains(flag), (target, selected) -> {
			final List<String> list = flags.apply(target);
			list.remove(flag);
			if (selected) {
				list.add(flag);
			}
		});
	}

	protected <E> JComboBox<E> addComboBox(final String label, final Supplier<List<E>> options,
			final Function<E, String> display, final Function<T, E> getter, final BiConsumer<T, E> setter) {
		final JComboBox<E> combo = new JComboBox<>();
		combo.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(final JList<?> list, final Object value,
					final int index, final boolean isSelected, final boolean cellHasFocus) {
				@SuppressWarnings("unchecked")
				final String text = value == null ? "(none)" : display.apply((E) value);
				return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
			}
		});
		combo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (loading) {
					return;
				}
				final T target = item;
				@SuppressWarnings("unchecked")
				final E selected = (E) combo.getSelectedItem();
				apply(label, getter.apply(target), selected, value -> setter.accept(target, value));
			}
		});
		addReloader(() -> {
			combo.removeAllItems();
			for (final E option : options.get()) {
				combo.addItem(option);
			}
			combo.setSelectedItem(getter.apply(item));
		});
		addRow(label, combo);
		return combo;
	}

	/** Three spinners bound to a Vertex valued property. */
	protected JPanel addVertexRow(final String label, final double step, final Function<T, Vertex> getter,
			final BiConsumer<T, Vertex> setter) {
		final JPanel row = new JPanel(new MigLayout("insets 0, fillx", "[grow,fill][grow,fill][grow,fill]", ""));
		final ComponentEditorJSpinner[] spinners = new ComponentEditorJSpinner[3];
		for (int i = 0; i < 3; i++) {
			spinners[i] = newDoubleSpinner(step);
			spinners[i].addActionListener(() -> {
				final T target = item;
				final Vertex oldValue = getter.apply(target);
				final Vertex newValue = new Vertex(spinnerValue(spinners[0]), spinnerValue(spinners[1]),
						spinnerValue(spinners[2]));
				apply(label, oldValue == null ? null : new Vertex(oldValue), newValue,
						value -> setter.accept(target, value == null ? null : new Vertex(value)));
			});
			row.add(spinners[i]);
		}
		addReloader(() -> {
			final Vertex value = getter.apply(item);
			spinners[0].reloadNewValue(value == null ? 0.0 : value.x);
			spinners[1].reloadNewValue(value == null ? 0.0 : value.y);
			spinners[2].reloadNewValue(value == null ? 0.0 : value.z);
		});
		addRow(label, row);
		return row;
	}

	/**
	 * Colour swatch button bound to a Vertex colour stored BGR (x = blue), the
	 * convention used throughout the MDL model classes.
	 */
	protected JButton addColorButton(final String label, final Function<T, Vertex> getter,
			final BiConsumer<T, Vertex> setter) {
		final JButton button = new JButton("Choose Color");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final T target = item;
				final Vertex current = getter.apply(target);
				final Color initial = current == null ? Color.WHITE
						: new Color(clamp(current.z), clamp(current.y), clamp(current.x));
				final Color chosen = JColorChooser.showDialog(getRootPane(), "Choose " + label, initial);
				if (chosen != null) {
					apply(label, current == null ? null : new Vertex(current),
							new Vertex(chosen.getBlue() / 255.0, chosen.getGreen() / 255.0, chosen.getRed() / 255.0),
							value -> setter.accept(target, value));
				}
			}
		});
		addReloader(() -> {
			final Vertex value = getter.apply(item);
			button.setIcon(new ImageIcon(
					IconUtils.createColorImage(value == null ? new Vertex(1, 1, 1) : value, 24, 24)));
		});
		addRow(label, button, "alignx left");
		return button;
	}

	/**
	 * Static float value that may also be animated by a same-named track.
	 */
	protected FloatValuePanel addFloatValue(final String title, final Function<T, Double> getter,
			final BiConsumer<T, Double> setter, final Function<T, TimelineContainer> container,
			final Function<T, List<AnimFlag>> tracks, final String trackName) {
		final FloatValuePanel panel = new FloatValuePanel(title);
		panel.setNavigationListener(navigationListener);
		addReloader(() -> {
			final T target = item;
			panel.setNavigationListener(navigationListener);
			panel.reloadNewValue(getter.apply(target).floatValue(), value -> {
				setter.accept(target, value.doubleValue());
				changeListener.componentChanged(target);
			}, AnimFlag.find(tracks.apply(target), trackName), container.apply(target), trackName, undoListener,
					changeListener);
		});
		addWide(panel);
		return panel;
	}

	/**
	 * Static colour (BGR Vertex) that may also be animated by a same-named
	 * track.
	 */
	protected ColorValuePanel addColorValue(final String title, final Function<T, Vertex> getter,
			final BiConsumer<T, Vertex> setter, final Function<T, TimelineContainer> container,
			final Function<T, List<AnimFlag>> tracks, final String trackName) {
		final ColorValuePanel panel = new ColorValuePanel(title);
		addReloader(() -> {
			final T target = item;
			panel.setNavigationListener(navigationListener);
			panel.reloadNewValue(getter.apply(target), value -> {
				setter.accept(target, value);
				changeListener.componentChanged(target);
			}, AnimFlag.find(tracks.apply(target), trackName), container.apply(target), trackName, undoListener,
					changeListener);
		});
		addWide(panel);
		return panel;
	}

	protected JButton addButton(final String text, final Runnable action) {
		final JButton button = new JButton(text);
		button.addActionListener(e -> action.run());
		return button;
	}

	// ---- small utilities ------------------------------------------------

	protected static ComponentEditorJSpinner newDoubleSpinner(final double step) {
		final ComponentEditorJSpinner spinner = new ComponentEditorJSpinner(
				new SpinnerNumberModel(0.0, -Double.MAX_VALUE, Double.MAX_VALUE, step));
		// a spinner with an unbounded model asks for unbounded width; size it like a
		// long-range spinner instead
		final JSpinner standin = new JSpinner(new SpinnerNumberModel(1.0, -Long.MAX_VALUE, Long.MAX_VALUE, 1.0));
		spinner.setPreferredSize(standin.getPreferredSize());
		spinner.setMinimumSize(standin.getMinimumSize());
		return spinner;
	}

	protected static double spinnerValue(final JSpinner spinner) {
		return ((Number) spinner.getValue()).doubleValue();
	}

	protected static String nullToEmpty(final String value) {
		return value == null ? "" : value;
	}

	private static int clamp(final double channel) {
		return (int) Math.max(0, Math.min(255, Math.round(channel * 255)));
	}
}
