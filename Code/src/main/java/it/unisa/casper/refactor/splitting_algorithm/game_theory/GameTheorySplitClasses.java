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

    private InputFinder inputFinder;
    private ProgressIndicator indicator;

    public GameTheorySplitClasses() {
        inputFinder = new InputFinder();
        this.indicator = ProgressManager.getInstance().getProgressIndicator();

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
        indicator.setText("Applying LDA...");
        Collection<ClassBean> result = new Vector<>();
        ArrayList<ArrayList<Byte>> playerChoices = inputFinder.extractTopic(toSplit, threshold);
        String packageName = toSplit.getFullQualifiedName().substring(0, toSplit.getFullQualifiedName().lastIndexOf("."));

        if (playerChoices.size() <= 1) {
            result.add(toSplit);
            return result;
        }

        MethodByMethodMatrixConstruction matrixConstruction = new MethodByMethodMatrixConstruction();
        double[][] methodByMethodMatrixRAW = matrixConstruction.buildMethodByMethodMatrix(0.4, 0.1, 0.5, toSplit);
        double[][] methodByMethodMatrix = matrixConstruction.filterMatrix(methodByMethodMatrixRAW, 0.09);


        ArrayList<Byte> remainingMethods = new ArrayList<>();
        for (MethodBean method : toSplit.getMethodList()) {
            boolean isChosen = false;
            for (ArrayList<Byte> playerChoice : playerChoices) {
                if ((playerChoice.get(0)) == toSplit.getMethodList().indexOf(method)) {
                    isChosen = true;
                }
            }
            if (!isChosen) {
                remainingMethods.add((byte) toSplit.getMethodList().indexOf(method));
            }

        }

        int gameIteration = 1;
        while (remainingMethods.size() != 0) {
            indicator.setText("Game Iteration number: " + gameIteration++ );
            PayoffMatrix pm = new PayoffMatrix(remainingMethods, playerChoices, methodByMethodMatrix, 0.5, 0.3);
            ArrayList<Byte> nashEquilibrium = pm.findNashEquilibrium();
            System.out.println(nashEquilibrium);
            for (int i = 0; i < nashEquilibrium.size() ; i++) {
                Byte move = nashEquilibrium.get(i);
                if (move != -1) {
                    playerChoices.get(i).add(move);
                    remainingMethods.remove(move);
                }
            }
        }

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

    public ClassBean createSplittedClassBean(int index, String packageName,
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
