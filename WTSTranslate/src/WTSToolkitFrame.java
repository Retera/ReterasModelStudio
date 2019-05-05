import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;


public class WTSToolkitFrame extends JFrame {
	public WTSToolkitFrame() {
		super("Retera's WTS Toolkit");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setContentPane(new WTSToolkitPanel());
		try {
			setIconImage(ImageIO.read(getClass().getClassLoader().getResourceAsStream("res/ActionsQuest.png")));
		} catch (IOException e) {
			System.err.println("Could not load icon file: res/ActionsQuest.png");
			e.printStackTrace();
		}
		pack();
		setLocationRelativeTo(null);
	}
	
	public static void main(String[] args) {
		// junk code to make it look good on windows.
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		
		WTSToolkitFrame frame = new WTSToolkitFrame();
		frame.setVisible(true);
	}
}
