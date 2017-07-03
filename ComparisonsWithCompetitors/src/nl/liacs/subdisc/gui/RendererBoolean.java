package nl.liacs.subdisc.gui;

import javax.swing.table.*;

// renders Boolean as 0/1
public final class RendererBoolean extends DefaultTableCellRenderer
{
	private static final long serialVersionUID = 1L;

	public static final RendererBoolean RENDERER = new RendererBoolean();

	// only one/uninstantiable
	private RendererBoolean() {}

	@Override
	public void setValue(Object aValue)
	{
		if (aValue instanceof Boolean)
			setText(((Boolean)aValue) ? "1" : "0");
		else	// not a Boolean, or null
			super.setValue(aValue);
	}
}
