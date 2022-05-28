package it.unisa.casper.refactor.splitting_algorithm.game_theory;

import com.google.common.primitives.Bytes;
import com.intellij.codeInsight.completion.CompletionPhase;
import com.intellij.util.ArrayUtil;
import it.unisa.casper.refactor.splitting_algorithm.MethodByMethodMatrixConstruction;
import it.unisa.casper.storage.beans.*;

import java.lang.reflect.Array;
import java.util.*;

import it.unisa.casper.refactor.exceptions.SplittingException;
import it.unisa.casper.refactor.strategy.SplittingStrategy;
import org.apache.commons.lang3.ArrayUtils;

public class GameTheorySplitClasses implements SplittingStrategy {

    private ArrayList<Byte[]> combinations;

    public GameTheorySplitClasses() {
        combinations = new ArrayList<>();
    }

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
    public Collection<ClassBean> split(ClassBean toSplit, double threshold) throws SplittingException, Exception {
        ArrayList<Byte> remainingMethods = new ArrayList<>();
        for (byte i = 0; i < toSplit.getMethodList().size() ; i++) {
            remainingMethods.add(i);
        }
        String packageName = toSplit.getFullQualifiedName().substring(0, toSplit.getFullQualifiedName().lastIndexOf("."));

        TopicExtractor te = new TopicExtractor();
        byte[] extractionResult = te.extractTopic(toSplit.getMethodList(), 10, 0.25);
        System.out.println(extractionResult.length + " topics extracted");

        MethodByMethodMatrixConstruction matrixConstruction = new MethodByMethodMatrixConstruction();
        double[][] methodByMethodMatrix = matrixConstruction.buildMethodByMethodMatrix(0.4, 0.1, 0.5, toSplit);
        double[][] methodByMethodMatrixFiltered = matrixConstruction.filterMatrix(methodByMethodMatrix, threshold);

        ArrayList<ArrayList<Byte>> playerChoices = new ArrayList<>();
        for (int i = 0; i < extractionResult.length ; i++) {
            playerChoices.add(new ArrayList<>());
            playerChoices.get(i).add(extractionResult[i]);
            remainingMethods.remove(Byte.valueOf(extractionResult[i]));
        }

        while (remainingMethods.size() != 0) {
            combinations = new ArrayList<>();
            PayoffMatrix pm = computePayoffMatrix(playerChoices, remainingMethods, methodByMethodMatrixFiltered, 0.5, 0.2);
            pm.calculatePayoffs();
            PayoffTuple nashEquilibrium = pm.findNashEquilibrium();
            for (int i = 0; i < nashEquilibrium.getMoves().length ; i++) {
                if (nashEquilibrium.getMoves()[i] != -1) {
                    playerChoices.get(i).add(nashEquilibrium.getMoves()[i]);
                    remainingMethods.remove(Byte.valueOf(nashEquilibrium.getMoves()[i]));
                }
            }
        }

        Collection<ClassBean> result = new Vector<>();
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

    private PayoffMatrix computePayoffMatrix(ArrayList<ArrayList<Byte>> playerChoices,
                                             ArrayList<Byte> remainingMethods,
                                             double[][] methodByMethodMatrix,
                                             double e1, double e2) {

        byte[] possibleChoices = new byte[remainingMethods.size()+1];
        for (int i = 0; i < remainingMethods.size() ; i++) {
            possibleChoices[i] = remainingMethods.get(i);
        }
        possibleChoices[remainingMethods.size()] = -1;
        calculateCombination(new ArrayList<>(), playerChoices.size(), 0, possibleChoices);
        combinations.remove(combinations.size()-1);
        PayoffMatrix pm = new PayoffMatrix(combinations, remainingMethods, playerChoices, methodByMethodMatrix, e1, e2);
        return pm;
    }

    private void calculateCombination(ArrayList<Byte> perm, int size, int pos, byte[] possibleChoices) {
        if (pos == size) {
            combinations.add(ArrayUtils.toObject(Bytes.toArray(perm)));
        } else {
            for (int i = 0 ; i < possibleChoices.length ; i++) {
                if (!perm.contains(possibleChoices[i]) | possibleChoices[i] == -1) {
                    ArrayList<Byte> temp = new ArrayList<>(perm);
                    temp.add(possibleChoices[i]);
                    calculateCombination(temp, size, pos+1, possibleChoices);
                }
            }
        }
    }

    public ClassBean createSplittedClassBean(int index, String packageName,
                                             ArrayList<MethodBean> methods,
                                             Vector<InstanceVariableBean> instanceVariables,
                                             PackageBean belongingPackage) {
        String classShortName = "Class_" + (index + 1);
        String tempName = packageName + "." + classShortName;
        List<MethodBean> methodsToAdd = new ArrayList<>();

        for (MethodBean m : methods)
            methodsToAdd.add(m);


        Set<InstanceVariableBean> instanceVariableBeanSet = new HashSet<>();

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
