package it.unisa.casper.refactor.splitting_algorithm.game_theory;

import it.unisa.casper.refactor.splitting_algorithm.Utility;
import it.unisa.casper.refactor.splitting_algorithm.game_theory.lda.*;
import it.unisa.casper.storage.beans.ClassBean;
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

    public ArrayList<Integer> extractTopic(ClassBean extractFrom) throws IOException {
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
        for (MethodBean m : extractFrom.getMethodList()) {
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

        int topicCount = 10;
        LdaGibbsSampler ldaGibbsSampler = new LdaGibbsSampler(corpus.getDocument(), corpus.getVocabularySize());
        ldaGibbsSampler.gibbs(topicCount);
        double[][] phi = ldaGibbsSampler.getPhi();
        Map<String, Double>[] topicMap = LdaUtil.translate(phi, corpus.getVocabulary(), 10);

        ArrayList<ArrayList<String>> topics = new ArrayList<>();
        for (Map<String, Double> topic : topicMap) {
            topics.add(new ArrayList<>(topic.keySet()));
        }

        // Topic merging if Jaccard Similarity >= 0.5
        for (int i = 0; i < topics.size()-1; i++) {
            for (int j = i+1; j < topics.size(); j++) {
                float jaccardSimilarity = computeJaccardSimilarity(topics.get(i), topics.get(j));
                System.out.println("comparing " + i + " " + j);
                if (jaccardSimilarity >= 0.5) {
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
        ArrayList<Integer> topicsSelected = new ArrayList<>();
        for (int i = 0; i < topics.size(); i++) {
            float maxJS = 0;
            for (int j = 0; j < methodsWordsSplitted.size(); j++) {
                if (topicsSelected.contains(j)) continue;
                float jaccardSimilarity = computeJaccardSimilarity(topics.get(i), methodsWordsSplitted.get(j));
                System.out.println("comparing " + i + " " + j);
                if (jaccardSimilarity >= maxJS) {
                    maxJS = jaccardSimilarity;
                    indexMethodMax = j;
                }
            }
            topicsSelected.add(indexMethodMax);
        }
        return topicsSelected;
    }

    float computeJaccardSimilarity(ArrayList<String> a, ArrayList<String> b){
        float match = 0;
        for (String aWord : a) {
            for (String bWord : b) {
                if (aWord.equals(bWord)) {
                    match+=1.0;
                }
            }
        }
        float jaccardSimilarity = match/(a.size() + b.size() - match);
        System.out.println("match: " + match);
        System.out.println("JS: " + jaccardSimilarity);
        return jaccardSimilarity;
    }

}
