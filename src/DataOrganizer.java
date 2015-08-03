import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;

/* The purpose of this class is to organize the data created in the DataExtractor class in a more readable manner for
*  the analysis and database engines.
*/

public class DataOrganizer
{
    File organizedFile, dataFile = null;
    String organizedData = "";
    public DataOrganizer(File f) throws IOException {
        organizedFile = new File("C:\\Users\\Matt\\Stocks\\Stock Information\\Disney\\Results\\report_2_7_15\\" +
                "organizedData.txt");
        dataFile = new File("C:\\Users\\Matt\\Stocks\\Stock Information\\Disney\\Results\\report_2_7_15\\" +
                "data.txt");
        if (organizedFile != null)
            organize();
    }
    private void organize() throws IOException {
        getSymbol();
        getRecentPriceAndPE();
        getHighLow();
        getLastYearValues();
        writeToFile();
    }
    private void getSymbol() {
        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile)))
        {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("NYSE")) {
                    String symbol = line.substring(line.indexOf('-'));
                    symbol = symbol.substring(1, symbol.indexOf(' '));
                    organizedData += "Symbol: " + symbol + "\n";
                }
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    private void getRecentPriceAndPE() {
        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile)))
        {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("NYSE")) {
                    String str = line.replaceAll("[^.?0-9]+", " ");
                    if (str.indexOf(' ') == 0) {
                        str = str.substring(1, str.length());
                    }
                    organizedData += "Recent Price: " + str.substring(0, str.indexOf(' ')) + "\n";
                    organizedData += "P/E ratio: " + str.substring(str.indexOf(' ') + 1) + "\n";
                    break;
                }
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    private void getHighLow() {
        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile)))
        {
            boolean highLowLocation = false;
            int locationFound = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("High")) {
                    highLowLocation = true;
                }
                else if (line.contains("Low") && highLowLocation) {
                    locationFound = 1;
                }
                else if (locationFound == 1) {
                    organizedData += "High projection: " + line + "\n";
                    locationFound++;
                }
                else if (locationFound == 2) {
                    organizedData += "Low projection: " + line + "\n";
                    break;
                }
                else {
                    highLowLocation = false;
                }
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    private void getLastYearValues() {
        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile)))
        {
            ArrayList<Integer> years = new ArrayList<>();
            String line;

            while ((line = reader.readLine()) != null) {
               if (line.contains("VALUE LINE PUB")) {
                   int placeholder = 0;
                   boolean isDigit = false;
                   while (placeholder < line.length()) {
                       String y = line.substring(placeholder, line.indexOf(" ", placeholder));
                       if (y.contains("VALUE")) {
                           break;
                       }
                       placeholder = line.indexOf(" ", placeholder) + 1;
                       for (int i = 0; i < y.length(); i++) {
                           if (Character.isDigit(y.charAt(i))) {
                                isDigit = true;
                           }
                           else {
                               isDigit = false;
                               break;
                           }
                       }
                       if (isDigit)
                        years.add(Integer.parseInt(y));
                   }
               }
            }
            getFirstYearValues(years);
            Collections.sort(years);
            for (Integer y : years)
                organizedData += y + " ";
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void getFirstYearValues(ArrayList<Integer> years) {
        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile)))
        {
            String line;
            int low = years.get(0) - 2;
            int low2 = low - 1;
            int low3 = low2 - 1;
            int low4 = low3 - 1;

            while ((line = reader.readLine()) != null) {
                if (line.contains(String.valueOf(low)) && line.contains(String.valueOf(low2)) &&
                        line.contains(String.valueOf(low3)) && line.contains(String.valueOf(low4))) {
                    int placeholder = 0;
                    boolean isDigit = false;
                    int year = 0;
                    while (placeholder < line.length()) {
                        String y = line.substring(placeholder, line.indexOf(" ", placeholder));
                        placeholder = line.indexOf(" ", placeholder) + 1;
                        for (int i = 0; i < y.length(); i++) {
                            if (Character.isDigit(y.charAt(i))) {
                                isDigit = true;
                            }
                            else {
                                isDigit = false;
                                break;
                            }
                        }
                        if (isDigit)
                            years.add(Integer.parseInt(y));
                            year = Integer.parseInt(y);
                            break;
                    }
                    int next = year + 1;
                    while (!years.contains(next)) {
                        years.add(next);
                        next++;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void writeToFile() throws IOException {
        FileWriter writer = new FileWriter(organizedFile);
        try {
            writer.write(organizedData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            writer.close();
        }

    }
}
