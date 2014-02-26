package com.migrsoft.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class PicList {
	
	public interface ActListener {
		public void onSelect(String name);
		public void onDelete(String name);
		public void onRemove();
	}

	private DefaultListModel mModel;
	private JList mList;
	private JScrollPane mPane;
	private JPopupMenu mPopup;
	
	private ActListener mActListener;
	
	private final String menuPopDelete = "删除";
	private final String menuPopRemove = "移除";
	
	public PicList() {
		
		// 创建列表
		
		mModel = new DefaultListModel();
		mList = new JList(mModel);
		mPane = new JScrollPane(mList);
		mList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		mList.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					String s = (String) mList.getSelectedValue();
					if (s != null && mActListener != null)
						mActListener.onSelect(s);
				}
			}
			
		});

		// 创建列表弹出菜单
		
		mPopup = new JPopupMenu();
		JMenuItem meuDelete = new JMenuItem(menuPopDelete);
		JMenuItem meuRemove = new JMenuItem(menuPopRemove);
		
		ActionListener popMenuHandler = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String cmd = e.getActionCommand();
				if (cmd.equals(menuPopDelete)) {
					Object o = mList.getSelectedValue();
					mModel.removeElement(o);
					mActListener.onDelete((String)o);
				}
				else if (cmd.equals(menuPopRemove)) {
					Object o = mList.getSelectedValue();
					mModel.removeElement(o);
					mActListener.onRemove();
				}
			}
		};
		
		meuDelete.addActionListener(popMenuHandler);
		meuRemove.addActionListener(popMenuHandler);
		
		mPopup.add(meuDelete);
		mPopup.addSeparator();
		mPopup.add(meuRemove);
		
		mList.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					if (mList.getSelectedValue() != null) {
						mPopup.show(e.getComponent(), e.getX(), e.getY());
					}
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseReleased(MouseEvent e) {
			}
			
		});
		
		mList.setFixedCellWidth(200);
	}
	
	public void setActListener (ActListener listener) {
		mActListener = listener;
	}
	
	public JScrollPane getPane() {
		return mPane;
	}
	
	public void update(Vector<String> values) {
		mModel.clear();
		for (String s : values) {
			mModel.addElement(s);
		}
		
		JScrollBar bar = mPane.getVerticalScrollBar();
		bar.setValue(bar.getMinimum());
	}
	
	public int getSelectedIndex() {
		return mList.getSelectedIndex();
	}
	
	public int getTotal() {
		return mModel.getSize();
	}
	
	public void loadPrevItem() {
		if (mModel.getSize() == 0)
			return;
		
		int sel = mList.getSelectedIndex();
		if (sel == -1)
			sel = 0;
		else if (sel > 0)
			sel--;
		else
			return;
		
		mList.setSelectedIndex(sel);
	}
	
	public void loadNextItem() {
		if (mModel.getSize() == 0)
			return;
		
		int sel = mList.getSelectedIndex();
		if (sel == -1)
			sel = 0;
		else if (sel < mModel.getSize())
			sel++;
		else
			return;
		
		mList.setSelectedIndex(sel);
	}

	public Vector<String> getList() {
		Vector<String> lst = new Vector<String>();
		for (int i = 0; i < mModel.getSize(); i++)
			lst.add((String)mModel.get(i));
		return lst;
	}
}
