package infs7410.project1;

import org.terrier.matching.models.WeightingModel;
import org.terrier.matching.models.basicmodel.In;
import org.terrier.structures.*;
import org.terrier.structures.postings.IterablePosting;

import infs7410.ranking.BM25_rsj;
import infs7410.project1.TrecResult;
import infs7410.project1.TrecResults;

import java.io.*;
import java.util.*;

public class relevanceFeedback {

    private Index index;

    private HashMap<String,HashMap<String,Integer>> qrels;

    public relevanceFeedback(Index index) {
        this.index = index;
    }

    public void readqrels(String filename) throws IOException {
        this.qrels = new HashMap<String,HashMap<String,Integer>>();
        InputStream is = new FileInputStream(filename);
        BufferedReader buf = new BufferedReader((new InputStreamReader(is)));

        String line = buf.readLine();
        while (line != null) {
            splitLine(line);
            line = buf.readLine();
        }
    }

    private void splitLine(String line) {
        String[] parts = line.split("\\s+");
        int rel = Integer.valueOf(parts[3]);

        // debug ////////////////////// 2018 qrel has no 1
//        if (rel ==1){
//            System.out.println("check rel == 1");
//        }
        if (this.qrels.containsKey(parts[0])){
            //  read existing doc_map
            HashMap<String,Integer> doc_map = qrels.get(parts[0]);
            doc_map.put(parts[2],rel);
            this.qrels.put(parts[0],doc_map);
        }
        else{
            //  create new doc_map
            HashMap<String,Integer> doc_map = new HashMap<String,Integer>();
            doc_map.put(parts[2],rel);
            this.qrels.put(parts[0],doc_map);
        }
    }

    public TrecResults runBM25_RSJ(String topic, ArrayList<String>  queryTerms, ArrayList<String>  docIds,TrecResults baseResult,BM25_rsj bm25_rsj) throws IOException {
        Lexicon lex = index.getLexicon();
        PostingIndex invertedIndex = index.getInvertedIndex();
        MetaIndex meta = index.getMetaIndex();
        bm25_rsj.setCollectionStatistics(index.getCollectionStatistics());
        HashSet<String> docIdSet = new HashSet<String>(docIds);

        //// calculate baseline result of R ////
        topic = topic.replace(" ","");
        HashMap<String, Integer> doc_map = baseResult.getDocByTopics(topic);
        String [] doc_arr = new String [doc_map.size()]; // rank from 1
        for (Map.Entry<String, Integer> entry : doc_map.entrySet()) {
            doc_arr[entry.getValue() - 1] = entry.getKey(); // rank - 1 = position
        }
        Integer [] doc_R = caculate_R(doc_arr, topic);

        //// calculate baseline result of ri.
        //// <term, ri>
        HashMap<String, HashSet<String>> rihash = calculate_ri( topic,queryTerms, lex, invertedIndex, doc_map, meta);

        // init
        TrecResults results = new TrecResults();
        TrecResults scores_new = baseResult;
        Set<String > include_doc = new HashSet<>();
        include_doc.add(doc_arr[0]);
        results.getTrecResults().add(scores_new.getTrecResults().get(0));
        String pre_doc = scores_new.getTrecResults().get(0).getDocID();
        int curIndx = 1;
        Integer R = 0;
        //// run baseline result and re-rank
        for (int i = 1; i<doc_arr.length; i++){
            // if R change then do re rank,
            if(isRelevance(topic,pre_doc)){
                R += 1;
                scores_new = reranker(topic, queryTerms, lex, invertedIndex, bm25_rsj, meta, docIdSet, doc_map, R,rihash, i,include_doc);
                curIndx = 0;
            }
            String cur_doc = scores_new.getTrecResults().get(curIndx).getDocID();
            // otherwise, use the old one to assign score and document
            results.getTrecResults().add(scores_new.getTrecResults().get(curIndx));
            curIndx += 1;
            pre_doc = cur_doc;
            include_doc.add(cur_doc);
        }

        // Assign the rank to the documents.
        for (int i = 0; i < results.getTrecResults().size(); i++) {
            results.getTrecResults().get(i).setRank(i + 1);
            results.getTrecResults().get(i).setRunName("bm25_rsj");
        }

        return results;
    }

