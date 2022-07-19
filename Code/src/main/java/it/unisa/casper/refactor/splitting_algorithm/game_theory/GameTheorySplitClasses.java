package it.unisa.casper.refactor.splitting_algorithm.game_theory;

import com.google.common.primitives.Bytes;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import it.unisa.casper.refactor.splitting_algorithm.MethodByMethodMatrixConstruction;
import it.unisa.casper.storage.beans.*;
import java.util.*;
import it.unisa.casper.refactor.exceptions.SplittingException;
import it.unisa.casper.refactor.strategy.SplittingStrategy;
import org.apache.commons.lang3.ArrayUtils;

public class GameTheorySplitClasses implements SplittingStrategy {
    private final InputFinder inputFinder;
    private ArrayList<Integer> remainingMethods;
    private ArrayList<ArrayList<Integer>> playerChoices;
    public GameTheorySplitClasses() {
        inputFinder = new InputFinder();
        remainingMethods = new ArrayList<>();
        playerChoices = new ArrayList<>();
    }

    /**
     * Splits the input class in n classes using Game Theory.
     *
     * @param toSplit   the class to be splitted
     * @param threshold threshold for the topic merging with Jaccard Similarity
     * @return a Collection of ClassBean containing the new classes
     * @throws SplittingException, Exception
     *
     */
    @Override
    public Collection<ClassBean> split(ClassBean toSplit, double threshold) throws SplittingException, Exception {
        Collection<ClassBean> result = new ArrayList<>();

        playerChoices = inputFinder.extractTopics(toSplit, threshold, 1000);
        if (playerChoices.size() <= 1) {
            result.add(toSplit);
            return result;
        }

        MethodByMethodMatrixConstruction matrixConstruction = new MethodByMethodMatrixConstruction();
        double[][] methodByMethodMatrix = matrixConstruction.buildMethodByMethodMatrix(0.4, 0.1, 0.5, toSplit);

        for (MethodBean method : toSplit.getMethodList()) {
            boolean isChosen = false;
            for (ArrayList<Integer> playerChoice : playerChoices) {
                if ((playerChoice.get(0)) == toSplit.getMethodList().indexOf(method)) {
                    isChosen = true;
                }
            }
            if (!isChosen) {
                remainingMethods.add(toSplit.getMethodList().indexOf(method));
            }
        }

        while (remainingMethods.size() != 0) {
            ArrayList<Integer> nashEquilibrium = makeIteration(methodByMethodMatrix);
            System.out.println(nashEquilibrium);
            for (int move : nashEquilibrium) {
                if (move != -1) {
                    playerChoices.get(nashEquilibrium.indexOf(move)).add(move);
                    remainingMethods.remove(Integer.valueOf(move));
                }
            }
        }

        String packageName = toSplit.getFullQualifiedName().substring(0, toSplit.getFullQualifiedName().lastIndexOf("."));
        for (int i = 0; i < playerChoices.size() ; i++) {
            ArrayList<MethodBean> methods = new ArrayList<>();
            for (int j = 0; j < playerChoices.get(i).size() ; j++) {
                methods.add(toSplit.getMethodList().get(playerChoices.get(i).get(j)));
            }
            ClassBean playerClass = createSplittedClassBean(i, packageName, methods, new Vector<>(toSplit.getInstanceVariablesList()), toSplit.getBelongingPackage());
            result.add(playerClass);
        }
        return result;
    }

    /**
     * Make an iteration of the game returning the Nash Equilibrium
     *
     * @param methodByMethodMatrix a matrix of structural and semantic similarity measure between methods
     * @return a list of choices namely the Nash Equilibrium
     *
     */

    private ArrayList<Integer> makeIteration(double[][] methodByMethodMatrix) {
        PayoffMatrix pm = new PayoffMatrix(remainingMethods, playerChoices, methodByMethodMatrix, 0.5, 0.4);
        ArrayList<Integer> possibleChoices = new ArrayList<>(remainingMethods);
        possibleChoices.add(-1);
        pm.computePayoffs(new ArrayList<>(), playerChoices.size(), 0, possibleChoices);
        HashMap<ArrayList<Integer>, ArrayList<Double>> nashEquilibriums = pm.findNashEquilibriums();

        ArrayList<Integer> nashEquilibrium = new ArrayList<>();
        double maxPayoff = -1;
        for (Map.Entry<ArrayList<Integer>, ArrayList<Double>> entry : nashEquilibriums.entrySet()) {
            double nePayoff = 0;
            for (double x : entry.getValue()) {
                nePayoff += x;
            }
            if (nePayoff >= maxPayoff) {
                maxPayoff = nePayoff;
                nashEquilibrium = entry.getKey();
            }
        }
        return nashEquilibrium;
    }

    private ClassBean createSplittedClassBean(int index, String packageName,
                                             ArrayList<MethodBean> methods,
                                             Vector<InstanceVariableBean> instanceVariables,
                                             PackageBean belongingPackage) {
        String classShortName = "Class_" + (index + 1);
        String tempName = packageName + "." + classShortName;
        ArrayList<MethodBean> methodsToAdd = new ArrayList<>(methods);

        HashSet<InstanceVariableBean> instanceVariableBeanSet = new HashSet<>();

        for (InstanceVariableBean currentInstanceVariable : instanceVariables) {
            for (MethodBean methodToInspect : methodsToAdd) {
                if (methodToInspect.getInstanceVariableList().contains(currentInstanceVariable)) {
                    instanceVariableBeanSet.add(currentInstanceVariable);
                }
            }
        }

        List<InstanceVariableBean> variableBeansToAdd = new ArrayList<>(instanceVariableBeanSet);
        InstanceVariableList instanceVariableList = new InstanceVariableList();
        instanceVariableList.setList(variableBeansToAdd);

        MethodList methodList = new MethodList();
        methodList.setList(methodsToAdd);

        StringBuilder classTextContent = new StringBuilder();
        classTextContent.append("public class ");
        classTextContent.append(classShortName);
        classTextContent.append(" {");
        for (MethodBean methodBean : methodsToAdd) {
            classTextContent.append(methodBean.getTextContent());
            classTextContent.append("\n");
        }

        classTextContent.append("}");

        return new ClassBean.Builder(tempName, classTextContent.toString())
                .setInstanceVariables(instanceVariableList)
                .setMethods(methodList)
                .setImports(new ArrayList<String>())
                .setLOC(0)
                .setBelongingPackage(belongingPackage)
                .setPathToFile("")
                .setEntityClassUsage(0)
                .setAffectedSmell()
                .build();
    }
}
