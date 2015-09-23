import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;

public class DatabaseEngine
{
    File f;
    ArrayList<Integer> years;
    ArrayList<String> valuesToReport;
    double recentPrice, peRatio, highProj, lowProj, relativePERatio, dividendYield,
            timeliness, safety, technical, priceStability, growthPersistence, predictability;

    int stockID;

    String totalDebt, LTDebt, LTInterest, commonStock, marketCap, stockSymbol, stockName, financialStrength;

    ArrayList<ArrayList<String>> concreteYearData;
    ArrayList<ArrayList<String>> futureYearData;
    ArrayList<String> titlesData;
    ArrayList<ArrayList<String>> currentPosition;
    ArrayList<ArrayList<String>> quarterlyRevenues;
    ArrayList<ArrayList<String>> earningsPerShare;
    ArrayList<ArrayList<String>> quarterlyDividendsPaid;
    ArrayList<ArrayList<String>> annualRates;

    public DatabaseEngine(File f) {
        this.f = f;
        //fillValuesToReport();
        createDatabase();
        populateQuartersTable();

        getStockNameAndSymbol();
        getYears();
        getValues();
        getCurrentPosition();
        getYearData();
        getQuarterlyRevenues();
        getEarningsPerShare();
        getQuarterlyDividendsPaid();
        getAnnualRates();
        getConcreteDataArrayListReady();

        addStock();
        getStockID();
        addYears();
        addConcreteYearData();
    }

    private void fillValuesToReport(){
        valuesToReport.add("Revenues per Share");
        valuesToReport.add("Cash Flow per share");
        valuesToReport.add("Earnings per share");
        valuesToReport.add("Dividends Declared per share");
        valuesToReport.add("Capital Spending per share");
        valuesToReport.add("Book Value per share");
        valuesToReport.add("Common Shares Outstanding");
        valuesToReport.add("Average Annual P/E Ratio");
        valuesToReport.add("Relative P/E Ratio");
        valuesToReport.add("Average Annual Dividend Yield");
        valuesToReport.add("Revenues ($mill)");
        valuesToReport.add("Operating Margin");
        valuesToReport.add("Depreciation ($mill)");
        valuesToReport.add("Net Profit ($mill)");
        valuesToReport.add("Income Tax Rate");
        valuesToReport.add("Net Profit Margin");
        valuesToReport.add("Working Capital ($mill)");
        valuesToReport.add("Long-Term Debt ($mill)");
        valuesToReport.add("Share Equity ($mill)");
        valuesToReport.add("Return on Total Capital");
        valuesToReport.add("Return on Share Equity ($mill)");
        valuesToReport.add("Retained to Common Equity");
        valuesToReport.add("Income Tax Rate");
        valuesToReport.add("All Dividends to Net Profit");
    }

    private void getConcreteDataArrayListReady() {

        /*
         * 1. Reverse each arraylist
         * 2. Prepend all concrete data arraylists with "void" to line everything up
         * 3. replace the d with -
        */

        concreteYearData.forEach(Collections::reverse);

        int size = concreteYearData.get(0).size();
        for (int i = 0; i < concreteYearData.size(); i++) {
            if (concreteYearData.get(i).size() != size) {
                int size2 = concreteYearData.get(i).size();
                if (size2 < size) {
                    for (int j = 0; j < size - size2; j++) {
                        concreteYearData.get(i).add(0, "VOID");
                    }
                }
                else {
                    if (size2 - size == 1) {
                        concreteYearData.get(i).remove(concreteYearData.get(i).size() - 1);
                    }
                }
            }
        }

        for (ArrayList<String> aConcreteYearData : concreteYearData) {
            for (int j = 0; j < aConcreteYearData.size(); j++) {
                if (aConcreteYearData.get(j).contains("d")) {
                    String s = aConcreteYearData.get(j).replaceAll("d", "-");
                    aConcreteYearData.set(j, s);
                }
            }
        }
    }

