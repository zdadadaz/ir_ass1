package infs7410.ranking;

import org.terrier.matching.models.WeightingModel;

public class TF_IDF extends WeightingModel {

    public TF_IDF() {
        super();
    }

    @Override
    public String getInfo() {
        return "tf-idf";
    }

    @Override
    public double score(double tf, double docLength) {
        return tf*Math.log(numberOfDocuments / (documentFrequency + 1));
    }
}
