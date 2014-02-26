package com.migrsoft.main;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;

public class Dashboard extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7555265674033457515L;
	
	public interface ActListener {
		public void onClockwise(boolean ctrlPressed);
		public void onAntiClockwise(boolean ctrlPressed);
		public void onSplit();
		public void onScale();
		public void onScaleWidth();
		public void onSave();
		public void onOptionChanged();
		public void onModeChanged(boolean isCrop);
		public void onCropChanged(boolean isWhite);
		public void onCalcCrop();
		public void onCrop();
		public void onGrowArea();
	}
	
	private ActListener mActListener;
	
	private final String labTaskSplit = "分割";
	private final String labTaskCrop = "剪裁";
	private final String labSplitOption = "分割选项";
	
	private final String labCropWhite = "切白边";
	private final String labCropBlack = "切黑边";
	private final String labCalcCropBox = "计算边缘";
	
	private final String labGrowArea = "放大边缘";
	
	private final String labOpSplit = "分割";
	private final String labOpAntiClockwise = "左旋";
	private final String labOpClockwise = "右旋";
	private final String labOpCrop = "剪裁";
	private final String labOpScale = "缩放";
	private final String labOpScaleWidth = "宽度";
	private final String labOpSave = "保存";
	
	private boolean mHoriSplit; // 水平分割
	private boolean mLeftToRight; // 从左到右
	private int mSplitNum; // 分割数量
	
	private JRadioButton mTaskSplit;
	private JRadioButton mTaskCrop;
	
	private JRadioButton mCropWhite;
	private JRadioButton mCropBlack;

	public Dashboard(boolean cropMode) {
		
		ActionListener buttonHandler = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				
				String cmd = event.getActionCommand();
				Object obj = event.getSource();
				
				if (cmd.equals(labTaskSplit) && obj instanceof JRadioButton) {
					mActListener.onModeChanged(false);
				}
				else if (cmd.equals(labTaskCrop) && obj instanceof JRadioButton) {
					mActListener.onModeChanged(true);
				}
				else if (cmd.equals(labSplitOption)) {
					final SplitOptionDlg dlg = new SplitOptionDlg(MainView.getInstance());
					dlg.setHoriSplit(mHoriSplit);
					dlg.setBlockNum(mSplitNum);
					dlg.setLtoR(mLeftToRight);
					dlg.initGui();
					dlg.setActListener(new SplitOptionDlg.ActListener() {
						
						@Override
						public void onOK() {
							mHoriSplit = dlg.isHoriSplit();
							mLeftToRight = dlg.isLtoR();
							mSplitNum = dlg.getBlockNum();
							mActListener.onOptionChanged();
						}
					});
					dlg.setVisible(true);
				}
				else if (cmd.equals(labCropWhite) && obj instanceof JRadioButton) {
					mActListener.onCropChanged(true);
				}
				else if (cmd.equals(labCropBlack) && obj instanceof JRadioButton) {
					mActListener.onCropChanged(false);
				}
				else if (cmd.equals(labCalcCropBox)) {
					mActListener.onCalcCrop();
				}
				else if (cmd.equals(labGrowArea)) {
					mActListener.onGrowArea();
				}
				else if (cmd.equals(labOpSplit) && obj instanceof JButton) {
					mActListener.onSplit();
				}
				else if (cmd.equals(labOpAntiClockwise)) {
					boolean ctrlPressed = false;
					if ((event.getModifiers() & ActionEvent.CTRL_MASK) != 0)
						ctrlPressed = true;
					mActListener.onAntiClockwise(ctrlPressed);
				}
				else if (cmd.equals(labOpClockwise)) {
					boolean ctrlPressed = false;
					if ((event.getModifiers() & ActionEvent.CTRL_MASK) != 0)
						ctrlPressed = true;
					mActListener.onClockwise(ctrlPressed);
				}
				else if (cmd.equals(labOpCrop) && obj instanceof JButton) {
					mActListener.onCrop();
				}
				else if (cmd.equals(labOpScale)) {
					mActListener.onScale();
				}
				else if (cmd.equals(labOpScaleWidth)) {
					mActListener.onScaleWidth();
				}
				else if (cmd.equals(labOpSave)) {
					mActListener.onSave();
				}
			}
		};

		////////////////////////////////////////////////////////////

		// 任务类型
		mTaskSplit = new JRadioButton(labTaskSplit, !cropMode);
		mTaskSplit.addActionListener(buttonHandler);
		
		mTaskCrop = new JRadioButton(labTaskCrop, cropMode);
		mTaskCrop.addActionListener(buttonHandler);
		
		ButtonGroup taskGroup = new ButtonGroup();
		taskGroup.add(mTaskSplit);
		taskGroup.add(mTaskCrop);
		
		Box taskChooseBox = Box.createHorizontalBox();
		taskChooseBox.add(mTaskSplit);
		taskChooseBox.add(mTaskCrop);
		
		JButton btnSplitOption = new JButton(labSplitOption);
		btnSplitOption.addActionListener(buttonHandler);
		
		Box taskOptionBox = Box.createHorizontalBox();
		taskOptionBox.add(btnSplitOption);
		
		// 切边类型
		mCropWhite = new JRadioButton(labCropWhite, MainParam.getInstance().isCropWhite());
		mCropWhite.addActionListener(buttonHandler);
		
		mCropBlack = new JRadioButton(labCropBlack, !MainParam.getInstance().isCropWhite());
		mCropBlack.addActionListener(buttonHandler);
		
		ButtonGroup cropGroup = new ButtonGroup();
		cropGroup.add(mCropWhite);
		cropGroup.add(mCropBlack);
		
		Box cropTypeBox = Box.createHorizontalBox();
		cropTypeBox.add(mCropWhite);
		cropTypeBox.add(mCropBlack);
		
		JButton btnAutoCalcCrop = new JButton(labCalcCropBox);
		btnAutoCalcCrop.addActionListener(buttonHandler);
		
		Box autoCalcBox = Box.createHorizontalBox();
		autoCalcBox.add(btnAutoCalcCrop);
		
		JButton btnGrowArea = new JButton(labGrowArea);
		btnGrowArea.addActionListener(buttonHandler);
		
		Box growAreaBox = Box.createHorizontalBox();
		growAreaBox.add(btnGrowArea);
				
		Box taskPanelBox = Box.createVerticalBox();
		taskPanelBox.add(taskChooseBox);
		taskPanelBox.add(taskOptionBox);
		taskPanelBox.add(cropTypeBox);
		taskPanelBox.add(autoCalcBox);
		taskPanelBox.add(growAreaBox);
		
		JPanel taskPanel = new JPanel();
		Border taskPanelBorder = BorderFactory.createEtchedBorder();
		Border taskPanelTitle = BorderFactory.createTitledBorder(taskPanelBorder, "任务类型");
		taskPanel.setBorder(taskPanelTitle);
		taskPanel.add(taskPanelBox);
		
		////////////////////////////////////////////////////////////
		
		// 分割操作
		JButton btnSplit = new JButton(labOpSplit);
		btnSplit.setAlignmentX(Component.CENTER_ALIGNMENT);
		setSize(btnSplit, true);
		btnSplit.addActionListener(buttonHandler);
		
		// 旋转操作
		JButton btnAntiClockwise = new JButton(labOpAntiClockwise);
		setSize(btnAntiClockwise, false);
		btnAntiClockwise.addActionListener(buttonHandler);
		
		JButton btnClockwise = new JButton(labOpClockwise);
		setSize(btnClockwise, false);
		btnClockwise.addActionListener(buttonHandler);

