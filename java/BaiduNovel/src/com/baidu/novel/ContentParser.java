package com.baidu.novel;

import java.io.DataInputStream;
import java.io.FileInputStream;

/**
 * @author wuyulun
 *
 */
public class ContentParser {

	/**
	 * 读取百度小说离线目录文件 contents
	 * @param path
	 */
	public void readBinContents(String path) {
		FileInputStream fis = null;
		DataInputStream dis = null;

		try {
			fis = new FileInputStream(path);
			dis = new DataInputStream(fis);

			String version = dis.readUTF();
			int size = dis.readInt();

			for (int i = 0; i < size; i++) {
				String title = dis.readUTF();
				int index = dis.readInt();
				int offset = dis.readInt();
				String id = dis.readUTF();
				System.out.println(title + "," + offset);
			}

			dis.close();
			dis = null;

			fis.close();
			fis = null;
		} catch (RuntimeException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {

				if (dis != null) {
					dis.close();
					dis = null;
				}

				if (fis != null) {
					fis.close();
					fis = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	public static void main(String[] args) {
		ContentParser parser = new ContentParser();
		parser.readBinContents("/Users/wuyulun/temp/contents");
	}
}
