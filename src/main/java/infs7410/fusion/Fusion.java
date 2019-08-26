package infs7410.fusion;

import infs7410.project1.TrecResult;
import infs7410.project1.TrecResults;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public abstract class Fusion {
    public abstract TrecResults Fuse(List<TrecResults> resultsLists);

    protected TrecResults flatten(HashMap<String, TrecResult> seen) {
        List<TrecResult> unique = new ArrayList<>(seen.size());
        unique.addAll(seen.values());
        Collections.sort(unique);
        for (int i = 0; i < unique.size(); i++) {
            unique.get(i).setRank(i);
        }
        return new TrecResults(unique);
    }
}
