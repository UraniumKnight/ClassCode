package geometry;

// Makes simple polygons.
// The "count" tab lets you draw points and move them,
// and it counts the number of simple polygons on the sets

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.*;

public class MakeSimple {
    static int POINTWID = 2; // size of points

    // the x and y arrays hold the coordinates
    // the B array is the order of the points in the polygon
    // You want to fill the C array with the simple polygon
    static double x[] = new double[200];
    static double y[] = new double[200];
    
    //static Line2D[] edges = new Line2D[200];
    //static PolygonDouble[] convex = new PolygonDouble[200];

    static int B[] = new int[200]; // the permutation matrix
    static int C[] = new int[200]; // the one that becomes simple
    static int D[] = new int[200]; // the one that becomes very simple
    static Integer E[] = new Integer[200]; // holds the convex hull
    
    // Variables for the minimal convexulation
    static int numConvexulationChords;
    static ArrayList<int[]> convexulationChords;

    static SimpleFrame myFrame;

    static int numPoints = 17;
    static int numHullPoints = 0;

    public static void main(String args[]) {
        makePolygons();

        // Create the frame to draw on
        myFrame = new SimpleFrame();
        myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        myFrame.setSize(800, 800);
        myFrame.setLocation(400, 100);
        myFrame.setVisible(true);
    }

    public static void makePolygons() {
        // Build an array of random points in the unit square
        for (int i = 0; i < numPoints; i++) {
            x[i] = Math.random();
            y[i] = Math.random();// Sample program
            B[i] = i; // default permutation
        }

        createSimplePolygon();
        createVerySimplePolygon();
        createConvexHull();
        createMinimalConvexulation();
    }

    /*
     * Creates a simply polygon by iteratively removing crossings.
     * Total polygon perimeter is a monovariant that decreases with
     * each step of the algorithm, proving that the process will
     * terminate in a a crossing-free polygon.
     */
    public static void createSimplePolygon() {
        // Initialize the C[] array with the identity permutation
        for (int i = 0; i < numPoints; i++)
            C[i] = i;

        // Find crossings, and eliminate them
        boolean hasCrossing = true;
        while (hasCrossing) {
            hasCrossing = false;
            for (int i = 0; (i < numPoints - 2); i++)
                for (int j = i + 2; j < numPoints; j++)
                    if (crosses(x[C[i]], y[C[i]], x[C[i + 1]], y[C[i + 1]],
                            x[C[j]], y[C[j]], x[C[(j + 1) % numPoints]],
                            y[C[(j + 1) % numPoints]])) {
                        hasCrossing = true;
                        int k = i + 1;
                        int l = j;
                        while (l > k) {
                            int temp = C[k];
                            C[k] = C[l];
                            C[l] = temp;
                            l--;
                            k++;
                        }
                    }
        }
    }

    
    /**
     * This algorithm makes use of the createSimplePolygon ideas
     * above, and adds one check. If a point P of the polygon 
     * lies close to another edge AB of the polygon, then sometimes
     * it is worth it to remove P from where it is in the polygon and
     * insert it between A and B in the polygon. 
     * Thus polygon ...XPY...AB... ==>  ...XY...APB...
     * 
     * After making this switch, new crossings might be introduced,
     * so we run again our createSimple algorithm to find and
     * eliminate crossings.
     */
    public static void createVerySimplePolygon() {
        // Initialize the D[] array with the identity permutation
        for (int i = 0; i < numPoints; i++)
            D[i] = i;

        // Find crossings, and eliminate them
        boolean stillTrying = true;
        while (stillTrying) {
            stillTrying = false;
            boolean hasCrossing = true;
            while (hasCrossing) {
                hasCrossing = false;
                for (int i = 0; (i < numPoints - 2); i++)
                    for (int j = i + 2; j < numPoints; j++)
                        if (crosses(x[D[i]], y[D[i]], x[D[i + 1]], y[D[i + 1]],
                                x[D[j]], y[D[j]], x[D[(j + 1) % numPoints]],
                                y[D[(j + 1) % numPoints]])) {
                            hasCrossing = true;
                            int k = i + 1;
                            int l = j;
                            while (l > k) {
                                int temp = D[k];
                                D[k] = D[l];
                                D[l] = temp;
                                l--;
                                k++;
                            }
                        }
            }
            // See if we can make it shorter
            for (int i = 0; i < numPoints; i++)
                for (int j = (i + 1) % numPoints; ((j + 2) % numPoints) != i; j = ((j + 1) % numPoints))
                    if (dis2(D[(i + numPoints - 1) % numPoints], D[i], D[(i + 1) % numPoints]) +
                            dis(D[j], D[(j + 1) % numPoints]) >
                    dis(D[(i + numPoints - 1) % numPoints], D[(i + 1) % numPoints])
                    + dis2(D[j], D[i], D[(j + 1) % numPoints])) {
                        stillTrying = true;
                        if (i < j) {
                            int temp = D[i];
                            for (int k = i; k < j; k++)
                                D[k] = D[(k + 1) % numPoints];
                            D[j] = temp;
                        } else {
                            int temp = D[i];
                            for (int k = i; k > j + 1; k--)
                                D[k] = D[(k + numPoints - 1) % numPoints];
                            D[j + 1] = temp;
                        }
                    }
        }
        makeCounterclockwise(D, numPoints);
    }
    
