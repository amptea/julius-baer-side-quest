package amptea;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

public class BankingClientTest {

    @org.junit.Test
    public void testTransferRequestSerialization() throws Exception {
        var request = new TransferRequest("ACC1000", "ACC1001", 100.0);
        var objectMapper = new ObjectMapper();

        String json = objectMapper.writeValueAsString(request);

        assertTrue(json.contains("\"fromAccount\":\"ACC1000\""));
        assertTrue(json.contains("\"toAccount\":\"ACC1001\""));
        assertTrue(json.contains("\"amount\":100.0"));
    }

    @org.junit.Test
    public void testTransferResponseDeserialization() throws Exception {
        String jsonResponse = """
                {
                    "transactionId":"12345",
                    "status":"SUCCESS",
                    "message":"Transfer completed",
                    "fromAccount":"ACC1000",
                    "toAccount":"ACC1001",
                    "amount":100.0,
                    "bonusPoints": 50
                }
                """;

        var objectMapper = new ObjectMapper();
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        TransferResponse response = objectMapper.readValue(jsonResponse, TransferResponse.class);

        assertEquals("12345", response.transactionId());
        assertEquals("SUCCESS", response.status());
        assertEquals("ACC1000", response.fromAccount());
        assertEquals("ACC1001", response.toAccount());
        assertEquals(100.0, response.amount());
    }
}
