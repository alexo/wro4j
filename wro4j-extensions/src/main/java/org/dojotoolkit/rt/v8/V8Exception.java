/*
    Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.rt.v8;

public class V8Exception extends Exception {
	private static final long serialVersionUID = 1L;
	
	public V8Exception(String message) {
		super(message);
	}
	
	public V8Exception(String exceptionString, String fileName, String sourceLine, int lineNum, int start, int end) {
		super(exceptionString + " : [file] "+fileName + " [line] " + lineNum + " [start] " + start + " [end] " + end + " [sourceline] "+sourceLine);
	}
}
