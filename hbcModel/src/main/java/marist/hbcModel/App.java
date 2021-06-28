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
		ArrayList<File> allfile = getFiles(filepath);
		
		// Get number of .java files in the directory.
		int filecount = 0; 
		for(int x=0;x<allfile.size();x++) {
			if(allfile.get(x).getName().contains(".java")) {
				filecount++;
			}
		}
		
		// Use the number of .java files to make a new array of the .java files and an array of their filenames.
		File[] file = new File[filecount]; // .java array
		String[] fnames=new String[filecount]; // file name array
		int counter=0;
		
		// Loop through the array of all files, recording each one that contains the extension ".java".
		for(int x=0;x<allfile.size();x++) {
			if(allfile.get(x).getName().contains(".java")) {
				file[counter]=allfile.get(x);
				fnames[counter]=allfile.get(x).getName().substring(0, allfile.get(x).getName().indexOf(".java"));
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
		
		System.exit(0);
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
	
	// This method is responsible for creating and populating the GUI window.
	public static void drawGUI() {
		// Set up the window itself
		JFrame frame = new JFrame("HBC Java Parser");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600,300);
        
        // This sets up entrypanel, the main area for all content, and makes button1, which is the "Go" button.
        JPanel entrypanel = new JPanel();
        JPanel nameHolder = new JPanel(new FlowLayout()); // Holds the name entry box
        JPanel pathHolder= new JPanel(new FlowLayout()); // Holds the name entry box
        JPanel listHolder = new JPanel(new FlowLayout()); // Holds the interaction selection list 
        JPanel newHolder = new JPanel(new FlowLayout()); // Holds the new interaction string entry boxes
        JLabel jslabel = new JLabel("Enter a Name:");
        JTextField jsname = new JTextField(20);
        JLabel fplabel = new JLabel("Enter path to the src directory:");
        JTextField fpath = new JTextField(20);
        JButton fselect = new JButton("Select");
        JButton button1 = new JButton("Parse");
        
        // Everything needs to be added to entrypanel in the order we want it to appear.
        entrypanel.setLayout(new BoxLayout(entrypanel, BoxLayout.Y_AXIS));
        nameHolder.add(jslabel);
        nameHolder.add(jsname);
        pathHolder.add(fplabel);
        pathHolder.add(fpath);
        //pathHolder.add(fselect); TODO: Make file selector?
        entrypanel.add(nameHolder);
        entrypanel.add(pathHolder);
        
        // To render the checklist of interaction strings, we need to create an array of CheckListItems 
        CheckListItem[] chk = new CheckListItem[userInteract.size()];
        for (int i = 0; i<userInteract.size(); i++) { 
		    chk[i] = new CheckListItem(("\"" + userInteract.get(i).getTerm()  + "\", weight " + userInteract.get(i).getWeight()));
		}
        
        // It's then converted to a JList...
        JList list = new JList(chk);
        list.setCellRenderer(new CheckListRenderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addMouseListener(new MouseAdapter() { // ...Given the proper On-Click Event code to select/deselect the item...
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
        JScrollPane listScroller = new JScrollPane(list); // Makes the interaction selection list scroll
        listScroller.setPreferredSize(new Dimension(250, 100));
        listHolder.add(new JLabel("Select strings to search for:"));
        listHolder.add(listScroller); // ...And added to its holder.
        entrypanel.add(listHolder);

        // Lastly, we need to add fields for the user to add a new interaction search string
        JLabel intlabel = new JLabel("Enter a new interaction String:");
        JTextField newint = new JTextField(20);     
        JSpinner newweight = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1)); // A Spinner is a number-only entry field. This one only accepts ints between 1 and 100.
        JButton addnewinter = new JButton("Add");
        
        // And add what we just created to the entrypanel.
        entrypanel.add(intlabel);
        newHolder.add(newint);
        newHolder.add(newweight);
        newHolder.add(addnewinter);
        entrypanel.add(newHolder);
        
            
     // This is the "Parse" button's on-click behavior
        button1.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent event) {
        		if(jsname.getText().equals("")) { // We need to have a name for the file.
        			JOptionPane.showMessageDialog(null, "Please enter a name for the final file");
        		}
        		else if(fpath.getText().equals("")) { // And we need to have a filepath for it.
        			JOptionPane.showMessageDialog(null, "Please enter a filepath");
        		}
        		else {
        			try {
        				// Set the array of interaction strings we're going to search for equal to what the user selected.
        				for(int i = chk.length - 1; i >= 0; i--) {
        					if(!chk[i].isSelected()) {
        						userInteract.remove(i);
        					}
        				}
        				
						analyzeFile(jsname.getText(), fpath.getText()); // Then, analyze the file.
					} catch (IOException e) {
						e.printStackTrace();
						System.exit(0);
					}
        		}
        	}
        });
        
        // This is the "Add" button's on-click behavior
        addnewinter.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent event) {
        		if(newint.getText().equals("")) { // The weight input always will have a value, but we need to make sure the text field does too.
        			JOptionPane.showMessageDialog(null, "Please enter a text string to search for");
        		}
        		else {
        			// We add a new InteractionObject with the given parameters into our ArrayList.
        			userInteract.add(new InteractionObject(newint.getText().toLowerCase(), (int) newweight.getValue()));
        			// Then, we reload the GUI by closing and reopening the window.
        			frame.dispose();
        			drawGUI();
        		}
        	}
        });
        
        // Lastly, we add what we have created into the window itself, making it appear for the user!
        frame.getContentPane().add(BorderLayout.CENTER, entrypanel);
        frame.getContentPane().add(BorderLayout.SOUTH, button1);
        frame.setVisible(true);
		
	}
	
	public static ArrayList<File> getFiles(String dir) {
		ArrayList<File> retFiles = new ArrayList<File>();
		File[] flist = new File(dir).listFiles();
		
		if(flist != null) {
	        for (File file : flist) {      
	            if (file.isFile()) {
	                retFiles.add(file);
	            } else if (file.isDirectory()) {
	                retFiles.addAll(getFiles(file.getAbsolutePath()));
	            }
	        }
	    }
		
		return retFiles;
	}
}

