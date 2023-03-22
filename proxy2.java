import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;

public class proxy2 {

	public static void main(String... args) throws IOException {
		// Set proxy address and port
		Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy.example.com", 8080));

		// Set Google credentials with proxy
		GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream("/path/to/credentials.json"))
			.createScoped(BigQuery.DEFAULT_SCOPES)
			.toBuilder()
			.setTransportOptions(BigQueryOptions.getDefaultHttpTransportOptions().toBuilder().setProxy(proxy).build())
			.build();

		// Set BigQuery options with Google credentials
		BigQueryOptions bqOptions = BigQueryOptions.newBuilder()
			.setCredentials(credentials)
			.setProjectId("my-project-id")
			.build();

		// Create a BigQuery client
		BigQuery bigquery = bqOptions.getService();

		// Do something with the BigQuery client
		// ...
	}
}