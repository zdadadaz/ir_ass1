package infs7410.project1;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TrecResults {

    private List<TrecResult> trecResults;

    public TrecResults() {
        this.trecResults = new ArrayList<>();
    }

    public TrecResults(String filename) throws IOException {
        this.trecResults = new ArrayList<>();

        InputStream is = new FileInputStream(filename);
        BufferedReader buf = new BufferedReader((new InputStreamReader(is)));

        String line = buf.readLine();
        while (line != null) {
            TrecResult t = splitLine(line);
            this.trecResults.add(t);
            line = buf.readLine();
        }
    }

    public TrecResults(List<TrecResult> results) {
        this.trecResults = results;
    }

    public List<TrecResult> getTrecResults() {
        return trecResults;
    }

    public List<TrecResult> getTrecResults(String topic) {
        List<TrecResult> resultsForTopic = new ArrayList<>();
        for (TrecResult t : trecResults) {
            if (t.getTopic().equals(topic)) {
                resultsForTopic.add(t);
            }
        }
        return resultsForTopic;
    }

    public int size() {
        return trecResults.size();
    }

    public TrecResult get(int i) {
        return trecResults.get(i);
    }

    public void setRunName(String name) {
        for (TrecResult result : trecResults) {
            result.setRunName(name);
        }
    }

    public Set<String> getTopics() {
        Set<String> topics = new HashSet<>();
        for (TrecResult result : trecResults) {
            topics.add(result.getTopic());
        }
        return topics;
    }

    public void write(String filename) throws IOException {
        OutputStream os = new FileOutputStream(filename,true);
        for (TrecResult result : trecResults) {
            os.write(String.format("%s\n", result.toString()).getBytes());
        }
        os.flush();
        os.close();
    }

    private TrecResult splitLine(String line) {
        String[] parts = line.split("\\s+");
        int rank = Integer.valueOf(parts[3]);
        double score = Double.valueOf(parts[4]);
        return new TrecResult(parts[0], parts[2], rank, score, parts[5]);
    }
}
