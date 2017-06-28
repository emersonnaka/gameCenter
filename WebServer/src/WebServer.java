import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class WebServer {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    public static void log(String message){
    	System.out.println(dateFormat.format(Calendar.getInstance().getTime()) + " " + message);
    }

    public static void main(String argv[]) throws Exception {

        int port = 8081; // Porta que o servidor ouvirá
        String dirBase = "/"; // diretório onde estarão os arquivos

        log("Servidor Web iniciado.\n Porta:" + port + "\n Pasta WWW:" + dirBase);
        ServerSocket serverSocket = new ServerSocket(port); // Cria um servidor de socket
        
        //Descomentar para fazer a comunicação entre pcs na rede
        /*String targetURL = "http://192.168.43.133:8081";
        String urlParameters = "{\"id\":\"1\",\"op\":\"list-trophy\", \"data\":\"\"}";
        executePost(targetURL, urlParameters);*/
        
        //Multicast multicast = new Multicast();
        
        while (true) { // Loop infinito aguardando conexões
            Socket socket = serverSocket.accept(); // Escuta o socket
            new Thread(new RequesteHandle(socket, dirBase)).run();
        }
    }
    /*
    public static String executePost(String targetURL, String urlParameters) {
    	  HttpURLConnection connection = null;

    	  try {
    	    //Create connection
    	    URL url = new URL(targetURL);
    	    connection = (HttpURLConnection) url.openConnection();
    	    connection.setRequestMethod("POST");
    	    connection.setRequestProperty("Content-Type", 
    	        "application/x-www-form-urlencoded");

    	    connection.setRequestProperty("Content-Length", 
    	        Integer.toString(urlParameters.getBytes().length));
    	    connection.setRequestProperty("Content-Language", "en-US");  

    	    connection.setUseCaches(false);
    	    connection.setDoOutput(true);

    	    //Send request
    	    DataOutputStream wr = new DataOutputStream (connection.getOutputStream());
    	    wr.writeBytes(urlParameters);
    	    wr.close();

    	    //Get Response  
    	    InputStream is = connection.getInputStream();
    	    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
    	    StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
    	    String line;
    	    while ((line = rd.readLine()) != null) {
    	      response.append(line);
    	      response.append('\r');
    	    }
    	    rd.close();
    	    return response.toString();
    	  } catch (Exception e) {
    	    e.printStackTrace();
    	    return null;
    	  } finally {
    	    if (connection != null) {
    	      connection.disconnect();
    	    }
    	  }
    	}*/
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
        //this.inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
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
            WebServer.log("REQUEST RECEIVED========================");
            requestHeader = buildRequestMap();
            cookieParams = buildCookieParams();
            
            byte[] response=null;
            /* tipo da requisicao */
            if (requestHeader.containsKey("GET")) {
                response = responseGet(requestHeader);
            } else if (requestHeader.containsKey("POST")){
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

    private byte[] responsePost(HashMap<String, String> requestHeader) throws IOException{
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

	private byte[] response_postJson(HashMap<String, String> requestHeader) throws IOException{
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

	private byte[] postJson(HashMap<String, String> requestHeader) throws IOException{
		DAO dao = new DAO();
		
		String requestJson = requestHeader.get("Json");
		JsonParser parser = new JsonParser();
        JsonObject obj = parser.parse(requestJson).getAsJsonObject();
        JsonObject jsonData;
        
        String op = "";
        String response = "";
		String name;
		String username;
		String password;
		String game;
        String description;
		
        op = obj.get("op").getAsString();

        switch(op){

        	case "add-profile":
        		username = obj.get("id").getAsString();
        		jsonData = obj.getAsJsonObject("data");
        		password = jsonData.get("password").getAsString();
        		String email = jsonData.get("email").getAsString();
        		response = dao.addProfile(username, password, email);
        		break;
        	case "query-profile":
        		username = obj.get("id").getAsString();
        		jsonData = obj.getAsJsonObject("data");
        		password = jsonData.get("password").getAsString();
        		response = dao.queryProfile(username, password);
        		break;
        	case "add-game":
        		username = obj.get("id").getAsString();
        		game = obj.get("game").getAsString();
        		jsonData = obj.getAsJsonObject("data");
        		name = jsonData.get("name").getAsString();
        		description = jsonData.get("description").getAsString();
        		response = dao.addGame(username, game, name, description);
        		break;
        	case "add-trophy":
        		username = obj.get("id").getAsString();
        		game = obj.get("game").getAsString();
        		jsonData = obj.getAsJsonObject("data");
        		name = jsonData.get("name").getAsString();
        		int xp = jsonData.get("xp").getAsInt();
        		String title = jsonData.get("title").getAsString();
        		description = jsonData.get("description").getAsString();
        		response = dao.addTrophy(username, game, name, xp, title, description);
        		break;
        		
        	case "list-trophy":
        		username = obj.get("id").getAsString();
        		game = obj.get("game").getAsString();
        		response = dao.listTrophy(username,game);
        		break;
        		
        	case "get-trophy":
        		username = obj.get("id").getAsString();
        		game = obj.get("game").getAsString();
        		name = obj.get("data").getAsString();
        		response = dao.getTrophy(name, username, game);
        		break;
        		
        	case "clear-trophy":
        		username = obj.get("id").getAsString();
        		game = obj.get("game").getAsString();
        		response = dao.clearTrophy(username, game);
        		break;
        		
        	case "save-state":
        		username = obj.get("id").getAsString();
        		game = obj.get("game").getAsString();
        		jsonData = obj.getAsJsonObject("data");
        		int x = jsonData.get("x").getAsInt();
        		int y = jsonData.get("y").getAsInt();
        		int fase = jsonData.get("phase").getAsInt();
        		response = dao.saveState(x, y, fase, username, game);
        		break;
        		
        	case "load-state":
        		username = obj.get("id").getAsString();
        		game = obj.get("game").getAsString();
        		response = dao.loadState(username, game);
        }
        return response.getBytes();
	}
	
	private HashMap<String, String> buildRequestMap() throws IOException {
        
		inputReader = new BufferedReader(new InputStreamReader(this.input));
        /* monta o header em HashMap */
        HashMap<String, String> requestHeader = new HashMap<String, String>();
        String requestLine = inputReader.readLine();
        while (requestLine != null && !requestLine.trim().isEmpty()) {
            String[] split = requestLine.split(" ", 2);
            requestHeader.put(split[0], split[1]);
            WebServer.log(split[0] + " " + split[1]);
            requestLine = inputReader.readLine();
        }
        
        if (requestHeader.containsKey("POST")) {
            String jsonbody = "";
            int contentlenght = Integer.parseInt(requestHeader.get("Content-Length:"));
            for (int i = 0; i < contentlenght; i++) {
                jsonbody += (char) inputReader.read();
            }
            WebServer.log(jsonbody);
            requestHeader.put("Json", jsonbody);
        }
        
        return requestHeader;
    }

    private HashMap<String, String> buildCookieParams() throws IOException {
    	HashMap<String, String> cookieParams = new HashMap<String, String>();
    	if (requestHeader.containsKey("Cookie:")) {
    		String[] cookieLine = requestHeader.get("Cookie:").split(";");
    		for(String param: cookieLine) {
    			String[] split = param.split("=");
    			cookieParams.put(split[0].trim(), split[1].trim());
    			WebServer.log(split[0] + " " + split[1]);
    		}
    	}
    	
    	return cookieParams;
    }

    private byte[] responseGet(HashMap<String, String> requestHeader) throws Exception {
        /* se veio sem credenciais, solicita */
//        if (!requestHeader.containsKey("Authorization:")
//                || !requestHeader.get("Authorization:").equals("Basic bHVjaW86bHVjaW8=")) {
//
//            return response401_NotAuthorized();
//        } else
        {
            /* nome do recurso requisitado */
        	String[] getParams = requestHeader.get("GET").split(" ");
        	if(getParams.length != 2)
        		return response400_BadRequest();
        	
            this.requestPath = getParams[0];
            /* montando caminho absoluto do arquivo */
            String fileName = dirBase + this.requestPath;
            WebServer.log("Accessing resource:" + fileName);
            /* acesso ao arquivo */
            File file = new File(fileName);

            /* se recurso não encontrado, retorna erro */
            if (!file.exists()) {
                return response404_NotFound(this.requestPath);
            } else {
            	if(file.isDirectory())
            		return response200_processDir(file);
            	else
            		return response200_processFile(file);
            }
        }
    }

    private byte[] response200_processDir(File file) throws Exception, IOException {
        /* devolvendo o cinteúdo do diretório */
        String contentType = "text/html";

        /* processando conteudo do diretório */
        StringBuffer content =  new StringBuffer();
        content.append("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\"><html><head><title>");
        content.append("Listing content for directory " + this.requestPath);
        content.append("</title></head><body><h1>Index of "+ this.requestPath +"</h1>");
        content.append("<pre><a href=''>Name</a>                                                 <a href=''>Last modified</a>      <a href=''>Size</a><hr>");
        for(File f: file.listFiles()){
        	if(!f.isHidden()) {
        		if(f.getName().length() < 50)
        			content.append("<a href= '" + f.getAbsolutePath() + "'>" + f.getName() + "</a>");
        		else
        			content.append("<a href= '" + f.getAbsolutePath() + "'>" + f.getName().substring(0, 47) + "..." + "</a>");
	            Calendar lastModified = Calendar.getInstance();
	            lastModified.setTimeInMillis(f.lastModified());
	            if(f.getName().length() < 50)
	            	content.append(new String(new char[50 - f.getName().length()]).replace("\0",  " ") + "   ").append(DateFormat.getInstance().format(lastModified.getTime()));
	            else
	            	content.append(new String().replace("\0",  " ") + "   ").append(DateFormat.getInstance().format(lastModified.getTime()));
	            content.append("     ").append(f.length());
	            content.append("<br>");
        	}
         }
        content.append("<hr></pre>");
        content.append("Desenvolvimento Web/0.1.1b on ").append(requestHeader.get("Host:"));
        content.append("</body></html>");

        /* montando a resposta */
        String responseHeader = "HTTP/1.1 200 OK\n"
                + "Content-Length: " + content.length() + "\n"
                + "Content-Type: " + contentType + "\n"
                + "Set-Cookie: " + processCookieParams() + "\n"
                +"\n";

        /* enviando header e content da resposta */
        ByteArrayOutputStream result =  new ByteArrayOutputStream();
        result.write(responseHeader.getBytes());
        result.write(content.toString().getBytes());
        return  result.toByteArray();
    }
   
    private byte[] response200_processFile(File file) throws Exception, IOException {
        /* devolvendo o arquivo */
        String contentType = contentType(file.getName());

        /* processando conteudo do arquivo */
        byte[] content;
        if (!contentType.equals("text/html")) {
            content = sendBytes(new FileInputStream(file));
        } else {
            content = processDynamicHtml(new FileReader(file));
        }

        /* montando a resposta */
        String responseHeader = "HTTP/1.1 200 OK\n"
                + "Content-Length: " + content.length + "\n"
                + "Content-Type: " + contentType + "\n"
                + "Set-Cookie: " + processCookieParams() + "\n"
                +"\n";

        /* enviando header e content da resposta */
        ByteArrayOutputStream result =  new ByteArrayOutputStream();
        result.write(responseHeader.getBytes());
        result.write(content);
        return  result.toByteArray();
    }
    
    private String processCookieParams() {
        /* Processa o parâmetro 'counter' */
        int counter = 0;
        if(cookieParams.containsKey("counter")){
        	counter = Integer.parseInt(cookieParams.get("counter"));
        	counter++;
        }
        cookieParams.put("counter",""+counter);
        
        /* Retorna todos os parâmetros dos Cookie, inclusive os que foram recebidos */
        StringBuffer sb = new StringBuffer();
        for(Entry<String, String> entry: cookieParams.entrySet())
        	sb.append(entry.getKey()).append("=").append(entry.getValue()).append(";");
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

    /*private byte[] response401_NotAuthorized() throws IOException {
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
    }*/

    private byte[] response501_NotImplemented() throws IOException {
        String responseHeader = "HTTP/1.1 501 Not implemented\n"
                + "\n"
                + "<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\"><html><head><title>"
                + "501 Not implemented"
        		+ "</title></head><body><h1>Not implemented</h1><p>The requested URL "
                +" was under security constraint.</p><hr><address>"
                + "ValentinServer/0.1.1b on "
                + requestHeader.get("Host:")
                +"</address></body></html>";
        return responseHeader.getBytes();
    }

    private byte[] response400_BadRequest() throws IOException {
        String responseHeader = "HTTP/1.1 400 Bad Request\n"
        		+ "\n"
        		+ "<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\"><html><head><title>"
        		+ "400 Bad Request"
        		+ "</title></head><body><h1>Bad Request</h1><p>The requested URL "
        		+" was under security constraint.</p><hr><address>"
        		+ "ValentinServer/0.1.1 on "
        		+ requestHeader.get("Host:")
        		+"</address></body></html>";
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

    private byte[] processDynamicHtml(FileReader reader) {
        try {
            /* faz leitura do conteudo do arquivo */
            StringBuffer stringBuffer = new StringBuffer();
            int c = reader.read();
            while (c != -1) {
                stringBuffer.append((char) c);
                c = reader.read();
            }
            
            /* INJETA o FOOTER com o counter */
            stringBuffer.append("<footer style='color:white;background:gray;border:1px solid #a29bc0;text-align:center;' title='This footer was dynamically appended by server into html files .' >");
            stringBuffer.append("You have accessed this server <b>"+ cookieParams.get("counter") + "</b> times.");
            stringBuffer.append("</footer>");

            
            /* Inicia o tratamento de SCRIPT */
            String content = stringBuffer.toString();

            /* faz o parsing da linguagem */
            Pattern p = Pattern.compile("<%.*?%>"); // expressao regular para trechos de codigo
            Matcher m = p.matcher(content);
            while (m.find()) {
                String script = m.group(); // codigo a executar
                String scriptOut = compileScript(script);

                /* Substitui o código compilado */
                content = content.replaceFirst(script, scriptOut);
            }
            return content.getBytes();

        } catch (IOException ex) {
            Logger.getLogger(RequesteHandle.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
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
        if (fileName.endsWith(".css")) {
            return "text/css";
        }
        if (fileName.endsWith(".png")) {
            return "image/png";
        }
        
        return "application/octet-stream";
    }
}
