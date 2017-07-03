/**
 * @author marvin
 * All classes that have their values written to a XML file (eg. autorun.xml)
 * should implement XMLNodeInterface. The classes should define how their
 * members' values should be written to, and loaded from, a XML file.
 */

package nl.liacs.subdisc;

import org.w3c.dom.*;

public interface XMLNodeInterface
{
	public void addNodeTo(Node theParentNode);
//	public void loadNodeData(Node theNode);
}
