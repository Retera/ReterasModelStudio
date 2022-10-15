package com.hiveworkshop.rms.util.TwiTextEditor.table;

import javax.swing.*;
import java.awt.*;

public class TwiTable extends JTable {

	// when the viewport shrinks below the preferred size, stop tracking the viewport width
	public boolean getScrollableTracksViewportWidth() {
		if (autoResizeMode != AUTO_RESIZE_OFF) {
			if (getParent() instanceof JViewport) {
				return (((JViewport)getParent()).getWidth() > getPreferredSize().width);
			}
		}
		return false;
	}

	// when the viewport shrinks below the preferred size, return the minimum size
// so that scrollbars will be shown
	public Dimension getPreferredSize() {
		if (getParent() instanceof JViewport) {
			if (((JViewport)getParent()).getWidth() < super.getPreferredSize().width) {
				return getMinimumSize();
			}
		}

		return super.getPreferredSize();
	}
}
