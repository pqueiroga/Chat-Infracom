package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class BancoAmizade {
	private Connection connection = null;
	private Statement statement = null;
	private ResultSet resultSet = null;
	
	public void conectar() {
		String serv = "jdbc:mysql://localhost:3306/amizade";
		String user = "root";
		String pass = "amigos";
		String driver = "com.mysql.jdbc.Driver";
		try {
			Class.forName(driver);
			this.connection = DriverManager.getConnection(serv, user, pass);
			this.statement = this.connection.createStatement();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public boolean conectado() {
		if (this.connection != null) {
			return true;
		}
		return false;
	}
	
	public ArrayList<String> listarAmigos(String user) {
		ArrayList<String> retorno = new ArrayList<String>();
		try {
			String query = "SELECT * FROM `relacao` WHERE (`username_one` = '" + user + "'"
					+ " OR `username_two` = '" + user + "') AND `status` = 1;";
			this.resultSet = this.statement.executeQuery(query);
			
			while (this.resultSet.next()) {
				String str = this.resultSet.getString("username_one");
				if (str.equals(user)) {
					retorno.add(this.resultSet.getString("username_two"));
				} else {
					retorno.add(str);
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}	
		retorno.sort(String::compareToIgnoreCase);;
		return retorno;
	}
	
	public boolean pedirAmizade(String user, String friend) {
		String user1, user2;
		if (user.compareToIgnoreCase(friend) < 0) {
			user1 = user;
			user2 = friend;
		} else {
			user1 = friend;
			user2 = user;
		}
		try {
			String query = "INSERT INTO `relacao` (`username_one`, `username_two`, `status`, `action_username`) "
					+ "VALUES ('"+ user1 +"', '" + user2 +"', 0, '"+ user + "');";
			this.statement.executeUpdate(query);
			return true;
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return false;
	}
	
	public ArrayList<String> pedidosPendentes(String user) {
		ArrayList<String> retorno = new ArrayList<String>();
		try {
			String query = "SELECT * FROM `relacao` WHERE (`username_one` = '" + user + "' OR `username_two` = "
					+ "'" + user + "') AND `status` = 0 AND `action_username` != '" + user +"';";
			this.resultSet = this.statement.executeQuery(query);
			while (this.resultSet.next()) {
//				String str = this.resultSet.getString("username_one");
//				if (str.equals(user)) {
					retorno.add(this.resultSet.getString("action_username"));
//				} else {
//					retorno.add(str);
//				}			
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return retorno;
	}
	
	public boolean aceitarAmizade(String user, String friend) {
		String user1, user2;
		if (user.compareToIgnoreCase(friend) < 0) {
			user1 = user;
			user2 = friend;
		} else {
			user1 = friend;
			user2 = user;
		}
		try {
			String query = "UPDATE `relacao` SET `status` = 1, `action_username` = '" + user + "'"
					+ " WHERE `username_one` = '" + user1 + "' AND `username_two` = '" + user2 + "'"
							+ " AND `action_username` = '"+ friend +"';";
			this.statement.executeUpdate(query);
			return true;
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return false;
	}
	
	public boolean removerAmigo(String user, String friend) {
		String user1, user2;
		if (user.compareToIgnoreCase(friend) < 0) {
			user1 = user;
			user2 = friend;
		} else {
			user1 = friend;
			user2 = user;
		}
		try {
			String query = "DELETE FROM `relacao` WHERE `username_one` = '" + user1 +"' AND `username_two` = '" + 
					user2 + "' AND `status` = 1;";
			this.statement.executeUpdate(query);
			return true;
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return false;
	}
	
	public boolean cadastrarUsuario(String user, String salt, String pw) {
		try {	
			String query = "INSERT INTO `users` (`username`, `salt`, `password`) VALUES"
					+ "('"+ user +"', '"+ salt +"', '"+ pw +"');";
			this.statement.executeUpdate(query);
			return true;
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return false;
	}
	
	public boolean usuarioExiste(String user) {
		try {
			String query = "SELECT * FROM `users` WHERE `username` = '" + user +"';";
			this.resultSet = this.statement.executeQuery(query);
			if (!this.resultSet.next()) {
				return false;
			} else {
				return true;
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return true; // pessimismo
	}
	
	public boolean usrPwOK(String user, String pw) {
		try {
			String query = "SELECT * FROM `users` WHERE (`username` = '" + user +"' AND "
					+ "`password` = '" + pw + "');";
			this.resultSet = this.statement.executeQuery(query);
			if (!this.resultSet.next()) {
				return false;
			} else {
				return true;
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return false;
	}
	
	public ResultSet getInfo(String user) {
		ResultSet retorno = null;
		try {
			String query = "SELECT * FROM `users` WHERE `username` = '" + user + "';";
			this.resultSet = this.statement.executeQuery(query);
			if (this.resultSet.next()) {
				retorno = this.resultSet;
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return retorno;
	}
	
	public void desconectar() {
		try {
			this.connection.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public static void main(String[] args) throws SQLException {
		BancoAmizade teste = new BancoAmizade();
		teste.conectar();
		if (teste.conectado()) {
			teste.removerAmigo("pocahontas", "pedro");
			teste.removerAmigo("pedro", "daniel");
			teste.removerAmigo("pocahontas", "daniel");
			
			String tu = "pedro";
			System.out.println(tu + " existe: " + teste.usuarioExiste(tu));
			System.out.println(tu + " cadastrado: " + teste.cadastrarUsuario(tu, "sal", "senhahehe"));
			System.out.println(tu + " existe: " + teste.usuarioExiste(tu));
			System.out.println(tu + " auth: " + teste.usrPwOK(tu, "senha"));
			System.out.println(tu + " auth: " + teste.usrPwOK(tu, "senhahehe"));
			ResultSet rs = teste.getInfo(tu);
			System.out.println(tu + " username: " + rs.getString("username"));
			System.out.println(tu + " salt: " + rs.getString("salt"));
			System.out.println(tu + " pw: " + rs.getString("password"));
			
			tu = "pocahontas";
			System.out.println(tu + " existe: " + teste.usuarioExiste(tu));
			System.out.println(tu + " cadastrado: " + teste.cadastrarUsuario(tu, "salada", "senha"));
			System.out.println(tu + " existe: " + teste.usuarioExiste(tu));
			
			System.out.println(tu + " adiciona pedro " + teste.pedirAmizade(tu, "pedro"));
			
			System.out.println("amigos de pedro: " + teste.listarAmigos("pedro"));
			System.out.println("amigos de pocahontas: " + teste.listarAmigos("pocahontas"));
			
			System.out.println("pedro aceita pocahontas " + teste.aceitarAmizade("pedro", "pocahontas"));
			
			System.out.println(tu + " adiciona pedro " + teste.pedirAmizade(tu, "pedro"));
			
			System.out.println("pedro aceita pocahontas " + teste.aceitarAmizade("pedro", "pocahontas"));
			
			System.out.println("amigos de pedro: " + teste.listarAmigos("pedro"));
			System.out.println("amigos de pocahontas: " + teste.listarAmigos("pocahontas"));
			
			tu = "daniel";	
			System.out.println(tu + " existe: " + teste.usuarioExiste(tu));
			System.out.println(tu + " cadastrado: " + teste.cadastrarUsuario(tu, "saladinha", "senha"));
			System.out.println(tu + " existe: " + teste.usuarioExiste(tu));
			
			System.out.println(tu + " adiciona pedro: " + teste.pedirAmizade(tu, "pedro"));
			System.out.println(tu + " adiciona pocahontas: " + teste.pedirAmizade(tu, "pocahontas"));
			
			System.out.println("amigos de pedro: " + teste.listarAmigos("pedro"));
			System.out.println("amigos de pocahontas: " + teste.listarAmigos("pocahontas"));
			System.out.println("amigos de daniel: " + teste.listarAmigos("daniel"));
			
			System.out.println("daniel aceita pedro " + teste.aceitarAmizade("daniel", "pedro"));
			System.out.println("daniel aceita pocahontas " + teste.aceitarAmizade("daniel", "pocahontas"));
			
			System.out.println("amigos de pedro: " + teste.listarAmigos("pedro"));
			System.out.println("amigos de pocahontas: " + teste.listarAmigos("pocahontas"));
			System.out.println("amigos de daniel: " + teste.listarAmigos("daniel"));

			System.out.println("pedidos pendentes pra pedro: " + teste.pedidosPendentes("pedro"));
			System.out.println("pedidos pendentes pra pocahontas: " + teste.pedidosPendentes("pocahontas"));
			System.out.println("pedidos pendentes pra daniel: " + teste.pedidosPendentes("daniel"));
			
			System.out.println("pedro aceita daniel " + teste.aceitarAmizade("pedro", "daniel"));
			System.out.println("pocahontas aceita daniel " + teste.aceitarAmizade("pocahontas", "daniel"));
			
			System.out.println("pedidos pendentes pra pedro: " + teste.pedidosPendentes("pedro"));
			System.out.println("pedidos pendentes pra pocahontas: " + teste.pedidosPendentes("pocahontas"));
			System.out.println("pedidos pendentes pra daniel: " + teste.pedidosPendentes("daniel"));
			
			System.out.println("amigos de pedro: " + teste.listarAmigos("pedro"));
			System.out.println("amigos de pocahontas: " + teste.listarAmigos("pocahontas"));
			System.out.println("amigos de daniel: " + teste.listarAmigos("daniel"));

//			Scanner in = new Scanner(System.in);
//			in.nextLine();
//			in.close();
		}
	}
}
