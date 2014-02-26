package com.migrsoft.main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class SplitOptionDlg extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3463647468272419861L;
	
	public interface ActListener {
		public void onOK();
	}
	
	private boolean mHoriSplit;
	private boolean mLtoR;
	private int mBlocks;

	private ActListener mListener;
	
	Point mLoc;
	Dimension mSize;
	
	public void setActListener(ActListener listener) {
		mListener = listener;
	}
	
	public SplitOptionDlg(JFrame owner) {
		super(owner, "分割页面选项", true);
		
		mLoc = owner.getLocation();
		mSize = owner.getSize();
	}
	
	public void initGui() {
		
		// 分割方向
		final JRadioButton radHoriSplit = new JRadioButton("水平分割", mHoriSplit);
		JRadioButton radVertSplit = new JRadioButton("垂直分割", !mHoriSplit);
		
		ButtonGroup grpSplit = new ButtonGroup();
		grpSplit.add(radHoriSplit);
		grpSplit.add(radVertSplit);
		
		Box splitBox = Box.createHorizontalBox();
		splitBox.add(radHoriSplit);
		splitBox.add(radVertSplit);
		
		// 页码顺序
		final JCheckBox chkPage = new JCheckBox("从左到右");
		chkPage.setSelected(mLtoR);
		
		Box pageBox = Box.createHorizontalBox();
		pageBox.add(new JLabel("页码方式: "));
		pageBox.add(chkPage);

		// 分割区域数量
		final JSpinner spiBlocks = new JSpinner(new SpinnerNumberModel(mBlocks, 2, 8, 1));
		
		Box blockBox = Box.createHorizontalBox();
		blockBox.add(new JLabel("分割数量: "));
		blockBox.add(spiBlocks);
		
		// 决定
		JButton btnOk = new JButton("确定");
		btnOk.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				
				mHoriSplit = radHoriSplit.isSelected();
				mLtoR = chkPage.isSelected();
				mBlocks = (Integer) spiBlocks.getValue();
				mListener.onOK();
			}
		});
		
		JButton btnCancel = new JButton("取消");
		btnCancel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		
		Box bottomBox = Box.createHorizontalBox();
		bottomBox.setAlignmentX(CENTER_ALIGNMENT);
		bottomBox.add(btnOk);
		bottomBox.add(btnCancel);
		
		// 主布局
		Box mainBox = Box.createVerticalBox();
		mainBox.add(splitBox);
		mainBox.add(pageBox);
		mainBox.add(blockBox);
		mainBox.add(bottomBox);
		
		add(mainBox, BorderLayout.CENTER);
		
		// 位置、区域
		setSize(250, 150);
		setLocation(mLoc.x + (mSize.width - getWidth()) / 2, mLoc.y + (mSize.height - getWidth()) / 2);
	}

	public void setHoriSplit(boolean hori) {
		mHoriSplit = hori;
	}
	
	public boolean isHoriSplit() {
		return mHoriSplit;
	}
	
	public void setLtoR(boolean rtol) {
		mLtoR = rtol;
	}
	
	public boolean isLtoR() {
		return mLtoR;
	}
	
	public void setBlockNum(int num) {
		mBlocks = num;
	}
	
	public int getBlockNum() {
		return mBlocks;
	}
}
