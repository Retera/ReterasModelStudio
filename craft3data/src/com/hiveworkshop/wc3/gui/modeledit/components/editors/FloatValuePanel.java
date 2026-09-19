package com.hiveworkshop.wc3.gui.modeledit.components.editors;

import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.timeline.SetFloatStaticValueAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.activity.UndoActionListener;
import com.hiveworkshop.wc3.gui.modeledit.componenttree.ModelComponentNavigationListener;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.animation.AddTimelineAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.animation.RemoveTimelineAction;
import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.TimelineContainer;
import com.hiveworkshop.wc3.util.Callback;

import net.miginfocom.swing.MigLayout;

/**
 * Editor for a float property that is either static or driven by a track.
 * <p>
 * The static value is edited here. When a track exists the card only
 * summarises it (key count, interpolation, global sequence) and offers "Open in
 * Tracks" and "Make Static"; keyframes themselves are edited in the Tracks
 * view. "Make Dynamic" creates a one-key track holding the static value and
 * jumps to it.
 */
public class FloatValuePanel extends JPanel {
	private final String title;
	private final ComponentEditorJSpinner staticSpinner;
	private final JLabel trackSummary;
	private final JLabel trackLabel;
	private final JPanel trackButtons;
	private final JButton openInTracks;
	private final JButton makeStatic;
	private final JButton makeDynamic;
	private UndoActionListener undoActionListener;
	private ModelStructureChangeListener modelStructureChangeListener;
	private ModelComponentNavigationListener navigationListener = ModelComponentNavigationListener.NONE;
	private AnimFlag valueTrack;
	private TimelineContainer container;
	private String trackName;
	private Callback<Float> valueSetter;
	private float lastLoadedValue;

	public FloatValuePanel(final String title) {
		this.title = title;
		setBorder(BorderFactory.createTitledBorder(title));
		setLayout(new MigLayout("fillx, insets 2, hidemode 3", "[][grow][]", ""));
		staticSpinner = new ComponentEditorJSpinner(
				new SpinnerNumberModel(1.0, -Double.MAX_VALUE, Double.MAX_VALUE, 0.01));
		// a spinner with an unbounded model asks for unbounded width
		final JSpinner standinGuiSpinner = new JSpinner(
				new SpinnerNumberModel(1.0, -Long.MAX_VALUE, Long.MAX_VALUE, 1.0));
		staticSpinner.setPreferredSize(standinGuiSpinner.getPreferredSize());
		staticSpinner.setMaximumSize(standinGuiSpinner.getMaximumSize());
		staticSpinner.setMinimumSize(standinGuiSpinner.getMinimumSize());
		staticSpinner.addActionListener(() -> {
			if (staticSpinner.isEnabled() && (valueSetter != null)) {
				final float newValue = ((Number) staticSpinner.getValue()).floatValue();
				if (newValue == lastLoadedValue) {
					return;
				}
				final SetFloatStaticValueAction action = new SetFloatStaticValueAction(title, lastLoadedValue,
						newValue, valueSetter);
				action.redo();
				undoActionListener.pushAction(action);
			}
		});
		trackSummary = new JLabel();
		openInTracks = new JButton("Open in Tracks");
		openInTracks.addActionListener(e -> navigationListener.openInTracks(valueTrack));
		makeStatic = new JButton("Make Static");
		makeStatic.addActionListener(e -> makeStatic());
		makeDynamic = new JButton("Make Dynamic");
		makeDynamic.addActionListener(e -> makeDynamic());

		add(new JLabel("Static:"));
		add(staticSpinner, "growx");
		add(makeDynamic, "wrap");
		trackLabel = new JLabel("Track:");
		add(trackLabel);
		add(trackSummary, "growx");
		trackButtons = new JPanel(new MigLayout("insets 0", "[][]", ""));
		trackButtons.add(openInTracks);
		trackButtons.add(makeStatic);
		add(trackButtons, "wrap");
	}

	public void setNavigationListener(final ModelComponentNavigationListener navigationListener) {
		this.navigationListener = navigationListener == null ? ModelComponentNavigationListener.NONE
				: navigationListener;
	}

	/**
	 * Legacy entry point without a container: track buttons stay hidden.
	 */
	public void reloadNewValue(final float value, final Callback<Float> valueSetter, final AnimFlag valueTrack,
			final UndoActionListener undoActionListener,
			final ModelStructureChangeListener modelStructureChangeListener) {
		reloadNewValue(value, valueSetter, valueTrack, null, valueTrack == null ? title : valueTrack.getName(),
				undoActionListener, modelStructureChangeListener);
	}

	public void reloadNewValue(final float value, final Callback<Float> valueSetter, final AnimFlag valueTrack,
			final TimelineContainer container, final String trackName, final UndoActionListener undoActionListener,
			final ModelStructureChangeListener modelStructureChangeListener) {
		this.lastLoadedValue = value;
		this.valueTrack = valueTrack;
		this.container = container;
		this.trackName = trackName;
		this.valueSetter = valueSetter;
		this.undoActionListener = undoActionListener;
		this.modelStructureChangeListener = modelStructureChangeListener;
		staticSpinner.reloadNewValue(Double.valueOf(value));
		final boolean animated = valueTrack != null;
		staticSpinner.setEnabled(!animated);
		trackSummary.setText(animated ? describe(valueTrack) : "");
		trackLabel.setVisible(animated);
		trackSummary.setVisible(animated);
		trackButtons.setVisible(animated);
		openInTracks.setVisible(animated);
		makeStatic.setVisible(animated && (container != null));
		makeDynamic.setVisible(!animated && (container != null));
		revalidate();
		repaint();
	}

	static String describe(final AnimFlag track) {
		final StringBuilder sb = new StringBuilder();
		sb.append(track.size()).append(track.size() == 1 ? " key, " : " keys, ");
		sb.append(track.getInterpTypeAsEnum());
		if (track.hasGlobalSeq() && (track.getGlobalSeq() != null)) {
			sb.append(", GlobalSeq ").append(track.getGlobalSeq());
		}
		return sb.toString();
	}

	private void makeStatic() {
		if ((valueTrack == null) || (container == null)) {
			return;
		}
		final RemoveTimelineAction action = new RemoveTimelineAction(container, valueTrack,
				modelStructureChangeListener);
		action.redo();
		undoActionListener.pushAction(action);
	}

	private void makeDynamic() {
		if ((valueTrack != null) || (container == null)) {
			return;
		}
		final AnimFlag track = new AnimFlag(trackName);
		track.addEntry(0, Double.valueOf(lastLoadedValue));
		final AddTimelineAction action = new AddTimelineAction(container, track, modelStructureChangeListener);
		action.redo();
		undoActionListener.pushAction(action);
		navigationListener.openInTracks(track);
	}

	/** Convenience for callers that keep flags in a plain list. */
	public static AnimFlag findTrack(final List<AnimFlag> flags, final String name) {
		return flags == null ? null : AnimFlag.find(flags, name);
	}
}
