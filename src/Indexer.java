/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;

/**
 * Index all text files under a directory.
 * <p>
 * This is a command-line application demonstrating simple Lucene indexing.
 * Run it with no command-line arguments for usage information.
 */
public class Indexer {

    private final String usage;
    private boolean pdfType;
    private String indexPath;
    private String docsPath;
    private boolean create;
    private Analyzer analyzer;
    private MyFactory myFactory;


    @Inject
    public Indexer(@Named("usage") String usage,
                   @Named("pdfType") boolean pdfType,
                   @Named("indexPath") String indexPath,
                   @Named("docsPath") String docsPath,
                   @Named("create") boolean create,
                   Analyzer analyzer,
                   MyFactory myFactory)
    {
        this.usage = usage;
        this.pdfType = pdfType;
        this.indexPath = indexPath;
        this.docsPath = docsPath;
        this.create = create;
        this.analyzer = analyzer;
        this.myFactory = myFactory;
    }

    /**
     * Index all text files under a directory.
     */
    public void start() {


        /** e' necessario specificare il path dei documenti*/
        if (docsPath == null) {
            System.err.println("Usage: " + usage);
            System.exit(1);
        } else {

            /**crea path da stringa*/
            final Path documentDirectoryPath = Paths.get(docsPath);

            /**controlla che il path sia leggibile*/
            if (!Files.isReadable(documentDirectoryPath)) {
                System.out.println("Document directory '" + documentDirectoryPath.toAbsolutePath() + "' does not exist or is not readable, please check the path");
                System.exit(1);
            }



            Date start = myFactory.createDate();
            try {
                System.out.println("Indexing to directory '" + indexPath + "'...");


                /**istanzia Directory di tipo filesitem*/
                Directory directory = FSDirectory.open(Paths.get(indexPath));
                /**istanzia analyzer*/
                //Analyzer analyzer = new StandardAnalyzer();

                /**istanzia IndexWriterConfig con alalyzer*/
                //IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
                IndexWriterConfig indexWriterConfig = myFactory.createIndexWriterConfigProvider(analyzer).get();

                /**verifica che bisogna cancellare o aggiungere*/
                if (create) {
                    // Create a new index in the directory, removing any
                    // previously indexed documents:
                    indexWriterConfig.setOpenMode(OpenMode.CREATE);
                } else {
                    // Add new documents to an existing index:
                    indexWriterConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);
                }

                // Optional: for better indexing performance, if you
                // are indexing many documents, increase the RAM
                // buffer.  But if you do this, increase the max heap
                // size to the JVM (eg add -Xmx512m or -Xmx1g):
                //
                // indexWriterConfig.setRAMBufferSizeMB(256.0);

                /**istanzia IndexWriter con directory e IndexWriterConfig */
                //IndexWriter writer = new IndexWriter(directory, indexWriterConfig);
                IndexWriter writer = myFactory.createIndexWriterProvider(directory, indexWriterConfig).get();

                /**avvia metodo di indexing passando come parametro il writer e il path dei documenti*/
                indexDocs(writer, documentDirectoryPath);

                // NOTE: if you want to maximize search performance,
                // you can optionally call forceMerge here.  This can be
                // a terribly costly operation, so generally it's only
                // worth it when your index is relatively static (ie
                // you're done adding documents to it):
                //
                // writer.forceMerge(1);

                /**chiudi il writer index*/
                writer.close();

                /**verifica il tempo */
                Date end = myFactory.createDate();
                System.out.println(end.getTime() - start.getTime() + " total milliseconds");

            } catch (IOException e) {
                System.out.println(" caught a " + e.getClass() +
                        "\n with message: " + e.getMessage());
            }
        }
    }

    /**
     * Indexes the given file using the given writer, or if a directory is given,
     * recurses over files and directories found under the given directory.
     * <p>
     * NOTE: This method indexes one document per input file.  This is slow.  For good
     * throughput, put multiple documents into your input file(s).  An example of this is
     * in the benchmark module, which can create "line doc" files, one document per line,
     * using the
     * <a href="../../../../../contrib-benchmark/org/apache/lucene/benchmark/byTask/tasks/WriteLineDocTask.html"
     * >WriteLineDocTask</a>.
     *
     * @param writer Writer to the index where the given file/dir info will be stored
     * @param path   The file to index, or the directory to recurse into to find files to index
     * @throws IOException If there is a low-level I/O error
     */
    private void indexDocs(final IndexWriter writer, Path path) throws IOException {
        if (Files.isDirectory(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    try {
                        indexDoc(writer, file, attrs.lastModifiedTime().toMillis());
                    } catch (IOException ignore) {
                        // don't index files that can't be read.
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            indexDoc(writer, path, Files.getLastModifiedTime(path).toMillis());
        }
    }

    /**
     * Indexes a single document
     */
    private void indexDoc(IndexWriter writer, Path file, long lastModified) throws IOException {
        try (InputStream stream = Files.newInputStream(file)) {
            // make a new, empty document
            Document doc = myFactory.createDocument();
            if (!pdfType) {

                // Add the path of the file as a field named "path".  Use a
                // field that is indexed (i.e. searchable), but don't tokenize
                // the field into separate words and don't index term frequency
                // or positional information:
                Field pathField = new StringField("path", file.toString(), Field.Store.YES);
                doc.add(pathField);

                // Add the last modified date of the file a field named "modified".
                // Use a LongField that is indexed (i.e. efficiently filterable with
                // NumericRangeFilter).  This indexes to milli-second resolution, which
                // is often too fine.  You could instead create a number based on
                // year/month/day/hour/minutes/seconds, down the resolution you require.
                // For example the long value 2011021714 would mean
                // February 17, 2011, 2-3 PM.
                doc.add(new LongField("modified", lastModified, Field.Store.NO));

                // Add the contents of the file to a field named "contents".  Specify a Reader,
                // so that the text of the file is tokenized and indexed, but not stored.
                // Note that FileReader expects the file to be in UTF-8 encoding.
                // If that's not the case searching for special characters will fail.
                doc.add(new TextField("contents", new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))));
            } else {

                PDDocument pdfDocument = PDDocument.load(file.toFile());
                String content = new PDFTextStripper().getText(pdfDocument);
                pdfDocument.close();


                Field pathField = new StringField("path", file.toString(), Field.Store.YES);
                doc.add(pathField);

                doc.add(new LongField("modified", lastModified, Field.Store.NO));

                try (InputStream streamPdf = new ByteArrayInputStream(content.getBytes())) {
                    doc.add(new TextField("contents", new BufferedReader(new InputStreamReader(streamPdf, StandardCharsets.UTF_8))));

                }



            }

            if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
                // New index, so we just add the document (no old document can be there):
                System.out.println("adding " + file);
                writer.addDocument(doc);
            } else {
                // Existing index (an old copy of this document may have been indexed) so
                // we use updateDocument instead to replace the old one matching the exact
                // path, if present:
                System.out.println("updating " + file);
                writer.updateDocument(new Term("path", file.toString()), doc);
            }
        }
    }
}
