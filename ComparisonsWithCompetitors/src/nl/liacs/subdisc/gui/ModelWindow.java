package nl.liacs.subdisc.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;

import javax.swing.*;

import nl.liacs.subdisc.*;

import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.chart.title.*;
import org.jfree.data.xy.*;

public class ModelWindow extends JFrame implements ActionListener
{
	private static final long serialVersionUID = 1L;

	private JScrollPane itsJScrollPaneCenter = new JScrollPane();

	// SINGLE_NUMERIC: show distribution over numeric target and Subgroup ==

	public ModelWindow(Column theDomain, ProbabilityDensityFunction theDatasetPDF, ProbabilityDensityFunction theSubgroupPDF, String theName)
	{
		initComponents();

		final boolean addSubgroup = (theSubgroupPDF != null);

		XYSeries aDatasetSeries = new XYSeries("dataset");
		XYSeries aSubgroupSeries = addSubgroup ? new XYSeries("subgroup") : null;
		for (int i = 0, j = theDatasetPDF.size(); i < j; ++i)
		{
			aDatasetSeries.add(theDatasetPDF.getMiddle(i), theDatasetPDF.getDensity(i));
			if (addSubgroup)
				aSubgroupSeries.add(theSubgroupPDF.getMiddle(i), theSubgroupPDF.getDensity(i));
		}
		XYSeriesCollection aDataCollection = new XYSeriesCollection(aDatasetSeries);
		if (addSubgroup)
			aDataCollection.addSeries(aSubgroupSeries);

		JFreeChart aChart =
			ChartFactory.createXYLineChart("", theDomain.getName(), "density", aDataCollection, PlotOrientation.VERTICAL, false, true, false);
		aChart.setAntiAlias(true);
		XYPlot plot = aChart.getXYPlot();
		plot.setBackgroundPaint(Color.white);
		plot.setDomainGridlinePaint(Color.gray);
		plot.setRangeGridlinePaint(Color.gray);
		plot.getRenderer().setSeriesPaint(0, Color.black);
		plot.getRenderer().setSeriesStroke(0, new BasicStroke(2.0f));
		plot.getRenderer().setSeriesPaint(1, Color.red); //subgroup
		plot.getRenderer().setSeriesStroke(1, new BasicStroke(2.0f)); //subgroup
		if (addSubgroup)
			aChart.addLegend(new LegendTitle(plot));

		itsJScrollPaneCenter.setViewportView(new ChartPanel(aChart));

		if (!addSubgroup)
			setTitle("Base Model: Numeric Distribution for " + theName);
		else
			setTitle(theName + ": Numeric Distribution");
		setIconImage(MiningWindow.ICON);
		setLocation(50, 50);
		setSize(GUI.WINDOW_DEFAULT_SIZE);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);
	}

	// DOUBLE_CORRELATION and DOUBLE_REGRESSION ============================

	//TODO There should never be this much code in a constructor
	public ModelWindow(Column theXColumn, Column theYColumn, RegressionMeasure theRM, Subgroup theSubgroup)
	{
		initComponents();

		final boolean isRegression = (theRM != null);
		final boolean forSubgroup = (theSubgroup != null);

		String aName;
		if (isRegression)
		{
				aName = String.format("%s = %f + %f * %s",
							theYColumn.getName(),
							(float) theRM.getIntercept(),
							(float) theRM.getSlope(),
							theXColumn.getName());
		}
		else
		{
			if (forSubgroup)
				aName = String.format("2D distribution (r = %f)",
						(float) theSubgroup.getSecondaryStatistic());
			else
				aName = "2D distribution";
		}

		//data
		XYSeries aSeries = new XYSeries("data");

		//if i is a member of the specified subgroup
		if (forSubgroup)
		{
			BitSet aMembers = theSubgroup.getMembers();
			for (int i = 0, j = theXColumn.size(); i < j; ++i)
				if (aMembers.get(i))
					aSeries.add(theXColumn.getFloat(i), theYColumn.getFloat(i));
		}
		//if complete database
		else
			for (int i = 0, j = theXColumn.size(); i < j; ++i)
				aSeries.add(theXColumn.getFloat(i), theYColumn.getFloat(i));
		XYSeriesCollection aDataSet = new XYSeriesCollection(aSeries);

		// create the chart
		JFreeChart aChart =
			ChartFactory.createScatterPlot(aName, theXColumn.getName(), theYColumn.getName(), aDataSet, PlotOrientation.VERTICAL, false, true, false);
		aChart.setAntiAlias(true);
		aChart.getTitle().setFont(new Font("title", Font.BOLD, 14));

		XYPlot plot = aChart.getXYPlot();
		plot.setBackgroundPaint(Color.white);
		plot.setDomainGridlinePaint(Color.gray);
		plot.setRangeGridlinePaint(Color.gray);
		plot.getRenderer().setSeriesPaint(0, Color.black);
		plot.getRenderer().setSeriesShape(0, new Rectangle2D.Float(0.0f, 0.0f, 2.5f, 2.5f));

		//line
		if (isRegression)
		{
			StandardXYItemRenderer aLineRenderer = new StandardXYItemRenderer(StandardXYItemRenderer.LINES);
			aDataSet = new XYSeriesCollection(); // ?
			aSeries = new XYSeries("line");
			aSeries.add(theXColumn.getMin(), theRM.getBaseFunctionValue(theXColumn.getMin()));
			aSeries.add(theXColumn.getMax(), theRM.getBaseFunctionValue(theXColumn.getMax()));
			aDataSet.addSeries(aSeries); //add second series to represent line
			plot.setDataset(1, aDataSet);
			plot.setRenderer(1, aLineRenderer);
			aLineRenderer.setSeriesStroke(0, new BasicStroke(2.0f));
		}

		itsJScrollPaneCenter.setViewportView(new ChartPanel(aChart));

		String aTitle;
		if (forSubgroup)
			aTitle = new StringBuilder()
					.append("Subgroup ")
					.append(theSubgroup.getID())
					.append(isRegression ? ": Regression" : ": Correlation")
					.toString();
		else
			aTitle = new StringBuilder()
					.append("Base Model: ")
					.append(isRegression ? "Regression" : "Correlation")
					.append(" for entire dataset")
					.toString();

		setTitle(aTitle);
		setIconImage(MiningWindow.ICON);
		setLocation(50, 50);
		setSize(GUI.WINDOW_DEFAULT_SIZE);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);
	}

	// MULTI_LABEL: show Subgroup induced DAG ==============================

	public ModelWindow(DAG theDAG, int theDAGWidth, int theDAGHeight)
	{
		initComponents();
		DAGView aDAGView = new DAGView(theDAG);
		aDAGView.setDAGArea(theDAGWidth, theDAGHeight);
		aDAGView.drawDAG();
		itsJScrollPaneCenter.setViewportView(aDAGView);

		setTitle("Base Model: Bayesian Network for entire dataset");
		setIconImage(MiningWindow.ICON);
		setLocation(0, 0);
		setSize(theDAGWidth, theDAGHeight);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);
	}
/*
	//never used yet
	//create window with the same layout of nodes as the one in theDAGView
	public ModelWindow(DAG theDAG, int theDAGWidth, int theDAGHeight, DAGView theDAGView)
	{
		initComponents();
		itsDAGView = new DAGView(theDAG);
		itsDAGView.setDAGArea(theDAGWidth, theDAGHeight);
		itsDAGView.drawDAG(theDAGView);
		jScrollPaneCenter.setViewportView(itsDAGView);
//		setIconImage(MiningWindow.ICON);
		pack();
	}
*/
	private void initComponents()
	{
		JPanel aPanel = new JPanel();

		aPanel.add(GUI.buildButton("Close", 'C', "close", this));
		getContentPane().add(itsJScrollPaneCenter, BorderLayout.CENTER);
		getContentPane().add(aPanel, BorderLayout.SOUTH);
	}

	@Override
	public void actionPerformed(ActionEvent theEvent)
	{
		if ("close".equals(theEvent.getActionCommand()))
			dispose();
	}
}
