import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
//import java.sql.Statement;
//import java.util.ArrayList;
//import java.util.List;

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
    
    /*public static boolean addTrophy(String name, int xp, String title, String description) {
        Connection conexao = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            Class.forName(driver).newInstance();
            conexao = DriverManager.getConnection(url + dbName, userName, password);
            Statement statement = conexao.createStatement();
            String sql = "INSERT INTO trophy(name, xp, title, description) VALUES ( '" + name + "'," + xp + ",'" + title + "','" + description + "')";

            statement.execute(sql);
            statement.close();
            return true;

        } catch (Exception ex) {
            System.out.println("Erro : " + ex.getMessage());
            return false;
        }
    }*/

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

    public static String getPlayer() throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
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
    }

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
