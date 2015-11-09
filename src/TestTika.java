import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

/**
 * Created by cataldo on 04/11/15.
 */
public class TestTika {

    public static void main(String[] args) throws IOException, TikaException, SAXException {



        //Assume sample.txt is in your current directory
        File file = new File("/home/cataldo/cataldo/lucene_test/odfFile/numeri.ods");

        //Instantiating Tika facade class
        Tika tika = new Tika();
        String filecontent = tika.parseToString(file);
        System.out.println("Extracted Content: " + filecontent);

    }


}
