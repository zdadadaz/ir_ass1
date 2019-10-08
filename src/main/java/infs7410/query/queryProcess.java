package infs7410.query;

import infs7410.project1.TrecResult;
import infs7410.project1.TrecResults;
import org.terrier.querying.*;
import org.terrier.terms.PorterStemmer;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

public class queryProcess {
    private String url = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&term=";
    private String indexPath = "./var/index/pubmed";
    private HashSet<String> queryList = new HashSet<String>();
    private HashMap<String, ArrayList<String>> queryhash = new HashMap<String, ArrayList<String>>();
    private ArrayList<String>  referenceID;

    public queryProcess(String foldername,ArrayList<String> docid) throws IOException {
        this.readQuery(foldername);
        this.referenceID = docid;
    }
    public ArrayList<String> expandQeury(ArrayList<String> terms, double k,String queryReduction) throws Exception {
        StringBuilder termlist = new StringBuilder();
        ArrayList<String> output = new ArrayList<String>();
        PorterStemmer ps = new PorterStemmer();
        HashSet<String> termslist = new HashSet<String>();

        this.queryList.clear();
        for (String s : terms){
            String tmpS = s.replaceAll("[^a-zA-Z0-9]", "");
            tmpS = ps.stem(tmpS);
            if (!this.queryList.contains(tmpS)){
//                this.queryList.add(ps.stem(tmpS));
                this.expandQeuryOne(s);
            }
            if(!termslist.contains(tmpS)){
                output.add(tmpS);
                termslist.add(tmpS);
            }
        }
        for (String s : this.queryList) {
            termlist.append(" " + s);
        }
//        choose method of query reduction
        String out;
        if(queryReduction.equals("IDF")){
            out = this.runIDFreduction(termlist.toString(),(int)k);
        }else if(queryReduction.equals("IDFr")){
            out = this.runIDFr(termlist.toString(),k);
        }else{
            out = this.runKLI(termlist.toString(),k);
        }

        String [] outArr = out.split(" ");
        for (String s: outArr){
            s = ps.stem(s);
            if(!termslist.contains(s)){
                output.add(s);
                termslist.add(s);
            }
        }

        return output;

    }

    public void expandQeuryOne(String term)throws IOException {
        StringBuilder termlist = new StringBuilder();
        PorterStemmer ps = new PorterStemmer();
        if (!term.endsWith("*")) {
            return;
        }
        try{
            Scanner s = new Scanner(new URL(this.url + term).openStream());
            String tmp = s.findWithinHorizon("<QueryTranslation>\\s*(.*)\\s*<\\/QueryTranslation>",0);
            termlist = this.parseResponse(tmp);
            Thread.sleep(1000);
        }catch (NullPointerException | InterruptedException nfe){
            String termTmp = term.replaceAll("[^a-zA-Z0-9]", "");
            if(!this.queryList.contains(ps.stem(termTmp))){
                this.queryList.add(ps.stem(termTmp));
                termlist.append(" " +termTmp);
            }
        }

    }
    public String runIDFr(String query, double k ) throws Exception {
        ArrayList<String> out = new ArrayList<String>();
        IndexRef ref = IndexRef.of(this.indexPath + ".properties");
        IDFr expansion = new IDFr();
        String idfrQuery = expansion.IDFr_reduce(query,k, ref);

        return idfrQuery;
    }
    public String runKLI(String query, double k) throws Exception {
        ArrayList<String> out = new ArrayList<String>();
        IndexRef ref = IndexRef.of(this.indexPath + ".properties");
        KLI expansion = new KLI(this.referenceID);
        String idfrQuery = expansion.KLI_reduce(query,k, ref);

        return idfrQuery;
    }

    public String runIDFreduction(String query, int k) throws IOException {
        IndexRef ref = IndexRef.of(this.indexPath + ".properties");
        IDFReduction expansion = new IDFReduction();
        String idfrQuery = expansion.reduce(query,k, ref);

        return idfrQuery;
    }

    public StringBuilder parseResponse(String input){
        StringBuilder queryList= new StringBuilder();
        String [] strArr = input.split(" OR ");
        PorterStemmer ps = new PorterStemmer();
        for (String s : strArr){
            s = s.toLowerCase();
            s = s.replace("<querytranslation>","");
            s = s.replace("<\\/querytranslation>","");
            s = s.replace("all fields","");
            s = s.replaceAll("[^a-zA-Z0-9]", "");
            s = s.replace("exp","");
            s = s.replace("adj3","");
            if (!s.isEmpty()){
                s = ps.stem(s);
                if(!this.queryList.contains(s)){
                    queryList.append(" "+s);
                    this.queryList.add(s);
                }
            }
        }
        return queryList;
    }

    static TrecResults scoredDocs2TrecResults(ScoredDocList resultsList, String topic) {
        TrecResults trecResults = new TrecResults();
        for (int i = 0; i < resultsList.size(); i++) {
            ScoredDoc doc = resultsList.get(i);
            trecResults.getTrecResults().add(new TrecResult(topic, doc.getMetadata("docno"), i + 1, doc.getScore(), "qe"));
        }
        return trecResults;
    }

    public Boolean HasBooleanQuery(String topic){
        topic = topic.replaceAll(" ","");
        return this.queryhash.containsKey(topic);
    }

    public ArrayList<String> GetBooleanQuery(String topic){
        topic = topic.replaceAll(" ","");
        return this.queryhash.get(topic);
    }


    public void readQuery(String foldername) throws IOException {
        File filec1 = new File(foldername );
        ArrayList<String> filelist = new ArrayList<String>();
        ArrayList<String> fileNameList = new ArrayList<String>();
        HashMap<String, ArrayList<String>> output = new HashMap<String, ArrayList<String>> ();
        if(filec1.exists()){
            File[] contents = filec1.listFiles();
            if (contents != null) {
                for (File f : contents) {
                    if (f.getName().endsWith(".qr")){
                        String[] tmp = f.getName().split("_");
                        String tmp1 = tmp[1].substring(0,tmp[1].length()-3);
                        tmp1 = tmp1.replaceAll(" ","");
                        if (!tmp1.isEmpty()){
                            fileNameList.add(tmp1);
                            filelist.add(f.getAbsolutePath());
                        }
                    }
                }
            }
        }

        for (Integer i = 0; i<filelist.size();i++){
            String f = filelist.get(i);
            String g = fileNameList.get(i);
            InputStream is = new FileInputStream(f);
            BufferedReader buf = new BufferedReader((new InputStreamReader(is)));
            ArrayList<String > tmpArr = new ArrayList<String >();
            String line = buf.readLine();
             while (line != null) {
                 String[] parts = line.split(" ");
                 if (!parts[0].isEmpty()){
                     tmpArr.add(parts[0]);
                 }
                 line = buf.readLine();
             }
            this.queryhash.put(g,tmpArr);

        }


    }



}
