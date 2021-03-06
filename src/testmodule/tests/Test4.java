package testmodule.tests;

import java.lang.reflect.InvocationTargetException;

import testmodule.Test;
import testmodule.exceptions.TestException;


import dbms.buffermanager.BufferManager;
import dbms.buffermanager.exceptions.PageNotPinnedException;
import dbms.diskspacemanager.DiskSpaceManager;
import dbms.diskspacemanager.exceptions.BadFileException;
import dbms.diskspacemanager.exceptions.BadPageIDException;
import dbms.diskspacemanager.exceptions.DBFileException;
import dbms.diskspacemanager.page.Page;


/**
 * Tests the clock replacement policy.
 * 
 */
public class Test4 implements Test {

	public void execute() throws DBFileException, BadFileException,
			BadPageIDException, TestException, NoSuchMethodException,
			SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchFieldException, InstantiationException,
			ClassNotFoundException, PageNotPinnedException {

		int poolSize = 20;
		String filename = "test";
		DiskSpaceManager.getInstance().createFile(filename, 0);
		BufferManager bm = new BufferManager(poolSize, "ClockPolicy");

		int pageNumber;

		// allocate some pages
		pageNumber = bm.newPage(filename, 5 * bm.getPoolSize());
		bm.unpinPage(filename, pageNumber, false);

		int[] frameNumbers = new int[bm.getPoolSize()];

		for (int i = 0; i < bm.getPoolSize(); i++) {
			Page p = bm.pinPage(filename, i + 5);
			if (p == null) {
				throw new TestException("Unable to pin page");
			}

			frameNumbers[i] = bm.findFrame(filename, i + 5);
			if (frameNumbers[i] < 0 || frameNumbers[i] >= bm.getPoolSize()) {
				throw new TestException("Invalid frame returned");
			}

			System.err.println("Page " + (i + 5) + " at frame "
					+ frameNumbers[i] + " is pinned.");
		}

		// try pinning an extra page
		Page p = bm.pinPage(filename, bm.getPoolSize() + 6);
		if (p != null) {
			throw new TestException("Pinned page in full buffer");
		}

		// Start unpinning pages
		for (int i = bm.getPoolSize() - 1; i >= 0; i--) {
			bm.unpinPage(filename, i + 5, true);
			System.err.println("Page " + (i + 5) + " at frame "
					+ frameNumbers[i] + " is unpinned.");
		}

		// Start pinning a new set of pages again. The page frames should be
		// exactly the same order as the previous one. Clock in that case will
		// resemble MRU.
		for (int i = bm.getPoolSize(); i < 2 * bm.getPoolSize(); i++) {
			p = bm.pinPage(filename, i + 5);
			if (p == null) {
				throw new TestException("Unable to pin page");
			}

			int frameNumber = bm.findFrame(filename, i + 5);
			System.err.println("Page " + (i + 5) + " pinned in frame "
					+ frameNumber);

			if (frameNumber != frameNumbers[i - bm.getPoolSize()]) {
				throw new TestException("Frame number incorrect!");
			}
		}

		// Unpin half the pages in order.
		for (int i = bm.getPoolSize(); i < 2 * bm.getPoolSize(); i += 2) {
			bm.unpinPage(filename, i + 5, true);
			System.err.println("Page " + (i + 5) + " at frame "
					+ frameNumbers[i - bm.getPoolSize()] + " is unpinned.");
		}

		// Now, pin a new set of pages. Again, it should resemble the previous
		// sequence. In this case, Clock behaves as LRU
		for (int i = 2 * bm.getPoolSize(); i < 3 * bm.getPoolSize(); i += 2) {
			p = bm.pinPage(filename, i + 5);
			if (p == null) {
				throw new TestException("Unable to pin page");
			}

			int frameNumber = bm.findFrame(filename, i + 5);
			bm.unpinPage(filename, i + 5, true);
			bm.unpinPage(filename, i - bm.getPoolSize() + 6, true);

			System.err.println("Page " + (i + 5) + " pinned in frame "
					+ frameNumber);
			if (frameNumber != frameNumbers[i - (2 * bm.getPoolSize())]) {
				throw new TestException("Frame number incorrect!");
			}
		}
	}

}
