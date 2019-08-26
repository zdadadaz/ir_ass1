package infs7410.ranking;

import org.terrier.matching.models.WeightingModel;

public class InverseDocumentFrequency extends WeightingModel {

    public InverseDocumentFrequency() {
        super();
    }

    @Override
    public String getInfo() {
        return "df";
    }

    @Override
    public double score(double tf, double docLength) {
        return Math.log(numberOfDocuments / (documentFrequency + 1));
    }
}
