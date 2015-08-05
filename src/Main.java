import com.pdflib.TETException;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;


public class Main {
    public static void main(String[] args) throws TETException, FileNotFoundException, UnsupportedEncodingException {
        JFileChooser chooser = new JFileChooser();
        File selection;
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("Choose a data location");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            selection = chooser.getSelectedFile();
            find_files(selection);
        } else {
            System.out.println("No Selection ");
        }
    }
    private static void find_files(File root) throws FileNotFoundException, UnsupportedEncodingException {
        File[] files = root.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                String extension = file.getName().substring(file.getName().lastIndexOf(".") + 1, file.getName().length());
                if (extension.equals("pdf")){
                    String fileName = file.getName();
                    fileName = fileName.substring(0, fileName.lastIndexOf('.'));
                    new File(root + "\\Results").mkdirs();
                    String f = root + "\\Results\\" + fileName;
                    new File(f).mkdirs();

                    new DataExtractor(file, f);

                    break;
                }
            } else if (file.isDirectory()) {
                find_files(file);
            }
        }
    }
}

