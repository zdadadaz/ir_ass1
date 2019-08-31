package infs7410.ranking;

import org.terrier.matching.models.WeightingModel;

public class BM25 extends WeightingModel {

    private double k1 = 1.2;
    private double b = 0.75;

    public BM25() {
        super();
    }
    public void setParameter(double _b) {
        this.b = _b;
    }
    public void setParameter2(double _k) {
        this.k1 = _k;
    }

    @Override
    public String getInfo() {
        return "BM25";
    }

    @Override
    public double score(double tf, double docLength) {
        double idf = Math.log(numberOfDocuments / (documentFrequency + 1));
        return idf*tf*(this.k1+1)/(tf + this.k1 * (1 - this.b + this.b * docLength/averageDocumentLength));
    }
}
//    double K = this.k_1 * (1.0D - this.b + this.b * docLength / this.averageDocumentLength);
//        return WeightingModelLibrary.log((this.numberOfDocuments - this.documentFrequency + 0.5D) / (this.documentFrequency + 0.5D)) * ((this.k_1 + 1.0D) * tf / (K + tf)) * ((this.k_3 + 1.0D) * this.keyFrequency / (this.k_3 + this.keyFrequency));
