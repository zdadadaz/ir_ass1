package infs7410.fusion;

import infs7410.project1.TrecResult;
import infs7410.project1.TrecResults;

import java.util.*;

public class Borda extends Fusion {
    @Override
    public TrecResults Fuse(List<TrecResults> resultsLists) {
        HashMap<String, TrecResult> seen = new HashMap<>();

        // Sum item scores from all the item lists where it is present.
        for (TrecResults results : resultsLists) {
            double n = results.size();
            for (int i = 0; i < results.size(); i++) {
                TrecResult result = results.get(i);

                // The score is relative to the number of results.
                double score = (n-result.getRank()+1)/n; //

                // If the seen map contains the docID, add the current score to the score computed above.
                if (seen.containsKey(result.getDocID())) {
                    seen.put(result.getDocID(), new TrecResult(result.getTopic(), result.getDocID(), 0, seen.get(result.getDocID()).getScore() + score, null));
                } else { // Otherwise, set the score to the one computed above.
                    seen.put(result.getDocID(), new TrecResult(result.getTopic(), result.getDocID(), 0, score, null));
                }
            }
        }

        // Convert the map to a list,
        // sort it by the new score, and
        // set the rank values.
        return flatten(seen);
    }
}
