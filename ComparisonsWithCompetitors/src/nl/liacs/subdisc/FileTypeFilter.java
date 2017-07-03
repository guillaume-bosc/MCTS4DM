/**
 * TODO return FileNameExtensionFilter based on FileType enum
 */
package nl.liacs.subdisc;

import java.io.*;

import javax.swing.filechooser.FileFilter;

public class FileTypeFilter extends FileFilter
{
	private final FileType itsFileType;

	/**
	 * This class will change and return an instance of the FileNameExtension
	 * class, based on the FileType.
	 * @param theFileType
	 */
	public FileTypeFilter(FileType theFileType)
	{
		itsFileType = theFileType;
	}

	@Override
	public boolean accept(File theFile)
	{
		if(theFile.isDirectory())
			return true;

		String aFileName = theFile.getName().toLowerCase();

		for (String s : itsFileType.getExtensions())
			if (aFileName.endsWith(s))
				return true;

		return false;
	}

	@Override
	public String getDescription() { return itsFileType.DESCRIPTION; }
}
