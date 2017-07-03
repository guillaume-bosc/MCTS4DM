/**
 * @author marvin
 * This is a convenience class for XML Node creation. It is uninstantiable and
 * contains only static methods. Therefore it does not implement the
 * XMLNodeInterface.
 */
package nl.liacs.subdisc;

import org.w3c.dom.*;

public class XMLNode
{
	private XMLNode() {}

	/**
	 * Creates and adds a Node to theParentNode. The element name is always
	 * converted to lowercase.
	 * @param theParentNode the Node to which to add the new Node.
	 * @param theElementName the name of the new Node.
	 * @return the newly created Node.
	 */
	public static Node addNodeTo(Node theParentNode, String theElementName)
	{
		return theParentNode.appendChild(
				theParentNode.getOwnerDocument().createElement(theElementName.toLowerCase()));
	}

	/**
	 * Creates and adds a Node to theParentNode. The element name is always
	 * converted to lowercase, and for any <code>Object</code> passed in as a
	 * parameter its <code>toString()</code> method is used as input
	 * <code>String</code> for the <code>setTextContent()</code> method for the
	 * new Node. This works fine for build in Java types (eg. <code>Float</code>
	 * and <code>Double</code>), but may cause trouble for self defined
	 * <Code>Objects</code>. In that case override the <code>toString()</code>
	 * method for the <code>Object</code>, or pass in a <code>String</code>.
	 * @param theParentNode the Node to which to add the new Node.
	 * @param theElementName the name of the new Node.
	 * @param theTextContent the text content for the new Node.
	 */
	public static void addNodeTo(Node theParentNode, String theElementName, Object theTextContent)
	{
		theParentNode.appendChild(theParentNode.getOwnerDocument().createElement(theElementName.toLowerCase()))
				.setTextContent(theTextContent == null ? "" : theTextContent.toString());
	}
}
