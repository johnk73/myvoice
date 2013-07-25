/**
 * 
 */
package edu.sbcc.jkalstrommyvoice;

import java.io.*;

/**
 * Metadata of a voice message
 */
public class Message implements Serializable {
	private static final long serialVersionUID = 0L;

	int id;
	String date;
	String duration;
	String callerID;
	String speechToText;
}
