import java.sql.SQLException;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import quick.dbtable.DBTable;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;
import javax.swing.SwingConstants;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

public class Login extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField user;
	private JPasswordField password;
	private static Login login;
	private Admin adm;
	private Inspector ins;
	public DBTable table;

	public static void main(String args[]) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				login = new Login();
				login.setSize(600, 400);
				login.setResizable(false);
				login.setLocationRelativeTo(null);
				login.setVisible(true);
			}
		});

	}

	public Login() {
		setTitle("App Parquimetros");
		getContentPane().setLayout(null);

		JLabel lblNewLabel = new JLabel("\u00A1BIENVENIDO A LA APP PARQUIMETROS!");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setFont(new Font("Copperplate Gothic Light", Font.BOLD, 21));
		lblNewLabel.setBounds(10, 31, 568, 63);
		getContentPane().add(lblNewLabel);

		user = new JTextField();
		user.setBounds(232, 118, 132, 20);
		getContentPane().add(user);
		user.setColumns(10);

		password = new JPasswordField();
		password.setBounds(232, 176, 132, 20);
		getContentPane().add(password);

		table = new DBTable();

		JLabel lblUser = new JLabel("Usuario:");
		lblUser.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 14));
		lblUser.setBounds(154, 120, 68, 14);
		getContentPane().add(lblUser);

		JLabel lblPass = new JLabel("Contrase\u00F1a:");
		lblPass.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 14));
		lblPass.setBounds(120, 178, 102, 14);
		getContentPane().add(lblPass);

		JButton btnLogin = new JButton("Iniciar sesi\u00F3n");
		btnLogin.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 11));
		btnLogin.setBounds(232, 224, 132, 23);
		btnLogin.setMnemonic(KeyEvent.VK_ENTER);
		getContentPane().add(btnLogin);

		btnLogin.addActionListener(new ActionListener() {
			@SuppressWarnings("deprecation")
			public void actionPerformed(ActionEvent arg0) {
				conectarBD(user.getText(), password.getText());
			}
		});

	}

	private void conectarBD(String user, String pw) {
		try {
			String driver = "com.mysql.cj.jdbc.Driver";
			String server = "localhost:3306";
			String bdd = "parquimetros";
			String url = "jdbc:mysql://" + server + "/" + bdd + "?serverTimezone=America/Argentina/Buenos_Aires";
			table.connectDatabase(driver, url, user, pw);
			if (user.equals("admin"))
				adminScreen();
			else if (user.equals("inspector"))
				inspecScreen();

		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			JOptionPane.showMessageDialog(getContentPane(), "SQLException: " + ex.getMessage(), "ERROR",
					JOptionPane.WARNING_MESSAGE);
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void adminScreen() {
		adm = new Admin(table);
		adm.setSize(1000, 600);
		adm.setVisible(true);
		adm.setLocationRelativeTo(null);
		this.dispose();
	}

	private void inspecScreen() {
		ins = new Inspector(table);
		ins.setSize(1000, 600);
		ins.setVisible(true);
		ins.setLocationRelativeTo(null);
		this.dispose();
	}

}
