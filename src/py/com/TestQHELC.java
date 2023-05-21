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
					ImagePlus im = IJ.openImage(ruta); // carga la imagen

					im.show(); // Muestra la imagen original
					
					ImagePlus im2 = im.duplicate();

					// Inicio del algoritmo
					long time_start = System.currentTimeMillis();

					// Agregar el algoritmo de QHELC
					QHELC qhelc = new QHELC();
					ImageProcessor ip = im2.getProcessor();
					qhelc.run(ip);

					showHistograms(im.getProcessor(), ruta, qhelc);
					
					// Fin del tiempo en milisegundos
					long time_end = System.currentTimeMillis();
					long time = time_end - time_start;
					//System.out.println(time);
														
					//im2.show();	// Muestra la imagen mejorada

					// Guardar los resultados
					String rGuardar = System.getProperty("user.dir").concat("\\resultados\\QHELC\\").concat(listado[i]);
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
				
		
		int [] histogram_ac = new int[q.LEVELS];
		histogram_ac = q.getACHistogram(ip);
		int max_histogram_ac = q.getMaxHistogram(histogram_ac);
				
		final int w_histogram = q.LEVELS;
		final int h_histogram = 100;

		float factor_escala = (float)(h_histogram) / (float)(q.getMaxHistogram(q.histo));
		
		System.out.print("SPL = ");
		System.out.print(q.SPL);
		System.out.print(", SP = ");
		System.out.print(q.SP);
		System.out.print(", SPU = ");
		System.out.print(q.SPU);

//		System.out.print(", IL1 = ");
//		System.out.print(q.IL1);
//		System.out.print(", IL2 = ");
//		System.out.print(q.IL2);
//		System.out.print(", IU1 = ");
//		System.out.print(q.IU1);
//		System.out.print(", IU2 = ");
//		System.out.print(q.IU2);
//
//		System.out.print(", NL1 = ");
//		System.out.print(q.NL1);
//		System.out.print(", NL2 = ");
//		System.out.print(q.NL2);
//		System.out.print(", NU1 = ");
//		System.out.print(q.NU1);
//		System.out.print(", NU2 = ");
//		System.out.print(q.NU2);

		System.out.print(", CLL1 = ");
		System.out.print(q.CLL1 * factor_escala);
		System.out.print(", CLL2 = ");
		System.out.print(q.CLL2 * factor_escala);
		System.out.print(", CLU1 = ");
		System.out.print(q.CLU1 * factor_escala);
		System.out.print(", CLU2 = ");
		System.out.println(q.CLU2 * factor_escala);
		
		//q.HE(histogram_ac, ip);
		q.HE2(q.histo, ip);
		//ImagePlus hi_ec = new ImagePlus("Ecualizado " + imTitle, ip);
		//hi_ec.show();
				
		// muestra el histograma
		
		ImageProcessor hip = new ByteProcessor(w_histogram, h_histogram);
		hip.setValue(255);
		hip.fill();
				
		for (int x = 0; x < w_histogram; ++x) {

			for (int y = 0; y < q.histo[x] * factor_escala; ++y) {
				
				hip.putPixel(x, h_histogram - y, 0);
				/*
				if (y >= q.CLL1 * factor_escala)
					for (int xx = 0; xx <= q.SPL; xx++)
						hip.putPixel(xx, y, 0);
				if (y >= q.CLL2 * factor_escala)
					for (int xx = q.SPL + 1; xx <= q.SP; xx++)
						hip.putPixel(xx, y, 0);
				if (y >= q.CLU1 * factor_escala)
					for (int xx = q.SP + 1; xx <= q.SPU; xx++)
						hip.putPixel(xx, y, 0);
				if (y >= q.CLU2 * factor_escala)
					for (int xx = q.SPU + 1; xx <= q.LEVELS - 1; xx++)
						hip.putPixel(xx, y, 0);
*/
			}
			
			if (x == q.SPL)
				for (int y = 0; y < h_histogram; y++)
					hip.putPixel(x, y, 0);
			if (x == q.SP)
				for (int y = 0; y < h_histogram; y++)
					hip.putPixel(x, y, 0);
			if (x == q.SPU)
				for (int y = 0; y < h_histogram; y++)
					hip.putPixel(x, y, 0);
		}
		
		ImagePlus him = new ImagePlus("Histograma de " + imTitle, hip);
		him.show();
		
		ImageProcessor hip_e = new ByteProcessor(w_histogram, h_histogram);
		hip_e.setValue(255);
		hip_e.fill();

		int [] histo_eq = new int[q.LEVELS];
		histo_eq = q.getHE(q.histo, q.m_histo, q.n_histo); 
		
		for (int x = 0; x < w_histogram; ++x) {

			for (int y = 0; y < histo_eq[x] * factor_escala; ++y) {
				
				hip_e.putPixel(x, h_histogram - y, 0);
				
			}
		}
		
		ImagePlus he = new ImagePlus("HE de " + imTitle, hip_e);
		he.show();
		
		// muestra el histograma acumulativo
		
		ImageProcessor hip_ac = new ByteProcessor(w_histogram, h_histogram);
		hip_ac.setValue(255);
		hip_ac.fill();
		
		for (int x = 0; x < w_histogram; ++x)
			for (int y = 0; y < histogram_ac[x] * h_histogram / max_histogram_ac; ++y)
				hip_ac.putPixel(x, h_histogram - y, 0);

		ImagePlus him_ac = new ImagePlus("Histograma ac. de " + imTitle, hip_ac);
		him_ac.show();
	}

}
