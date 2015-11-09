import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import provider.IndexWriterConfigProvider;
import provider.IndexWriterProvider;

import java.util.Date;

/**
 * Created by cataldo on 04/11/15.
 */
public interface MyFactory {

    IndexWriterConfigProvider createIndexWriterConfigProvider(Analyzer analyzer);
    IndexWriterProvider createIndexWriterProvider(Directory directory, IndexWriterConfig indexWriterConfig);

    Document createDocument();
    Date createDate();

}
