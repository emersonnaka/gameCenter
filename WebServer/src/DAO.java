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
    private final String trophyTable = "Trophy";
    
    /*public static boolean addPlayer(int id, String nome, String email) {
        Connection conexao = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            Class.forName(driver).newInstance();
            conexao = DriverManager.getConnection(url + dbName, userName, password);
            Statement statement = conexao.createStatement();
            String sql = "INSERT INTO Player(id, name, email) VALUES (" + id + ",'" + nome + "','" + email + "')";

            statement.execute(sql);
            statement.close();
            return true;

        } catch (Exception ex) {
            System.out.println("Erro : " + ex.getMessage());
            return false;
        }
    }*/
    
    public DAO() {
    	String createDatabase = new String("CREATE DATABASE IF NOT EXISTS " + dbName);
    	StringBuilder createTrophyTable = new StringBuilder();
    	createTrophyTable.append("CREATE TABLE IF NOT EXISTS " + trophyTable + "(");
    	createTrophyTable.append("`id` int(11) NOT NULL AUTO_INCREMENT, ");
    	createTrophyTable.append("`name` varchar(255) DEFAULT NULL, ");
    	createTrophyTable.append("`xp` int(11) DEFAULT NULL, ");
    	createTrophyTable.append("`title` varchar(255) DEFAULT NULL, ");
    	createTrophyTable.append("`description` varchar(255) DEFAULT NULL, ");
    	createTrophyTable.append("PRIMARY KEY (`id`))");
    	
    	try {
			connectionDatabase = DriverManager.getConnection(url, userName, password);
			stmt = connectionDatabase.prepareStatement(createDatabase);
			stmt.execute();
			
			connectionDatabase = DriverManager.getConnection(url + dbName, userName, password);
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
