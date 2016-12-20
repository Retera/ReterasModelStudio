package swife;

import javax.swing.JPanel;

public class ViewportImpl implements Viewport {

	private final JPanel panel;

	public ViewportImpl() {
		panel = new JPanel();
	}

	@Override
	public JPanel getPanel() {
		return panel;
	}

}
