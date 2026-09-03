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
        // Elindítjuk a webszervert a 8090-es porton
        HttpServer server = HttpServer.create(new InetSocketAddress(8090), 0);

        // Útvonalak regisztrálása
        server.createContext("/", new KezdoOldalKezelo());
        server.createContext("/style.css", new CssKezelo());
        server.createContext("/mentes", new RandiMentoKezelo()); // Az új adatfogadó útvonal

        server.setExecutor(null);
        System.out.println("==================================================================");
        System.out.println(" SIKER: A randi szerver elindult!");
        System.out.println(" Nyisd meg a böngészőben: http://localhost:8090");
        System.out.println("==================================================================");
        server.start();
    }

    // HTML betöltése
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
                String hiba = "Hiba: Az index.html nincs a RandiApp főmappájában! Jelenlegi helye: " + file.getAbsolutePath();
                exchange.sendResponseHeaders(404, hiba.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(hiba.getBytes());
                os.close();
            }
        }
    }

    // CSS betöltése
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

    // ÚJ: Ez a rész fogadja a HTML-ből küldött adatokat, és kiírja a konzolra
    static class RandiMentoKezelo implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                // Beolvassuk a böngészőből küldött adatokat
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(isr);
                String query = br.readLine();

                // Szétvágjuk és dekódoljuk a kapott szöveget (pl: etel=Pizza&ital=Bor...)
                String etel = "";
                String ital = "";
                String idopont = "";

                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    String[] idx = pair.split("=");
                    String kulcs = idx[0];
                    String ertek = idx.length > 1 ? URLDecoder.decode(idx[1], StandardCharsets.UTF_8) : "";

                    if ("etel".equals(kulcs)) etel = ertek;
                    if ("ital".equals(kulcs)) ital = ertek;
                    if ("idopont".equals(kulcs)) idopont = ertek;
                }

                // 🚨 ITT JELENIK MEG NEKED A KONZOLBAN A VÉGEREDMÉNY! 🚨
                System.out.println("\n==================================================");
                System.out.println(" ❤️  ÚJ RANDI VÁLASZ ÉRKEZETT! ❤️ ");
                System.out.println("==================================================");
                System.out.println(" 🍕 Kiválasztott étel : " + etel);
                System.out.println(" 🍷 Kiválasztott ital : " + ital);
                System.out.println(" 📅 Kiválasztott idő  : " + idopont);
                System.out.println("==================================================\n");

                // Válaszolunk a böngészőnek, hogy minden rendben ment
                String valasz = "OK";
                exchange.sendResponseHeaders(200, valasz.length());
                OutputStream os = exchange.getResponseBody();
                os.write(valasz.getBytes());
                os.close();
            } else {
                exchange.sendResponseHeaders(405, 0); // Method Not Allowed
                exchange.getResponseBody().close();
            }
        }
    }
}

