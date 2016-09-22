import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;


public class MainFrame extends JFrame {
	public MainFrame() throws IOException {
		super("Hayate's Character Engine");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setContentPane(new MainPanel());
		setIconImage(ImageIO.read(MainFrame.class.getResource("BTNcaptain_barbosa.png")));
		pack();
		setLocationRelativeTo(null);
	}
	
	public static void main(String[] args) {
		MainFrame frame;
		try {
			frame = new MainFrame();
			frame.setVisible(true);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,"Program error: failed to load required resource.");
			e.printStackTrace();
		}
	}
}
