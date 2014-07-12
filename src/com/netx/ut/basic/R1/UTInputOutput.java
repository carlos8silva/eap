package com.netx.ut.basic.R1;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import com.netx.generics.R1.collections.IList;
import com.netx.generics.R1.time.Timestamp;
import com.netx.generics.R1.util.UnitTester;
import com.netx.generics.R1.util.Strings;
import com.netx.generics.R1.util.Tools;
import com.netx.basic.R1.shared.Globals;
import com.netx.basic.R1.io.*;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;


public class UTInputOutput extends UnitTester {

	// TYPE:
	public static final String IO_TEST_DIR = "basic.R1.io";
	
	// INSTANCE:
	private String _testLocation;
	private boolean _testExcelLock;
	private boolean _testJVMLock;
	//private Locale _systemLocale = new Locale("pt", "PT");
	private Locale _systemLocale = new Locale("en", "GB");

	@BeforeClass
	public void setUp() throws FileSystemException {
		_testLocation = getTestResourceLocation().getDirectory(IO_TEST_DIR).getAbsolutePath();
		_testExcelLock = false;
		_testJVMLock = false;
		Globals.setSystemLocale(_systemLocale);
		FileSystem.addDefaultFilenameFilter(new FilenameFilter() {
			public boolean accept(String filename) {
				if(filename.equals(".svn")) {
					return false;
				}
				return true;
			}
		});
	}

	@AfterClass
	public void exit() {}

	@Test
	public void testProtocolImpl() throws FileSystemException, ReadWriteException {
		// 1) Test a root path:
		final String basePath = getTestResourceLocation().getAbsolutePath();
		ProtocolImpl impl = UT.createImpl(basePath);
		assertEquals(impl.getAbsolutePath(), basePath);
		assertEquals(impl.getRelativePath(), "/");
		assertNull(impl.getParent());
		
		// 2) Test a relative path:
		final String relativePath = "/temp-dir";
		impl = UT.createImpl(basePath, relativePath);
		// Just in case:
		impl.delete();
		assertEquals(impl.getAbsolutePath(), basePath+Strings.replaceAll(relativePath, "/", "\\"));
		assertEquals(impl.getRelativePath(), relativePath);
		assertFalse(impl.exists());
		assertFalse(impl.getReadable());
		assertFalse(impl.getWritable());
		impl.mkdirs();
		assertTrue(impl.exists());
		assertTrue(impl.getReadable());
		assertTrue(impl.getWritable());
		assertEquals(impl.getSize(), 0L);
		impl.delete();
		assertFalse(impl.exists());
		assertFalse(impl.getReadable());
		assertFalse(impl.getWritable());
		
		// 3) Test non-canonical paths with root:
		impl = UT.createImpl("C:\\Some\\Random\\Path\\..");
		assertEquals(impl.getAbsolutePath(), "C:\\Some\\Random");
		impl = UT.createImpl("C:\\Some\\Random\\Path\\..\\.\\Path");
		assertEquals(impl.getAbsolutePath(), "C:\\Some\\Random\\Path");
		try {
			impl = UT.createImpl("C:\\Some\\Random\\Path\\..\\..\\..\\..");
			fail();
		}
		catch(IllegalArgumentException iae) {
			println(iae);
		}
		
		// 3) Test non-canonical paths with relative path:
		impl = UT.createImpl("C:\\Some\\Random\\Path", "/Somewhere/Lost/..");
		assertEquals(impl.getAbsolutePath(), "C:\\Some\\Random\\Path\\Somewhere");
		assertEquals(impl.getRelativePath(), "/Somewhere");
		impl = UT.createImpl("C:\\Some\\Random\\Path", "/Somewhere/../Somewhere/./Lost/.");
		assertEquals(impl.getAbsolutePath(), "C:\\Some\\Random\\Path\\Somewhere\\Lost");
		assertEquals(impl.getRelativePath(), "/Somewhere/Lost");
		try {
			impl = UT.createImpl("C:\\Some\\Random\\Path", "/Somewhere/../Lost/../..");
			fail();
		}
		catch(IllegalArgumentException iae) {
			println(iae);
		}

		// 4) Relative to root and current directory:
		impl = impl.getChild("in-the-forest");
		assertEquals(impl.getAbsolutePath(), "C:\\Some\\Random\\Path\\Somewhere\\Lost\\in-the-forest");
		assertEquals(impl.getRelativePath(), "/Somewhere/Lost/in-the-forest");
		impl = impl.getChild("/in-the-forest");
		assertEquals(impl.getAbsolutePath(), "C:\\Some\\Random\\Path\\in-the-forest");
		assertEquals(impl.getRelativePath(), "/in-the-forest");
		impl = impl.getChild("..");
		assertEquals(impl.getAbsolutePath(), "C:\\Some\\Random\\Path");
		assertEquals(impl.getRelativePath(), "/");
		try {
			impl = impl.getChild("/..");
			fail();
		}
		catch(IllegalArgumentException iae) {
			println(iae);
		}

		FileSystem fs = new FileSystem(_testLocation);
		Directory dir = fs.getDirectory("files-in-folders");
		assertEquals(dir.getFile("/files-in-folders/DOC/EBS.01.Spec_v1.0.doc").getRelativePath(), "/files-in-folders/DOC/EBS.01.Spec_v1.0.doc");
		assertEquals(dir.getFile("DOC/EBS.01.Spec_v1.0.doc").getRelativePath(), "/files-in-folders/DOC/EBS.01.Spec_v1.0.doc");
	}

