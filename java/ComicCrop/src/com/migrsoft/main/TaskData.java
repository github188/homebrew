package com.migrsoft.main;

import java.io.Serializable;

public class TaskData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public int[] sides;
	
	public int x;
	public int y;
	public int w;
	public int h;
	
	public float angle;
	
	public boolean gray;
	
	public boolean cropWhite;
}
