package com.hiveworkshop.rms;

import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.filesystem.sources.DataSourceDescriptor;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.parsers.slk.DataTable;
import com.hiveworkshop.rms.ui.application.MainFrame;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;
import com.hiveworkshop.rms.ui.browsers.model.ModelOptionPanel;
import com.hiveworkshop.rms.ui.browsers.unit.UnitOptionPanel;
import com.hiveworkshop.rms.ui.preferences.DataSourceChooserPanel;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.ui.preferences.SaveProfile;
import com.hiveworkshop.rms.ui.util.EditorDisplayManager;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import net.infonode.gui.laf.InfoNodeLookAndFeel;
import net.infonode.gui.laf.InfoNodeLookAndFeelTheme;
import net.infonode.gui.laf.InfoNodeLookAndFeelThemes;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(final String[] args) throws IOException {
        final boolean hasArgs = args.length >= 1;
        final List<String> startupModelPaths = new ArrayList<>();
        if (hasArgs) {
            if ((args.length > 1) && args[0].equals("-convert")) {
                runAsConverter(args[1]);
                return;
            } else {
                if (args[0].endsWith(".mdx") || args[0].endsWith(".mdl") || args[0].endsWith(".blp")
                        || args[0].endsWith(".dds") || args[0].endsWith(".obj")) {
                    startupModelPaths.addAll(Arrays.asList(args));
                }
            }
        }
        final boolean dataPromptForced = hasArgs && args[0].equals("-forcedataprompt");
        try {
            LwjglNativesLoader.load();

            // Load the jassimp natives.
            try {
                final SharedLibraryLoader loader = new SharedLibraryLoader();
                loader.load("jassimp-natives");
            } catch (final Exception e) {
                JOptionPane.showMessageDialog(null, "The C++ natives to parse FBX models failed to load. You will not be able to open FBX until you install the necessary software\nand restart Retera Model Studio.\n\nMaybe you are missing some Visual Studio Runtime dependency?\n\nNext up I will show you the error message that says why these C++ jassimp natives failed to load,\nin case you want to copy them and ask for help. Once you press OK on that error popup, you can probably still use\nRetera Model Studio just fine for everything else.", "Error", JOptionPane.ERROR_MESSAGE);
                ExceptionPopup.display(e);
            }

            final ProgramPreferences preferences = SaveProfile.get().getPreferences();
            switch (preferences.getTheme()) {
                case DARK:
                    EditorDisplayManager.setupLookAndFeel();
                    break;
                case HIFI:
                    EditorDisplayManager.setupLookAndFeel("HiFi");
                    break;
                case ACRYL:
                    EditorDisplayManager.setupLookAndFeel("Acryl");
                    break;
                case ALUMINIUM:
                    EditorDisplayManager.setupLookAndFeel("Aluminium");
                    break;
                case FOREST_GREEN:
                    try {
                        final InfoNodeLookAndFeelTheme theme = new InfoNodeLookAndFeelTheme("Retera Studio",
                                new Color(44, 46, 20), new Color(116, 126, 36), new Color(44, 46, 20),
                                new Color(220, 202, 132), new Color(116, 126, 36), new Color(220, 202, 132));
                        theme.setShadingFactor(-0.8);
                        theme.setDesktopColor(new Color(60, 82, 44));

                        UIManager.setLookAndFeel(new InfoNodeLookAndFeel(theme));
                    } catch (final UnsupportedLookAndFeelException e) {
                        e.printStackTrace();
                    }
                    break;
                case WINDOWS:
                    try {
                        UIManager.put("desktop", new ColorUIResource(Color.WHITE));
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                        System.out.println(UIManager.getLookAndFeel());
                    } catch (final UnsupportedLookAndFeelException e) {
                        // handle exception
                    } catch (final ClassNotFoundException e) {
                        // handle exception
                    } catch (final InstantiationException e) {
                        // handle exception
                    } catch (final IllegalAccessException e) {
                        // handle exception
                    }
                    break;
                case WINDOWS_CLASSIC:
                    try {
                        UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
                    } catch (final Exception exc) {
                        try {
                            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                        } catch (final ClassNotFoundException e) {
                            e.printStackTrace();
                        } catch (final InstantiationException e) {
                            e.printStackTrace();
                        } catch (final IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (final UnsupportedLookAndFeelException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case JAVA_DEFAULT:
                    break;
                case SOFT_GRAY:
                    try {
                        final InfoNodeLookAndFeelTheme softGrayTheme = InfoNodeLookAndFeelThemes.getSoftGrayTheme();
                        UIManager.setLookAndFeel(new InfoNodeLookAndFeel(softGrayTheme));
                    } catch (final Exception exc) {
                        try {
                            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                        } catch (final ClassNotFoundException e) {
                            e.printStackTrace();
                        } catch (final InstantiationException e) {
                            e.printStackTrace();
                        } catch (final IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (final UnsupportedLookAndFeelException e) {
                            e.printStackTrace();
                        }
                    }

                    break;
                case BLUE_ICE:
                    try {
                        final InfoNodeLookAndFeelTheme blueIceTheme = InfoNodeLookAndFeelThemes.getBlueIceTheme();
                        UIManager.setLookAndFeel(new InfoNodeLookAndFeel(blueIceTheme));
                    } catch (final Exception exc) {
                        try {
                            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                        } catch (final ClassNotFoundException e) {
                            e.printStackTrace();
                        } catch (final InstantiationException e) {
                            e.printStackTrace();
                        } catch (final IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (final UnsupportedLookAndFeelException e) {
                            e.printStackTrace();
                        }
                    }

                    break;
                case DARK_BLUE_GREEN:
                    try {
                        final InfoNodeLookAndFeelTheme darkBlueGreenTheme = InfoNodeLookAndFeelThemes
                                .getDarkBlueGreenTheme();
                        UIManager.setLookAndFeel(new InfoNodeLookAndFeel(darkBlueGreenTheme));
                    } catch (final Exception exc) {
                        try {
                            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                        } catch (final ClassNotFoundException e) {
                            e.printStackTrace();
                        } catch (final InstantiationException e) {
                            e.printStackTrace();
                        } catch (final IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (final UnsupportedLookAndFeelException e) {
                            e.printStackTrace();
                        }
                    }

                    break;
                case GRAY:
                    try {
                        final InfoNodeLookAndFeelTheme grayTheme = InfoNodeLookAndFeelThemes.getGrayTheme();
                        UIManager.setLookAndFeel(new InfoNodeLookAndFeel(grayTheme));
                    } catch (final Exception exc) {
                        try {
                            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                        } catch (final ClassNotFoundException e) {
                            e.printStackTrace();
                        } catch (final InstantiationException e) {
                            e.printStackTrace();
                        } catch (final IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (final UnsupportedLookAndFeelException e) {
                            e.printStackTrace();
                        }
                    }

                    break;
            }
            SwingUtilities.invokeLater(() -> {
                try {
                    final List<DataSourceDescriptor> dataSources = SaveProfile.get().getDataSources();
                    if ((dataSources == null) || dataPromptForced) {
                        final DataSourceChooserPanel dataSourceChooserPanel = new DataSourceChooserPanel(
                                dataSources);

                        final JFrame jFrame = new JFrame("Retera Model Studio: Setup");

                        jFrame.setUndecorated(true);
                        jFrame.pack();
                        jFrame.setSize(0, 0);
                        jFrame.setLocationRelativeTo(null);
                        jFrame.setIconImage(MainFrame.MAIN_PROGRAM_ICON);
                        jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                        jFrame.setVisible(true);
                        try {
                            if (JOptionPane.showConfirmDialog(jFrame, dataSourceChooserPanel,
                                    "Retera Model Studio: Setup", JOptionPane.OK_CANCEL_OPTION,
                                    JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
                                return;
                            }
                        } finally {
                            jFrame.setVisible(false);
                        }
                        SaveProfile.get().setDataSources(dataSourceChooserPanel.getDataSourceDescriptors());
                        SaveProfile.save();
                        GameDataFileSystem.refresh(SaveProfile.get().getDataSources());
                        // cache priority order...
                        UnitOptionPanel.dropRaceCache();
                        DataTable.dropCache();
                        ModelOptionPanel.dropCache();
                        WEString.dropCache();
                        BLPHandler.get().dropCache();
                    }

                    JPopupMenu.setDefaultLightWeightPopupEnabled(false);
                    MainFrame.create(startupModelPaths);
                } catch (final Throwable th) {
                    th.printStackTrace();
                    ExceptionPopup.display(th);
                    if (!dataPromptForced) {
                        new Thread(() -> {
                            try {
                                main(new String[]{"-forcedataprompt"});
                            } catch (final IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }).start();
                    } else {
                        JOptionPane.showMessageDialog(null,
                                "Retera Model Studio startup sequence has failed for two attempts. The program will now exit.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                        System.exit(-1);
                    }
                }
            });
        } catch (final Throwable th) {
            th.printStackTrace();
            SwingUtilities.invokeLater(() -> ExceptionPopup.display(th));
            if (!dataPromptForced) {
                main(new String[]{"-forcedataprompt"});
            } else {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null,
                            "Retera Model Studio startup sequence has failed for two attempts. The program will now exit.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    System.exit(-1);
                });
            }
        }
    }

    private static void runAsConverter(final String arg) throws IOException {
        final String path = arg;
        final EditableModel model = MdxUtils.loadEditable(new File(path));
        if (path.toLowerCase().endsWith(".mdx")) {
            MdxUtils.saveMdl(model, new File(path.substring(0, path.lastIndexOf('.')) + ".mdl"));
        } else if (path.toLowerCase().endsWith(".mdl")) {
            MdxUtils.saveMdx(model, new File(path.substring(0, path.lastIndexOf('.')) + ".mdx"));
        }
    }
}
