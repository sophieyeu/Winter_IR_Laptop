package de.uni_mannheim.informatik.dws.wdi.ExerciseIdentityResolution;

import de.uni_mannheim.informatik.dws.wdi.ExerciseIdentityResolution.Comparators.*;
import de.uni_mannheim.informatik.dws.wdi.ExerciseIdentityResolution.model.Laptop;
import de.uni_mannheim.informatik.dws.wdi.ExerciseIdentityResolution.model.LaptopXMLReader;
import de.uni_mannheim.informatik.dws.winter.matching.MatchingEngine;
import de.uni_mannheim.informatik.dws.winter.matching.MatchingEvaluator;
import de.uni_mannheim.informatik.dws.winter.matching.algorithms.RuleLearner;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.NoBlocker;
import de.uni_mannheim.informatik.dws.winter.matching.rules.WekaMatchingRule;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.HashedDataSet;
import de.uni_mannheim.informatik.dws.winter.model.MatchingGoldStandard;
import de.uni_mannheim.informatik.dws.winter.model.Performance;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.io.CSVCorrespondenceFormatter;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.utils.WinterLogManager;
import org.apache.logging.log4j.Logger;
import weka.classifiers.trees.REPTree;

import java.io.File;

public class IR_using_machine_learning_laptop {

	/*
	 * Logging Options:
	 * 		default: 	level INFO	- console
	 * 		trace:		level TRACE     - console
	 * 		infoFile:	level INFO	- console/file
	 * 		traceFile:	level TRACE	- console/file
	 *
	 * To set the log level to trace and write the log to winter.log and console,
	 * activate the "traceFile" logger as follows:
	 *     private static final Logger logger = WinterLogManager.activateLogger("traceFile");
	 *
	 */

	private static final Logger logger = WinterLogManager.activateLogger("trace");

    public static void main( String[] args ) throws Exception
    {
		// loading data


//		// laptop
		HashedDataSet<Laptop, Attribute> laptop = new HashedDataSet<>();
		new LaptopXMLReader().loadFromXML(new File("data/input/laptop.xml"),
				"/laptop/product", laptop);



		// create a matching rule

		// -- SimpleLogistic
//		String options[] = new String[] { "-S" };
//		String modelType = "SimpleLogistic"; // use a logistic regression

//		// -- NaiveBayes
//        String options[] = new String[1];
//        options[0] = "-K";
//        String modelType = "NaiveBayes"; // use a NaiveBayes

		// -- Random forest
		String options[] = new String[] {"-M", "3.0", "-S", "42"};
		String modelType = "RandomForest";

		// -- HoeffdingTree
//		String options[] = new String[] {"-S", "0", "-L", "2"};
//		String modelType = "HoeffdingTree";


		// -- REP Tree
//		String options[] = new String[] {"-S", "42", "-M", "3.0"};
//		String modelType = "REPTree";

		// -- AdaBoost
//       	String options[] = new String[1];
//        options[0] = "-Q";
//        String modelType = "AdaBoostM1";
//
        WekaMatchingRule<Laptop, Attribute> matchingRule = new WekaMatchingRule<>(0.5, modelType, options);
		matchingRule.setClassifier(new REPTree());
        matchingRule.activateDebugReport("data/output/debugResultsMatchingRule_laptop.csv", 1000);
//
//
//		// add comparators

//	    matchingRule.addComparator(new LaptopNameComparatorEqual());
	    matchingRule.addComparator(new LaptopNameComparatorJaccard());
//	    matchingRule.addComparator(new LaptopNameComparatorJaroWinkler());
//	    matchingRule.addComparator(new LaptopNameComparatorLevenshtein());
//		matchingRule.addComparator(new LaptopComparatorJaroWinklerTfIdf());
		matchingRule.addComparator(new LaptopComparatorMongeElkan());
		matchingRule.addComparator(new LaptopComparatorMongeElkanTfIdf());
//		matchingRule.addComparator(new LaptopComparatorSoftTfIdf());

////
//        // Adding CrossValidation?
//        //matchingRule.setForwardSelection(true);
		matchingRule.setRandomSeed(42);
        matchingRule.setBackwardSelection(true);
//
//
//		// load the training set
		MatchingGoldStandard gsTraining = new MatchingGoldStandard();
		gsTraining.loadFromCSVFile(new File("data/goldstandard/train_laptop_final_300.csv"));
//
		// train the matching rule's model
		System.out.println("*\n*\tLearning matching rule\n*");
		RuleLearner<Laptop, Attribute> learner = new RuleLearner<>();
		learner.learnMatchingRule(laptop, laptop, null, matchingRule, gsTraining);
		System.out.println(String.format("Matching rule is:\n%s", matchingRule.getModelDescription()));
//
//		 create a blocker (blocking strategy)
//		StandardRecordBlocker<laptop, Attribute> blocker = new StandardRecordBlocker<Laptop, Attribute>(new LaptopBlockingKeyByNameGenerator());
//		SortedNeighbourhoodBlocker<laptop, Attribute, Attribute> blocker = new SortedNeighbourhoodBlocker<>(new GameBlockingKeyByPlatformNameGenerator(), 15);
		NoBlocker blocker = new NoBlocker();
		blocker.collectBlockSizeData("data/output/debugResultsBlocking_laptop.csv", 100);
//
//		// Initialize Matching Engine
		MatchingEngine<Laptop, Attribute> engine = new MatchingEngine<>();
//
//		// Execute the matching
		System.out.println("*\n*\tRunning identity resolution\n*");
		Processable<Correspondence<Laptop, Attribute>> correspondences = engine.runIdentityResolution(
				laptop, laptop, null, matchingRule,
				blocker);
//
//		// write the correspondences to the output file
		new CSVCorrespondenceFormatter().writeCSV(new File("data/output/laptop_correspondences.csv"), correspondences);
//
		// load the gold standard (test set)
		System.out.println("*\n*\tLoading gold standard\n*");
		MatchingGoldStandard gsTest = new MatchingGoldStandard();
		gsTest.loadFromCSVFile(new File(
				"data/goldstandard/test_laptop_final_300.csv"));

		// evaluate your result
		System.out.println("*\n*\tEvaluating result\n*");
		MatchingEvaluator<Laptop, Attribute> evaluator = new MatchingEvaluator<Laptop, Attribute>();
		Performance perfTest = evaluator.evaluateMatching(correspondences,
				gsTest);

		// print the evaluation result
		System.out.println("Laptop_Identity Resolution");
		System.out.println(String.format(
				"Precision: %.4f", perfTest.getPrecision()));
		System.out.println(String.format(
				"Recall: %.4f", perfTest.getRecall()));
		System.out.println(String.format(
				"F1: %.4f", perfTest.getF1()));
    }
}
