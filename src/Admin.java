import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import quick.dbtable.DBTable;

import javax.swing.JTextArea;
import javax.swing.ListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;

public class Admin extends JFrame {
	private DBTable table;
	private JTextArea textArea;
	private JList listTablas, listAtributos;
	private JButton btnEjecutar, btnBorrar;
	private JLabel lblTablas;

	public Admin(DBTable t) {
		table = t;
		listarTablas();
		getContentPane().setLayout(null);

		textArea = new JTextArea();
		textArea.setBounds(100, 0, 500, 70);
		getContentPane().add(textArea);

		table.setEditable(false);
		table.setBounds(0, 100, 600, 460);
		getContentPane().add(table);

		btnEjecutar = new JButton("Ejecutar");
		btnEjecutar.setBounds(622, 11, 89, 23);
		getContentPane().add(btnEjecutar);
		btnEjecutar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				refrescarTabla();
			}
		});

		btnBorrar = new JButton("Borrar");
		btnBorrar.setBounds(622, 45, 89, 23);
		getContentPane().add(btnBorrar);
		btnBorrar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				textArea.setText("");
				listarAtributos();
			}
		});

		lblTablas = new JLabel("Lista de tablas");
		lblTablas.setBounds(670, 79, 89, 14);
		getContentPane().add(lblTablas);

		JLabel lblNewLabel = new JLabel("Lista de atributos");
		lblNewLabel.setBounds(834, 79, 103, 14);
		getContentPane().add(lblNewLabel);

	}

	private void listarTablas() {
		DefaultListModel<String> listModel = new DefaultListModel();
		try {
			table.setSelectSql("show tables");
			table.createColumnModelFromQuery();
			table.refresh();
			Vector v = table.getDataVector();
			for (int i = 0; i < v.size(); i++) {
				listModel.addElement(v.get(i).toString());
			}
			listTablas = new JList(listModel);
			listTablas.setBounds(622, 100, 162, 272);
			getContentPane().add(listTablas);
		} catch (SQLException e) {
			e.printStackTrace();

		}
	}

	private void listarAtributos() {
		ListModel<String> listModel;
		DefaultListModel<String> nueva = new DefaultListModel();
		try {
			int i = listTablas.getSelectedIndex();
			listModel = listTablas.getModel();
			String selected = listModel.getElementAt(i).toString();
			selected = selected.replaceAll("[^a-z,_]", "");
			table.setSelectSql("describe" + selected);
			table.createColumnModelFromQuery();
			table.refresh();
			Vector v = table.getDataVector();
			for (int j = 0; j < v.size(); j++) {
				nueva.addElement(v.get(j).toString());
			}

			listAtributos = new JList(nueva);
			System.out.println("Supuestamente creé la lista");
			listAtributos.setBounds(804, 100, 162, 272);
			getContentPane().add(listAtributos);

		} catch (SQLException e) {

		}
	}

	private void desconectarBD() {
		try {
			table.close();
		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}

	}

	private void refrescarTabla() {
		try {
			table.setSelectSql(this.textArea.getText().trim());

			table.createColumnModelFromQuery();
			for (int i = 0; i < table.getColumnCount(); i++) {

				if (table.getColumn(i).getType() == Types.TIME) {
					table.getColumn(i).setType(Types.CHAR);
				}
				if (table.getColumn(i).getType() == Types.DATE) {
					table.getColumn(i).setDateFormat("dd/MM/YYYY");
				}
			}
			table.refresh();

		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
			JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), ex.getMessage() + "\n",
					"Error al ejecutar la consulta.", JOptionPane.ERROR_MESSAGE);

		}

	}
}
