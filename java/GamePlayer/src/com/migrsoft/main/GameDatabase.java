package com.migrsoft.main;

import java.util.Vector;

import SQLite.Database;
import SQLite.Exception;
import SQLite.TableResult;

/**
 * @author wuyulun
 *
 */
public class GameDatabase {
	
	interface EventListener {
		
		void onRomChanged();
	}
	
	private static final String DB_NAME = "/games.db";
	
	private Database mDatabase;
	
	private EventListener mListener;
	
	public GameDatabase() {
		
		mDatabase = new Database();
		
		String home = System.getenv("HOME");
		String path = home + DB_NAME;
		System.out.println("open database: " + path);
		
		try {
			mDatabase.open(path, SQLite.Constants.SQLITE_OPEN_READWRITE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void close() {
		try {
			mDatabase.close();
			System.out.println("close database.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setEventListener(EventListener listener) {
		mListener = listener;
	}
	
	public void addRom(String game, String type, String rom) {
		
		String sql = String.format("INSERT INTO collection VALUES (null, '%s', '%s', '%s');", type, game, rom);
		System.out.println(sql);
		try {
			mDatabase.exec(sql, null);
			if (mListener != null) {
				mListener.onRomChanged();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean isRomExist(String rom) {
		
		boolean exist = false;
		String sql = String.format("SELECT name FROM collection WHERE path='%s';", rom);
		try {
			TableResult r = mDatabase.get_table(sql);
			exist = (r.nrows == 0) ? false : true;
		} catch (Exception e) {
			exist = true;
			e.printStackTrace();
		}
		return exist;
	}
	
	public void updateRom(String game, String type, String rom) {
		
		String sql = String.format("UPDATE collection SET name='%s', type='%s' WHERE path='%s';", game, type, rom);
		System.out.println(sql);
		try {
			mDatabase.exec(sql, null);
			if (mListener != null) {
				mListener.onRomChanged();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Vector getDataVector() {
		
		Vector data = new Vector<Vector<String>>();
		try {
			TableResult r = mDatabase.get_table("SELECT name, type, path FROM collection ORDER BY name;");
			for (int i = 0; i < r.nrows; i++) {
				String[] dbRow = (String[]) r.rows.elementAt(i);
				Vector row = new Vector<String>();
				for (int j = 0; j < dbRow.length; j++) {
					row.add(dbRow[j]);
				}
				data.add(row);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}
}
