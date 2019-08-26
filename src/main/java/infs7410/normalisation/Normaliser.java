package infs7410.normalisation;

import infs7410.project1.TrecResult;
import infs7410.project1.TrecResults;

import java.util.List;

public interface Normaliser {
    void init(TrecResults items);

    double normalise(TrecResult result);
}
