package infs7410.ranking;

import org.terrier.matching.models.WeightingModel;

public class TermFrequency extends WeightingModel {

    public TermFrequency() {
        super();
    }

    @Override
    public String getInfo() {
        return "tf";
    }

    @Override
    public double score(double tf, double docLength) {
        return tf;
    }
}
