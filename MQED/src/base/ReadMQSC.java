package base;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

public class ReadMQSC {
	public static void readMQSC() throws SQLException {
		String[][] queue_managers = InitializeQueueManagers.initializeQueueManagers();
		for(int i = 0; i < queue_managers.length; i++) {
			File myObj = new File("C:\\Shared\\Career\\Software Engineering\\Projects\\mqed\\" + queue_managers[i][1] + ".mqsc");
			String new_objects_string = "";
			String[][] current_objects = selectSQL();
		    try (Scanner myReader = new Scanner(myObj)) {
		      while (myReader.hasNextLine()) {
		        String data = myReader.nextLine();
		        String parsed_line = parseMQSCLine(data);
		        if(!parsed_line.equals("")) {
		        	new_objects_string += parsed_line;
		        }
		      }
		    } catch (FileNotFoundException e) {
		      System.out.println("An error occurred.");
		      e.printStackTrace();
		    }
		    String[] new_objects = new_objects_string.split("\n");
		    
		    compareNewCurrent(new_objects, current_objects, queue_managers, i);
		}
	}

	public static String parseMQSCLine(String line) {
		String [] splitted_lines = line.split(" ");
		for(int i = 0; i < splitted_lines.length; i++) {
			if(splitted_lines[i].contains("QLOCAL")) {
				String[] splitted_line_2 = splitted_lines[i].split("'");
				return splitted_line_2[1] + "\n";
			}
		}
		return "";
	}
	
	private static String[][] selectSQL() throws SQLException {
		ArrayList<String> queries = new ArrayList<String>();
		queries.add("SELECT id FROM object");
		queries.add("SELECT name FROM object");
		queries.add("SELECT id FROM object ORDER BY id DESC LIMIT 1");
		ArrayList<String> search_result = PostgresqlConnector.runSelectQueries(queries);
		if(search_result.size() > 1) {
			String[][] current_objects = new String[search_result.size()/2][2];
			for(int i = 0; i < search_result.size()/2; i++) {
				current_objects[i][0] = search_result.get(i);
				current_objects[i][1] = search_result.get(i+search_result.size()/2);
			}
			return current_objects;
		}
		
		String[][] current_objects = new String[0][0];
		return current_objects;
	}
	
	private static void compareNewCurrent(String[] new_objects, String[][] current_objects, String[][] queue_managers, int queue_manager_index) throws SQLException {
		int object_id = 1;
		if(current_objects.length > 0) {
			object_id = Integer.parseInt(current_objects[current_objects.length-1][0]);
			object_id++;
		}
		ArrayList<String> queries = new ArrayList<String>();
		
		for(int i = 0; i < new_objects.length; i++) {
			boolean exists = false;
			for(int j = 0; j < current_objects.length; j++) {
				if(new_objects[i].equals(current_objects[j][1])) {
					exists = true;
					break;
				}
			}
			if(!exists) {
				queries.add("INSERT INTO \"object\" VALUES (" + object_id + ", '" + new_objects[i] + "')");
				queries.add("INSERT INTO \"object_queue_manager\" VALUES (" + object_id + ", " + queue_managers[queue_manager_index][0] + ")");
				queries.add("INSERT INTO \"object_application\" VALUES (" + object_id + ", " + "1" + ")");
				queries.add("INSERT INTO \"object_object_type\" VALUES (" + object_id + ", " + "6" + ")");
				object_id++;
			}
		}
		
		for(int i = 0; i < current_objects.length; i++) {
			boolean exists = false;
			for(int j = 0; j < new_objects.length; j++) {
				if(current_objects[i][1].equals(new_objects[j])) {
					exists = true;
					break;
				}
			}
			if(!exists) {
				queries.add("DELETE FROM object_queue_manager WHERE object_id = " + current_objects[i][0]);
				queries.add("DELETE FROM object_application WHERE object_id = " + current_objects[i][0]);
				queries.add("DELETE FROM object_object_type WHERE object_id = " + current_objects[i][0]);
				queries.add("DELETE FROM object WHERE name =  '" + current_objects[i][1] + "'");
			}
		}
		
		PostgresqlConnector.runQueries(queries);
	}
}