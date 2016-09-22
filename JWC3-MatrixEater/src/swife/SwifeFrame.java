package swife;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.hiveworkshop.wc3.gui.GlobalIcons;

public class SwifeFrame extends JFrame {
	public SwifeFrame() {
		super("SwifeMDL Pre-Alpha .01");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setContentPane(new SwifePanel());
		setIconImage(GlobalIcons.bigGeoIcon.getImage());
		pack();
		setLocationRelativeTo(null);
	}

	public static void main(final String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				final SwifeFrame frame = new SwifeFrame();
				frame.setVisible(true);
			}
		});
	}
}
