import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.sql.SQLException;

import javax.swing.*;

public class Dextor implements ActionListener {
	private JFrame frame;
	private JTextArea chatArea;
	private JTextField inputField;
	private JButton sendButton;
	// private JList chatList;

	private Brain brain;
	private Mouth mouth;

	public Dextor() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		frame = new JFrame("Chatbox");
		frame.setSize(1024, 800);
		JPanel contentPanel = new JPanel();
		contentPanel
				.setLayout(new BoxLayout(contentPanel, BoxLayout.PAGE_AXIS));
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowEvent) {
				closingWindow();
			}
		});

		chatArea = new JTextArea();
		// chatArea.setSize(1020, 780);
		chatArea.setEditable(false);
		chatArea.setLineWrap(true);
		chatArea.setFont(new Font("Times New Roman", Font.PLAIN, 20));
		// chatArea.setBackground();

		/*
		 * chatList = new JList<String>(); chatList.setVisibleRowCount(10);
		 * chatList.setPreferredSize(new Dimension(1024, 700));
		 */
		JScrollPane scrollPane = new JScrollPane(chatArea);
		inputField = new JTextField(80);
		sendButton = new JButton("Send");
		JPanel inputPanel = new JPanel();
		inputPanel.setLayout(new FlowLayout());
		inputPanel.add(inputField);
		inputPanel.add(sendButton);
		inputPanel.setMaximumSize(new Dimension(frame.getWidth(), inputField
				.getHeight()));
		// frame.add(chatArea);
		contentPanel.add(scrollPane);
		contentPanel.add(inputPanel);
		frame.add(contentPanel);
		frame.setVisible(true);
	}

	public static void main(String[] args) throws IOException, SQLException,
			ClassNotFoundException {
		// TODO Auto-generated method stub
		Dextor dextor = new Dextor();
		dextor.initialize();

		// System.out.println("Learning Started");
		// dextor.learnFromFile();

	}

	public void initialize() throws ClassNotFoundException, SQLException {
		brain = new Brain(this);
		mouth = new Mouth();
		mouth.initialize();
		brain.setup();
		brain.greeting();

		inputField.addActionListener(this);
		sendButton.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		final String input = inputField.getText();
		inputField.setText("");
		// chatList.add(new JLabel("You: " + input));
		sendButton.setEnabled(false);
		inputField.setEnabled(false);
		chatArea.append("\nYou: " + input);
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				brain.userInput(input);
			}
		}).start();
	}

	public void displayOutput(String output) {
		chatArea.append("\nDextor: " + output);
		// chatList.add(new JLabel("Dextor: " + output));
		mouth.speak(output);
		sendButton.setEnabled(true);
		inputField.setEnabled(true);
		inputField.requestFocusInWindow();
	}

	public void closingWindow() {
		displayOutput("bye bye");
		brain.clearGarbage();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(0);
	}
}
