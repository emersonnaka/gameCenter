import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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
    	createMediaTable.append("src varchar(255) NOT NULL,");
    	createMediaTable.append("name varchar(100) NOT NULL,");
    	createMediaTable.append("PRIMARY KEY (id),");
    	createMediaTable.append("FOREIGN KEY (name) REFERENCES Game(name))");
    	
    	StringBuilder createProfileTable = new StringBuilder();
    	createProfileTable.append("CREATE TABLE IF NOT EXISTS " + profileTable + "(");
    	createProfileTable.append("username varchar(255) NOT NULL,");
    	createProfileTable.append("password varchar(20) NOT NULL,");
    	createProfileTable.append("email varchar(255),");
    	createProfileTable.append("lastLogin varchar(100) NOT NULL,");
    	createProfileTable.append("PRIMARY KEY (username))");
    	
    	StringBuilder createStateTable = new StringBuilder();
    	createStateTable.append("CREATE TABLE IF NOT EXISTS " + stateTable + "(");
    	createStateTable.append("id int(11) NOT NULL AUTO_INCREMENT,");
    	createStateTable.append("x int(11) NOT NULL,");
    	createStateTable.append("y int(11) NOT NULL,");
    	createStateTable.append("fase int(11) NOT NULL,");
    	createStateTable.append("name varchar(100) NOT NULL,");
    	createStateTable.append("PRIMARY KEY (id),");
    	createStateTable.append("FOREIGN KEY (name) REFERENCES Game(name))");
    	
    	StringBuilder createTrophyTable = new StringBuilder();
    	createTrophyTable.append("CREATE TABLE IF NOT EXISTS " + trophyTable + "(");
    	createTrophyTable.append("name varchar(100) NOT NULL,");
    	createTrophyTable.append("xp int(11) NOT NULL,");
    	createTrophyTable.append("title varchar(255) NOT NULL,");
    	createTrophyTable.append("description varchar(255) NOT NULL,");
    	createTrophyTable.append("nameGame varchar(100) NOT NULL,");
    	createTrophyTable.append("PRIMARY KEY (name),");
    	createTrophyTable.append("FOREIGN KEY (nameGame) REFERENCES Game(name))");
    	
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
    
    public String addTrophy(String name, int xp, String title, String description) {
    	String respOk = "{\"response\":\"ok\", \"data\":\"\"}";
    	String respErr = "{\"response\":\"no\", \"data\":\"\"}";
    	
        try {
        	System.out.println("Inserindo o troféu");
        	Class.forName(driver).newInstance();
            connectionDatabase = DriverManager.getConnection(url + dbName, userName, password);
            Statement statement = connectionDatabase.createStatement();
            String sql = "INSERT INTO Trophy(name, xp, title, description) VALUES ( '" + name + "'," + xp + ",'" + title + "','" + description + "')";
            statement.execute(sql);
            statement.close();
            System.out.println("Troféu inserido com sucesso!");
            return respOk;

        } catch (Exception ex) {
            System.out.println("Erro : " + ex.getMessage());
            System.out.println("Troféu não inserido");
            return respErr;
        }
    }
    
    public String getTrophy(String data){
    	PreparedStatement pst = null;
    	ResultSet rs = null;
    	String respOk = "";
    	String respErr = "{\"response\":\"no\", \"data\":\"\"}";
    	
    	try {
    		System.out.println("Procurando por troféu");
    		Class.forName(driver).newInstance();
            connectionDatabase = DriverManager.getConnection(url + dbName, userName, password);
            pst = connectionDatabase.prepareStatement("SELECT * FROM " + trophyTable + " WHERE name = ?");
            pst.setString(1, data);
            rs = pst.executeQuery();
            String aux = "";
            while (rs.next()) {
                aux = "{\"name\":\"" + rs.getString("name")+"\"";
                aux += ",\"xp\":\"" + String.valueOf(rs.getInt("xp"))+"\"";
                aux += ",\"title\":\"" + rs.getString("title")+"\"";
                aux += ",\"description\":\"" + rs.getString("description") +"\"" + "}";
            }
            respOk = "\"response\":\"ok\", \"data\": \"" + aux + "\"";
            return respOk;
    		
    	} catch (Exception ex) {
    		System.out.println("Troféu não encontrado");
    		return respErr;
    	}
    }
    
    public String clearTrophy(){
    	String resp = null;
    	
        try {
        	System.out.println("Limpando troféus salvos");
        	Class.forName(driver).newInstance();
            connectionDatabase = DriverManager.getConnection(url + dbName, userName, password);
            Statement statement = connectionDatabase.createStatement();
            String sql = "TRUNCATE TABLE " + trophyTable;
            statement.execute(sql);
            statement.close();
            System.out.println("Troféus excluídos com sucesso!");
            resp = "{\"response\":\"ok\",\"data\":\"\"}";
            return resp;

        } catch (Exception ex) {
            System.out.println("Erro : " + ex.getMessage());
            System.out.println("Troféu não inserido");
            return resp;
        }
    }
    
    public String listTrophy() {
    	PreparedStatement pst = null;
    	ResultSet rs = null;
    	List<String> respAux = new ArrayList<String>();    
    	String resp = "";
    	
    	
    	try {
    		System.out.println("Listando Troféus");
        	Class.forName(driver).newInstance();
            connectionDatabase = DriverManager.getConnection(url + dbName, userName, password);
            pst = connectionDatabase.prepareStatement("SELECT * FROM " + trophyTable);
            rs = pst.executeQuery();
            String aux = "";
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

    public String saveState(int x, int y){
    	Connection conexao = null;
    	String respOk = "{\"response\":\"ok\", \"data\":\"\"}";
    	String respErr = "{\"response\":\"no\", \"data\":\"\"}";
    	
        try {
        	Class.forName(driver).newInstance();
            conexao = DriverManager.getConnection(url + dbName, userName, password);
            Statement statement = conexao.createStatement();
            String sql = "INSERT INTO State(x, y) VALUES ("+ x + ", " + y + ")";
            statement.execute(sql);
            statement.close();
            System.out.println("Estado salvo!");
            return respOk;

        } catch (Exception ex) {
            System.out.println("Erro : " + ex.getMessage());
            System.out.println("Não foi possível salvar o estado");
            return respErr;
        }
    }
    
    public String loadState(){
    	Connection conexao = null;
    	PreparedStatement pst = null;
    	ResultSet rs = null;
    	    
    	String resp = "";
    	
    	
    	try {
    		System.out.println("Pegando estado do um determinado id");
        	Class.forName(driver).newInstance();
            conexao = DriverManager.getConnection(url + dbName, userName, password);
            pst = conexao.prepareStatement("SELECT * FROM State");
            rs = pst.executeQuery();
            String aux = "";
            while (rs.next()) {
                aux = "{\"x\":\"" + rs.getString("x")+"\"";
                aux += ",\"y\":\"" + String.valueOf(rs.getInt("y"))+"\"}";
            }
            if(aux.isEmpty()){
            	resp = "[]";
            } else {
	            resp = "{\"response\": \"ok\", \"data\": " + aux + "}";
            }
			return resp;

        } catch (Exception ex) {
            System.out.println("Erro : " + ex.getMessage());
			return resp ;
        }
    }
    
    /*public static boolean verificaPlayer(int id) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        Connection conexao = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        Class.forName(driver).newInstance();
        conexao = DriverManager.getConnection(url + dbName, userName, password);
        pst = conexao.prepareStatement("select idplayer from Player where Player.id = " + "" + id + "");
        rs = pst.executeQuery();

        if (rs != null) {
            return true;
        } else {
            return false;
        }
    }*/

    /*public static String getPlayer() throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
        Connection conexao = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        Class.forName(driver).newInstance();
        conexao = DriverManager.getConnection(url + dbName, userName, password);
        pst = conexao.prepareStatement("select * from Player ");

        rs = pst.executeQuery();
        String aux = "";
        while (rs.next()) {
            aux = "{id:" + String.valueOf(rs.getInt("Player.id"));
            aux += ", name:" + rs.getString("name");
            aux += ", email:" + rs.getString("email") + "}";
        }
        return aux;
    }*/

    /*public static boolean atualizarUsuario(int id, String nome, String email) {
        Connection conexao = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            Class.forName(driver).newInstance();
            conexao = DriverManager.getConnection(url + dbName, userName, password);
            try (Statement statement = conexao.createStatement()) {
                String sql = "UPDATE Player SET name='" + nome + "', email='" + email + "' where Player.id = " + id + "";
                statement.execute(sql);
            }
            return true;

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException ex) {
            System.out.println("Erro : " + ex.getMessage());
            return false;
        }
    }*/

}