    /**
     * Your old homework went here...
     */
    public static void createConvexHull(){
    	numHullPoints = numPoints;
    	E = new Integer[numHullPoints];
    	for(int i = 0; i < numHullPoints; i++)
    		E[i] = new Integer(B[i]);
    	
    	int lowest = 0; // index of lowest
    	for(int i = 0; i < numHullPoints; i++)
    		if(y[E[i]] < y[E[lowest]])
    			lowest = i;
    	// Swap 0th element and flowest to put lowest in position 0
    	int tmp = E[lowest]; E[lowest] = E[0]; E[0] = tmp; 
    	// Using this version of sort, so we don't move sort the 0th element
    	// sort(T[] a, int fromIndex, int toIndex, Comparator<? super T> c)
    	Arrays.sort(E, 1, numHullPoints, new Comparator<Integer>(){
    		public int compare(Integer a, Integer b){
    			return sigma(x[E[0]], y[E[0]],
    					x[a], y[a], x[b], y[b]);
    		}
    	});
    }
    
    // Your homework goes here
    /**
     * This finds a decomposition of the interior of the very simple convexulation
     * as ordered in the array D.
     */
    public static void createMinimalConvexulation(){
    	makeCounterclockwise(D, numPoints);  // This is required for chordInside() to work properly
    	ArrayList<int[]> tempchords = getChords(D, x, y, numPoints);
    	convexulationChords = convexulate(D, tempchords, numPoints);
    	numConvexulationChords = convexulationChords.size();
    }
    
    static ArrayList<int[]> convexulate(int[] d2, ArrayList<int[]> chords, int np){
    	if(isConvex(d2, np)){
    		return null;
    	}
    	
    	ArrayList<int[]> rval = chords;
    	ArrayList<Integer> dl = new ArrayList<Integer>(), dr = new ArrayList<Integer>();
    	for(int i = 0; i < chords.size() ;i++){
    		int[] ti = chords.get(i);
    		
    		HashSet<int[]> combx = new HashSet<int[]>();
    		combx.add(ti);
    		
    		for(int j = ti[0]; j!=ti[1]; j++){
    			dl.add(d2[j%np]);
    		}
    		dl.add(d2[ti[1]]);
    		for(int j = ti[1]; j!=ti[0]; j++){
    			dr.add(d2[j%np]);
    		}
    		dr.add(d2[ti[0]]);
    		
    		int[] dleft = new int[dl.size()], dright = new int[dl.size()];
    		
    		for(int j = 0; j < dl.size();j++){
    			dleft[j] = dl.get(j);
    			dright[j] = dr.get(j);
    		}
    		ArrayList<int[]> cleft = getChords(dleft, x, y, dleft.length);
    		ArrayList<int[]> cright = getChords(dright, x, y, dright.length);
    		
    		ArrayList<int[]> templeft = convexulate(dleft, cleft, dleft.length );
    		ArrayList<int[]> tempright =convexulate(dright, cright, dright.length);
    		
    		combx.addAll(templeft);
    		combx.addAll(tempright);
    		
    		if(combx.size()<rval.size()){
    			rval = new ArrayList<int[]>();
    			rval.addAll(combx);
    		}
    	}
		return rval;	
    }

