package com.migrsoft.main;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.filechooser.FileFilter;

import com.migrsoft.main.ProgressDlg.TaskType;

public class MainView extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 778619808848682268L;
	
	private static final String sMainTitle = "ComicCrop";
	private static final String sVersion = "1.3.3";
	
	private PicEditor mEditor;
	private PicList mList;
	private Dashboard mBoard;
	
	private String mLastPath;

	public MainView() {
		super(sMainTitle);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		createMenu();
		createContent();
		
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		
		setSize((int)(screen.getWidth() * 0.9f), (int)(screen.getHeight() * 0.9f));
		setLocation((int)(screen.getWidth() - getWidth()) / 2, (int)(screen.getHeight() - getHeight()) / 2);
		setVisible(true);
	}
	
	private final String menuFile = "文件";
	private final String menuFileOpen = "打开图片";
	private final String menuFileTest = "测试";
	
	private final String menuPic = "图片";
	private final String menuPicToGray = "转换为灰度图";
	private final String menuPicAutoGrayLevel = "自动计算灰阶";
	private final String menuPicOutputPng = "输出为 PNG 格式";
	private final String menuPicOutputJpeg = "输出为 JPEG 格式（质量：中）";
	private final String menuPicCropWhite = "切白边";
	private final String menuPicCropBlack = "切黑边";
	private final String menuPicSize480 = "480 x 800";
	private final String menuPicSize600 = "600 x 1000";
	private final String menuPicSize800 = "800 x 1280";
	
	private final String menuTask = "任务";
	private final String menuTaskSplit = "批量页面分割";
	private final String menuTaskCrop1 = "批量页面操作：旋转→剪裁";
	private final String menuTaskCrop2 = "批量页面操作：旋转→剪裁→缩放";
	private final String menuTaskCrop3 = "批量页面操作：旋转→剪裁→缩放宽度";
	
	private final String menuTaskThread = "线程设置";
	private final String menuTaskThreadUse1 = "1 线程";
	private final String menuTaskThreadUse2 = "2 线程";
	private final String menuTaskThreadUse3 = "3 线程";
	private final String menuTaskThreadUse4 = "4 线程";
	
	private final String menuHelp = "帮助";
	private final String menuHelpGuide = "使用说明";
	private final String menuHelpUpdate = "查看更新";
	private final String menuHelpAbout = "关于";
	
	private void createMenu() {
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		////////////////////////////////////////////////////////////
		
		ActionListener menuHandler = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				String cmd = event.getActionCommand();
				
				if (cmd.equals(menuFileOpen)) {
					openFile();
				}
				else if (cmd.equals(menuFileTest)) {
				}
				else if (cmd.equals(menuPicToGray)) {
					mEditor.toGrayMode();
				}
				else if (cmd.equals(menuPicAutoGrayLevel)) {
					JCheckBoxMenuItem item = (JCheckBoxMenuItem)event.getSource();
					MainParam.getInstance().setAutoGrayLevel(item.isSelected());
					mEditor.reload();
				}
				else if (cmd.equals(menuPicOutputPng)) {
					JRadioButtonMenuItem item = (JRadioButtonMenuItem)event.getSource();
					if (item.isSelected())
						MainParam.getInstance().setOutputFormat(MainParam.OUTPUT_FORMAT_PNG);
				}
				else if (cmd.equals(menuPicOutputJpeg)) {
					JRadioButtonMenuItem item = (JRadioButtonMenuItem)event.getSource();
					if (item.isSelected())
						MainParam.getInstance().setOutputFormat(MainParam.OUTPUT_FORMAT_JPEG);
				}
				else if (cmd.equals(menuPicCropWhite)) {
					JRadioButtonMenuItem item = (JRadioButtonMenuItem)event.getSource();
					if (item.isSelected())
						MainParam.getInstance().setCropWhite(true);
					mEditor.reload();
				}
				else if (cmd.equals(menuPicCropBlack)) {
					JRadioButtonMenuItem item = (JRadioButtonMenuItem)event.getSource();
					if (item.isSelected())
						MainParam.getInstance().setCropWhite(false);
					mEditor.reload();
				}
				else if (cmd.equals(menuPicSize480)) {
					MainParam.getInstance().setMaxWidth(480);
					MainParam.getInstance().setMaxHeight(800);
				}
				else if (cmd.equals(menuPicSize600)) {
					MainParam.getInstance().setMaxWidth(600);
					MainParam.getInstance().setMaxHeight(1000);
				}
				else if (cmd.equals(menuPicSize800)) {
					MainParam.getInstance().setMaxWidth(800);
					MainParam.getInstance().setMaxHeight(1280);
				}
				else if (cmd.equals(menuTaskSplit)) {
					batchSplitWork();
				}
				else if (cmd.equals(menuTaskCrop1)) {
					batchCropWork();
				}
				else if (cmd.equals(menuTaskCrop2)) {
					batchCropAndScaleWork();
				}
				else if (cmd.equals(menuTaskCrop3)) {
					batchCropAndScaleWidthWork();
				}
				else if (cmd.equals(menuHelpGuide)) {
					ManualDlg dlg = new ManualDlg(MainView.this);
					dlg.setVisible(true);
				}
				else if (cmd.equals(menuHelpUpdate)) {
					try {
						URI url = new URI("http://pan.baidu.com/share/link?shareid=185881&uk=2299302813");
						Desktop.getDesktop().browse(url);
					}
					catch (Exception e) {
					}
				}
				else if (cmd.equals(menuHelpAbout)) {
					JOptionPane.showMessageDialog(null,
							"ComicCrop 版本 " + sVersion + "\n2012-2013 © migr",
							"关于",
							JOptionPane.INFORMATION_MESSAGE);
				}
				else if (cmd.equals(menuTaskThreadUse1)) {
					MainParam.getInstance().setMaxThreads(1);
				}
				else if (cmd.equals(menuTaskThreadUse2)) {
					MainParam.getInstance().setMaxThreads(2);
				}
				else if (cmd.equals(menuTaskThreadUse3)) {
					MainParam.getInstance().setMaxThreads(3);
				}
				else if (cmd.equals(menuTaskThreadUse4)) {
					MainParam.getInstance().setMaxThreads(4);
				}
			}
		};
		
		////////////////////////////////////////////////////////////
		
		JMenu file = new JMenu(menuFile);
		menuBar.add(file);
		
		JMenuItem file_open = new JMenuItem(menuFileOpen);
		file_open.addActionListener(menuHandler);

		JMenuItem file_test = new JMenuItem(menuFileTest);
		file_test.addActionListener(menuHandler);
		
		file.add(file_open);
