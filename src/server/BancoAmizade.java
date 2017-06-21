package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

public class BancoAmizade {
	private Connection connection = null;
	private Statement statement = null;
	private ResultSet resultSet = null;
	
	public void conectar() {
		String serv = "jdbc:mysql://localhost:3306/amizade?autoReconnect=true&useSSL=false";
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
		retorno.sort(String::compareToIgnoreCase);
		return retorno;
	}
	
	/**
	 * user Solicita amizade pro friend.
	 * @param user usuário que pede a amizade
	 * @param friend usuário que receberá solicitação de amizade
	 * @return verdadeiro se conseguiu executar a operação, falso caso contrário
	 * @throws MySQLIntegrityConstraintViolationException para que você saiba que o problema foi
	 * de pedido duplicado
	 */
	public boolean pedirAmizade(String user, String friend) throws MySQLIntegrityConstraintViolationException {
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
		} catch (com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException e) {
			System.out.println(e.getMessage());
			throw e;
		} catch (SQLException e) {
			e.printStackTrace();
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
				retorno.add(this.resultSet.getString("action_username"));			
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		retorno.sort(String::compareToIgnoreCase);
		return retorno;
	}
	
	/**
	 * Atualiza banco de dados para status ficar em 1 (que significa que são amigos)
	 * @param user
	 * @param friend
	 * @return verdadeiro se conseguiu executar a operação.<br>falso caso contrário
	 */
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
	