    static ArrayList<int[]> getChords(int[] d2, double[]tx, double[] ty, int nP){
    	ArrayList<int[]> rval = new ArrayList<int[]>();
    	numConvexulationChords = 0;
    	// Find all interior chords of the polygon, very naively.
    	for(int i = 0; i < nP - 2; i++){
    		for(int j = i+2; j < nP - (i==0 ? 1 : 0); j++){
    			if(chordInside(d2, i, j, nP) && !chordCrossesBoundary(d2, i, j, nP)){
    				int[] chord = {i, j};
    				rval.add(chord);
        			numConvexulationChords++;
    			}
    		}
    	}
    	for(int i = rval.size()-1;i>=0;i--){
        	int topbefore = (rval.get(i)[0] + nP - 1) % nP;
        	int topafter  = (rval.get(i)[0] + 1) % nP;
        	int bbefore = (rval.get(i)[1] + nP - 1) % nP;
        	int bafter = (rval.get(i)[1] + 1) % nP;
    		double tmx = tx[d2[rval.get(i)[0]]];
    		double tmy = ty[d2[rval.get(i)[0]]];
    		double bmx = tx[d2[rval.get(i)[1]]];
			double bmy = ty[d2[rval.get(i)[1]]];
			if(sigma(tx[d2[topbefore]], ty[d2[topbefore]], tmx, tmy, tx[d2[topafter]], ty[d2[topafter]])!=-1){
				if(sigma(tx[d2[bbefore]], ty[d2[bbefore]], bmx, bmy, tx[d2[bafter]], ty[d2[bafter]])!=-1){
					rval.remove(i);
					//numConvexulationChords--;
				}
			}
    	}
		return rval;	
    }
    
    
    static boolean isConvex(int[] d2, int np){
    	for(int i= 1; i <= d2.length;i++){
    		if(sigma(x[d2[(i-1)%np]], y[d2[(i-1)%np]], x[d2[i%np]], y[d2[i%np]], x[d2[(i+1)%np]], y[d2[(i+1)%np]]) == 1){
    			return false;
    		}
    	}
    	return true;
    }
        
    /**
     * Decides whether the chord from point s to e enters the polygon P as it leaves vertex s
     * @param P  The array giving the order of the simple polygon's vertices
     * @param s  the start index of the chord, in the array P
     * @param e  the end index of the chord, in the array P
     * @param n  the number of sides of the polygon
     * @return  true if the chord enters P as it leaves s. 
     * 
     * Note that the polygon is assumed to be given in counter-clockwise order.
     */
    public static boolean chordInside(int[] P, int s, int e, int n){
    	int before = (s + n - 1) % n;
    	int after  = (s + 1) % n;
    	if(sigma(x[P[before]], y[P[before]], x[P[s]], y[P[s]], x[P[after]], y[P[after]]) > 0){
    		if(sigma(x[P[before]], y[P[before]], x[P[s]], y[P[s]], x[P[e]], y[P[e]]) > 0 &&
    				sigma(x[P[e]], y[P[e]], x[P[s]], y[P[s]], x[P[after]], y[P[after]]) > 0)
    			return true;
    		else 
    			return false;
    	} else {
    		if(sigma(x[P[before]], y[P[before]], x[P[s]], y[P[s]], x[P[e]], y[P[e]]) > 0 ||
    				sigma(x[P[e]], y[P[e]], x[P[s]], y[P[s]], x[P[after]], y[P[after]]) > 0)
    			return true;
    		else 
    			return false;
    	}
    }
    
    
    /**
     * Decides whether the chord from point s to e crosses any edges of P
     * @param P  The array giving the order of the simple polygon's vertices
     * @param s  the start index of the chord, in the array P
     * @param e  the end index of the chord, in the array P
     * @param n  the number of points on the polygon
     * @return  true if the chord enters P as it leaves s.
     */
    public static boolean chordCrossesBoundary(int[] P, int s, int e, int n){
    	for(int i = 0; i < n; i++){
    		if(crosses(x[P[i]], y[P[i]], x[P[(i+1)%n]], y[P[(i+1)%n]], x[P[s]], y[P[s]], x[P[e]], y[P[e]]))
    			return true;
    	}
    	return false;
    }
    
    
    
