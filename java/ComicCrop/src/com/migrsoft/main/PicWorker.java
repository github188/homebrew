package com.migrsoft.main;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

public class PicWorker {
	
	static public BufferedImage load(String path) {
		BufferedImage image = null;
		try {
			image = ImageIO.read(new File(path));
			if (   image.getType() == BufferedImage.TYPE_BYTE_BINARY
				|| image.getType() == BufferedImage.TYPE_BYTE_INDEXED
				|| MainParam.getInstance().isForceGray()) {
				image = convertToGray(image);
			}
		}
		catch (Exception e) {
		}
		return image;
	}
	
	static public boolean save(BufferedImage image, String path) {
		
		boolean ret = false;
		int ext = getExtNamePos(path);
		String newPath;
		
		String extName = MainParam.getInstance().getOutputExtName();
		
		if (ext == -1)
			newPath = path + extName;
		else
			newPath = path.substring(0, ext) + extName;
		
		switch (MainParam.getInstance().getOutputFormat()) {
		case MainParam.OUTPUT_FORMAT_PNG:
			ret = saveAsPng(image, newPath);
			break;
			
		case MainParam.OUTPUT_FORMAT_JPEG:
			ret = saveAsJpeg(image, newPath);
			break;
		}
		
		return ret;
	}
	
	static private boolean saveAsPng(BufferedImage image, String path) {
		
		try {
			ImageIO.write(image, "PNG", new File(path));
			return true;
		}
		catch (Exception e) {
		}
		return false;
	}
	
	static private boolean saveAsJpeg(BufferedImage image, String path) {
		
		ImageTypeSpecifier type = ImageTypeSpecifier.createFromRenderedImage(image);
		ImageWriter writer = null;
		Iterator<ImageWriter> it = ImageIO.getImageWriters(type, "JPG");
		if (it.hasNext())
			writer = it.next();
		else
			return false;
		
		ImageWriteParam param = writer.getDefaultWriteParam();
		param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		param.setCompressionQuality(MainParam.getInstance().getJpegQuality());
		
		IIOImage ioimage = new IIOImage(image, null, null);
		
		try {
			ImageOutputStream output = ImageIO.createImageOutputStream(new File(path));
			writer.setOutput(output);
			writer.write(null, ioimage, param);
			return true;
		}
		catch (Exception e) {
		}
		
		return false;
	}
	
	static public BufferedImage convertToGray(BufferedImage image) {
		BufferedImage tmp = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		Graphics2D g2 = (Graphics2D)tmp.getGraphics();
		g2.drawImage(image, 0, 0, null);
		return tmp;
	}
	
	static public BufferedImage crop(BufferedImage image, int x, int y, int w, int h) {
		
		if (Math.abs(w - image.getWidth()) < 2 && Math.abs(h - image.getHeight()) < 2)
			return null;
		
		BufferedImage tmp = new BufferedImage(w, h, image.getType());

		// 支持大文件操作
		int x1, y1, x2, y2;
		int r = x + w;
		int b = y + h;
		for (x1 = x, x2 = 0; x1 < r; x1++, x2++) {
			for (y1 = y, y2 = 0; y1 < b; y1++, y2++) {
				int color = image.getRGB(x1, y1);
				tmp.setRGB(x2, y2, color);
			}
		}
		
		return tmp;
	}

	static public BufferedImage rotate(BufferedImage image, float angle) {
		
		AffineTransform ts = AffineTransform.getRotateInstance(Math.toRadians(angle), image.getWidth() / 2, image.getHeight() / 2);
		AffineTransformOp op = new AffineTransformOp(ts, AffineTransformOp.TYPE_BICUBIC);

		BufferedImage tmp = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
		Graphics2D g2 = (Graphics2D)tmp.getGraphics();
		g2.setPaint(Color.WHITE);
		g2.fillRect(0, 0, image.getWidth(), image.getHeight());
		op.filter(image, tmp);
		
		return tmp;
	}
	
