package nl.liacs.subdisc.gui;

import java.awt.*;
import java.awt.geom.*;
import java.text.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import nl.liacs.subdisc.*;

public class ROCCurve extends JPanel
{
	private static final long serialVersionUID = 1L;

	private GeneralPath itsCurve;
	private GeneralPath itsLines;
	private List<Arc2D.Float> itsPoints;
	private String itsAreaUnderCurve;
	private float itsXMin, itsXMax, itsYMin, itsYMax;
	private float itsXStart, itsYStart, itsXEnd, itsYEnd;
	private int itsMin, itsMax;
	private QualityMeasure itsQualityMeasure;

	public ROCCurve(SubgroupSet theSubgroupSet, SearchParameters theSearchParameters, QualityMeasure theQualityMeasure)
	{
		super();
		setBackground(Color.white);

		ROCList aROCList = theSubgroupSet.getROCList();

		if (aROCList == null)
			return;

		NumberFormat aFormatter = NumberFormat.getNumberInstance();
		aFormatter.setMaximumFractionDigits(3);
		itsAreaUnderCurve = aFormatter.format(aROCList.getAreaUnderCurve());

		List<SubgroupROCPoint> aPoints = new ArrayList<SubgroupROCPoint>(theSubgroupSet.size());
		for(Subgroup s : theSubgroupSet)
			aPoints.add(new SubgroupROCPoint(s));

		itsCurve = new GeneralPath();
		itsCurve.moveTo(0, 0);
		for(SubgroupROCPoint p : aROCList)
			itsCurve.lineTo(p.getFPR(), -p.getTPR());
		itsCurve.lineTo(1, -1);

		itsPoints = new ArrayList<Arc2D.Float>(aPoints.size());
		for(SubgroupROCPoint p : aPoints)
			itsPoints.add(new Arc2D.Float(p.getFPR(), -p.getTPR(), 0.0f, 0.0f, -180.0f, 180.0f, Arc2D.OPEN));

		int aTotalCoverage = theSubgroupSet.getTotalCoverage();
		float aTotalTargetCoverage = theSubgroupSet.getTotalTargetCoverage();
		int aMinCoverage = theSearchParameters.getMinimumCoverage();
		int aMaxCoverage = (int) (aTotalCoverage * theSearchParameters.getMaximumCoverageFraction());
		float aFalseCoverage = aTotalCoverage - aTotalTargetCoverage;
		itsMin = aMinCoverage;
		itsMax = aMaxCoverage;

		itsQualityMeasure = theQualityMeasure;

		itsXMin = aMinCoverage/aFalseCoverage;
		itsYMin = aMinCoverage/aTotalTargetCoverage;
		itsXMax = aMaxCoverage/aFalseCoverage;
		itsYMax = aMaxCoverage/aTotalTargetCoverage;
		itsXStart = (itsYMax-1f)*(itsXMax/itsYMax);
		itsYStart = 1f;
		if (itsYMax < 1f) // crosses left boundary, rather than top boundary
		{
			itsXStart = 0f;
			itsYStart = itsYMax;
		}
		itsXEnd = 1f;
		itsYEnd = itsYMax-(itsYMax/itsXMax);
		if (itsXMax < 1f) // crosses bottom boundary, rather than right boundary
		{
			itsXEnd = itsXMax;
			itsYEnd = 0f;
		}

		itsLines = new GeneralPath();
		itsLines.moveTo(itsXMin, 0);		//min cov
		itsLines.lineTo(0, -itsYMin);		//min cov
		itsLines.moveTo(itsXStart, -itsYStart);	//max cov
		itsLines.lineTo(itsXEnd, -itsYEnd);	//max cov
		itsLines.moveTo(0, 0);
		itsLines.lineTo(0, -1);
		itsLines.lineTo(1, -1);
		itsLines.lineTo(1, 0);
		itsLines.lineTo(0, 0);
		for(int i=0; i<11; i++)
		{
			itsLines.moveTo(i*0.1f, 0.0f);
			itsLines.lineTo(i*0.1f, 0.01f);
			itsLines.moveTo(0.0f, i*-0.1f);
			itsLines.lineTo(-0.01f, i*-0.1f);
		}
	}

