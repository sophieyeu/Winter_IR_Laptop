package de.uni_mannheim.informatik.dws.wdi.WDI_IR_Laptop.model;

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
        Laptop product = new Laptop(getValueFromChildElement(node, "productid"), provenanceInfo);

        // fill the attributes
        product.setClusterid(getValueFromChildElement(node, "clusterid"));

        String rawSize = getValueFromChildElement(node, "size");
        Integer size = rawSize != null && !rawSize.isEmpty() ? Integer.parseInt(rawSize) : null;
        product.setSize(size);

        String productname = getValueFromChildElement(node, "productname");
        productname = lowerCase(productname);
        productname = removePunctuation(productname);
        productname = removeStopwords(productname);
        product.setProductname(productname);

        String brand = getValueFromChildElement(node, "brand");
        brand = lowerCase(brand);
        product.setBrand(brand);

        String description = getValueFromChildElement(node, "description");
        description = lowerCase(description);
        description = removePunctuation(description);
        description = removeStopwords(description);
        product.setDescription(description);


        String gtin = getValueFromChildElement(node, "gtin");
        gtin = lowerCase(gtin);
        product.setMpn(gtin);

        String mpn = getValueFromChildElement(node, "mpn");
        mpn = lowerCase(mpn);
        product.setMpn(mpn);

        String sku = getValueFromChildElement(node, "sku");
        sku = lowerCase(sku);
        product.setSku(sku);

        String currency = getValueFromChildElement(node, "currency");
        currency = lowerCase(currency);
        product.setCurrency(currency);

        String rawPrice = getValueFromChildElement(node, "price");
        Double price = rawPrice != null && !rawPrice.isEmpty() ? Double.parseDouble(rawPrice) : null;
        price = convertToUsd(price, currency);
        product.setPrice(price);


        return product;
    }

    private String removeStopwords(String str) {
        if (str == "" || str == null) {
            return str;
        }

        // Remove stopwords - NLTK stopwords list
        List<String> name_tokens = new ArrayList<String>(Arrays.asList(str.split(" ")));
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

    private String removePunctuation(String str) {
        if (str == "" || str == null) {
            return str;
        }

        // Remove punctuation
        for (String symbol: punctuation) {
            str = str.replaceAll(symbol, "");
        }

        return str;
    }

    private String lowerCase(String str) {
        if (str == "" || str == null) {
            return str;
        }

        str = str.toLowerCase();

        return str;
    }

    private String removeFileExtension(String str) {
        if (str == "" || str == null) {
            return str;
        }

        return str.replace(".html", "");
    }

//    private Double poundsToKg(Double pounds) {
//        if (pounds == null || pounds < 0) {
//            System.out.println("Amount is wrong: " + pounds != null ? pounds : "NULL");
//            return null;
//        }
//
//        return round(pounds * 0.4535,1);
//    }
//
    private static double round(double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }

    private Double convertToUsd(Double amount, String currency) {
        if (currency == null || currency.isEmpty()) {
            System.out.println("Provide the currency name");
            return null;
        }

        if (amount == null || amount < 0) {
            System.out.println("Amount is wrong: " + amount != null ? amount : "NULL");
            return null;
        }

        currency = currency.toLowerCase();

        switch (currency) {
            case "aud": return round(0.73 * amount, 2);
            case "gbp": return round(1.27 * amount, 2);
            case "cad": return round(0.75 * amount, 2);
            case "eur": return round(1.13 * amount, 2);
            case "peso": return round(0.019 * amount, 2);
        }

        return amount;
    }
}
