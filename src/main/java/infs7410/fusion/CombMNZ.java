package infs7410.fusion;

import infs7410.project1.TrecResult;
import infs7410.project1.TrecResults;

import java.util.HashMap;
import java.util.List;


public class CombMNZ extends Fusion {
    @Override
    public TrecResults Fuse(List<TrecResults> resultsLists) {
        HashMap<String, TrecResult> seen = new HashMap<>();
        HashMap<String, Integer> counts = new HashMap<>();

        for (TrecResults trecResults : resultsLists) {
            for (TrecResult result : trecResults.getTrecResults()) {
                if (!seen.containsKey(result.getDocID())) {
                    seen.put(result.getDocID(), result);
                    counts.put(result.getDocID(), 1);
                } else {
                    double score = result.getScore();
                    result.setScore(result.getScore() + score);
                    seen.put(result.getDocID(), result);
                    if (score > 0){
                        counts.put(result.getDocID(), counts.get(result.getDocID())+1);
                    }
                }
            }
        }
        for (TrecResults trecResults : resultsLists) {
            for (TrecResult result : trecResults.getTrecResults()) {
                    result.setScore(result.getScore()*counts.get(result.getDocID()));
                    seen.put(result.getDocID(), result);
            }
        }

        return flatten(seen);
    }

}
