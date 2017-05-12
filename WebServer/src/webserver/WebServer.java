package webserver;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class WebServer {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public static void log(String message) {
        System.out.println(dateFormat.format(Calendar.getInstance().getTime()) + " " + message);
    }

    public static void main(String argv[]) throws Exception {
        int port = 8081; // Porta que o servidor ouvirá
        String dirBase = "C:\\Users\\Maisa\\Documents\\NetBeansProjects\\WebServer\\src"; // diretório onde estarão os arquivos

//        Jogador jogador = new Jogador("Teste", "teste@teste", "teste");
//
//        Gson gson = new Gson();
//        String json = gson.toJson(jogador);
//        FileWriter writer = new FileWriter("C:\\Users\\Maisa\\Documents\\NetBeansProjects\\WebServer\\src\\www");
//        writer.write(json);
//        writer.close();
        log("Servidor Web iniciado.\n Porta:" + port + "\n Pasta WWW:" + dirBase);
        ServerSocket serverSocket = new ServerSocket(port); // Cria um servidor de socket

        while (true) { // Loop infinito aguardando conexões
            Socket socket = serverSocket.accept(); // Escuta o socket
            new Thread(new RequesteHandle(socket, dirBase)).run();
        }
    }
}

final class RequesteHandle implements Runnable {

    final static String CRLF = "\r\n";
    String dirBase;
    Socket socket;
    InputStream input;
    BufferedReader inputReader;
    OutputStream output;
    private String requestPath;
    private HashMap<String, String> requestHeader;
    private HashMap<String, String> cookieParams;

    public RequesteHandle(Socket socket, String diretorioBase) throws Exception {
        this.socket = socket;
        this.input = socket.getInputStream();
        this.inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.output = socket.getOutputStream();

        this.dirBase = diretorioBase;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        output.close();
        input.close();
        socket.close();
    }

    @Override
    public void run() {
        try {
            WebServer.log("REQUEST RECEIVED AQUI========================");
            requestHeader = buildRequestMap();
            cookieParams = buildCookieParams();
            System.out.println("HEADER ESCOLHER METODO : " + requestHeader.toString());

            byte[] response = null;
            /* tipo da requisicao */
            if (requestHeader.containsKey("GET")) {
                System.out.println("-------- Entrou GET --------");
                response = responseGet(requestHeader);

            } else if (requestHeader.containsKey("POST")) {
                System.out.println("-------- Entrou post --------");
                response = responsePost(requestHeader);

            } else {
                response = response501_NotImplemented();
            }

            output.write(response);
            WebServer.log("RESPONSE PROCESSED========================\n" + new String(response));

            finalize(); // força um fim
        } catch (Throwable e) {
            WebServer.log(e.getMessage());
            e.printStackTrace();
        }

    }

    private HashMap<String, String> buildRequestMap() throws IOException {
        System.out.println("---- buildRequestMap ---- ");
        /* monta o header em HashMap */
        HashMap<String, String> requestHeader = new HashMap<String, String>();
        String requestLine = inputReader.readLine();
        while (requestLine != null && !requestLine.trim().isEmpty()) {
            String[] split = requestLine.split(" ", 2);
            requestHeader.put(split[0], split[1]);
            WebServer.log(split[0] + " " + split[1]);
            requestLine = inputReader.readLine();

        }
//        this.requestPath = requestHeader.get("GET").split(" ")[0];
        return requestHeader;
    }

    private HashMap<String, String> buildCookieParams() throws IOException {
        HashMap<String, String> cookieParams = new HashMap<String, String>();
        if (requestHeader.containsKey("Cookie:")) {
            String[] cookieLine = requestHeader.get("Cookie:").split(";");
            for (String param : cookieLine) {
                String[] split = param.split("=");
                cookieParams.put(split[0].trim(), split[1].trim());
                WebServer.log(split[0] + " " + split[1]);
            }
        }
        return cookieParams;
    }

    private byte[] responseGet(HashMap<String, String> requestHeader) throws Exception {
        /* nome do recurso requisitado */
        String[] getParams = requestHeader.get("GET").split(" ");
        if (getParams.length != 2) {
            return response400_BadRequest();
        }

        this.requestPath = getParams[0];

        if (!this.requestPath.contains("/game/profile")) {
            return response400_BadRequest();

        } else {
            return response_getJson();
        }
    }

