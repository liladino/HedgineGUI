package chess.IO;

import java.io.IOException;

public class FENException extends IOException{
	private static final long serialVersionUID = 6430546L;
	private int succesfulFields;
		
	public FENException(String message, int succesfulFields) {
		super(message); 
		this.succesfulFields = succesfulFields; 
	}
	public FENException(String message, boolean[] readFields) {
		super(message); 
		int temp = 0;
		for(int i = 0; i < readFields.length; i++) if (readFields[i]) temp++; else break;
		
		succesfulFields = temp;
	}
	
	public int getSuccesfulFields() {
		return succesfulFields;
	}
}