	// TODO automate Excel and JVM locks
	@Test
	public void tFailExceptions() throws BasicIOException {
		// String constructor:
		FileSystem fs = new FileSystem("C:\\WINDOWS");
		// File constructor:
		fs = new FileSystem(new java.io.File("C:\\WINDOWS"));
		// Null args:
		try {
			fs = new FileSystem((String)null);
			fail();
		}
		catch(IllegalArgumentException iae) {
			println(iae);
		}
		try {
			fs = new FileSystem((java.io.File)null);
			fail();
		}
		catch(IllegalArgumentException iae) {
			println(iae);
		}
		try {
			fs = new FileSystem("C:\\", null, "password");
			fail();
		}
		catch(IllegalArgumentException iae) {
			println(iae);
		}
		try {
			fs = new FileSystem("C:\\", "username", null);
			fail();
		}
		catch(IllegalArgumentException iae) {
			println(iae);
		}
		try {
			fs = new FileSystem("C:\\WINDOWS?", "username", null);
			fail();
		}
		catch(IllegalArgumentException iae) {
			println(iae);
		}
		// Directory does not exist:
		try {
			fs = new FileSystem("C:\\AopD823khlw34S\\J8274KKL");
			fail();
		}
		catch(FileNotFoundException dnfe) {
			println(dnfe);
		}
		// Path refers to a file:
		try {
			fs = new FileSystem("C:\\WINDOWS\\explorer.exe");
			fail();
		}
		catch(FileNotFoundException dnfe) {
			println(dnfe);
		}
		// Create a directory and a read only file:
		fs = new FileSystem(_testLocation);
		Directory dir = fs.getDirectory("test-exceptions");
		if(dir != null) {
			try {
				dir.deleteContents();
			}
			catch(OperationFailedException ofe) {
				// This may happen if there are locked files in the directory:
				println("WARNING: could not delete file "+ofe.getMessage());
			}
		}
		else {
			dir = fs.mkdirs("test-exceptions");
		}
		File readOnly = dir.createFile("read-only-file");
		ExtendedWriter writer = new ExtendedWriter(readOnly);
		writer.write("this is one line on the file\r\n");
		writer.close();
		readOnly.setWritable(false);
		assertFalse(readOnly.getWritable());
		// Attempt to write to the file:
		try {
			writer = new ExtendedWriter(readOnly);
			fail();
		}
		catch(WriteAccessDeniedException wade) {
			println(wade);
		}
		// Attempt to delete the file:
		try {
			readOnly.getFileSystem().setIgnoreReadOnly(false);
			readOnly.delete();
			fail();
		}
		catch(WriteAccessDeniedException wade) {
			println(wade);
		}
		// Attempt to delete the parent directory:
		try {
			dir.delete();
			fail();
		}
		catch(OperationFailedException ofe) {
			println(ofe);
		}
		// Attempt to create a file with the same name:
		try {
			dir.createFile("read-only-file");
			fail();
		}
		catch(FileAlreadyExistsException fae) {
			println(fae);
		}
		// Delete the file:
		readOnly.getFileSystem().setIgnoreReadOnly(true);
		readOnly.delete();
		// Try to use it again:
		try {
			readOnly.getLastModified();
			fail();
		}
		catch(FileNotFoundException fnfe) {
			println(fnfe);
		}
		// Lock a file:
		String filename = "shopping-list.csv";
		File other = dir.createFile(filename);
		ExtendedOutputStream stream = other.getOutputStreamAndLock();
		// Attempt to open it to read:
		try {
			other.getInputStream();
			fail();
		}
		catch(FileLockedException fle) {
			println(fle);
		}
		// Attempt to open it to write:
		try {
			stream = other.getOutputStream();
			fail();
		}
		catch(FileLockedException fle) {
			println(fle);
		}
		// Attempt to lock it again:
		try {
			stream = other.getOutputStreamAndLock();
			fail();
		}
		catch(FileLockedException fle) {
			println(fle);
		}
		// Attempt to delete it (cannot because there are open streams):
		try {
			other.delete();
			fail();
		}
		catch(OperationFailedException ofe) {
			println(ofe);
		}
		// Attempt to access it externally while locked:
		// (internal lock and prompt user to open in Excel)
		if(_testExcelLock) {
			println("Please attempt to open file '"+other.getAbsolutePath()+"' with Excel.");
			println("This should result in an error message in Excel.");
			println("When finished, please press ENTER:");
			Tools.readInput();
		}
		// Close the stream:
		stream.close();
		// Now prompt the user to open the file externally, and when we try
		// to open it in this program, we should get a FileLockedException:
		if(_testExcelLock) {
			println("Please open file '"+other.getAbsolutePath()+"' with Excel.");
			println("This will cause the unit test to get a file lock exception.");
			println("Please press ENTER when the file is open:");
			Tools.readInput();
			try {
				stream = other.getOutputStreamAndLock();
				fail();
			}
			catch(FileLockedException fle) {
				println(fle);
			}
			// Close the stream:
			stream.close();
			println("Now please close Excel and press ENTER:");
			Tools.readInput();
		}
		// We should be able to open and lock the stream again:
		stream = other.getOutputStreamAndLock();
		stream.close();
		// Try to use the stream after it has been closed:
		try {
			stream.flush();
			fail();
		}
		catch(IllegalStateException ise) {
			println(ise);
		}

		// Open two concurrent streams:
		stream = other.getOutputStream();
		ExtendedOutputStream anotherStream = other.getOutputStream(true);
		stream.write("abc".getBytes());
		anotherStream.write("def".getBytes());
		stream.close();
		anotherStream.close();
		// Ensure we have all the contents:
		ExtendedReader reader = new ExtendedReader(other.getInputStream());
		assertEquals(reader.readLine(), "abcdef");
		reader.close();

		// Test mark() and reset():
		ExtendedInputStream in = other.getInputStream();
		// Mark not called yet:
		try {
			in.reset();
			fail();
		}
		catch(IllegalStateException ise) {
			println(ise);
		}
		// Call mark:
		in.mark(1000);
		char[] array = new char[] {'a', 'b', 'c', 'd', 'e', 'f'};
		for(int i=0; i<array.length; i++) {
			char c = (char)in.read();
			assertEquals(c, array[i]);
		}
		// This should return back to the beginning:
		in.reset();
		for(int i=0; i<array.length; i++) {
			char c = (char)in.read();
			assertEquals(c, array[i]);
		}
		in.close();
		try {
			in.mark(1000);
			fail();
		}
		catch(IllegalStateException ise) {
			println(ise);
		}
		
		// Open a stream to read, then lock it on another JVM:
		if(_testJVMLock) {
			in = other.getInputStream();
			// TODO launch this from within the test case, and always perform the check
			println("On another window, please run the LockStream class.");
			println("Please press ENTER when the program is running:");
			Tools.readInput();
			// Methods that run ok without exception:
			in.available();
			in.mark(1000);
			in.reset();
			// Methods that fail with a file locked exception:
			try {
				in.skip(10);
				fail();
			}
			catch(FileLockedException fle) {
				println(fle);
			}
			try {
				in.read();
				fail();
			}
			catch(FileLockedException fle) {
				println(fle);
			}
			// Note that if we open the stream again, skip()
			// works but read() does not work:
			in.close();
			in = other.getInputStream();
			// Methods that fail with a file locked exception:
			assertEquals(in.skip(1000), 1000);
			try {
				in.read();
				fail();
			}
			catch(FileLockedException fle) {
				println(fle);
			}
			reader = new ExtendedReader(in);
			assertFalse(reader.ready());
			try {
				reader.readLine();
				fail();
			}
			catch(FileLockedException fle) {
				println(fle);
			}
			reader.close();
			println("Please press ENTER when the program finishes:");
			Tools.readInput();
		}

		// Delete the file:
		other.delete();
		// Create sub-directories that are larger than 256 chars in length:
		String bigPath = "A876876BBN98273984738PPP454HHSDJK998";
		Directory bigDir = null;
		bigDir = dir.mkdirs(bigPath);
		for(int i=0; i<10; i++) {
			bigDir = bigDir.mkdirs(bigPath);
		}
		bigDir.listAll();
		bigDir.createFile("A_FILE.tmp");
		// Delete the parent directory:
		dir.delete(true);
	}
	
