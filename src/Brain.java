import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.StringTokenizer;

public class Brain {

	private Connection con = null;
	private Statement stmt = null;
	private PreparedStatement pstmt = null;
	private ArrayList<String> previousKeywords = new ArrayList<String>();
	private Dextor dextor;

	public Brain(Dextor dextor) {
		this.dextor = dextor;
	}

	public void setup() throws SQLException, ClassNotFoundException {
		Class.forName("com.mysql.jdbc.Driver");
		con = DriverManager.getConnection("jdbc:mysql://localhost/dextorBrain",
				"root", "");
		stmt = con.createStatement();
	}

	public void greeting() {
		String output = eventReply("greeting", "events");

		displayOutput("Conversation started", output);
	}

	public void userInput(String input) {
		String temp = input;
		input = input.trim();
		input = input.toUpperCase();

		if (input.endsWith("?") || input.endsWith("."))
			input = input.substring(0, input.length() - 1);

		preProcessInput(input);

		// learnMarkov(temp);
	}

	public void preProcessInput(String input) {
		int c = 0;

		try {
			pstmt = con
					.prepareStatement("select output from temp_log where input=?");
			pstmt.setString(1, input);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				c++;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (c > 1) {
			String output = eventReply("repetition", "events");
			displayOutput(input, output);
		} else if (input.contains("YOUR NAME")
				|| input.contains("TELL ME ABOUT YOURSELF")) {
			String output = eventReply("name", "personality");
			displayOutput(input, output);
		} else if (input.contains("ROHIT AGARWAL")) {
			String output = eventReply("rohit", "personality");
			displayOutput(input, output);
		} else if (input.contains("PANKAJ ARORA")) {
			String output = eventReply("pankaj", "personality");
			displayOutput(input, output);
		} else if (input.contains("SUMIT RAWAT")) {
			String output = eventReply("sumit", "personality");
			displayOutput(input, output);
		} else if (input.contains("YOUR CREATOR")) {
			String output = eventReply("creator", "personality");
			displayOutput(input, output);
		} else if (input.contains("YOUR AGE")
				|| input.contains("HOW OLD ARE YOU")) {
			String output = eventReply("age", "personality");
			displayOutput(input, output);
		} else if (input.contains("HOW DO YOU LIVE")) {
			String output = eventReply("live", "personality");
			displayOutput(input, output);
		} else if (input.contains("HOW ARE YOU")) {
			String output = "I am fine.";
			displayOutput(input, output);
		} else if (input.equalsIgnoreCase("bye"))
			dextor.closingWindow();
		else
			processInput(input);
	}

	public void processInput(String input) {
		ArrayList<String> words = breakWords(input);
		words = swapWords(words);
		ArrayList<String> keywords = getKeywords(words);
		ArrayList<String> responses = generateResponses(keywords);
		String output = "";
		if (responses.size() == 0)
			output = eventReply("unknown", "events");
		else
			output = findBestOutput(responses, keywords, words, input);
		displayOutput(input, output);
		previousKeywords = keywords;
	}

	public ArrayList<String> breakWords(String input) {
		ArrayList<String> words = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(input);
		while (st.hasMoreTokens())
			words.add(st.nextToken());
		return words;
	}

	public ArrayList<String> swapWords(ArrayList<String> words) {
		int size = words.size();
		try {
			pstmt = con
					.prepareStatement("select to_key from swap where from_key=?");
			for (int i = 0; i < size; i++) {
				pstmt.setString(1, words.get(i));
				ResultSet rs = pstmt.executeQuery();
				if (rs.next()) {
					String swapString = rs.getString("to_key");
					if (swapString.equals(" ")) {
						words.remove(i);
						size = size - 1;
						i = i - 1;
					} else
						words.set(i, swapString);
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return words;
	}

	public ArrayList<String> getKeywords(ArrayList<String> words) {
		ArrayList<String> keywords = new ArrayList<String>();
		try {
			pstmt = con
					.prepareStatement("select * from aux_keywords where keyword=?");
			int size = words.size();
			for (int i = 0; i < size; i++) {
				pstmt.setString(1, words.get(i));
				ResultSet rs = pstmt.executeQuery();
				if (!rs.next())
					keywords.add(words.get(i));

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return keywords;
	}

	public ArrayList<String> generateResponses(ArrayList<String> keywords) {
		ArrayList<String> responses = new ArrayList<String>();
		int size = keywords.size();
		if (size == 0)
			keywords = previousKeywords;
		size = keywords.size();
		if (size != 0) {
			Thread t[] = new Thread[size];
			Babble babble[] = new Babble[size];
			for (int i = 0; i < size; i++) {
				String seed = keywords.get(i);
				if (seed.endsWith("?") || seed.endsWith(".")
						|| seed.endsWith(",") || seed.endsWith("!")
						|| seed.endsWith(")"))
					seed = seed.substring(0, seed.length() - 1);
				babble[i] = new Babble(seed, con);
				t[i] = new Thread(babble[i]);
				t[i].start();
			}
			for (int i = 0; i < size; i++) {
				try {
					t[i].join();
					responses.addAll(babble[i].getResponses());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return responses;
	}

	public String findBestOutput(ArrayList<String> responses,
			ArrayList<String> keywords, ArrayList<String> words, String input) {
		String output = responses.get(0);
		int bestScore = 0;
		int size = responses.size();
		int keySize = keywords.size();
		int wordSize = words.size();
		for (int i = 0; i < size; i++) {
			int score = 0;
			try {
				pstmt = con
						.prepareStatement("select output from temp_log where output=?");
				pstmt.setString(1, responses.get(i));
				ResultSet rs = pstmt.executeQuery();
				if (rs.next())
					score = score - 4;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String response = " " + responses.get(i) + " ";
			if (!responses.get(i).equalsIgnoreCase(input)) {
				for (int j = 0; j < wordSize; j++) {
					if (response.contains(" " + words.get(j) + " "))
						score = score + 2;
				}
				for (int j = 0; j < keySize; j++) {
					if (response.contains(" " + keywords.get(j) + " "))
						score = score + 4;
				}
				if (previousKeywords != null) {
					int prevKeySize = previousKeywords.size();
					for (int j = 0; j < prevKeySize; j++) {
						if (response.contains(" " + previousKeywords.get(j)
								+ " "))
							score = score + 4;
					}
				}
			}
			if (score > bestScore) {
				bestScore = score;
				output = responses.get(i);
			}
		}
		return output;
	}

	public String eventReply(String event, String table) {
		String sql = "select response from " + table + " where event='" + event
				+ "'";
		String response = "";
		try {
			ResultSet rs = stmt.executeQuery(sql);
			ArrayList<String> responses = new ArrayList<String>();
			while (rs.next())
				responses.add(rs.getString("response"));
			int num = responses.size();
			Random random = new Random();
			int index = random.nextInt(num);
			response = responses.get(index);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}

	public void learnMarkov(String input) {
		String w[] = new String[6]; // order of Markov chain is 4
		w[0] = w[1] = w[2] = w[3] = w[4] = w[5] = "";
		try {
			pstmt = con
					.prepareStatement("insert ignore into chain values(?,?,?)");

			StringTokenizer st = new StringTokenizer(input);
			while (st.hasMoreTokens()) {
				w[0] = w[1];
				w[1] = w[2];
				w[2] = w[3];
				w[3] = w[4];
				w[4] = w[5];
				w[5] = st.nextToken();
				if (w[0].equals(""))
					w[0] = " ";
				pstmt.setString(1, w[0]);
				pstmt.setString(2, w[1] + " " + w[2] + " " + w[3] + " " + w[4]);
				pstmt.setString(3, w[5]);
				pstmt.executeUpdate();
			}
			pstmt.setString(1, w[1]);
			pstmt.setString(2, w[2] + " " + w[3] + " " + w[4] + " " + w[5]);
			pstmt.setString(3, " ");
			pstmt.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void displayOutput(String input, String output) {
		tempLog(input, output);
		dextor.displayOutput(output);
	}

	public void tempLog(String input, String output) {
		try {
			pstmt = con.prepareStatement("insert into temp_log values(?,?)");
			pstmt.setString(1, input);
			pstmt.setString(2, output);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void clearGarbage() {
		String sql = "delete from temp_log where 1";
		try {
			stmt.executeUpdate(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void learnFromFile() throws IOException {
		BufferedReader a = new BufferedReader(new InputStreamReader(
				new FileInputStream("res/Brain.txt")));
		String input = null;
		while ((input = a.readLine()) != null) {
			input = input.trim();
			input = input.toUpperCase();

			if (input.endsWith("?") || input.endsWith("."))
				input = input.substring(0, input.length() - 1);
			learnMarkov(input);
		}
		System.out.println("Learning Completed! I am pro now.");
	}

}
