package infs7410.ranking;

import org.terrier.matching.models.WeightingModel;
import org.terrier.matching.models.WeightingModelLibrary;

public class BM25_rsj extends WeightingModel {
    /** The constant k_1.*/
    private double k_1 = 1.2d;

    /** The constant k_3.*/
    private double k_3 = 8d;

    /** The parameter b.*/
    private double b;

    private double ri;

    private double R;

    /** A default constructor.*/
    public BM25_rsj() {
        super();
        b=0.75d;
    }
    public final String getInfo() {
        return "BM25 with RSJ"+b;
    }

    public void set_R_ri(double R,double ri){
        this.R = R;
        this.ri = ri;
    }

    public double score(double tf, double docLength) {
        final double K = k_1 * ((1 - b) + b * docLength / averageDocumentLength);
        return WeightingModelLibrary.log((numberOfDocuments - documentFrequency - this.R + this.ri + 0.5d) / (documentFrequency - this.ri + 0.5d) * (this.ri + 0.5d)/(this.R - this.ri +0.5d)) *
                ((k_1 + 1d) * tf / (K + tf)) *
                ((k_3+1)*keyFrequency/(k_3+keyFrequency));
    }

    public void setParameter(double _b) {
        this.b = _b;
    }

    public double getParameter() {
        return this.b;
    }


}