    private byte[] response_getJson() throws Exception, IOException {

        System.out.println("ENTROU RESPONSE JSON");
        String contentType = "application/json";

        Gson gson = new Gson();
        String json = gson.toJson(DAO.getPlayer());
        byte[] jsonBanco = json.getBytes();

        String responseHeader = "HTTP/1.1 200 OK\n"
                + "Content-Length: " + jsonBanco.length + "\n"
                + "Content\"application/json\";\n"
                + "        byte[] content = getJsonFromBD();\n"
                + "        String responseHeader = \"HTTP/1.1 200 OK\\n\"\n"
                + "                + \"Content-Leng-Type: " + contentType + "\n"
                + "Set-Cookie: " + processCookieParams() + "\n"
                + "\n";

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        result.write(responseHeader.getBytes());
        result.write(jsonBanco);

        return result.toByteArray();
    }

    private byte[] responsePost(HashMap<String, String> requestHeader) throws Exception {
        String[] getParams = requestHeader.get("POST").split(" ");

        if (getParams.length != 2) {
            return response400_BadRequest();
        }

        this.requestPath = getParams[0];

        if (!this.requestPath.contains("/game/profile")) {
            return response400_BadRequest();

        } else {
            return response_postJson(requestHeader);
        }
    }

    private byte[] response_postJson(HashMap<String, String> requestHeader) throws Exception, IOException {

        System.out.println("-------- responde_postJson() --------");
        String contentType = "application/json";
        byte[] content = postJson(requestHeader);

        String responseHeader = "HTTP/1.1 200 OK\n"
                + "Content-Length: " + content.length + "\n"
                + "Content-Type: " + contentType + "\n"
                + "Set-Cookie: " + processCookieParams() + "\n"
                + "\n";

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        result.write(responseHeader.getBytes());
        result.write(content);
        return result.toByteArray();
    }

    private byte[] postJson(HashMap<String, String> requestHeader) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException, IOException {

        String requestJson = inputReader.readLine();
        String op = "";
        String dataAux[] = null;
        String data[] = null;
        int id = 0;
        String[] jason = null;
        boolean flagOk = false;

        System.out.println("HEADER --- " + requestHeader.toString() + "----------");
        System.out.println("JSON --" + requestJson);

        jason = requestJson.replace("{", "").replace("}", "").replace("\"", "").split(",");

        System.out.println("JASON " + Arrays.toString(jason));

        id = Integer.parseInt(jason[0].substring((jason[0].indexOf(":") + 1), (jason[0].length())));
        op = jason[1].substring((jason[1].indexOf(":") + 1), (jason[1].length()));

//        System.out.println("ID : " + id + " OP:  " + op);
        if (op.equalsIgnoreCase("add-trophy")) {
//            System.out.println("Teste : " + requestJson.replace("\"", "").substring(requestJson.lastIndexOf("{"), requestJson.lastIndexOf("}")));
            dataAux = requestJson.replace("\"", "").replace("}", "").split("\\{");
            data = dataAux[2].replace("\"", "").split(",");
//            String name = data[1].split(",")[0].substring()
            System.out.println("DATA " + Arrays.toString(dataAux));
            System.out.println("DATA " + Arrays.toString(data));
            String name = data[0].substring((data[0].indexOf(":") + 1), (data[0].length())).trim();
            int xp = Integer.parseInt(data[1].substring((data[1].indexOf(":") + 1), (data[1].length())).trim());
            String title = data[2].substring((data[2].indexOf(":") + 1), (data[2].length())).trim();
            String description = data[3].substring((data[3].indexOf(":") + 1), (data[3].length())).trim();
            
            System.out.println("nome : " + name);
            System.out.println("XP : " + xp);
            System.out.println("Title : " + title);
            System.out.println("Description : " + description);
            
            boolean addTrophyOk = DAO.addTrophy(name, xp, title, description);
            
            System.out.println("Boolean " + addTrophyOk);

//            flagOk = DAO.addTrophy(op, id, CRLF, requestJson)
        }

//        requestJson = requestHeader.get("Json");
//        JsonObject jsonObject = new JsonParser().parse(requestJson).getAsJsonObject();
//        System.out.println("JSON" + jsonObject.getAsString());
//        if (!DAO.verificaPlayer(jsonObject.get("id").getAsInt())) {
//            DAO.addPlayer(jsonObject.get("id").getAsInt(), jsonObject.get("nome").getAsString(), jsonObject.get("email").getAsString());
//        } else {
//            DAO.atualizarUsuario(jsonObject.get("id").getAsInt(), jsonObject.get("nome").getAsString(), jsonObject.get("email").getAsString());
//        }
//
//        String content = jsonObject.toString();
//        return content.getBytes();
        return new byte[1];
    }

