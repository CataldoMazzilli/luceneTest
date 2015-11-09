package provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;

import java.io.IOException;

/**
 * Created by cataldo on 04/11/15.
 */
public class IndexWriterProvider implements Provider<IndexWriter> {
    private Directory directory;
    private IndexWriterConfig indexWriterConfig;

    @Inject
    public IndexWriterProvider(@Assisted Directory directory, @Assisted IndexWriterConfig indexWriterConfig) {
        this.directory = directory;
        this.indexWriterConfig = indexWriterConfig;
    }

    @Override
    public IndexWriter get() {
        try {
            return new IndexWriter(directory, indexWriterConfig);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}



