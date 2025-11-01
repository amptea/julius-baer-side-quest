package amptea;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

// Immutable DTOs using records
record TransferRequest(
        @JsonProperty("fromAccount") String fromAccount,
        @JsonProperty("toAccount") String toAccount,
        @JsonProperty("amount") double amount
) {}

@JsonIgnoreProperties(ignoreUnknown = true)
record TransferResponse(
        @JsonProperty("transactionId") String transactionId,
        @JsonProperty("status") String status,
        @JsonProperty("message") String message,
        @JsonProperty("fromAccount") String fromAccount,
        @JsonProperty("toAccount") String toAccount,
        @JsonProperty("amount") double amount
) {}

public class BankingClient {

    private static final Logger logger = Logger.getLogger(BankingClient.class.getName());

    private static final String BASE_URL = "http://localhost:8123";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public BankingClient() {
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.configure(
                com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public TransferResponse transferFunds(String fromAccount, String toAccount, double amount) throws Exception {
        logger.info(String.format("Starting transfer from %s to %s for amount %.2f", fromAccount, toAccount, amount));

        var requestPayload = new TransferRequest(fromAccount, toAccount, amount);
        var jsonRequest = objectMapper.writeValueAsString(requestPayload);

        var request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/transfer"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                .build();

        logger.fine("Request JSON: " + jsonRequest);

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        logger.info("Received HTTP status: " + response.statusCode());
        logger.fine("Response body: " + response.body());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            var transferResponse = objectMapper.readValue(response.body(), TransferResponse.class);
            logger.info("Transfer successful: " + transferResponse.transactionId());
            return transferResponse;
        } else {
            logger.severe("Transfer failed with body: " + response.body());
            throw new RuntimeException("Failed to transfer funds: " + response.body());
        }
    }

    public static void main(String[] args) {
        var client = new BankingClient();
        try {
            var response = client.transferFunds("ACC1000", "ACC1001", 100.00);
            System.out.println("Transfer successful:");
            System.out.println("Transaction ID: " + response.transactionId());
            System.out.println("Status: " + response.status());
            System.out.println("Message: " + response.message());
            System.out.println("From: " + response.fromAccount());
            System.out.println("To: " + response.toAccount());
            System.out.println("Amount: " + response.amount());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error during transfer", e);
        }
    }
}