    private String processCookieParams() {
        /* Processa o parâmetro 'counter' */
        int counter = 0;
        if (cookieParams.containsKey("counter")) {
            counter = Integer.parseInt(cookieParams.get("counter"));
            counter++;
        }
        cookieParams.put("counter", "" + counter);

        /* Retorna todos os parâmetros dos Cookie, inclusive os que foram recebidos */
        StringBuffer sb = new StringBuffer();
        for (Entry<String, String> entry : cookieParams.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append(";");
        }
        return sb.toString();
    }

    private byte[] response404_NotFound(String resource) throws IOException {
        String responseHeader = "HTTP/1.1 404 Not found\n"
                + "\n"
                + "<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\"><html><head><title>"
                + "404 Not Found"
                + "</title></head><body><h1>Not Found</h1><p>The requested URL "
                + resource
                + " was not found on this server.</p><hr><address>"
                + "ValentinServer/0.1.1b on "
                + requestHeader.get("Host:")
                + "</address></body></html>";
        return responseHeader.getBytes();
    }

    private byte[] response401_NotAuthorized() throws IOException {
        String responseHeader = "HTTP/1.1 401 Not Authorized\n"
                + "WWW-Authenticate: Basic realm=\"Entre com usuário e senha\"\n"
                + "\n"
                + "<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\"><html><head><title>"
                + "401 Not Authorized"
                + "</title></head><body><h1>Not Authorized</h1><p>The requested URL "
                + " was under security constraint.</p><hr><address>"
                + "Valentin/0.1.1b on "
                + requestHeader.get("Host:")
                + "</address></body></html>";

        return responseHeader.getBytes();
    }

    private byte[] response501_NotImplemented() throws IOException {
        String responseHeader = "HTTP/1.1 501 Not implemented\n"
                + "\n"
                + "<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\"><html><head><title>"
                + "501 Not implemented"
                + "</title></head><body><h1>Not implemented</h1><p>The requested URL "
                + " was under security constraint.</p><hr><address>"
                + "ValentinServer/0.1.1b on "
                + requestHeader.get("Host:")
                + "</address></body></html>";
        return responseHeader.getBytes();
    }

    private byte[] response400_BadRequest() throws IOException {
        String responseHeader = "HTTP/1.1 400 Bad Request\n"
                + "\n"
                + "<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\"><html><head><title>"
                + "400 Bad Request"
                + "</title></head><body><h1>Bad Request</h1><p>The requested URL "
                + " was under security constraint.</p><hr><address>"
                + "ValentinServer/0.1.1 on "
                + requestHeader.get("Host:")
                + "</address></body></html>";
        return responseHeader.getBytes();
    }

    private static byte[] sendBytes(FileInputStream fis)
            throws Exception {
        byte[] buffer = new byte[1024];
        int bytes = 0;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        while ((bytes = fis.read(buffer)) != -1) {
            stream.write(buffer, 0, bytes);
        }
        stream.flush();
        return stream.toByteArray();
    }

    private String compileScript(String source) {
        source = source.substring(2, source.length() - 2);
        String out = "";
        String[] statements = source.split(";");
        for (int i = 0; i < statements.length; i++) {
            String stm = statements[i].trim();
            String result = "";
            if (stm.equals("now")) {
                result = Calendar.getInstance().getTime().toString();

            } else if (stm.startsWith("host")) {
                result = System.getProperty("os.name");

            } else if (stm.startsWith("sysinfo")) {
                Properties prop = System.getProperties();
                Enumeration<?> e = prop.propertyNames();
                while (e.hasMoreElements()) {
                    String key = (String) e.nextElement();
                    result += (key + ": " + prop.getProperty(key)) + "<br />";
                }
            }
            out += result;
        }
        return out;
    }

    private static String contentType(String fileName) {
        if (fileName.endsWith(".htm") || fileName.endsWith(".html")) {
            return "text/html";
        }
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (fileName.endsWith(".gif")) {
            return "image/gif";
        }
        if (fileName.endsWith(".txt")) {
            return "text/plain";
        }
        if (fileName.endsWith(".pdf")) {
            return "application/pdf";
        }
        return "application/octet-stream";
    }
}
