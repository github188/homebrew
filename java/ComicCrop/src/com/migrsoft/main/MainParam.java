package com.migrsoft.main;

public class MainParam {
	
	static private MainParam sMainParam = null;
	
	static public MainParam getInstance() {
		if (sMainParam == null)
			sMainParam = new MainParam();
		return sMainParam;
	}

	static public final int OUTPUT_FORMAT_PNG = 1;
	static public final int OUTPUT_FORMAT_JPEG = 2;
	
	private int mOutputType;
	private float mJpegQuality;
	private boolean mAutoGrayLevel;
	
	private int mMaxThreads;
	
	private boolean mCropWhite;
	
	private int mMaxWidth;
	private int mMaxHeight;
	
	public MainParam() {
		mOutputType = OUTPUT_FORMAT_PNG;
		mJpegQuality = 0.5f;
		mAutoGrayLevel = true;
		mMaxThreads = 2;
		mCropWhite = true;
		
		mMaxWidth = 600;
		mMaxHeight = 1000;
	}

	public int getOutputFormat() {
		return mOutputType;
	}

	public void setOutputFormat(int format) {
		this.mOutputType = format;
	}
	
	public String getOutputExtName() {
		if (mOutputType == OUTPUT_FORMAT_PNG)
			return ".png";
		else if (mOutputType == OUTPUT_FORMAT_JPEG)
			return ".jpg";
		else
			return null;
	}

	public float getJpegQuality() {
		return mJpegQuality;
	}

	public void setJpegQuality(float quality) {
		this.mJpegQuality = quality;
	}

	public boolean isAutoGrayLevel() {
		return mAutoGrayLevel;
	}

	public void setAutoGrayLevel(boolean auto) {
		this.mAutoGrayLevel = auto;
	}

	public int getMaxThreads() {
		return mMaxThreads;
	}

	public void setMaxThreads(int maxThreads) {
		this.mMaxThreads = maxThreads;
	}
	
	public boolean isCropWhite() {
		return mCropWhite;
	}
	
	public void setCropWhite(boolean b) {
		mCropWhite = b;
	}
	
	public int getMaxWidth() {
		return mMaxWidth;
	}
	
	public void setMaxWidth(int maxw) {
		mMaxWidth = maxw;
	}
	
	public int getMaxHeight() {
		return mMaxHeight;
	}
	
	public void setMaxHeight(int maxh) {
		mMaxHeight = maxh;
	}
}
