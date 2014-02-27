package com.migrsoft.main;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class GameInfoView extends JPanel {

	private static final long serialVersionUID = 4148816333554541444L;
	
	private static final String LAB_BUTTON_START = "*** 开始游戏 ***";
	
	private static final String LAB_NAME = "游戏:";
	private static final String LAB_TYPE = "机型:";
	private static final String LAB_ROM = "ROM:";
	private static final String LAB_BROWSE = "浏览";
	
	private static final String LAB_BUTTON_ADD = "增加";
	private static final String LAB_BUTTON_UPDATE = "更新";
	
	private GameDatabase mDatabase;
	
	private SnapshotView mSnapshot;
	
	private JTextField mTxtGame;
	
	private JTextField mTxtType;
	
	private JTextField mTxtRom;

	public GameInfoView() {
		
		setSize(400, 300);
		
		ButtonListener buttonListener = new ButtonListener();
		
		// 图片显示区
		
		mSnapshot = new SnapshotView();
		
		JButton btnStart = new JButton(LAB_BUTTON_START);
		btnStart.setAlignmentX(CENTER_ALIGNMENT);
		btnStart.addActionListener(buttonListener);
		
		// 游戏信息编辑区
		
		JLabel labName = new JLabel(LAB_NAME);
		mTxtGame = new JTextField();
		Box hbox1 = Box.createHorizontalBox();
		hbox1.add(labName);
		hbox1.add(mTxtGame);
		
		JLabel labType = new JLabel(LAB_TYPE);
		mTxtType = new JTextField();
		Box hbox2 = Box.createHorizontalBox();
		hbox2.add(labType);
		hbox2.add(mTxtType);
		
		JLabel labRom = new JLabel(LAB_ROM);
		mTxtRom = new JTextField();
		mTxtRom.setEditable(false);
		JButton btnBrowse = new JButton(LAB_BROWSE);
		btnBrowse.addActionListener(buttonListener);
		Box hbox3 = Box.createHorizontalBox();
		hbox3.add(labRom);
		hbox3.add(mTxtRom);
		hbox3.add(btnBrowse);
		
		JButton btnAdd = new JButton(LAB_BUTTON_ADD);
		btnAdd.setAlignmentX(CENTER_ALIGNMENT);
		btnAdd.addActionListener(buttonListener);
		setButtonSize(btnAdd);
		
		JButton btnUpdate = new JButton(LAB_BUTTON_UPDATE);
		btnUpdate.setAlignmentX(CENTER_ALIGNMENT);
		btnUpdate.addActionListener(buttonListener);
		setButtonSize(btnUpdate);
		
		Box hbox4 = Box.createHorizontalBox();
		hbox4.add(btnAdd);
		hbox4.add(btnUpdate);
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(mSnapshot);
		add(btnStart);
		add(hbox1);
		add(hbox2);
		add(hbox3);
		add(hbox4);
	}
	
	public void setDatabase(GameDatabase database) {
		mDatabase = database;
	}
	
	public void setGame(String game, String type, String rom) {
		mTxtGame.setText(game);
		mTxtType.setText(type);
		mTxtRom.setText(rom);
		
		int p1 = rom.lastIndexOf("/");
		int p2 = rom.lastIndexOf(".");
		if (p1 != -1 && p2 != -1) {
			String romName = rom.substring(p1 + 1, p2);
			mSnapshot.setRom(romName, type);
		}
	}
	
	private void setButtonSize(Component comp) {
		
		final int WIDTH = 100;
		final int HEIGHT = 24;
		
		comp.setPreferredSize(new Dimension(WIDTH, HEIGHT)); //设置最大、最小和合适的大小相同 
		comp.setMaximumSize(new Dimension(WIDTH, HEIGHT));
		comp.setMinimumSize(new Dimension(WIDTH, HEIGHT));
	}
	
	private void openRom() {
		JFileChooser dlg = new JFileChooser();
		int r = dlg.showOpenDialog(this);
		if (r == JFileChooser.APPROVE_OPTION) {
			File rom = dlg.getSelectedFile();
			mTxtRom.setText(rom.getAbsolutePath());
		}
	}
	
	class SnapshotView extends JPanel {
		
		private static final long serialVersionUID = 1552637189242684270L;
		
		private static final int WIDTH = 320;
		private static final int HEIGHT = 240;
		
		private String mName;
		
		private BufferedImage mImage;

		public SnapshotView() {
			
			this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
			this.setMaximumSize(new Dimension(WIDTH, HEIGHT));
			this.setMinimumSize(new Dimension(WIDTH, HEIGHT));
		}
		
		public void setRom(String name, String type) {
			mName = name;
			if (type.equals("gba")) {
				openGbaImage(mName);
			}
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			Graphics2D g2 = (Graphics2D)g;
			
			if (mImage != null) {
				int x = (getWidth() - mImage.getWidth()) / 2;
				int y = (getHeight() - mImage.getHeight()) / 2;
				g2.drawImage(mImage, x, y, null);
			}
		}

		private void openGbaImage(String name) {
			
			mImage = null;
			
			Vector<String> list = getGbaImages();
			if (list != null && !list.isEmpty()) {
				for (int i = 0; i < list.size(); i++) {
					String path = list.elementAt(i);
					int pos = path.lastIndexOf("/");
					if (pos != -1 && path.substring(pos + 1).startsWith(name)) {
						
						try {
							System.out.println("snap " + path);
							mImage = ImageIO.read(new File(path));
						} catch (IOException e) {
							e.printStackTrace();
						}
						break;
					}
				}
			}
			repaint();
		}
		
		private Vector<String> getGbaImages() {
			
			final String HOME = System.getenv("HOME");
			final String PATH = HOME + "/.mednafen/snaps/";
			
			File p = new File(PATH);
			if (!p.exists()) {
				return null;
			}
			
			String[] all = p.list();
			if (all == null) {
				return null;
			}
			
			Vector<String> list = new Vector<String>();
			for (String s : all) {
				File f = new File(PATH + s);
				if (f.isFile() && f.getName().endsWith(".png")) {
					list.add(PATH + s);
				}
			}
			return list;
		}
	}
	
	class ButtonListener implements ActionListener {
		
		private String mGame;
		private String mType;
		private String mRom;
		
		private boolean getValues() {
			mGame = mTxtGame.getText();
			mType = mTxtType.getText();
			mRom = mTxtRom.getText();
			if (mGame != null && mGame.length() > 0
				&& mType != null && mType.length() > 0
				&& mRom != null && mRom.length() > 0 ) {
				return true;
			}
			else {
				return false;
			}
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			
			String cmd = event.getActionCommand();
			
			if (cmd.equals(LAB_BROWSE)) {
				openRom();
			}
			else if (cmd.equals(LAB_BUTTON_ADD)) {
				if (getValues()) {
					if (mDatabase.isRomExist(mRom)) {
						String msg = String.format("%s 已存在！", mGame);
						JOptionPane.showMessageDialog(null, msg);
					}
					else {
						mDatabase.addRom(mGame, mType, mRom);
					}
				}
			}
			else if (cmd.equals(LAB_BUTTON_UPDATE)) {
				if (getValues()) {
					if (mDatabase.isRomExist(mRom)) {
						mDatabase.updateRom(mGame, mType, mRom);
					}
					else {
						String msg = String.format("%s 不存在，请先添加！", mGame);
						JOptionPane.showMessageDialog(null, msg);
					}
				}
			}
			else if (cmd.equals(LAB_BUTTON_START)) {
				if (getValues()) {
					if (mType.equals("gba")) {
						String command = String.format("/Users/wuyulun/bin/mednafen %s", mRom);
						System.out.println(command);
						try {
							Runtime.getRuntime().exec(command);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		
	}
}
