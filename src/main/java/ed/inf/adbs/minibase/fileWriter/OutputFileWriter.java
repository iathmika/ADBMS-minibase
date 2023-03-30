package ed.inf.adbs.minibase.fileWriter;

import java.io.File;
import java.nio.file.Paths;
import java.io.FileWriter;
import java.io.IOException;


//  singleton filewriter that handles writing to the output file.
public class OutputFileWriter {

    private static FileWriter fW;

    public static FileWriter getFileWriter() {
        if (fW != null) {
            return fW;
        }
        else throw new UnsupportedOperationException("File Writer not initialised. ");
    }

    public static void initialiseOutputWriter(String outputFileName) throws IOException {
        File outFile = Paths.get(outputFileName).toFile();
        System.out.println("Output file name is: "+outputFileName);
        outFile.createNewFile();

        fW = new FileWriter(outputFileName);
    }

    private OutputFileWriter() {
    }

    public static boolean outputWriterInitialised() {
        return fW != null;
    }
}