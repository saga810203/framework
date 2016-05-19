package org.jfw.util.web.fileupload;

import java.io.IOException;

public interface UploadItemIterator {
	boolean hasNext() throws IOException;
	Item next() throws IOException;
	void clean();
}
