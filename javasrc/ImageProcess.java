package led;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.activation.MailcapCommandMap;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.ImageIcon;

import led.ReflectImageBuilder.Mode;

public class ImageProcess {
	public static enum Mode {
		LINEAR, EXP
	}

	public static void main(String[] args) throws IOException {
		ImageProcess.trumb("./login-bg_504.jpg", 110, 110, "./small_5.png", "./r_5.png");
	}

	private static BufferedImage cropImageForReflect(String filePath) {
		FileInputStream is = null;
		ImageInputStream iis = null;
		try {
			Image srcImage = new ImageIcon(filePath).getImage();
			is = new FileInputStream(filePath);
			iis = ImageIO.createImageInputStream(is);
			Iterator<ImageReader> it = ImageIO.getImageReaders(iis);
			ImageReader reader = it.next();
			reader.setInput(iis, true);
			ImageReadParam param = reader.getDefaultReadParam();
			Rectangle rect = new Rectangle(0, srcImage.getHeight(null) - 10,
					srcImage.getWidth(null), 10);
			param.setSourceRegion(rect);
			BufferedImage bi = reader.read(0, param);
			return bi;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static BufferedImage scaleImage(BufferedImage _src, int _width,
			int _height) {
		BufferedImage tag = new BufferedImage((int) _width, (int) _height,
				BufferedImage.TYPE_INT_RGB);
		tag.getGraphics().drawImage(
				_src.getScaledInstance(_width, _height, Image.SCALE_SMOOTH), 0,
				0, null);
		return tag;
	}

	private static BufferedImage cropImage(String _filePath, int _x, int _y,
			int _width, int _height) {
		FileInputStream is = null;
		ImageInputStream iis = null;
		try {

			is = new FileInputStream(_filePath);
			iis = ImageIO.createImageInputStream(is);
			Iterator<ImageReader> it = ImageIO.getImageReaders(iis);
			ImageReader reader = it.next();
			reader.setInput(iis, true);
			ImageReadParam param = reader.getDefaultReadParam();
			Rectangle rect = new Rectangle(_x, _y, _width, _height);
			param.setSourceRegion(rect);
			BufferedImage bi = reader.read(0, param);
			return bi;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static Image createReflectImage(Image img, Mode mode) {
		// 从图片中提取像素数据
		int w = img.getWidth(null);
		int h = img.getHeight(null);

		int[] pixels = new int[w * h];
		PixelGrabber pg = new PixelGrabber(img, 0, 0, w, h, pixels, 0, w);
		try {
			pg.grabPixels();
		} catch (Exception ex) {
		}

		for (int y = 0; y < h; ++y) {
			double p = (double) (y) / h; // 每一行的透明度0~255
			if (mode == Mode.EXP) {
				p = Math.sqrt(p);
			}
			int a = 100 - (int) (p * 100);

			for (int x = 0; x < w; ++x) {
				int rgb = pixels[y * w + x] & 0xFFFFFF;
				pixels[y * w + x] = (a << 24) | rgb;
			}
		}

		MemoryImageSource source = new MemoryImageSource(w, h, pixels, 0, w);
		Image reflectImg = Toolkit.getDefaultToolkit().createImage(source);
		pixels = null;

		return reflectImg;
	}

	private static BufferedImage flipImage(BufferedImage bufferedimage) {
		BufferedImage dstImage = null;
		AffineTransform transform = new AffineTransform(1, 0, 0, -1, 0,
				bufferedimage.getHeight());// 垂直翻转
		AffineTransformOp op = new AffineTransformOp(transform,
				AffineTransformOp.TYPE_BILINEAR);
		dstImage = op.filter(bufferedimage, null);
		return dstImage;
	}
    private static BufferedImage ImageToBufferedImage(Image image)
    {
      BufferedImage dest = new BufferedImage(
   		   image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2 = dest.createGraphics();
      g2.drawImage(image, 0, 0, null);
      g2.dispose();
      return dest;
    }
	public static void trumb(String _imagePath, int _width, int _height,
			String _outputPath, String _reflectPath) throws IOException {
		Image srcImage = new ImageIcon(_imagePath).getImage();
		int imageWidth = srcImage.getWidth(null);
		int imageHeight = srcImage.getHeight(null);
		int x, y, width, height;
		if (imageHeight > (_height * ((float) imageWidth / (float) _width))) {
			// cropHeight
			x = 0;
			y = (imageHeight - (int) (_height * ((float) imageWidth / (float) _width))) / 2;
			width = imageWidth;
			height = imageHeight - y * 2;

		} else {
			// cropWidth
			x = (imageWidth - (int) (_width * ((float) imageHeight / (float) _height))) / 2;
			;
			y = 0;
			width = imageWidth - x * 2;
			height = imageHeight;

		}

		BufferedImage bi = ImageProcess.scaleImage(
				ImageProcess.cropImage(_imagePath, x, y, width, height),
				_width, _height);
		ImageIO.write(bi, "png", new File(_outputPath));
		// BufferedImage reflectBi =
		// ImageProcess.cropImageForReflect(_outputPath);
		Image tmpImg = ImageProcess.createReflectImage(ImageProcess
				.flipImage(ImageProcess.cropImageForReflect(_outputPath)),
				ImageProcess.Mode.LINEAR);
		BufferedImage bin = ImageProcess.ImageToBufferedImage(tmpImg);
		ImageIO.write(bin, "png", new File(_reflectPath));
		// String filePath = "./aaa2.png";

	}
}
