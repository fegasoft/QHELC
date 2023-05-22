package py.com;


/*
 * Prueba del algoritmo QHELC
 * Autor: Federico Gaona
 * 
 */

import java.io.File;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import ij.process.ByteProcessor;

public class TestQHELC {

	public static void main(String[] args) {
		
		//String sCarpAct = System.getProperty("user.dir").concat("..\\ImageJ-PDI\\imagenes");
		String sCarpAct = "..\\ImageJ-PDI\\imagenes";
		File carpeta = new File(sCarpAct);
		String[] listado = carpeta.list();

		if (listado == null || listado.length == 0) {
			System.out.println("No hay elementos dentro de la carpeta actual");
			return;
		} else {
			
			for (int i = 0; i < listado.length; i++)	
			
				try {
								
					System.out.println(listado[i]);
					String ruta = sCarpAct.concat("\\").concat(listado[i]);
					System.out.println(ruta);
					
					ImagePlus im = IJ.openImage(ruta);
					im.show(); 								// show the original image
					
					ImagePlus im2 = im.duplicate();

					long time_start = System.currentTimeMillis();

					QHELC qhelc = new QHELC();
					ImageProcessor ip = im2.getProcessor();
					qhelc.run(ip);
					
					//showHistograms(im.getProcessor(), ruta, qhelc);
					
					long time_end = System.currentTimeMillis();
					long time = time_end - time_start;
					
					System.out.print("execution time = ");
					System.out.println(time);
														
					im2.show();								// show the enhanced image
					
					String rGuardar = sCarpAct.concat("\\resultados\\").concat("QHELC");
					System.out.println(rGuardar);
					IJ.saveAs(im2, "tif", rGuardar);
					
					System.out.println("Terminado");
									
				}
				catch (Exception e) {
					System.out.println(e);
				}
		}
	}
	
	private static void showHistograms(ImageProcessor ip, String imTitle, QHELC q) {
								
		final int w_histogram_window = q.K;
		final int h_histogram_window = 100;
		
		// show the histogram

		float scale_factor_h = (float)(h_histogram_window) / (float)(q.getMaxHistogram());
		
		ImageProcessor h_ip = new ByteProcessor(w_histogram_window, h_histogram_window);
		h_ip.setValue(255);
		h_ip.fill();
				
		for (int x = 0; x < w_histogram_window; ++x) {
			
			for (int y = 0; y < (int)((float)(q.histogram[x]) * scale_factor_h); ++y) {
				
				h_ip.putPixel(x, h_histogram_window - y, 0);
								
//				if (y == (int)((float)(q.CLL1) * scale_factor_h))
//					for (int xx = 0; xx <= q.SPL; xx++)
//						h_ip.putPixel(xx, y, 0);
//				
//				if (y == (int)((float)(q.CLL2) * scale_factor_h))
//					for (int xx = q.SPL + 1; xx <= q.SP; xx++)
//						h_ip.putPixel(xx, y, 0);
//				
//				if (y == (int)((float)(q.CLU1) * scale_factor_h))
//					for (int xx = q.SP + 1; xx <= q.SPU; xx++)
//						h_ip.putPixel(xx, y, 0);
//				
//				if (y == (int)((float)(q.CLU2) * scale_factor_h))
//					for (int xx = q.SPU + 1; xx <= q.K - 1; xx++)
//						h_ip.putPixel(xx, y, 0);

			}
			
//			if (x == q.SPL)
//				for (int y = 0; y < h_histogram_window; y++)
//					h_ip.putPixel(x, y, 0);
//			if (x == q.SP)
//				for (int y = 0; y < h_histogram_window; y++)
//					h_ip.putPixel(x, y, 0);
//			if (x == q.SPU)
//				for (int y = 0; y < h_histogram_window; y++)
//					h_ip.putPixel(x, y, 0);
			
		}
		
		ImagePlus h_im = new ImagePlus("Histogram of " + imTitle, h_ip);
		h_im.show();
		
		// show the cumulative histogram
		
//		float scale_factor_ch = (float)(h_histogram_window) / (float)(q.getMaxCHistogram());
//		
//		ImageProcessor ch_ip = new ByteProcessor(w_histogram_window, h_histogram_window);
//		ch_ip.setValue(255);
//		ch_ip.fill();
//		
//		for (int x = 0; x < w_histogram_window; ++x)
//			for (int y = 0; y < (int)((float)(q.chistogram[x]) * scale_factor_ch); ++y)
//				ch_ip.putPixel(x, h_histogram_window - y, 0);
//
//		ImagePlus ch_im = new ImagePlus("Cumulative Histogram of " + imTitle, ch_ip);
//		ch_im.show();
				
		// show the sqrt cumulative histogram
		
//		float scale_factor_chsqrt = (float)(h_histogram_window) / q.max_sqrt_cH;
//		
//		ImageProcessor ch_ipsqrt = new ByteProcessor(w_histogram_window, h_histogram_window);
//		ch_ipsqrt.setValue(255);
//		ch_ipsqrt.fill();
//		
//		for (int x = 0; x < w_histogram_window; ++x)
//			for (int y = 0; y < (int)(q.sqrt_cH[x] * scale_factor_chsqrt); ++y)
//				ch_ipsqrt.putPixel(x, h_histogram_window - y, 0);
//
//		ImagePlus ch_imsqrt = new ImagePlus("SQRT Cumulative Histogram of " + imTitle, ch_ipsqrt);
//		ch_imsqrt.show();
		
	}

}
