package nl.liacs.subdisc;

import java.io.*;

/*
 * This class is copied straight from the Internet.
 * FileType/ FileTypeFilter provide similar functionality, TODO merge.
 */
@Deprecated
public class OnlyExt implements FilenameFilter
{
	private final String ext;

	public OnlyExt(String ext)
	{
		this.ext = "." + ext;
	}

	@Override
	public boolean accept(File dir, String name)
	{
		return name.endsWith(ext);
	}
}