	@Test
	public void testBulkCopies() throws BasicIOException {
		FileSystem fs = new FileSystem(_testLocation);
		// Copy the files-in-folders dir as COPY:
		Directory src = fs.getDirectory("files-in-folders");
		long srcSize = src.getSize(true);
		Directory copy = fs.getDirectory("COPY");
		if(copy != null) {
			copy.delete(true);
		}
		src.copyTo(fs, "COPY");
		// Copy the files-listed dir into COPY:
		copy = fs.getDirectory("COPY");
		src = fs.getDirectory("files-listed");
		srcSize += src.getSize(true);
		src.copyTo(copy);
		// Check if dir sizes match:
		assertEquals(srcSize, copy.getSize(true));
		// Delete the newly created directory:
		copy.delete(true);
	}

	@Test
	// TODO on setUp, mark all .doc files as hidden (when we retrieve them from SVN, they are marked visible)
	public void testFileListing() throws FileSystemException, ReadWriteException {
		// 1) Go to folder 'files-in-folders':
		FileSystem fs = new FileSystem(_testLocation);
		Directory dir = fs.getDirectory("files-in-folders");
		// 2) List all files:
		String[] expectedResults = {"config.xml", "file1.tmp", "file2.tmp", "JSP FAQ.html", "log_20060313.csv", "servlet-2_4-fr-spec-doc.zip"};
		_checkExpectedResults(dir.listFiles(), expectedResults);
		// Check name and extension:
		File f = dir.getFile("servlet-2_4-fr-spec-doc.zip");
		assertEquals(f.getNameWithoutExtension(), "servlet-2_4-fr-spec-doc");
		assertEquals(f.getExtension(), "zip");
		// 3) List all folders:
		expectedResults = new String[] {"DOC", "PDF", "TXT"};
		_checkExpectedResults(dir.listDirectories(), expectedResults);
		// 4) List all:
		expectedResults = new String[] {
			"DOC", "PDF", "TXT", "config.xml", "file1.tmp", "file2.tmp", "JSP FAQ.html", "log_20060313.csv", "servlet-2_4-fr-spec-doc.zip"
		};
		_checkExpectedResults(dir.listAll(), expectedResults);
		// 5) Go to folder 'files-listed':
		dir = fs.getDirectory("files-listed");
		// 6) List all files:
		expectedResults = new String[] {
			"config.xml", "file1.tmp", "file2.tmp", "file-with-no-extension", "JSP FAQ.html", "log_20060313.csv", "servlet-2_4-fr-spec-doc.zip", "Design Decisions.doc", "Dev_Notes.doc", 
			"Dev_Notes2.docx", "DM.03.API_v0.1.doc", "EBS.01.Spec_v1.0.doc", "EBS.02.Userdoc_Java_v0.2_02.doc", "EBS.03.API_Java_v0.1.doc", "J2EE Developer's Guide.pdf", "J2EE Specification v1.4.pdf"
		};
		_checkExpectedResults(dir.listFiles(), expectedResults);
		// 7) List all:
		_checkExpectedResults(dir.listAll(), expectedResults);
		// 8) Apply a filter for *.* files on the folder:
		dir.addFilenameFilter(new WildcardFilenameFilter("*.*"));
		expectedResults = new String[] {
			"config.xml", "file1.tmp", "file2.tmp", "JSP FAQ.html", "log_20060313.csv", "servlet-2_4-fr-spec-doc.zip", "Design Decisions.doc", "Dev_Notes.doc", "Dev_Notes2.docx", 
			"DM.03.API_v0.1.doc", "EBS.01.Spec_v1.0.doc", "EBS.02.Userdoc_Java_v0.2_02.doc", "EBS.03.API_Java_v0.1.doc", "J2EE Developer's Guide.pdf", "J2EE Specification v1.4.pdf"
		};
		_checkExpectedResults(dir.listAll(), expectedResults);
		// 9) Apply a filter for *.doc on the fs:
		fs.addFilenameFilter(new WildcardFilenameFilter("*.doc"));
		expectedResults = new String[] {
			"Design Decisions.doc", "Dev_Notes.doc", "DM.03.API_v0.1.doc",
			"EBS.01.Spec_v1.0.doc", "EBS.02.Userdoc_Java_v0.2_02.doc", "EBS.03.API_Java_v0.1.doc"
		};
		_checkExpectedResults(dir.listAll(), expectedResults);
		// 10) Get rid of the folder's and fs's filter:
		fs = new FileSystem(_testLocation);
		dir = fs.getDirectory("files-listed");
		// 11) Appply a filter for *_* files using search options:
		SearchOptions options = new SearchOptions(new WildcardFilenameFilter("*_*"));
		expectedResults = new String[] {
			"log_20060313.csv", "servlet-2_4-fr-spec-doc.zip", "Dev_Notes.doc", "Dev_Notes2.docx",
			"DM.03.API_v0.1.doc", "EBS.01.Spec_v1.0.doc", "EBS.02.Userdoc_Java_v0.2_02.doc", "EBS.03.API_Java_v0.1.doc"
		};
		_checkExpectedResults(dir.listAll(options), expectedResults);
		// 12) .doc files are hidden; filter them:
		options = new SearchOptions(null, false);
		expectedResults = new String[] {
			"config.xml", "file1.tmp", "file2.tmp", "JSP FAQ.html", "log_20060313.csv", "servlet-2_4-fr-spec-doc.zip",
			"Dev_Notes2.docx", "file-with-no-extension", "J2EE Developer's Guide.pdf", "J2EE Specification v1.4.pdf"
		};
		_checkExpectedResults(dir.listFiles(options), expectedResults);
		// 13) Also sort the files in descending order:
		options = new SearchOptions(null, true, SearchOptions.ORDER.NAME_DESCENDING);
		Iterator<String> it = dir.listAll(options).iterator();
		assertEquals(it.next(), "servlet-2_4-fr-spec-doc.zip");
		assertEquals(it.next(), "log_20060313.csv");
		assertEquals(it.next(), "file2.tmp");
		assertEquals(it.next(), "file1.tmp");
		assertEquals(it.next(), "file-with-no-extension");
		assertEquals(it.next(), "config.xml");
		assertEquals(it.next(), "JSP FAQ.html");
		assertEquals(it.next(), "J2EE Specification v1.4.pdf");
		assertEquals(it.next(), "J2EE Developer's Guide.pdf");
		assertEquals(it.next(), "EBS.03.API_Java_v0.1.doc");
		assertEquals(it.next(), "EBS.02.Userdoc_Java_v0.2_02.doc");
		assertEquals(it.next(), "EBS.01.Spec_v1.0.doc");
		assertEquals(it.next(), "Dev_Notes2.docx");
		assertEquals(it.next(), "Dev_Notes.doc");
		assertEquals(it.next(), "Design Decisions.doc");
		assertEquals(it.next(), "DM.03.API_v0.1.doc");
		assertFalse(it.hasNext());
	}
	
