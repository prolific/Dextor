import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Babble implements Runnable {

	private String seed = "";
	private Connection con = null;
	private ArrayList<String> responses = new ArrayList<String>();

	public Babble(String seed, Connection con) {
		this.seed = seed;
		this.con = con;
	}

	public ArrayList<String> getResponses() {
		return responses;
	}

	public void run() {
		try {
			PreparedStatement pstmt = con
					.prepareStatement("select current,next from chain where next=? or next=? or next=? or next=? or next=? or next=? or next=?");
			pstmt.setString(1, seed);
			pstmt.setString(2, seed + ".");
			pstmt.setString(3, seed + "?");
			pstmt.setString(4, seed + "!");
			pstmt.setString(5, seed + ",");
			pstmt.setString(6, seed + ")");
			pstmt.setString(7, "(" + seed);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				forwardBabble(
						rs.getString("current") + " " + rs.getString("next"), 5);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void forwardBabble(String output, int count) {
		if (count < 50) {
			int index = output.lastIndexOf(" ", output.length() - 1);
			index = output.lastIndexOf(" ", index - 1);
			index = output.lastIndexOf(" ", index - 1);
			index = output.lastIndexOf(" ", index - 1);
			String current = output.substring(index + 1);
			try {
				PreparedStatement pstmt = con
						.prepareStatement("select next from chain where current=?");
				pstmt.setString(1, current);
				ResultSet rs = pstmt.executeQuery();
				while (rs.next()) {
					String next = rs.getString("next");
					if (next.equals(" "))
						backwardBabble(output + " ", count);
					else
						forwardBabble(output + " " + next, count + 1);
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void backwardBabble(String output, int count) {
		if (count < 50) {
			int index = output.indexOf(" ", 0);
			index = output.indexOf(" ", index + 1);
			index = output.indexOf(" ", index + 1);
			index = output.indexOf(" ", index + 1);
			String current = output.substring(0, index);
			try {
				PreparedStatement backpstmt = con
						.prepareStatement("select previous from chain where current=?");
				backpstmt.setString(1, current);
				ResultSet rs = backpstmt.executeQuery();
				while (rs.next()) {
					String previous = rs.getString("previous");
					if (previous.equals(" "))
						responses.add(output.trim());
					else
						backwardBabble(previous + " " + output, count + 1);
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
