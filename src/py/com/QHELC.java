package py.com;


/*
 * Prueba del algoritmo QHELC
 * Autor: Federico Gaona
 * 
 */

import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

public class QHELC implements PlugInFilter {
	
	public final int LEVELS = 256;
	public int [] histo = new int[LEVELS];
	public int m_histo = 0, n_histo = 0;
	public int SP, SPL, SPU;
	public int IL1, IL2, IU1, IU2;
	public int NL1=0, NL2=0, NU1=0, NU2=0;
	public int CLL1, CLL2, CLU1, CLU2;
	public int TL1=0, TL2=0, TU1=0, TU2=0;
	public float gama = 0.0f;
	public int AIL1, AIL2, AIU1, AIU2;

	public void run(ImageProcessor ip) {
		
		histo = getHistogram(ip);
		m_histo = ip.getWidth();
		n_histo = ip.getHeight();
		
		SP = getSP(histo, m_histo, n_histo, 0, LEVELS - 1);
		SPL = getSP(histo, m_histo, n_histo, 0, SP);
		SPU = SP + 1 + getSP(histo, m_histo, n_histo, SP + 1, LEVELS - 1);
		
		IL1 = SPL + 1;
		IL2 = SP - SPL;
		IU1 = SPU - SP;
		IU2 = LEVELS - 1 - SPU;
		
		for (int i = 0; i <= SPL; i++)
			NL1 += histo[i];

		for (int i = SPL + 1; i <= SP; i++)
			NL2 += histo[i];

		for (int i = SP + 1; i <= SPU; i++)
			NU1 += histo[i];

		for (int i = SPU + 1; i <= LEVELS - 1; i++)
			NU2 += histo[i];
		
		CLL1 = Math.round((float)(NL1) / (float)(IL1)) + Math.round(gama * ((float)(NL1) - (float)(NL1) / (float)(IL1)));
		CLL2 = Math.round((float)(NL2) / (float)(IL2)) + Math.round(gama * ((float)(NL2) - (float)(NL2) / (float)(IL2)));
		CLU1 = Math.round((float)(NU1) / (float)(IU1)) + Math.round(gama * ((float)(NU1) - (float)(NU1) / (float)(IU1)));
		CLU2 = Math.round((float)(NU2) / (float)(IU2)) + Math.round(gama * ((float)(NU2) - (float)(NU2) / (float)(IU2)));
		
		for (int i = 0; i <= SPL; i++) {
			int v = histo[i] - CLL1;
			if (v > 0)
				TL1 += v;
		}

		for (int i = SPL + 1; i <= SP; i++) {
			int v = histo[i] - CLL2;
			if (v > 0)
				TL2 += v;
		}

		for (int i = SP + 1; i <= SPU; i++) {
			int v = histo[i] - CLU1;
			if (v > 0)
				TU1 += v;
		}

		for (int i = SPU + 1; i <= LEVELS - 1; i++) {
			int v = histo[i] - CLU2;
			if (v > 0)
				TU2 += v;
		}
		
		for (int i = 0; i < ip.getWidth(); i++) {
			for (int j = 0; j < ip.getHeight(); j++) {
				ip.putPixel(i, j, 0);
			}
		}
		
		AIL1 = Math.round((float)(TL1) / (float)(IL1));
		AIL2 = Math.round((float)(TL2) / (float)(IL2));
		AIU1 = Math.round((float)(TU1) / (float)(IU1));
		AIU2 = Math.round((float)(TU2) / (float)(IU2));
	}

	public int setup(String args, ImagePlus im) {
		return 0;
	}
	
	public int [] getACHistogram(ImageProcessor ip) {
		
		int [] histogram = new int[LEVELS];	
		int [] histogram_ac = new int[LEVELS];
		
		histogram = getHistogram(ip);
		
		// frecuencia acumulativa y su valor maximo
		histogram_ac[0] = histogram[0];
		
		for (int i = 1; i < LEVELS; ++i)
			histogram_ac[i] = histogram_ac[i-1] + histogram[i];
		
		return histogram_ac;
	}
	
	public int getMaxHistogram(int [] h) {
		int max = 0;
		for (int i = 0; i < h.length; i++)
			if (h[i] > max)
				max = h[i];
		return max;
	}
	
	public int [] getHistogram(ImageProcessor ip) {
		
		int [] h = new int[LEVELS];
		int m = ip.getWidth();
		int n = ip.getHeight();
		
		for (int x = 0; x < m; ++x)
			for (int y = 0; y < n; ++y) {
				
				int i = ip.getPixel(x, y);
				
				h[i] ++;
						
			}
		
		return h;
	}

	private float getQProbability(int [] h, int q, int m, int n) {
		float p = 0;
		try {
			p = (float)(h[q]) / (float)(m*n);
		} catch (Exception e) {
			p = 0;
		}
		return p;
	}
	
	public int getSP(int [] h, int m, int n, int lo, int up) {
		int sp = 0;
		for (int q = lo; q <= up; q++) {
			sp += (int)(getQProbability(h, q, m, n) * (float)(q));
		}
		return sp;
	}
	
	public int getX0(int [] h) {
		int x0 = 0;
		for (int i = 0; i < h.length; i++)
			if (h[i] != 0) {
				x0 = i;
				break;
			}
		return x0;
	}
	
	public int getXL(int [] h) {
		int xL = 0;
		for (int i = h.length - 1; i > 0; i--)
			if (h[i] != 0) {
				xL = i;
				break;
			}
		return xL;
	}
	
	// cumulative density
	public float cd(int [] h, int q, int m, int n) {
		float cq = 0;
		for (int i = getX0(h); i <= q; i++) {
			cq += getQProbability(h, i, m, n);
		}
		
//		if (cq > 0.0) {
//			System.out.print("q = ");
//			System.out.print(q);
//		System.out.print("cq = ");
//		System.out.println(cq);
//		}
		
		return cq;
	}

	public float fq(int [] h, int q, int m, int n) {
		//System.out.println("fq");
		float fq = 0;
		int x0 = getX0(h);
		int xL_1 = getXL(h) - 1;
		float _cd = cd(h, q, m, n);
//		System.out.print("q = ");
//		System.out.print(q);
//		System.out.print(" cd = ");
//		System.out.println(_cd);
		fq = (float)(x0) + ((float)(xL_1 - x0) * _cd);
		return fq;
	}
	
	public int [] getHE(int [] h, int m, int n) {
		//System.out.println("getHE");
		int [] he = new int[h.length];
		
		//he[0] = fq(h, 100, m, n);
		
		
		for (int i = 0; i < h.length; i++) {
			float _fq = fq(h, i, m, n);
			he[i] = (int)(_fq);
			System.out.print("i = ");
			System.out.print(i);
			System.out.print(" fq = ");
			System.out.print(_fq);
			System.out.print(" he[i] = ");
			System.out.println(he[i]);
		}
		
		return he;
	}

	public void HE(int [] h_ac, ImageProcessor ip) {
		for (int v = 0; v < ip.getHeight(); v++) {
			for (int u = 0; u < ip.getWidth(); u++) {
				int a = ip.get(u, v);
				int b = h_ac[a] * (LEVELS - 1) / (ip.getWidth() * ip.getHeight());
				ip.set(u, v, b);
			}
		}
	}

	public void HE2(int [] h, ImageProcessor ip) {
		for (int v = 0; v < ip.getHeight(); v++) {
			for (int u = 0; u < ip.getWidth(); u++) {
				int a = ip.get(u, v);
				int b = (int)(fq(h, a, ip.getWidth(), ip.getHeight()));
				ip.set(u, v, b);
			}
		}
	}

	
}
