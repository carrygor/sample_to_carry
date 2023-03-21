import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.FieldList;
import com.google.cloud.bigquery.FormatOptions;
import com.google.cloud.bigquery.Job;
import com.google.cloud.bigquery.JobInfo;
import com.google.cloud.bigquery.Table;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.bigquery.TableResult;
import com.google.cloud.bigquery.schema.Schema;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class bq {

	private static final String PROJECT_ID = "YOUR_PROJECT_ID";
	private static final String BUCKET_NAME = "YOUR_BUCKET_NAME";
	private static final String CREDENTIALS_FILE_PATH = "PATH_TO_YOUR_CREDENTIALS_FILE.json";

	private static final String EXPORT_FORMAT = "AVRO";
	private static final String EXPORT_SCHEMA_FORMAT = "JSON";

	public static void main( String[] args ) throws Exception {
		// Set up the GCP credentials and the BigQuery and Cloud Storage clients
		GoogleCredentials credentials = GoogleCredentials.fromStream(Files.newInputStream(Paths.get(CREDENTIALS_FILE_PATH)));
		BigQuery bigQuery = BigQueryOptions.newBuilder().setProjectId(PROJECT_ID).setCredentials(credentials).build().getService();
		Storage storage = StorageOptions.newBuilder().setProjectId(PROJECT_ID).setCredentials(credentials).build().getService();

		// Set up the table to be exported
		String datasetName = "your_dataset_name";
		String tableName = "your_table_name";
		TableId tableId = TableId.of(datasetName, tableName);
		Table table = bigQuery.getTable(tableId);

		// Export the table to Cloud Storage
		String objectName = tableName + ".avro";
		String destinationUri = "gs://" + BUCKET_NAME + "/" + objectName;
		Job job = table.extract(FormatOptions.of(EXPORT_FORMAT), destinationUri);
		job = job.waitFor();
		if ( job.getStatus().getError() != null ) {
			throw new RuntimeException(
				"Export job failed with error: " + job.getStatus().getError());
		}

		// Get the exported file from Cloud Storage
		BlobId blobId = BlobId.of(BUCKET_NAME, objectName);
		Blob blob = storage.get(blobId);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		blob.downloadTo(outputStream);
		File avroFile = new File(objectName);
		FileOutputStream fileOutputStream = new FileOutputStream(avroFile);
		outputStream.writeTo(fileOutputStream);
		fileOutputStream.close();

		// Get the schema of the table and save it to a file
		BigQuery bigQuery = BigQueryOptions.getDefaultInstance().getService();
		TableId tableId = TableId.of("dataset_name", "table_name");
		Table table = bigQuery.getTable(tableId);
		Schema schema = table.getDefinition().getSchema();
		String schemaJson = schema.toPrettyString();

		try {
			Schema.Parser parser = new Schema.Parser();
			Schema schemaAvro = parser.parse(schemaJson);
			File schemaFile = new File("schema.avsc");
			schemaFile.createNewFile();
			OutputStream os = new FileOutputStream(schemaFile);
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
			writer.write(schemaAvro.toString(true));
			writer.close();
		} catch (IOException ex) {
			System.err.println("Error writing schema file: " + ex.getMessage());
		}
	}
}
