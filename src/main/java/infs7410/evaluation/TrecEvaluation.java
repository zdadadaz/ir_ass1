package infs7410.evaluation;

import java.io.*;
import java.util.*;

public class TrecEvaluation {

    private HashMap<String, HashMap<String, Double>> topics;
    private Set<String> measures;

    /**
     * @param filename The filename of the evaluation file.
     * @throws IOException An Exception is thrown if the file does not exist.
     */
    public TrecEvaluation(String filename) throws IOException {
        this.measures = new HashSet<>();
        this.topics = new HashMap<>();

        InputStream is = new FileInputStream(filename);
        BufferedReader buf = new BufferedReader((new InputStreamReader(is)));

//        System.out.println(filename);

        String line = buf.readLine();
        while (line != null) {
            Line l = splitLine(line);
            if (l.getTopic().equals("all")) {
                line = buf.readLine();
                continue;
            }

            measures.add(l.getMeasure());

            if (!topics.containsKey(l.getTopic())) {
                topics.put(l.getTopic(), new HashMap<>());
            }

            topics.get(l.getTopic()).put(l.getMeasure(), l.getScore());
            line = buf.readLine();
        }
    }

    /**
     * getScoresForMeasure creates an array of scores that can be later used for statistical significance testing.
     *
     * @param measure The evaluation measure to find scores for.
     * @return An array of scores.
     * @throws Exception An exception is thrown if the measure is not found.
     */
    public double[] getScoresForMeasure(String measure) throws Exception {
        List<Double> scores = new ArrayList<>();
        for (String topic : topics.keySet()) {
            if (!topics.get(topic).containsKey(measure)) {
                throw new Exception("measure not found in eval file");
            }
            scores.add(topics.get(topic).get(measure));
        }

        double[] doubles = new double[scores.size()];
        for (int i = 0; i < scores.size(); i++) {
            doubles[i] = scores.get(i);
        }

        return doubles;
    }

    /**
     * @return A list of the topics in the eval file.
     */
    public Set<String> getToics() {
        return topics.keySet();
    }

    /**
     * @return A list of measures in the eval file.
     */
    public Set<String> getMeasures() {
        return measures;
    }

    private Line splitLine(String input) {
        String[] parts = input.split("\\s+");
        Double score = Double.valueOf(parts[2].trim());
        return new Line(parts[0], parts[1], score);
    }

    private class Line {
        private String measure;
        private String topic;
        private Double score;

        Line(String measure, String topic, Double score) {
            this.measure = measure;
            this.topic = topic;
            this.score = score;
        }

        String getMeasure() {
            return measure;
        }

        String getTopic() {
            return topic;
        }

        Double getScore() {
            return score;
        }
    }


}
