package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.SearchListPanel;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.util.sound.*;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class EventObjectPanel extends JPanel {
	EventMappings eventMappings = new EventMappings(null);
	SearchListPanel<EventTarget> eventSearchListPanel;
	SearchListPanel<Sound> soundsSearchListPanel;
	SearchListPanel<SpawnMappings.Spawn> spawnsSearchListPanel;
	SearchListPanel<SplatMappings.Splat> splatsSearchListPanel;
	SearchListPanel<UberSplatMappings.UberSplat> uberSplatsSearchListPanel;

	public EventObjectPanel() {
		super(new MigLayout("gap 0, fill", "[grow]", "[grow]"));
		JTabbedPane tabbedPane = new JTabbedPane();

//		soundsSearchListPanel = new SearchListPanel<>("Sounds", this::filterEventTarget);
//		soundsSearchListPanel.addAll(eventMappings.getSoundMappings().getSounds());
//		add(soundsSearchListPanel, "wrap");
//
//		splatsSearchListPanel = new SearchListPanel<>("Splats", this::filterEventTarget);
//		splatsSearchListPanel.addAll(eventMappings.getSplatMappings().getSplats());
//		add(splatsSearchListPanel, "wrap");
//
//		uberSplatsSearchListPanel = new SearchListPanel<>("UberSplats", this::filterEventTarget);
//		uberSplatsSearchListPanel.addAll(eventMappings.getUberSplatMappings().getSplats());
//		add(uberSplatsSearchListPanel, "wrap");
//
//		spawnsSearchListPanel = new SearchListPanel<>("Spawns", this::filterEventTarget);
//		spawnsSearchListPanel.addAll(eventMappings.getSpawnMappings().getSplats());
//		add(spawnsSearchListPanel, "wrap");

		eventSearchListPanel = new SearchListPanel<>("All", this::filterEventTarget);
		eventSearchListPanel.setRenderer(getEventRenderer());
		eventMappings.getSoundMappings().getEvents().forEach(eventSearchListPanel::add);
		eventMappings.getSplatMappings().getEvents().forEach(eventSearchListPanel::add);
		eventMappings.getUberSplatMappings().getEvents().forEach(eventSearchListPanel::add);
		eventMappings.getSpawnMappings().getEvents().forEach(eventSearchListPanel::add);
		tabbedPane.addTab("All", null, eventSearchListPanel);

		soundsSearchListPanel = new SearchListPanel<>("Sounds", this::filterEventTarget);
		soundsSearchListPanel.setRenderer(getEventRenderer());
		soundsSearchListPanel.addAll(eventMappings.getSoundMappings().getEvents());
		tabbedPane.addTab("Sounds", null, soundsSearchListPanel);

		splatsSearchListPanel = new SearchListPanel<>("Splats", this::filterEventTarget);
		splatsSearchListPanel.setRenderer(getEventRenderer());
		splatsSearchListPanel.addAll(eventMappings.getSplatMappings().getEvents());
		tabbedPane.addTab("Splats", null, splatsSearchListPanel);

		uberSplatsSearchListPanel = new SearchListPanel<>("UberSplats", this::filterEventTarget);
		uberSplatsSearchListPanel.setRenderer(getEventRenderer());
		uberSplatsSearchListPanel.addAll(eventMappings.getUberSplatMappings().getEvents());
		tabbedPane.addTab("UberSplats", null, uberSplatsSearchListPanel);

		spawnsSearchListPanel = new SearchListPanel<>("Spawns", this::filterEventTarget);
		spawnsSearchListPanel.setRenderer(getEventRenderer());
		spawnsSearchListPanel.addAll(eventMappings.getSpawnMappings().getEvents());
		tabbedPane.addTab("Spawns", null, spawnsSearchListPanel);


		add(tabbedPane, "growx, growy, wrap");
//		add(Label.create("FootPrints"), "wrap");
//		add(Label.create("Splats"), "wrap");
//		add(Label.create("UberSplats"), "wrap");
//
//		add(Label.create("Sounds"), "wrap");
//		add(Label.create("FootPrints"), "wrap");
//		add(Label.create("Splats"), "wrap");
//		add(Label.create("UberSplats"), "wrap");
	}


	public static void showFrame(){
		getFrame().setVisible(true);
	}

	public static JMenuItem getMenuItem(){
		JMenuItem menuItem = new JMenuItem("Show EventPanel");
		menuItem.addActionListener(e -> showFrame());
		return menuItem;
	}

	private static JFrame getFrame() {
		JFrame frame = new JFrame("Search EventObjects");
		try {
			frame.setIconImage(RMSIcons.MDLIcon.getImage());
		} catch (final Exception e) {
			JOptionPane.showMessageDialog(null, "Error: Image files were not found! Due to bad programming, this might break the program!");
		}

		frame.setContentPane(new EventObjectPanel());

		frame.setBounds(0, 0, 1024, 780);
		frame.setLocationRelativeTo(null);
//		frame.addWindowListener(new WindowAdapter() {
//			@Override
//			public void windowClosing(final WindowEvent e) {
//				cancelImport(frame);
//			}
//		});
//		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		return frame;
	}

	public <Q extends EventTarget> boolean filterEventTarget2(Q target, String s){
		return target.getName().toLowerCase().contains(s.toLowerCase());
	}

	public boolean filterEventTarget(EventTarget target, String s){
		return target.getName().toLowerCase().contains(s.toLowerCase())
				|| target.getTag().toLowerCase().contains(s.toLowerCase())
				|| target.getFileNames() != null && Arrays.stream(target.getFileNames()).anyMatch(t -> t.toLowerCase().contains(s.toLowerCase()));
	}


	public ListCellRenderer<Object> getEventRenderer(){
		return new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSel, boolean hasFoc) {
				super.getListCellRendererComponent(list, value, index, isSel, hasFoc);
//				Font font = getFont();
				setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
//				Vec3 bg = noOwnerBgCol;
//				Vec3 fg = noOwnerFgCol;
//
//				// ToDo check if type matters for this renders
//				if (value instanceof MatrixShell) { // ObjectShell
//					setText(value.toString());
//					if (((MatrixShell) value).getNewBones().isEmpty()) {
////				bg = otherOwnerBgCol;
////				fg = otherOwnerFgCol;
//						bg = emptyBgCol;
////				new Color(220, 160, 160);
////				new Color(150, 80, 80);
//					}
////			else if (bonesNotInAllMatricies.contains(value)) {
////				new Color(150, 80, 80);
////				bg = new Vec3(150, 80, 80);
////				fg = otherOwnerFgCol;
////			}
//				} else {
//					setText(value.toString());
//				}
////		if (value instanceof IdObjectShell<?> && ((IdObjectShell<?>) value).getImportStatus() != IdObjectShell.ImportType.IMPORT) { // BoneShell
////			bg = Vec3.getProd(bg, otherOwnerBgCol).normalize().scale(160);
////			fg = Vec3.getProd(bg, otherOwnerFgCol).normalize().scale(60);
////		}
//
//				if (isSel) {
//					bg = Vec3.getSum(bg, hLAdjBgCol);
//				}
//
//				this.setBackground(bg.asIntColor());
//				this.setForeground(fg.asIntColor());

				return this;
			}
		};
	}
}
