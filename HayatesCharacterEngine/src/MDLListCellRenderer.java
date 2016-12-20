import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import com.hiveworkshop.wc3.mdl.MDL;


public class MDLListCellRenderer extends DefaultListCellRenderer {
    @Override
	public Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean iss, final boolean chf)
    {
    	final MDL model = (MDL)value;
        super.getListCellRendererComponent( list, model.getHeaderName(), index, iss, chf );

        return this;
    }
}
