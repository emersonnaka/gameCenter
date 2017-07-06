import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
//import java.util.ArrayList;
import java.util.List;

public class DAO {

	private Connection connectionDatabase;
	private PreparedStatement stmt;
    private final String url = "jdbc:mysql://localhost:3306/";
    private final String driver = "com.mysql.jdbc.Driver";
    private final String userName = "root";
    private final String password = "root";
    private final String dbName = "web";
    private final String gameTable = "Game";
    private final String mediaTable = "Media";
    private final String profileTable = "Profile";
    private final String stateTable = "State";
    private final String trophyTable = "Trophy";
    
    public DAO() {
    	String createDatabase = new String("CREATE DATABASE IF NOT EXISTS " + dbName);
    	
    	StringBuilder createGameTable = new StringBuilder();
    	createGameTable.append("CREATE TABLE IF NOT EXISTS " + gameTable + "(");
    	createGameTable.append("name varchar(100) NOT NULL,");
    	createGameTable.append("description varchar(255) NOT NULL,");
    	createGameTable.append("username varchar(255) NOT NULL,");
    	createGameTable.append("PRIMARY KEY(name),");
    	createGameTable.append("FOREIGN KEY(username) REFERENCES Profile(username))");

    	StringBuilder createMediaTable = new StringBuilder();
    	createMediaTable.append("CREATE TABLE IF NOT EXISTS " + mediaTable + "(");
    	createMediaTable.append("id int(11) NOT NULL AUTO_INCREMENT,");
    	createMediaTable.append("mimeType varchar(50) NOT NULL,");
    	createMediaTable.append("src longtext NOT NULL,");
    	createMediaTable.append("gamename varchar(100) NOT NULL,");
    	createMediaTable.append("username varchar(255) NOT NULL,");
    	createMediaTable.append("PRIMARY KEY (id),");
    	createMediaTable.append("FOREIGN KEY (gamename) REFERENCES Game(name),");
    	createMediaTable.append("FOREIGN KEY (username) REFERENCES Profile(username))");
    	
    	StringBuilder createProfileTable = new StringBuilder();
    	createProfileTable.append("CREATE TABLE IF NOT EXISTS " + profileTable + "(");
    	createProfileTable.append("username varchar(255) NOT NULL,");
    	createProfileTable.append("password varchar(20) NOT NULL,");
    	createProfileTable.append("email varchar(255),");
    	createProfileTable.append("lastLogin varchar(100) NOT NULL,");
    	createProfileTable.append("PRIMARY KEY (username))");
    	
    	StringBuilder createStateTable = new StringBuilder();
    	createStateTable.append("CREATE TABLE IF NOT EXISTS " + stateTable + "(");
    	createStateTable.append("x int(11) NOT NULL,");
    	createStateTable.append("y int(11) NOT NULL,");
    	createStateTable.append("phase int(11) NOT NULL,");
    	createStateTable.append("gamename varchar(100) NOT NULL,");
    	createStateTable.append("username varchar(255) NOT NULL,");
    	createStateTable.append("FOREIGN KEY (gamename) REFERENCES Game(name),");
    	createStateTable.append("FOREIGN KEY (username) REFERENCES Profile(username))");
    	
    	StringBuilder createTrophyTable = new StringBuilder();
    	createTrophyTable.append("CREATE TABLE IF NOT EXISTS " + trophyTable + "(");
    	createTrophyTable.append("name varchar(100) NOT NULL,");
    	createTrophyTable.append("xp int(11) NOT NULL,");
    	createTrophyTable.append("title varchar(255) NOT NULL,");
    	createTrophyTable.append("description varchar(255) NOT NULL,");
    	createTrophyTable.append("nameGame varchar(100) NOT NULL,");
    	createTrophyTable.append("username varchar(255) NOT NULL,");
    	createTrophyTable.append("FOREIGN KEY (nameGame) REFERENCES Game(name),");
    	createTrophyTable.append("FOREIGN KEY (username) REFERENCES Profile(username))");
    	
    	try {
			connectionDatabase = DriverManager.getConnection(url, userName, password);
			stmt = connectionDatabase.prepareStatement(createDatabase);
			stmt.execute();
			
			connectionDatabase = DriverManager.getConnection(url + dbName, userName, password);
			stmt = connectionDatabase.prepareStatement(createProfileTable.toString());
			stmt.execute();
			stmt = connectionDatabase.prepareStatement(createGameTable.toString());
			stmt.execute();
			stmt = connectionDatabase.prepareStatement(createMediaTable.toString());
			stmt.execute();
			stmt = connectionDatabase.prepareStatement(createStateTable.toString());
			stmt.execute();
			stmt = connectionDatabase.prepareStatement(createTrophyTable.toString());
			stmt.execute();
			
			System.out.println("Database created successfully");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
    
    public String addProfile(String username, String passwordP, String email){
    	String respOk = "{\"response\":\"ok\", \"data\":\"\"}";
    	String respErrSenha = "{\"response\":\"error\", \"data\":\"Id do usuário já existe em nossos servidores\"}";
    	String respErr = "{\"response\":\"error\", \"data\":\"Alguma exceção não esperada\"}";
    	Date date = new Date();
    	String lastLogin = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date);

    	try{
	    	System.out.println("Inserindo o profile");
	    	Class.forName(driver).newInstance();
	        connectionDatabase = DriverManager.getConnection(url + dbName, userName, password);
	        Statement statement = connectionDatabase.createStatement();
	        String sql = "INSERT INTO Profile(username, password, email, lastLogin) VALUES ( '" + username + "'," + passwordP + ",'" + email + "','" + lastLogin + "')";
	        statement.execute(sql);
	        statement.close();
	        System.out.println("Perfil inserido com sucesso!");
	        return respOk;
    	} catch (Exception ex){
    		if(ex.getMessage().contains("Duplicate")){
    			System.out.println("Erro : " + ex.getMessage());
        		return respErrSenha;
    		} else {
    			System.out.println("Erro : " + ex.getMessage());
        		return respErr;
    		}
    	}
    }
    
    public String queryProfile(String username, String passwordP){
    	PreparedStatement pst = null;
    	ResultSet rs = null;
    	/*response: 'ok', data: ' {lastLogin: '2017-06-3T18:25:43.511Z', email:'user@user.com'}'*/
    	String respOk = "";
    	String respErr = "{\"response\":\"error\", \"data\":\"Alguma exceção não esperada\"}";
    	String respErrSenha = "{\"response\":\"error\", \"data\":\"Usuário ou senha inválidos\"}";
    	Date date = new Date();
    	String lastLogin = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date);
    	
    	try{
    		System.out.println("Procurando profile");
    		Class.forName(driver).newInstance();
            connectionDatabase = DriverManager.getConnection(url + dbName, userName, password);
            pst = connectionDatabase.prepareStatement("SELECT lastLogin, email FROM " + profileTable + " WHERE username = ? AND password = ?");
            pst.setString(1, username);
            pst.setString(2, passwordP);
            rs = pst.executeQuery();
            String aux = null;
            while (rs.next()) {
                aux = "{\"lastLogin\":\"" + rs.getString("lastLogin")+"\"";
                aux += ",\"email\":\"" + rs.getString("email")+"\"}";
            }
            if(aux == null){
            	return respErrSenha;
            } else {
            	respOk = "{\"response\":\"ok\", \"data\":"+ aux + "}";
            	pst = connectionDatabase.prepareStatement("UPDATE " + profileTable + " SET lastLogin=? WHERE username=?");
            	pst.setString(1, lastLogin);
                pst.setString(2, username);
                pst.executeUpdate();
                return respOk;
            }
    	} catch (Exception ex){
    		System.out.println("Erro: " + ex.getMessage());
    		return respErr;
    	}    	
    }
    
