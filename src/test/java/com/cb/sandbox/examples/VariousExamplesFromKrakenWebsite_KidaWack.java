package com.cb.sandbox.examples;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.net.http.WebSocket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.security.MessageDigest;

public class VariousExamplesFromKrakenWebsite_KidaWack {

    public static void main(String[] args) throws Exception {
        // TODO: UPDATE WITH YOUR KEYS :)
        String apiPublicKey = "YOUR_PUBLIC_KEY";
        String apiPrivateKey = "YOUR_PRIVATE_KEY";
        try {
            System.out.println("|=========================================|");
            System.out.println("| KRAKEN.COM JAVA TEST APP |");
            System.out.println("|=========================================|");
            System.out.println();
            /*
             * PUBLIC REST API Examples
             */
            if (1 == 0) {
                CompletableFuture<String> publicResponse;
                String publicEndpoint = "SystemStatus";
                String publicInputParameters = "";
                /*
                 * MORE PUBLIC REST EXAMPLES
                 *
                 * String publicEndpoint = "AssetPairs";
                 * String publicInputParameters = "pair=ethusd,xbtusd";
                 *
                 * String publicEndpoint = "Ticker";
                 * String publicInputParameters = "pair=ethusd";
                 *
                 * String publicEndpoint = "Trades";
                 * String publicInputParameters = "pair=ethusd&since=0";
                 */
                publicResponse = QueryPublicEndpoint(publicEndpoint, publicInputParameters);
                while (publicResponse.isDone() == false) {
                    // WAIT
                }
                System.out.println(publicResponse.get());
            }
            /*
             * PRIVATE REST API Examples
             */
            if (1 == 0) {
                String privateResponse = "";
                String privateEndpoint = "Balance";
                String privateInputParameters = "";
                /*
                 * MORE PRIVATE REST EXAMPLES
                 *
                 * String privateEndpoint = "AddOrder";
                 * String privateInputParameters =
                 * "pair=xbteur&type=buy&ordertype=limit&price=1.00&volume=1";
                 *
                 * String privateEndpoint = "AddOrder"
                 * String privateInputParameters =
                 * "pair=xdgeur&type=sell&ordertype=limit&volume=3000&price=%2b10.0%" //Positive
                 * Percentage Example (%2 represtes +, which is a reseved character in HTTP)
                 *
                 * String privateEndpoint = "AddOrder"
                 * String privateInputParameters =
                 * "pair=xdgeur&type=sell&ordertype=limit&volume=3000&price=-10.0%" //Negative
                 * Percentage Example
                 *
                 * String privateEndpoint = "AddOrder"
                 * String privateInputParameters =
                 * "pair=xdgeur&type=buy&ordertype=market&volume=3000&userref=789" //Userref
                 * Example
                 *
                 * String privateEndpoint = "Balance" //{"error":[]} IS SUCCESS, Means EMPTY
                 * BALANCE
                 * String privateInputParameters = ""
                 *
                 * String privateEndpoint = "QueryOrders"
                 * String privateInputParameters = "txid=OFUSL6-GXIIT-KZ2JDJ"
                 *
                 * String privateEndpoint = "AddOrder"
                 * String privateInputParameters =
                 * "pair=xdgusd&type=buy&ordertype=market&volume=5000"
                 *
                 * String privateEndpoint = "DepositAddresses"
                 * String privateInputParameters = "asset=xbt&method=Bitcoin"
                 *
                 * String privateEndpoint = "DepositMethods"
                 * String privateInputParameters = "asset=eth"
                 *
                 * String privateEndpoint = "WalletTransfer"
                 * String privateInputParameters =
                 * "asset=xbt&to=Futures Wallet&from=Spot Wallet&amount=0.0045"
                 *
                 * String privateEndpoint = "TradesHistory"
                 * String privateInputParameters = "start=1577836800&end=1609459200"
                 *
                 * String privateEndpoint = "GetWebSocketsToken"
                 * String privateInputParameters = ""
                 */
                privateResponse = QueryPrivateEndpoint(privateEndpoint,
                        privateInputParameters,
                        apiPublicKey,
                        apiPrivateKey);
                System.out.println(privateResponse);
            }
            /*
             * PUBLIC WEBSOCKET Examples
             */
            if (1 == 0) {
                String publicWebSocketURL = "wss://ws.kraken.com/";
                String publicWebSocketSubscriptionMsg = "{ \"event\":\"subscribe\", \"subscription\":{\"name\":\"trade\"},\"pair\":[\"XBT/USD\"] }";
                /*
                 * MORE PUBLIC WEBSOCKET EXAMPLES
                 *
                 * String publicWebSocketSubscriptionMsg =
                 * "{ \"event\": \"subscribe\", \"subscription\": { \"interval\": 1440, \"name\": \"ohlc\"}, \"pair\": [ \"XBT/EUR\" ]}"
                 * ;
                 * String publicWebSocketSubscriptionMsg =
                 * "{ \"event\": \"subscribe\", \"subscription\": { \"name\": \"spread\"}, \"pair\": [ \"XBT/EUR\",\"ETH/USD\" ]}"
                 * ;
                 */
                OpenAndStreamWebSocketSubscription(publicWebSocketURL, publicWebSocketSubscriptionMsg);
            }
            /*
             * PRIVATE WEBSOCKET Examples
             */
            if (1 == 0) {
                String privateWebSocketURL = "wss://ws-auth.kraken.com/";
                // GET THE WEBSOCKET TOKEN FROM THE JSON RESPONSE
                Object webSocketToken = "";

               String tokenQuery = QueryPrivateEndpoint("GetWebSocketsToken", "", apiPublicKey, apiPrivateKey);

               JsonFactory factory = new JsonFactory();
               ObjectMapper mapper = new ObjectMapper(factory);
               TypeReference<HashMap<String, HashMap<String, Object>>> typeRef = new TypeReference<>() {};
               HashMap<String, HashMap<String, Object>> o = mapper.readValue(tokenQuery, typeRef);
               webSocketToken = o.get("result").get("token");
                /*
                 * MORE PRIVATE WEBSOCKET EXAMPLES
                 *
                 * String privateWebSocketSubscriptionMsg =
                 * "{ \"event\": \"subscribe\", \"subscription\": { \"name\": \"openOrders\", \"token\": \""
                 * + webSocketToken + "\" }}";
                 * String privateWebSocketSubscriptionMsg =
                 * "{ \"event\": \"subscribe\", \"subscription\": { \"name\": \"balances\", \"token\": \""
                 * + webSocketToken + "\" }}";
                 * String privateWebSocketSubscriptionMsg =
                 * "{ \"event\":\"addOrder\",\"reqid\":1234,\"ordertype\":\"limit\",\"pair\":\"XBT/EUR\",\"token\":\""
                 * + webSocketToken +
                 * "\",\"type\":\"buy\",\"volume\":\"1\", \"price\":\"1.00\"}";
                 */
                // REPLACE PLACEHOLDER WITH TOKEN
                String privateWebSocketSubscriptionMsg = "{ \"event\": \"subscribe\", \"subscription\": { \"name\": \"balances\", \"token\": \""
                        + webSocketToken + "\" }}";
                System.out.println(privateWebSocketSubscriptionMsg);
                OpenAndStreamWebSocketSubscription(privateWebSocketURL, privateWebSocketSubscriptionMsg);
            }
            System.out.println("|=======================================|");
            System.out.println("| END OF PROGRAM - HAVE A GOOD DAY :) |");
            System.out.println("|=======================================|");
            System.out.println("\n");
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    /*
     * Public REST API Endpoints
     */
    public static CompletableFuture<String> QueryPublicEndpoint(String endPointName, String inputParameters) {
        CompletableFuture<String> jsonData;
        String baseDomain = "https://api.kraken.com";
        String publicPath = "/0/public/";
        String apiEndpointFullURL = baseDomain + publicPath + endPointName + "?" + inputParameters;
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiEndpointFullURL)).build();
        jsonData = client.sendAsync(request, BodyHandlers.ofString()).thenApply(HttpResponse::body);
        return jsonData;
    }
    /*
     * Private REST API Endpoints
     */
    public static String QueryPrivateEndpoint(String endPointName, String inputParameters, String apiPublicKey, String apiPrivateKey) {
        String responseJson = "";
        String baseDomain = "https://api.kraken.com";
        String privatePath = "/0/private/";
        String apiEndpointFullURL = baseDomain + privatePath + endPointName + "?" + inputParameters;
        String nonce = String.valueOf(System.currentTimeMillis());
        String apiPostBodyData = "nonce=" + nonce + "&" + inputParameters;
        String signature = CreateAuthenticationSignature(apiPrivateKey,
                privatePath,
                endPointName,
                nonce,
                apiPostBodyData);
        // CREATE HTTP CONNECTION
        try {
            HttpsURLConnection httpConnection = null;
            URL apiUrl = new URL(apiEndpointFullURL);
            httpConnection = (HttpsURLConnection) apiUrl.openConnection();
            httpConnection.setRequestMethod("POST");
            httpConnection.setRequestProperty("API-Key", apiPublicKey);
            httpConnection.setRequestProperty("API-Sign", signature);
            httpConnection.setDoOutput(true);
            DataOutputStream os = new DataOutputStream(httpConnection.getOutputStream());
            os.writeBytes(apiPostBodyData);
            os.flush();
            os.close();
            BufferedReader br = null;
            // GET JSON RESPONSE DATA
            br = new BufferedReader(new InputStreamReader((httpConnection.getInputStream())));
            String line;
            while ((line = br.readLine()) != null) {
                responseJson += line;
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return responseJson;
    }
    /*
     * Authentication Algorithm
     */
    public static String CreateAuthenticationSignature(String apiPrivateKey,
                                                    String apiPath,
                                                    String endPointName,
                                                    String nonce,
                                                    String apiPostBodyData) {
        try {
            // GET 256 HASH
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update((nonce + apiPostBodyData).getBytes());
            byte[] sha256Hash = md.digest();
            // GET 512 HASH
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(Base64.getDecoder().decode(apiPrivateKey.getBytes()), "HmacSHA512"));
            mac.update((apiPath + endPointName).getBytes());
            // CREATE API SIGNATURE
            String signature = new String(Base64.getEncoder().encodeToString(mac.doFinal(sha256Hash)));
            return signature;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
    /*
     * WebSocket API
     */
    public static void OpenAndStreamWebSocketSubscription(String connectionURL, String webSocketSubscription) {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            WebSocket ws = HttpClient
                    .newHttpClient()
                    .newWebSocketBuilder()
                    .buildAsync(URI.create(connectionURL), new WebSocketClient(latch))
                    .join();
            ws.sendText(webSocketSubscription, true);
            latch.await();
        } catch (Exception e) {
            System.out.println();
            System.out.println("AN EXCEPTION OCCURED :(");
            System.out.println(e);
        }
    }
    private static class WebSocketClient implements WebSocket.Listener
    {
        private final CountDownLatch latch;

        public WebSocketClient(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onOpen(WebSocket webSocket) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            System.out.println(dtf.format(now) + ": " + webSocket.getSubprotocol());
            WebSocket.Listener.super.onOpen(webSocket);
        }

        @Override
        public CompletionStage<?> onText (WebSocket webSocket, CharSequence data, boolean last){
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            System.out.println(dtf.format(now) + ": " + data);
            return WebSocket.Listener.super.onText(webSocket, data, false);
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            System.out.println("ERROR OCCURED: " + webSocket.toString());
            WebSocket.Listener.super.onError(webSocket, error);
        }
    }

}