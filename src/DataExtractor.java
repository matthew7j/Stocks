import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import com.pdflib.TET;
import com.pdflib.TETException;

public class DataExtractor
{
    static final String PAGE_OPTLIST = "granularity=page";

    public DataExtractor(File f) throws FileNotFoundException, UnsupportedEncodingException {
        TET tet = null;
        int doc = -1;
        File data = null;
        String stockData = "";
        try {
            tet = new TET();
            doc = tet.open_document(f.getAbsolutePath(), "");

            int numPages = (int) tet.pcos_get_number(doc, "length:pages");
            data = new File("C:\\Users\\mjones\\Desktop\\Stocks\\Stock Information\\Disney\\Results\\report_2_7_15\\data.txt");
            stockData = "";
            for (int num = 1; num <= numPages; ++num) {
                String text;
                int page = tet.open_page(doc, num, PAGE_OPTLIST);

                if (page < 0) {
                    System.out.println("Something went wrong");
                    continue;
                }
                while ((text = tet.get_text(page)) != null) {
                    stockData += text;
                }

                if (tet.get_errnum() != 0) {
                    System.out.println("Something went wrong");
                }
                tet.close_page(page);
            }
        }
        catch (TETException e) {
            System.out.println(e.get_errmsg());
        }
        finally {
            PrintWriter writer = new PrintWriter(data, "UTF-8");
            try {
                writer.write(stockData);
                assert tet != null;
                tet.close_document(doc);
                writer.close();
                new DataOrganizer(data);
            }
            catch(TETException e) {
                System.out.println(e.get_errmsg());
            }
            catch(Exception e){
                System.out.println(e.getMessage());
            }
        }
    }
}