    /**
     * Finds whether traveling A->B->C makes a right- or left-turn
     * @param Ax coordinates of the point A
     * @param Ay
     * @param Bx coordinates of the point B
     * @param By
     * @param Cx coordinates of the point C
     * @param Cy
     * @return  sigma(  (Ax, Ay),  (Bx, By),  (Cx, Cy)  ), which is in the set {-1, 0, 1} for {right, collinear, left} resp.
     */
    public static int sigma(double Ax, double Ay, double Bx, double By, double Cx, double Cy){
    	double det = ((Bx-Ax)*(Cy-Ay) - (By-Ay)*(Cx-Ax));
    	if(det < 0)
    		return -1;
    	else if(det > 0)
    		return 1;
    	else 
    		return 0; 
    }

    
    /**
     * Returns the signed area (half the sum of the 2x2 determinants) of the polygon described by P 
     * @param P Array of indices into the x[] and y[] arrays, giving a simple polygon
     * @param n Number of points in the polygon, in case its not all of the points
     * @return  the area of the polygon. Positive if the points are traversed counter-clockwise, 
     * otherwise negative. 
     */
    public static double signedArea(int[] P, int n){
    	double rv = 0;
    	for(int i = 0; i < n; i++)
    		rv += x[P[i]]*y[P[(i+1)%n]] - x[P[(i+1)%n]]*y[P[i]];
    	return rv/2;
    }

    /**
     * Returns the sum of the lengths of two segments sharing a common point
     * @param i the index into the x[] and y[] arrays of the first point
     * @param j the index into the x[] and y[] arrays of the second point
     * @param k the index into the x[] and y[] arrays of the third point
     * @return distance(i, j) + distance(j, k)
     */
    public static double dis2(int i, int j, int k) {
        return Math.sqrt((x[i] - x[j]) * (x[i] - x[j]) + (y[i] - y[j])
                * (y[i] - y[j]))
                + Math.sqrt((x[k] - x[j]) * (x[k] - x[j]) + (y[k] - y[j])
                        * (y[k] - y[j]));
    }

    /**
     * Returns the distance between two points
     * @param i the index into the x[] and y[] arrays of the first point
     * @param j the index into the x[] and y[] arrays of the second point
     * @return the distance between the two points
     */
    public static double dis(int i, int j) {
        return Math.sqrt((x[i] - x[j]) * (x[i] - x[j]) + (y[i] - y[j])
                * (y[i] - y[j]));
    }

    /*
     * Naive crosses method. Assumes points are in general position, which
     * is good enough for this project.
     * Returns true if (a,b)-(c,d) crosses (e,f)-(g,h)
     */
    public static boolean crosses(double a, double b, double c, double d,
            double e, double f, double g, double h) {
        double det1a = (c - a) * (f - b) - (e - a) * (d - b);
        double det1b = (c - a) * (h - b) - (g - a) * (d - b);
        double det2a = (e - g) * (b - h) - (a - g) * (f - h);
        double det2b = (e - g) * (d - h) - (c - g) * (f - h);
        return ((det1a * det1b < 0) && (det2a * det2b < 0));
    }

    
    /**
     * If the polygon P is traversed counter-clockwise, it leaves it alone, and otherwise reverses it.
     * @param P  The indices of the polygon, assumed to be simple
     */
    public static void makeCounterclockwise(int[] P, int n){
    	if(signedArea(P, n) < 0){ // reverse the indices
    		for(int i = 0; i < n/2; i++){
    			int tmp = P[i];
    			P[i] = P[n-1-i];
    			P[n-1-i] = tmp;
    		}
    	}
    }

    
    /**
     * The frame that displays this application's GUI.
     *
     */
    public static class SimpleFrame extends JFrame {
        public static JSlider numPointsSlider;

