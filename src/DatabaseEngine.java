import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.InputMismatchException;

public class DatabaseEngine
{
    File f;
    String stockName;
    String stockSymbol;
    ArrayList<Integer> years;
    double recentPrice, peRatio, highProj, lowProj, relativePERatio, dividendYield,
            timeliness, safety, technical;

    public DatabaseEngine(File f) {
        this.f = f;
        getData();
        addStock();
        getYears();
        addYears();
        getValues();
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
                        ret = Double.parseDouble(line.substring(line.indexOf(':') + 1, line.indexOf('%')));
                        break;
                    }
                    else {
                        ret = Double.parseDouble(line.substring(line.indexOf(':') + 1));
                        break;
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
