package infs7410.fusion;

import infs7410.project1.TrecResult;
import infs7410.project1.TrecResults;

import java.util.HashMap;
import java.util.List;

public class CombSUM extends Fusion {
    @Override
    public TrecResults Fuse(List<TrecResults> resultsLists) {
        HashMap<String, TrecResult> seen = new HashMap<>();

        for (TrecResults trecResults : resultsLists) {
            for (TrecResult result : trecResults.getTrecResults()) {
                if (!seen.containsKey(result.getDocID())) {
                    seen.put(result.getDocID(), result);
                } else {
                    double score = result.getScore(); // TODO: IMPLEMENT ME.
                    result.setScore(result.getScore() + score);
                    seen.put(result.getDocID(), result);
                }
            }
        }

        return flatten(seen);
    }
}
