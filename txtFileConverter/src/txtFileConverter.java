import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import java.awt.event.*;


@SuppressWarnings("serial")
public class txtFileConverter extends JFrame implements ActionListener{
	/**
	 * @param args
	 */
	
	String[] fileItems;
	List<String> colHeaders = new ArrayList<String>();
	List<String> rowHeaders = new ArrayList<String>();
	List<String> rowReasoningMethods = new ArrayList<String>();
	List<String> rowIterations = new ArrayList<String>();

	String tileSeperator = ",";
	String fileLocation = "Results-June-11-15.txt";
	String fileDirectory; 
		
	JFrame frame;
	JFormattedTextField fileCreator;
	JFileChooser fileChooser;
	JButton start, create;
	JLabel instructions;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		txtFileConverter file1 = new txtFileConverter();
		file1.guiCreator();
	}
	/*
	 * This function creates the GUI for the user to interact with
	 */
	public void guiCreator() throws java.awt.HeadlessException{
		frame = new JFrame("Txt to csv converter");
		frame.setLayout(null);
		frame.setSize(450,300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		fileCreator = new JFormattedTextField("Enter a txt Folder Location.csv");
		fileCreator.setFont(new Font("Arial", Font.PLAIN, 10));
		fileCreator.setBounds(25, 100, 250, 50);
		fileCreator.setBackground(Color.white);
		fileCreator.setVisible(false);
		frame.add(fileCreator);

		start = new JButton("Start");
		start.setFont(new Font("Arial", Font.PLAIN, 10));
		start.setBounds(25, 175, 250, 50);
		start.setBackground(Color.white);
		start.addActionListener(this);
		start.setVisible(true);
		frame.add(start);
		
		create = new JButton("Create csv file");
		create.setFont(new Font("Arial", Font.PLAIN, 10));
		create.setBounds(25, 175, 250, 50);
		create.setBackground(Color.green);
		create.addActionListener(this);
		create.setVisible(false);
		frame.add(create);
		
		instructions = new JLabel("Select a txt file : ");
		instructions.setFont(new Font("Arial", Font.PLAIN, 11));
		instructions.setBounds(25, 25, 425, 25);
		frame.add(instructions);
		
		frame.setVisible(true);
	}
	
	/*
	 * This function read the entered .txt file and gathers useful information which will be used later, such as the total number of simulations, number of weight functions, etc
	 */
	public void txtFileReader (String filePlace) throws IOException{
		BufferedReader txtFile = new BufferedReader(new FileReader(fileLocation));
		String line = "not set";
		
		int numLines = -1;
		while(line != null){
			line = txtFile.readLine();
			if(line != null){
				
				boolean isInListAlready = false;

				if((line.split(" : ")[0]).equals("Weight Function") == true){
					for(int i = 0; i < colHeaders.size() && isInListAlready == false; i++){
						if((colHeaders.get(i)).equals(line.split(" : ")[1]) == true){
							isInListAlready = true;
						}
					}
					if(isInListAlready == false){
						colHeaders.add(line.split(" : ")[1]);
					}
				}
				
				if((line.split(" : ")[0]).equals("Reasoning Method")){
					isInListAlready = false;
					for(int i = 0; i < rowReasoningMethods.size() && isInListAlready == false; i++){
						if((rowReasoningMethods.get(i)).equals(line.split(" : ")[1]) == true){
							isInListAlready = true;
						}
					}
					if(isInListAlready == false){
						rowReasoningMethods.add(line.split(" : ")[1]);
					}
				}
				
				if((line.split(" : ")[0]).equals("Random, k") || (line.split(" : ")[0]).equals("NonRandom, k")){
					isInListAlready = false;
					for(int i = 0; i < rowIterations.size() && isInListAlready == false; i++){
						if((rowIterations.get(i)).equals(line.split(" : ")[1]) == true){
							isInListAlready = true;
						}
					}
					if(isInListAlready == false){
						rowIterations.add(line.split(" : ")[1]);
					}
				}
				isInListAlready = false;
				for(int i = 0; i < rowHeaders.size() && isInListAlready == false; i++){
					if((rowHeaders.get(i)).equals(line.split(" : ")[0]) == true || line.split(" : ")[0].equals("Weight Function") == true || 
							line.split(" : ")[0].equals("Random, k") == true || line.split(" : ")[0].equals("NonRandom, k") == true ||
							line.split(" : ")[0].equals("Reasoning Method") == true){
						isInListAlready = true;
					}
				}
				
				if(isInListAlready != true && line.isEmpty() == false){
					rowHeaders.add(line.split(" : ")[0]);
				}
			}
			numLines++;
		}
		
		rowHeaders.remove(0); //for some reason the header "Reasoning Method " remains within this array, thus it must be removed manually
		
		System.out.println("Simulations : ");
		for(int i = 0; i < rowHeaders.size(); i++){
			System.out.println(rowHeaders.get(i));
		}
		System.out.println();
		System.out.println("Weight Functions : ");
		for(int i = 0; i < colHeaders.size(); i++){
			System.out.println(colHeaders.get(i));
		}
		System.out.println();
		System.out.println("Reasoning Methods : ");
		for(int i = 0; i < rowReasoningMethods.size(); i++){
			System.out.println(rowReasoningMethods.get(i));
		}
		System.out.println();
		System.out.println("Random-types, k values, and Iterations : ");
		for(int i = 0; i < rowIterations.size(); i++){
			System.out.println(rowIterations.get(i));
		}
		System.out.println();
		
		System.out.println("total number of lines in txt file : " + numLines);
		System.out.println();
		txtFile.close();
	}
	
	/*
	 * This function creates and returns an array (within another array) which will store all the data from the .txt file which will be transfered to the created .csv document
	 */
	public String[][] arrayCreator() throws IOException {
		int totalNumRows = (rowHeaders.size() + 4)*rowReasoningMethods.size()*rowIterations.size() + rowReasoningMethods.size() + 1;
		System.out.println("Number of rows in the csv doc : " +  totalNumRows);
		String[][] fileText = new String[totalNumRows][colHeaders.size()+1];
		System.out.println("Number of columns in the csv doc : " + fileText[0].length);
		fileText[0][0] = "Weight Function";
		for(int h = 1; h <= colHeaders.size(); h++){
			fileText[0][h] = colHeaders.get(h-1);
		}
		int currentRow = 1;
		for(String r:rowReasoningMethods){
			fileText[currentRow][0] = "Reasoning Method";
			fileText[currentRow][1] = r;	
			currentRow++;
			for(String k:rowIterations){
				fileText[currentRow][0] = "Random Type:";
				currentRow++;
				fileText[currentRow][0] = "k";
				currentRow++;
				fileText[currentRow][0] = "Iteration";

				currentRow++;
				for(String s:rowHeaders){
					fileText[currentRow][0] = s;
					currentRow++;
				}
				currentRow++;
			}
		}
	
		for(int j = 0; j < fileText.length; j ++){
			for(int u = 0; u < fileText[0].length; u ++){
				System.out.print(fileText[j][u] + tileSeperator);
			}
			System.out.println();
		}
		System.out.println("\n");
		int currentCol = 1;
		int currentReasoning = 1;
		String currentRand = null;
		String currentReasoningName = null;
		currentRow = 0;
		
		BufferedReader txtFile = new BufferedReader(new FileReader(fileLocation));		
		String line = txtFile.readLine();
		while(line != null){
			System.out.println("Beginning Row: " + currentRow);
			System.out.println(line);
			if(line.split(" : ")[0].equals("Weight Function") == true){
				currentCol = colHeaders.indexOf(line.split(" : ")[1]) + 1;
				if(colHeaders.indexOf(line.split(" : ")[1]) !=0){
					currentRow = currentRow - rowHeaders.size() - 3;
				}
					String[] randomType = currentRand.split(", k : ");
					fileText[currentRow-1][currentCol] = randomType[0];
					currentRow++;
					fileText[currentRow-1][currentCol] = randomType[1].split(" Iter ")[0];
					currentRow++;
					fileText[currentRow-1][currentCol] = randomType[1].split(" Iter ")[1];	
			}
			else if(line.split(" : ")[0].equals("Reasoning Method") == true){
				if(line.split(" : ")[1].equals("SEQ")){
					currentReasoningName = "SEQ";
				}
				else{
					currentReasoningName = null;
				}
				currentReasoning = rowReasoningMethods.indexOf(line.split(" : ")[1]);
				System.out.println("Current Reasoning : " + currentReasoning);
				if(rowReasoningMethods.indexOf(line.split(" : ")[1]) == 0){
					currentRow = 2;
				}
				else{
//					currentRow = (currentReasoning) * (rowHeaders.size() + 4) * rowIterations.size() + 3;
					currentRow = currentReasoning * (rowHeaders.size() + 4) * rowIterations.size() + currentReasoning + 2;
					System.out.println(currentReasoning + " * (" + rowHeaders.size() + " + 4) * " + rowIterations.size() + " + " + currentReasoning);
				}
				
				currentRand = null;
			}
			else if(line.split(" : ")[0].equals("Random, k") || line.split(" : ")[0].equals("NonRandom, k")){
				currentRow--;
				currentRand = line;

				if(currentReasoningName == "SEQ"){
					currentRow++;
					String[] randomType = currentRand.split(", k : ");
					fileText[currentRow-1][currentCol] = randomType[0];
					currentRow++;
					fileText[currentRow-1][currentCol] = randomType[1].split(" Iter ")[0];
					currentRow++;
					fileText[currentRow-1][currentCol] = randomType[1].split(" Iter ")[1];
				}						
			}		
			else if(line.isEmpty() == false){
				System.out.println("Text being entered on line: " + currentRow);
				fileText[currentRow-1][currentCol] = line.split(" : ")[1];
			}
			currentRow++;
			line = txtFile.readLine();
			System.out.println("Ending Row: " + currentRow + "\n");
		}			
		for(int j = 0; j < fileText.length; j ++){
			for(int u = 0; u < fileText[0].length; u ++){
				System.out.print(fileText[j][u] + tileSeperator);
			}
			System.out.println();
		}	
		return fileText;
	}
	
	/*
	 * This function creates the .csv file and transfers all the data from the array created by the "arrayCreator" function
	 */
	public void csvFileCreator(String location, String[][] fileText) {
		FileWriter newFile = null;
		try{	
			newFile = new FileWriter(location);
			for(int i = 0; i < fileText.length; i++){
				String text = "";
				for(int j = 0; j < fileText[0].length; j++){
					if(fileText[i][j] != null){
						text = text + fileText[i][j] + tileSeperator;
					}				
				}
				text = text + "\n";
				newFile.append(text);
			}
		}
		catch(Exception e){
			System.out.println("Error while adding elements to csv file");
		}
		try {
			newFile.flush();
			newFile.close();
		}
		catch (IOException e) {
			System.out.println("Error while flushing or closing csv file");
			e.printStackTrace();
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		if(arg0.getSource() == start){
			try{
				fileChooser = new JFileChooser();
				fileChooser.showOpenDialog(this);
				fileLocation = fileChooser.getSelectedFile().toString() + "/";

				BufferedReader txtFile = new BufferedReader(new FileReader(fileLocation));	//only present to test whether the user-inputed file can be read or not
				start.setVisible(false);
				create.setVisible(true);
				fileCreator.setVisible(true);
				instructions.setText("Enter a file name and then select a save directory for the csv file: ");
				
				
			}
			catch(Exception e){
				instructions.setText("Error finding txt file");
				System.out.println("Error finding txt file");
			}					
		}
		else if(arg0.getSource() == create){
			try{
				txtFileReader("");
			}		
			catch(Exception e){
				instructions.setText("Error reading txt file");
				System.out.println("Error reading txt file");
			}
			try{
				fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fileChooser.showOpenDialog(this);
				fileDirectory = fileChooser.getSelectedFile().toString() + "/";

				
				csvFileCreator(fileDirectory + fileCreator.getText(), arrayCreator());
			}
			catch(Exception IOException){
				instructions.setText("Error in file format/contents");
				System.out.println("Error in file format/contents");
			}
			System.exit(0);
		}
	}
}