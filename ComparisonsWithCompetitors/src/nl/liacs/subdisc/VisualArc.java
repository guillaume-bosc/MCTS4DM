package nl.liacs.subdisc;

import java.awt.*;

public class VisualArc extends MShape
{
	private static final long serialVersionUID = 1L;

	private final VisualNode itsFromNode;
	private final VisualNode itsToNode;
	private int x1;
	private int x2;
	private int y1;
	private int y2;

	public VisualArc(VisualNode theFromNode, VisualNode theToNode)
	{
		super("");
		itsFromNode = theFromNode;
		itsToNode = theToNode;
		calcBounds();
	}

	public Rectangle calcBounds()
	{
		Point p1a = itsFromNode.getConnectPoint();
		Point p2a = itsToNode.getConnectPoint();
		Point p1 = new Point(Math.min(p1a.x, p2a.x), Math.min(p1a.y, p2a.y));
		Point p2 = new Point(Math.max(p1a.x, p2a.x), Math.max(p1a.y, p2a.y));
		int xmin = Math.min(p1.x, p2.x);
		int xmax = Math.max(p1.x, p2.x);
		int ymin = Math.min(p1.y, p2.y);
		int ymax = Math.max(p1.y, p2.y);
		int w = xmax - xmin;
		int h = ymax - ymin;
		setBounds(xmin, ymin, w, h);
		return getBounds();
	}

	@Override
	public void setBounds(int x, int y, int width, int height)
	{
		super.setBounds(x, y, width, height);
		Rectangle r = this.getBounds();
		Point p1a = itsFromNode.getConnectPoint();
		Point p2a = itsToNode.getConnectPoint();
		Rectangle r1, r2;
		if (p1a.x <= p2a.x )
		{
			r1 = itsFromNode.getBounds();
			r2 = itsToNode.getBounds();
		}
		else
		{
			r1 = itsToNode.getBounds();
			r2 = itsFromNode.getBounds();
		}
		float aSlope = height/(float)width; // could by /0

		if ((p1a.x > p2a.x && p1a.y > p2a.y) || (p1a.x < p2a.x && p1a.y < p2a.y)) //topleft to bottomright
		{
			if (r1.height/(float)r1.width > aSlope)
			{
				x1 = r.x + Math.round(r1.width / 2f);
				y1 = r.y + Math.round(aSlope * r1.width / 2f);
			}
			else
			{
				x1 = r.x + Math.round(r1.height  / aSlope / 2f);
				y1 = r.y + Math.round(r1.height / 2f);
			}
			if (r2.height/(float)r2.width > aSlope)
			{
				x2 = r.x + r.width - Math.round(r2.width / 2f);
				y2 = r.y + r.height - Math.round(aSlope * r2.width / 2f);
			}
			else
			{
				x2 = r.x + r.width - Math.round(r2.height / aSlope / 2f);
				y2 = r.y + r.height - Math.round(r2.height / 2f);
			}
		}
		else //topright to bottomleft
		{
			if (r1.height/(float)r1.width > aSlope)
			{
				x2 = r.x + Math.round(r1.width / 2f);
				y1 = r.y + Math.round(aSlope * r1.width / 2f);
			}
			else
			{
				x2 = r.x + Math.round(r1.height / aSlope / 2f);
				y1 = r.y + Math.round(r1.height / 2f);
			}
			if (r2.height/(float)r2.width > aSlope)
			{
				x1 = r.x + r.width - Math.round(r2.width / 2f);
				y2 = r.y + r.height - Math.round(aSlope * r2.width / 2f);
			}
			else
			{
				x1 = r.x + r.width - Math.round(r2.height / aSlope / 2f);
				y2 = r.y + r.height - Math.round(r2.height / 2f);
			}
		}
	}

	@Override
	public void paint(java.awt.Graphics g)
	{
		super.paint(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setStroke(new BasicStroke(1.5f));
		g2.setColor(Color.black);
		g2.drawLine(x1, y1, x2, y2);
//		g2.drawString(itsDescription, (x1+x2)/2 + 10, (y1+y2)/2 + 10);

		//arrow head
		int aSize = 15;
		double PHI = Math.toRadians(20);

		Point p1a = itsFromNode.getConnectPoint();
		Point p2a = itsToNode.getConnectPoint();
		int dy = y1 - y2;
		int dx = x1 - x2;
		double theta = Math.atan2(dy, dx);

		Polygon tmpPoly=new Polygon();
		if (p1a.y <= p2a.y)
		{
			tmpPoly.addPoint(x2, y2);							// arrow tip
			tmpPoly.addPoint(x2 + (int)(aSize * Math.cos(theta - PHI)), y2 + (int)(aSize * Math.sin(theta - PHI)));
			tmpPoly.addPoint(x2 + (int)(aSize * Math.cos(theta + PHI)), y2 + (int)(aSize * Math.sin(theta + PHI)));
			tmpPoly.addPoint(x2, y2);							// arrow tip
		}
		else
		{
			theta += Math.PI;
			tmpPoly.addPoint(x1, y1);							// arrow tip
			tmpPoly.addPoint(x1 + (int)(aSize * Math.cos(theta - PHI)), y1 + (int)(aSize * Math.sin(theta - PHI)));
			tmpPoly.addPoint(x1 + (int)(aSize * Math.cos(theta + PHI)), y1 + (int)(aSize * Math.sin(theta + PHI)));
			tmpPoly.addPoint(x1, y1);							// arrow tip
		}
		g2.setStroke(new BasicStroke(2f));
//		g2.drawPolygon(tmpPoly);
		g2.fillPolygon(tmpPoly);
	}

	public boolean containsFromShape(MShape aShape)
	{
		return itsFromNode.equals(aShape);
	}

	public MShape getConnectedShape(MShape aShape)
	{
		if(itsFromNode.equals(aShape))
			return itsToNode;
		else if(itsToNode.equals(aShape))
			return itsFromNode;
		else
			return null;
	}
}
