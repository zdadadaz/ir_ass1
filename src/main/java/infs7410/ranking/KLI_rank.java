package infs7410.ranking;

import org.terrier.matching.models.WeightingModel;

public class KLI_rank extends WeightingModel {
    private double doclength = 0;

    public KLI_rank() {
        super();
    }

    public double getDoclength() {
        return this.doclength;
    }

    @Override
    public String getInfo() {
        return "KLI";
    }

    @Override
    public double score(double tf, double docLength) {
        this.doclength = docLength;
        return tf;
    }
}
