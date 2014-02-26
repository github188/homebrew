package com.migrsoft.main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

public class ManualDlg extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2321720365717710413L;
	
	public ManualDlg(JFrame owner) {
//		super(owner, "帮助", false);
		super();
		
		JTextPane txtPane = new JTextPane();
		txtPane.setEditable(false);
		try {
			txtPane.setPage(getClass().getResource("manual.htm"));
		}
		catch (Exception e) {
		}
		JScrollPane scrPane = new JScrollPane(txtPane);
		
		JButton btnClose = new JButton("确定");
		btnClose.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		
		add(btnClose, BorderLayout.SOUTH);
		add(scrPane, BorderLayout.CENTER);
		
		Dimension size = owner.getSize();
		setSize(size.width / 2, size.height / 2);
		
		Point loc = owner.getLocation();
		setLocation(loc.x + size.width - getWidth(), loc.y);
	}

}