	private void _checkExpectedResults(IList<String> list, String[] expectedResults) {
		Set<String> set = new HashSet<String>();
		for(String s : expectedResults) {
			set.add(s);
		}
		for(String s : list) {
			if(!set.remove(s)) {
				println("extra filename found in listing: "+s);
				fail();
			}
		}
		if(!set.isEmpty()) {
			println("expected elements not found in listing:");
			printIterator(set.iterator());
			fail();
		}
	}

	@Test
	public void testFileSearch() throws FileSystemException, ReadWriteException {
		// 1) Go to folder 'files-in-folders':
		FileSystem fs = new FileSystem(_testLocation);
		Directory dir = fs.getDirectory("files-in-folders");
		// 2) Search for files with pattern '*_*':
		SearchOptions options = new SearchOptions(new WildcardFilenameFilter("*_*"), true, SearchOptions.ORDER.NAME_ASCENDING);
		Iterator<FileObject> it = dir.findAll(options).iterator();
		assertEquals(it.next().getRelativePath(), "/files-in-folders/log_20060313.csv");
		assertEquals(it.next().getRelativePath(), "/files-in-folders/servlet-2_4-fr-spec-doc.zip");
		assertEquals(it.next().getRelativePath(), "/files-in-folders/DOC/DM.03.API_v0.1.doc");
		assertEquals(it.next().getRelativePath(), "/files-in-folders/DOC/Dev_Notes.doc");
		assertEquals(it.next().getRelativePath(), "/files-in-folders/DOC/Dev_Notes2.docx");
		assertEquals(it.next().getRelativePath(), "/files-in-folders/DOC/EBS.01.Spec_v1.0.doc");
		assertEquals(it.next().getRelativePath(), "/files-in-folders/DOC/EBS.02.Userdoc_Java_v0.2_02.doc");
		assertEquals(it.next().getRelativePath(), "/files-in-folders/DOC/EBS.03.API_Java_v0.1.doc");
		assertFalse(it.hasNext());
	}
	
