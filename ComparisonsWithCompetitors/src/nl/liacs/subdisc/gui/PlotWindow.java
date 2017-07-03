package nl.liacs.subdisc.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;

import javax.swing.*;

import nl.liacs.subdisc.*;

import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.xy.*;

// NOTE always stores original + sorted values, should not pose memory problems
public class PlotWindow extends JFrame implements ActionListener
{
	private static final long serialVersionUID = 1L;

	private XYItemRenderer itsRenderer;

	// TODO null check
	public PlotWindow(Column theColumn)
	{
		if (theColumn == null)
			return;

		initComponents(theColumn);

		setTitle("PlotWindow " + theColumn.getName());
		setIconImage(MiningWindow.ICON);
		setLocation(50, 50);
		setSize(GUI.WINDOW_DEFAULT_SIZE);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		//pack();
		setVisible(true);
	}

	private void initComponents(Column theColumn)
	{
		setLayout(new BorderLayout());

		add(new JScrollPane(new ChartPanel(createChart(theColumn))),
			BorderLayout.CENTER);

		final JPanel aPanel = new JPanel();
		final JPanel aPlotTypePanel = new JPanel();
		aPanel.add(GUI.buildLabel("Plot Data for: ", aPlotTypePanel));

		aPlotTypePanel.add(GUI.buildRadioButton("Original", "original", this));
		aPlotTypePanel.add(GUI.buildRadioButton("Sorted", "sorted", this));
		aPlotTypePanel.add(GUI.buildRadioButton("Both", "both", this));
		((JRadioButton) aPlotTypePanel.getComponent(0)).setSelected(true);

		final ButtonGroup aPlotType = new ButtonGroup();
		for (Component rb : aPlotTypePanel.getComponents())
			aPlotType.add((AbstractButton) rb);

		aPanel.add(aPlotTypePanel);
		aPanel.add(GUI.buildButton("Close", 'C',"close", this));
		add(aPanel, BorderLayout.SOUTH);
	}

	@Override
	public void actionPerformed(ActionEvent theEvent)
	{
		String anEvent = theEvent.getActionCommand();

		if ("close".equals(anEvent))
			dispose();
		else
		{
			itsRenderer.setSeriesVisible(0, !"sorted".equals(anEvent));
			itsRenderer.setSeriesVisible(1, !"original".equals(anEvent));
		}
	}

	private JFreeChart createChart(Column theColumn)
	{
		// fastest + little memory
		JFreeChart aChart =
			ChartFactory.createXYLineChart("",
							null,
							null,
							createDataset(theColumn),
							PlotOrientation.VERTICAL,
							false,
							true,
							false);

		XYPlot aPlot = aChart.getXYPlot();
		aChart.getTitle().setFont(aPlot.getDomainAxis().getLabelFont());
		aPlot.setBackgroundPaint(Color.WHITE);
		aPlot.setDomainGridlinePaint(Color.GRAY);
		aPlot.setRangeGridlinePaint(Color.GRAY);
		aPlot.getDomainAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());


		itsRenderer = aPlot.getRenderer();
		itsRenderer.setSeriesPaint(0, Color.BLACK);
		itsRenderer.setSeriesShape(0, new Ellipse2D.Float(0.0f, 0.0f, 1.0f, 1.0f));
		itsRenderer.setSeriesPaint(1, Color.RED);
		itsRenderer.setSeriesShape(1, new Ellipse2D.Float(0.0f, 0.0f, 1.0f, 1.0f));
		itsRenderer.setSeriesVisible(1, Boolean.FALSE);
		// TODO render order, see Quickie.PlotWindow.drawRegressionLine
		//aPlot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

		return aChart;
	}

	private XYSeriesCollection createDataset(Column theColumn)
	{
		// values in original order
		XYSeries o = new XYSeries("original", true, false);
		float[] oa = new float[theColumn.size()];

		for (int i = 0, j = oa.length; i < j; ++i)
		{
			oa[i] = theColumn.getFloat(i);
			o.add(i, oa[i]);
		}

		// sort original values
		Arrays.sort(oa);

		// values in sorted order
		XYSeries s = new XYSeries("sorted", true, false);
		for (int i = 0, j = oa.length; i < j; ++i)
			s.add(i, oa[i]);

		XYSeriesCollection aDataset= new XYSeriesCollection(o);
		aDataset.addSeries(s);

		return aDataset;
	}
}

