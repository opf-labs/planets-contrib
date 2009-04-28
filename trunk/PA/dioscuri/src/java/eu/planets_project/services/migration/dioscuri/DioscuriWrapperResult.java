package eu.planets_project.services.migration.dioscuri;

import java.io.File;

public class DioscuriWrapperResult {
	
	private int status = -1;
	private String message = null;
	private File resultFile = null;
	
	static final int SUCCESS = 0;
	static final int ERROR = -1;
	
	public int getState() {
		return status;
	}
	public void setState(int status) {
		this.status = status;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public File getResultFile() {
		return resultFile;
	}
	public void setResultFile(File resultFile) {
		this.resultFile = resultFile;
	}
	
}