    public String addGame(String gamename, String description, String username){

    	PreparedStatement pst = null;
    	ResultSet rs = null;
    	String respOk1 = "{\"response\":\"ok1\", \"data\":\"Jogo adicionado com sucesso ao usuário\"}";
    	String respOk2 = "{\"response\":\"ok2\", \"data\":\"Jogo já existente para o usuáro, operação ignorada\"}";
    	String respErrE = "{\"response\":\"error\", \"data\":\"Usuário não existente\"}";
    	String respErr = "{\"response\":\"error2\", \"data\":\"Alguma exceção não esperada\"}";
    	try{
	    	System.out.println("Adicionando jogo");
			Class.forName(driver).newInstance();
	        connectionDatabase = DriverManager.getConnection(url + dbName, userName, password);
	        pst = connectionDatabase.prepareStatement("SELECT email FROM " + profileTable + " WHERE username = ?");
	        pst.setString(1, username);
	        rs = pst.executeQuery();
	        String aux = null;
	        while (rs.next()) {
	            aux = rs.getString("email");
	        }
	        if(aux == null){
	        	return respErrE;
	        } else {
	        	pst = connectionDatabase.prepareStatement("SELECT name FROM " + gameTable + " WHERE username = ? AND name = ?");
		        pst.setString(1, username);
		        pst.setString(2, gamename);
		        rs = pst.executeQuery();
		        aux = null;
		        while (rs.next()) {
		            aux += rs.getString("name");
		        }

		        if(aux == null){
		        	Statement statement = connectionDatabase.createStatement();
		        	String sql = "INSERT INTO " + gameTable + " (name, description, username) VALUES ('" + gamename + "', '" 
		        					+ description + "', '" + username + "')";
		        	System.out.println("INSERINDO JOGO");
		        	statement.execute(sql);
		        	statement.close();
		        	System.out.println("Jogo inserido com sucesso");
		        	return respOk1;
		        } else {
		            return respOk2;
		        }
	        }
    	} catch (Exception ex){
    		System.out.println("Erro: " + ex.getMessage());
    		return respErr;
    	}
    }
    
