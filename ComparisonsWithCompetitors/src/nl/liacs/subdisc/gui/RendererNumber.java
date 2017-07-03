package nl.liacs.subdisc.gui;

import java.text.*;

import javax.swing.table.*;

// renders Number using 6 decimals
public final class RendererNumber extends DefaultTableCellRenderer
{
	private static final long serialVersionUID = 1L;

	public static final RendererNumber RENDERER = new RendererNumber();
	public static final NumberFormat FORMATTER;

	static
	{
		FORMATTER  = NumberFormat.getNumberInstance();
		FORMATTER.setMaximumFractionDigits(6);
	}

	// only one/uninstantiable
	private RendererNumber() {}

	@Override
	public void setValue(Object aValue)
	{
		if (aValue instanceof Number)
			setText(FORMATTER.format((Number)aValue));
		else	// not a Number, or null
			super.setValue(aValue);
	}
}
