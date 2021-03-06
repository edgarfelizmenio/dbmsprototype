package testmodule.tests;

import java.lang.reflect.InvocationTargetException;

import testmodule.Test;
import testmodule.exceptions.TestException;


import dbms.buffermanager.BufferManager;
import dbms.buffermanager.exceptions.PageNotPinnedException;
import dbms.buffermanager.exceptions.PagePinnedException;
import dbms.diskspacemanager.DiskSpaceManager;
import dbms.diskspacemanager.exceptions.BadFileException;
import dbms.diskspacemanager.exceptions.BadPageIDException;
import dbms.diskspacemanager.exceptions.DBFileException;
import dbms.diskspacemanager.page.Page;

/**
 * Tests if the following cases are handled properly:
 * <ul>
 * <li>Pinning an allocated page.</li>
 * <li>Modifying a pinned page.</li>
 * <li>Flushing changes of page to disk.</li>
 * <li>Unpinning a modified page.</li>
 * <li>Allocating a new page (new page must be empty).</li>
 * </ul>
 */
public class Test12 implements Test {

	public void execute() throws DBFileException, BadFileException,
			BadPageIDException, TestException, NoSuchMethodException,
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

	private void testPolicy(String policy) throws InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			SecurityException, InvocationTargetException,
			NoSuchMethodException, ClassNotFoundException, DBFileException,
			BadFileException, BadPageIDException, TestException,
			NoSuchFieldException, PagePinnedException, PageNotPinnedException {

		int poolSize = 20;
		String filename = "test";
		DiskSpaceManager.getInstance().createFile(filename, 0);
		BufferManager bm = new BufferManager(poolSize, policy);

		System.err.println("Testing " + policy + "...");
		
		bm.newPage(filename, bm.getPoolSize() * 2);
		bm.unpinPage(filename, 0, false);
		
		for (int i = 0; i < bm.getPoolSize() + 1; i++) {
			Page p = bm.pinPage(filename, i);
			if (p == null) {
				throw new TestException("Pinning page failed!");
			}
			System.err.println("after pinPage " + i);
			String dataStr = "This is test 8 for page " + i;
			p.setContents(dataStr.getBytes());
			bm.flushPage(filename, i);
			System.err.println("after flushPage " + i);
			bm.unpinPage(filename, i, true);
		}
		
		int pageNumber = bm.newPage(filename, 1);
		if (pageNumber == Page.NO_PAGE_NUMBER) {
			throw new TestException("newPage failed!");
		}
		Page p = bm.findPage(filename, pageNumber);
		
		// Verify that page is empty
		boolean empty = true;
		byte contents[] = p.getContents();
		for (int i = 0; i < Page.PAGE_SIZE; i++) {
			if (contents[i] != '\0') {
				empty = false;
				break;
		}}
		
		if (!empty) {
			System.err.println("Test failed: page is not empty.");
			for (int i = 0; i < Page.PAGE_SIZE; i++) {
				System.err.println(contents[i]);
			}
			throw new TestException("Page is not empty.");
		}
	}

}
