package infs7410.fusion;
import infs7410.project1.TrecResults;

import infs7410.normalisation.Normaliser;
import infs7410.normalisation.MinMax;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
/**
 * Fusion_run - Run fusion algorithm
 * @author Chien-chi chen
 */
public class Fusion_run {
    /**
     * Filename list for fusion
     */
    private List<String> filenames;
    /**
     * Fusion result list
     */
    private List<TrecResults> results;
    /**
     * Add filename list into private variable
     *
     * @param resultFilenames Input query path
     * @require {@code resultFilenames != null}
     */
    public Fusion_run(List<String> resultFilenames) throws IOException {
        filenames = new ArrayList<String>(resultFilenames);
        results = new ArrayList<TrecResults>(resultFilenames.size());
        for (String filename : resultFilenames) {
//            System.out.println(filename);
            results.add(new TrecResults(filename));
        }
    }
    /**
     * Do fusion algorithm according to Algorithm
     *
     * @param algorithm Input query path
     * @param normFlag True or False for normilization
     * @param outName output path for fusion result
     * @require {@code algorithm != null, normFlag != null,outName != null}
     */
    public void Fusion_do(String algorithm, Boolean normFlag, String outName) throws IOException {

            Normaliser norm = new MinMax();
            Fusion fusion;
            switch(algorithm) {
                case "combsum":
                    fusion = new CombSUM();
                    break;
                case "combmnz":
                    fusion = new CombMNZ();
                    break;
                default:
                    fusion = new Borda();
            }

            if (normFlag) {
                for (TrecResults trecResults : results) {
                    norm.init(trecResults);
                    for (int j = 0; j < trecResults.getTrecResults().size(); j++) {
                        double normScore = norm.normalise(trecResults.getTrecResults().get(j));
                        trecResults.getTrecResults().get(j).setScore(normScore);
                    }
                }
            }
            Set<String> topics = results.get(0).getTopics();
            TrecResults fusedResults = new TrecResults();
            for (String topic : topics) {
//                logger.info(topic);
                List<TrecResults> topicResults = new ArrayList<>();
                for (TrecResults r : results) {
                    topicResults.add(new TrecResults(r.getTrecResults(topic)));
                }

                // Fuse the results together and write the new results list to disk.
                fusedResults.getTrecResults().addAll(fusion.Fuse(topicResults).getTrecResults());
            }
            fusedResults.setRunName(algorithm);
            fusedResults.write_noAppend(outName);
    }
}
