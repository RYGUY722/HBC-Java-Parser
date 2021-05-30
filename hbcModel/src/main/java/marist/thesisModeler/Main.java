package marist.thesisModeler;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Random;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.jzy3d.chart.Chart;
import org.jzy3d.chart.factories.AWTChartFactory;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.Scatter;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.jzy3d.colors.Color;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Main {

	final static public boolean DEBUG = true; // This affects the verbosity when using the console.
	public static void main(String[] args) {
		Scanner userin=new Scanner(System.in);
		String fname = "";
		
		/* COLLECT INPUT */
		System.out.println(System.getProperty("user.dir"));
		System.out.println("Please input the target JSON (no file extension): ");
		fname = userin.nextLine();
		fname = fname + ".json";
		
		InputStream is = Main.class.getResourceAsStream(fname);
        if (is == null) {
            throw new NullPointerException("Cannot find resource file " + fname);
        }
        else if(DEBUG) {
        	System.out.println("File " + fname + " found.");
        }

        JSONTokener tokener = new JSONTokener(is);
		JSONArray jarray = new JSONArray(tokener);
        JSONObject[] fbreakdown = new JSONObject[jarray.length()];
        
        for(int i = 0; i < jarray.length(); i++) {
        	fbreakdown[i] = jarray.getJSONObject(i);
        	if(DEBUG) {
        		System.out.println("Logged object " + fbreakdown[i].getString("filename") + ".");
        	}
        }
        
		/* PROCESS INPUT */
		
		/* CREATE GRAPH */

        Coord3d[] points = new Coord3d[fbreakdown.length];
        
        Color color = Color.BLACK;

        for (int i = 0; i < fbreakdown.length; i++) {
          points[i] = new Coord3d(fbreakdown[i].getInt("filenum"), fbreakdown[i].getInt("linecount"), fbreakdown[i].getInt("interaction score"));
          if(DEBUG) {
        	  System.out.println("Point added at " + points[i].toString() + ".");
          }
        }

        Scatter scatter = new Scatter(points, color);

        Quality q = Quality.Advanced();
        // q.setPreserveViewportSize(true);

        Chart chart = new AWTChartFactory().newChart(q);
        chart.getScene().add(scatter);
        
		/* SHOW GRAPH */
        
        chart.render();
        chart.open(fname);
	}

}
