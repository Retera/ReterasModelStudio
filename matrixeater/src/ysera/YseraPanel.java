package ysera;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

import org.lwjgl.LWJGLException;

import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.modeledit.PerspectiveViewport;
import com.hiveworkshop.wc3.mdl.MDL;
import com.hiveworkshop.wc3.mdl.render3d.RenderModel;
import com.hiveworkshop.wc3.mdl.v2.ModelViewManager;

public class YseraPanel extends JPanel {

	public YseraPanel() {
		setLayout(new BorderLayout());
		try {
			final MDL model = new MDL();
			add(BorderLayout.CENTER, new PerspectiveViewport(new ModelViewManager(model), new ProgramPreferences(),
					new RenderModel(model, null)));
			/*
			 * MdxUtils.loadModel(new BlizzardDataInputStream(
			 * MpqCodebase.get().getResourceAsStream("Units\\Human\\Footman\\Footman.mdx")))
			 */
		} catch (final LWJGLException e) {
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
