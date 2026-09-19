package com.hiveworkshop.wc3.gui.modeledit.components.cards;

import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.hiveworkshop.wc3.gui.modeledit.componenttree.ModelComponentNavigationListener;
import com.hiveworkshop.wc3.mdl.AnimFlag;

import net.miginfocom.swing.MigLayout;

/**
 * Read-only list of a component's tracks (name, key count, interpolation)
 * with an "Open" button per track that jumps to the Tracks view. Keyframes
 * are edited there, not in the cards.
 */
public class TrackSummaryPanel extends JPanel {
	public TrackSummaryPanel() {
		setBorder(BorderFactory.createTitledBorder("Tracks"));
		setLayout(new MigLayout("fillx, insets 4, wrap 3", "[][grow][]", ""));
	}

	public void setTracks(final List<AnimFlag> tracks, final ModelComponentNavigationListener navigationListener) {
		clear();
		addGroup("", tracks, navigationListener);
		finish();
	}

	/** Start a multi-group listing; follow with addGroup(...) calls and finish(). */
	public void clear() {
		removeAll();
	}

	public void addGroup(final String prefix, final List<AnimFlag> tracks,
			final ModelComponentNavigationListener navigationListener) {
		if (tracks == null) {
			return;
		}
		for (final AnimFlag track : tracks) {
			add(new JLabel(prefix + track.getName()));
			add(new JLabel(describe(track)), "growx");
			final JButton open = new JButton("Open in Tracks");
			open.addActionListener(e -> navigationListener.openInTracks(track));
			add(open);
		}
	}

	public void finish() {
		if (getComponentCount() == 0) {
			add(new JLabel("No tracks; every property is static."), "span 3");
		}
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
}
