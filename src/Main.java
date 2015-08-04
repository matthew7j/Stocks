import com.pdflib.TETException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

public class Main {
    public static void main(String[] args) throws TETException, FileNotFoundException, UnsupportedEncodingException {
        File f = new File("C:\\Users\\mjones\\Desktop\\Stocks\\Stock Information\\Disney\\report_2_7_15.pdf");
        new DataExtractor(f);
    }
}

