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
import java.util.Scanner;

public class App {
	
	final static public boolean DEBUG = false;
	
	public static void main(String[] args) throws IOException {
		//---SETUP---
		Scanner userin=new Scanner(System.in);
		
		//---PREPARATION---
		//Collect name for final JSON file
		System.out.println("Please enter a name for the analysis file: ");
		String jsonname=userin.nextLine().trim();
		//Collect target files
		System.out.println("Please enter the src directory: ");
		String filepath=userin.nextLine();
		System.out.println("Gathering files...");
		File[] allfile = null; //Get all files in directory
		allfile = new File(filepath).listFiles();
		userin.close(); 
		
		int filecount = 0; //Get number of .java files
		for(int x=0;x<allfile.length;x++) {
			if(allfile[x].getName().contains(".java")) {
				filecount++;
			}
		}
		
		File[] file = new File[filecount]; //Create an array of just .java files
		String[] fnames=new String[filecount]; //Also create an array of file names
		int counter=0;
		for(int x=0;x<allfile.length;x++) {
			if(allfile[x].getName().contains(".java")) {
				file[counter]=allfile[x];
				fnames[counter]=allfile[x].getName().substring(0, allfile[x].getName().indexOf(".java"));
				if(DEBUG)
					System.out.println("Filename added:"+fnames[counter]);
				counter++;
			}
		}
		if(filecount==0) { //Check that there are actually files to analyze; if not, close
			System.out.println("No files found! Please restart the program and try again.");
			System.exit(0);
		}
		else
			System.out.println("Files recorded!");
		
		//Read files and record in jslist
		JSONArray jslist = new JSONArray();
		System.out.println("Analysing files...");
		for(int x=0;x<filecount;x++) {
			if(DEBUG)
				System.out.println(file[x].getName());
			FileInputStream currfile = new FileInputStream(file[x]);
			parseFile(jslist, currfile, jsonname, x, fnames);
		}
		System.out.println("All files analyzed!");
		
		//Write jslist to a file
		FileWriter jsout = new FileWriter(jsonname+".json");
		jsout.write(jslist.toString());
		jsout.flush();
		jsout.close();
		System.out.println("Analysis file created:");
		System.out.println("git\\HBCModeling\\hbcModel\\"+jsonname+".json");
	}
	
	public static void parseFile(JSONArray jslist, FileInputStream file, String jsonname, int filecount, String[] fnames) {
		String fname = fnames[filecount];
		CompilationUnit cu = JavaParser.parse(file);
		MethodVisitor mc = new MethodVisitor();
		int interscore=0;
		
		//linecount
		String[] fileText = (cu.toString().split("\n"));
		int linecount = fileText.length;
		
		//methods
		cu.accept(mc, null);
		ArrayList<String> mnames = mc.getMethods();
		
		//interactions
		ArrayList<Number> interact = new ArrayList<Number>(); //Arraylist to track console interactions
		ArrayList<String> reference = new ArrayList<String>(); //Arraylist to track references to other files
		for(int x=0;x<=linecount-1;x++) {
			int ref=referenceCheck(fnames, fileText[x], filecount);
			//COMMENT REMOVAL
			if(fileText[x].toLowerCase().contains("//")) { //Remove single comments
				fileText[x]=fileText[x].substring(0, fileText[x].indexOf("//"));
			}
			if(fileText[x].toLowerCase().contains("/*")) { //Remove multi-line comments
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
			
			//CHECK FOR INTERACTIONS
			if(ref>=0) { //If it mentions another class, record that and the line
				reference.add(fnames[ref]+" "+x);
			}
			if(fileText[x].toLowerCase().contains("system.out")) { //If it prints to the console, add score
				interscore++;
				interact.add(x);
			}
			//If it takes information from the console, add score
			if((fileText[x].toLowerCase().contains("system.in")&&!(fileText[x].toLowerCase().contains("scanner(system.in)"))||fileText[x].toLowerCase().contains(".next"))) {
				interscore+=10;
				interact.add(x);
			}
		}
		
		//output
		if(DEBUG) {
			System.out.println("File name: "+fname);
			System.out.println("File imports: "+cu.getImports().toString());
			System.out.println("File line count: "+linecount);
			System.out.println("File methods: "+mnames.toString());
			System.out.println("Class References: "+reference.toString());
			System.out.println("File interaction score: "+interscore);
		}
		 try {
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
	
	public static int referenceCheck(String[] classnames, String text, int fc) { //Checks for references to other classes and returns the matrix index if one is found
		for(int z=0;z<classnames.length;z++) {
			if((text.toLowerCase().contains(classnames[z].toLowerCase()))&&(z!=fc)) {
				return z;
			}
		}
		return -1;
	}
	
}

