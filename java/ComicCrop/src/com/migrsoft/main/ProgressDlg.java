package com.migrsoft.main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

public class ProgressDlg extends JDialog implements BatchTask.ProgressListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1709185996564717692L;
	
	public enum TaskType {
		TASK_SPLIT,
		TASK_CROP,
		TASK_CROP_SCALE,
		TASK_CROP_SCALE_WIDTH
	};
	
	Point mLoc;
	Dimension mSize;

	private int mMax;
	private JProgressBar mProgressBar;
	private int mProgress;
	
	private BatchTask mTask;
	private TaskType mType;
	
	private String mPath;
	private String mDir;
	private boolean mHori;
	private boolean mLtor;
	private int mNum;
	private int mMaxWidth;
	private int mMaxHeight;
	
	private Thread mThread;
	
	private BatchTask[] mTasks;
	private Thread[] mThreads;
	
	private boolean mStop;
	
	private void init(TaskType type,
			String path, String dir,
			boolean hori, boolean ltor, int num,
			int maxWidth, int maxHeight) {
		
		mType = type;
		
		mPath = path;
		mDir = dir;
		
		mHori = hori;
		mLtor = ltor;
		mNum = num;
		
		mMaxWidth = maxWidth;
		mMaxHeight = maxHeight;
		
		mProgress = 0;
		mTime = 0;
		
		mStop = false;
	}
	
	public void setMax(int max) {
		mMax = max;
	}
	
	// 单线程模式
	public void setBatchTask(BatchTask task, TaskType type,
			String path, String dir,
			boolean hori, boolean ltor, int num,
			int maxWidth, int maxHeight) {
		
		init(type, path, dir, hori, ltor, num, maxWidth, maxHeight);
		
		mTask = task;
		mTask.setProgressListener(this);
	}
	
	// 多线程模式
	public void arrangeWork(Vector<String> list, HashMap<String, TaskData> info,
			TaskType type, String path, String dir,
			boolean hori, boolean ltor, int num,
			int maxWidth, int maxHeight) {
		
		init(type, path, dir, hori, ltor, num, maxWidth, maxHeight);
		
		setMax(list.size());
		
		int tasks = MainParam.getInstance().getMaxThreads();
		int n = list.size() / tasks;
		
		mTasks = new BatchTask[tasks];
		
		for (int i = 0; i < tasks; i++) {
			mTasks[i] = new BatchTask();
			Vector<String> ls = new Vector<String>();
			mTasks[i].setTask(ls, info);
			mTasks[i].setProgressListener(this);

			int b = i * n;
			for (int j = 0; j < n; j++)
				ls.add(list.get(b + j));
			
			// 增加剩余的工作到最后的任务
			if (i == tasks - 1) {
				for (int j = (i + 1) * n; j < list.size(); j++)
					ls.add(list.get(j));
			}
		}
	}

	public ProgressDlg(JFrame owner) {
		super(owner, "任务进度", true);
		
		mLoc = owner.getLocation();
		mSize = owner.getSize();

		addWindowListener(new WindowListener() {

			@Override
			public void windowActivated(WindowEvent event) {
				startTask();
			}

			@Override
			public void windowClosed(WindowEvent event) {
			}

			@Override
			public void windowClosing(WindowEvent event) {
				stopTask();
			}

			@Override
			public void windowDeactivated(WindowEvent event) {
			}

			@Override
			public void windowDeiconified(WindowEvent event) {
			}

			@Override
			public void windowIconified(WindowEvent event) {
			}

			@Override
			public void windowOpened(WindowEvent event) {
			}
		});
	}
	
	public void initGui() {
		mProgressBar = new JProgressBar(0, mMax);
		mProgressBar.setStringPainted(true);
		add(mProgressBar, BorderLayout.CENTER);
		
		setSize(500, 80);
		setLocation(mLoc.x + (mSize.width - getWidth()) / 2, mLoc.y + (mSize.height - getWidth()) / 2);
	}

	@Override
	public void onProgress(final int progress) {
		
		if (mStop)
			return;
		
		Runnable r = new Runnable() {

			@Override
			public void run() {
				mProgress++;
				if (mProgress >= mMax) {
					
					setVisible(false);
					
					if (showTaskBrief()) {
						MainView.getInstance().openPath(mPath + mDir);
					}
					else {
						if (mType != TaskType.TASK_SPLIT) {
							MainView.getInstance().clearTaskInfo();
							MainView.getInstance().resetEditor();
						}
					}
				}
				else {
					mProgressBar.setValue(mProgress);
				}
			}
		};
		
		EventQueue.invokeLater(r);
	}
	
	private boolean showTaskBrief() {
		
		String msg1 = "";
		if (mType == TaskType.TASK_SPLIT)
			msg1 = "任务类型: 批量分割\n结果保存: " + mPath + mDir + "\n";
		else if (mType == TaskType.TASK_CROP)
			msg1 = "任务类型: 批量剪裁\n结果保存: " + mPath + "\n";
		else if (mType == TaskType.TASK_CROP_SCALE)
			msg1 = "任务类型: 批量剪裁、缩放\n结果保存: " + mPath + "\n";
		else if (mType == TaskType.TASK_CROP_SCALE_WIDTH)
			msg1 = "任务类型: 批量剪裁、缩放宽度\n结果保存: " + mPath + "\n";
		
		String msg2 = "";
		float consume = (float)(System.currentTimeMillis() - mTime) / 100;
		consume = Math.round(consume);
		consume /= 10;
		if (consume >= 60) {
			consume /= 60;
			consume = Math.round(consume * 100);
			consume /= 100;
			msg2 = consume + " 分";
		}
		else
			msg2 = consume + " 秒";
		
		String msg = msg1 + "处理图片数: " + mProgress + "\n任务耗时: " + msg2;
		
		if (mType == TaskType.TASK_SPLIT) {
			msg += "\n\n是否载入分割结果文件？";
			int ret = JOptionPane.showConfirmDialog(null, msg, "任务摘要", JOptionPane.YES_NO_OPTION);
			return (ret == JOptionPane.YES_OPTION) ? true : false;
		}
		else {
			JOptionPane.showMessageDialog(null, msg, "任务摘要", JOptionPane.INFORMATION_MESSAGE);
			return false;
		}
	}
	
	private long mTime;
	
	private void startTask() {
		
		if (mTime == 0)
			mTime = System.currentTimeMillis();
		
		if (mTasks == null)
			startSingleTask();
		else
			startMultiTasks();
	}
	
	private class TaskRunnable implements Runnable {
		
		private BatchTask mTask;
		
		public TaskRunnable(BatchTask task) {
			mTask = task;
		}

		@Override
		public void run() {
			if (mType == TaskType.TASK_SPLIT) {
				this.mTask.doSplit(mPath, mDir, mHori, mLtor, mNum);
			}
			else if (mType == TaskType.TASK_CROP) {
				this.mTask.doCropAndScale(mPath, mMaxWidth, mMaxHeight, false, false);
			}
			else if (mType == TaskType.TASK_CROP_SCALE) {
				this.mTask.doCropAndScale(mPath, mMaxWidth, mMaxHeight, true, false);
			}
			else if (mType == TaskType.TASK_CROP_SCALE_WIDTH) {
				this.mTask.doCropAndScale(mPath, mMaxWidth, mMaxHeight, true, true);
			}
		}
	};
	
	private void startSingleTask() {
		
		if (mThread != null)
			return;
		
		TaskRunnable r = new TaskRunnable(mTask);
		mThread = new Thread(r);
		mThread.start();
	}
	
	private void startMultiTasks() {
		
		if (mThreads != null)
			return;
		
		mThreads = new Thread[mTasks.length];
		for (int i = 0; i < mTasks.length; i++) {
			TaskRunnable r = new TaskRunnable(mTasks[i]);
			mThreads[i] = new Thread(r);
			mThreads[i].start();
		}
	}
	
	private void stopTask() {
		mStop = true;
		if (mTasks == null)
			stopSingleTask();
		else
			stopMultiTasks();
	}
	
	private void stopSingleTask() {
		mTask.stop();
		mThread.interrupt();
	}
	
	private void stopMultiTasks() {
		for (int i = 0; i < mTasks.length; i++) {
			mTasks[i].stop();
			mThreads[i].interrupt();
		}
	}
}