	/**
	 * 在指定区域内缩放
	 * @param image
	 * @param maxW
	 * @param maxH
	 * @return
	 */
	static public BufferedImage scale(BufferedImage image, double maxW, double maxH) {
		
		double ws = maxW / image.getWidth();
		double hs = maxH / image.getHeight();
		
		if (ws >= 1f && hs >= 1f)
			return null;
		
		double s = Math.min(ws, hs);
		
		AffineTransform ts = AffineTransform.getScaleInstance(s, s);
		AffineTransformOp op = new AffineTransformOp(ts, AffineTransformOp.TYPE_BICUBIC);

		int w = (int) (image.getWidth() * s);
		int h = (int) (image.getHeight() * s);
		BufferedImage tmp = new BufferedImage(w, h, image.getType());
		op.filter(image, tmp);
		
		return tmp;
	}
	
	static public BufferedImage scaleWidth(BufferedImage image, double maxW, double maxH) {

		if (maxW == image.getWidth())
			return null;
		
		double s = maxW / image.getWidth();
		
		if (s < 1f) {
			// 缩小宽度
			
			AffineTransform ts = AffineTransform.getScaleInstance(s, s);
			AffineTransformOp op = new AffineTransformOp(ts, AffineTransformOp.TYPE_BICUBIC);

			int w = (int) (image.getWidth() * s);
			int h = (int) (image.getHeight() * s);
			BufferedImage tmp = new BufferedImage(w, h, image.getType());
			op.filter(image, tmp);
			
			return tmp;
		}
		else {
			// 放大宽度，加左右占位符

			BufferedImage tmp = new BufferedImage((int) maxW, image.getHeight(), image.getType());
			
			Graphics2D g2 = (Graphics2D)tmp.getGraphics();
			
			g2.setColor(Color.WHITE);
			g2.fillRect(0, 0, tmp.getWidth(), tmp.getHeight());
			
			g2.setColor(Color.LIGHT_GRAY);
			int w = 10, h = 30;
			
			g2.fillRect(0, (tmp.getHeight() - h) / 2, w, h);
			g2.fillRect(tmp.getWidth() - w, (tmp.getHeight() - h) / 2, w, h);
			
			g2.drawImage(image, (int) ((maxW - image.getWidth()) / 2), 0, null);
			
			return tmp;
		}
	}
	
	/**
	 * 扫描白边
	 * @param image
	 * @return
	 */
	static public Rectangle2D.Float calcCropBox(BufferedImage image, boolean cropWhite) {
		
		Rectangle2D.Float box = new Rectangle2D.Float();
		
		int i, j;
		int limit, valid, count;
		int ci, cc;
		
		int w = image.getWidth();
		int h = image.getHeight();
		
		WritableRaster raster = image.getRaster();
		ColorModel model = image.getColorModel();

		int scan = 20; // 在首次扫描到有效位置后，再进行的试探扫描量
				
		// 水平扫描范围限制
		limit = w / 5;
		valid = (int) (h * 0.02);
		
		// 扫描左侧白边
		ci = -1; // 记录上次的切边位置
	    cc = 0; // 有效扫描次数
		for (i = 0; i < limit; i++) {
			count = 0; // 有效像素
			for (j = 0; j < h; j++) {
				Object data = raster.getDataElements(i, j, null);
				int argb = model.getRGB(data);
				Color color = new Color(argb, false);
				if (isValidColor(color, cropWhite))
					count++;
				if (count > valid)
					break;
			}
			
			// 检测列扫描结果
			if (count > valid)
				cc++;
			if (count > valid && ci == -1)
				ci = i;
			if (count < valid) {
				cc = 0;
				ci = -1;
			}
			if (cc > scan) {
				i = ci;
				break;
			}
		}
		box.x = i;
		
		// 扫描右侧白边
		ci = -1;
		cc = 0;
		for (i = w - 1; i > w - limit; i--) {
			count = 0;
			for (j = 0; j < h; j++) {
				Object data = raster.getDataElements(i, j, null);
				int argb = model.getRGB(data);
				Color color = new Color(argb, false);
				if (isValidColor(color, cropWhite))
					count++;
				if (count > valid)
					break;
			}
			
			// 检测列扫描结果
			if (count > valid)
				cc++;
			if (count > valid && ci == -1)
				ci = i;
			if (count < valid) {
				cc = 0;
				ci = -1;
			}
			if (cc > scan) {
				i = ci;
				break;
			}
		}
		box.width = i - box.x;
		
		// 垂直扫描范围限制
		limit = h / 6;
		valid = (int) (w * 0.01);
		
		// 扫描顶部白边
		ci = -1;
		cc = 0;
		for (i = 0; i < limit; i++) {
			count = 0;
			for (j = 0; j < w; j++) {
				Object data = raster.getDataElements(j, i, null);
				int argb = model.getRGB(data);
				Color color = new Color(argb, false);
				if (isValidColor(color, cropWhite))
					count++;
				if (count > valid)
					break;
			}

			// 检测列扫描结果
			if (count > valid)
				cc++;
			if (count > valid && ci == -1)
				ci = i;
			if (count < valid) {
				cc = 0;
				ci = -1;
			}
			if (cc > scan) {
				i = ci;
				break;
			}
		}
		box.y = i;
		
		// 扫描底部白边
		for (i = h - 1; i > h - limit; i--) {
			count = 0;
			for (j = 0; j < w; j++) {
				Object data = raster.getDataElements(j, i, null);
				int argb = model.getRGB(data);
				Color color = new Color(argb, false);
				if (isValidColor(color, cropWhite))
					count++;
				if (count > valid)
					break;
			}
			
			if (count > valid)
				break;
		}
		box.height = i - box.y;
		
		return box;
	}
	
