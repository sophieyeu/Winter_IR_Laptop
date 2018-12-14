package de.uni_mannheim.informatik.dws.wdi.WDI_IR_Laptop;


import de.uni_mannheim.informatik.dws.winter.utils.WinterLogManager;
import org.apache.logging.log4j.Logger;
import com.github.jfasttext.JFastText;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Instances;
import weka.core.Utils;

import java.io.*;
import java.util.Date;

public class IR_Embeddings_WordVector {

    private static final Logger logger = WinterLogManager.activateLogger("trace");

    public static void main( String[] args ) throws Exception
    {


        // ======================    Unsupervised IR methods
        // ======================    Embeddings

//        learn embeddings -> feature vectors
//        learning input is laptop names from own corpus
//        problematic: overfitting as only "learning" from own corpus
//        if later time: learn embeddings with pretrained from fasttext with wikipedia data ("https://github.com/facebookresearch/fastText/blob/master/pretrained-vectors.md") 6,1GB!!!!!
//        to get 100-dimensional feature vectors from all laptop names
        logger.info("learning embeddings");
        JFastText jft = new JFastText();
        jft.runCmd(new String[]{
                "skipgram",
                "-input", "data/input/laptop_name.txt",
                "-output", "data/output/laptop_embeddings",
                "-bucket", "100",
                "-minCount", "1"
        });
        logger.info("Learned the features of laptops names of our corpus");

        //Deep learning neural network
        Date beginDate = new Date();
        //network variables
        String backPropOptions =
                "-L " + 0.1 //learning rate
                        + " -M " + 0 //momentum
                        + " -N " + 10000 //epoch
                        + " -V " + 0 //validation
                        + " -S " + 0 //seed
                        + " -E " + 0 //error
                        + " -H " + "3";
        // hidden nodes. //e.g. use "3,3" for 2 level hidden layer with 3 nodes

        try {
            //prepare historical data
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            BufferedReader reader = new BufferedReader(new InputStreamReader(classloader.getResourceAsStream("train_laptop_final_300.arff")));
            Instances trainingset = new Instances(reader);
            reader.close();

            trainingset.setClassIndex(trainingset.numAttributes() - 1);
            //final attribute in a line stands for output

            //network training
            MultilayerPerceptron mlp = new MultilayerPerceptron();
            mlp.setOptions(Utils.splitOptions(backPropOptions));
            mlp.buildClassifier(trainingset);
            System.out.println("final weights:");
            System.out.println(mlp);
            byte[] binaryNetwork = serialize(mlp);

            // writeToFile(binaryNetwork,  "C:\\Users\\User\\workspace\\Winter_IR_Dogfood\\data\\output\\");
            //display actual and forecast values
            System.out.println("\nactual\tprediction");
            for (int i = 0; i < trainingset.numInstances(); i++) {

                double actual = trainingset.instance(i).classValue();
                double prediction =
                        mlp.distributionForInstance(trainingset.instance(i))[0];

                System.out.println(actual+"\t"+prediction);

            }

            //success metrics
            System.out.println( "\nSuccess Metrics: ");
            Evaluation eval = new Evaluation(trainingset);
            eval.evaluateModel(mlp, trainingset);

            //display metrics
            System.out.println("Recall: " + eval.weightedRecall());
            System.out.println("Precision: " + eval.weightedPrecision());
            System.out.println("Correlation: "+eval.correlationCoefficient());
            System.out.println("MAE: "+eval.meanAbsoluteError());
            System.out.println("RMSE: "+eval.rootMeanSquaredError());
            System.out.println("RAE: "+eval.relativeAbsoluteError()+"%");
            System.out.println("RRSE: "+eval.rootRelativeSquaredError()+"%");
            System.out.println("Instances: "+eval.numInstances());
            Date endDate = new Date();

            System.out.println("\nprogram ends in " +(double)(endDate.getTime() - beginDate.getTime())/1000+" seconds\n");

        } catch (Exception ex) {

            System.out.println(ex);

        }
    }


    public static byte[] serialize(Object obj) throws Exception {

        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream o = new ObjectOutputStream(b);
        o.writeObject(obj);
        return b.toByteArray();

    }

    public static Object deserialize(byte[] bytes) throws Exception {

        ByteArrayInputStream b = new ByteArrayInputStream(bytes);

        ObjectInputStream o = new ObjectInputStream(b);

        return o.readObject();

    }

    public static void writeToFile(byte[] binaryNetwork, String dumpLocation) throws Exception {

        FileOutputStream stream = new FileOutputStream(dumpLocation + "trained_network2.txt");
        stream.write(binaryNetwork);
        stream.close();

    }

    public static MultilayerPerceptron readFromFile(String dumpLocation) {

        MultilayerPerceptron mlp = new MultilayerPerceptron();

        //binary network is saved to following file
        File file = new File(dumpLocation + "trained_network.txt");

        FileInputStream fileInputStream = null;

        //binary content will be stored in the binaryFile variable
        byte[] binaryFile = new byte[(int) file.length()];

        try {

            fileInputStream = new FileInputStream(file);
            fileInputStream.read(binaryFile);
            fileInputStream.close();

        } catch (Exception ex) {
            System.out.println(ex);
        }

        try {

            mlp = (MultilayerPerceptron) deserialize(binaryFile);

        } catch (Exception ex) {
            System.out.println(ex);
        }

        return mlp;


  }
}