    public TrecResults reranker(String topic,ArrayList<String>  queryTerms, Lexicon lex, PostingIndex invertedIndex,BM25_rsj bm25_rsj,MetaIndex meta,HashSet<String> docIdSet, HashMap<String, Integer> doc_map,Integer doc_R, HashMap<String, HashSet<String>> ri_hash,Integer rel_index,Set<String > include_doc) throws IOException {
        ////   reranker //////
        HashMap<String, Double> scores = new HashMap<>();
        // Iterate over all query terms.
        for (String queryTerm : queryTerms) {
            LexiconEntry entry = lex.getLexiconEntry(queryTerm);
            if (entry == null) {
                continue; // This term is not in the index, go to next document.
            }

            HashSet<String> ri_set = ri_hash.get(queryTerm);
            Integer rank = rel_index;
            // accumulate ri from rank 1 to rank i, where i is current found document
            Integer ri = 0;
            for (int rr = 0; rr < rank ; rr ++){ // rank - 1 == positio
                if (ri_arr[rr] != null){
                    ri += 1;
                }
            }
            // Obtain entry statistics.
            bm25_rsj.setEntryStatistics(entry.getWritableEntryStatistics());

            // Set the number of times the query term appears in the query.
            double kf = 0.0;
            for (String otherTerm : queryTerms) {
                if (otherTerm.equals(queryTerm)) {
                    kf++;
                }
            }
            bm25_rsj.setKeyFrequency(kf);

            // Prepare the weighting model for scoring.
            bm25_rsj.prepare();

            IterablePosting ip = invertedIndex.getPostings(entry);
            ip = invertedIndex.getPostings(entry);
            double score = 0.0;
            while (ip.next() != IterablePosting.EOL) {
                String docId = meta.getItem("docno", ip.getId());
                if (docIdSet.contains(docId)) {
                    bm25_rsj.set_R_ri(doc_R,ri);
                    score = bm25_rsj.score(ip);
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


//        // Create a results list from the scored documents.
        TrecResults results = new TrecResults();
        for (Map.Entry<String, Double> entry : scores.entrySet()) {
            if(!include_doc.contains(entry.getKey())){
                results.getTrecResults().add(new TrecResult(
                        topic,
                        entry.getKey(),
                        0,
                        entry.getValue(),
                        null
                ));
            }
        }

        // Sort the documents by the score assigned by the weighting model.
        Collections.sort(results.getTrecResults());
        Collections.reverse(results.getTrecResults());
//
//        // Assign the rank to the documents.
        for (int i = 0; i < results.getTrecResults().size(); i++) {
            results.getTrecResults().get(i).setRank(i + 1);
        }

        return results;

    }

    public HashMap<String, HashSet<String>> calculate_ri(String topic,ArrayList<String>  queryTerms, Lexicon lex, PostingIndex invertedIndex,HashMap<String, Integer> doc_map,MetaIndex meta) throws IOException {
        HashMap<String, HashSet<String>> output = new HashMap<>();
        for (String queryTerm : queryTerms) {
            LexiconEntry entry = lex.getLexiconEntry(queryTerm);
            if (entry == null) {
                continue; // This term is not in the index, go to next document.
            }

            // Calculate ri: document frequecy within relevant documents R.
            IterablePosting ip = invertedIndex.getPostings(entry);
            HashSet<String> ri_arr = getDocumentFreq(ip, meta, doc_map, topic);
            output.put(queryTerm,ri_arr);
        }
        return output;
    }

    public HashSet<String> getDocumentFreq(IterablePosting ip, MetaIndex meta, HashMap<String, Integer> doc_map_hash, String Topic) throws IOException {
        //    ri contain in doc_map && rank < cur_rank
        HashSet<String> doc_ri = new HashSet<String>();
        while (ip.next() != IterablePosting.EOL) {
            String docId = meta.getItem("docno", ip.getId());
            if (doc_map_hash.containsKey(docId) && isRelevance(Topic,docId)) {
                doc_ri.add(docId);
            }
        }
        return doc_ri;
    }

    public boolean isRelevance(String Topic, String docId){
        HashMap<String,Integer> doc_map = this.qrels.get(Topic);
        if (doc_map.containsKey(docId)){
            if (doc_map.get(docId) == 1){
                return true;
            }
            else{
                return false;
            }
        }else{
            return false;
        }
    }

    public Integer [] caculate_R(String [] doc_map, String Topic){
        Integer [] doc_R = new Integer[doc_map.length];
        Integer count = 0;
        for (int i = 0; i < doc_map.length; i++){
            if(isRelevance(Topic,doc_map[i])){
                count += 1;
            }
            doc_R[i] = count;
        }
        return doc_R;
    }




}