	/**
	 * Na verdade tanto faz a ordem do user/friend, pois a relação é feita sempre com o usuário menor
	 * (ordem alfabética) ficando à esquerda do maior (menor, maior), e é removido isso.
	 * Remove pendentes também, serve pra recusar solicitação de amizade.
	 * @param user nome de usuário de quem pede pra remover
	 * @param friend amigo que foi pedido pra ser removido
	 * @return verdadeiro se conseguiu mexer (remover ou não encontrou, que dá no mesmo)
	 * <br>falso caso não tenha conseguido executar a operação
	 */
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
					user2 + "';"; // remove pendentes também, serve pra recusar solicitação.
			this.statement.executeUpdate(query);
			return true;
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return false;
	}
	
	/**
	 * Cadastra user com salt e pw: insere na tabela users.
	 * @param user usuário
	 * @param salt sal do hash
	 * @param pw senha
	 * @return verdadeiro se conseguiu cadastrar, falso cc.
	 * @throws MySQLIntegrityConstraintViolationException 
	 */
	public boolean cadastrarUsuario(String user, String salt, String pw) throws MySQLIntegrityConstraintViolationException {
		try {	
			String query = "INSERT INTO `users` (`username`, `salt`, `password`) VALUES"
					+ "('"+ user +"', '"+ salt +"', '"+ pw +"');";
			this.statement.executeUpdate(query);
			return true;
		} catch (MySQLIntegrityConstraintViolationException e) {
			System.out.println(e.getMessage());
			throw e;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	
	public boolean usrAuthOK(String user, String salt, String pw) {
		try {
			String query = "SELECT * FROM `users` WHERE (`username` = '" + user +"' AND "
					+ "`salt` = '" + salt + "' AND `password` = '" + pw + "');";
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
	
	/**
	 * Pega as colunas (username, salt e pw) do user.
	 * @param user Nome de usuário a ser procurado.
	 * @return null se user não existe, row do user se existe (com username, salt e pw nas colunas)
	 */
	public ArrayList<String> getInfo(String user) {
		ArrayList<String> retorno = new ArrayList<String>();
		try {
			String query = "SELECT * FROM `users` WHERE `username` = '" + user + "';";
			this.resultSet = this.statement.executeQuery(query);
			if (this.resultSet.next()) {
				retorno.add(this.resultSet.getString("username"));
				retorno.add(this.resultSet.getString("salt"));
				retorno.add(this.resultSet.getString("password"));
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
	/*
	public static void main(String[] args) throws SQLException {
		BancoAmizade teste = new BancoAmizade();
		teste.conectar();
		if (teste.conectado()) {
			teste.removerAmigo("pocahontas", "pedro");
			teste.removerAmigo("pedro", "daniel");
			teste.removerAmigo("pocahontas", "daniel");
			try {
				System.out.println("pedro adiciona cleide: " + teste.pedirAmizade("pedro", "pocahontas"));
			} catch (com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException e) {
				System.out.println("Já existe uma relação para esses usuários!");
			}
			try {
				System.out.println("pocahontas adiciona pedro: " + teste.pedirAmizade("pocahonas", "pedro"));
				System.out.println("pedro adiciona cleide: " + teste.pedirAmizade("pedro", "pocahontas"));
				System.out.println("pocahontas adiciona pedro: " + teste.pedirAmizade("pocahonas", "pedro"));
			} catch (com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException e) {
				System.out.println("Já existe uma relação para esses usuários!");
			}
			ArrayList<String> rsrs = teste.getInfo("poseidon");
			if (rsrs == null) {
				System.out.println(rsrs);
			} else {
				System.out.println("poseidon username: " + rsrs.get(0));
				System.out.println("poseidon salt: " + rsrs.get(1));
				System.out.println("poseidon pw: " + rsrs.get(2));
			}
			try {
				System.out.println("poseidon adiciona pedro" + teste.pedirAmizade("poseidon", "pedro"));
			} catch (MySQLIntegrityConstraintViolationException e) {
				System.out.println(e.getMessage());
			}
			try {
				System.out.println("pedro adiciona poseidon" + teste.pedirAmizade("pedro", "poseidon"));
			} catch (MySQLIntegrityConstraintViolationException e) {
				System.out.println(e.getMessage());
			}

			
//			String tu = "pedro";
//			System.out.println(tu + " existe: " + teste.usuarioExiste(tu));
//			System.out.println(tu + " cadastrado: " + teste.cadastrarUsuario(tu, "sal", "senhahehe"));
//			System.out.println(tu + " existe: " + teste.usuarioExiste(tu));
//			System.out.println(tu + " auth: " + teste.usrPwOK(tu, "senha"));
//			System.out.println(tu + " auth: " + teste.usrPwOK(tu, "senhahehe"));
//			ResultSet rs = teste.getInfo(tu);
//			System.out.println(tu + " username: " + rs.getString("username"));
//			System.out.println(tu + " salt: " + rs.getString("salt"));
//			System.out.println(tu + " pw: " + rs.getString("password"));
//			
//			tu = "pocahontas";
//			System.out.println(tu + " existe: " + teste.usuarioExiste(tu));
//			System.out.println(tu + " cadastrado: " + teste.cadastrarUsuario(tu, "salada", "senha"));
//			System.out.println(tu + " existe: " + teste.usuarioExiste(tu));
//			
//			System.out.println(tu + " adiciona pedro " + teste.pedirAmizade(tu, "pedro"));
//			
//			System.out.println("amigos de pedro: " + teste.listarAmigos("pedro"));
//			System.out.println("amigos de pocahontas: " + teste.listarAmigos("pocahontas"));
//			
//			System.out.println("pedro aceita pocahontas " + teste.aceitarAmizade("pedro", "pocahontas"));
//			
//			System.out.println(tu + " adiciona pedro " + teste.pedirAmizade(tu, "pedro"));
//			
//			System.out.println("pedro aceita pocahontas " + teste.aceitarAmizade("pedro", "pocahontas"));
//			
//			System.out.println("amigos de pedro: " + teste.listarAmigos("pedro"));
//			System.out.println("amigos de pocahontas: " + teste.listarAmigos("pocahontas"));
//			
//			tu = "daniel";	
//			System.out.println(tu + " existe: " + teste.usuarioExiste(tu));
//			System.out.println(tu + " cadastrado: " + teste.cadastrarUsuario(tu, "saladinha", "senha"));
//			System.out.println(tu + " existe: " + teste.usuarioExiste(tu));
//			
//			System.out.println(tu + " adiciona pedro: " + teste.pedirAmizade(tu, "pedro"));
//			System.out.println(tu + " adiciona pocahontas: " + teste.pedirAmizade(tu, "pocahontas"));
//			
//			System.out.println("amigos de pedro: " + teste.listarAmigos("pedro"));
//			System.out.println("amigos de pocahontas: " + teste.listarAmigos("pocahontas"));
//			System.out.println("amigos de daniel: " + teste.listarAmigos("daniel"));
//			
//			System.out.println("daniel aceita pedro " + teste.aceitarAmizade("daniel", "pedro"));
//			System.out.println("daniel aceita pocahontas " + teste.aceitarAmizade("daniel", "pocahontas"));
//			
//			System.out.println("amigos de pedro: " + teste.listarAmigos("pedro"));
//			System.out.println("amigos de pocahontas: " + teste.listarAmigos("pocahontas"));
//			System.out.println("amigos de daniel: " + teste.listarAmigos("daniel"));
//
//			System.out.println("pedidos pendentes pra pedro: " + teste.pedidosPendentes("pedro"));
//			System.out.println("pedidos pendentes pra pocahontas: " + teste.pedidosPendentes("pocahontas"));
//			System.out.println("pedidos pendentes pra daniel: " + teste.pedidosPendentes("daniel"));
//			
//			System.out.println("pedro aceita daniel " + teste.aceitarAmizade("pedro", "daniel"));
//			System.out.println("pocahontas aceita daniel " + teste.aceitarAmizade("pocahontas", "daniel"));
//			
//			System.out.println("pedidos pendentes pra pedro: " + teste.pedidosPendentes("pedro"));
//			System.out.println("pedidos pendentes pra pocahontas: " + teste.pedidosPendentes("pocahontas"));
//			System.out.println("pedidos pendentes pra daniel: " + teste.pedidosPendentes("daniel"));
//			
//			System.out.println("amigos de pedro: " + teste.listarAmigos("pedro"));
//			System.out.println("amigos de pocahontas: " + teste.listarAmigos("pocahontas"));
//			System.out.println("amigos de daniel: " + teste.listarAmigos("daniel"));
//			
//			System.out.println("informação de usuário inexistente poseidon: " + teste.getInfo("poseidon"));
			
//			Scanner in = new Scanner(System.in);
//			in.nextLine();
//			in.close();
			teste.desconectar();
		}
	}*/
}
