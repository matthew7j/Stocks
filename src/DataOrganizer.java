import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;

/* The purpose of this class is to organize the data created in the DataExtractor class in a more readable manner for
*  the analysis and database engines.
*/

public class DataOrganizer
{
    ArrayList<Integer> years;
    ArrayList<ArrayList<String>> table;
    ArrayList<ArrayList<String>> currentPosition;
    ArrayList<ArrayList<String>> annualRates;
    ArrayList<ArrayList<String>> quarterlyRevenues;
    ArrayList<ArrayList<String>> earningsPerShare;
    ArrayList<ArrayList<String>> quarterlyDividendsPaid;

    int original;
    int highestYear;

    File organizedFile, dataFile = null;
    String organizedData = "";
    public DataOrganizer(String path) throws IOException {
        organizedFile = new File(path + "\\organizedData.txt");
        dataFile = new File(path + "\\data.txt");
        if (organizedFile != null)
            organize();
    }
    private void organize() throws IOException {
        getSymbol();
        getRecentPriceAndPE();
        getHighLow();
        getLastYearValues();
        getRelativePERatioAndDividendYield();
        createTableArray();
        manipulateTableArray();
        organizeData();
        regulate();
        getCurrentPosition();
        getAnnualRates();
        getQuarterlyRevenues();
        getEarningsPerShare();
        getQuarterlyDividendsPaid();
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
                    while (str.indexOf(' ') == 0 || !Character.isDigit(str.charAt(0))) {
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
            years = new ArrayList<>();
            String line;

            while ((line = reader.readLine()) != null) {
               if (line.contains("VALUE LINE PUB")) {
                   int placeholder = 0;
                   boolean isDigit = false;
                   while (placeholder < line.length()) {
                       String y = line.substring(placeholder, line.indexOf(" ", placeholder));

                       placeholder = line.indexOf(" ", placeholder) + 1;
                       if (y.contains("LLC")) {
                           line += " ";
                           y = line.substring(placeholder, line.indexOf(" ", placeholder));
                           if (y.contains("-")) {
                               String year1 = y.substring(0, y.indexOf('-'));
                               String year2 = y.substring(y.indexOf('-') + 1);
                               int y1, y2;

                               y1 = Integer.parseInt(year1);
                               y2 = Integer.parseInt(year2);
                               y1 += 2000;
                               y2 += 2000;
                               years.add(y1);
                               years.add(y2);
                           }
                       }
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
            organizedData += "\n";
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
                    highestYear = next;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void getRelativePERatioAndDividendYield() {
        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile)))
        {
            String previousLine = "";
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("High:")) {
                    String next = reader.readLine();
                    if (next.contains("Low:")) {

                        /* The next line in the data text file should have 4 values, 1st is the median PE, 2nd is the relative
                         *  PE ratio, 3rd is the string YLD and 4th is the Div'd yield value */

                        int placeholder;
                        placeholder = previousLine.indexOf(" ") + 1;
                        String secondWord = previousLine.substring(placeholder, previousLine.indexOf(" ", placeholder));
                        placeholder = previousLine.indexOf(" ", placeholder) + 1;
                        previousLine.substring(placeholder, previousLine.indexOf(" ", placeholder));
                        placeholder = previousLine.indexOf(" ", placeholder) + 1;
                        String fourthWord = previousLine.substring(placeholder);
                        boolean isDigit = true;
                        for (int i = 0; i < secondWord.length(); i++) {
                            if (!Character.isDigit(secondWord.charAt(i)) && secondWord.charAt(i) != '.') {
                                isDigit = false;
                                break;
                            }
                        }
                        organizedData += "Relative P/E Ratio: ";
                        if (isDigit){
                            organizedData += secondWord + "\n";
                        }
                        else
                            organizedData += "\n";
                        isDigit = true;
                        for (int i = 0; i < fourthWord.length(); i++) {
                            if (!Character.isDigit(fourthWord.charAt(i)) && fourthWord.charAt(i) != '.' &&
                                    fourthWord.charAt(i) != '%') {
                                isDigit = false;
                                break;
                            }
                        }
                        organizedData += "Dividend Yield: ";
                        if (isDigit){
                            organizedData += fourthWord + "\n";
                        }
                        else
                            organizedData += "\n";
                    }
                }
                previousLine = line;
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /* The next block of methods are for the bulk of the PDF forms. */

    private void createTableArray() {
        table = new ArrayList<>();
        ArrayList<String> yearList = new ArrayList<>();

        boolean foundFirst = false, foundSecond = false;

        for (Integer year : years) {
            yearList.add(year.toString());
        }
        table.add(yearList);

        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile)))
        {
            String line;
            boolean found = false;
            while ((line = reader.readLine()) != null) {
                if (line.contains(yearList.get(0)) && line.contains(yearList.get(1)) && line.contains(yearList.get(2))) {
                    String dataLine = reader.readLine();
                    while (isAllNumbers(dataLine) && dataLine.length() > 5) {
                        if (!checkForOutliers(dataLine)) {
                            ArrayList<String> tempArrayList = new ArrayList<>();
                            int placeholder = 0;
                            dataLine += ' ';
                            while (placeholder < dataLine.length()) {
                                tempArrayList.add(dataLine.substring(placeholder, dataLine.indexOf(' ', placeholder)));
                                placeholder = dataLine.indexOf(' ', placeholder) + 1;
                            }
                            table.add(tempArrayList);
                            foundFirst = true;
                        }
                        found = true;
                        dataLine = reader.readLine();
                    }
                    if (found)
                    {
                        int length = 0;
                        while ((dataLine = reader.readLine()) != null && length < 6) {
                            if (!checkForOutliers(dataLine) && isAllNumbers(dataLine)) {
                                ArrayList<String> tempArrayList = new ArrayList<>();
                                int placeholder = 0;
                                dataLine += ' ';
                                while (placeholder < dataLine.length()) {
                                    tempArrayList.add(dataLine.substring(placeholder, dataLine.indexOf(' ', placeholder)));
                                    placeholder = dataLine.indexOf(' ', placeholder) + 1;
                                }
                                table.add(tempArrayList);
                            }
                            else {
                                length++;
                            }
                        }
                    }

                }
                else if ((line.contains("-")) && (yearList.get(yearList.size() - 1).contains(line.substring(line.lastIndexOf('-') + 1))) && (
                        yearList.get(yearList.size() - 2).contains(line.substring(line.indexOf("LLC") + 4, line.lastIndexOf('-')))))
                {
                    String dataLine;
                    boolean isBlankFoundFirst = false;
                    boolean isBlankFoundSecond = false;
                    String before;
                    int i = 1;
                    original = table.size();
                    dataLine = reader.readLine();
                    while ((isAllNumbers((dataLine).substring(0, dataLine.indexOf(' '))) && i < (original))) {
                        if (dataLine.contains("Bold figures are")) {
                            dataLine = dataLine.replace("Bold figures are", "");
                            isBlankFoundFirst = true;
                        } else if (dataLine.contains("Value Line")) {
                            dataLine = dataLine.replace("Value Line", "");
                            isBlankFoundSecond = true;
                        } else if (isBlankFoundFirst && isBlankFoundSecond) {
                            before = dataLine + " ";

                            dataLine = reader.readLine();
                            if (dataLine.contains("estimates"))
                                dataLine = reader.readLine();
                            before += dataLine;
                            dataLine = before;
                            isBlankFoundFirst = false;
                            isBlankFoundSecond = false;
                        }

                        int placeholder = 0;
                        dataLine += ' ';
                        while (placeholder < dataLine.length()) {
                            table.get(i).add(dataLine.substring(placeholder, dataLine.indexOf(' ', placeholder)));
                            placeholder = dataLine.indexOf(' ', placeholder) + 1;
                        }
                        i++;
                        foundSecond = true;

                        dataLine = reader.readLine();
                        if (dataLine.contains("C") && dataLine.length() < 5) {
                            dataLine = reader.readLine();
                            if (isAllNumbers(dataLine)) {
                                table.get(i - 1).add(dataLine);
                                dataLine = reader.readLine();
                            }
                        }
                    }
                }
            }
            reader.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile)))
        {
            String line;
            int len = table.size() - 1;
            while ((line = reader.readLine()) != null) {
                line += ' ';
                if (len < 18) {
                    if (line.substring(0, line.indexOf(' ')).contains(table.get(len).get(0))) {
                        if (foundFirst && foundSecond && isAllNumbers(line)) {
                            line = reader.readLine() + " ";
                            boolean start = false;
                            while (!start) {
                                while (isAllNumbers(line)) {
                                    start = true;
                                    ArrayList<String> tempArrayList = new ArrayList<>();
                                    int placeholder = 0;
                                    if (line.charAt(line.length() - 1) != ' ') {
                                        line += ' ';
                                    }
                                    while (placeholder < line.length()) {
                                        tempArrayList.add(line.substring(placeholder, line.indexOf(' ', placeholder)));
                                        placeholder = line.indexOf(' ', placeholder) + 1;
                                    }
                                    table.add(tempArrayList);
                                    line = reader.readLine();
                                }
                                line = reader.readLine();
                            }
                        }
                    }
                    else {
                        line = line.replaceAll("\\s+$", "");
                        if (line.substring(line.lastIndexOf(' ') + 1).contains(table.get(len).get(table.get(len).size() - 1))) {
                            if (line.contains(table.get(len).get(table.get(len).size() - 1)) &&
                                    line.contains(table.get(len).get(table.get(len).size() - 2))) {
                                int i = len + 1;
                                if (foundFirst && foundSecond) {
                                    boolean start = false;
                                    while (!start) {
                                        while (isAllNumbers((line = reader.readLine()).substring(0, line.indexOf(' ')))) {
                                            start = true;
                                            int placeholder = 0;
                                            if (line.charAt(line.length() - 1) != ' ') {
                                                line += ' ';
                                            }
                                            while (placeholder < line.length()) {
                                                table.get(i).add(line.substring(placeholder, line.indexOf(' ', placeholder)));
                                                placeholder = line.indexOf(' ', placeholder) + 1;
                                            }
                                            i++;
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            System.out.println(e);
        }
    }
    private void manipulateTableArray() {
        putTitleIntoOne();
        table = putDashesIntoTwos(table);
        fillInBlanks();
    }
    private void putTitleIntoOne(){
        ArrayList<ArrayList<String>> temp = new ArrayList<>();
        boolean foundTitle = false;
        String title = "";
        for (int i = 0; i < table.size(); i++) {
            ArrayList<String> tempList = new ArrayList<>();
            for (int j = 0; j < table.get(i).size(); j++) {
                if (isAllNumbers(table.get(i).get(j)) || table.get(i).get(j).contains("Nil") || table.get(i).get(j) == "NMF") {
                    if (foundTitle) {
                        tempList.add(title);
                        foundTitle = false;
                        title = "";
                    }
                    tempList.add(table.get(i).get(j));
                }
                else {
                    title += table.get(i).get(j) + " ";
                    foundTitle = true;
                }
            }
            temp.add(tempList);
        }
        table.clear();
        table = temp;
    }
    private ArrayList<ArrayList<String>> putDashesIntoTwos(ArrayList<ArrayList<String>> a) {
        ArrayList<ArrayList<String>> temp = new ArrayList<>();
        for (int i = 0; i < a.size(); i++) {
            ArrayList<String> tempList = new ArrayList<>();
            for (int j = 0; j < a.get(i).size(); j++) {
                if (a.get(i).get(j).equals("-")) {
                    if (a.get(i).get(j + 1).equals("-")) {
                        tempList.add("--");
                        j = j + 1;
                    }
                }
                else {
                    tempList.add(a.get(i).get(j));
                }
            }
            temp.add(tempList);
        }
        a = temp;
        return a;
    }
    private void fillInBlanks() {
        for (int i = 1; i < table.size(); i++) {
            if (table.get(i).size() != table.get(0).size()) {
                // It is probably the blank values for the current year for P/E and Div yield
                if (table.get(i).size() == table.get(0).size() - 1 &&
                        (table.get(i).get(table.get(i).size() - 2).contains("P/E")) ||
                        table.get(i).get(table.get(i).size() - 2).contains("Yield")){


                    table.get(i).add(table.get(i).size() - 2, "BLANK");
                }
                // It is probably the values gathered from the lesser number of years
            }
        }
    }
    private boolean isAllNumbers(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i)) && s.charAt(i) != '.' && s.charAt(i) != '%' && s.charAt(i) != 'd'
                    && s.charAt(i) != ' ' && s.charAt(i) != '-' && !s.contains("NMF") && !s.contains("Nil") && (!s.contains("C") || s.length() >= 3))
                return false;
        }
        return true;
    }

    private boolean checkForOutliers(String s) {
        try {
            if (years.contains(Integer.parseInt(s.substring(0, s.indexOf(' '))))) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private void organizeData() {
        for (int i = 1; i < table.size(); i++) {
            Collections.swap(table.get(i), table.get(i).size() - 1, table.get(i).size() - 2);
            if (table.get(i).size() > table.get(0).size()) {
                if (table.get(i).get(table.get(i).size() - 1).equals("C") || table.get(i).get(table.get(i).size() - 1).length() < 3) {
                    Collections.swap(table.get(i), table.get(i).size() - 2, table.get(i).size() - 3);
                    table.get(i).remove(table.get(i).size() - 1);
                }
            }
        }
    }

    private void regulate() {
        for (int i = 1; i < table.size(); i++) {
            String s = table.get(i).get(table.get(i).size() - 1);

            String firstWord = s.substring(0, s.indexOf(' '));
            String secondWord = s.split("\\s+")[1];

            if (s.contains("Revenues per")) {
                table.get(i).remove(table.get(i).size() - 1);
                table.get(i).add("Revenues per share");
            }
            else if (s.contains("Cash Flow")) {
                table.get(i).remove(table.get(i).size() - 1);
                table.get(i).add("Cash Flow per share");
            }
            else if (s.contains("Earnings per")) {
                table.get(i).remove(table.get(i).size() - 1);
                table.get(i).add("Earnings per share");
            }
            else if (firstWord.contains("Div") &&
                    secondWord.contains("Decl")) {
                table.get(i).remove(table.get(i).size() - 1);
                table.get(i).add("Dividends Declared per share");
            }
            else if (firstWord.contains("Cap") &&
                     secondWord.contains("Spending")) {
                table.get(i).remove(table.get(i).size() - 1);
                table.get(i).add("Capital Spending per share");
            }
            else if (s.contains("Book Value")) {
                table.get(i).remove(table.get(i).size() - 1);
                table.get(i).add("Book Value per share");
            }
            else if (s.contains("Common Shs")) {
                table.get(i).remove(table.get(i).size() - 1);
                table.get(i).add("Common Shares Outstanding");
            }
            else if (firstWord.contains("Avg") &&
                    secondWord.contains("Ann") &&
                    s.contains("P/E")) {
                table.get(i).remove(table.get(i).size() - 1);
                table.get(i).add("Average Annual P/E Ratio");
            }
            else if (s.contains("Relative P/E")) {
                table.get(i).remove(table.get(i).size() - 1);
                table.get(i).add("Relative P/E Ratio");
            }
            else if (firstWord.contains("Avg") &&
                    secondWord.contains("Ann") &&
                    s.contains("Yield")) {
                table.get(i).remove(table.get(i).size() - 1);
                table.get(i).add("Average Annual Dividend Yield");
            }
            else if (firstWord.contains("Working") &&
                    secondWord.contains("Cap")) {
                table.get(i).remove(table.get(i).size() - 1);
                table.get(i).add("Working Capital ($mill)");
            }
            else if (s.contains("Revenues ($mill)")) {
                table.get(i).remove(table.get(i).size() - 1);
                table.get(i).add("Revenues ($mill)");
            }
            else if (s.contains("Shr. Equity")) {
                table.get(i).remove(table.get(i).size() - 1);
                table.get(i).add("Share Equity ($mill)");
            }
            else if (s.contains("Return on Total")) {
                table.get(i).remove(table.get(i).size() - 1);
                table.get(i).add("Return on Total Capital");
            }
            else if (s.contains("Return on Shr. Equity")) {
                table.get(i).remove(table.get(i).size() - 1);
                table.get(i).add("Return on Share Equity");
            }
            else if (s.contains("Retained to Com Eq")) {
                table.get(i).remove(table.get(i).size() - 1);
                table.get(i).add("Retained to Common Equity");
            }
            else if (firstWord.contains("All") &&
                    secondWord.contains("Div") &&
                    s.contains("Net Prof")) {
                table.get(i).remove(table.get(i).size() - 1);
                table.get(i).add("All Dividends to Net Profit");
            }
        }
    }

    private void getCurrentPosition() {
        currentPosition = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("CURRENT POSITION")) {
                    while (!(line = reader.readLine()).contains("Current Liab.")) {
                        ArrayList<String> a = new ArrayList<>();
                        if (line.contains("$MILL")) {
                            a.add("CURRENT POSITION ($MILL)");
                        }
                        else {
                            int placeholder = 0;
                            if (line.charAt(line.length() - 1) != ' ')
                                line += ' ';
                            while (placeholder < line.length() - 1) {
                                String s = line.substring(placeholder, line.indexOf(' ', placeholder));
                                placeholder = line.indexOf(' ', placeholder) + 1;
                                a.add(s);
                            }
                            currentPosition.add(a);
                        }
                    }
                    ArrayList<String> a = new ArrayList<>();
                    int placeholder = 0;
                    if (line.charAt(line.length() - 1) != ' ')
                        line += ' ';
                    while (placeholder < line.length() - 1) {
                        String s = line.substring(placeholder, line.indexOf(' ', placeholder));
                        placeholder = line.indexOf(' ', placeholder) + 1;
                        a.add(s);
                    }
                    currentPosition.add(a);
                }
            }
            currentPosition = putDashesIntoTwos(currentPosition);
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    private void getAnnualRates() {
        annualRates = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
            String line;
            boolean past1bool = false;
            String past1 = " ";
            String past2 = " ";
            String years = " ";
            while ((line = reader.readLine()) != null) {
                if (line.contains("ANNUAL RATES")) {
                    ArrayList<String> temp = new ArrayList<>();
                    String[] words = line.split("\\s+");
                    for (int i = 0; i < words.length; i++) {
                        if (!words[i].contains("ANNUAL") && !words[i].contains("RATES")) {
                            if (words[i].contains("Past") && past1bool == false) {
                                past1 += "Past ";
                                past1bool = true;
                            }
                            else if (words[i].contains("Past") && past1bool == true) {
                                past2 += "Past ";
                            }
                            else {
                                years += words[i] + " ";
                            }
                        }
                    }
                    line = reader.readLine();
                    String[] words2 = line.split("\\s+");
                    for (int i = 0; i < words2.length; i++) {
                        if (words2[i].contains("sh)")) {
                            i = i + 1;
                            if (isAllNumbers(words2[i])) {
                                past1 += words2[i] + " years";
                                i = i + 2;
                                if (isAllNumbers(words2[i])) {
                                    past2 += words2[i] + " years";
                                    i = i + 2;
                                    if (words2[i].contains("to")) {
                                        years += " to " + words2[i + 1];
                                    }
                                }
                            }
                        }
                    }
                    temp.add(past1);
                    temp.add(past2);
                    temp.add(years);
                    annualRates.add(temp);
                    while (!(line = reader.readLine()).contains("Book Value")) {
                        ArrayList<String> a = new ArrayList<>();
                        int placeholder = 0;
                        if (line.charAt(line.length() - 1) != ' ')
                            line += ' ';
                        while (placeholder < line.length() - 1) {
                            String s = line.substring(placeholder, line.indexOf(' ', placeholder));
                            placeholder = line.indexOf(' ', placeholder) + 1;
                            a.add(s);
                        }
                        annualRates.add(a);
                    }
                    ArrayList<String> a = new ArrayList<>();
                    int placeholder = 0;
                    if (line.charAt(line.length() - 1) != ' ')
                        line += ' ';
                    while (placeholder < line.length() - 1) {
                        String s = line.substring(placeholder, line.indexOf(' ', placeholder));
                        placeholder = line.indexOf(' ', placeholder) + 1;
                        a.add(s);
                    }
                    annualRates.add(a);
                }
            }
            annualRates = putDashesIntoTwos(annualRates);
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    private void getQuarterlyRevenues() {
        quarterlyRevenues = new ArrayList();
        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.contains("QUARTERLY REVENUES")) {
                    if (line.contains("mill")) {
                        ArrayList<String> title = new ArrayList<>();
                        title.add("Quarterly Revenues ($mill)");
                    }
                    line = reader.readLine();
                    line += " ";
                    if (line.contains(".")) {
                        ArrayList<String> months = new ArrayList<>();
                        String[] monthList = line.split("\\s+");
                        for (int i = 0; i < monthList.length; i++) {
                            if (monthList[i].contains(".")) {
                                months.add(monthList[i].substring(0, monthList[i].indexOf('.')));
                            }
                        }
                        quarterlyRevenues.add(months);
                    }
                    while (!line.contains("Ends") && !line.contains("EARNINGS")) {
                        if (isAllNumbers(line)) {
                            ArrayList<String> values = new ArrayList<>();
                            String[] valueList = line.split("\\s+");
                            if (valueList.length > 2) {
                                for (int i = 0; i < valueList.length; i++) {
                                    values.add(valueList[i]);
                                }
                                quarterlyRevenues.add(values);
                            }
                        }
                        line = reader.readLine();
                    }
                }
            }
            quarterlyRevenues = putDashesIntoTwos(quarterlyRevenues);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getEarningsPerShare() {
        earningsPerShare = new ArrayList();
        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.contains("EARNINGS PER SHARE")) {
                    ArrayList<String> title = new ArrayList<>();
                    title.add("Earnings per Share");

                    line = reader.readLine();
                    line += " ";
                    if (line.contains(".")) {
                        ArrayList<String> months = new ArrayList<>();
                        String[] monthList = line.split("\\s+");
                        for (int i = 0; i < monthList.length; i++) {
                            if (monthList[i].contains(".")) {
                                months.add(monthList[i].substring(0, monthList[i].indexOf('.')));
                            }
                        }
                        earningsPerShare.add(months);
                    }
                    while (!line.contains("QUARTERLY")) {
                        if (isAllNumbers(line)) {
                            ArrayList<String> values = new ArrayList<>();
                            String[] valueList = line.split("\\s+");
                            if (valueList.length > 2) {
                                for (int i = 0; i < valueList.length; i++) {
                                    values.add(valueList[i]);
                                }
                                earningsPerShare.add(values);
                            }
                        }
                        line = reader.readLine();
                    }
                }
            }
            earningsPerShare = putDashesIntoTwos(earningsPerShare);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getQuarterlyDividendsPaid() {
        quarterlyDividendsPaid = new ArrayList();
        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.contains("QUARTERLY DIVIDENDS PAID")) {
                    ArrayList<String> title = new ArrayList<>();
                    title.add("Quarterly Dividends Paid");
                    quarterlyDividendsPaid.add(title);
                    line = reader.readLine();
                    line += " ";
                    if (line.contains(".")) {
                        ArrayList<String> months = new ArrayList<>();
                        String[] monthList = line.split("\\s+");
                        for (int i = 0; i < monthList.length; i++) {
                            if (monthList[i].contains(".")) {
                                months.add(monthList[i].substring(0, monthList[i].indexOf('.')));
                            }
                        }
                        quarterlyDividendsPaid.add(months);
                    }
                    int count = 0;
                    while (!line.contains("Trailing") && count < 7) {
                        if (isAllNumbers(line)) {
                            ArrayList<String> values = new ArrayList<>();
                            String[] valueList = line.split("\\s+");
                            if (valueList.length > 2) {
                                for (int i = 0; i < valueList.length; i++) {
                                    values.add(valueList[i]);
                                }
                                quarterlyDividendsPaid.add(values);
                            }
                        }
                        line = reader.readLine();
                        count++;
                    }
                }
            }
            quarterlyDividendsPaid = putDashesIntoTwos(quarterlyDividendsPaid);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeToFile() throws IOException {
        try (FileWriter writer = new FileWriter(organizedFile)) {
            writer.write(organizedData + "\n\n");
            for (ArrayList<String> aTable : table) {
                if (aTable.size() < table.get(0).size()) {
                    int i = table.get(0).size() - aTable.size();
                    for (int j = 0; j < i; j++) {
                        writer.write("\t\t\t");
                    }
                }
                for (String anATable : aTable) {
                    if (anATable.length() < 3) {
                        writer.write(anATable + " \t\t\t");
                    }
                    else if (anATable.length() < 4)
                        writer.write(anATable + " \t\t");
                    else
                        writer.write(anATable + "\t\t");
                }
                writer.write("\n\n");
            }
            writer.write("\nCurrent Position: \n");
            for (ArrayList<String> aTable : currentPosition) {
                if (aTable.size() < currentPosition.get(0).size()) {
                    int i = currentPosition.get(0).size() - aTable.size();
                    for (int j = 0; j < i; j++) {
                        writer.write("\t\t\t");
                    }
                }
                for (String anATable : aTable) {
                    if (anATable.length() < 3) {
                        writer.write(anATable + " \t\t\t");
                    }
                    else if (anATable.length() < 4)
                        writer.write(anATable + " \t\t");
                    else
                        writer.write(anATable + "\t\t");
                }
                writer.write("\n");
            }
            writer.write("\nAnnual Rates: \n");
            for (ArrayList<String> aTable : annualRates) {
                if (aTable.size() < annualRates.get(0).size()) {
                    int i = annualRates.get(0).size() - aTable.size();
                    for (int j = 0; j < i; j++) {
                        writer.write("\t\t\t");
                    }
                }
                for (String anATable : aTable) {
                    if (anATable.length() < 3) {
                        writer.write(anATable + " \t\t\t");
                    }
                    else if (anATable.length() < 4)
                        writer.write(anATable + " \t\t");
                    else
                        writer.write(anATable + "\t\t");
                }
                writer.write("\n");
            }
            writer.write("\nQuarterly Revenues: \n");
            for (ArrayList<String> aTable : quarterlyRevenues) {
                if (aTable.size() < quarterlyRevenues.get(0).size()) {
                    int i = quarterlyRevenues.get(0).size() - aTable.size();
                    for (int j = 0; j < i; j++) {
                        writer.write("\t\t\t");
                    }
                }
                for (String anATable : aTable) {
                    if (anATable.length() < 3) {
                        writer.write(anATable + " \t\t\t");
                    }
                    else if (anATable.length() < 4)
                        writer.write(anATable + " \t\t");
                    else
                        writer.write(anATable + "\t\t");
                }
                writer.write("\n");
            }
            writer.write("\nEarnings per Share: \n");
            for (ArrayList<String> aTable : earningsPerShare) {
                if (aTable.size() < earningsPerShare.get(0).size()) {
                    int i = earningsPerShare.get(0).size() - aTable.size();
                    for (int j = 0; j < i; j++) {
                        writer.write("\t\t\t");
                    }
                }
                for (String anATable : aTable) {
                    if (anATable.length() < 3) {
                        writer.write(anATable + " \t\t\t");
                    }
                    else if (anATable.length() < 4)
                        writer.write(anATable + " \t\t");
                    else
                        writer.write(anATable + "\t\t");
                }
                writer.write("\n");
            }
            writer.write("\n");
            for (ArrayList<String> aTable : quarterlyDividendsPaid) {
                if (aTable.size() < quarterlyDividendsPaid.get(0).size()) {
                    int i = quarterlyDividendsPaid.get(0).size() - aTable.size();
                    for (int j = 0; j < i; j++) {
                        writer.write("\t\t\t");
                    }
                }
                for (String anATable : aTable) {
                    if (anATable.length() < 3) {
                        writer.write(anATable + " \t\t\t");
                    }
                    else if (anATable.length() < 4)
                        writer.write(anATable + " \t\t");
                    else
                        writer.write(anATable + "\t\t");
                }
                writer.write("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
