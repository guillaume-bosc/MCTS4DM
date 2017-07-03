package nl.liacs.subdisc.gui;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

import nl.liacs.subdisc.*;
import nl.liacs.subdisc.FileHandler.Action;

public class SubDisc
{
	/*
	 * External jars required for correct execution.
	 * KNIME related jars are not required, when used as a KNIME plugin,
	 * KNIME loads is own jfreechart, jcommon and KNIME related jars.
	 */
	/*
	 * jfreechart, jcommon licence:
	 * Like JFreeChart, JCommon is also licensed under the terms of the GNU
	 * Lesser General Public Licence.
	 */
	/* 
	 * Jama licence:
	 * Copyright Notice This software is a cooperative product of
	 * The MathWorks and the National Institute of Standards and Technology
	 * (NIST) which has been released to the public domain.
	 * Neither The MathWorks nor NIST assumes any responsibility whatsoever
	 * for its use by other parties, and makes no guarantees, expressed or
	 * implied, about its quality, reliability, or any other characteristic.
	 */
	private static final String[] JARS = {
		// for drawing
		"jfreechart-1.0.14.jar",
		"jcommon-1.0.17.jar",
		// for propensity score, Rob
		"weka.jar",
		// for Cook's distance only
		"Jama-1.0.2.jar",
		// for KNIME
//		"knime-core.jar",
//		"org.eclipse.osgi_3.6.1.R36x_v20100806.jar",
//		"knime-base.jar",
//		"org.eclipse.core.runtime_3.6.0.v20100505.jar",
//		"org.knime.core.util_4.1.1.0034734.jar",
	};
	
	
	
	public static void main(String[] args)
	{
		

		
		
		
		checkLibs();
		if (!GraphicsEnvironment.isHeadless() && (SplashScreen.getSplashScreen() != null))
		{
			// assume it is an XML-autorun experiment
			if (args.length > 0)
				SplashScreen.getSplashScreen().close();
			else
			{
				try { Thread.sleep(3000); }
				catch (InterruptedException e) {};
				SplashScreen.getSplashScreen().close();
			}
		}

		if (XMLAutoRun.autoRunSetting(args))
			return;

		FileHandler aLoader = new FileHandler(Action.OPEN_FILE);
		Table aTable = aLoader.getTable();
		SearchParameters aSearchParameters = aLoader.getSearchParameters();

		if (aTable == null)
			new MiningWindow();
		else if (aSearchParameters == null)
			new MiningWindow(aTable);
		else
			new MiningWindow(aTable, aSearchParameters);
	}

	// may move to a separate class
	private static void checkLibs() {
/*
		// spaces in paths may give problems, leave code in just in case
		String path = SubDisc.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		String decodedPath;
		try {
			decodedPath = URLDecoder.decode(path, "UTF-8");
			path = new File(path).getParentFile().getPath();
		} catch (UnsupportedEncodingException e) {}
*/
		File dir = new File(new File("").getAbsolutePath());
		File libs = null;
		for (File f : dir.listFiles()) {
			if ("libs".equals(f.getName())) {
				libs = f;
				break;
			}
		}
		System.out.println("Starting Cortana from:");
		System.out.println("\t" + dir.getAbsolutePath());
		System.out.println("Looking for /libs/ directory...");
		System.out.format("/libs/ directory %sfound:%n", libs == null ? "not " : "");
		System.out.println("\t" + (libs == null ? "" : libs.getAbsolutePath()));

		if (libs != null) {
			System.out.format("Looking for required jars (%d)...%n",
						JARS.length);
			checkJars(libs);
		} else {
			System.out.println("Most drawing functionality will not work.");
		}
	}

	private static void checkJars(File libsDir) {
		final String[] files = libsDir.list();
		List<String> notFound = new ArrayList<String>();
		OUTER: for (String jar : JARS) {
				for (String file : files) {
					if (jar.equals(file)) {
						System.out.format("\tFound: '%s'.%n", jar);
						continue OUTER;
					}
				}
				notFound.add(jar);
			}

		if (!notFound.isEmpty())
			tryHarder(notFound, files);
	}

	/*
	 * If another version is found it might work.
	 * Newer version may have removed deprecated methods, older version may
	 * not have implemented some of the required methods.
	 * 
	 * TODO cortana.mf's Class-Path attribute defines the required jars,
	 * other libraries will not be loaded automatically, could be done here.
	 */
	private static void tryHarder(List<String> jars, String[] files) {
		OUTER: for (String jar : jars) {
				String base = jar.substring(0, jar.indexOf("-"));
				for (String file : files) {
					if (file.startsWith(base)) {
						System.out.format("\tFound: '%s', ('%s' expected).%n",
									file,
									jar);
						continue OUTER;
					}
				}
				System.out.format("\t'%s' not found, some functions will not work.%n", jar);
			}
	}
}