    public String addTrophy(String username, String game, String name, int xp, String title, String description) {
    	PreparedStatement pst = null;
    	ResultSet rs = null;
    	String respOk = "{\"response\":\"ok\", \"data\":\"\"}";
    	String respErrJ = "{\"response\":\"error\", \"data\":\"Troféu ja inserido para esse jogador nesse mapa\"}";
    	String respErr = "{\"response\":\"error2\", \"data\":\"Exceção não esperada\"}";
    	
        try {
        	System.out.println("Inserindo o troféu");
        	Class.forName(driver).newInstance();
            connectionDatabase = DriverManager.getConnection(url + dbName, userName, password);
            Statement statement = connectionDatabase.createStatement();
            pst = connectionDatabase.prepareStatement("SELECT name FROM " + trophyTable + " WHERE username = ? AND nameGame = ?");
            pst.setString(1, username);
            pst.setString(2, game);
            rs = pst.executeQuery();
            String aux = "";
            while (rs.next()) {
                aux += rs.getString("name") + " ";
            }
            if(aux.contains(name)){
            	return respErrJ;
            } else {
            	String sql = "INSERT INTO " + trophyTable + " (name, xp, title, description, nameGame, username) VALUES ('" + name + "','" + xp 
            			+ "','" + title + "','" + description + "','" + game + "','" + username + "')";
            	statement.execute(sql);
                statement.close();
                System.out.println("Troféu inserido com sucesso!");
                return respOk;
            }
        } catch (Exception ex) {
            System.out.println("Erro : " + ex.getMessage());
            System.out.println("Troféu não inserido");
            return respErr;
        }
    }
    
    public String getTrophy(String data, String username, String game){
    	PreparedStatement pst = null;
    	ResultSet rs = null;
    	String respOk = "";
    	String respErr = "{\"response\":\"no\", \"data\":\"\"}";
    	
    	try {
    		System.out.println("Procurando por troféu");
    		
    		pst = connectionDatabase.prepareStatement("SELECT name, username FROM " + gameTable + " WHERE username = ? AND name = ?");
            pst.setString(1, username);
            pst.setString(2, game);
            rs = pst.executeQuery();
            String aux = "";
            while (rs.next()) {
                aux += rs.getString("name");
            }
            if(aux == ""){
            	return respErr;
            } else {
            	Class.forName(driver).newInstance();
                connectionDatabase = DriverManager.getConnection(url + dbName, userName, password);
                pst = connectionDatabase.prepareStatement("SELECT * FROM " + trophyTable + " WHERE name = ?");
                pst.setString(1, data);
                rs = pst.executeQuery();
                aux = "";
                while (rs.next()) {
                    aux = "{\"name\":\"" + rs.getString("name")+"\"";
                    aux += ",\"xp\":\"" + String.valueOf(rs.getInt("xp"))+"\"";
                    aux += ",\"title\":\"" + rs.getString("title")+"\"";
                    aux += ",\"description\":\"" + rs.getString("description") +"\"" + "}";
                }
                respOk = "\"response\":\"ok\", \"data\": \"" + aux + "\"";
                return respOk;
            }
    	} catch (Exception ex) {
    		System.out.println("Troféu não encontrado");
    		return respErr;
    	}
    }
    
