package de.uni_mannheim.informatik.dws.wdi.WDI_IR_Laptop;

import de.uni_mannheim.informatik.dws.wdi.WDI_IR_Laptop.Comparators.LaptopNameComparatorJaccard;
import de.uni_mannheim.informatik.dws.wdi.WDI_IR_Laptop.Comparators.LaptopNameComparatorLevenshtein;
import de.uni_mannheim.informatik.dws.wdi.WDI_IR_Laptop.model.Laptop;
import de.uni_mannheim.informatik.dws.wdi.WDI_IR_Laptop.model.LaptopXMLReader;
import de.uni_mannheim.informatik.dws.winter.matching.MatchingEngine;
import de.uni_mannheim.informatik.dws.winter.matching.MatchingEvaluator;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.NoBlocker;
import de.uni_mannheim.informatik.dws.winter.matching.rules.LinearCombinationMatchingRule;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.HashedDataSet;
import de.uni_mannheim.informatik.dws.winter.model.MatchingGoldStandard;
import de.uni_mannheim.informatik.dws.winter.model.Performance;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.utils.WinterLogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class IR_LinearCombination {

    public static final Logger logger = WinterLogManager.activateLogger("default");
//    private static final Logger logger = WinterLogManager.activateLogger("trace");

    public static void main(String[] args) throws Exception {

        // load the laptops data set
        logger.info("Loading data set...");
        HashedDataSet<Laptop, Attribute> laptops = new HashedDataSet<>();
        new LaptopXMLReader().loadFromXML(new File("data/input/laptop.xml"),
                "/laptop/product", laptops);

        // for laptop offer instead of laptop(product)
//        new LaptopXMLReader().loadFromXML(new File("data/input/laptops_relevant_offers.xml"),
//                "/target/laptop", laptops);
        logger.info("Successfully loaded data sets");

        // define matching rule
        LinearCombinationMatchingRule<Laptop, Attribute> laptopRule = new LinearCombinationMatchingRule<>(0.65);
        laptopRule.addComparator(new LaptopNameComparatorJaccard(),0.6);
        laptopRule.addComparator(new LaptopNameComparatorLevenshtein(), 0.4);

        // define blocker/no blocker
        NoBlocker laptopBlocker = new NoBlocker();

        // create a matching engine
        MatchingEngine<Laptop, Attribute> laptopEngine = new MatchingEngine<>();

        // run the matching
        Processable<Correspondence<Laptop, Attribute>> laptopCorrespondences = laptopEngine.runIdentityResolution(laptops, laptops, null, laptopRule, laptopBlocker);

        // load the gold standard
        logger.info("Successfully loaded the gold standard");
        MatchingGoldStandard laptopGS = new MatchingGoldStandard();
        laptopGS.loadFromCSVFile(new File("data/goldstandard/CSV_laptop_final_300.csv"));

        // evaluate the result
        MatchingEvaluator<Laptop, Attribute> laptopEvaluator = new MatchingEvaluator<>();
        Performance perf = laptopEvaluator.evaluateMatching(laptopCorrespondences, laptopGS);

        // print the performance
        logger.info(String.format("Precision: %.4f", perf.getPrecision()));
        logger.info(String.format("Recall: %.4f", perf.getRecall()));
        logger.info(String.format("F1: %.4f", perf.getF1()));
    }

}

