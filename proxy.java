import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport.Builder;
import java.net.InetSocketAddress;
import java.net.Proxy;

import com.google.auth.http.HttpTransportFactory;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;

public class proxy {

	public static void main(String... args) throws Exception {
		// Set proxy address and port
		String proxyAddress = "proxy.example.com";
		int proxyPort = 8080;

		// Create a HTTP transport factory with the proxy settings
		HttpTransportFactory httpTransportFactory = new HttpTransportFactory() {
			@Override
			public HttpTransport create() {
				Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyAddress, proxyPort));
				Builder builder = new NetHttpTransport.Builder();
				builder.setProxy(proxy);
				return builder.build();
			}
		};

		// Get the application default credentials
		GoogleCredentials credentials = GoogleCredentials.getApplicationDefault().createScoped(BigQueryOptions.getDefaultScopes());

		// Create a BigQuery client with the proxy-enabled HTTP transport
		BigQuery bigquery = BigQueryOptions.newBuilder()
			.setProjectId("my-project-id")
			.setCredentials(credentials)
			.setHttpTransportFactory(httpTransportFactory)
			.build()
			.getService();

		// Do something with the BigQuery client
		// ...
	}
}
