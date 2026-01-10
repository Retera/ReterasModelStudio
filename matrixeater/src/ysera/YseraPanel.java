package ysera;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import com.matrixeater.localization.LocalizationManager;
import java.util.Locale;

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
		final LocalizationManager lm = LocalizationManager.getInstance();
		final JMenuBar jMenuBar = new JMenuBar();

		final JMenu fileMenu = new JMenu(lm.get("menu.file"));
		jMenuBar.add(fileMenu);
		jMenuBar.add(new JMenu(lm.get("menu.recent_files")));
		jMenuBar.add(new JMenu(lm.get("menu.edit")));
		jMenuBar.add(new JMenu(lm.get("menu.view")));
		jMenuBar.add(new JMenu(lm.get("menu.team_color")));
		jMenuBar.add(new JMenu(lm.get("menu.windows")));
		jMenuBar.add(new JMenu(lm.get("menu.extras")));
		jMenuBar.add(new JMenu(lm.get("menu.help")));

		final JMenu languageMenu = new JMenu(lm.get("menu.language"));
		final JMenuItem enItem = new JMenuItem(lm.get("language.en"));
		enItem.addActionListener(e -> LocalizationManager.getInstance().setLocale(Locale.ENGLISH));
		final JMenuItem zhItem = new JMenuItem(lm.get("language.zh_CN"));
		zhItem.addActionListener(e -> LocalizationManager.getInstance().setLocale(new Locale("zh","CN")));
		languageMenu.add(enItem);
		languageMenu.add(zhItem);
		jMenuBar.add(languageMenu);

		return jMenuBar;
	}
}
