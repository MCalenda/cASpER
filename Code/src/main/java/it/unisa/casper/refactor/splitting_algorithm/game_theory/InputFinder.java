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
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class InputFinder {
    private File stopwordList;
    public InputFinder() {
        this.stopwordList = new File(System.getProperty("user.home") + File.separator + ".casper" + File.separator+ "stopwordlist.txt");
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
    public ArrayList<ArrayList<Integer>> extractTopic(ClassBean toExtract, double threshold, int numIter) throws IOException {

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

        ArrayList<ArrayList<String>> methodsWordsSplitted = new ArrayList<>();
        for (MethodBean m : toExtract.getMethodList()) {
            String text = Utility.clean(m.getTextContent());
            ArrayList<String> textSplitted = new ArrayList<>(Arrays.asList(text.split("((\\s+)|((?<=[a-z])(?=[A-Z])))")));
            ArrayList<String> texSplittedClean = new ArrayList<>();
            for (String s : textSplitted) {
                if (!StringUtils.isNumeric(s) && s.length() > 3) {
                    texSplittedClean.add(s.toLowerCase());
                }
            }
            texSplittedClean.removeAll(stopWords);
            methodsWordsSplitted.add(texSplittedClean);
        }

        List<String> flat =
                methodsWordsSplitted.stream()
                        .flatMap(List::stream)
                        .collect(Collectors.toList());
        String text = String.join(" ", flat);
        corpus.addThruPipe(new Instance(text, null, null, null));

        int numTopics = 5;
        ParallelTopicModel lda = new ParallelTopicModel(numTopics);
        lda.addInstances(corpus);
        lda.setNumThreads(4);
        lda.setNumIterations(numIter);
        lda.estimate();

        Alphabet dataAlphabet = corpus.getDataAlphabet();
        ArrayList<ArrayList<String>> topics = new ArrayList<>();
        for (TreeSet<IDSorter> topWords : lda.getSortedWords()){
            Iterator<IDSorter> iterator = topWords.iterator();
            ArrayList<String> topic = new ArrayList<>();
            int rank = 0;
            while (iterator.hasNext() && rank++ < 20) {
                IDSorter idCountPair = iterator.next();
                topic.add(dataAlphabet.lookupObject(idCountPair.getID()).toString());
            }
            topics.add(topic);
        }

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

        ArrayList<ArrayList<Integer>> result = new ArrayList<>();
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
            ArrayList<Integer> playerChoice = new ArrayList<>();
            playerChoice.add(candidateSeedMethod);
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
    public double computeJaccardSimilarity(ArrayList<String> a, ArrayList<String> b) {
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
