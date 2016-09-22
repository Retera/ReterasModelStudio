package com.matrixeater.src;

import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.util.HashMap;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JList;

public 
class MaterialListRenderer extends DefaultListCellRenderer
{
	HashMap<Material,ImageIcon> map = new HashMap<Material,ImageIcon>();
	Font theFont = new Font("Arial",Font.BOLD,32);
    public MaterialListRenderer()
    {
    	
    }
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean iss, boolean chf)
    {
        String name = ((Material)value).getName();
        ImageIcon myIcon = map.get((Material)value);
        if( myIcon == null )
        {
        	myIcon = new ImageIcon(((Material)value).getBufferedImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH));
        	map.put((Material)value, myIcon);
        }
        super.getListCellRendererComponent( list, name, index, iss, chf );
        setIcon( myIcon );
        setFont(theFont);
        
        return this;
    }
}
