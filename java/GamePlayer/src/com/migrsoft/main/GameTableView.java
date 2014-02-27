package com.migrsoft.main;

import java.awt.GridLayout;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

public class GameTableView extends JPanel {
	
	interface EventListener {
		
		void onGameSelected(String game, String type, String rom);
	}

	private static final long serialVersionUID = 8097297175104405079L;
	
	private GameDatabase mDatabase;
	
	private MyTableModel mModel;
	
	private JTable mTable;
	
	private String[] columnNames = { "Game", "Type", "ROM" };
	
	private EventListener mListener;

	public GameTableView() {
		super(new GridLayout(1, 0));

		mModel = new MyTableModel();
		mTable = new JTable(mModel);
		mTable.setFillsViewportHeight(true);
		mTable.getSelectionModel().addListSelectionListener(new RowListener());

		// Create the scroll pane and add the table to it.
		JScrollPane scrollPane = new JScrollPane(mTable);

		// Add the scroll pane to this panel.
		add(scrollPane);
		
		initData();
	}
	
	public void setDatabase(GameDatabase database) {
		mDatabase = database;
	}
	
	public void setEventListener(EventListener listener) {
		mListener = listener;
	}
	
	private void initData() {
		mModel.setColumnCount(columnNames.length);
		mModel.setColumnIdentifiers(columnNames);
		mModel.setRowCount(0);
	}
	
	public void updateData() {
		Vector data = mDatabase.getDataVector();
		Vector col = new Vector<String>();
		for (int i=0; i < columnNames.length; i++) {
			col.add(columnNames[i]);
		}
		mModel.setDataVector(data, col);
	}
	
	class MyTableModel extends DefaultTableModel {

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
		
	}
	
	class RowListener implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent event) {
			if (event.getValueIsAdjusting()) {
				return;
			}
			if (mListener != null) {
				int sel = mTable.getSelectedRow();
				if (sel != -1) {
					String game = (String) mModel.getValueAt(sel, 0);
					String type = (String) mModel.getValueAt(sel, 1);
					String rom = (String) mModel.getValueAt(sel, 2);
					mListener.onGameSelected(game, type, rom);
				}
			}
		}
		
	}
}
