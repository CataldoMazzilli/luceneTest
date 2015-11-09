import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

/**
 * Created by cataldo on 04/11/15.
 */
public class MyModule extends AbstractModule {


    private boolean pdfType = false;
    private String indexPath = "indexDirectory";
    private String docsPath = null;
    private boolean create = true;

    public MyModule(String args[]) {

        for (int i = 0; i < args.length; i++) {
            if ("-index".equals(args[i])) {
                indexPath = args[i + 1];
                i++;
            } else if ("-docs".equals(args[i])) {
                docsPath = args[i + 1];
                i++;
            } else if ("-update".equals(args[i])) {
                create = false;
            } else if ("-type".equals(args[i])) {
                pdfType = true;
            }
        }
    }

    @Override
    protected void configure() {

        bindConstant().annotatedWith(Names.named("usage")).to("java org.apache.lucene.demo.IndexFiles"
                + " [-index INDEX_PATH] [-docs DOCS_PATH] [-update] [-type]\n\n"
                + "This indexes the documents in DOCS_PATH, creating a Lucene index"
                + "in INDEX_PATH that can be searched with SearchFiles");
        bindConstant().annotatedWith(Names.named("pdfType")).to(pdfType);
        bindConstant().annotatedWith(Names.named("indexPath")).to(indexPath);
        bindConstant().annotatedWith(Names.named("docsPath")).to(docsPath);
        bindConstant().annotatedWith(Names.named("create")).to(create);

        bind(Analyzer.class).to(StandardAnalyzer.class);

          install(new FactoryModuleBuilder().build(MyFactory.class));

    }
}
