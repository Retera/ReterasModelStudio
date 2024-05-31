package com.hiveworkshop.rms.ui.application.model.nodepanels;

import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.util.sound.*;
import com.hiveworkshop.rms.util.uiFactories.Button;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutionException;

public class EventPreviewPanel extends JPanel {
	private EventTarget currEvent = null;
	private SwingWorker<ImageIcon, String> gifLoader;

	public EventPreviewPanel() {
		super(new MigLayout(""));
		add(new JLabel("Nothing selected!"));
	}


	public EventPreviewPanel setEvent(EventTarget target) {
//		System.out.println("selected: " + target + " (" + target.getClass().getSimpleName() + ")");
		removeAll();
		revalidate();
		repaint();

		if (target != null) {
			add(new JLabel("Name: "), "");
			add(new JLabel(target.getName()), "wrap");
			add(new JLabel("Tag:  "), "");
			add(new JLabel(EventTarget.getFullTag(target)), "wrap");
			add(new JLabel("Type: "), "");
			add(new JLabel(target.getClass().getSimpleName()), "wrap, gapbottom 10");

			if (target instanceof Sound sound) {
//			System.out.println("was Sound");
				fillSoundInfo(sound);
			} else if (target instanceof SplatMappings.Splat splat) {
//			System.out.println("was Splat");
				fillSplatInfo(splat);
			} else if (target instanceof UberSplatMappings.UberSplat splat) {
//			System.out.println("was UberSplat");
				fillSplatInfo(splat);
			} else if (target instanceof SpawnMappings.Spawn spawn) {
//			System.out.println("was Spawn");
				fillSpawnInfo(spawn);
			}
			revalidate();
			repaint();
		}

		currEvent = target;
		return this;
	}

	private void fillSoundInfo(Sound sound) {
//		System.out.println("added label: \"" + "Name: " + sound.getName() + "\"");
		String[][] soundNameAndPaths = getFileNameAndPaths(sound.getFileNameAndPaths());
		JPanel soundFiles = new JPanel(new MigLayout(""));
		String dir;
		if (0 < soundNameAndPaths.length) {
			dir = soundNameAndPaths[0][1].replaceAll("[\\\\/]?" + soundNameAndPaths[0][0], "");
		} else {
			dir = sound.getDirectoryBase();
		}
		add(new JLabel("Dir:"), "span 2, split");
		add(new JLabel(dir), "wrap");
		for (String[] soundNameAndPath : soundNameAndPaths) {
//			System.out.println("SoundPath: \"" + soundNameAndPath[1] + "\" (\"" + soundNameAndPath[0] + "\")");
			soundFiles.add(new JLabel(soundNameAndPath[0]), "growx, split");
			soundFiles.add(Button.create(button -> SoundPlayer.play(soundNameAndPath[1], button), "play"), "wrap");
		}
		add(soundFiles, "span 2");
	}

	private void fillSpawnInfo(SpawnMappings.Spawn spawn) {
		String[][] soundNameAndPaths = spawn.getFileNameAndPaths();
		for (String[] soundNameAndPath : soundNameAndPaths) {
//			System.out.println("SpawnPath: \"" + soundNameAndPath[1] + "\" (\"" + soundNameAndPath[0] + "\")");
			add(new JLabel("File:"), "span 2, split 2");
			add(new JLabel(soundNameAndPath[0]), "wrap");
		}
	}

	private void fillSplatInfo(SplatTarget splat) {
		String[][] soundNameAndPaths = splat.getFileNameAndPaths();
		for (String[] soundNameAndPath : soundNameAndPaths) {
//			System.out.println("SplatPath: \"" + soundNameAndPath[1] + "\" (\"" + soundNameAndPath[0] + "\")");
			BufferedImage image = BLPHandler.getImage(soundNameAndPath[1] + ".blp");
			if (image != null) {
				add(new JLabel("File:"), "span 2, split");
				add(new JLabel(soundNameAndPath[1]), "wrap");
				JLabel iconLabel = new JLabel(getTempIcon(splat));
				add(iconLabel, "span 2, wrap");
				setGifWhenReady(splat, iconLabel);
			} else {
				add(new JLabel(new ImageIcon(BLPHandler.getBlankImage())), "span 2, wrap");
			}
		}
	}

	private ImageIcon getTempIcon(SplatTarget splatTarget) {
		if (splatTarget instanceof SplatMappings.Splat splat) {
			return SplatImageGenerator.getMidImage(splat);
		} else if (splatTarget instanceof UberSplatMappings.UberSplat splat) {
			return SplatImageGenerator.getMidImage(splat);
		} else {
			return new ImageIcon(BLPHandler.getBlankImage());
		}
	}

	private void setGifWhenReady(SplatTarget splatTarget, JLabel iconLabel) {
		if (gifLoader != null && splatTarget != currEvent) {
			gifLoader.cancel(false);
			gifLoader = null;
		}

		if (gifLoader == null && splatTarget != currEvent) {
			gifLoader = new SwingWorker<>() {
				@Override
				protected ImageIcon doInBackground() {
					if (splatTarget instanceof SplatMappings.Splat splat) {
						return SplatImageGenerator.generateIcon(splat);
					} else if (splatTarget instanceof UberSplatMappings.UberSplat splat) {
						return SplatImageGenerator.generateIcon(splat);
					}
					return new ImageIcon(BLPHandler.getBlankImage());
				}
				@Override
				protected void done() {
					if (!isCancelled() && isDone()) {
						try {
							if (splatTarget == currEvent) {
								iconLabel.setIcon(get());
								iconLabel.repaint();
								System.out.println("updated icon for " + EventTarget.getFullTag(splatTarget));
							}
						} catch (InterruptedException | ExecutionException e) {
							Throwable cause = e.getCause();
							if (cause instanceof RuntimeException) {
								throw (RuntimeException) cause;
							} else {
								throw new RuntimeException("Failed to load \"" + EventTarget.getFullTag(splatTarget) + "\"", e);
							}
						}
					} else {
						System.out.println("canceled loading icon for " + EventTarget.getFullTag(splatTarget));
					}
				}
			};

			gifLoader.execute();
		}

	}

	public String[][] getFileNameAndPaths(String[][] namesAndPaths) {
		if (0 < namesAndPaths.length) {
			String[][] paths = new String[namesAndPaths.length][2];

			if (1 < namesAndPaths.length) {
				String[] split1 = namesAndPaths[0][0].replaceAll("^[\\\\/]", "").split("[\\\\/]");
				int numCommonPathElements = split1.length-1;
				for (int i = 1; i < namesAndPaths.length; i++) {
					String[] split2 = namesAndPaths[i][0].replaceAll("^[\\\\/]", "").split("[\\\\/]");
					for (int j = 0; j < numCommonPathElements && j < split2.length; j++) {
						if (!split1[j].equals(split2[j])) {
							numCommonPathElements = j;
						}
					}
				}
				for (int i = 0; i < paths.length; i++) {
					paths[i][0] = namesAndPaths[i][0].replaceAll("^[\\\\/]?(.+[\\\\/]){" + numCommonPathElements + "}", "");
					paths[i][1] = namesAndPaths[i][1];
				}
				return paths;
			}
			for (int i = 0; i < paths.length; i++) {
				paths[i][0] = namesAndPaths[i][0].replaceAll("^[\\\\/]?(.+[\\\\/])*", "");
				paths[i][1] = namesAndPaths[i][1];
			}
			return paths;
		}
		return new String[0][0];
	}
}
