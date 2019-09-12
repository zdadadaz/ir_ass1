package infs7410.query;

import org.terrier.querying.IndexRef;
import org.terrier.querying.Manager;
import org.terrier.querying.ManagerFactory;
import org.terrier.structures.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IDFr {
    public String IDFr_reduce(String query, int K, IndexRef ref) throws IOException {
        String[] terms = query.split(" +");

        Index index = IndexFactory.of(ref);
        PostingIndex inverted = index.getInvertedIndex();
        Lexicon<String> lexicon = index.getLexicon();
        double N = index.getCollectionStatistics().getNumberOfDocuments();

        List<IDFReduction.Pair> scoredTerms = new ArrayList<>(terms.length);

        // Run a search request using the original query.
        Manager queryManager = ManagerFactory.from(ref);
        for (String term : terms) {
            if(term.equals("")){
                continue;
            }

            LexiconEntry entry = lexicon.getLexiconEntry(term);
            if (entry == null) {
                scoredTerms.add(new IDFReduction.Pair(term, 0));
                continue;
            }

            double docFreq = entry.getDocumentFrequency();

            double idf = Math.log(N / (docFreq + 1));
            scoredTerms.add(new IDFReduction.Pair(term, idf));
        }
        Collections.sort(scoredTerms);
        Collections.reverse(scoredTerms);

        StringBuilder sb = new StringBuilder();

//      The proportion of r remain in the list
        int numberLeftTerm = (int) Math.ceil(scoredTerms.size()*K);
        for (int i = 0; i < numberLeftTerm && i < scoredTerms.size(); i++) {
            sb.append(scoredTerms.get(i).term).append(" ");
        }
        return sb.toString();
    }

}
