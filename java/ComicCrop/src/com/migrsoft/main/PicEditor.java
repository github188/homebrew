package com.migrsoft.main;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import essex.ooechs.hough.HoughLine;
import essex.ooechs.hough.HoughTransform;

public class PicEditor extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5499877512153985223L;
	
	public interface ActListener {
		public void loadPrevItem();
		public void loadNextItem();
		public void cropChanged(boolean isWhite);
	}
	
	private ActListener mActListener;
	
	public void setActListener(ActListener listener) {
		mActListener = listener;
	}
	
	private static final int WORK_MODE_SPLIT = 1;
	private static final int WORK_MODE_CROP = 2;
	
	private static final int MAX_EDGE_NUM = 8;
	private static final int CROP_EDGE_USED = 4;
	
	private class Rect {
		public int l;
		public int t;
		public int r;
		public int b;
		
		public void setRect(int x, int y, int w, int h) {
			l = x;
			t = y;
			r = x + w;
			b = y + h;
		}
	}
	
	/** 原始图片 */
	private BufferedImage mImage;
	/** 显示图片 */
	private BufferedImage mSnapshot;
	/** 是否灰度图 */
	private boolean mIsGray;
	
	private String mImagePath;
	private boolean mModified = false;
	private boolean mAdjust = false;
	
	/** 显示图片的左上角X坐标 */
	private int mX;
	/** 显示图片的左上角Y坐标 */
	private int mY;
	/** 图片缩放率。显示大小与实际大小 */
	private double mScale;
	/** 显示图片的显示坐标及大小 */
	private Rect mViewRect;
	/** 图片旋转角度 */
	private float mAngle;
	
	private int mWorkMode;

	/** 分割块数 */
	private int mMaxSplit = 2;
	private boolean mHorizonal = true; // 水平分割
	private int[] mSplit; // 分割界线
	private String mSplitSaveDir = "temp" + File.separator;
	private boolean mLeftToRight = false; // 从左到右计算页码
	private boolean mCropWhite; // 切白边
	
	/** 剪裁区，矩形区域 */
	private Rectangle2D.Float mCrop;
	
	private ArrayList<Rectangle2D.Float> mEdge; // 边界感应区
	private int mSelectedEdge; // 选择的边
	
	private Font mFont;
	
	public void useSplitMode() {
//		promptSave();
		mWorkMode = WORK_MODE_SPLIT;
		if (mImage != null) {
			calcPosition(true);
			repaint();
		}
	}
	
	public void useCropMode() {
		mWorkMode = WORK_MODE_CROP;
		if (mImage != null) {
			calcPosition(true);
			repaint();
		}
	}
	
	public void setSplitInfo(boolean horizonal, int splitNum, boolean ltor) {
		boolean update = false;
		if (mWorkMode == WORK_MODE_SPLIT) {
			if (mHorizonal != horizonal || mMaxSplit != splitNum || mLeftToRight != ltor)
				update = true;
		}
		
		mHorizonal = horizonal;
		mMaxSplit = (splitNum > MAX_EDGE_NUM) ? MAX_EDGE_NUM : splitNum;
		mLeftToRight = ltor;
		
		if (update && mImage != null) {
			calcPosition(true);
			repaint();
		}
	}
	
	public boolean isCropMode() {
		return mWorkMode == WORK_MODE_CROP;
	}
	
	public boolean isHorizonalSplit() {
		return mHorizonal;
	}
	
	public boolean isLeftToRight() {
		return mLeftToRight;
	}
	
	public int getSplitNum() {
		return mMaxSplit;
	}
	
	public String getSaveDir() {
		return mSplitSaveDir;
	}
	
	public PicEditor() {
		
		mWorkMode = WORK_MODE_SPLIT;
		
		mViewRect = new Rect();
		mCrop = new Rectangle2D.Float();
		
		mEdge = new ArrayList<Rectangle2D.Float>(MAX_EDGE_NUM);
		for (int i=0; i < MAX_EDGE_NUM; i++) {
			mEdge.add(new Rectangle2D.Float());
		}
		
		addMouseListener(new MouseHandler());
		addMouseMotionListener(new MouseMotionHandler());
		addKeyListener(new KeyHandler());
		
		setFocusable(true);
		
		mFont = new Font("SansSerif", Font.BOLD, 20);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D g2 = (Graphics2D)g;
		
//		g2.setPaint(Color.GRAY);
//		g2.drawLine(0, 22, getWidth() - 2, 22);

		if (mSnapshot != null) {
			g2.drawImage(mSnapshot, mX, mY, null);
		}
		else if (mImage != null) {
			g2.drawImage(mImage, mX, mY, null);
		}
			
		if (mSnapshot != null || mImage != null) {
			
			String txt = mImage.getWidth() + " x " + mImage.getHeight();
			if (mIsGray)
				txt += " GRAY";
			FontRenderContext frc = g2.getFontRenderContext();
			Rectangle2D bounds = mFont.getStringBounds(txt, frc);
			Rectangle2D area = new Rectangle2D.Double(0, 0, bounds.getWidth() + 4, bounds.getHeight() + 4);
			
			g2.setPaint(Color.LIGHT_GRAY);
			g2.fill(area);
			
			g2.setFont(mFont);
			g2.setPaint(Color.BLUE);
			g2.drawString(txt, 2, -(int)bounds.getY() + 2);
			
			g2.setPaint(Color.RED);
			if (mWorkMode == WORK_MODE_CROP) {
				g2.draw(mCrop);
			}
			else if (mWorkMode == WORK_MODE_SPLIT  && mSplit != null) {
				int w = getWidth();
				int h = getHeight();
				int id = (mLeftToRight || !mHorizonal) ? 1 : mMaxSplit;
				int x = mX, y = mY;
				
				for (int i=0; i < mSplit.length; i++) {
					txt = String.valueOf(id);
					bounds = mFont.getStringBounds(txt, frc);
					
					if (mHorizonal) {
						g2.drawString(txt,
								(int)(x + ((mSplit[i] - x) - bounds.getWidth()) / 2),
								(int)((h - bounds.getHeight()) / 2 - bounds.getY()));
						g2.drawLine(mSplit[i], 0, mSplit[i], h);
						x = mSplit[i];
					}
					else {
						g2.drawString(txt,
								(int)((w - bounds.getWidth()) / 2),
								(int)(y + ((mSplit[i] - y) - bounds.getHeight()) / 2 - bounds.getY()));
						g2.drawLine(0, mSplit[i], w, mSplit[i]);
						y = mSplit[i];
					}
					id += (mLeftToRight || !mHorizonal) ? 1 : -1;
				}
				
				txt = String.valueOf(id);
				bounds = mFont.getStringBounds(txt, frc);
				if (mHorizonal) {
					g2.drawString(txt,
							(int)(x + ((mViewRect.r - x) - bounds.getWidth()) / 2),
							(int)((h - bounds.getHeight()) / 2 - bounds.getY()));
				}
				else {
					g2.drawString(txt,
							(int)((w - bounds.getWidth()) / 2),
							(int)(y + ((mViewRect.b - y) - bounds.getHeight()) / 2 - bounds.getY()));
				}
			}
		}

//		g2.setPaint(Color.BLUE);
//		for (int i=0; i < 4; i++) {
//			g2.draw(mEdge.get(i));
//		}
	}

	private int mLastX;
	private int mLastY;
	
	private class MouseHandler extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent event) {
			if (event.getButton() == MouseEvent.BUTTON1) {
				requestFocus();
				mLastX = event.getX();
				mLastY = event.getY();
				checkEdge(event.getPoint());
			}
			else if (event.getButton() == MouseEvent.BUTTON2) {
			}
			else if (event.getButton() == MouseEvent.BUTTON3) {
				crop();
			}
		}

		@Override
		public void mouseReleased(MouseEvent event) {
			if (event.getButton() == MouseEvent.BUTTON1) {
				setCursor(Cursor.getDefaultCursor());
				if (mWorkMode == WORK_MODE_CROP)
					createCropEdge();
				else if (mWorkMode == WORK_MODE_SPLIT)
					createSplitSides();
			}
		}
	}
	
	private class MouseMotionHandler implements MouseMotionListener {

		@Override
		public void mouseDragged(MouseEvent event) {
			int dx = event.getX() - mLastX;
			int dy = event.getY() - mLastY;
			
			if (mWorkMode == WORK_MODE_SPLIT)
				changeSplitSide(dx, dy);
			else if (mWorkMode == WORK_MODE_CROP)
				changeCropEdge(dx, dy);
			
			mLastX = event.getX();
			mLastY = event.getY();
		}

		@Override
		public void mouseMoved(MouseEvent event) {
			checkEdge(event.getPoint());
		}
	}
	
	private class KeyHandler implements KeyListener {

		@Override
		public void keyPressed(KeyEvent event) {
		}

		@Override
		public void keyReleased(KeyEvent event) {
		}

		@Override
		public void keyTyped(KeyEvent event) {
			
			switch (event.getKeyChar()) {
			case 'w':
				if (mActListener != null)
					mActListener.loadPrevItem();
				break;
			case 's':
				if (mActListener != null)
					mActListener.loadNextItem();
				break;
				
			case 'a':
				rotate(false, false);
				break;
			case 'd':
				rotate(true, false);
				break;
			case 'q':
				rotate(false, true);
				break;
			case 'e':
				rotate(true, true);
				break;
				
			case 'c':
				calcCrop();
				break;
			}
		}
	}
	
	final static int SENSE_AREA = 10;
	
	final static int SENSE_LEFT_SIDE = 0;
	final static int SENSE_TOP_SIDE = 1;
	final static int SENSE_RIGHT_SIDE = 2;
	final static int SENSE_BOTTOM_SIDE = 3;
	final static int SENSE_TOPLEFT_CORNER = 4;
	final static int SENSE_TOPRIGHT_CORNER = 5;
	final static int SENSE_BOTTOMLEFT_CORNER = 6;
	final static int SENSE_BOTTOMRIGHT_CORNER = 7;
	
	// 创建剪裁区域感应区
	private void createCropEdge() {
		if (mImage == null)
			return;
		
		float w = mCrop.width - SENSE_AREA;
		float h = mCrop.height - SENSE_AREA;
		float half = SENSE_AREA / 2;
		
		mEdge.get(SENSE_LEFT_SIDE).setRect(mCrop.x - half, mCrop.y + half, SENSE_AREA, h);
		mEdge.get(SENSE_TOP_SIDE).setRect(mCrop.x + half, mCrop.y - half, w, SENSE_AREA);
		mEdge.get(SENSE_RIGHT_SIDE).setRect(mCrop.x + mCrop.width - half, mCrop.y + half, SENSE_AREA, h);
		mEdge.get(SENSE_BOTTOM_SIDE).setRect(mCrop.x + half, mCrop.y + h + half, w, SENSE_AREA);
		
		mSelectedEdge = -1;
	}

	// 创建分割边感应区
	private void createSplitSides() {
		if (mImage == null || mSplit == null)
			return;
		
		float w = getWidth();
		float h = getHeight();
		float half = SENSE_AREA / 2;
		
		for (int i=0; i < mSplit.length; i++) {
			if (mHorizonal) {
				mEdge.get(i).setRect(mSplit[i] - half, 0, SENSE_AREA, h);
			}
			else {
				mEdge.get(i).setRect(0, mSplit[i] - half, w, SENSE_AREA);
			}
		}
		
		mSelectedEdge = -1;
	}
	
	private final int[] CURSOR_SET = {
			Cursor.W_RESIZE_CURSOR,
			Cursor.N_RESIZE_CURSOR,
			Cursor.E_RESIZE_CURSOR,
			Cursor.S_RESIZE_CURSOR
	};
	
	private void checkEdge(Point2D point) {
		
		if (mImage == null)
			return;
		
		int cursor = Cursor.DEFAULT_CURSOR;
		
		int limit = 0;
		if (mWorkMode == WORK_MODE_CROP)
			limit = CROP_EDGE_USED;
		else if (mSplit != null)
			limit = mSplit.length;
		
		for (int i=0; i < limit; i++) {
			if (mEdge.get(i).contains(point)) {
				if (mWorkMode == WORK_MODE_CROP)
					cursor = CURSOR_SET[i];
				else if (mWorkMode == WORK_MODE_SPLIT)
					cursor = (mHorizonal) ? Cursor.E_RESIZE_CURSOR : Cursor.S_RESIZE_CURSOR;
				mSelectedEdge = i;
				break;
			}
		}
		setCursor(Cursor.getPredefinedCursor(cursor));
	}
	
	final static int AREA_MIN_SIZE = 100;
	
	// 改变剪裁区域
	private void changeCropEdge(int dx, int dy) {
		
		if (mImage == null)
			return;
		
		switch (mSelectedEdge) {
		case SENSE_LEFT_SIDE:
			double l = mCrop.x + dx;
			if (l >= mViewRect.l && mCrop.width - dx > AREA_MIN_SIZE) {
				mCrop.x += dx;
				mCrop.width -= dx;
				mAdjust = true;
			}
			break;
			
		case SENSE_RIGHT_SIDE:
			double r = mCrop.x + mCrop.width + dx;
			if (r - mCrop.x > AREA_MIN_SIZE && r < mViewRect.r) {
				mCrop.width += dx;
				mAdjust = true;
			}
			break;
			
		case SENSE_TOP_SIDE:
			double t = mCrop.y + dy;
			if (t >= mViewRect.t && mCrop.height - dy > AREA_MIN_SIZE) {
				mCrop.y += dy;
				mCrop.height -= dy;
				mAdjust = true;
			}
			break;
			
		case SENSE_BOTTOM_SIDE:
			double b = mCrop.y + mCrop.height + dy;
			if (b - mCrop.y > AREA_MIN_SIZE && b < mViewRect.b) {
				mCrop.height += dy;
				mAdjust = true;
			}
			break;
		}
		
		if (mAdjust)
			repaint();
	}
	
	// 改变分割界线
	private void changeSplitSide(int dx, int dy) {
		if (mImage == null || mSelectedEdge == -1 || mSplit == null)
			return;
		
		int coord, min, max;
		
		if (mHorizonal) {
			coord = mSplit[mSelectedEdge] + dx;
			if (mSelectedEdge == 0) {
				min = mViewRect.l;
				max = (mSelectedEdge < mSplit.length - 1) ? mSplit[mSelectedEdge + 1] : mViewRect.r;
			}
			else if (mSelectedEdge == mSplit.length - 1) {
				min = mSplit[mSelectedEdge - 1];
				max = mViewRect.r;
			}
			else {
				min = mSplit[mSelectedEdge - 1];
				max = mSplit[mSelectedEdge + 1];
			}
		}
		else {
			coord = mSplit[mSelectedEdge] + dy;
			if (mSelectedEdge == 0) {
				min = mViewRect.t;
				max = (mSelectedEdge < mSplit.length - 1) ? mSplit[mSelectedEdge + 1] : mViewRect.b;
			}
			else if (mSelectedEdge == mSplit.length - 1) {
				min = mSplit[mSelectedEdge - 1];
				max = mViewRect.b;
			}
			else {
				min = mSplit[mSelectedEdge - 1];
				max = mSplit[mSelectedEdge + 1];
			}
		}
		
		if (coord > min + AREA_MIN_SIZE && coord < max - AREA_MIN_SIZE) {
			mSplit[mSelectedEdge] = coord;
			mAdjust = true;
			repaint();
		}
	}
	
	/**
	 * 按照剪裁区域剪裁原图片
	 */
	public void crop() {
		if (mImage == null || mWorkMode != WORK_MODE_CROP)
			return;
		
		int x = (int) ((mCrop.x - mX) / mScale);
		int y = (int) ((mCrop.y - mY) / mScale);
		int w = (int) (mCrop.width / mScale);
		int h = (int) (mCrop.height / mScale);
		
		BufferedImage tmp = PicWorker.crop(mImage, x, y, w, h);
		if (tmp != null) {
			mImage = tmp;
			calcPosition(false);
			mModified = true;
			repaint();
		}
	}
	
	/**
	 * 旋转图片
	 * @param clockwise 方向。true:顺时针; false:逆时针
	 * @param micro 旋转角度。true:0.25度; false:0.5度
	 */
	public void rotate(boolean clockwise, boolean micro) {
		if (mImage == null)
			return;
		
		float da = (micro) ? 0.25f : 0.5f;
		float angle = (clockwise) ? da : -da;
		mAngle += angle;
		mAdjust = true;
		
		BufferedImage tmp = PicWorker.rotate(mImage, angle);
		if (tmp != null) {
			mImage = tmp;
			calcPosition(false);
			mModified = true;
			repaint();
		}
	}
	
	/**
	 * 按预定区域范围进行图片的放大或缩小
	 * @param widthOnly 仅做宽度的缩放。
	 */
	public void scale(boolean widthOnly) {
		if (mImage == null)
			return;
		
		BufferedImage tmp;
		double maxw = MainParam.getInstance().getMaxWidth();
		double maxh = MainParam.getInstance().getMaxHeight();
		
		if (widthOnly)
			tmp = PicWorker.scaleWidth(mImage, maxw, maxh);
		else
			tmp = PicWorker.scale(mImage, maxw, maxh);
		if (tmp != null) {
			mImage = tmp;
			calcPosition(false);
			mModified = true;
			repaint();
		}
	}

	private final int SPACING = 10;
	
	private void calcPosition(boolean first) {

		TaskData td = null;
		if (first) {
			td = MainView.getInstance().loadTaskData(mImagePath);
			
			// 计算灰阶
			if (MainParam.getInstance().isAutoGrayLevel()) {
				if ((td != null && td.gray) || PicWorker.isGrayMode(mImage) || MainParam.getInstance().isForceGray()) {
					mIsGray = true;
					mModified = PicWorker.autoGrayLevel(mImage);
				}
			}
			
			if (td != null) {
				// 旋转
				if (td.angle != 0f) {
					mImage = PicWorker.rotate(mImage, td.angle);
					mAngle = td.angle;
				}
				
				// 切边类型
				mCropWhite = td.cropWhite;
			}
			
			mActListener.cropChanged(mCropWhite);
		}

		// 计算显示大小与实际大小的缩放率
		double vw = getWidth() - SPACING;
		double vh = getHeight() - SPACING;
		double ws = vw / mImage.getWidth();
		double hs = vh / mImage.getHeight();
		
		if (ws < 1f || hs < 1f) {
			mScale = Math.min(ws, hs);
		}
		else {
			mScale = 1f;
		}
		
		// 计算显示图片的左上角显示坐标
		double iw, ih;
		if (mScale != 1f) {
			mSnapshot = PicWorker.scale(mImage, vw, vh);
			iw = mSnapshot.getWidth();
			ih = mSnapshot.getHeight();
		}
		else {
			mSnapshot = null;
			iw = mImage.getWidth();
			ih = mImage.getHeight();
		}
		
		mX = (int) ((getWidth() - iw) / 2);
		mY = (int) ((getHeight() - ih) / 2);
		
		// 适合窗口的原始图片大小
		mViewRect.setRect((int)mX, (int)mY, (int)iw, (int)ih);
		
		if (mWorkMode == WORK_MODE_SPLIT) {
			if (td != null && td.sides != null) { // 恢复
				mSplit = td.sides.clone();
				for (int i = 0; i < mSplit.length; i++)
					mSplit[i] *= mScale;
			}
			else
				mSplit = PicWorker.calcSplitSides((int)iw, (int)ih, mHorizonal, mMaxSplit);
			if (mSplit != null) {
				for (int i = 0; i < mSplit.length; i++) {
					mSplit[i] += (mHorizonal) ? mX : mY;
				}
				createSplitSides();
			}
		}
		else if (mWorkMode == WORK_MODE_CROP) {
			mCrop.setRect(mX, mY, iw, ih);
			if (first) { // 自动计算白边
				if (td != null && td.w != 0) { // 恢复上次选择的区域
					mCrop.x += td.x * mScale;
					mCrop.width = (float) (td.w * mScale);
					mCrop.y += td.y * mScale;
					mCrop.height = (float) (td.h * mScale);
				}
				else {
					Rectangle2D.Float box = PicWorker.calcCropBox(mImage, mCropWhite);
					mCrop.x += box.x * mScale;
					mCrop.width = (float) (box.width * mScale);
					mCrop.y += box.y * mScale;
					mCrop.height = (float) (box.height * mScale);
				}
			}
			createCropEdge();
		}
	}
	
	/**
	 * 根据页面四周空白，计算剪裁区域。
	 */
	public void calcCrop() {
		if (mImage == null || mWorkMode != WORK_MODE_CROP)
			return;
		
		mCrop.setRect(mViewRect.l, mViewRect.t, mViewRect.r - mViewRect.l, mViewRect.b - mViewRect.t);
		Rectangle2D.Float box = PicWorker.calcCropBox(mImage, mCropWhite);
		mCrop.x += box.x * mScale;
		mCrop.width = (float) (box.width * mScale);
		mCrop.y += box.y * mScale;
		mCrop.height = (float) (box.height * mScale);
		
		createCropEdge();
		repaint();
	}
	
	private void promptSave() {
		if (mModified && mWorkMode == WORK_MODE_CROP) {
			int r = JOptionPane.showConfirmDialog(this, "图片已修改，是否保存？", "保存", JOptionPane.YES_NO_OPTION);
			if (r == JOptionPane.YES_OPTION) {
				save(null);
			}
		}
	}
	
	public void reset() {
		mModified = false;
		mAdjust = false;
		
		mImagePath = null;
		mSnapshot = null;
		mImage = null;
		mIsGray = false;
		mCropWhite = MainParam.getInstance().isCropWhite();
		
		mAngle = 0f;
		mScale = 1f;
		mSplit = null;
		mCrop.setRect(0, 0, 0, 0);
	}
	
	/**
	 * 保存当前图片的编辑相关信息。
	 */
	public void saveTaskData() {
		
		if (!mAdjust)
			return;
		mAdjust = false;
	
		TaskData td = new TaskData();
		
		// 旋转角度
		td.angle = mAngle;
		
		// 分割边
		if (mSplit != null) {
			td.sides = mSplit;
			for (int i = 0; i < td.sides.length; i++) {
				td.sides[i] -= (mHorizonal) ? mX : mY;
				td.sides[i] = (int) ((float)td.sides[i] / mScale);
			}
		}
		
		// 剪裁区域
		if (!mCrop.isEmpty()) {
			td.x = (int) ((mCrop.x - mX) / mScale);
			td.y = (int) ((mCrop.y - mY) / mScale);
			td.w = (int) (mCrop.width / mScale);
			td.h = (int) (mCrop.height / mScale);
		}
		
		// 灰度
		td.gray = mIsGray;
		
		// 切边类型
		td.cropWhite = mCropWhite;
		
		MainView.getInstance().saveTaskData(mImagePath, td);
	}
	
	/**
	 * 加载图片文件
	 * @param path 文件路径。
	 */
	public void load(String path) {
		
		saveTaskData();

		reset();
		
//		promptSave();
		
		mImage = PicWorker.load(path);
		if (mImage != null) {
			mImagePath = path;
			calcPosition(true);
			repaint();
		}
	}
	
	/**
	 * 重新加载当前图片。
	 */
	public void reload() {
		if (mImagePath == null)
			return;
		
		saveTaskData();
		mImage = PicWorker.load(mImagePath);
		calcPosition(true);
		repaint();
	}
	
	/**
	 * 保存当前图片到指定路径。
	 * @param filename 保存路径
	 */
	public void save(String filename) {
		if (mImage == null || !mModified)
			return;
		
		String name = (filename != null) ? filename : mImagePath;
		if (name == null)
			return;
		
		if (PicWorker.save(mImage, name))
			mModified = false;
	}
	
	/**
	 * 切分图片。
	 */
	public void split() {
		if (mImage == null || mWorkMode != WORK_MODE_SPLIT || mMaxSplit < 2 || mSplit == null)
			return;
		
		File f = new File(mImagePath);
		String name = f.getName();
		String path = mImagePath.substring(0, mImagePath.length() - name.length()) + mSplitSaveDir;
		f = new File(path);
		if (!f.exists())
			f.mkdirs();
		f = null;
		
		int[] lines = mSplit.clone();
		for (int i=0; i < lines.length; i++) {
			lines[i] -= (mHorizonal) ? mX : mY;
			lines[i] = (int) ((float)lines[i] / mScale);
		}
		
		PicWorker.split(mImage, mHorizonal, lines, path, name, mLeftToRight);
	}
	
	/**
	 * 转为灰度模式。
	 */
	public void toGrayMode() {
		if (mImage == null || mIsGray)
			return;
		
		mIsGray = true;
		mModified = PicWorker.autoGrayLevel(mImage);
		mAdjust = true;
		calcPosition(false);
		repaint();
	}
	
	public void cropChanged(boolean isWhite) {
		if (mCropWhite != isWhite) {
			mCropWhite = isWhite;
			mAdjust = true;
			calcCrop();
		}
	}
	
	/**
	 * 放大剪裁区域。四边同时放大。
	 */
	public void growArea() {
		if (mImage == null || mWorkMode != WORK_MODE_CROP)
			return;
		
		int grow = 10;
		int l, t, r, b;
		l = (int) (mCrop.x - grow);
		t = (int) (mCrop.y - grow);
		r = (int) (l + mCrop.width + grow * 2);
		b = (int) (t + mCrop.height + grow * 2);
		if (l < mViewRect.l) l = mViewRect.l;
		if (t < mViewRect.t) t = mViewRect.t;
		if (r > mViewRect.r) r = mViewRect.r;
		if (b > mViewRect.b) b = mViewRect.b;
		
		mCrop.x = l;
		mCrop.y = t;
		mCrop.width = r - l;
		mCrop.height = b - t;
		
		mModified = true;
		mAdjust = true;
		repaint();
	}
	
	public void test() {
	}
}
