/**
 * @author marvin
 * DTDResolver to determine which DTD to use.
 * TODO check if systemId is valid (using XMLType), else return null
 * TODO does not reach catch if systemId is null
 */

package nl.liacs.subdisc;

import org.xml.sax.*;

public class DTDResolver implements EntityResolver
{
	public InputSource resolveEntity(String publicId, String systemId)
	{
		try
		{
			return new InputSource(this.getClass().getResourceAsStream(systemId.substring(systemId.lastIndexOf("/")).toLowerCase()));
		}
		catch (Exception e)
		{
//			new ErrorDialog(e, ErrorDialog.DTDError);
		}
		// if systemId is null or other error assume and try mrml
		return null;
	}
}