package it.unisa.casper.refactor.splitting_algorithm.game_theory;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import it.unisa.casper.refactor.splitting_algorithm.Utility;
import it.unisa.casper.storage.beans.ClassBean;
import it.unisa.casper.storage.beans.MethodBean;

import cc.mallet.util.*;
import cc.mallet.types.*;
import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.*;
import cc.mallet.topics.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.regex.Pattern;


public class InputFinder {

    private File stopwordList;
    private ProgressIndicator indicator;


    public InputFinder() {
        this.stopwordList = new File(System.getProperty("user.home") + File.separator + ".casper" + File.separator+ "stopwordlist.txt");
        this.indicator = ProgressManager.getInstance().getProgressIndicator();
    }

    /**
     * Extract the number of topics from the class extractFrom using Latent Dirichlet Allocation (LDA) and merging the
     * ones that have a Jaccard Similarity higher than a threshold
     *
     * @param toExtract the ClassBean to extract topics from
     * @param threshold threshold for the topic merging with Jaccard Similarity
     * @return An Array of integer with the size of the topics extracted i.e. number of player of
     * the Game Theory splitting and each slot has the index of the most similar method of extractFrom
     * @throws IOException
     */
    public ArrayList<ArrayList<Byte>> extractTopic(ClassBean toExtract, double threshold) throws IOException {

        indicator.setText2("Cleaning text...");
        Utility.createStopwordList(stopwordList);
        Scanner sc = new Scanner(stopwordList);
        ArrayList<String> stopWords = new ArrayList<>();
        while (sc.hasNext()) {
            stopWords.add(sc.nextLine().toLowerCase());
        }

        ArrayList<Pipe> pipeList = new ArrayList<>();
        pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")));
        pipeList.add( new TokenSequence2FeatureSequence());
        InstanceList corpus = new InstanceList (new SerialPipes(pipeList));

        ArrayList<Instance> methods = new ArrayList<>();
        ArrayList<ArrayList<String>> methodsWordsSplitted = new ArrayList<>();
        for (MethodBean m : toExtract.getMethodList()) {
            String text = Utility.clean(m.getTextContent());
            String[] textSplitted = text.split("((\\s+)|((?<=[a-z])(?=[A-Z])))");
            ArrayList<String> textSplittedClean = new ArrayList<>();
            for (String s : textSplitted) {
                if (s.length() >= 4) {
                    textSplittedClean.add(s.toLowerCase());
                }
            }
            textSplittedClean.removeAll(stopWords);
            methodsWordsSplitted.add(textSplittedClean);
            methods.add(new Instance(String.join(" ", textSplittedClean), null, m.getFullQualifiedName(), null));
        }

        corpus.addThruPipe(methods.listIterator());

        int numTopics = 5;
        ParallelTopicModel lda = new ParallelTopicModel(numTopics, 1.0, 0.01);
        lda.addInstances(corpus);
        lda.setNumThreads(4);
        indicator.setText2("Training...");
        lda.estimate();

        ArrayList<ArrayList<String>> topics = new ArrayList<>();
        for (Object[] src : lda.getTopWords(20)){
            String[] dest = new String[src.length];
            System.arraycopy(src, 0, dest, 0, src.length);
            topics.add(new ArrayList<>(Arrays.asList(dest)));
        }

        indicator.setText2("Merging topics...");
        for (int i = 0; i < topics.size()-1; i++) {
            for (int j = i+1; j < topics.size(); j++) {
                double jaccardSimilarity = computeJaccardSimilarity(topics.get(i), topics.get(j));
                if (jaccardSimilarity >= threshold && topics.size() != 1) {
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

        indicator.setText2("Assigning seed method...");
        ArrayList<ArrayList<Byte>> result = new ArrayList<>();
        ArrayList<Integer> alreadySelected = new ArrayList<>();
        for (int i = 0; i < topics.size() ; i++) {
            double maxJS = 0;
            int candidateSeedMethod = -1;
            for (int j = 0; j < methodsWordsSplitted.size() ; j++) {
                if (alreadySelected.contains(j)) continue;
                double jaccardSimilarity = computeJaccardSimilarity(topics.get(i), methodsWordsSplitted.get(j));
                if (jaccardSimilarity >= maxJS) {
                    maxJS = jaccardSimilarity;
                    candidateSeedMethod = j;
                }
            }
            alreadySelected.add(candidateSeedMethod);
            ArrayList<Byte> playerChoice = new ArrayList<>();
            playerChoice.add((byte) candidateSeedMethod);
            result.add(playerChoice);
        }

        return result;
    }

    /**
     * Compute the Jaccard Similarity i.e. JS(A, B) = |A⋂B| / |A⋃B|
     *
     * @param a the first list of string to compare
     * @param b the secondo list of string to compare
     * @return the Jaccard Similarity between the two lists of strings
     */
    double computeJaccardSimilarity(ArrayList<String> a, ArrayList<String> b) {
        double match = 0;
        for (String aWord : a) {
            for (String bWord : b) {
                if (aWord.equals(bWord)) {
                    match++;
                }
            }
        }
        return match/(a.size() + b.size() - match);
    }

}