    public String clearTrophy(String username, String gamename){
    	String resp = null;
    	
        try {
        	System.out.println("Limpando troféus salvos");
        	Class.forName(driver).newInstance();
            connectionDatabase = DriverManager.getConnection(url + dbName, userName, password);
            String sql = "DELETE FROM " + trophyTable + " WHERE username = ? AND nameGame = ?";
            PreparedStatement preparedStmt = connectionDatabase.prepareStatement(sql);
            preparedStmt.setString(1, username);
            preparedStmt.setString(2, gamename);
            preparedStmt.execute();
            preparedStmt.close();
            System.out.println("Troféus de " + username + " no jogo " + gamename + " excluídos com sucesso!");
            resp = "{\"response\":\"ok\",\"data\":\"\"}";
            return resp;

        } catch (Exception ex) {
            System.out.println("Erro : " + ex.getMessage());
            System.out.println("Troféus não removidos");
            return resp;
        }
    }
    
    public String listTrophy(String username, String gamename) {
    	PreparedStatement pst = null;
    	ResultSet rs = null;
    	List<String> respAux = new ArrayList<String>();    
    	String resp = "";
    	
    	
    	try {
    		System.out.println("Listando Troféus");
        	Class.forName(driver).newInstance();
            connectionDatabase = DriverManager.getConnection(url + dbName, userName, password);
            
            //verificando se existe o jogo pro jogador, caso não, adiciona o jogo pra ele
            pst = connectionDatabase.prepareStatement("SELECT name FROM " + gameTable + " WHERE username = ? AND name = ?");
            pst.setString(1, username);
            pst.setString(2, gamename);
            rs = pst.executeQuery();
            String aux = "";
            while (rs.next())
                aux = rs.getString("name");
            if(aux == "")
            	addGame(gamename, "Um jogo bem legal", username);
            
            pst = connectionDatabase.prepareStatement("SELECT * FROM " + trophyTable + " WHERE username = ? AND nameGame = ?");
            pst.setString(1, username);
            pst.setString(2, gamename);
            rs = pst.executeQuery();
            aux = "";
            while (rs.next()) {
                aux = "{\"name\":\"" + rs.getString("name")+"\"";
                aux += ",\"xp\":\"" + String.valueOf(rs.getInt("xp"))+"\"";
                aux += ",\"title\":\"" + rs.getString("title")+"\"";
                aux += ",\"description\":\"" + rs.getString("description") +"\"" + "}";
                respAux.add(aux);
            }
            if(respAux.isEmpty()){
            	resp = "[]";
            } else {
	            resp = "[";
	            for (int i = 0; i < respAux.size(); i++) {
					if(i+1 == respAux.size())
						resp += respAux.get(i)+"]";
					else
						resp += respAux.get(i) + ", ";
				}
            }
			return resp;

        } catch (Exception ex) {
            System.out.println("Erro : " + ex.getMessage());
			return resp ;
        }
	}

    public String saveState(int x, int y, int fase, String username, String gamename){
    	Connection conexao = null;
    	PreparedStatement pst = null;
    	ResultSet rs = null;
    	
    	String respOk = "{\"response\":\"ok\", \"data\":\"\"}";
    	String respErr = "{\"response\":\"no\", \"data\":\"\"}";
    	
        try {
        	Class.forName(driver).newInstance();
            conexao = DriverManager.getConnection(url + dbName, userName, password);
            pst = conexao.prepareStatement("SELECT phase FROM " + stateTable + " WHERE username = ? AND gamename = ?");
            pst.setString(1, username);
            pst.setString(2, gamename);
            rs = pst.executeQuery();
            String aux = "";
            while (rs.next()) {
                aux += String.valueOf(rs.getInt("phase"));
            }
            if(aux == ""){
            	Statement statement = conexao.createStatement();
                String sql = "INSERT INTO " + stateTable + " (x, y, phase, gamename, username) VALUES ("+ x + ", " + y + ", " 
                											  + fase + ",'" + gamename + "','" + username + "')";
                statement.execute(sql);
                statement.close();
                System.out.println("Estado salvo!");
                return respOk;
            } else {
            	pst = conexao.prepareStatement("UPDATE " + stateTable + " SET x=?, y=?, phase=? WHERE username=? AND gamename=?");
            	pst.setInt(1, x);
                pst.setInt(2, y);
                pst.setInt(3, fase);
                pst.setString(4, username);
                pst.setString(5, gamename);
                pst.executeUpdate();
                pst.close();
            	return respOk;
            }
            

        } catch (Exception ex) {
            System.out.println("Erro : " + ex.getMessage());
            System.out.println("Não foi possível salvar o estado");
            return respErr;
        }
    }
    
