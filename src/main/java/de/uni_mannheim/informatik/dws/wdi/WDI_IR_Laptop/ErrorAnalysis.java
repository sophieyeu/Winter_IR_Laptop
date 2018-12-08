package de.uni_mannheim.informatik.dws.wdi.WDI_IR_Laptop;

import java.util.HashSet;
import java.util.Set;

import de.uni_mannheim.informatik.dws.wdi.WDI_IR_Laptop.model.Laptop;
import org.apache.logging.log4j.Logger;

import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.MatchingGoldStandard;
import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.utils.WinterLogManager;

public class ErrorAnalysis {
	
	private static final Logger logger = WinterLogManager.getLogger();

	public void printFalsePositives(Processable<Correspondence<Laptop, Attribute>> correspondences, MatchingGoldStandard gs) {
		
		// go through the correspondences and check if they are incorrect
		for(Correspondence<Laptop, Attribute> c : correspondences.get()) {
			
			// is the match incorrect?
			if(gs.containsNegative(c.getFirstRecord(), c.getSecondRecord())) {
				
				// if yes, print the records to the console
				Laptop m1 = c.getFirstRecord();
				Laptop m2 = c.getSecondRecord();
				
				// print both records to the console
				logger.info("[Incorrect Correspondence]");
				logger.info(String.format("\t%s", m1));	
				logger.info(String.format("\t%s", m2));	
			}
		}		
	}
	
	public void printFalseNegatives(DataSet<Laptop, Attribute> ds1, DataSet<Laptop, Attribute> ds2, Processable<Correspondence<Laptop, Attribute>> correspondences, MatchingGoldStandard gs) {
		
		// first generate a set of all correct correspondences in the gold standard
		// (if a pair is not in the gold standard, we cannot say if its correct or not)
		Set<Pair<String,String>> allPairs = new HashSet<>();
		allPairs.addAll(gs.getPositiveExamples());
		
		// then go through the correspondences and remove all correct matches from the set
		for(Correspondence<Laptop, Attribute> c : correspondences.get()) {
			
			// create a pair of both record ids
			Pair<String, String> p1 = new Pair<>(c.getFirstRecord().getIdentifier(), c.getSecondRecord().getIdentifier());
			
			// create a second pair where record1 and record2 are switched 
			// (we don't know in which direction the ids were entered in the gold standard 
			Pair<String, String> p2 = new Pair<>(c.getSecondRecord().getIdentifier(), c.getFirstRecord().getIdentifier());
			
			// check if one of the pairs is in the set of correct matches
			if(allPairs.contains(p1) || allPairs.contains(p2)) {
				// if so, remove it
				allPairs.remove(p1);
				allPairs.remove(p2);
			}
		}
		
		// now, the remaining pairs in the set are those that were not found by the matching rule
		// we go through them and print them to the console
		for(Pair<String, String> p : allPairs) {
			
			// get the first record
			Laptop m1 = ds1.getRecord(p.getFirst());
			if(m1==null) {
				m1 = ds2.getRecord(p.getFirst());
			}
			
			// get the second record
			Laptop m2 = ds1.getRecord(p.getSecond());
			if(m2==null) {
				m2 = ds2.getRecord(p.getSecond());
			}
			
			// print both records to the console
			logger.info("[Missing Correspondence]");
			logger.info(String.format("\t%s", m1));	
			logger.info(String.format("\t%s", m2));	
		}
	}
}
