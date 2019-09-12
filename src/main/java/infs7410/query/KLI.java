package infs7410.query;

import infs7410.project1.TrecResult;
import infs7410.project1.TrecResults;
import org.terrier.querying.IndexRef;
import org.terrier.querying.Manager;
import org.terrier.querying.ManagerFactory;
import org.terrier.structures.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import infs7410.ranking.KLI_rank;
import org.terrier.matching.models.queryexpansion.KL;
import org.terrier.querying.ExpansionTerms.ExpansionTerm;
import org.terrier.querying.ExpansionTerms.ExpansionTerm;
import org.terrier.structures.postings.IterablePosting;

public class KLI {
    HashSet<String> docSet = new HashSet<>();
    TrecResults initDoc;
    HashSet<String> docIdSet;
    public KLI(String resPath) throws IOException {
        this.docIdSet = new HashSet<String>();
        this.initDoc = new TrecResults(resPath);
        for (TrecResult result : this.initDoc.getTrecResults()) {
            this.docIdSet.add(result.getDocID());
        }

    }

    public String KLI_reduce(String query, int K, IndexRef ref) throws IOException {
        KLI_rank wm = new KLI_rank();
        String[] terms = query.split(" +");
        Index index = IndexFactory.of(ref);
        MetaIndex meta = index.getMetaIndex();
        PostingIndex invertedIndex = index.getInvertedIndex();
        Lexicon<String> lexicon = index.getLexicon();
        double N = index.getCollectionStatistics().getNumberOfDocuments();
        long collectlength = index.getCollectionStatistics().getNumberOfTokens(); // how to get collection length ???

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
            wm.setEntryStatistics(entry.getWritableEntryStatistics());
            wm.setKeyFrequency(0);
            // Prepare the weighting model for scoring.
            wm.prepare();
            IterablePosting ip = invertedIndex.getPostings(entry);
            double withindocTF = 0; // get initial intrieved document TF??
            double withindoclength = 0; // how to get initial intrieved document length??
            while (ip.next() != IterablePosting.EOL) {
                String docId = meta.getItem("docno", ip.getId());
                if (docIdSet.contains(docId)) {
                    withindocTF += wm.score(ip);
                    withindoclength += wm.getDoclength();
                }
            }
            
            double docFreq = entry.getDocumentFrequency();
            double collectionTF = entry.getFrequency();
            double ptD = withindocTF/withindoclength;
            double ptC = collectionTF/collectlength;

            double kli = ptD * Math.log(ptD / ptC);
            scoredTerms.add(new IDFReduction.Pair(term, kli));
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
