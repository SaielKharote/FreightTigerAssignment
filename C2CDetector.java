import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.security.cert.X509Certificate;

public class C2CDetector {

    public static void disableSSLValidation() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            // Install the custom TrustManager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static boolean isC2CWebsite(String url) {

        try {
            // Connect to the website and fetch the HTML content
            Connection.Response response = Jsoup.connect(url).execute();

            Document doc = response.parse();

            String pageContent = doc.text().toLowerCase(); // Convert to lowercase for case-insensitive search
            boolean containsBuyer = pageContent.contains("buyer");
            boolean containsSeller = pageContent.contains("seller");
            boolean containsBuy = pageContent.contains("buy");
            boolean containsSell = pageContent.contains("sell");

            return (containsBuyer && containsSeller) || (containsBuy && containsSell);
        }
        catch (HttpStatusException e) {
            // Handle HTTP status exceptions (e.g., 502)
            System.err.println("HTTP error fetching URL. Status=" + e.getStatusCode() + ", URL=" + url);
            return false;
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }


    }

    public static void main(String[] args) {
        disableSSLValidation();

        String outputFile = "C:\\Users\\Admin\\Downloads\\c2c_websites.txt"; // Path to the output file

        String csvFile = "C:\\Users\\Admin\\Downloads\\top1milliondomains.csv"; // Path to your CSV file
        String line;

        int count = 1;
        int desired_rows = 5000;  // modify the value as per requirement...

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
                boolean firstLineSkipped = false;

                while ((line = br.readLine()) != null) {
                    if (!firstLineSkipped) {
                        firstLineSkipped = true;
                        continue;
                    }

                    String[] data = line.split(",");
                    String url = "https://www." + data[1].substring(1, data[1].length() - 1);

                    if (isC2CWebsite(url)) {
                        System.out.println(url + " is a C2C platform.");
                        writer.write(url + "\n"); // Write the URL to the output file
                        count++;

                        if (count == desired_rows+1) {
                            break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
