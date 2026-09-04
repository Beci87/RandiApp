import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class RandiAlkalmazas {

    private static final String WEB3FORMS_KEY = "362b4fca-032a-4577-ab3b-a3b1b397796e";

    public static void main(String[] args) throws IOException {
        String portVar = System.getenv("PORT");
        int port = (portVar != null) ? Integer.parseInt(portVar) : 8090;

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", new KezdoOldalKezelo());
        server.createContext("/style.css", new CssKezelo());
        server.createContext("/mentes", new RandiMentoKezelo());

        server.setExecutor(null);
        System.out.println("Szerver elindult a " + port + "-es porton!");
        server.start();
    }

    static class KezdoOldalKezelo implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            java.io.File file = new java.io.File("index.html");
            if (file.exists()) {
                byte[] response = Files.readAllBytes(file.toPath());
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, response.length);
                OutputStream os = exchange.getResponseBody();
                os.write(response);
                os.close();
            } else {
                String hiba = "Hiba: Az index.html nem talalhato!";
                exchange.sendResponseHeaders(404, hiba.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(hiba.getBytes());
                os.close();
            }
        }
    }

    static class CssKezelo implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            java.io.File file = new java.io.File("style.css");
            if (file.exists()) {
                byte[] response = Files.readAllBytes(file.toPath());
                exchange.getResponseHeaders().set("Content-Type", "text/css");
                exchange.sendResponseHeaders(200, response.length);
                OutputStream os = exchange.getResponseBody();
                os.write(response);
                os.close();
            } else {
                exchange.sendResponseHeaders(404, 0);
                exchange.getResponseBody().close();
            }
        }
    }

    static class RandiMentoKezelo implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(isr);

                StringBuilder bodyBuilder = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    bodyBuilder.append(line);
                }
                String query = bodyBuilder.toString();

                String etel = "";
                String ital = "";
                String idopont = "";
                String helyszin = "";
                String dressCode = "";

                if (!query.isEmpty()) {
                    String[] pairs = query.split("&");
                    for (String pair : pairs) {
                        String[] idx = pair.split("=");
                        String kulcs = idx[0];
                        String ertek = idx.length > 1 ? URLDecoder.decode(idx[1], StandardCharsets.UTF_8) : "";

                        if ("etel".equals(kulcs)) etel = ertek;
                        if ("ital".equals(kulcs)) ital = ertek;
                        if ("idopont".equals(kulcs)) idopont = ertek;
                        if ("helyszin".equals(kulcs)) helyszin = ertek;
                        if ("dressCode".equals(kulcs)) dressCode = ertek;
                    }
                }

                System.out.println("\n❤️  RANDI VALASZ ERKEZETT! ❤️");
                System.out.println(" Etel: " + etel + " | Ital: " + ital + " | Idopont: " + idopont + " | Helyszin: " + helyszin);

                kuldEmailErtesitest(etel, ital, idopont, helyszin, dressCode);

                byte[] valaszBytes = "OK".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, valaszBytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(valaszBytes);
                os.close();
            } else {
                exchange.sendResponseHeaders(405, 0);
                exchange.getResponseBody().close();
            }
        }

        private void kuldEmailErtesitest(String etel, String ital, String idopont, String helyszin, String dressCode) {
            new Thread(() -> {
                try {
                    String uzenet = "Szia!\n\nUj randi meghivas lett elfogadva!\n\n" +
                            "Etel: " + etel + "\n" +
                            "Ital: " + ital + "\n" +
                            "Idopont: " + idopont + "\n" +
                            "Helyszin: " + helyszin + "\n" +
                            "Dress Code Tipp: " + dressCode;

                    String postData = "access_key=" + WEB3FORMS_KEY +
                            "&name=" + java.net.URLEncoder.encode("Randi Partner 😍", StandardCharsets.UTF_8) +
                            "&email=" + java.net.URLEncoder.encode("randiapp@felho.hu", StandardCharsets.UTF_8) +
                            "&subject=" + java.net.URLEncoder.encode("Uj Randi Foglalas! ❤️", StandardCharsets.UTF_8) +
                            "&message=" + java.net.URLEncoder.encode(uzenet, StandardCharsets.UTF_8);

                    java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();

                    java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                            .uri(java.net.URI.create("[https://api.web3forms.com/submit](https://api.web3forms.com/submit)"))
                            .header("Content-Type", "application/x-www-form-urlencoded")
                            .POST(java.net.http.HttpRequest.BodyPublishers.ofString(postData))
                            .build();

                    java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
                    System.out.println("E-mail szerver valasza: " + response.body());

                } catch (Exception e) {
                    System.out.println("Hiba tortent az e-mail kuldese kozben: " + e.getMessage());
                }
            }).start();
        }
    }
}