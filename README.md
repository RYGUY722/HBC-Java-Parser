# HBC-Java-Parser
A simple Java parser program that attempts to score classes based on their interactions with the user and keep track of intraprogram interactions.

The program is made up of two individual parts, the parser and the modeler. The parser examines files and outputs a JSON file of the information it collects. That JSON can be fed into the modeler to produce a 3D graph

# Using the program
## The Parser
The main method of the parser component is stored in `hbcModel\App.java`. This file can be run either through the console or through its GUI, though the GUI is recommended.
The program requires 2 input strings: a name and a src directory filepath. The name will be used for the final JSON it produces. The filepath is where the code to be analysed is located. All subdirectories will be explored for `.java` files, so choose whatever directory contains all the files you want analysed.
If using the GUI, there is also the option to select and/or add interaction strings. The parser attempts to score each file based on how much it interacts with the user. Each string on the list is weighted. Whenever the parser comes across a selected string, it will add that string's weight to the file's interaction score. This means that a program using special methods or outside libraries to create content for or receive input from the user can be properly scored. By default, the list contains "System.out" and "System.in". This search is not case sensitive.
## The Modeler
The modeler must be started through the console, but has a GUI beyond the initial step. The modeler component will not run on Mac computers due to the libraries used. The main method of this component is stored in `thesisModeler\Main.java`. On startup, the program will ask for a filename. This is the JSON from the parser, with the file extension excluded. For the easiest startup, it's recommended to place the JSON file in the target directory alongside the `.class` files. After this file is given to the modeler, it will attempt to generate a graph based on it.
Assuming the given file is valid, 2 windows will open: an interactive scatter plot and a list of all the points on that graph. The graph can be rotated and scaled at will by the user and contains points relating to each file that was analysed. The point list contains a button for each point and a list of the other files it calls. Mousing over a button will highlight the corresponding point on the graph. Clicking a button will open another window with a more detailed visual representation of the file. Here, the file is represented as a rectangle based on its length. Each line coming off of the rectangle represents a call to another file within the program. These lines are color-coded with randomly generated colors, and a legend can be found below the drawing. Also found below the drawing is a list of the components the file imports and the methods within the file. 

# Known Issues
* Rarely, files will throw an error during parsing. This appears to be an issue with JavaParser, rather than with code exclusive to this project. This was only encountered once during testing, but should be listed.
* The modeler component's windows for specific files contain everything within a JScrollPane, however for an unknown reason this pane refuses to act properly. Resizing the window will remove elements from the bottom rather than create a scroll bar. This results in files above a certain size dependant on the user's vertical screen size to simply run off the end.

# Acknowledgements
This project was created by Ryan Sheffler as a thesis project for the Marist Honors program under the guidance of [Professor Michael Gildein](https://github.com/megildei)
This project uses 3 outside libraries:
* [JSON In Java](https://mvnrepository.com/artifact/org.json/json)
* [JavaParser](http://javaparser.org)
* [Jzy3D](http://www.jzy3d.org) 
