package com.migrsoft.main;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

public class MainGui extends JFrame {

	private static final long serialVersionUID = -234139175394101960L;
	
	private static final String APP_TITLE = "GamePlayer";
	
	private static final int WIDTH = 640;
	private static final int HEIGHT = 480;
	
	private GameDatabase mDatabase;
	
	private GameTableView mTableView;
	
	private GameInfoView mInfoView;
	
	public MainGui() {
		super(APP_TITLE);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		addWindowListener(new MyWindowListener());
		
		mDatabase = new GameDatabase();
		mDatabase.setEventListener(new MyDatabaseEventListener());
		
		mTableView = new GameTableView();
		mTableView.setDatabase(mDatabase);
		mTableView.setEventListener(new MyTableEventListener());
		mTableView.setOpaque(true);
		mTableView.updateData();
		
		mInfoView = new GameInfoView();
		mInfoView.setDatabase(mDatabase);
		
		Container contentPane = getContentPane();
		contentPane.add(mTableView, BorderLayout.CENTER);
		contentPane.add(mInfoView, BorderLayout.EAST);
		
		setSize(WIDTH, HEIGHT);
		setVisible(true);
	}

	public static void main(String[] args) {
		
		Runnable r = new Runnable() {
			public void run() {
				new MainGui();
			}
		};
		EventQueue.invokeLater(r);
		
		System.out.println("SQLite version: " + SQLite.Database.version());
	}
	
	class MyWindowListener implements WindowListener {

		@Override
		public void windowOpened(WindowEvent e) {
		}

		@Override
		public void windowClosing(WindowEvent e) {
			mDatabase.close();
		}

		@Override
		public void windowClosed(WindowEvent e) {
		}

		@Override
		public void windowIconified(WindowEvent e) {
		}

		@Override
		public void windowDeiconified(WindowEvent e) {
		}

		@Override
		public void windowActivated(WindowEvent e) {
		}

		@Override
		public void windowDeactivated(WindowEvent e) {
		}
		
	}
	
	class MyDatabaseEventListener implements GameDatabase.EventListener {

		@Override
		public void onRomChanged() {
			mTableView.updateData();
		}
		
	}
	
	class MyTableEventListener implements GameTableView.EventListener {

		@Override
		public void onGameSelected(String game, String type, String rom) {
			mInfoView.setGame(game, type, rom);
		}
		
	}
}
