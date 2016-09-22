package com.matrixeater.src;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
/**
 * Write a description of class MainFrame here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class MainFrame extends JFrame
{
    static MainFrame frame;
    static MainPanel panel;
    static JMenuBar menuBar;
    public static MainPanel getPanel()
    {
    	return panel;
    }
    public static void main( String [] args )
    {
//    	IIORegistry registry = IIORegistry.getDefaultInstance();
//    	registry.registerServiceProvider(
//    	    new com.realityinteractive.imageio.tga.TGAImageReaderSpi());

        try {
                // Set cross-platform Java L&F (also called "Metal")
            UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName());
        } 
        catch (UnsupportedLookAndFeelException e) {
           // handle exception
        }
        catch (ClassNotFoundException e) {
           // handle exception
        }
        catch (InstantiationException e) {
           // handle exception
        }
        catch (IllegalAccessException e) {
           // handle exception
        }
    	
    	
        frame = new MainFrame("The MatrixEater");
        panel.init();
    }
    public MainFrame(String title)
    {
        super(title);
//         setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        
        setBounds(0,0,1000,650);
        panel = new MainPanel();
        setContentPane(panel);
        menuBar = panel.createMenuBar();
        setJMenuBar(menuBar);
        setIconImage((new ImageIcon(MainFrame.class.getResource("ImageBin/MDLIcon.png"))).getImage());
        
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e)
            {
                if( panel.closeAll() )
                {
                    System.exit(0);
                }
            }
        });
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