    public String loadState(String username, String gamename){
    	Connection conexao = null;
    	PreparedStatement pst = null;
    	ResultSet rs = null;
    	    
    	String resp = "";
    	
    	
    	try {
    		System.out.println("Loading state");
        	Class.forName(driver).newInstance();
            conexao = DriverManager.getConnection(url + dbName, userName, password);
            pst = conexao.prepareStatement("SELECT x, y, phase FROM " + stateTable + " WHERE username = ? AND gamename = ?");
            pst.setString(1, username);
            pst.setString(2, gamename);
            rs = pst.executeQuery();
            String aux = "";
            while (rs.next()) {
                aux = "{\"x\":\"" + String.valueOf(rs.getInt("x"))+"\"";
                aux += ",\"y\":\"" + String.valueOf(rs.getInt("y"))+"\"";
                aux += ",\"state\":\"" + String.valueOf(rs.getInt("phase"))+"\"}";
            }
            if(aux.isEmpty()){
            	resp = "{\"response\":\"erro\",\"data\":\"\"}";
            } else {
	            resp = "{\"response\":\"ok\",\"data\":" + aux + "}";
            }
			return resp;

        } catch (Exception ex) {
        	resp = "{\"response\":\"erro\",\"data\":\"\"}";
            System.out.println("Erro : " + ex.getMessage());
			return resp ;
        }
    }
    
    public String saveMedia(String username, String gamename, String mimeType, String src){
    	String respErr = "{\"response\":\"error\", \"data\":\"\"}";
    	String respOk = "{\"response\":\"ok\", \"data\":\"\"}";
    	
    	try{
	    	System.out.println("Salvando midia");
	    	Class.forName(driver).newInstance();
	        connectionDatabase = DriverManager.getConnection(url + dbName, userName, password);
	        Statement statement = connectionDatabase.createStatement();
	        String sql = "INSERT INTO Media (mimeType,src,gamename,username) VALUES ('" + mimeType + "','" + src + "','" + gamename + "','" + username + "')";
	        statement.execute(sql);
	        statement.close();
	        System.out.println("Midia inserida com sucesso!");
	        return respOk;
    	} catch (Exception ex){
    		System.out.println("Erro : " + ex.getMessage());
        	return respErr;
    	}
    }
    
    public String listMedia(String username, String gamename){
    	PreparedStatement pst = null;
    	ResultSet rs = null;
    	List<String> respAux = new ArrayList<String>();    
    	String resp = "";
    	System.out.println("Dentro listMedia");
    	
    	try {
    		System.out.println("Listando Mídias");
        	Class.forName(driver).newInstance();
            connectionDatabase = DriverManager.getConnection(url + dbName, userName, password);
            pst = connectionDatabase.prepareStatement("SELECT * FROM " + mediaTable + " WHERE username = ? AND gamename = ?");
            pst.setString(1, username);
            pst.setString(2, gamename);
            rs = pst.executeQuery();
            String aux = "";
            while (rs.next()) {
                aux = "{\"mimeType\":\"" + rs.getString("mimeType")+"\"";
                aux += ",\"src\":\"" + rs.getString("src") +"\"" + "}";
                respAux.add(aux);
            }
            if(respAux.isEmpty()){
            	resp = "[]";
            } else {
	            resp = "[";
	            for (int i = 0; i < respAux.size(); i++) {
					if(i+1 == respAux.size())
						resp += respAux.get(i)+"]";
					else
						resp += respAux.get(i) + ", ";
				}
            }
			return resp;

        } catch (Exception ex) {
            System.out.println("Erro : " + ex.getMessage());
			return resp ;
        }
    }
}
