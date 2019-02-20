package it.unisa.ascetic.analysis.splitting_algorithm;

import it.unisa.ascetic.analysis.code_smell_detection.similarityComputation.CosineSimilarity;
import it.unisa.ascetic.analysis.splitting_algorithm.checkQuality.packageLevel.PromiscuousPackageQualityChecker;
import it.unisa.ascetic.storage.beans.ClassBean;
import it.unisa.ascetic.storage.beans.ClassList;
import it.unisa.ascetic.storage.beans.PackageBean;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;


public class SplitPackages {

	private static Vector<String> chains = new Vector<String>();

	private ArrayList<String> nameOfPackages=new ArrayList<String>();

	public Collection<String> getNameOfPackages() {
		return nameOfPackages;
	}

	public void setNameOfPackages(ArrayList<String> nameOfPackages) {
		this.nameOfPackages = nameOfPackages;
	}

	public void addPackageName(String pPackageName){
		this.nameOfPackages.add(pPackageName);
	}

	/**
	 * Splits the input package, i.e., pToSplit in two or more new packages.
	 * @param pToSplit the package to be splitted
	 * @param pThreshold the threshold to filter the class-by-class matrix
	 * @return a Collection of PackagBean containing the new packages
	 */

	/*public Collection<PackageBean> split2(PackageBean pToSplit, double pWicp, double pWccbc, double pTreshold){

		Collection<PackageBean> result = new Vector<PackageBean>(); 
		Collection<ClassBean> classes = new Vector<ClassBean>();
		Random random = new Random();
		PackageBean firstPackage=new PackageBean();
		PackageBean secondPackage=new PackageBean();
		PackageBean thirdPackage=new PackageBean();
		PackageBean fourPackage = new PackageBean();
		PackageBean fivePackage = new PackageBean();

		int i=0;
		for(ClassBean c: pToSplit.getClasses()){
			if(i>1 &&(i<=3)){
				firstPackage.setName("Insert");
				firstPackage.addClass(c);
			} else if(i>3){
				secondPackage.addClass(c);
				secondPackage.setName("Insert");
			} else if(i<=1){
				thirdPackage.setName("UNASSIGNED_CLASSES");
				thirdPackage.addClass(c);
			}

			i++;
		}
		result.add(firstPackage);
		result.add(secondPackage);
		result.add(thirdPackage);

		return result;
	}


	/**
	 * Splits the input package, i.e., pToSplit in two or more new packages.
	 * @param pToSplit the package to be splitted
	 * @param pWicp the weight of the ICP measure (structural)
	 * @param pWccbc the weight of the CCBC measure (semantic)
	 * @param pThreshold the threshold to filter the class-by-class matrix
	 * @return a Collection of PackagBean containing the new packages
	 * @throws Exception 
	 */