    private boolean checkIfStockYearDataExists(int i) {
        /*
         * This function will check the Concrete Year DB to see if information is there.
        */

        Connection conn = null;
        ResultSet rs = null;
        Statement s = null;

        try {
            conn = createConnection();
            s = conn.createStatement();

            String stockSQL = "SELECT Stocks.concreteyeardata.ConcreteYearDataID " +
                    "FROM Stocks.concreteyeardata " +
                    "WHERE Stocks.concreteyeardata.StockID = '" + stockID + "' " +
                    "AND Stocks.concreteyeardata.YearID= '" + getYearID(i) +"';";

            try {
                rs = s.executeQuery(stockSQL);
                return rs.next();

            } catch (Exception ex) {
                return true;
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        finally
        {
            closeConnection(rs, s, conn);
        }
        return false;
    }

    private HashMap<String, String> populateTitlesFound(HashMap<String, String> a, int i) {

        a.put("Revenues per Share", concreteYearData.get(titlesData.indexOf("Revenues per share")).get(i));
        a.put("Cash Flow per share", concreteYearData.get(titlesData.indexOf("Cash Flow per share")).get(i));
        a.put("Earnings per share", concreteYearData.get(titlesData.indexOf("Earnings per share")).get(i));
        a.put("Dividends Declared per share", concreteYearData.get(titlesData.indexOf("Dividends Declared per share")).get(i));
        a.put("Capital Spending per share", concreteYearData.get(titlesData.indexOf("Capital Spending per share")).get(i));
        a.put("Book Value per share", concreteYearData.get(titlesData.indexOf("Book Value per share")).get(i));
        a.put("Common Shares Outstanding", concreteYearData.get(titlesData.indexOf("Common Shares Outstanding")).get(i));
        a.put("Average Annual P/E Ratio", concreteYearData.get(titlesData.indexOf("Average Annual P/E Ratio")).get(i));
        a.put("Relative P/E Ratio", concreteYearData.get(titlesData.indexOf("Relative P/E Ratio")).get(i));
        a.put("Average Annual Dividend Yield", concreteYearData.get(titlesData.indexOf("Average Annual Dividend Yield")).get(i));
        a.put("Revenues ($mill)", concreteYearData.get(titlesData.indexOf("Revenues ($mill)")).get(i));
        a.put("Operating Margin", concreteYearData.get(titlesData.indexOf("Operating Margin")).get(i));
        a.put("Depreciation ($mill)", concreteYearData.get(titlesData.indexOf("Depreciation ($mill)")).get(i));
        a.put("Net Profit ($mill)", concreteYearData.get(titlesData.indexOf("Net Profit ($mill)")).get(i));
        a.put("Income Tax Rate", concreteYearData.get(titlesData.indexOf("Income Tax Rate")).get(i));
        a.put("Net Profit Margin", concreteYearData.get(titlesData.indexOf("Net Profit Margin")).get(i));
        a.put("Working Capital ($mill)", concreteYearData.get(titlesData.indexOf("Working Capital ($mill)")).get(i));
        a.put("Long-Term Debt ($mill)", concreteYearData.get(titlesData.indexOf("Long-Term Debt ($mill)")).get(i));
        a.put("Share Equity ($mill)", concreteYearData.get(titlesData.indexOf("Share Equity ($mill)")).get(i));
        a.put("Return on Total Capital", concreteYearData.get(titlesData.indexOf("Return on Total Capital")).get(i));
        a.put("Return on Share Equity ($mill)", concreteYearData.get(titlesData.indexOf("Return on Share Equity ($mill)")).get(i));
        a.put("Retained to Common Equity", concreteYearData.get(titlesData.indexOf("Retained to Common Equity")).get(i));
        a.put("All Dividends to Net Profit", concreteYearData.get(titlesData.indexOf("All Dividends to Net Profit")).get(i));
        return a;
    }

    private String checkIfValueExist(String s, int i) {
        String st;
        try {
            st = concreteYearData.get(titlesData.indexOf(s)).get(i);
        }
        catch (Exception e) {
            st = "VOID";
        }
        return st;
    }

    private void addConcreteYearData() {
        Connection conn = null;
        Statement s = null;

        if (stockID != -1) {

            for (int i = 0; i < concreteYearData.get(0).size(); i++) {
                if (!checkIfStockYearDataExists(years.get(i))) {
                    try {
                        conn = createConnection();
                        s = conn.createStatement();

                        String sql = "INSERT INTO Stocks.concreteyeardata " +
                                "(" +
                                "Stocks.concreteyeardata.StockID, " +
                                "Stocks.concreteyeardata.YearID," +
                                "Stocks.concreteyeardata.RevenuesPerShare," +
                                "Stocks.concreteyeardata.CashFlowPerShare," +
                                "Stocks.concreteyeardata.EarningsPerShare," +
                                "Stocks.concreteyeardata.DividendsDeclaredPerShare," +
                                "Stocks.concreteyeardata.CapitalSpendingPerShare," +
                                "Stocks.concreteyeardata.BookValuePerShare," +
                                "Stocks.concreteyeardata.CommonSharesOutstanding," +
                                "Stocks.concreteyeardata.AverageAnnualPERatio," +
                                "Stocks.concreteyeardata.RelativePERatio," +
                                "Stocks.concreteyeardata.AverageAnnualDividendYield," +
                                "Stocks.concreteyeardata.Revenues," +
                                "Stocks.concreteyeardata.OperatingMargin," +
                                "Stocks.concreteyeardata.Depreciation," +
                                "Stocks.concreteyeardata.NetProfit," +
                                "Stocks.concreteyeardata.IncomeTaxRate," +
                                "Stocks.concreteyeardata.NetProfitMargin," +
                                "Stocks.concreteyeardata.WorkingCapital," +
                                "Stocks.concreteyeardata.LongTermDebt," +
                                "Stocks.concreteyeardata.ShareEquity," +
                                "Stocks.concreteyeardata.ReturnOnTotalCapital," +
                                "Stocks.concreteyeardata.ReturnOnShareEquity," +
                                "Stocks.concreteyeardata.RetainedToCommonEquity," +
                                "Stocks.concreteyeardata.AllDividendsToNetProfit" +
                                ") " +
                                "VALUES " +
                                "('" +
                                stockID + "', '" +
                                getYearID(years.get(i)) + "', '" +
                                checkIfValueExist("Revenues per share", i) + "', '" +
                                checkIfValueExist("Cash Flow per share", i) + "', '" +
                                checkIfValueExist("Earnings per share", i) + "', '" +
                                checkIfValueExist("Dividends Declared per share", i) + "', '" +
                                checkIfValueExist("Capital Spending per share", i) + "', '" +
                                checkIfValueExist("Book Value per share", i) + "', '" +
                                checkIfValueExist("Common Shares Outstanding", i) + "', '" +
                                checkIfValueExist("Average Annual P/E Ratio", i) + "', '" +
                                checkIfValueExist("Relative P/E Ratio", i) + "', '" +
                                checkIfValueExist("Average Annual Dividend Yield", i) + "', '" +
                                checkIfValueExist("Revenues ($mill)", i) + "', '" +
                                checkIfValueExist("Operating Margin", i) + "', '" +
                                checkIfValueExist("Depreciation ($mill)", i) + "', '" +
                                checkIfValueExist("Net Profit ($mill)", i) + "', '" +
                                checkIfValueExist("Income Tax Rate", i) + "', '" +
                                checkIfValueExist("Net Profit Margin", i) + "', '" +
                                checkIfValueExist("Working Capital ($mill)", i) + "', '" +
                                checkIfValueExist("Long-Term Debt ($mill)", i) + "', '" +
                                checkIfValueExist("Share Equity ($mill)", i) + "', '" +
                                checkIfValueExist("Return on Total Capital", i) + "', '" +
                                checkIfValueExist("Return on Share Equity ($mill)", i) + "', '" +
                                checkIfValueExist("Retained to Common Equity", i) + "', '" +
                                checkIfValueExist("All Dividends to Net Profit", i) + "''" +
                                "');";
                        s.executeUpdate(sql);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        closeConnection(null, s, conn);
                    }
                }
            }
        }
    }

    private void populateQuartersTable() {
        boolean exists = checkForQuarters();
        Connection conn = null;
        Statement s = null;
        if (!exists) {
            try {
                conn = createConnection();
                s = conn.createStatement();
                for (int i = 1; i < 5; i++) {
                    String sql = "INSERT INTO Stocks.quarters (Stocks.Quarters.QuarterValue) " +
                            "VALUES ('" + i + "');";
                    s.executeUpdate(sql);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                closeConnection(null, s, conn);
            }
        }
    }

    private void createDatabase() {
        Connection conn = null;
        Statement s = null;

        try {
            conn = createConnection();
            s = conn.createStatement();
            try {
                s.executeUpdate("CREATE DATABASE Stocks");
            }
            catch (SQLException e) {
                if (e.getErrorCode() == 1007) {
                    System.out.println(e.getMessage());
                }
            }
            try {
                s.executeUpdate("CREATE TABLE Stocks.Stocks " +
                        "(" +
                        "StockID INT NOT NULL AUTO_INCREMENT, " +
                        "StockName VARCHAR(255), " +
                        "StockSymbol VARCHAR(255), " +
                        "PRIMARY KEY (StockID) " +
                        ");");
            }
            catch (SQLException e) {
                if (e.getErrorCode() == 1007) {
                    System.out.println(e.getMessage());
                }
            }
            try {
                s.executeUpdate("CREATE TABLE Stocks.Years " +
                        "(" +
                        "YearID INT NOT NULL AUTO_INCREMENT, " +
                        "YearValue VARCHAR(255), " +
                        "PRIMARY KEY (YearID) " +
                        ");");
            }
            catch (SQLException e) {
                if (e.getErrorCode() == 1007) {
                    System.out.println(e.getMessage());
                }
            }
            try {
                s.executeUpdate("CREATE TABLE Stocks.Quarters " +
                        "(" +
                        "QuarterID INT NOT NULL AUTO_INCREMENT, " +
                        "QuarterValue VARCHAR(255), " +
                        "PRIMARY KEY (QuarterID) " +
                        ");");
            }
            catch (SQLException e) {
                if (e.getErrorCode() == 1007) {
                    System.out.println(e.getMessage());
                }
            }
            try {
                s.executeUpdate("CREATE TABLE Stocks.ConcreteYearData " +
                        "(" +
                        "ConcreteYearDataID INT NOT NULL AUTO_INCREMENT, " +
                        "StockID INT, " +
                        "YearID INT, " +
                        "RevenuesPerShare VARCHAR(255), " +
                        "CashFlowPerShare VARCHAR(255), " +
                        "EarningsPerShare VARCHAR(255), " +
                        "DividendsDeclaredPerShare VARCHAR(255), " +
                        "CapitalSpendingPerShare VARCHAR(255), " +
                        "BookValuePerShare VARCHAR(255), " +
                        "CommonSharesOutstanding VARCHAR(255), " +
                        "AverageAnnualPERatio VARCHAR(255), " +
                        "RelativePERatio VARCHAR(255), " +
                        "AverageAnnualDividendYield VARCHAR(255), " +
                        "Revenues VARCHAR(255), " +
                        "OperatingMargin VARCHAR(255), " +
                        "Depreciation VARCHAR(255), " +
                        "NetProfit VARCHAR(255), " +
                        "IncomeTaxRate VARCHAR(255), " +
                        "NetProfitMargin VARCHAR(255), " +
                        "WorkingCapital VARCHAR(255), " +
                        "LongTermDebt VARCHAR(255), " +
                        "ShareEquity VARCHAR(255), " +
                        "ReturnOnTotalCapital VARCHAR(255), " +
                        "ReturnOnShareEquity VARCHAR(255), " +
                        "RetainedToCommonEquity VARCHAR(255), " +
                        "AllDividendsToNetProfit VARCHAR(255), " +
                        "CurrentPosition VARCHAR(255), " +
                        "PRIMARY KEY (ConcreteYearDataID), " +
                        "FOREIGN KEY (StockID) REFERENCES Stocks.stocks(StockID), " +
                        "FOREIGN KEY (YearID) REFERENCES Stocks.years(YearID)" +
                        ");");
            }
            catch (SQLException e) {
                if (e.getErrorCode() == 1007) {
                    System.out.println(e.getMessage());
                }
            }
            try {
                s.executeUpdate("CREATE TABLE Stocks.FutureYearData " +
                        "(" +
                        "FutureYearDataID INT NOT NULL AUTO_INCREMENT, " +
                        "StockID INT, " +
                        "YearID INT, " +
                        "RevenuesPerShare VARCHAR(255), " +
                        "CashFlowPerShare VARCHAR(255), " +
                        "EarningsPerShare VARCHAR(255), " +
                        "DividendsDeclaredPerShare VARCHAR(255), " +
                        "CapitalSpendingPerShare VARCHAR(255), " +
                        "BookValuePerShare VARCHAR(255), " +
                        "CommonSharesOutstanding VARCHAR(255), " +
                        "AverageAnnualPERatio VARCHAR(255), " +
                        "RelativePERatio VARCHAR(255), " +
                        "AverageAnnualDividendYield VARCHAR(255), " +
                        "Revenues VARCHAR(255), " +
                        "OperatingMargin VARCHAR(255), " +
                        "Depreciation VARCHAR(255), " +
                        "NetProfit VARCHAR(255), " +
                        "IncomeTaxRate VARCHAR(255), " +
                        "NetProfitMargin VARCHAR(255), " +
                        "WorkingCapital VARCHAR(255), " +
                        "LongTermDebt VARCHAR(255), " +
                        "ShareEquity VARCHAR(255), " +
                        "ReturnOnTotalCapital VARCHAR(255), " +
                        "ReturnOnShareEquity VARCHAR(255), " +
                        "RetainedToCommonEquity VARCHAR(255), " +
                        "AllDividendsToNetProfit VARCHAR(255), " +
                        "HighProj VARCHAR(255), " +
                        "LowProj VARCHAR(255), " +
                        "PRIMARY KEY (FutureYearDataID), " +
                        "FOREIGN KEY (StockID) REFERENCES Stocks.stocks(StockID), " +
                        "FOREIGN KEY (YearID) REFERENCES Stocks.years(YearID)" +
                        ");");
            }
            catch (SQLException e) {
                if (e.getErrorCode() == 1007) {
                    System.out.println(e.getMessage());
                }
            }
            try {
                s.executeUpdate("CREATE TABLE Stocks.ConcreteQuarterData " +
                        "(" +
                        "ConcreteQuarterDataID INT NOT NULL AUTO_INCREMENT, " +
                        "StockID INT, " +
                        "YearID INT, " +
                        "QuarterID INT, " +
                        "RecentPrice VARCHAR(255), " +
                        "PERatio VARCHAR(255), " +
                        "RelativePERatio VARCHAR(255), " +
                        "DividendYield VARCHAR(255), " +
                        "Timeliness VARCHAR(255), " +
                        "Safety VARCHAR(255), " +
                        "Technical VARCHAR(255), " +
                        "TotalDebt VARCHAR(255), " +
                        "CommonStock VARCHAR(255), " +
                        "MarketCap VARCHAR(255), " +
                        "PRIMARY KEY (ConcreteQuarterDataID), " +
                        "FOREIGN KEY (StockID) REFERENCES Stocks.stocks(StockID), " +
                        "FOREIGN KEY (YearID) REFERENCES Stocks.years(YearID)," +
                        "FOREIGN KEY (QuarterID) REFERENCES Stocks.quarters(QuarterID)" +
                        ");");
            }
            catch (SQLException e) {
                if (e.getErrorCode() == 1007) {
                    System.out.println(e.getMessage());
                }
            }
        }
        catch (SQLException e) {
            if (e.getErrorCode() == 1007) {
                System.out.println(e.getMessage());
            }
        }
        finally
        {
            closeConnection(null, s, conn);
        }
    }

    private void getAnnualRates() {
        try (BufferedReader reader = new BufferedReader(new FileReader(f)))
        {
            annualRates = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Annual Rates")) {
                    line = reader.readLine();
                    while (!line.contains("Quarterly")) {
                        String[] ys = line.split("\\s+");

                        if (ys.length > 7 && annualRates.size() == 0) {
                            ArrayList<String> temp = new ArrayList<>();
                            temp.add("Past 10");
                            temp.add("Past 5");
                            temp.add("Future");
                            annualRates.add(temp);
                        }
                        else
                        {
                            ArrayList<String> temp = new ArrayList<>();
                            if (ys[0].contains("Cash")) {
                                temp.add("Cash Flow");
                                for (int i = 2; i < ys.length; i++) {
                                    temp.add(ys[i]);
                                }
                            }
                            else if (ys[0].contains("Book")) {
                                temp.add("Book Value");
                                for (int i = 2; i < ys.length; i++) {
                                    temp.add(ys[i]);
                                }
                            }
                            else {
                                for (int i = 0; i < ys.length; i++) {
                                    temp.add(ys[i]);
                                }
                            }
                            if (temp.size() > 1)
                                annualRates.add(temp);
                        }
                        line = reader.readLine();
                    }
                }
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void getQuarterlyDividendsPaid() {
        try (BufferedReader reader = new BufferedReader(new FileReader(f)))
        {
            quarterlyDividendsPaid = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Quarterly Dividends Paid")) {
                    line = reader.readLine();
                    while (!line.contains("Institutional")) {
                        String[] ys = line.split("\\s+");

                        if (ys.length == 4 && quarterlyDividendsPaid.size() == 0) {
                            ArrayList<String> temp = new ArrayList<>();
                            temp.add("Q1");
                            temp.add("Q2");
                            temp.add("Q3");
                            temp.add("Q4");
                            quarterlyDividendsPaid.add(temp);
                        }
                        else
                        {
                            if (concreteYearData.get(0).contains(ys[0]) || futureYearData.get(0).contains(ys[0])) {
                                ArrayList<String> temp = new ArrayList<>();
                                for (String s : ys) {
                                    temp.add(s);
                                }
                                quarterlyDividendsPaid.add(temp);
                            }
                        }
                        line = reader.readLine();
                    }
                }
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void getEarningsPerShare() {
        try (BufferedReader reader = new BufferedReader(new FileReader(f)))
        {
            earningsPerShare = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Earnings per Share")) {
                    line = reader.readLine();
                    while (!line.contains("Quarterly")) {
                        String[] ys = line.split("\\s+");

                        if (ys.length == 4 && earningsPerShare.size() == 0) {
                            ArrayList<String> temp = new ArrayList<>();
                            temp.add("Q1");
                            temp.add("Q2");
                            temp.add("Q3");
                            temp.add("Q4");
                            earningsPerShare.add(temp);
                        }
                        else
                        {
                            if (concreteYearData.get(0).contains(ys[0]) || futureYearData.get(0).contains(ys[0])) {
                                ArrayList<String> temp = new ArrayList<>();
                                for (String s : ys) {
                                    temp.add(s);
                                }
                                earningsPerShare.add(temp);
                            }
                        }
                        line = reader.readLine();
                    }
                }
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void getQuarterlyRevenues() {
        try (BufferedReader reader = new BufferedReader(new FileReader(f)))
        {
            quarterlyRevenues = new ArrayList<>();
            String line;
            String ending = "";
            while ((line = reader.readLine()) != null) {
                if (line.contains("Quarterly Revenues")) {
                    if (line.contains("mill")) {
                        ending = "($mill)";
                    }
                    line = reader.readLine();
                    while (!line.contains("Earnings per Share")) {
                        String[] ys = line.split("\\s+");

                        if (ys.length == 4 && quarterlyRevenues.size() == 0) {
                            ArrayList<String> temp = new ArrayList<>();
                            temp.add("Q1");
                            temp.add("Q2");
                            temp.add("Q3");
                            temp.add("Q4");
                            quarterlyRevenues.add(temp);
                        }
                        else
                        {
                            if (concreteYearData.get(0).contains(ys[0]) || futureYearData.get(0).contains(ys[0])) {
                                ArrayList<String> temp = new ArrayList<>();
                                for (String s : ys) {
                                    temp.add(s);
                                }
                                temp.add(ending);
                                quarterlyRevenues.add(temp);
                            }
                        }
                        line = reader.readLine();
                    }
                }
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void getCurrentPosition() {
        try (BufferedReader reader = new BufferedReader(new FileReader(f)))
        {
            currentPosition = new ArrayList<>();
            String line;
            int numYears = 0;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Current Position")) {
                    line = reader.readLine();
                    String[] ys = line.split("\\s+");
                    ArrayList<String> temp = new ArrayList<>();
                    ArrayList<String> assets = new ArrayList<>();
                    ArrayList<String> liabs = new ArrayList<>();
                    for (String s : ys) {
                        if (!s.contains("/")) {
                            temp.add(s);
                            numYears++;
                        }
                    }
                    temp.add("quarter");
                    currentPosition.add(temp);
                    while (!line.contains("Current Assets")) {
                        line = reader.readLine();
                    }
                    String[] words = line.split("\\s+");
                    int j = 0;
                    for (int i = 0; i < numYears;) {
                        if (checkIfNumber(words[j])) {
                            i++;
                            assets.add(words[j]);
                        }
                        j++;
                    }
                    assets.add(words[words.length - 1]);
                    currentPosition.add(assets);
                    while (!line.contains("Current Liab.")) {
                        line = reader.readLine();
                    }
                    String[] nums = line.split("\\s+");
                    j = 0;
                    for (int i = 0; i < numYears;) {
                        if (checkIfNumber(nums[j])) {
                            i++;
                            liabs.add(nums[j]);
                        }
                        j++;
                    }
                    liabs.add(nums[nums.length - 1]);
                    currentPosition.add(liabs);
                }
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void getValues() {
        recentPrice = getSingleValue("Recent Price");
        peRatio = getSingleValue("P/E ratio");
        highProj = getSingleValue("High projection");
        lowProj = getSingleValue("Low projection");
        relativePERatio = getSingleValue("Relative P/E Ratio");
        dividendYield = getSingleValue("Dividend Yield");
        timeliness = getSingleValue("Timeliness");
        safety = getSingleValue("Safety");
        technical = getSingleValue("Technical");
        priceStability = getSingleValue("Price Stability");
        growthPersistence = getSingleValue("Price Growth Persistence");
        predictability = getSingleValue("Earnings Predictability");
        totalDebt = getSingleValueWithString("Total Debt");
        LTDebt = getSingleValueWithString("LT Debt");
        LTInterest = getSingleValueWithString("LT Interest");
        commonStock = getSingleValueWithString("Common Stock");
        marketCap = getSingleValueWithString("Market Cap");
        financialStrength = getSingleValueWithString("Financial Strength");
    }

    private String getSingleValueWithString(String s) {
        String ret = "";
        try (BufferedReader reader = new BufferedReader(new FileReader(f)))
        {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(s)) {
                    if (line.contains("%")) {
                        try {
                            ret = line.substring(line.indexOf(':') + 1, line.indexOf('%'));
                            break;
                        }
                        catch (NumberFormatException ex) {
                            ret = "0";
                        }
                    } else {
                        try {
                            ret = line.substring(line.indexOf(':') + 1);
                            break;
                        }
                        catch (NumberFormatException ex) {
                            ret = "0";
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return ret;
    }

    private Double getSingleValue(String s) {
        double ret = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(f)))
        {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(s)) {
                    if (line.contains("%")) {
                        try {
                            ret = Double.parseDouble(line.substring(line.indexOf(':') + 1, line.indexOf('%')));
                            break;
                        }
                        catch (NumberFormatException ex) {
                            ret = 0;
                        }
                    } else {
                        try {
                            ret = Double.parseDouble(line.substring(line.indexOf(':') + 1));
                            break;
                        }
                        catch (NumberFormatException ex) {
                            ret = 0;
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return ret;
    }

    private void getStockNameAndSymbol() {
        try (BufferedReader reader = new BufferedReader(new FileReader(f)))
        {
            String line;
            boolean hasName = false;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Stock Name")) {
                    stockName = line.substring(line.indexOf(':') + 1);
                    hasName = true;
                }
                else if (line.contains("Symbol")) {
                    stockSymbol = line.substring(line.indexOf(':') + 1);
                    if (hasName)
                        break;
                }
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    private void getYears() {
        /*
         * Get the years from the data text file and put them in an arraylist to add to the Years Table
        */
        years = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(f)))
        {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Years:")) {
                    String[] words = line.split("\\s+");
                    for (int i = 1; i < words.length; i++) {
                        int y = Integer.parseInt(words[i]);
                        years.add(y);
                    }
                }
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void getStockID() {
        Connection conn = null;
        ResultSet rs = null;
        Statement s = null;

        try {
            conn = createConnection();
            s = conn.createStatement();

            String stockSQL = "SELECT Stocks.stocks.stockID FROM Stocks.stocks " +
                    "WHERE Stocks.stocks.StockSymbol = '" + stockSymbol + "';";

            try {
                rs = s.executeQuery(stockSQL);
                while(rs.next()) {
                    stockID = rs.getInt("StockID");
                }

            } catch (Exception ex) {
                System.out.println(ex);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        finally
        {
            closeConnection(rs, s, conn);
        }
    }
    private int getYearID(int n) {
        Connection conn = null;
        ResultSet rs = null;
        Statement s = null;

        int id = -1;

        try {
            conn = createConnection();
            s = conn.createStatement();

            String stockSQL = "SELECT Stocks.years.yearID FROM Stocks.years " +
                    "WHERE Stocks.years.YearValue = '" + n + "';";

            try {
                rs = s.executeQuery(stockSQL);
                while(rs.next()) {
                    id = rs.getInt("YearID");
                }

            } catch (Exception ex) {
                System.out.println(ex);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        finally
        {
            closeConnection(rs, s, conn);
        }
        return id;
    }

    private boolean checkForStock() {
        /*
         * This function will check the Stocks Database Table and
         *  return true if that stock exists and false if it doesn't
        */

        Connection conn = null;
        ResultSet rs = null;
        Statement s = null;

        try {
            conn = createConnection();
            s = conn.createStatement();

            String stockSQL = "SELECT Stocks.stocks.stockID FROM Stocks.stocks " +
                    "WHERE Stocks.stocks.StockSymbol = '" + stockSymbol + "';";

            try {
                rs = s.executeQuery(stockSQL);
                return rs.next();

            } catch (Exception ex) {
                return true;
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        finally
        {
            closeConnection(rs, s, conn);
        }
        return false;
    }

    private boolean checkForYear(int y) {
        Connection conn = null;
        ResultSet rs = null;
        Statement s = null;

        try {
            conn = createConnection();
            s = conn.createStatement();

            String stockSQL = "SELECT Stocks.years.yearID FROM Stocks.years " +
                    "WHERE Stocks.years.yearValue = '" + y + "';";

            try {
                rs = s.executeQuery(stockSQL);
                return rs.next();

            } catch (Exception ex) {
                return true;
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        finally
        {
            closeConnection(rs, s, conn);
        }
        return false;
    }

    private boolean checkForQuarters() {
        Connection conn = null;
        ResultSet rs = null;
        Statement s = null;

        try {
            conn = createConnection();
            s = conn.createStatement();

            String stockSQL = "SELECT Stocks.quarters.quarterID FROM Stocks.quarters;";

            try {
                rs = s.executeQuery(stockSQL);
                return rs.next();

            } catch (Exception ex) {
                return true;
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        finally
        {
            closeConnection(rs, s, conn);
        }
        return false;
    }

    private void addStock() {
        /*
         * Adds the stock symbol and name to table if it doesn't already exist.
        */
        Connection conn = null;
        Statement s = null;
        stockSymbol = stockSymbol.replaceAll("\\s+","");
        boolean exists = checkForStock();

        if (!exists) {
            try {
                conn = createConnection();
                s = conn.createStatement();

                String sql = "INSERT INTO Stocks.stocks (Stocks.stocks.StockName, Stocks.stocks.StockSymbol) " +
                        "VALUES ('" + stockName + "', '" + stockSymbol + "');";
                s.executeUpdate(sql);
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                closeConnection(null, s, conn);
            }
        }
    }

    private void addYears() {
        for (Integer year : years) {
            boolean exists = checkForYear(year);
            Connection conn = null;
            Statement s = null;
            if (!exists) {
                try {
                    conn = createConnection();
                    s = conn.createStatement();

                    String sql = "INSERT INTO Stocks.years (Stocks.years.yearValue) " +
                            "VALUES ('" + year + "');";
                    s.executeUpdate(sql);
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    closeConnection(null, s, conn);
                }
            }
        }
    }

    /* This method will get the data that is not estimates in the year data table */

    private void getYearData() {
        String path = f.getAbsolutePath();
        String estimatedYear = (path.substring(0, path.lastIndexOf('\\')));
        estimatedYear = estimatedYear.substring(estimatedYear.lastIndexOf('_') + 1);

        futureYearData = new ArrayList<>();
        concreteYearData = new ArrayList<>();
        titlesData = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(f)))
        {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.contains("Technical")) {
                    reader.readLine();
                    line = reader.readLine();
                    int numFutureYears = 0;
                    boolean numFutureYearsFound = false;

                    while (!line.contains("Current Position") && !line.contains("Annual Rates")) {
                        String[] words = line.split("\\s+");
                        ArrayList<String> futureTemp = new ArrayList<>();
                        ArrayList<String> concreteTemp = new ArrayList<>();

                        String title = "";

                        boolean inConcreteData = false;
                        int n = 0;
                        for (int i = words.length - 1; i >= 0; i--) {
                            if (!numFutureYearsFound) {
                                if (words[i].contains(estimatedYear)) {
                                    futureTemp.add(words[i]);
                                    numFutureYearsFound = true;
                                    inConcreteData = true;
                                }
                                else {
                                    numFutureYears++;
                                    futureTemp.add(words[i]);
                                }
                            }
                            else {
                                if (!inConcreteData) {
                                    if (checkIfNumber(words[i]) && !Objects.equals(words[i], "")) {
                                        n++;
                                        if (n < numFutureYears) {
                                            futureTemp.add(words[i]);
                                        }
                                        else if (n == numFutureYears) {
                                            futureTemp.add(words[i]);
                                            inConcreteData = true;
                                        }
                                    }
                                    else {
                                        if (!Objects.equals(words[i], "") &&
                                                !Objects.equals(words[i], "Nil") &&
                                                !Objects.equals(words[i], "--") &&
                                                !Objects.equals(words[i], "NMF")) {
                                            String oldTitle = title;
                                            title = words[i] + " " + oldTitle;
                                         }
                                    }
                                }
                                else {
                                    if (!Objects.equals(words[i], ""))
                                        concreteTemp.add(words[i]);
                                }
                            }
                        }
                        if (title.length() > 2)
                            title = title.substring(0, title.length() - 1);

                        titlesData.add(title);
                        futureYearData.add(futureTemp);
                        concreteYearData.add(concreteTemp);
                        line = reader.readLine();
                    }
                }
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
        int numConcreteYears = concreteYearData.get(0).size();
        for (int i = 1; i < concreteYearData.size(); i++) {
            if (numConcreteYears - concreteYearData.get(i).size() == 1) {
                // Likely is a blank box in the PDF form.. adding another double dash to the beginning.
                concreteYearData.get(i).add(concreteYearData.get(i).size(), "--");
            }
        }
        int numFutureYears = futureYearData.get(0).size();
        for (int i = 1; i < futureYearData.size(); i++) {
            if (numFutureYears - futureYearData.get(i).size() == 2) {
                // Likely is a blank box in the PDF form.. adding another double dash to the beginning.
                futureYearData.get(i).add(futureYearData.get(i).size(), "--");
            }
        }
    }

    private boolean checkIfNumber(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i)) && s.charAt(i) != '.' && s.charAt(i) != '%' &&
                    !s.contains("BLANK") && s.charAt(i) != 'd') {
                return false;
            }
        }
        return true;
    }

    private Connection createConnection() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection("jdbc:mysql://localhost/?user=root&password=Stocks123");
        } catch (Exception e) {e.printStackTrace();}
        return conn;
    }
    private void closeConnection(ResultSet rs, Statement s, Connection conn) {
        try
        {
            if (rs != null) {
                rs.close();
            }
            if (s != null) {
                s.close();
            }
            if (conn != null) {
                conn.close();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
