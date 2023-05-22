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
	
	public final int K = 256;
	public int [] histogram = new int[K];
	public int [] chistogram = new int[K];		// cumulative histogram
	public int M = 0, N = 0;
	
	public float [] sqrt_cH = new float[K];		// modified cumulative histogram
	public float max_sqrt_cH = 0;
	
	ImageProcessor ip;
	
	public int SP, SPL, SPU;
	public int IL1, IL2, IU1, IU2;
	public int NL1=0, NL2=0, NU1=0, NU2=0;
	public int CLL1, CLL2, CLU1, CLU2;
	public int TL1=0, TL2=0, TU1=0, TU2=0;
	public float gama = 0.0f;
	public int AIL1, AIL2, AIU1, AIU2;

	int [] h1 = new int[K];
	int [] h2 = new int[K];
	int [] h3 = new int[K];
	int [] h4 = new int[K];
	
	public void run(ImageProcessor ip) {
		
		setImageProcessor(ip);
		getHistogram();
		getCumulativeHistogram();
		
		SP = getSP(0, K - 1);
		SPL = getSP(0, SP);
		SPU = SP + 1 + getSP(SP + 1, K - 1);
		
		IL1 = SPL + 1;
		IL2 = SP - SPL;
		IU1 = SPU - SP;
		IU2 = K - 1 - SPU;
		
		for (int i = 0; i <= SPL; i++)
			NL1 += histogram[i];

		for (int i = SPL + 1; i <= SP; i++)
			NL2 += histogram[i];

		for (int i = SP + 1; i <= SPU; i++)
			NU1 += histogram[i];

		for (int i = SPU + 1; i <= K - 1; i++)
			NU2 += histogram[i];
		
		CLL1 = Math.round((float)(NL1) / (float)(IL1)) + Math.round(gama * ((float)(NL1) - (float)(NL1) / (float)(IL1)));
		CLL2 = Math.round((float)(NL2) / (float)(IL2)) + Math.round(gama * ((float)(NL2) - (float)(NL2) / (float)(IL2)));
		CLU1 = Math.round((float)(NU1) / (float)(IU1)) + Math.round(gama * ((float)(NU1) - (float)(NU1) / (float)(IU1)));
		CLU2 = Math.round((float)(NU2) / (float)(IU2)) + Math.round(gama * ((float)(NU2) - (float)(NU2) / (float)(IU2)));
		
		for (int i = 0; i <= SPL; i++) {
			int v = histogram[i] - CLL1;
			if (v > 0)
				TL1 += v;
		}

		for (int i = SPL + 1; i <= SP; i++) {
			int v = histogram[i] - CLL2;
			if (v > 0)
				TL2 += v;
		}

		for (int i = SP + 1; i <= SPU; i++) {
			int v = histogram[i] - CLU1;
			if (v > 0)
				TU1 += v;
		}

		for (int i = SPU + 1; i <= K - 1; i++) {
			int v = histogram[i] - CLU2;
			if (v > 0)
				TU2 += v;
		}
				
		AIL1 = (int)((float)(TL1) / (float)(IL1));
		AIL2 = (int)((float)(TL2) / (float)(IL2));
		AIU1 = (int)((float)(TU1) / (float)(IU1));
		AIU2 = (int)((float)(TU2) / (float)(IU2));
		
		// obtaining the 4 subhistograms
		for (int i = 0; i <= SPL; i++)
			if (histogram[i] > CLL1 - AIL1)
				h1[i] = CLL1;
			else
				h1[i] = histogram[i] + AIL1;

		for (int i = SPL + 1; i <= SP; i++)
			if (histogram[i] > CLL2 - AIL2)
				h2[i] = CLL2;
			else
				h2[i] = histogram[i] + AIL2;

		for (int i = SP + 1; i <= SPU; i++)
			if (histogram[i] > CLU1 - AIU1)
				h3[i] = CLU1;
			else
				h3[i] = histogram[i] + AIU1;
		
		for (int i = SPU + 1; i <= K - 1; i++)
			if (histogram[i] > CLU2 - AIU2)
				h4[i] = CLU2;
			else
				h4[i] = histogram[i] + AIU2;
		
		// joining the 4 subhistograms
		for (int i = 0; i < histogram.length; ++i) {
			
			if (i <= SPL)
				histogram[i] = h1[i];
			else if (i > SPL && i <= SP)
				histogram[i] = h2[i];
			else if (i > SP && i <= SPU)
				histogram[i] = h3[i];
			else
				histogram[i] = h4[i];
		}
		
		// equalizing
		QHE_gq();
	}

	public int setup(String args, ImagePlus im) {
		return 0;
	}
	
	public void setImageProcessor(ImageProcessor _ip) {
		M = _ip.getWidth();
		N = _ip.getHeight();
		ip = _ip;
	}
	
	public void getHistogram() {
		
		for (int x = 0; x < M; ++x)
			for (int y = 0; y < N; ++y) {
				
				int i = ip.getPixel(x, y);
								
				histogram[i] ++;
			}
	}
	
	public void getCumulativeHistogram() {
						
		chistogram[0] = histogram[0];

		for (int i = 1; i < histogram.length; ++i)
			chistogram[i] = chistogram[i-1] + histogram[i];
		
	}
			
	public float [] toNormalize(float [] h, float value) {
		
		float [] nh = new float [h.length];
		float max = 0;
		
		for (int i = 0; i < h.length; ++i)
			if (h[i] > max)
				max = h[i];
		
		for (int i = 0; i < h.length; ++i)
			nh[i] = (h[i] * value) / max;
		
		return nh;
	}
	
	public int getMaxHistogram() {
		
		int max = 0;
		
		for (int i = 0; i < histogram.length; ++i)
			if (histogram[i] > max)
				max = histogram[i];
		
		return max;
	}
	
	public int getMaxCHistogram() {
		
		int max = 0;
		
		for (int i = 0; i < chistogram.length; ++i)
			if (chistogram[i] > max)
				max = chistogram[i];
		
		return max;
	}
	
	// get the smallest pixel value
	public int get_a_lo() {
		
		int alo = 0;
		
		for (int i = 0; i < histogram.length; ++i)
			if (histogram[i] != 0) {
				alo = i;
				break;
			}
		
		return alo;
	}
	
	
	// get the highest pixel value
	public int get_a_hi() {
		
		int ahi = 0;
		
		for (int i = histogram.length - 1; i > 0; --i)
			if (histogram[i] != 0) {
				ahi = i;
				break;
			}
		
		return ahi;
	}
		
	// probability distribution or probability density function
	private float get_pdf(int q) {
		float p = 0;
		try {
			p = (float)(histogram[q]) / (float)(M * N);
		} catch (Exception e) {
			p = 0;
		}
		return p;
	}
	
	/** Histogram Equalization **/
	
	// 2016_Book_DigitalImageProcessing, pag 66
	public void HE() {
		
		for (int v = 0; v < N; ++v) {
			for (int u = 0; u < M; ++u) {
				
				int a = ip.get(u, v);
				int b = (int)((float)(chistogram[a]) * (float)(histogram.length - 1) / (float)(M * N));
				
				ip.set(u, v, b);
			}
		}	
	}
	
	// 2016_Book_DigitalImageProcessing, pag 66
	public void HE_ImageJ() {
				
		sqrt_cH[0] = (float)(histogram[0]);
		max_sqrt_cH = sqrt_cH[0];
		
		for (int i = 1; i < histogram.length; ++i) {
		
			sqrt_cH[i] = sqrt_cH[i-1] + (float)(Math.sqrt((double)(histogram[i])));
			
			if (sqrt_cH[i] > max_sqrt_cH)
				max_sqrt_cH = sqrt_cH[i];
			
		}
				
		float [] normalized_sqrt_cH = new float [sqrt_cH.length];
		
		normalized_sqrt_cH = toNormalize(sqrt_cH, (float)(getMaxCHistogram()));
				
		for (int v = 0; v < N; ++v) {
			for (int u = 0; u < M; ++u) {
				
				int a = ip.get(u, v);
				int b = (int)(normalized_sqrt_cH[a] * (float)(histogram.length - 1) / (float)(M * N));
				
				ip.set(u, v, b);
				
			}
		}
	}
	
	// cumulative density function. QHELC paper Ec. 3
	public float get_cdf(int q) {
		
		float cq = 0;
		
		for (int i = get_a_lo(); i <= q; ++i)
			cq += get_pdf(i);
	
		return cq;
	}
	
	// equalization function. QHELC paper Ec. 4
	public float fq(int q) {
		
		float fq = 0;
		
		int x0 = get_a_lo();
		int xL_1 = get_a_hi() - 1;
		float cq = get_cdf(q);
		
		fq = (float)(x0) + ((float)(xL_1 - x0) * cq);
		
		return fq;
	}
	
	// modified equalization function. QHELC paper Ec. 5
	public float gq(int q) {
		
		float gq = 0;
		
		int x0 = get_a_lo();
		int xL_1 = get_a_hi() - 1;
		float cq = get_cdf(q);
		float pq = get_pdf(q);
		
		gq = (float)(x0) + ((float)(xL_1 - x0) * (cq - 0.5f * pq));
		
		return gq;
	}
	
	public void QHE_fq() {
		
		for (int v = 0; v < N; ++v) {
			for (int u = 0; u < M; ++u) {
				
				int a = ip.get(u, v);
				int b = (int)(fq(a));
				
				ip.set(u, v, b);
			}
		}	
	}
	
	public void QHE_gq() {
		
		for (int v = 0; v < N; ++v) {
			for (int u = 0; u < M; ++u) {
				
				int a = ip.get(u, v);
				int b = (int)(gq(a));
				
				ip.set(u, v, b);
			}
		}	
	}
	
	// QHELC paper Ec. 7
	public int getSP(int lo, int up) {
		
		int sp = 0;
		
		for (int q = lo; q <= up; ++q)
			sp += (int)(get_pdf(q) * (float)(q));

		return sp;
	}
}
