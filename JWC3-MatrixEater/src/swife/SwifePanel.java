package swife;

import javax.swing.JPanel;

public class SwifePanel extends JPanel {
	private final Viewport viewport;

	public SwifePanel() {
		viewport = new ViewportImpl();
		add(viewport.getPanel());
	}
}