	public Collection<PackageBean> split(PackageBean pToSplit, double pThreshold) throws Exception {

		Collection<PackageBean> result = new Vector<PackageBean>(); 

		Iterator<ClassBean> it = pToSplit.getClassList().iterator();
		Vector<ClassBean> vectorClasses = new Vector<ClassBean>();
		PromiscuousPackageQualityChecker qualityChecker = new PromiscuousPackageQualityChecker();

		/*if(containsClassroomKeyword(pToSplit)) {
			PackageBean classroomManagement=new PackageBean();
			PackageBean teachingsManagement=new PackageBean();

			for(ClassBean classBean: pToSplit.getClasses()) {
				if(classBean.getName().toLowerCase().contains("teaching"))
					teachingsManagement.addClass(classBean);
				else classroomManagement.addClass(classBean);
			} 

			result.add(teachingsManagement);
			result.add(classroomManagement);

			return result;

		} else {*/

			ClassBean tmpClass = null;
			while (it.hasNext()){
				tmpClass = (ClassBean) it.next();
				if (!tmpClass.getFullQualifiedName().equals(pToSplit.getFullQualifiedName()))
					vectorClasses.add(tmpClass);
			}

			Collections.sort(vectorClasses);

			Pattern p = Pattern.compile("-");

			ClassByClassMatrixConstruction matrixConstruction  = new ClassByClassMatrixConstruction();
			double[][] classByClassMatrix = matrixConstruction.buildClassByClassMatrix(0.5, 0.5, pThreshold, pToSplit);

			double[][] classByClassMatrixFiltered = new double[classByClassMatrix.length][classByClassMatrix.length];

			classByClassMatrixFiltered = matrixConstruction.filterMatrix(classByClassMatrix, pThreshold);
			Vector<Integer>tmpMarkovChain = new Vector<Integer>();
			Vector<Integer>makeMethods = new Vector<Integer>();
			double[] tmpProbability = new double[classByClassMatrix.length];

			chains = new Vector<String>();
			getMarkovChains(classByClassMatrixFiltered, 0, tmpMarkovChain, tmpProbability, makeMethods);

			// Placing trivial chains
			Vector<String> newChains = new Vector<String>();
			for(int i=0; i<chains.size(); i++){
				String[] classes = p.split(chains.elementAt(i));
				if (classes.length < 3) {
					//it's a trivial chain
					double maxSimilarity = 0;
					int indexChain = -1;
					for(int j=0; j<chains.size(); j++){
						if (i!=j) {
							String[] tmpChains = p.split(chains.elementAt(j));

							if (tmpChains.length > 2) {
								double sim = 0;
								for (int k=0; k<classes.length; k++){
									for(int s=0; s<tmpChains.length; s++){
										sim += classByClassMatrix[Integer.valueOf(classes[k])][Integer.valueOf(tmpChains[s])];
									}
								}
								sim = (double)sim/(classes.length*tmpChains.length);
								if (sim > maxSimilarity){
									indexChain = j;
									maxSimilarity = sim;
								}
							}
						}
					}

					if (indexChain > -1) {
						newChains.add(chains.elementAt(i) + chains.elementAt(indexChain));
					} else {
						newChains.add(chains.elementAt(i));
					}

				} else {
					newChains.add(chains.elementAt(i));
				}

			}
			Collection<PackageBean> actualPackages = new Vector<PackageBean>();

			if (newChains.size() > 5) {
				Vector<PackageBean> trivialPackages = new Vector<PackageBean>();

				//Conto le trivial chains
				int count = 0;
				for(int i=0; i<newChains.size(); i++) {
				    String packageName = "package_"+(i+1);
					//PackageBean tmpPackageClasses = new PackageBean();
                    List<ClassBean> tmpPackageClasses = new ArrayList<>();
					String[] classes = p.split(newChains.elementAt(i));

					for (int j=0; j<classes.length; j++){
						tmpPackageClasses.add(vectorClasses.elementAt(Integer.valueOf(classes[j])));
					}

                    ClassList tmpClassList = new ClassList();
					tmpClassList.setList(tmpPackageClasses);
					PackageBean tmpPackageBean = new PackageBean.Builder(packageName,"")
                                                    .setClassList(tmpClassList)
                                                    .build();
					if(tmpPackageClasses.size() < 3) {
						count++;
						trivialPackages.add(tmpPackageBean);
					} else actualPackages.add(tmpPackageBean);
				}

				for(PackageBean pack: trivialPackages) {
					for(ClassBean classBean: pack.getClassList()) {
						double[][] mbm = matrixConstruction.buildClassByClassMatrix(0.9, 0.1, 0.4, pToSplit);
						PackageBean whereAdd = selectPackageWhereInsert(classBean, actualPackages);
						whereAdd.getClassList().add((classBean));
					}
				}
			}
			return actualPackages;
		//}
	}

	private PackageBean selectPackageWhereInsert(ClassBean pClassBean, Collection<PackageBean> actualPackages) {
		double max = 0.0;
		CosineSimilarity cosineSimilarity = new CosineSimilarity();
		PackageBean toReturn = null;

		for(PackageBean pack: actualPackages) {
			double similarity = 0.0;

			for(ClassBean classBean: pack.getClassList()) {
				String[] document1 = new String[2];
				String[] document2 = new String[2];

				document1[0] = classBean.getFullQualifiedName();
				document1[1] = classBean.getTextContent();

				document2[0] = pClassBean.getFullQualifiedName();
				document2[1] = pClassBean.getTextContent();

				try {
					similarity += cosineSimilarity.computeSimilarity(document1, document2);

				} catch (IOException e) {
					similarity += 0.0;
				}
			}

			if(similarity > max) {
				toReturn = pack;
			}
		}

		return toReturn;
	}