        public SimpleFrame() {
            super("Create you own Simple Polygon");
            Container content = getContentPane();
            content.setLayout(new java.awt.BorderLayout());
            JTabbedPane tabbedPane = new JTabbedPane();
            tabbedPane.setPreferredSize(new java.awt.Dimension(300, 400));
            tabbedPane.addTab("Scrambled", new ScrambledPanel());
            tabbedPane.addTab("Simple", new SimplePanel());
            tabbedPane.addTab("Very Simple", new VerySimplePanel());
            tabbedPane.addTab("Convex Hull", new ConvexPanel());
            tabbedPane.addTab("Convexulated Very Simple", new ConvexulationPanel());
            content.add(tabbedPane, java.awt.BorderLayout.CENTER);

            // Slider for the number of points
            numPointsSlider = new JSlider(
                    javax.swing.SwingConstants.HORIZONTAL, 3, 180, numPoints);
            numPointsSlider
            .addChangeListener(new javax.swing.event.ChangeListener() {
                public void stateChanged(
                        javax.swing.event.ChangeEvent evt) {
                    numPointsSliderStateChanged(evt);
                }
            });
            content.add(numPointsSlider, java.awt.BorderLayout.SOUTH);
        }

        private void numPointsSliderStateChanged(
                javax.swing.event.ChangeEvent evt) {
            numPoints = numPointsSlider.getValue();
            makePolygons();
            repaint();
        }
    }

    
    /**
     * Displays the point set and a randomly-ordered (non-simple) polygon on it.
     *
     */
    public static class ScrambledPanel extends JPanel {
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;

            // First set the scaling to fit the window
            Dimension size = getSize();
            int Xwid = (int) (0.95 * size.width);
            int Ywid = (int) (0.95 * size.height);

            // First draw the segments
            g2.setColor(Color.red);
            for (int i = 0; i < numPoints; i++)
                g2.drawLine((int) (Xwid * x[B[i]]), size.height - (int) (Ywid * y[B[i]]),
                        (int) (Xwid * x[B[(i + 1) % numPoints]]),
                        size.height - (int) (Ywid * y[B[(i + 1) % numPoints]]));

