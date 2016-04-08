package org.xmlcml.cmine.files;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import nu.xom.Element;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.xml.XMLUtil;

/** for CProject and CTree 
 * 
 * @author pm286
 *
 */
public abstract class CContainer {

	private static final Logger LOG = Logger.getLogger(CContainer.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	public static final String FILES_RESOURCE = "/org/xmlcml/cmine/files";

	protected static final String MANIFEST_XML = "manifest.xml";
	protected static final String LOG_XML = "log.xml";
	public static final String RESULTS = "results";
	public static final String SUMMARY = "summary";

	protected CManifest manifest;
	protected File directory;
	protected List<File> allChildDirectoryList;
	protected List<File> allChildFileList;
	protected List<File> allowedChildDirectoryList;
	protected List<File> allowedChildFileList;
	protected List<File> unknownChildDirectoryList;
	protected List<File> unknownChildFileList;

	public CContainer() {
	}
	
	private static boolean isAllowedPattern(String name, Pattern[] allowedPatterns) {
		for (Pattern allowedPattern : allowedPatterns) {
			if (allowedPattern.matcher(name).matches()) {
				return true;
			}
		}
		return false;
	}

	private static boolean isAllowedName(String name, String[] allowedNames) {
		for (String allowedName : allowedNames) {
			if (allowedName.equals(name)) {
				return true;
			}
		}
		return false;
	}

	protected static boolean isAnyAllowed(List<File> files, String[] names) {
		for (File file : files) {
			for (String name : names) {
				if (file.getName().equals(name)) {
					return true;
				}
			}
		}
		return false;
	}

	protected boolean isAnyAllowed(List<File> files, Pattern[] patterns) {
		for (Pattern pattern : patterns) {
			for (File file : files) {
				if (pattern.matcher(file.getName()).matches()) {
					return true;
				}
			}
		}
		return false;
	}

	/** gets manifest object or creates if null.
	 * 
	 * Does NOT create manifest file
	 * 
	 * @return
	 */
	public CManifest getOrCreateManifest() {
		if (manifest == null) {
			this.manifest = createManifest();
		}
		return manifest;
	}

	protected abstract CManifest createManifest();

	/** gets Directory file.
	 * may or may not be null, or existing
	 * 
	 * @return
	 */
	public File getDirectory() {
		return directory;
	}
	
	protected Element readTemplate(String projectTemplateXml) {
		InputStream is = this.getClass().getResourceAsStream(FILES_RESOURCE+"/"+projectTemplateXml);
		Element templateElement = XMLUtil.parseQuietlyToDocument(is).getRootElement();
		return templateElement;
	}

	protected void getDirectoryAndFileList() {
		allChildDirectoryList = new ArrayList<File>();
		allChildFileList = new ArrayList<File>();
		File[] ff = directory.listFiles();
		if (ff != null) {
			for (File f : ff) {
				if (f.isDirectory()) {
					if (!CProject.isReservedProjectChildDirectory(f)) {
						allChildDirectoryList.add(f);
					}
				} else {
					allChildFileList.add(f);
				}
			}
		}
	}
	
	/** not yet implemented
	 * 
	 */
	protected void updateManifest() {
		this.getOrCreateManifest();
//		this.getOrCreateCTreeList(); this should be in CProject
		this.checkAllowedCounts();
	}


	private void checkAllowedCounts() {
//		resetCounts();
	}

	public void getOrCreateFilesDirectoryCTreeLists() {
//		CProjectManifest cProjectManifest = (CProjectManifest) manifest;
		if (allChildDirectoryList == null) {
			getTreesAndDirectories();
			makeLists();
			getDirectoryAndFileList();
			getAllowedAndUnknownDirectories();
			getAllowedAndUnknownFiles();
		}
	}

	protected abstract void getAllowedAndUnknownDirectories();
	protected abstract void getAllowedAndUnknownFiles();

	void makeLists() {
		allowedChildDirectoryList = new ArrayList<File>();
		allowedChildFileList = new ArrayList<File>();
		unknownChildDirectoryList = new ArrayList<File>();
		unknownChildFileList = new ArrayList<File>();
	}

	protected boolean isAllowedFile(File file, Pattern[] allowedPatterns) {
		return isAllowedPattern(file.getName(), allowedPatterns);
	}

	protected boolean isAllowedFileName(File file, String[] allowedNames) {
		return isAllowedName(file.getName(), allowedNames);
	}

	public void getOrCreateDirectory() {
		if (directory != null) {
			if (!directory.exists()) {
				directory.mkdirs();
			} else if (!directory.isDirectory()) {
				throw new RuntimeException("Expected "+directory+" to be a directory in cProject/cTree");
			} else {
				// already exists
			}
		} else {
			throw new RuntimeException("directory is null");
		}
	}

	protected void getTreesAndDirectories() {
		getOrCreateDirectory();
		getDirectoryAndFileList();
		/** NYI
		manifest.resetCounts();
		manifest.checkFiles();
		manifest.checkDirectories();
		*/
	}

	public List<File> getAllChildDirectoryList() {
		this.getOrCreateFilesDirectoryCTreeLists();
		return allChildDirectoryList;
	}

	public List<File> getAllChildFileList() {
		this.getOrCreateFilesDirectoryCTreeLists();
		return allChildFileList;
	}

	public List<File> getAllowedChildDirectoryList() {
		this.getOrCreateFilesDirectoryCTreeLists();
		return allowedChildDirectoryList;
	}

	public List<File> getAllowedChildFileList() {
		this.getOrCreateFilesDirectoryCTreeLists();
		return allowedChildFileList;
	}

	public List<File> getUnknownChildDirectoryList() {
		this.getOrCreateFilesDirectoryCTreeLists();
		return unknownChildDirectoryList;
	}

	public List<File> getUnknownChildFileList() {
		this.getOrCreateFilesDirectoryCTreeLists();
		return unknownChildFileList;
	}

	public File getAllowedChildDirectory(String filename) {
		this.getOrCreateFilesDirectoryCTreeLists();
		for (File file : allowedChildDirectoryList) {
			if (file.getName().equals(filename)) {
				return file;
			}
		}
		return null;
	}

	public File getAllowedChildFile(String filename) {
		this.getOrCreateFilesDirectoryCTreeLists();
		for (File file : allowedChildFileList) {
			if (file.getName().equals(filename)) {
				return file;
			}
		}
		return null;
	}

	public File getUnknownChildDirectory(String filename) {
		this.getOrCreateFilesDirectoryCTreeLists();
		for (File file : unknownChildDirectoryList) {
			if (file.getName().equals(filename)) {
				return file;
			}
		}
		return null;
	}

	public File getUnknownChildFile(String filename) {
		this.getOrCreateFilesDirectoryCTreeLists();
		for (File file : unknownChildFileList) {
			if (file.getName().equals(filename)) {
				return file;
			}
		}
		return null;
	}



}
