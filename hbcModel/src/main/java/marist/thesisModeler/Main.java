package marist.thesisModeler;

import java.io.InputStream;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.jzy3d.chart.factories.AWTChartFactory;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.selectable.SelectableScatter;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.jzy3d.colors.Color;
import org.jzy3d.analysis.AWTAbstractAnalysis;
import org.jzy3d.analysis.AnalysisLauncher;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Main extends AWTAbstractAnalysis{

	final static public boolean DEBUG = true; // This affects the verbosity when using the console.
	static JSONObject[] fbreakdown;
	static SelectableScatter scatter;
	
	public static void main(String[] args) throws Exception {
		System.out.println("Loading...");
		
		JFrame frame = new JFrame("HBC Java Parser");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600,300);
		
        AnalysisLauncher.open(new Main());
        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        for(JSONObject obj: fbreakdown) {
        	content.add(generateButton(obj));
        }
        content.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JScrollPane sizeLocker = new JScrollPane(content); // Locks the window size, making it scroll.
        sizeLocker.setPreferredSize(new Dimension(600, 300));
        
        frame.add(sizeLocker);
        frame.setVisible(true);
	}
	
	@Override
	public void init() {
		Scanner userin=new Scanner(System.in);
		String fname = "";
		
		/* COLLECT INPUT */
		if(DEBUG) {
			System.out.println("Search directory: " + System.getProperty("user.dir"));
		}
		System.out.println("Please input the target JSON (no file extension): ");
		fname = userin.nextLine();
		fname = fname + ".json";
		userin.close();
		if(DEBUG) {
			System.out.println("Searching for file " + fname + ".");
		}
		
		InputStream is = Main.class.getResourceAsStream(fname);
        if (is == null) {
            throw new NullPointerException("Cannot find resource file " + fname);
        }
        else {
        	System.out.println("File " + fname + " found.");
        }

        JSONTokener tokener = new JSONTokener(is);
		JSONArray jarray = new JSONArray(tokener);
        fbreakdown = new JSONObject[jarray.length()];
        
        for(int i = 0; i < jarray.length(); i++) {
        	fbreakdown[i] = jarray.getJSONObject(i);
        	if(DEBUG) {
        		System.out.println("Logged object " + fbreakdown[i].getString("filename") + ".");
        	}
        }
        
		/* PROCESS INPUT */

        Coord3d[] points = new Coord3d[fbreakdown.length];
        Color[] colors = new Color[fbreakdown.length];

        for (int i = 0; i < fbreakdown.length; i++) {
          points[i] = new Coord3d(fbreakdown[i].getInt("filenum"), fbreakdown[i].getInt("linecount"), fbreakdown[i].getInt("interaction score"));
          colors[i] = Color.BLUE;
          if(DEBUG) {
        	  System.out.println("Point added at " + points[i].toString() + ".");
          }
        }
		
		/* CREATE GRAPH */

        scatter = new SelectableScatter(points, colors);
        scatter.setWidth(10);
        scatter.setColor(Color.GREEN);
        scatter.setHighlightColor(Color.MAGENTA);

        Quality q = Quality.Advanced();

        chart = new AWTChartFactory().newChart(q);
        chart.getScene().add(scatter);
        
        chart.getAxisLayout().setXAxisLabel("File ID");
        chart.getAxisLayout().setYAxisLabel("Linecount");
        chart.getAxisLayout().setZAxisLabel("Interaction Score");
	}
	
	public static JPanel generateButton(JSONObject obj) {
		if(DEBUG) {
			System.out.println("Creating detail line for " + obj.getString("filename"));
		}
		
		/* CREATE VARIABLES */
		JPanel newLine = new JPanel(new FlowLayout());
		JButton objButton = new JButton(obj.getString("filename"));
		newLine.add(objButton);
		String linkString = "Calls: ";
		
		/* CONFIGURE LINK LIST */
		String refStr = obj.getString("references");
		String[] references = refStr.substring(1, refStr.length()-1).split(", ");
		for(String s: references) {
			String callName = s.split(" ")[0];
			if(!linkString.contains(callName)) {
				linkString += (callName + ", ");
			}
		}
		
		if(!(linkString.length() == 7)) {
			linkString = linkString.substring(0, linkString.length()-2);
		}
		JLabel links = new JLabel(linkString);
		newLine.add(links);
		
		/* CREATE BUTTON BEHAVIOR */
		objButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent event) {
				scatter.setHighlighted(obj.getInt("filenum"), true);
			}

			@Override
			public void mouseExited(MouseEvent event) {
				scatter.setHighlighted(obj.getInt("filenum"), false);
			}
			
        	@Override
        	public void mouseClicked(MouseEvent event) {
        		new DetailWindow(obj).setVisible(true);;
        	}
        });
		
		/* RETURN JPANEL */
		return newLine;
	}

}
