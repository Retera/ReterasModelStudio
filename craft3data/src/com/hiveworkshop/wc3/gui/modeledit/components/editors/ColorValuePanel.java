package com.hiveworkshop.wc3.gui.modeledit.components.editors;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.timeline.SetFloat3StaticValueAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.activity.UndoActionListener;
import com.hiveworkshop.wc3.gui.modeledit.componenttree.ModelComponentNavigationListener;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.animation.AddTimelineAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.animation.RemoveTimelineAction;
import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.TimelineContainer;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.util.Callback;
import com.hiveworkshop.wc3.util.IconUtils;

import net.miginfocom.swing.MigLayout;

/**
 * Editor for a colour property that is either static or driven by a track.
 * Colours are stored as BGR vertices (x = blue) like everywhere else in the
 * model classes. See {@link FloatValuePanel} for the static/dynamic contract.
 */
public class ColorValuePanel extends JPanel {
	private static final Vertex DEFAULT_COLOR = new Vertex(1, 1, 1);
	private final String title;
	private final JButton staticColorButton;
	private final JLabel trackSummary;
	private final JLabel trackLabel;
	private final JPanel trackButtons;
	private final JButton openInTracks;
	private final JButton makeStatic;
	private final JButton makeDynamic;
	private UndoActionListener undoActionListener;
	private ModelStructureChangeListener modelStructureChangeListener;
	private ModelComponentNavigationListener navigationListener = ModelComponentNavigationListener.NONE;
	private AnimFlag colorTrack;
	private TimelineContainer container;
	private String trackName;
	private Vertex lastLoadedStaticColor = DEFAULT_COLOR;
	private Callback<Vertex> setter;

	public ColorValuePanel(final String title) {
		this.title = title;
		setBorder(BorderFactory.createTitledBorder(title));
		setLayout(new MigLayout("fillx, insets 2, hidemode 3", "[][grow][]", ""));
		staticColorButton = new JButton("Choose Color");
		staticColorButton.addActionListener(e -> chooseColor());
		trackSummary = new JLabel();
		openInTracks = new JButton("Open in Tracks");
		openInTracks.addActionListener(e -> navigationListener.openInTracks(colorTrack));
		makeStatic = new JButton("Make Static");
		makeStatic.addActionListener(e -> makeStatic());
		makeDynamic = new JButton("Make Dynamic");
		makeDynamic.addActionListener(e -> makeDynamic());

		add(new JLabel("Static:"));
		add(staticColorButton, "growx");
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

	private void chooseColor() {
		if (setter == null) {
			return;
		}
		final Color newColor = JColorChooser.showDialog(getRootPane(), "Choose " + title,
				new Color(clamp(lastLoadedStaticColor.z), clamp(lastLoadedStaticColor.y),
						clamp(lastLoadedStaticColor.x)));
		if (newColor != null) {
			final SetFloat3StaticValueAction action = new SetFloat3StaticValueAction(title, lastLoadedStaticColor,
					new Vertex(newColor.getBlue() / 255f, newColor.getGreen() / 255f, newColor.getRed() / 255f),
					setter);
			action.redo();
			undoActionListener.pushAction(action);
		}
	}

	private static float clamp(final double channel) {
		return (float) Math.max(0, Math.min(1, channel));
	}

	/**
	 * Legacy entry point without a container: track buttons stay hidden.
	 */
	public void reloadNewValue(final Vertex color, final Callback<Vertex> setter, final AnimFlag colorTrack,
			final UndoActionListener undoActionListener,
			final ModelStructureChangeListener modelStructureChangeListener) {
		reloadNewValue(color, setter, colorTrack, null, colorTrack == null ? title : colorTrack.getName(),
				undoActionListener, modelStructureChangeListener);
	}

	public void reloadNewValue(final Vertex color, final Callback<Vertex> setter, final AnimFlag colorTrack,
			final TimelineContainer container, final String trackName, final UndoActionListener undoActionListener,
			final ModelStructureChangeListener modelStructureChangeListener) {
		this.setter = setter;
		this.colorTrack = colorTrack;
		this.container = container;
		this.trackName = trackName;
		this.undoActionListener = undoActionListener;
		this.modelStructureChangeListener = modelStructureChangeListener;
		lastLoadedStaticColor = color != null ? color : DEFAULT_COLOR;
		staticColorButton.setIcon(new ImageIcon(IconUtils.createColorImage(lastLoadedStaticColor, 24, 24)));
		final boolean animated = colorTrack != null;
		staticColorButton.setEnabled(!animated);
		trackSummary.setText(animated ? FloatValuePanel.describe(colorTrack) : "");
		trackLabel.setVisible(animated);
		trackSummary.setVisible(animated);
		trackButtons.setVisible(animated);
		openInTracks.setVisible(animated);
		makeStatic.setVisible(animated && (container != null));
		makeDynamic.setVisible(!animated && (container != null));
		revalidate();
		repaint();
	}

	private void makeStatic() {
		if ((colorTrack == null) || (container == null)) {
			return;
		}
		final RemoveTimelineAction action = new RemoveTimelineAction(container, colorTrack,
				modelStructureChangeListener);
		action.redo();
		undoActionListener.pushAction(action);
	}

	private void makeDynamic() {
		if ((colorTrack != null) || (container == null)) {
			return;
		}
		final AnimFlag track = new AnimFlag(trackName);
		track.addEntry(0, new Vertex(lastLoadedStaticColor));
		final AddTimelineAction action = new AddTimelineAction(container, track, modelStructureChangeListener);
		action.redo();
		undoActionListener.pushAction(action);
		navigationListener.openInTracks(track);
	}
}
