package nl.liacs.subdisc;

import org.xml.sax.*;

public class XMLErrorHandler implements ErrorHandler
{
	/** 
	 * For now only use one errorHandler, may change.
	 */
	public static final XMLErrorHandler THE_ONLY_INSTANCE =
							new XMLErrorHandler();

	// uninstantiable
	private XMLErrorHandler() {};

	@Override
	public void error(SAXParseException e) throws SAXException
	{
		logWarning(e);
	}

	@Override
	public void fatalError(SAXParseException e) throws SAXException
	{
		logWarning(e);
	}

	@Override
	public void warning(SAXParseException e) throws SAXException
	{
		logWarning(e);
	}

	private void logWarning(SAXParseException e)
	{
		System.out.println(e.getMessage());
		System.out.println(e.getLineNumber());
//		e.printStackTrace();
	}
}
