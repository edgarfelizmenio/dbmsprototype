package main.tests;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import main.Test;
import main.exceptions.TestException;
import buffermanager.BufferManager;
import buffermanager.Frame;
import buffermanager.database.FileSystem;
import buffermanager.database.exceptions.BadFileException;
import buffermanager.database.exceptions.BadPageNumberException;
import buffermanager.database.exceptions.DBFileException;
import buffermanager.exceptions.PageNotPinnedException;
import buffermanager.exceptions.PagePinnedException;

/**
 * Tests if the following cases are handled properly:
 * <ol>
 * <li>Unpinning a page twice.</li>
 * <li>Pinning a nonexistent page.</li>
 * <li>Unpinning a nonexistent page.</li>
 * <li>Freeing a page that is still pinned.</li>
 * <li>Filling the buffer pool with pinned pages and pinning 1 more page.</li>
 * </ol>
 */
public class Test9 implements Test {

	@Override
	public void execute() throws DBFileException, BadFileException,
			BadPageNumberException, TestException, NoSuchMethodException,
			SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchFieldException, InstantiationException,
			ClassNotFoundException, PagePinnedException, PageNotPinnedException {

		String[] policies = { "LRUPolicy", "MRUPolicy", "ClockPolicy",
				"RandomPolicy" };

		for (String policy : policies) {
			testPolicy(policy);
		}
	}

	@SuppressWarnings("unchecked")
	private void testPolicy(String policy) throws InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			SecurityException, InvocationTargetException,
			NoSuchMethodException, ClassNotFoundException, DBFileException,
			BadFileException, BadPageNumberException, TestException,
			NoSuchFieldException, PagePinnedException, PageNotPinnedException {

		int poolSize = 20;
		String filename = "test";
		FileSystem.getInstance().createFile(filename, 0);
		BufferManager bm = new BufferManager(poolSize, policy);

		System.out.println("Testing " + policy + "...");

		int[] pageIds = new int[30];

		ClassLoader cl = Test9.class.getClassLoader();
		Class<Frame> frameClass = (Class<Frame>) cl.loadClass(Frame.class
				.getName());
		Field pageNumField = frameClass.getDeclaredField("pageNum");
		pageNumField.setAccessible(true);

		// Allocate 10 pages to database
		for (int i = 0; i < 10; i++) {
			Frame f = bm.newPage(1, filename);
			if (f == null) {
				throw new TestException("newPage failed");
			}
			pageIds[i] = pageNumField.getInt(f);
		}

		System.out.println("Allocated 10 pages successful");

		// Try to unpin a pinned page twice
		bm.unpinPage(pageIds[0], filename, false);
		System.out.println("Unpinning of a pinned page successful");

		// Try to unpin an unpinned page
		boolean success = true;
		try {
			bm.unpinPage(pageIds[0], filename, false);
		} catch (PageNotPinnedException pnpe) {
			success = false;
		}

		if (success) {
			throw new TestException("Unpinning of an unpinned page succeeded!");
		}
		System.out
				.println("Unpinning of an unpinned page failed (as it should)");

		// Pin a nonexistent page
		success = true;
		Frame f;
		try {
			f = bm.pinPage(999, filename);
		} catch (BadPageNumberException bpne) {
			success = false;
		}

		if (success) {
			throw new TestException("Pinning of a non-existent page succeeded!");
		}
		System.out
				.println("Pinning of a non-existent page failed (as it should)");

		// Unpin a nonexistent page
		success = true;
		try {
			bm.unpinPage(999, filename, false);
		} catch (PageNotPinnedException pnpe) {
			success = false;
		}

		if (success) {
			throw new TestException(
					"Unpinning of a non-existent page succeeded!");
		}
		System.out
				.println("Unpinning of a non-existent page failed (as it should)");

		// Free a page that is still pinned
		f = bm.pinPage(pageIds[0], filename);
		if (f == null) {
			throw new TestException("Unable to pin page!");
		}
		System.out.println("Pinning of page successful");
		
		success = true;
		try {
			bm.freePage(filename, pageIds[0]);
		} catch(PagePinnedException ppe) {
			success = false;
		}
		if (success) {
			throw new TestException("Freeing a pinned page succeeded!");
		}
		System.out.println("Freeing a pinned page failed (as it should)");
		
		// Free all allocated pages
		for (int i = 0; i < 10; i++) {
			bm.unpinPage(pageIds[i], filename,false);
			bm.freePage(filename, pageIds[i]);
		}
		System.out.println("Freeing allocated pages successful");
		
		// Allocate new buffer manager
		bm = new BufferManager(poolSize, policy);
		
		// Fill up buffer with pinned pages
		for (int i = 0; i < bm.getPoolSize(); i++) {
			f = bm.newPage(1, filename);
			if (f == null) {
				throw new TestException("newPage failed");
			}
			pageIds[i] = pageNumField.getInt(f);
		}
		System.out.println("Allocate pages successful");

		// Try to pin one more page
		f = bm.newPage(1, filename);
		if (f != null) {
			throw new TestException("Pinning a page in a full buffer succeeded!");
		}
		System.out.println("Pinning a page in a full buffer failed (as it should)");
	
		// Free all allocated pages
		for (int i = 0; i < bm.getPoolSize(); i++) {
			bm.unpinPage(pageIds[i], filename, false);
			bm.freePage(filename, pageIds[i]);
		}
		System.out.println("Freeing allocated pages successful");
		
	}

}
