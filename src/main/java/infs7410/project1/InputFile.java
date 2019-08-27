package infs7410.project1;

import infs7410.util.topicInfo;
import java.io.*;
import java.util.*;

import org.terrier.terms.PorterStemmer;

/**
 * InputFile - Read train topic in with stopword deteciin
 * @author Chien-chi chen
 */
public class InputFile {
    /**
     * A list of Topic info including topic, title, query and docid
     */
    private ArrayList<topicInfo> output;
    private Integer fileSize;
    /**
     * A set of stop word for detection
     */
    private HashSet<String> stopwords;

    public topicInfo getOutput(Integer i) {
        return this.output.get(i);
    }
    public Integer getFileSize() {
        return this.fileSize;
    }

    /**
     * Read all topics file into output.
     *
     * @param foldername Input folder path
     * @require {@code foldername != null}
     */
    public InputFile(String foldername) throws IOException {
        this.output = new ArrayList<topicInfo>();
        File[] files = new File(foldername).listFiles();
        stopwords = new HashSet<String> ();
        readStopword();

        //If this pathname does not denote a directory, then listFiles() returns null.
        this.fileSize = files.length;

        for (File file : files) {
            if (file.isFile()) {
                if (!file.getName().substring(0,1).equals(".")){
                    topicInfo aa =this.readTopicFile(foldername+ file.getName());
                    aa.setFilename(file.getName());
                    this.output.add(aa);
                }
            }
        }
//        System.out.println(this.output.get(0).getTopic());
//        System.out.println(this.output.get(0).getTitle());

    }
    /**
     * Read one topic file into class topicInfo
     *
     * @param filename Input file path
     * @require {@code filename != null}
     */
    public topicInfo readTopicFile(String filename) throws IOException {
        InputStream is = new FileInputStream(filename);
        BufferedReader buf = new BufferedReader((new InputStreamReader(is)));
        PorterStemmer ps = new PorterStemmer();

        String topic = "";
        ArrayList<String> titles = new ArrayList<String>();
        ArrayList<String> queries = new ArrayList<String>();
        ArrayList<String> pids = new ArrayList<String>();

        String line = buf.readLine();
        Integer count =0;
        Integer flag = 0;
        while (line != null) {
            String[] parts = line.split(": ");
//          insert the first line info
            if (parts[0].equals("Topic")) {
                topic = parts[1];

            } else if (parts[0].equals("Title")) {
                flag = 1;
                String[] tmp = parts[1].split(" ");
                for (String s : tmp) {
                    if (!this.stopwords.contains(s)) {
                        s = ps.stem(s);
                        titles.add(s);
                    }
                }
            } else if (parts[0].equals("Query")) {
                flag = 2;
            } else if (parts[0].equals("Pids")) {
                flag = 3;
            }

            if (flag == 2) {
                if (!parts[0].isEmpty() && !parts[0].equals("Query")) {
                    String[] tmp = parts[0].split(" ");
                    for (String s : tmp) {
                        if (!s.toLowerCase().equals("or") && !s.toLowerCase().equals("add")){
                            s = s.replace("[tiab]","");
                            s = s.replaceAll("[^a-zA-Z0-9]", "");
                            if (!this.stopwords.contains(s) && !s.isEmpty()) {
                                queries.add(s);
                            }
                        }

                    }
                }
            } else if (flag == 3) {
                if (!parts[0].isEmpty() && !parts[0].equals("Pids")) {
                    String[] tmp = parts[0].split("    ");
                    if (tmp.length >1) {
                        pids.add(tmp[1]);
                    }
                }
            }
            line = buf.readLine();
        }
        topicInfo topicvariable = new topicInfo(topic,titles,queries,pids);
        return topicvariable;
    }
    /**
     * Read stop word file into variable stopwords
     */
    public void readStopword() throws IOException {
        String filename = "./stopword";
        this.stopwords = new HashSet<String>();
        InputStream is = new FileInputStream(filename);
        BufferedReader buf = new BufferedReader((new InputStreamReader(is)));
        String line = buf.readLine();
        while (line != null) {
            if(!stopwords.contains(line)){
                this.stopwords.add(line);
            }
            line = buf.readLine();
        }
    }

}

