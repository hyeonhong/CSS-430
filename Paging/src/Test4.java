/**
 *
 * @author      Hyeon Hong
 * @date        08/18/2016
 * @assignment  Lab 4
 *
 *
 * Test4 class has been modified from initial version to measure the performance of the average read and write time.
 *
 *
 */


import java.util.Date;
import java.util.Random;

class Test4 extends Thread {
	private boolean enabled;
	private int testcase;
	private long startTime;
	private long endTime;
	private long totalReadTime;
	private long totalWriteTime;
	private double averageReadTime;
	private double averageWriteTime;
	private byte[] wbytes;
	private byte[] rbytes;
	private Random rand;

	private void getPerformance(String msg) {
		if (enabled == true)
			SysLib.cout("\tTest " + msg + "(cache enabled): " + "\n\t\tAverage Read Time: " + averageReadTime
					+ "\n\t\tAverage Write Time: " + averageWriteTime + "\n");
		else
			SysLib.cout("\tTest " + msg + "(cache disabled): " + "\n\t\tAverage Read Time: " + averageReadTime
					+ "\n\t\tAverage Write Time: " + averageWriteTime + "\n");
	}

	private void read(int blk, byte[] bytes) {
		if (enabled == true)
			SysLib.cread(blk, bytes);
		else
			SysLib.rawread(blk, bytes);
	}

	private void write(int blk, byte[] bytes) {
		if (enabled == true)
			SysLib.cwrite(blk, bytes);
		else
			SysLib.rawwrite(blk, bytes);
	}

	private void randomAccess() {
		totalReadTime = 0;
		totalWriteTime = 0;
		averageReadTime = 0;
		averageWriteTime = 0;
		int[] accesses = new int[200];
		for (int i = 0; i < 200; i++) {
			accesses[i] = Math.abs(rand.nextInt() % 512);
			// SysLib.cout( accesses[i] + " " );
		}
		// SysLib.cout( "\n" );
		for (int i = 0; i < 200; i++) {
			for (int j = 0; j < 512; j++)
				wbytes[j] = (byte) (j);
			// start time
			startTime = new Date().getTime();
			write(accesses[i], wbytes);
			endTime = new Date().getTime();
			// end time
			totalWriteTime += (endTime - startTime);
		}
		// average write time
		averageWriteTime = 1.0 * totalWriteTime / 200;
		
		for (int i = 0; i < 200; i++) {
			startTime = new Date().getTime();
			read(accesses[i], rbytes);
			endTime = new Date().getTime();
			for (int k = 0; k < 512; k++) {
				if (rbytes[k] != wbytes[k]) {
					SysLib.cerr("ERROR\n");
					SysLib.exit();
				}
			}
			totalReadTime += (endTime - startTime);
		}
		// average read time
		averageReadTime = 1.0 * totalReadTime / 200;
	}

	private void localizedAccess() {
		totalReadTime = 0;
		totalWriteTime = 0;
		averageReadTime = 0;
		averageWriteTime = 0;
		for (int i = 0; i < 20; i++) {
			for (int j = 0; j < 512; j++)
				wbytes[j] = (byte) (i + j);
			for (int j = 0; j < 1000; j += 100) {
				startTime = new Date().getTime();
				write(j, wbytes);
				endTime = new Date().getTime();
				totalWriteTime += (endTime - startTime);
			}

			for (int j = 0; j < 1000; j += 100) {
				startTime = new Date().getTime();
				read(j, rbytes);
				endTime = new Date().getTime();
				totalReadTime += (endTime - startTime);
				for (int k = 0; k < 512; k++) {
					if (rbytes[k] != wbytes[k]) {
						SysLib.cerr("ERROR\n");
						SysLib.exit();
					}
				}
			}
		}
		// average write time
		averageWriteTime = 1.0 * totalWriteTime / 200;
		// average read time
		averageReadTime = 1.0 * totalReadTime / 200;
	}

	private void mixedAccess() {
		totalReadTime = 0;
		totalWriteTime = 0;
		averageReadTime = 0;
		averageWriteTime = 0;
		int[] accesses = new int[200];
		for (int i = 0; i < 200; i++) {
			if (Math.abs(rand.nextInt() % 10) > 8) {
				// random
				accesses[i] = Math.abs(rand.nextInt() % 512);
			} else {
				// localized
				accesses[i] = Math.abs(rand.nextInt() % 10);
			}
		}
		for (int i = 0; i < 200; i++) {
			for (int j = 0; j < 512; j++)
				wbytes[j] = (byte) (j);
			startTime = new Date().getTime();
			write(accesses[i], wbytes);
			endTime = new Date().getTime();
			totalWriteTime += (endTime - startTime);
		}
		// average write time
		averageWriteTime = 1.0 * totalWriteTime / 200;

		for (int i = 0; i < 200; i++) {
			startTime = new Date().getTime();
			read(accesses[i], rbytes);
			endTime = new Date().getTime();
			totalReadTime += (endTime - startTime);
			for (int k = 0; k < 512; k++) {
				if (rbytes[k] != wbytes[k]) {
					SysLib.cerr("ERROR\n");
					SysLib.exit();
				}
			}
		}
		// average read time
		averageReadTime = 1.0 * totalReadTime / 200;
	}

	private void adversaryAccess() {
		totalReadTime = 0;
		totalWriteTime = 0;
		averageReadTime = 0;
		averageWriteTime = 0;
		for (int i = 0; i < 20; i++) {
			for (int j = 0; j < 512; j++)
				wbytes[j] = (byte) (j);
			for (int j = 0; j < 10; j++) {
				startTime = new Date().getTime();
				write(i + 100 * j, wbytes);
				endTime = new Date().getTime();
				totalWriteTime += (endTime - startTime);
			}
		}
		// average write time
		averageWriteTime = 1.0 * totalWriteTime / 200;
		
		for (int i = 0; i < 20; i++) {
			for (int j = 0; j < 10; j++) {
				startTime = new Date().getTime();
				read(i + 100 * j, rbytes);
				endTime = new Date().getTime();
				totalReadTime += (endTime - startTime);
				for (int k = 0; k < 512; k++) {
					if (rbytes[k] != wbytes[k]) {
						SysLib.cerr("ERROR\n");
						SysLib.exit();
					}
				}
			}
		}
		// average read time
		averageReadTime = 1.0 * totalReadTime / 200;
	}

	public Test4(String[] args) {
		enabled = args[0].equals("enabled") ? true : false;
		testcase = Integer.parseInt(args[1]);
		wbytes = new byte[Disk.blockSize];
		rbytes = new byte[Disk.blockSize];
		rand = new Random();
	}

	public void run() {
		SysLib.flush();
		switch (testcase) {
		case 1:
			randomAccess();
			getPerformance("random accesses");
			break;
		case 2:
			localizedAccess();

			getPerformance("localized accesses");
			break;
		case 3:
			mixedAccess();
			getPerformance("mixed accesses");
			break;
		case 4:
			adversaryAccess();
			getPerformance("adversary accesses");
			break;
		case 5:
			randomAccess();
			getPerformance("random accesses");

			localizedAccess();
			getPerformance("localized accesses");

			mixedAccess();
			getPerformance("mixed accesses");

			adversaryAccess();
			getPerformance("adversary accesses");
			break;
		}
		SysLib.exit();
	}
}