	static private boolean isValidColor(Color color, boolean cropWhite) {
		if (cropWhite)
			return color.getRed() < 220 || color.getGreen() < 220 || color.getBlue() < 220;
		else
			return color.getRed() > 50 || color.getGreen() > 50 || color.getBlue() > 50;
	}
	
	/**
	 * 分割页面
	 * @param image			原图
	 * @param horizonal		水平分割
	 * @param lines			分平线
	 * @param path			保存路径
	 * @param name			保存主文件名
	 * @param ltor			从左到右的编排页号
	 */
	static public void split(BufferedImage image, boolean horizonal, int[] lines, String path, String name, boolean ltor) {
		
		if (lines == null) {
			save(image, genSplitFileName(path, name, 1));
			return;
		}
		
		int x, y, w, h;
		int no = (ltor || !horizonal) ? 1 : lines.length + 1;
		
		x = y = 0;
		for (int i = 0; i < lines.length; i++) {
			if (horizonal) {
				w = lines[i] - x;
				h = image.getHeight();
			}
			else {
				w = image.getWidth();
				h = lines[i] - y;
			}

			String newPath = genSplitFileName(path, name, no);
			BufferedImage im = crop(image, x, y, w, h);
			if (im != null)
				save(im, newPath);
			
			no = (ltor || !horizonal) ? no + 1 : no - 1;
			if (horizonal)
				x = lines[i];
			else
				y = lines[i];
		}
		
		if (horizonal) {
			w = image.getWidth() - x;
			h = image.getHeight();
		}
		else {
			w = image.getWidth();
			h = image.getHeight() - y;
		}
		String newPath = genSplitFileName(path, name, no);
		BufferedImage im = crop(image, x, y, w, h);
		if (im != null)
			save(im, newPath);
	}
	
	/**
	 * 生成分页文件名
	 * @param path	保存路径
	 * @param name	主文件名
	 * @param no	页码
	 * @return		新文件名
	 */
	static private String genSplitFileName(String path, String name, int no) {
		int ext = getExtNamePos(name);
		if (ext == -1)
			return path + name + "_" + no;
		else
			return path + name.substring(0, ext) + "_" + no + name.substring(ext);
	}
	
