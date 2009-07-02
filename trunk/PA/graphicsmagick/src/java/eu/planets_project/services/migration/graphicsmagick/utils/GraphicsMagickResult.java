/**
 * 
 */
package eu.planets_project.services.migration.graphicsmagick.utils;

import java.io.File;

/**
 * @author melmsp
 *
 */
public class GraphicsMagickResult {
	
	private File resultFile = null;
	private String formatExtension = null;
	
	private String errorMsg = null;
	private String outputMsg = null;
	
	public boolean SUCCESS = false; 
	
	public GraphicsMagickResult() {
		this.resultFile = null;
		this.formatExtension = null;
		this.errorMsg = "";
		this.outputMsg = "";
		SUCCESS = false;
	}
	
	/**
	 * @return the resultFile
	 */
	public File getResultFile() {
		return resultFile;
	}

	/**
	 * @param resultFile the resultFile to set
	 */
	public void setResultFile(File resultFile) {
		this.resultFile = resultFile;
		SUCCESS = true;
	}

	/**
	 * @return the formatExtension
	 */
	public String getFormatExtension() {
		return formatExtension;
	}

	/**
	 * @param formatExtension the formatExtension to set
	 */
	public void setFormatExtension(String formatExtension) {
		this.formatExtension = formatExtension;
		SUCCESS = true;
	}

	/**
	 * @return the errorMsg
	 */
	public String getErrorMsg() {
		return errorMsg;
	}

	/**
	 * @param errorMsg the errorMsg to set
	 */
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	/**
	 * @return the outputMsg
	 */
	public String getOutputMsg() {
		return outputMsg;
	}

	/**
	 * @param outputMsg the outputMsg to set
	 */
	public void setOutputMsg(String outputMsg) {
		this.outputMsg = outputMsg;
	}

}