            // Now draw the points
            for (int i = 0; i < numPoints; i++) {
                g2.fillRect((int) (Xwid * x[i]) - POINTWID, size.height - (int) (Ywid * y[i])
                        - POINTWID, 2 * POINTWID + 1, 2 * POINTWID + 1);
            }
        }
    }

    
    /**
     * Displays a simple polygon on the point set.
     *
     */
    public static class SimplePanel extends JPanel {
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;

            // First set the scaling to fit the window
            Dimension size = getSize();
            int Xwid = (int) (0.95 * size.width);
            int Ywid = (int) (0.95 * size.height);

            // First draw the segments
            g2.setColor(Color.red);
            for (int i = 0; i < numPoints; i++) {
                if (i == 0)
                    g2.setColor(Color.black);
                if (i == 1)
                    g2.setColor(Color.green);
                if (i > 1)
                    g2.setColor(Color.red);
                g2.drawLine((int) (Xwid * x[C[i]]), size.height - (int) (Ywid * y[C[i]]),
                        (int) (Xwid * x[C[(i + 1) % numPoints]]),
                        size.height - (int) (Ywid * y[C[(i + 1) % numPoints]]));
            }
            // Now draw the points
            for (int i = 0; i < numPoints; i++) {
                g2.fillRect((int) (Xwid * x[i]) - POINTWID, size.height - (int) (Ywid * y[i])
                        - POINTWID, 2 * POINTWID + 1, 2 * POINTWID + 1);
            }
        }
    }

    
    /**
     * Displays the simple polygon on the point set, having somewhat shorter length.
     *
     */
    public static class VerySimplePanel extends JPanel {
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;

            // First set the scaling to fit the window
            Dimension size = getSize();
            int Xwid = (int) (0.95 * size.width);
            int Ywid = (int) (0.95 * size.height);

            // First draw the segments
            g2.setColor(Color.red);
            for (int i = 0; i < numPoints; i++) {
                if (i == 0)
                    g2.setColor(Color.black);
                if (i == 1)
                    g2.setColor(Color.green);
                if (i > 1)
                    g2.setColor(Color.red);
                g2.drawLine((int) (Xwid * x[D[i]]), size.height - (int) (Ywid * y[D[i]]),
                        (int) (Xwid * x[D[(i + 1) % numPoints]]),
                        size.height - (int) (Ywid * y[D[(i + 1) % numPoints]]));
            }
            // Now draw the points
            for (int i = 0; i < numPoints; i++) {
                g2.fillRect((int) (Xwid * x[i]) - POINTWID, size.height - (int) (Ywid * y[i])
                        - POINTWID, 2 * POINTWID + 1, 2 * POINTWID + 1);
            }
        }
    }

    /**
     * Displays the convex hull of the point set.
     *
     */
    public static class ConvexPanel extends JPanel{
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;

            // First set the scaling to fit the window
            Dimension size = getSize();
            int Xwid = (int) (0.95 * size.width);
            int Ywid = (int) (0.95 * size.height);

            // First draw the segments
            g2.setColor(Color.red);
            for (int i = 0; i < numHullPoints; i++) {
                g2.drawLine((int) (Xwid * x[E[i]]), size.height - (int) (Ywid * y[E[i]]),
                        (int) (Xwid * x[E[(i + 1) % numHullPoints]]),
                        size.height - (int) (Ywid * y[E[(i + 1) % numHullPoints]]));
            }
            // Now draw the points
            for (int i = 0; i < numPoints; i++) {
                g2.fillRect((int) (Xwid * x[i]) - POINTWID, size.height - (int) (Ywid * y[i])
                        - POINTWID, 2 * POINTWID + 1, 2 * POINTWID + 1);
            }
        }
    }

    /**
     * Displays the very simple polygon decomposed into convex polygons
     *
     */
    public static class ConvexulationPanel extends JPanel{
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;

            // First set the scaling to fit the window
            Dimension size = getSize();
            int Xwid = (int) (0.95 * size.width);
            int Ywid = (int) (0.95 * size.height);
            
            // First draw the segments from the D[] array, 
            // corresponding to the very simple polygon.
            g2.setColor(Color.red);
            for (int i = 0; i < numHullPoints; i++) {
                g2.drawLine((int) (Xwid * x[D[i]]), size.height - (int) (Ywid * y[D[i]]),
                        (int) (Xwid * x[D[(i + 1) % numHullPoints]]),
                        size.height - (int) (Ywid * y[D[(i + 1) % numHullPoints]]));
            }
            // Now draw the points
            for (int i = 0; i < numPoints; i++) {
                g2.fillRect((int) (Xwid * x[i]) - POINTWID, size.height - (int) (Ywid * y[i])
                        - POINTWID, 2 * POINTWID + 1, 2 * POINTWID + 1);
            }
            
            // Now draw the chords found in our createMinimalConvexulation function
            g2.setColor(Color.blue);
            for(int i = 0; i < numConvexulationChords; i++){
            	int[] chord = convexulationChords.get(i);
                g2.drawLine((int) (Xwid * x[D[chord[0]]]), size.height - (int) (Ywid * y[D[chord[0]]]),
                        (int) (Xwid * x[D[chord[1]]]),
                        size.height - (int) (Ywid * y[D[chord[1]]]));
            }
        }
    }
}