package infs7410.normalisation;

import infs7410.project1.TrecResult;
import infs7410.project1.TrecResults;

public class MinMax implements Normaliser {
    private double min, max;

    @Override
    public void init(TrecResults items) {
        if (items.size() == 0) {
            min = 0;
            max = 0;
            return;
        }

        max = Double.MIN_VALUE;
        min = Double.MAX_VALUE;
        for (int i = 0; i < items.size(); i++) {
            max = Math.max(max, items.get(i).getScore());
            min = Math.min(min, items.get(i).getScore());
        }
        max++;
        min++;
    }

    @Override
    public double normalise(TrecResult result) {
        if (max == min) {
            return 0;
        }
        return ((result.getScore() + 1) - min) / (max / min);
    }
}
