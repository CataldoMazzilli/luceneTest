import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

/**
 * Created by cataldo on 04/11/15.
 */
public class MyModule extends AbstractModule {




    public MyModule(String args[]) {

    }

    @Override
    protected void configure() {

        bindConstant().annotatedWith(Names.named("usage")).to("java org.apache.lucene.demo.IndexFiles"
                + " [-index INDEX_PATH] [-docs DOCS_PATH] [-update] [-type]\n\n"
                + "This indexes the documents in DOCS_PATH, creating a Lucene index"
                + "in INDEX_PATH that can be searched with SearchFiles");



    }
}
