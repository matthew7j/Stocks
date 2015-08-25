import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.*;

public class DatabaseEngine
{
    File f;
    String stockName;
    String stockSymbol;
    ArrayList<Integer> years;
    double recentPrice, peRatio, highProj, lowProj, relativePERatio, dividendYield,
            timeliness, safety, technical;

    ArrayList<ArrayList<String>> concreteYearData;
    ArrayList<ArrayList<String>> futureYearData;
    ArrayList<ArrayList<String>> titlesData;
    ArrayList<ArrayList<String>> currentPosition;
    ArrayList<ArrayList<String>> quarterlyRevenues;
    ArrayList<ArrayList<String>> earningsPerShare;
    ArrayList<ArrayList<String>> quarterlyDividendsPaid;
    ArrayList<ArrayList<String>> annualRates;

    public DatabaseEngine(File f) {
        this.f = f;
        getData();
        /*addStock();
        getYears();
        addYears();*/
        getValues();
        getCurrentPosition();
        getYearData();
        getQuarterlyRevenues();
        getEarningsPerShare();
        getQuarterlyDividendsPaid();
        getAnnualRates();
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

    private void getData() {
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

    private boolean checkForStock() {
        Connection conn = null;
        ResultSet rs = null;
        Statement s = null;

        try {
            conn = createConnection();
            s = conn.createStatement();

            String stockSQL = "SELECT Stocks.Stock.stockID FROM Stocks.Stock " +
                    "WHERE Stocks.Stock.StockSymbol = '" + stockSymbol + "';";

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

            String stockSQL = "SELECT Stocks.Year.yearID FROM Stocks.Year " +
                    "WHERE Stocks.Year.yearNumber = '" + y + "';";

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
        boolean exists = checkForStock();
        Connection conn = null;
        Statement s = null;
        if (!exists) {
            try {
                conn = createConnection();
                s = conn.createStatement();

                String sql = "INSERT INTO Stocks.Stock (Stocks.Stock.StockName, Stocks.Stock.StockSymbol) " +
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

                    String sql = "INSERT INTO Stocks.Year (Stocks.Year.yearNumber) " +
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
                        ArrayList<String> titlesTemp = new ArrayList<>();

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
                                        if (!Objects.equals(words[i], "")) {
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

                        titlesTemp.add(title);
                        titlesData.add(titlesTemp);
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
