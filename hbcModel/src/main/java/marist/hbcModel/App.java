package marist.hbcModel;

import java.io.File;
import java.io.FileInputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import javax.swing.*;
import java.awt.*;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class App {
	
	final static public boolean DEBUG = false; // This affects the verbosity when using the console.
	public static ArrayList<InteractionObject> userInteract = new ArrayList<InteractionObject>(); // This keeps track of the phrases the program will search for, as well as their weight.
	
	public static void main(String[] args) throws IOException {
		//---SETUP---
		Scanner userin=new Scanner(System.in);
		userInteract.add(new InteractionObject("system.in", 1));
		userInteract.add(new InteractionObject("system.out", 10));
		drawGUI();
		
		//---PREPARATION---
		//Collect name for final JSON file
		System.out.println("Please enter a name for the analysis file: ");
		String jsonname=userin.nextLine().trim();
		//Collect target files
		System.out.println("Please enter the src directory: ");
		String filepath=userin.nextLine();
		System.out.println("Gathering files...");
		userin.close(); 
		
		analyzeFile(jsonname, filepath);
	}
	
	 // The primary method, taking in the name and filepath to find all .java files in the directory, handing each one off to be parsed, and saving the final .json
	public static void analyzeFile(String jsonname, String filepath) throws IOException {
		// Get all files in directory.
		File[] allfile = null; 
		allfile = new File(filepath).listFiles();
		
		// Get number of .java files in the directory.
		int filecount = 0; 
		for(int x=0;x<allfile.length;x++) {
			if(allfile[x].getName().contains(".java")) {
				filecount++;
			}
		}
		
		// Use the number of .java files to make a new array of the .java files and an array of their filenames.
		File[] file = new File[filecount]; // .java array
		String[] fnames=new String[filecount]; // file name array
		int counter=0;
		
		// Loop through the array of all files, recording each one that contains the extension ".java".
		for(int x=0;x<allfile.length;x++) {
			if(allfile[x].getName().contains(".java")) {
				file[counter]=allfile[x];
				fnames[counter]=allfile[x].getName().substring(0, allfile[x].getName().indexOf(".java"));
				if(DEBUG) { System.out.println("Filename added:"+fnames[counter]); }
				counter++;
			}
		}
		
		// Check that there are actually .java files to analyze; if not, close.
		if(filecount==0) { 
			System.out.println("No files found! Please restart the program and try again.");
			System.exit(0);
		}
		else { System.out.println("Files recorded!"); }
		
		// Pass the files into the parseFile method and record the results of the parsing in jslist.
		JSONArray jslist = new JSONArray();
		System.out.println("Analysing files...");
		for(int x=0;x<filecount;x++) {
			if(DEBUG) { System.out.println(file[x].getName()); }
			FileInputStream currfile = new FileInputStream(file[x]);
			parseFile(jslist, currfile, jsonname, x, fnames);
		}
		System.out.println("All files analyzed!");
		
		// Write jslist to a file.
		FileWriter jsout = new FileWriter(jsonname+".json");
		jsout.write(jslist.toString());
		jsout.flush();
		jsout.close();
		System.out.println("Analysis file created:");
		System.out.println("git\\HBCModeling\\hbcModel\\"+jsonname+".json");
	}
	
	// This method takes in information from the main analyzeFile method to iterate through a file, produce a score, save interactions, and save all the data to the JSON array.
	public static void parseFile(JSONArray jslist, FileInputStream file, String jsonname, int filecount, String[] fnames) {
		// Set up a few variables and objects to parse through the file with.
		String fname = fnames[filecount];
		CompilationUnit cu = JavaParser.parse(file);
		MethodVisitor mc = new MethodVisitor();
		int interscore=0;
		
		// Make an int to easily reference the linecount later with.
		String[] fileText = (cu.toString().split("\n"));
		int linecount = fileText.length;
		
		// This package luckily has an easy way to get the method names, so stick those in an array.
		cu.accept(mc, null);
		ArrayList<String> mnames = mc.getMethods();
		
		// This loop goes through the file line by line to find inter-file interactions and interactions with the user.
		ArrayList<Number> interact = new ArrayList<Number>(); // Arraylist to track console interactions
		ArrayList<String> reference = new ArrayList<String>(); // Arraylist to track references to other files
		for(int x=0;x<=linecount-1;x++) {
			int ref=referenceCheck(fnames, fileText[x], filecount); // This method quickly finds if the line contains a reference to another file. TODO: Is it possible to have more than 1 reference in a single line?
			
			//---COMMENT REMOVAL---
			if(fileText[x].toLowerCase().contains("//")) { // Remove single comments (If there's a //, everything after it doesn't matter to us.
				fileText[x]=fileText[x].substring(0, fileText[x].indexOf("//"));
			}
			
			if(fileText[x].toLowerCase().contains("/*")) { // Remove multi-line comments (If there's a /*, we need to ignore everything until we reach a */, even if that means "fast-forwarding" through lines.
				if(fileText[x].toLowerCase().contains("*/")) {
					fileText[x]=fileText[x].substring(0, fileText[x].indexOf("/*"))+fileText[x].substring(fileText[x].indexOf("*\\")+1, fileText[x].length());
				}
				else {
					fileText[x]=fileText[x].substring(0, fileText[x].indexOf("/*"));
					for(int y=x+1;y<=linecount-1;y++) {
						if(fileText[y].toLowerCase().contains("*/")) {
							fileText[y]=fileText[y].substring(fileText[y].indexOf("*/")+1, fileText[y].length());
							y=linecount;
						}
						else {
							fileText[y]="";
						}
					}
				}
			}
			
			//---CHECK FOR INTERACTIONS---
			if(ref>=0) { // If it mentions another class/file, record that and the line
				reference.add(fnames[ref]+" "+x);
			}
			
			for (int i = 0; i < userInteract.size(); i++) { // If it contains any of the user-defined "interaction" terms, add the associated weight to this file's interaction score.
				if(fileText[x].toLowerCase().contains(userInteract.get(i).getTerm())){
					interscore+=userInteract.get(i).getWeight();
					interact.add(x);
				}
			}
			
//			if(fileText[x].toLowerCase().contains("system.out")) { //If it prints to the console, add score
//				interscore++;
//				interact.add(x);
//			}
//			//If it takes information from the console, add score
//			if((fileText[x].toLowerCase().contains("system.in")&&!(fileText[x].toLowerCase().contains("scanner(system.in)"))||fileText[x].toLowerCase().contains(".next"))) {
//				interscore+=10;
//				interact.add(x);
//			}
		}
		
		//---OUTPUT---
		if(DEBUG) { // If we're in debug mode, print information to the console for the user to see.
			System.out.println("File name: "+fname);
			System.out.println("File imports: "+cu.getImports().toString());
			System.out.println("File line count: "+linecount);
			System.out.println("File methods: "+mnames.toString());
			System.out.println("Class References: "+reference.toString());
			System.out.println("File interaction score: "+interscore);
		}
		
		 try { // Print the collected information into a JSONObject, then put it in jslist.
			JSONObject js = new JSONObject();
			js.put("filenum", filecount);
			js.put("filename", fname);
			js.put("linecount", linecount);
			js.put("imports", cu.getImports().toString());
			js.put("method names", mnames.toString());
			js.put("references", reference.toString());
			js.put("interaction score",interscore);
			js.put("interaction locations", interact.toString());
			jslist.put(js);
			if(DEBUG) {
				System.out.println(js.toString());
				FileWriter jsout = new FileWriter(jsonname+filecount+".json");
				jsout.write(js.toString());
				jsout.flush();
				jsout.close();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// Checks for references to other classes and returns the matrix index if one is found.
	public static int referenceCheck(String[] classnames, String text, int fc) { 
		for(int z=0;z<classnames.length;z++) {
			if((text.toLowerCase().contains(classnames[z].toLowerCase()))&&(z!=fc)) {
				return z;
			}
		}
		return -1;
	}
	
	
	public static void drawGUI() {
		JFrame frame = new JFrame("HBC Java Parser");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600,600);
        JPanel entrypanel = new JPanel();
        JLabel jslabel = new JLabel("Enter a Name:");
        JTextField jsname = new JTextField();
        JLabel fplabel = new JLabel("Enter path to the src directory:");
        JTextField fpath = new JTextField();
        JButton button1 = new JButton("Parse");
        entrypanel.setLayout(new BoxLayout(entrypanel, BoxLayout.Y_AXIS));
        entrypanel.add(jslabel);
        entrypanel.add(jsname);
        entrypanel.add(fplabel);
        entrypanel.add(fpath);
        
        CheckListItem[] chk = new CheckListItem[userInteract.size()];
        for (int i = 0; i<userInteract.size(); i++) { 
		    chk[i] = new CheckListItem(("\"" + userInteract.get(i).getTerm()  + "\", weight " + userInteract.get(i).getWeight()));
		}
        
        JList list = new JList(chk);
        list.setCellRenderer(new CheckListRenderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent event) {
            JList list = (JList) event.getSource();
            int index = list.locationToIndex(event.getPoint());// Get index of item
                                                              // clicked
            CheckListItem item = (CheckListItem) list.getModel()
                .getElementAt(index);
            item.setSelected(!item.isSelected()); // Toggle selected state
            list.repaint(list.getCellBounds(index, index));// Repaint cell
          }
        });
        entrypanel.add(list);    

        JLabel intlabel = new JLabel("Enter a new string to search for as an interaction:");
        JTextField newint = new JTextField();
        JTextField newweight = new JTextField();
        JButton addnewint = new JButton("Add");
        entrypanel.add(intlabel);
        entrypanel.add(newint);
        entrypanel.add(addnewint);
            
            
        button1.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent event) {
        		if(jsname.getText().equals("")) {
        			JOptionPane.showMessageDialog(null, "Please enter a name for the final file");
        		}
        		else if(fpath.getText().equals("")) {
        			JOptionPane.showMessageDialog(null, "Please enter a filepath");
        		}
        		else {
        			//TODO: Set the array of searched strings to only the selected strings
        			try {
						analyzeFile(jsname.getText(), fpath.getText());
					} catch (IOException e) {
						e.printStackTrace();
						System.exit(0);
					}
        		}
        	}
        });
        
        addnewint.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent event) {
        		if(newint.getText().equals("")) {
        			JOptionPane.showMessageDialog(null, "Please enter a text string to search for");
        		}
        		else {
        			userInteract.add(new InteractionObject(newint.getText().toLowerCase()));
        			frame.dispose();
        			drawGUI();
        		}
        	}
        });
        
        frame.getContentPane().add(BorderLayout.CENTER, entrypanel);
        // TODO: Add a weight selection
        frame.getContentPane().add(BorderLayout.SOUTH, button1);
        frame.setVisible(true); // TODO: Redo the disgusting GUI, make everything sized properly.
		
	}
	

	
}

class CheckListItem {

	  private String label;
	  private boolean isSelected = true;

	  public CheckListItem(String label) {
	    this.label = label;
	  }

	  public boolean isSelected() {
	    return isSelected;
	  }

	  public void setSelected(boolean isSelected) {
	    this.isSelected = isSelected;
	  }

	  @Override
	  public String toString() {
	    return label;
	  }
	}

class CheckListRenderer extends JCheckBox implements ListCellRenderer {
	  public Component getListCellRendererComponent(JList list, Object value,
	      int index, boolean isSelected, boolean hasFocus) {
	    setEnabled(list.isEnabled());
	    setSelected(((CheckListItem) value).isSelected());
	    setFont(list.getFont());
	    setBackground(list.getBackground());
	    setForeground(list.getForeground());
	    setText(value.toString());
	    return this;
	  }
}

class InteractionObject {
	private String search;
	private int weight = 1;
	
	public InteractionObject(String search) {
		this.search = search;
	}
	
	public InteractionObject(String search, int weight) {
		this.search = search;
		this.weight = weight;
	}
	
	public String getTerm() {
		return this.search;
	}
	
	public int getWeight() {
		return this.weight;
	}
}
