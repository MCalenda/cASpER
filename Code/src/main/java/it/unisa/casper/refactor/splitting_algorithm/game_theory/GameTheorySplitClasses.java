package it.unisa.casper.refactor.splitting_algorithm.game_theory;

import com.intellij.util.messages.Topic;
import it.unisa.casper.refactor.splitting_algorithm.MethodByMethodMatrixConstruction;
import it.unisa.casper.storage.beans.*;

import java.util.*;
import java.util.regex.Pattern;

import it.unisa.casper.refactor.exceptions.SplittingException;
import it.unisa.casper.refactor.strategy.SplittingStrategy;

public class GameTheorySplitClasses implements SplittingStrategy {

    public GameTheorySplitClasses(){}

    /**
     * Splits the input class in n classes using Game Theory.
     *
     * @param toSplit   the class to be splitted
     * @param threshold the threshold to filter the method-by-method matrix
     * @return a Collection of ClassBean containing the new classes
     * @throws SplittingException, Exception
     * @throws Exception
     */
    @Override
    public Collection<ClassBean> split(ClassBean toSplit, double threshold) throws SplittingException, Exception {Collection<ClassBean> result = new Vector<>();
        TopicExtractor te = new TopicExtractor();
        ArrayList<Integer> numPlayers = te.extractTopic(toSplit);
        return result;

    }
}
