package provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriterConfig;

/**
 * Created by cataldo on 04/11/15.
 */
public class IndexWriterConfigProvider implements Provider<IndexWriterConfig> {
    private Analyzer analyzer;

    @Inject
    public IndexWriterConfigProvider(@Assisted Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    @Override
    public IndexWriterConfig get() {
        return new IndexWriterConfig(analyzer);
    }
}
