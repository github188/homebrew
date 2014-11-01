package com.migrsoft.main;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Vector;

public class BatchTask {
	
	public interface ProgressListener {
		public void onProgress(int progress);
	}

	private ProgressListener mListener;
	
	void setProgressListener(ProgressListener listener) {
		mListener = listener;
	}
	
	private Vector<String> mTaskList;
	private HashMap<String, TaskData> mTaskInfo;
	
	private boolean mStop;
	
	public BatchTask() {
		mStop = false;
	}
	
	public void setTask(Vector<String> list, HashMap<String, TaskData> info) {
		mTaskList = list;
		mTaskInfo = info;
	}
	
	public void stop() {
		mStop = true;
	}
	
	public void doSplit(String path, String dir, boolean hori, boolean ltor, int num) {

		File f = new File(path + dir);
		if (!f.exists())
			f.mkdirs();
		f = null;
		
		TaskData td;
		int[] lines;
		int i = 0;
		for (String name : mTaskList) {
			
			if (mStop)
				return;
			
			BufferedImage image = PicWorker.load(path + name);
			if (image != null) {
				td = mTaskInfo.get(name);
				if (td == null || (td != null && td.sides == null)) {
					lines = PicWorker.calcSplitSides(image.getWidth(), image.getHeight(), hori, num);
				}
				else {
					lines = td.sides;
				}
				
				PicWorker.split(image, hori, lines, path + dir, name, ltor);
			}
			
			i++;
			if (mListener != null)
				mListener.onProgress(i);
		}
	}
	
	public void doCropAndScale(String path, int maxWidth, int maxHeight, boolean scale, boolean widthOnly) {
		
		TaskData td;
		int x, y, w, h;
		int i = 0;
		for (String name : mTaskList) {
			
			if (mStop)
				return;
			
			BufferedImage image = PicWorker.load(path + name);
			boolean save = false;
			boolean cropWhite = true;
			if (image != null) {
				
				td = mTaskInfo.get(name);
				
				if (MainParam.getInstance().isAutoGrayLevel()) {
					if ((td != null && td.gray) || PicWorker.isGrayMode(image) || MainParam.getInstance().isForceGray()) {
						PicWorker.autoGrayLevel(image);
					}
				}
				
				if (td != null) {
					if (td.angle != 0f) {
						image = PicWorker.rotate(image, td.angle);
						save = true;
					}
					cropWhite = td.cropWhite;
				}
				
				if (td == null || (td != null && td.w == 0)) {
					Rectangle2D.Float box = PicWorker.calcCropBox(image, cropWhite);
					x = (int) box.x;
					y = (int) box.y;
					w = (int) box.width;
					h = (int) box.height;
				}
				else {
					x = td.x;
					y = td.y;
					w = td.w;
					h = td.h;
				}
				
				BufferedImage tm = PicWorker.crop(image, x, y, w, h);
				if (tm != null) {
					image = tm;
					save = true;
				}
				
				if (scale) {
					if (widthOnly)
						tm = PicWorker.scaleWidth(image, maxWidth, maxHeight);
					else
						tm = PicWorker.scale(image, maxWidth, maxHeight);
					if (tm != null) {
						image = tm;
						save = true;
					}
				}
				
				if (save)
					PicWorker.save(image, path + name);
			}
			
			i++;
			if (mListener != null)
				mListener.onProgress(i);
		}
	}
}
