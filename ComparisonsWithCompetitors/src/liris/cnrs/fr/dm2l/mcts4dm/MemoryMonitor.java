package liris.cnrs.fr.dm2l.mcts4dm;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Get the current JVM heap size
 * 
 * @author Mehdi Kaytoue
 * @date June 2017
 *
 */
public class MemoryMonitor implements Runnable {

	long maxMemoryUsed = 0;
	long sleepTimeInMs = 500;
	long beginMemory;
	public MemoryMonitor(long sleepTimeInMs, AtomicBoolean stopMemMonitor) throws Exception {
		this.sleepTimeInMs = sleepTimeInMs;
		this.stopMemMonitor = stopMemMonitor;
		this.beginMemory = memoryUsage();
	}
	AtomicBoolean stopMemMonitor;

	public void run()
	{
		try {
			while(!stopMemMonitor.get()) {
				long memInBytes = MemoryMonitor.memoryUsage();
				if (memInBytes > maxMemoryUsed) maxMemoryUsed = memInBytes-beginMemory;
				//System.err.println("Mem usage in thread: " + memInBytes);
				Thread.sleep(sleepTimeInMs);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	public long getMaxMemoryUsed() { return this.maxMemoryUsed; }
	public void reset() { maxMemoryUsed = 0;}

	/**
	 * The memory in bytes used by the JVM.
	 * Use the Oracle tool JMAP
	 * @return Memory in bytes
	 * 
	 * @throws Exception
	 */
	public static long memoryUsage() throws Exception
	{
		long pid = getPID();
		java.lang.Process p = Runtime.getRuntime().exec("jmap -histo " + pid);
		p.waitFor();
		BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line = "";
		StringBuffer sb = new StringBuffer();
		while ((line = reader.readLine())!= null) sb.append(line + "\n");
		String[] lines = sb.toString().split("\n");
		int lastSpace = lines[lines.length - 1].lastIndexOf(' ');
		return Long.parseLong(lines[lines.length - 1].substring(lastSpace+1));
	}

	/**
	 * Waiting for Java9 and the new process API... This should work on UNIX MacOs and Windows.
	 * @return The pid of the CURRENT THREAD!
	 * @throws Exception
	 */
	public static long getPID() throws Exception
	{
		java.lang.management.RuntimeMXBean runtime = java.lang.management.ManagementFactory.getRuntimeMXBean();
		java.lang.reflect.Field jvm = runtime.getClass().getDeclaredField("jvm");
		jvm.setAccessible(true);
		@SuppressWarnings("restriction")
		sun.management.VMManagement mgmt = (sun.management.VMManagement) jvm.get(runtime);
		java.lang.reflect.Method pid_method = mgmt.getClass().getDeclaredMethod("getProcessId");
		pid_method.setAccessible(true);
		return  (Integer) pid_method.invoke(mgmt);
	}

	static MemoryMonitor startDefaultMemoryMonitor(long sleepTimeInMs, AtomicBoolean stopMemMonitor){
		MemoryMonitor monitor;
		try {
			monitor = new MemoryMonitor(500,stopMemMonitor);
			Thread t = new Thread(monitor);
			t.start();
			return monitor;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args) throws Exception
	{
		AtomicBoolean stopMemMonitor = new AtomicBoolean(false);
		MemoryMonitor monitor = MemoryMonitor.startDefaultMemoryMonitor(500, stopMemMonitor);
		System.out.println("Mem usage in parent: " + monitor.getMaxMemoryUsed());
		Thread.sleep(1000);
		System.out.println("Mem usage in parent: " + monitor.getMaxMemoryUsed());
		Thread.sleep(1000);
		System.out.println("Mem usage in parent: " + monitor.getMaxMemoryUsed());
		Thread.sleep(1000);
		System.out.println("Mem usage in parent: " + monitor.getMaxMemoryUsed());
		Thread.sleep(1000);
		System.out.println("Mem usage in parent: " + monitor.getMaxMemoryUsed());
		stopMemMonitor.set(true);
	}

}
