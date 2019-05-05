import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JTabbedPane;


public class WTSToolkitPanel extends JTabbedPane {
	public WTSToolkitPanel() {
		//setPreferredSize(new Dimension(800,600));
		this.addTab("Raw String Files", new ImageIcon(WTSToolkitPanel.class.getResource("res/ActionsQuest.png")), new PanelRawTranslate());
		this.addTab("Map", new ImageIcon(WTSToolkitPanel.class.getResource("res/EditorTerrain.png")), new PanelMapTranslate());
	}
}
