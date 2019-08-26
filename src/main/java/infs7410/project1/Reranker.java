package infs7410.project1;

import org.terrier.matching.models.WeightingModel;
import org.terrier.structures.*;
import org.terrier.structures.postings.IterablePosting;

import java.io.IOException;
import java.util.*;

public class Reranker {

    private Index index;

    public Reranker(Index index) {
        this.index = index;
    }

    public TrecResults rerank(String topic, ArrayList<String>  queryTerms, ArrayList<String>  docIds, WeightingModel wm) throws IOException {
        Lexicon lex = index.getLexicon();
        PostingIndex invertedIndex = index.getInvertedIndex();
        MetaIndex meta = index.getMetaIndex();
        wm.setCollectionStatistics(index.getCollectionStatistics());
        HashSet<String> docIdSet = new HashSet<String>(docIds);

        HashMap<String, Double> scores = new HashMap<>();

        // Iterate over all query terms.
        for (String queryTerm : queryTerms) {
//            System.out.println(queryTerm);
            // Get the lexicon entry for the term.
            LexiconEntry entry = lex.getLexiconEntry(queryTerm);
            if (entry == null) {
                continue; // This term is not in the index, go to next document.
            }

            // Obtain entry statistics.
            wm.setEntryStatistics(entry.getWritableEntryStatistics());

            // Set the number of times the query term appears in the query.
            double kf = 0.0;
            for (String otherTerm : queryTerms) {
                if (otherTerm.equals(queryTerm)) {
                    kf++;
                }
            }
            wm.setKeyFrequency(kf);

            // Prepare the weighting model for scoring.
            wm.prepare();


            IterablePosting ip = invertedIndex.getPostings(entry);
            double score = 0.0;

            while (ip.next() != IterablePosting.EOL) {
                String docId = meta.getItem("docno", ip.getId());
                if (docIdSet.contains(docId)) {
                    score = wm.score(ip);
                    if (!scores.containsKey(docId)) {
                        scores.put(docId, score);
                    } else {
                        scores.put(docId, scores.get(docId) + score);
                    }
                }
            }

        }
        // Set score to 0 for docs that do not contain any term.
        for (String id : docIdSet) {
            if (!scores.containsKey(id)) {
                scores.put(id, 0.0);
            }
        }


        // Create a results list from the scored documents.
        TrecResults results = new TrecResults();
        for (Map.Entry<String, Double> entry : scores.entrySet()) {
            results.getTrecResults().add(new TrecResult(
                    topic,
                    entry.getKey(),
                    0,
                    entry.getValue(),
                    null
            ));
        }

        // Sort the documents by the score assigned by the weighting model.
        Collections.sort(results.getTrecResults());
        Collections.reverse(results.getTrecResults());

        // Assign the rank to the documents.
        for (int i = 0; i < results.getTrecResults().size(); i++) {
            results.getTrecResults().get(i).setRank(i + 1);
        }

        return results;
    }
}
