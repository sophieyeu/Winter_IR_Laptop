package de.uni_mannheim.informatik.dws.wdi.ExerciseIdentityResolution.model;

import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.io.XMLMatchableReader;
import org.w3c.dom.Node;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class LaptopXMLReader extends XMLMatchableReader<Laptop, Attribute> {
    private static String[] punctuation = new String[] {
            "\\.", ",", ":", "/", "\\\\", "'", "\"", "!", "\\?",
            "@", "#", "$", "%", "\\^", "&", "\\*", "\\(", "\\)",
            "\\[", "\\]", "\\{", "\\}", "\\|", " - ", ";", "\\+" };

    private static List<String> stopwords = new ArrayList<>();

    public LaptopXMLReader() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader("data/stopwords/nltk.txt"));
//            List<String> stopwords = new ArrayList<String>();
            String word;
            while ((word = bufferedReader.readLine()) != null) {
                stopwords.add(word);
            }
        } catch (Exception e) {
            System.out.println("Stopwords were not loaded");
        }
    }



    @Override
    public Laptop createModelFromElement(Node node, String provenanceInfo) {

        // create the object with id and provenance information
        Laptop electronic = new Laptop(getValueFromChildElement(node, "productid"), provenanceInfo);

        // fill the attributes
        electronic.setClusterid(getValueFromChildElement(node, "clusterid"));
        electronic.setProductname(getValueFromChildElement(node, "productname"));

        String rawSize = getValueFromChildElement(node, "size");
        Integer size = rawSize != null && !rawSize.isEmpty() ? Integer.parseInt(rawSize) : null;
        electronic.setSize(size);

        return electronic;
    }

    private String preprocessName(String name) {
        if (name == "" || name == null) {
            return name;
        }

        name = name.toLowerCase();



        // Remove punctuation
        for (String symbol: punctuation) {
            name = name.replaceAll(symbol, "");
        }


        // Remove stopwords - NLTK stopwords list
        List<String> name_tokens = new ArrayList<String>(Arrays.asList(name.split(" ")));
        List<String> cleaned_name_tokens = new ArrayList<String>();
        for (int i = 0; i < name_tokens.size(); i++) {
            boolean out_of_nltk = true;
            String name_part = name_tokens.get(i);
            for (String word: stopwords) {

                if (name_part.equals(word)) {
                    out_of_nltk = false;
                }
            }

            if (out_of_nltk) {
                cleaned_name_tokens.add(name_tokens.get(i));
            }
        }

        String clean_name = String.join(" ", cleaned_name_tokens);
        return clean_name;
    }


}
