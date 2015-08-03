import com.pdflib.TETException;

import java.io.File;

public class Main {
    public static void main(String[] args) throws TETException {
        File f = new File("C:\\Users\\Matt\\Stocks\\Stock Information\\Disney\\report_2_7_15.pdf");
        new DataExtractor(f);
    }
}

