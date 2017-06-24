import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
//import java.util.ArrayList;
import java.util.List;

public class DAO {

    private static final String url = "jdbc:mysql://localhost:3306/";
    private static final String dbName = "web";
    private static final String driver = "com.mysql.jdbc.Driver";
    private static final String userName = "root";
    private static final String password = "root";
    
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
    
    public String addTrophy(String name, int xp, String title, String description) {
        Connection conexao = null;
    	String respOk = "{\"response\":\"ok\", \"data\":\"\"}";
    	String respErr = "{\"response\":\"no\", \"data\":\"\"}";
    	
        try {
        	System.out.println("Inserindo o troféu");
        	Class.forName(driver).newInstance();
            conexao = DriverManager.getConnection(url + dbName, userName, password);
            Statement statement = conexao.createStatement();
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
    	
    	Connection conexao = null;
    	PreparedStatement pst = null;
    	ResultSet rs = null;
    	String respOk = "";
    	String respErr = "{\"response\":\"no\", \"data\":\"\"}";
    	
    	try {
    		System.out.println("Procurando por troféu");
    		Class.forName(driver).newInstance();
            conexao = DriverManager.getConnection(url + dbName, userName, password);
            pst = conexao.prepareStatement("SELECT * FROM Trophy WHERE name = ?");
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
    	Connection conexao = null;
    	
        try {
        	System.out.println("Limpando troféus salvos");
        	Class.forName(driver).newInstance();
            conexao = DriverManager.getConnection(url + dbName, userName, password);
            Statement statement = conexao.createStatement();
            String sql = "TRUNCATE TABLE Trophy";
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
    	
    	Connection conexao = null;
    	PreparedStatement pst = null;
    	ResultSet rs = null;
    	List<String> respAux = new ArrayList<String>();    
    	String resp = "";
    	
    	
    	try {
    		System.out.println("Listando Troféus");
        	Class.forName(driver).newInstance();
            conexao = DriverManager.getConnection(url + dbName, userName, password);
            pst = conexao.prepareStatement("SELECT * FROM Trophy");
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