	/**
	 * Estrae le catene di markov (Classi) e le stampa su un file
	 * @param startIndex: l'indice da cui iniziare
	 * @param tmpMarkovChain: conserva la catena di markov tra le chiamate ricorsive
	 * @param tmpMarkovChainProbability: vettore riga conserva la probabilit�
	 * @param makeMethods: memorizza tutti i metodi sinora inclusi in una qualunque catena di markov
	 * @return true quando l'operazione e' terminata
	 */
	public static boolean getMarkovChains(double[][] methodByMethodMatrix, int startIndex, Vector<Integer> tmpMarkovChain, double[] tmpMarkovChainProbability, Vector<Integer> makeMethods){

		//Le dimensioni della matrice
		int matrixSize = methodByMethodMatrix.length;

		//Variabili temporanee
		int tmpSum = 0;
		double tmpRowSum = 0;


		//Vettore utilizzato per contenere le probabilit� presenti su una riga
		Vector<Double> tmpRowProbability = new Vector<Double>();
		//Vettore utilizzato per contenere gli indici delle probabilit� presenti su una riga
		Vector<Integer> tmpRowIndexProbability = new Vector<Integer>();

		makeMethods.add(startIndex);//Segno che ho gi� analizzato il metodo legato allo startIndex
		tmpMarkovChain.add(startIndex);//Aggiungo l'indice passato alla catena di markov in produzione

		//Azzero la colonna inerente il metodo gi� incluso nella catena di markov
		//in questo modo nessun altro metodo potr� raggiungerlo
		for (int i=0; i<methodByMethodMatrix.length; i++){
			methodByMethodMatrix[i][startIndex] = 0;
		}

		//Sommo le probabilit� nella catena di markov
		for(int j=0; j<matrixSize; j++){
			if (j!=startIndex){
				tmpMarkovChainProbability[j] = methodByMethodMatrix[startIndex][j] + tmpMarkovChainProbability[j];
			} else {
				//Se stiamo operando nella cella rappresentante il nuovo metodo inserito nella catena l'azzero
				tmpMarkovChainProbability[j] = 0;
			}
		}

		//Calcolo le probabilit�
		for (int j=0; j<tmpMarkovChainProbability.length; j++){
			if (startIndex != j){
				if (tmpMarkovChainProbability[j] > 0){
					tmpRowProbability.add(tmpMarkovChainProbability[j]);
					tmpRowIndexProbability.add(j);
				}
			}
		}

		//Criterio di arresto della catena di markov
		if (tmpRowProbability.size() > 0){

			/*Effettuo l'estrazione casuale del metodo*/
			tmpSum = 0;
			for (int i=0; i< tmpRowProbability.size(); i++){
				tmpSum = (int) (tmpSum + (tmpRowProbability.elementAt(i)*1000));
			}
			int[] extraction = new int[tmpSum];
			int iterationStart = 0;
			for (int i=0; i<tmpRowProbability.size(); i++){
				for (int j=iterationStart; j<((int)(tmpRowProbability.elementAt(i)* 1000)+iterationStart); j++){
					extraction[j] = tmpRowIndexProbability.elementAt(i);
				}
				iterationStart = ((int)(tmpRowProbability.elementAt(i)* 1000) + iterationStart);
			}
			//Estraiamo l'indice del prossimo metodo da inserire nella catena di markov
			//MAX
			int newStartIndex = extraction[getMaxValueFromVector(extraction)];

			//Effettuiamo la chiamata ricorsiva
			getMarkovChains(methodByMethodMatrix, newStartIndex, tmpMarkovChain, tmpMarkovChainProbability, makeMethods);

		} else {//In questo caso devo fermare la produzione della catena di markov

			//Ordino il contenuto della catena di markov

			Collections.sort(tmpMarkovChain);
			String chain = "";
			for (int i=0; i<tmpMarkovChain.size(); i++){
				chain = chain + tmpMarkovChain.elementAt(i) + "-";
			}
			chains.add(chain);

			//Svuoto il contenuto della catena di markov
			tmpMarkovChain = new Vector<Integer>();

			//Cerco il primo metodo non incluso in alcuna catena ed effettuo la chiamata ricorsiva all'algoritmo
			for (int i=0; i<methodByMethodMatrix.length; i++){
				if (!makeMethods.contains(i)){
					startIndex = i;
					getMarkovChains(methodByMethodMatrix, startIndex, tmpMarkovChain, tmpMarkovChainProbability, makeMethods);
				}
			}
			return true;
		}
		return true;
	}

	public static int getSmallestNonTrivialChain(Vector<String> chains){
		int result = -1;
		int minLength = 10000;
		Pattern p = Pattern.compile("-");
		for (int i=0; i<chains.size(); i++){
			String s = chains.elementAt(i);
			String[] methods = p.split(s);
			if (methods.length < minLength && methods.length > 2){
				minLength = methods.length;
				result = i;
			}
		}
		return result;
	}

	public static int getMaxValueFromVector(int[] vector){
		int tmpMax = 0;
		int tmpIndexMax = 0;

		for (int i=0; i<vector.length; i++){
			if (vector[i]>tmpMax){
				tmpMax = vector[i];
				tmpIndexMax = i;
			}
		}
		return tmpIndexMax;
	}

	/*private boolean containsClassroomKeyword(PackageBean pPackage) {
		boolean classroomContained = false;
		boolean teachingsContained = false;

		for(ClassBean classBean: pPackage.getClasses()) {
			if(classBean.getName().toLowerCase().contains("teaching"))
				teachingsContained = true;

			if(classBean.getName().toLowerCase().contains("classroom"))
				classroomContained = true;
		}

		if(classroomContained && teachingsContained && (pPackage.getClasses().size() == 14) ) 
			return true;

		return false;
	}*/

}
