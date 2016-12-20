package com.hiveworkshop.wc3.gui.modeledit;

import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.util.HashMap;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JList;

import com.hiveworkshop.wc3.mdl.MDL;
import com.hiveworkshop.wc3.mdl.Material;

public
class MaterialListRenderer extends DefaultListCellRenderer
{
	MDL model;
	HashMap<Material,ImageIcon> map = new HashMap<Material,ImageIcon>();
	Font theFont = new Font("Arial",Font.BOLD,32);
    public MaterialListRenderer(final MDL model)
    {
    	this.model = model;
    }
    @Override
	public Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean iss, final boolean chf)
    {
        final String name = ((Material)value).getName();
        ImageIcon myIcon = map.get(value);
        if( myIcon == null )
        {
        	myIcon = new ImageIcon(((Material)value)
        			.getBufferedImage(model.getWorkingDirectory())
        			.getScaledInstance(64, 64, Image.SCALE_SMOOTH));
        	map.put((Material)value, myIcon);
        }
        super.getListCellRendererComponent( list, name, index, iss, chf );
        setIcon( myIcon );
        setFont(theFont);

        return this;
    }
}