	public String getAreaUnderCurve() { return itsAreaUnderCurve; }

	public void paintComponent(Graphics theGraphic)
	{
		int aWidth = getWidth();
		int aHeight = getHeight();
		float aSize = Math.min(aWidth, aHeight)*0.85f;

		super.paintComponent(theGraphic);
		Graphics2D aGraphic = (Graphics2D)theGraphic;
		aGraphic.scale(aSize, aSize);
		aGraphic.translate(0.15, 1.05);
		aGraphic.setStroke(new BasicStroke(3.0f/aSize));

		//isometrics
		int N = itsQualityMeasure.getNrRecords();
		int p = itsQualityMeasure.getNrPositives();
		int aResolution = 400;
		float aMax = Math.max(itsQualityMeasure.calculate(p, p), itsQualityMeasure.calculate(p, N));
		aMax = Math.max(aMax, itsQualityMeasure.calculate(0, N-p));
		for (int i=0; i<aResolution; i++)
		{
			float anX = i/(float)aResolution;
			float aNegatives = anX*(N-p); //this can be fractional, even though the counts are always integer, picture looks nicer this way
			for (int j=0; j<aResolution; j++)
			{
				float aY = -(j+1)/(float)aResolution;
				float aPositives = -aY*p; //this can be fractional, even though the counts are always integer
				int aValue = (int) (255*itsQualityMeasure.calculate(aPositives, aNegatives+aPositives)/aMax);
				boolean isNegative = (aValue<0);
				if (isNegative)
					aValue = -aValue;
				aValue = Math.min(aValue, 255);
				aValue = Math.max(aValue, 0);
				aValue = 15*(Math.round(aValue/15));
				if (isNegative)
					aGraphic.setColor(new Color(255, 255-aValue, 255-aValue)); //red
				else
					aGraphic.setColor(new Color(255-aValue, 255, 255-aValue)); //green
				aGraphic.fill(new Rectangle2D.Double(anX, aY, 1/(float)aResolution, 1/(float)aResolution));
			}
		}

		aGraphic.setColor(Color.black);

		if (itsPoints != null)
			for(Arc2D aPoint : itsPoints)
				aGraphic.draw(aPoint);

		aGraphic.setStroke(new BasicStroke(2.0f/aSize));
		aGraphic.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
									RenderingHints.VALUE_ANTIALIAS_ON);
		aGraphic.draw(itsCurve);
		aGraphic.setStroke(new BasicStroke(1.0f/aSize));
		aGraphic.draw(itsLines);

		Font aFont = new Font("SansSerif", Font.PLAIN, 11);
		Font aNewFont = aFont.deriveFont(11.0f/aSize);
		aGraphic.setFont(aNewFont);
		aGraphic.drawString("(0,0)", -0.05f, 0.04f);
		aGraphic.drawString("FPR", 0.44f, 0.08f);
		aGraphic.drawString("TPR", -0.1f, -0.44f);

		//scales
		NumberFormat aFormatter = NumberFormat.getNumberInstance();
		aFormatter.setMaximumFractionDigits(1);
		for(int i=1; i<11; i++)
		{
			aGraphic.drawString(aFormatter.format(i*0.1f), i*0.1f, 0.04f);
			aGraphic.drawString(aFormatter.format(i*0.1f), -0.07f, i*-0.1f);
		}

		//qualities
		aFormatter.setMaximumFractionDigits(4);
		aFont = new Font("SansSerif", Font.PLAIN, 10);
		aNewFont = aFont.deriveFont(10.0f/aSize);
		aGraphic.setFont(aNewFont);
		aGraphic.drawString(aFormatter.format(itsQualityMeasure.getROCHeaven()), 0.02f, -0.96f);
		aGraphic.drawString(aFormatter.format(itsQualityMeasure.getROCHell()), 0.9f, -0.02f);

		//min and max support
		aGraphic.drawString(Integer.toString(itsMin), itsXMin, -0.03f);
		aGraphic.drawString(Integer.toString(itsMax), itsXEnd+0.01f, -Math.max(itsYEnd, 0.03f));
	}
}
