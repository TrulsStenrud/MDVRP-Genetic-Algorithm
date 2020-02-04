package DataFiles;

import Stuff.Customer;
import Stuff.Depot;
import Stuff.Problem;
import Types.Phenotype;
import javafx.geometry.Point2D;

import javax.management.InvalidAttributeValueException;
import java.io.*;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FileParser {
    private static String dataFolder = "./TaskFiles";
    private static String solutionFolder = "./SolutionFiles";
    private static DecimalFormat format = new DecimalFormat("##.##");

    public static void write(String fileName, Phenotype result) {
        var dataFile = new File(solutionFolder + "/" + fileName);

        try {
            innerWriter(dataFile, result);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void innerWriter(File dataFile, Phenotype result) throws IOException {
        var writer = new FileWriter(dataFile);

        var newWriter = new BufferedWriter(writer);

        newWriter.write(format.format(result.fitness()));
        newWriter.newLine();

        for(int i = 0; i < result.FML.size(); i++){

            var depot = result.FML.get(i);
            for(int j = 0 ; j < depot.size(); j++){
                var current = depot.get(j);
                var sb = new StringBuilder();
                sb.append(i+1).append("\t");
                sb.append(j+1).append("\t");

                sb.append(format.format(result.getRouteCost(current, i))).append("\t");
                sb.append(format.format(result.getRouteLoad(current))).append("\t");


                sb.append("0 ");

                for(var c : current)
                    sb.append(c).append(" ");

                sb.append("0 ");

                newWriter.write(sb.toString());
                newWriter.newLine();
            }
        }

        newWriter.flush();

    }

    public static String[] getFiles(){
        File[] listOfFiles = new File(dataFolder).listFiles();

        if(listOfFiles == null)
            return new String[0];

        return Arrays.stream(listOfFiles).map(File::getName).filter(x -> !x.contains(".DS_Store")).toArray(String[]::new);
    }

    public static Problem readParseFile(String file){
        var dataFile = new File(dataFolder + "/" + file);
        try {
            return innerParse(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null; //TODO return something containing error message
        }
    }

    private enum State{
        FirstLine,
        DepotInformation,
        CustomerInformation,
        DepotLocation,
        Finish
    }

    private static Problem innerParse(File dataFile) throws IOException {
        int m = 0;
        int n = 0;
        int t = 0;

        var state = State.FirstLine;
        int counter = 0;

        List<Depot> depots = new ArrayList<>();
        List<Customer> customers = new ArrayList<>();

        var innerReader = new FileReader(dataFile);
        BufferedReader reader = new BufferedReader(innerReader);
        String line;
        while ((line = reader.readLine()) != null){
            Integer[] values = null;
            try{
                values = Arrays.stream(line.trim().split("\\s+")).map(Integer::parseInt).toArray(Integer[]::new);

            }catch (NumberFormatException e){
                int stop = 2;
            }

            switch (state){
                case FirstLine:
                    m = values[0];
                    n = values[1];
                    t = values[2];

                    state = State.DepotInformation;
                    break;

                case DepotInformation:
                    counter++;
                    depots.add(new Depot(m, values[0], values[1]));

                    if (counter == t){
                        state = State.CustomerInformation;
                        counter = 0;
                    }

                    break;

                case CustomerInformation:
                    counter++;
                    customers.add(new Customer(new Point2D(values[1], values[2]), values[3], values[4]));

                    if(counter == n){
                        state = State.DepotLocation;
                        counter =0;
                    }

                    break;


                case DepotLocation:
                    counter++;
                    depots.get(values[0] - n - 1).point = new Point2D(values[1], values[2]);

                    if(counter == t){
                        state = State.Finish;
                        counter = 0;
                    }
            }

        }

        if(depots.size() != t)
            System.out.println("Wrong number of depots");

        if(customers.size() != n)
            System.out.println("Wrong number of customers, " + customers.size() + " should be " + n);

        return new Problem(depots, customers);
    }
}