	static private int getExtNamePos(String name) {
		int pos = name.lastIndexOf(".");
		if (pos > 0) {
			String ext = name.substring(pos);
			ext.toLowerCase();
			if (ext.compareTo(".jpg") == 0 || ext.compareTo(".png") == 0)
				return pos;
		}
		return -1;
	}
	
	static public int[] calcSplitSides(int w, int h, boolean hori, int num) {
		int step;
		if (hori) {
			if (w < h)
				return null;
			step = w / num;
		}
		else {
			if (w > h)
				return null;
			step = h / num;
		}
		int[] sides = new int[num - 1];
		for (int i = 0; i < num - 1; i++) {
			sides[i] = step * (i + 1);
		}
		return sides;
	}
	
	static public BufferedImage binary(BufferedImage image) {
		BufferedImage tmp = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
		WritableRaster srcRaster = image.getRaster();
		WritableRaster dstRaster = tmp.getRaster();
		ColorModel model = image.getColorModel();
		int x, y;
		final int threshold = 50;
		for (x = 0; x < image.getWidth(); x++) {
			for (y = 0; y < image.getHeight(); y++) {
				Object data = srcRaster.getDataElements(x, y, null);
				int argb = model.getRGB(data);
				Color c = new Color(argb, true);
				if (c.getRed() < threshold || c.getGreen() < threshold || c.getBlue() < threshold)
					argb = Color.BLACK.getRGB();
				else
					argb = Color.WHITE.getRGB();
				data = model.getDataElements(argb, null);
				dstRaster.setDataElements(x, y, data);
			}
		}
		return tmp;
	}
	
	static public BufferedImage edge(BufferedImage image) {
		BufferedImage tmp = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
		float[] elements = { 0.0f, -1.0f, 0.0f, -1.0f, 4.0f, -1.0f, 0.0f, -1.0f, 0.0f };
		Kernel kernel = new Kernel(3, 3, elements);
		ConvolveOp cop = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
		cop.filter(image, tmp);
		return tmp;
	}
	
	static public boolean isGrayMode(BufferedImage image) {
		
		if (image.getType() == BufferedImage.TYPE_BYTE_GRAY)
			return true;
		
		int step = image.getHeight() / 10; // 采样间隔
		for (int y = 0; y < image.getHeight(); y += step) {
			for (int x = 0; x < image.getWidth(); x++) {
				int color = image.getRGB(x, y);
//				int a = color >> 24 & 0xff;
				int r = color >> 16 & 0xff;
				int g = color >> 8 &0xff;
				int b = color & 0xff;
				if (r != g || g != b || r != b)
					return false;
			}
		}
		return true;
	}
	
	static public boolean autoGrayLevel(BufferedImage image) {
		
		int[] gl = new int[256];
		
		// 计算直方图
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int color = image.getRGB(x, y);
				int g = color & 0xff;
				gl[g]++;
			}
		}
		
//		for (int i = 0; i < gl.length; i++) {
//			if (i % 16 == 0)
//				System.out.println();
//			System.out.print(gl[i] + " ");
//		}
		
		// 计算阀值
		final float factor = 0.001f;
		int total = (int) (image.getWidth() * image.getHeight() * factor);
		int min = 0, max = 255;
		
		int c = 0;
		for (int i = 0; i < gl.length; i++) {
			c += gl[i];
			if (c >= total) {
				min = i;
				break;
			}
		}
		
		c = 0;
		for (int i = 255; i >= 0; i--) {
			c += gl[i];
			if (c >= total) {
				max = i;
				break;
			}
		}
		
//		System.out.println();
//		System.out.println("min " + min + " max " + max);
		
		if (min == 0 && max == 255)
			return false;
		if (min == max)
			return false;
		
		// 改变灰阶
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int color = image.getRGB(x, y);
				int g = color & 0xff;
				if (g <= min)
					g = 0;
				else if (g >= max)
					g = 255;
				else
					g = (g - min) * 255 / (max - min);
				color = 0xff000000 | g << 16 | g << 8 | g;
				image.setRGB(x, y, color);
			}
		}
		
		return true;
	}
}