//		file.addSeparator();
//		file.add(file_test);
		
		////////////////////////////////////////////////////////////
		
		JMenu pic = new JMenu(menuPic);
		menuBar.add(pic);
		
		JMenuItem pic_gray = new JMenuItem(menuPicToGray);
		pic_gray.addActionListener(menuHandler);
		
		JCheckBoxMenuItem pic_autoGrayLevel = new JCheckBoxMenuItem(menuPicAutoGrayLevel, MainParam.getInstance().isAutoGrayLevel());
		pic_autoGrayLevel.addActionListener(menuHandler);
		
		JRadioButtonMenuItem pic_formatPng = new JRadioButtonMenuItem(menuPicOutputPng);
		pic_formatPng.addActionListener(menuHandler);
		
		JRadioButtonMenuItem pic_formatJpg = new JRadioButtonMenuItem(menuPicOutputJpeg);
		pic_formatJpg.addActionListener(menuHandler);
		
		ButtonGroup formatGroup = new ButtonGroup();
		formatGroup.add(pic_formatPng);
		formatGroup.add(pic_formatJpg);
		
		if (MainParam.getInstance().getOutputFormat() == MainParam.OUTPUT_FORMAT_PNG)
			pic_formatPng.setSelected(true);
		else
			pic_formatJpg.setSelected(true);

		JRadioButtonMenuItem pic_cropWhite = new JRadioButtonMenuItem(menuPicCropWhite);
		pic_cropWhite.addActionListener(menuHandler);
		
		JRadioButtonMenuItem pic_cropBlack = new JRadioButtonMenuItem(menuPicCropBlack);
		pic_cropBlack.addActionListener(menuHandler);
		
		ButtonGroup cropGroup = new ButtonGroup();
		cropGroup.add(pic_cropWhite);
		cropGroup.add(pic_cropBlack);
		
		if (MainParam.getInstance().isCropWhite())
			pic_cropWhite.setSelected(true);
		else
			pic_cropBlack.setSelected(true);
		
		JRadioButtonMenuItem pic_size480 = new JRadioButtonMenuItem(menuPicSize480);
		pic_size480.addActionListener(menuHandler);
		
		JRadioButtonMenuItem pic_size600 = new JRadioButtonMenuItem(menuPicSize600);
		pic_size600.addActionListener(menuHandler);
		
		JRadioButtonMenuItem pic_size800 = new JRadioButtonMenuItem(menuPicSize800);
		pic_size800.addActionListener(menuHandler);
		
		ButtonGroup sizeGroup = new ButtonGroup();
		sizeGroup.add(pic_size480);
		sizeGroup.add(pic_size600);
		sizeGroup.add(pic_size800);
		
		pic_size600.setSelected(true);
		
		pic.add(pic_gray);
		pic.addSeparator();
		pic.add(pic_autoGrayLevel);
		pic.addSeparator();
		pic.add(pic_formatPng);
		pic.add(pic_formatJpg);
		pic.addSeparator();
		pic.add(pic_cropWhite);
		pic.add(pic_cropBlack);
		pic.addSeparator();
		pic.add(pic_size480);
		pic.add(pic_size600);
		pic.add(pic_size800);
		
		////////////////////////////////////////////////////////////
		
		JMenu task = new JMenu(menuTask);
		menuBar.add(task);
		
		JMenuItem task_batchSplit = new JMenuItem(menuTaskSplit);
		task_batchSplit.addActionListener(menuHandler);
		
		JMenuItem task_batchCrop2 = new JMenuItem(menuTaskCrop1);
		task_batchCrop2.addActionListener(menuHandler);
		
		JMenuItem task_batchCrop3 = new JMenuItem(menuTaskCrop2);
		task_batchCrop3.addActionListener(menuHandler);
		
		JMenuItem task_batchCrop4 = new JMenuItem(menuTaskCrop3);
		task_batchCrop4.addActionListener(menuHandler);

		
		JMenu taskThread = new JMenu(menuTaskThread);
		
		JRadioButtonMenuItem taskThread_1 = new JRadioButtonMenuItem(menuTaskThreadUse1);
		taskThread_1.addActionListener(menuHandler);
		JRadioButtonMenuItem taskThread_2 = new JRadioButtonMenuItem(menuTaskThreadUse2);
		taskThread_2.addActionListener(menuHandler);
		JRadioButtonMenuItem taskThread_3 = new JRadioButtonMenuItem(menuTaskThreadUse3);
		taskThread_3.addActionListener(menuHandler);
		JRadioButtonMenuItem taskThread_4 = new JRadioButtonMenuItem(menuTaskThreadUse4);
		taskThread_4.addActionListener(menuHandler);
		
		taskThread.add(taskThread_1);
		taskThread.add(taskThread_2);
		taskThread.add(taskThread_3);
		taskThread.add(taskThread_4);
		
		ButtonGroup threadGroup = new ButtonGroup();
		threadGroup.add(taskThread_1);
		threadGroup.add(taskThread_2);
		threadGroup.add(taskThread_3);
		threadGroup.add(taskThread_4);
		
		switch (MainParam.getInstance().getMaxThreads()) {
		case 1:
			taskThread_1.setSelected(true);
			break;
		case 2:
			taskThread_2.setSelected(true);
			break;
		case 3:
			taskThread_3.setSelected(true);
			break;
		case 4:
			taskThread_4.setSelected(true);
			break;
		}
		
		task.add(task_batchSplit);
		task.addSeparator();
		task.add(task_batchCrop2);
		task.addSeparator();
		task.add(task_batchCrop3);
		task.addSeparator();
		task.add(task_batchCrop4);
		task.addSeparator();
		task.add(taskThread);
		
		////////////////////////////////////////////////////////////
		
		JMenu help = new JMenu(menuHelp);
		menuBar.add(help);
		
		JMenuItem help_manual = new JMenuItem(menuHelpGuide);
		help_manual.addActionListener(menuHandler);
		
		JMenuItem help_update = new JMenuItem(menuHelpUpdate);
		help_update.addActionListener(menuHandler);
		
		JMenuItem help_about = new JMenuItem(menuHelpAbout);
		help_about.addActionListener(menuHandler);
		
		help.add(help_manual);
		help.add(help_update);
		help.addSeparator();
		help.add(help_about);
	}
	
	private void createContent() {
		
		mEditor = new PicEditor();
		mEditor.setActListener(new PicEditor.ActListener() {
			
			@Override
			public void loadPrevItem() {
				mList.loadPrevItem();
			}
			
			@Override
			public void loadNextItem() {
				mList.loadNextItem();
			}

			@Override
			public void cropChanged(boolean isWhite) {
				mBoard.setCropType(isWhite);
			}
		});
		
		mList = new PicList();
		mList.setActListener(new PicList.ActListener() {
			
			@Override
			public void onSelect(String name) {
				updateTitle();
				mEditor.load(mLastPath + name);
			}

			@Override
			public void onDelete(String name) {
				updateTitle();
				File f = new File(mLastPath + name);
				f.delete();
				resetEditor();
			}

			@Override
			public void onRemove() {
				updateTitle();
				resetEditor();
			}
		});
		
		mBoard = new Dashboard(mEditor.isCropMode());
		mBoard.setHorizontalSplit(mEditor.isHorizonalSplit());
		mBoard.setSplitNum(mEditor.getSplitNum());
		mBoard.setLeftToRight(mEditor.isLeftToRight());
		mBoard.setActListener(new Dashboard.ActListener() {
			
			@Override
			public void onClockwise(boolean ctrlPressed) {
				mEditor.rotate(true, ctrlPressed);
			}
			
			@Override
			public void onAntiClockwise(boolean ctrlPressed) {
				mEditor.rotate(false, ctrlPressed);
			}

			@Override
			public void onSave() {
				mEditor.save(null);
			}

			@Override
			public void onScale() {
				mEditor.scale(false);
			}

			@Override
			public void onScaleWidth() {
				mEditor.scale(true);
			}

			@Override
			public void onOptionChanged() {
				boolean hori = mBoard.isHorizontalSplit();
				boolean ltor = mBoard.isLeftToRight();
				int num = mBoard.getSplitNum();
				mEditor.setSplitInfo(hori, num, ltor);
			}

			@Override
			public void onModeChanged(boolean isCrop) {
				if (isCrop)
					mEditor.useCropMode();
				else
					mEditor.useSplitMode();
			}

			@Override
			public void onSplit() {
				mEditor.split();
			}

			@Override
			public void onCalcCrop() {
				mEditor.calcCrop();
			}

			@Override
			public void onCrop() {
				mEditor.crop();
			}

			@Override
			public void onCropChanged(boolean isWhite) {
				mEditor.cropChanged(isWhite);
			}

			@Override
			public void onGrowArea() {
				mEditor.growArea();
			}
		});
		
		Container contentPane = getContentPane();
		contentPane.add(mEditor, BorderLayout.CENTER);
		contentPane.add(mList.getPane(), BorderLayout.WEST);
		contentPane.add(mBoard, BorderLayout.EAST);
	}
	
	private void updateTitle() {
		int index = mList.getSelectedIndex() + 1;
		int total = mList.getTotal();
		setTitle(sMainTitle + " " + index + " | " + total);
	}
	
	private HashMap<String, TaskData> mTaskInfo;
	
	private void openFile() {
		JFileChooser dlg = new JFileChooser();
		dlg.setFileFilter(new FileFilter() {

			@Override
			public boolean accept(File f) {
				String name = f.getName().toLowerCase();
				return name.endsWith(".jpg") || name.endsWith(".png") || f.isDirectory();
			}

			@Override
			public String getDescription() {
				return "图像文件";
			}
			
		});
		dlg.setMultiSelectionEnabled(true);
		if (mLastPath != null)
			dlg.setCurrentDirectory(new File(mLastPath));
		int r = dlg.showOpenDialog(this);
		if (r == JFileChooser.APPROVE_OPTION) {
			File[] fl = dlg.getSelectedFiles();
			int pathLen = fl[0].getPath().length();
			int nameLen = fl[0].getName().length();
			
			mLastPath = fl[0].getPath().substring(0, pathLen - nameLen);
			Vector<String> taskList = new Vector<String>();
			mTaskInfo = new HashMap<String, TaskData>();
			
			for (File f : fl) {
				taskList.add(f.getName());
			}
			
			mList.update(taskList);

			resetEditor();
		}
	}
	
	public void openPath(String path) {
		
		File p = new File(path);
		String[] all = p.list();

		mLastPath = path;
		Vector<String> taskList = new Vector<String>();
		mTaskInfo = new HashMap<String, TaskData>();
		
		String ext = MainParam.getInstance().getOutputExtName();
		for (String s : all) {
			File f = new File(path + s);
			if (f.isFile() && !f.isHidden()) {
				if (f.getName().endsWith(ext)) {
					taskList.add(f.getName());
				}
			}
		}
		
		mList.update(taskList);
		
		resetEditor();
		
		mBoard.useCropMode();
		mEditor.useCropMode();
	}
	
	private void clearTempDir(String path) {

		File p = new File(path);
		if (!p.exists())
			return;
		
		String[] all = p.list();
		if (all == null)
			return;
		
		if (all.length > 0) {
			if (JOptionPane.showConfirmDialog(
					this, "是否清空 " + path + " 文件夹？", "删除确认", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
				return;
		}
		
		String ext = MainParam.getInstance().getOutputExtName();
		for (String s : all) {
			File f = new File(path + s);
			if (f.isFile() && !f.isHidden()) {
				if (f.getName().endsWith(ext))
					f.delete();
			}
		}
	}
	
	private boolean useMultiThreads(int size) {
		int max = MainParam.getInstance().getMaxThreads();
		if (max == 1 || size < max)
			return false;
		else
			return true;
	}
	
	private void batchSplitWork() {
		Vector<String> taskList = mList.getList();
		if (taskList.size() > 0) {
			mEditor.saveTaskData();
			
			clearTempDir(mLastPath + mEditor.getSaveDir());
			
			ProgressDlg dlg = new ProgressDlg(this);
			
			if (useMultiThreads(taskList.size())) {
				dlg.arrangeWork(taskList, mTaskInfo, TaskType.TASK_SPLIT,
						mLastPath, mEditor.getSaveDir(),
						mEditor.isHorizonalSplit(), mEditor.isLeftToRight(), mEditor.getSplitNum(),
						0, 0);
			}
			else {
				BatchTask batch = new BatchTask();
				batch.setTask(taskList, mTaskInfo);
				
				dlg.setMax(taskList.size());
				dlg.setBatchTask(batch, TaskType.TASK_SPLIT,
						mLastPath, mEditor.getSaveDir(),
						mEditor.isHorizonalSplit(), mEditor.isLeftToRight(), mEditor.getSplitNum(),
						0, 0);
			}
			
			dlg.initGui();
			dlg.setVisible(true);
		}
	}
	
	private void batchCropWork() {
		Vector<String> taskList = mList.getList();
		if (taskList.size() > 0) {
			mEditor.saveTaskData();
			
			ProgressDlg dlg = new ProgressDlg(this);
			int maxw = MainParam.getInstance().getMaxWidth();
			int maxh = MainParam.getInstance().getMaxHeight();

			if (useMultiThreads(taskList.size())) {
				dlg.arrangeWork(taskList, mTaskInfo, TaskType.TASK_CROP,
						mLastPath, null, false, false, 0, maxw, maxh);
			}
			else {
				BatchTask batch = new BatchTask();
				batch.setTask(taskList, mTaskInfo);
	
				dlg.setMax(taskList.size());
				dlg.setBatchTask(batch, TaskType.TASK_CROP,
						mLastPath, null, false, false, 0, maxw, maxh);
			}
			
			dlg.initGui();
			dlg.setVisible(true);
		}
	}
	
	private void batchCropAndScaleWork() {
		Vector<String> taskList = mList.getList();
		if (taskList.size() > 0) {
			mEditor.saveTaskData();
			
			ProgressDlg dlg = new ProgressDlg(this);
			int maxw = MainParam.getInstance().getMaxWidth();
			int maxh = MainParam.getInstance().getMaxHeight();

			if (useMultiThreads(taskList.size())) {
				dlg.arrangeWork(taskList, mTaskInfo, TaskType.TASK_CROP_SCALE,
						mLastPath, null, false, false, 0, maxw, maxh);
			}
			else {
				BatchTask batch = new BatchTask();
				batch.setTask(taskList, mTaskInfo);
	
				dlg.setMax(taskList.size());
				dlg.setBatchTask(batch, TaskType.TASK_CROP_SCALE,
						mLastPath, null, false, false, 0, maxw, maxh);
			}
			
			dlg.initGui();
			dlg.setVisible(true);
		}
	}
	
	private void batchCropAndScaleWidthWork() {
		Vector<String> taskList = mList.getList();
		if (taskList.size() > 0) {
			mEditor.saveTaskData();
			
			ProgressDlg dlg = new ProgressDlg(this);
			int maxw = MainParam.getInstance().getMaxWidth();
			int maxh = MainParam.getInstance().getMaxHeight();

			if (useMultiThreads(taskList.size())) {
				dlg.arrangeWork(taskList, mTaskInfo, TaskType.TASK_CROP_SCALE_WIDTH,
						mLastPath, null, false, false, 0, maxw, maxh);
			}
			else {
				BatchTask batch = new BatchTask();
				batch.setTask(taskList, mTaskInfo);
	
				dlg.setMax(taskList.size());
				dlg.setBatchTask(batch, TaskType.TASK_CROP_SCALE_WIDTH,
						mLastPath, null, false, false, 0, maxw, maxh);
			}
			
			dlg.initGui();
			dlg.setVisible(true);
		}
	}
	
	public TaskData loadTaskData(String path) {
		String name = getNameFromPath(path);
		if (mTaskInfo.containsKey(name))
			return mTaskInfo.get(name);
		return null;
	}
	
	public void saveTaskData(String path, TaskData data) {
		String name = getNameFromPath(path);
		if (mTaskInfo.containsKey(name))
			mTaskInfo.remove(name);
		mTaskInfo.put(name, data);
	}
	
	private String getNameFromPath(String path) {
		return path.substring(mLastPath.length());
	}
	
	public void clearTaskInfo() {
		mTaskInfo.clear();
	}
	
	public void resetEditor() {
		mEditor.reset();
		mEditor.repaint();
	}
	
	static private MainView sInstance = null;
	
	static public MainView getInstance() {
		return sInstance;
	}
	
	public static void main(String[] args) {
		
		Runnable r = new Runnable() {
			public void run() {
				sInstance = new MainView();
			}
		};
		EventQueue.invokeLater(r);
		
		Runnable checkin = new Runnable() {

			@Override
			public void run() {
				try {
					URL url = new URL("http://pan.baidu.com/share/link?shareid=185765&uk=2299302813");
					HttpURLConnection conn = (HttpURLConnection)url.openConnection();
					conn.setRequestProperty("Accept-Encoding", "gzip");
					conn.connect();
					int code = conn.getResponseCode();
					if (code == 200) {
					}
				}
				catch (Exception e) {
				}
				finally {
					System.out.println("Welcome to ComicCrop!");
				}
			}
		};
//		new Thread(checkin).start();
	}
}
