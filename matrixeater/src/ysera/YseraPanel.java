package ysera;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

import org.lwjgl.LWJGLException;
import org.lwjgl.util.vector.Quaternion;

import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.modelviewer.AnimatedPerspectiveViewport;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.v2.ModelViewManager;
import com.hiveworkshop.wc3.mdx.MdxModel;
import com.hiveworkshop.wc3.mdx.MdxUtils;
import com.hiveworkshop.wc3.mpq.MpqCodebase;

import de.wc3data.stream.BlizzardDataInputStream;

public class YseraPanel extends JPanel {
	private static final Quaternion IDENTITY = new Quaternion();

	public YseraPanel() {
		setLayout(new BorderLayout());
		try {
			final MdxModel footman = MdxUtils.loadModel(new BlizzardDataInputStream(
					MpqCodebase.get().getResourceAsStream("Units\\Human\\Footman\\Footman.mdx")));
			final EditableModel footmanMDL = new EditableModel(footman);
			final AnimatedPerspectiveViewport perspectiveViewport = new AnimatedPerspectiveViewport(
					new ModelViewManager(footmanMDL), new ProgramPreferences(), true);
			perspectiveViewport.setAnimationTime(0);
			perspectiveViewport.setLive(true);
			perspectiveViewport.setAnimation(footmanMDL.getAnim(0));
			add(BorderLayout.CENTER, perspectiveViewport);

		} catch (final LWJGLException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setPreferredSize(new Dimension(640, 480));
	}

	public JMenuBar createJMenuBar() {
		final JMenuBar jMenuBar = new JMenuBar();

		final JMenu fileMenu = new JMenu("File");
		jMenuBar.add(fileMenu);
		jMenuBar.add(new JMenu("Recent Files"));
		jMenuBar.add(new JMenu("Edit"));
		jMenuBar.add(new JMenu("View"));
		jMenuBar.add(new JMenu("Team Color"));
		jMenuBar.add(new JMenu("Windows"));
		jMenuBar.add(new JMenu("Extras"));
		jMenuBar.add(new JMenu("Help"));

		return jMenuBar;
	}
}
