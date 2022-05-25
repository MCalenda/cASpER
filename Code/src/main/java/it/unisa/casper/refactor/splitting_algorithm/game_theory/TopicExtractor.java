package it.unisa.casper.refactor.splitting_algorithm.game_theory;

import it.unisa.casper.refactor.splitting_algorithm.Utility;
import it.unisa.casper.refactor.splitting_algorithm.game_theory.lda.*;
import it.unisa.casper.storage.beans.MethodBean;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class TopicExtractor {

    private String casperDirectoryPath;
    private File stopwordList;

    public TopicExtractor() {
        casperDirectoryPath = System.getProperty("user.home") + "/.casper";
        stopwordList = new File(casperDirectoryPath + "stopwordlist.txt");
    }

    /**
     * Extract the number of topics from the class extractFrom using Latent Dirichlet Allocation (LDA) and merging the
     * ones that have a Jaccard Similarity more than a fixed threshold of 0.5
     *
     * @param methods the methods list to extract topics from
     * @return An Array of integer with the size of the topics extracted i.e. number of player of
     * the Game Theory splitting and each slot has the index of the most similar method of extractFrom
     * @throws IOException
     */
    public byte[] extractTopic(List<MethodBean> methods, int numTopics, double JSThreshold) throws IOException {
        // Cleaning code
        if (!stopwordList.exists()) {
            stopwordList.createNewFile();
            Utility.createStopwordList(stopwordList);
        }
        Scanner sc = new Scanner(stopwordList);
        ArrayList<String> stopWords = new ArrayList<String>();
        while (sc.hasNext()) {
            stopWords.add(sc.nextLine().toLowerCase());
        }

        ArrayList<ArrayList<String>> methodsWordsSplitted = new ArrayList<>();
        for (MethodBean m : methods) {
            String mWords = Utility.clean(m.getTextContent());
            ArrayList<String> mWordsSplitted = new ArrayList<>(Arrays.asList(mWords.split("((\\s+)|((?<=[a-z])(?=[A-Z])))")));
            for (int i = 0; i < mWordsSplitted.size(); i++) {
                if (mWordsSplitted.get(i).equals("")) mWordsSplitted.remove(i);
                else mWordsSplitted.set(i, mWordsSplitted.get(i).toLowerCase());
            }
            mWordsSplitted.removeAll(stopWords);
            methodsWordsSplitted.add(mWordsSplitted);
        }

        // LDA topic analysis
        Corpus corpus = new Corpus();
        for (ArrayList<String> aw : methodsWordsSplitted) {
            corpus.addDocument(aw);
        }

        LdaGibbsSampler ldaGibbsSampler = new LdaGibbsSampler(corpus.getDocument(), corpus.getVocabularySize());
        ldaGibbsSampler.gibbs(numTopics);
        double[][] phi = ldaGibbsSampler.getPhi();
        Map<String, Double>[] topicMap = LdaUtil.translate(phi, corpus.getVocabulary(), 10);

        ArrayList<ArrayList<String>> topics = new ArrayList<>();
        for (Map<String, Double> topic : topicMap) {
            topics.add(new ArrayList<>(topic.keySet()));
        }

        // Topic merging if Jaccard Similarity >= 0.5
        for (int i = 0; i < topics.size()-1; i++) {
            for (int j = i+1; j < topics.size(); j++) {
                double jaccardSimilarity = computeJaccardSimilarity(topics.get(i), topics.get(j));
                if (jaccardSimilarity >= JSThreshold) {
                    ArrayList<String> iCopy = new ArrayList<>(topics.get(i));
                    iCopy.removeAll(topics.get(j));
                    iCopy.addAll(topics.get(j));
                    topics.set(i, iCopy);
                    topics.remove(j);
                    i=-1;
                    break;
                }
            }
        }

        // Assigning methods to topic
        int indexMethodMax = -1;
        byte[] topicsSelected = new byte[topics.size()];
        for (int i = 0; i < topics.size(); i++) {
            double maxJS = 0;
            for (int j = 0; j < methodsWordsSplitted.size(); j++) {
                boolean alreadySelected = false;
                for (int k = 0; k <  topicsSelected.length; k++) {
                    if (topicsSelected[k] == j) alreadySelected = true;
                }
                if (alreadySelected) continue;
                double jaccardSimilarity = computeJaccardSimilarity(topics.get(i), methodsWordsSplitted.get(j));
                if (jaccardSimilarity >= maxJS) {
                    maxJS = jaccardSimilarity;
                    indexMethodMax = j;
                }
            }
            topicsSelected[i] = (byte) indexMethodMax;
        }
        return topicsSelected;
    }

    /**
     * Compute the Jaccard Similarity i.e. JS(A, B) = |A⋂B| / |A⋃B|
     *
     * @param a the first list of string to compare
     * @param b the secondo list of string to compare
     * @return the Jaccard Similarity between the two lists of strings
     */
    double computeJaccardSimilarity(ArrayList<String> a, ArrayList<String> b){
        double match = 0;
        for (String aWord : a) {
            for (String bWord : b) {
                if (aWord.equals(bWord)) {
                    match++;
                }
            }
        }
        double jaccardSimilarity = match/(a.size() + b.size() - match);
        return jaccardSimilarity;
    }

}