//		JPanel whirlPanel = new JPanel();
//		whirlPanel.setLayout(new GridLayout(1, 2));
		Box whirlPanel = Box.createHorizontalBox();
		whirlPanel.add(btnAntiClockwise);
		whirlPanel.add(btnClockwise);

		// 剪裁
		JButton btnCrop = new JButton(labOpCrop);
		btnCrop.setAlignmentX(Component.CENTER_ALIGNMENT);
		setSize(btnCrop, true);
		btnCrop.addActionListener(buttonHandler);
		
		// 缩放
		JButton btnScale = new JButton(labOpScale);
		setSize(btnScale, false);
		btnScale.addActionListener(buttonHandler);
		
		JButton btnScaleWidth = new JButton(labOpScaleWidth);
		setSize(btnScaleWidth, false);
		btnScaleWidth.addActionListener(buttonHandler);
		
//		JPanel scalePanel = new JPanel();
//		scalePanel.setLayout(new GridLayout(1, 2));
		Box scalePanel = Box.createHorizontalBox();
		scalePanel.add(btnScale);
		scalePanel.add(btnScaleWidth);
		
		// 保存
		JButton btnSave = new JButton(labOpSave);
		btnSave.setAlignmentX(Component.CENTER_ALIGNMENT);
		setSize(btnSave, true);
		btnSave.addActionListener(buttonHandler);
		
//		setLayout(new GridLayout(6, 1));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(taskPanel);
		add(btnSplit);
		add(whirlPanel);
		add(btnCrop);
		add(scalePanel);
		add(btnSave);
	}

	private void setSize(Component comp, boolean single) {
		int width = single ? 120 : 60;
		comp.setPreferredSize(new Dimension(width, 60)); //设置最大、最小和合适的大小相同 
		comp.setMaximumSize(new Dimension(width, 60));
		comp.setMinimumSize(new Dimension(width, 60));
	}
	
	public void setActListener(ActListener listener) {
		mActListener = listener;
	}
	
	public void setHorizontalSplit(boolean b) {
		mHoriSplit = b;
	}
	
	public boolean isHorizontalSplit() {
		return mHoriSplit;
	}
	
	public void setLeftToRight(boolean b) {
		mLeftToRight = b;
	}
	
	public boolean isLeftToRight() {
		return mLeftToRight;
	}
	
	public void setSplitNum(int n) {
		mSplitNum = n;
	}
	
	public int getSplitNum() {
		return mSplitNum;
	}
	
	public void useSplitMode() {
		mTaskSplit.setSelected(true);
	}
	
	public void useCropMode() {
		mTaskCrop.setSelected(true);
	}
	
	public void setCropType(boolean isWhite) {
		if (isWhite) {
			mCropWhite.setSelected(true);
		}
		else {
			mCropBlack.setSelected(true);
		}
	}
}
