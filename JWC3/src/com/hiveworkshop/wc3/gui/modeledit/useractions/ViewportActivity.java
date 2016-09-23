package com.hiveworkshop.wc3.gui.modeledit.useractions;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

public interface ViewportActivity {
	void mousePressed(MouseEvent e);

	void mouseReleased(MouseEvent e);

	void mouseMoved(MouseEvent e);

	void mouseDragged(MouseEvent e);

	void render(Graphics2D g);
}
