package buffermanager.database;

import java.util.Arrays;

import buffermanager.page.Page;

class File {
	Page[] pages;
	int numPages;

	File(String filename, int numPages) {
		this.pages = new Page[numPages];
		this.numPages = numPages;
		for (int i = 0; i < numPages; i++) {
			this.pages[i] = Page.makePage();
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Page p: pages) {
			if (p == null) { 
				sb.append("[]\n");
			} else {
				sb.append(Arrays.toString(p.getContents()) + "\n");
			}
		}
		return sb.toString();
	}
}