	// TODO this test is not currently in use (need to make "network" folder shared)
	public void testNetwork() throws FileSystemException {
		FileSystem fs = null;
		// 1) Try connecting to the root of a server:
		try {
			fs = new FileSystem("\\\\10.150.0.10");
			fail();
		}
		catch(IllegalArgumentException iae) {
			println(iae);
		}
		// 2) Try connecting to a non-existing server using an IP address:
		try {
			fs = new FileSystem("\\\\10.150.0.10\\data");
			fail();
		}
		catch(UnknownHostException uhe) {
			println(uhe);
		}
		// 3) Try connecting to a non-existing server using a host name:
		try {
			fs = new FileSystem("\\\\some-host-name\\data");
			fail();
		}
		catch(UnknownHostException uhe) {
			println(uhe);
		}
		// 4) Connect to this server via hostname:
		String localAddress = null;
		try {
			InetAddress localhost = InetAddress.getLocalHost();
			localAddress = localhost.getHostAddress();
		}
		catch(IOException io) {
			fail("unexpected I/O exception", io);
		}
		// 5) Connect to this server via IP address:
		fs = new FileSystem("\\\\"+localAddress+"\\network");
		// 6) Retrieve some local resources:
		assertEquals(fs.getDirectory("servidordados/Ficheiros/2003/08/03-08-9762").getRelativePath(), "/servidordados/Ficheiros/2003/08/03-08-9762");
		assertEquals(fs.getFile("servidordados/Ficheiros/2003/08/03-08-9762/API_v1.2.doc").getRelativePath(), "/servidordados/Ficheiros/2003/08/03-08-9762/API_v1.2.doc");
	}

	@Test
	public void testFileAttributes() throws BasicIOException {
		FileSystem fs = new FileSystem(_testLocation);
		String filename = "aFile.tmp";
		File f = fs.createFile(filename);
		// Readable:
		assertTrue(f.getReadable());
		f.setReadable(false);
		assertTrue(f.getReadable());
		// Writable:
		assertTrue(f.getWritable());
		f.setWritable(false);
		assertFalse(f.getWritable());
		f.setWritable(true);
		assertTrue(f.getWritable());
		// Hidden:
		assertFalse(f.getHidden());
		f.setHidden(true);
		assertTrue(f.getHidden());
		// Last modified:
		Timestamp now = new Timestamp();
		f.setLastModified(now);
		assertEquals(f.getLastModified(), now);
		// Delete the file:
		f.delete();
	}
	
	// TODO zip a folder
}
