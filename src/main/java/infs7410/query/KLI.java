package infs7410.query;

import infs7410.project1.Project1;
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
import org.terrier.structures.postings.Posting;

public class KLI {
    private HashSet<String> docSet = new HashSet<>();
    private ArrayList<String> initDoc;
    private Integer resNum;
    private double withindoclength;

    public KLI(ArrayList<String>  docIds) {
        this.initDoc = docIds;
        this.resNum = initDoc.size();
        this.withindoclength = 0;
        for (String s: docIds){
            docSet.add(s);
        }

    }
    // public void readWithinDoclength(Index index) throws IOException {
    //     DocumentIndex documentIndex = index.getDocumentIndex();
    //     this.withindoclength = 0;
    //     for ( String docno : this.initDoc){
    //         //if docno doesn't exist return -40000
    //         int docId = Project1.docNoToDocId.getOrDefault(docno, -4000);
    //         if (docId != -4000) {
    //             this.withindoclength += documentIndex.getDocumentLength(docId);
    //         }

    //     }
    // }

    public String KLI_reduce(String query, double K, IndexRef ref) throws Exception {
        KLI_rank wm = new KLI_rank();
        String[] terms = query.split(" +");
        Index index = IndexFactory.of(ref);
        MetaIndex meta = index.getMetaIndex();
        PostingIndex invertedIndex = index.getInvertedIndex();
        Lexicon<String> lexicon = index.getLexicon();
        double N = index.getCollectionStatistics().getNumberOfDocuments();
        long collectlength = index.getCollectionStatistics().getNumberOfTokens(); // how to get collection length ???
        List<IDFReduction.Pair> scoredTerms = new ArrayList<>(terms.length);
        // this.readWithinDoclength(index);

        DocumentIndex documentIndex = index.getDocumentIndex();
        wm.setCollectionStatistics(index.getCollectionStatistics());

        // Run a search request using the original query.
        //Manager queryManager = ManagerFactory.from(ref);
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
            wm.setKeyFrequency(0.0);
            // Prepare the weighting model for scoring.
            wm.prepare();
            IterablePosting ip = invertedIndex.getPostings(entry);
            double withindocTF = 0;
            while (ip.next() != IterablePosting.EOL) {
                String docId = meta.getItem("docno", ip.getId());
                if (this.docSet.contains(docId)) {
                    withindocTF += wm.score(ip);
                    this.withindoclength += ip.getDocumentLength();
                }
            }

            double collectionTF = entry.getFrequency();
            double ptD = withindocTF/this.withindoclength;
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
