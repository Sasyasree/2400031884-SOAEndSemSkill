import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class AdditionServer {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 8000), 0);
        server.createContext("/add", AdditionServer::handleAddition);
        server.start();
        System.out.println("Addition API running at http://localhost:8000");
    }

    private static void handleAddition(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
            return;
        }

        Map<String, String> query = parseQuery(exchange.getRequestURI());
        try {
            int a = Integer.parseInt(query.get("a"));
            int b = Integer.parseInt(query.get("b"));
            sendResponse(exchange, 200,
                    String.format("{\"a\":%d,\"b\":%d,\"result\":%d}", a, b, a + b));
        } catch (NumberFormatException | NullPointerException exception) {
            sendResponse(exchange, 400,
                    "{\"error\":\"Query parameters 'a' and 'b' must be integers\"}");
        }
    }

    private static Map<String, String> parseQuery(URI uri) {
        Map<String, String> parameters = new HashMap<>();
        String query = uri.getRawQuery();
        if (query != null) {
            for (String parameter : query.split("&")) {
                String[] pair = parameter.split("=", 2);
                if (pair.length == 2) {
                    parameters.put(pair[0], pair[1]);
                }
            }
        }
        return parameters;
    }

    private static void sendResponse(HttpExchange exchange, int status, String body) throws IOException {
        byte[] response = body.getBytes();
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, response.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(response);
        }
    }
}
