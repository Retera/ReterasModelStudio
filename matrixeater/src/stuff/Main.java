package stuff;

import java.awt.HeadlessException;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import com.hiveworkshop.wc3.gui.modeledit.PerspDisplayPanel;
import com.hiveworkshop.wc3.mdl.MDL;
import com.hiveworkshop.wc3.mdl.RenderModel;
import com.hiveworkshop.wc3.mdl.v2.ModelViewManager;
import com.hiveworkshop.wc3.mdx.MdxUtils;
import com.hiveworkshop.wc3.mpq.MpqCodebase;

import de.wc3data.stream.BlizzardDataInputStream;

public class Main {
	public static void main(final String[] args) {
		MDL model;
		try {
			model = new MDL(MdxUtils.loadModel(new BlizzardDataInputStream(
					MpqCodebase.get().getResourceAsStream("units\\critters\\zergling\\zergling.mdx"))));
		} catch (final IOException e1) {
			throw new RuntimeException(e1);
		}
		System.out.println(model.getHeaderName());
		try {

			final PerspDisplayPanel perspDisplayPanel = new PerspDisplayPanel("Zergling man!",
					new ModelViewManager(model), null, new RenderModel(model));
			JOptionPane.showMessageDialog(null, perspDisplayPanel);
			final BufferedImage bufferedImage = perspDisplayPanel.getViewport().getBufferedImage();
			JOptionPane.showMessageDialog(null, new ImageIcon(model.getMaterial(1).getBufferedImage(null)));
			JOptionPane.showMessageDialog(null, new ImageIcon(bufferedImage));
		} catch (final HeadlessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
