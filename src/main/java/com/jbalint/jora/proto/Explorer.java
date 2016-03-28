package com.jbalint.jora.proto;

import javax.swing.*;
import java.awt.*;
import java.time.Duration;
import com.yworks.yfiles.canvas.*;
import com.yworks.yfiles.system.*;
import com.yworks.yfiles.drawing.*;
import com.yworks.yfiles.graph.*;
import com.yworks.yfiles.input.*;
import com.yworks.yfiles.geometry.*;
import com.yworks.yfiles.io.*;
import com.yworks.yfiles.layout.*;
import com.yworks.yfiles.layout.circular.*;
import com.yworks.yfiles.layout.hierarchic.*;
import com.yworks.yfiles.layout.organic.*;
import com.yworks.yfiles.layout.orthogonal.*;
import com.yworks.yfiles.layout.radial.*;
import com.yworks.yfiles.layout.random.*;
import com.yworks.yfiles.layout.router.*;
import com.yworks.yfiles.layout.router.polyline.*;
import com.yworks.yfiles.layout.tree.*;

// file:///home/jbalint/sw/java-sw/yFiles-3.0TP4/doc/api/index.html#/dguide/mvc_controller
public class Explorer {
	static GraphComponent graphComponent = new GraphComponent();
	public static void main(String argsXXX[]) {
		JFrame frame = new JFrame();
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.add(graphComponent, BorderLayout.CENTER);
		frame.setVisible(true);

		IGraph graph = graphComponent.getGraph();
    //////////// Sample node creation ///////////////////
    // Creates two nodes with the default node size
    // The location is specified for the center
    INode node1 = graph.createNode(new PointD(50, 50));
    INode node2 = graph.createNode(new PointD(150, 50));
    // Creates a third node with a different size of 80x40
    // In this case, the location of (360,380) describes the upper left
    // corner of the node bounds
    INode node3 = graph.createNode(new RectD(360, 380, 80, 40));
    /////////////////////////////////////////////////////

    //////////// Sample edge creation ///////////////////
    // Creates some edges between the nodes
    IEdge edge1 = graph.createEdge(node1, node2);
    IEdge edge2 = graph.createEdge(node2, node3);
    /////////////////////////////////////////////////////

    //////////// Using Bends ////////////////////////////
    // Creates the first bend for edge2 at (400, 50)
    IBend bend1 = graph.addBend(edge2, new PointD(400, 50));
    /////////////////////////////////////////////////////

    //////////// Using Ports ////////////////////////////
    // Actually, edges connect "ports", not nodes directly.
    // If necessary, you can manually create ports at nodes
    // and let the edges connect to these.
    // Creates a port in the center of the node layout
    //IPort port1AtNode1 = graph.addPort(node1, NodeScaledPortLocationModel.NODE_CENTER_ANCHORED);

    // Creates a port at the middle of the left border
    // Note to use absolute locations in world coordinates when placing ports using PointD.
    // The method obtains a model parameter that best matches the given port location.
    //IPort port1AtNode3 = graph.addPort(node3, new PointD(node3.getLayout().getX(), node3.getLayout().getCenter().getY()));

    // Creates an edge that connects these specific ports
    //IEdge edgeAtPorts = graph.createEdge(port1AtNode1, port1AtNode3);
    /////////////////////////////////////////////////////

    //////////// Sample label creation ///////////////////
    // Adds labels to several graph elements
    graph.addLabel(node1, "This is node 1");
    graph.addLabel(node2, "This is node 2");
    graph.addLabel(node3, "This is node 3");
    //graph.addLabel(edgeAtPorts, "Edge at Ports");
    /////////////////////////////////////////////////////

	graphComponent.updateContentRect();
	graphComponent.fitGraphBounds();
	LayoutExtensions.morphLayout(graphComponent, new IncrementalHierarchicLayouter(), Duration.ofSeconds(3), null);

    // create a simple mode that reacts to mouse clicks on nodes.
    GraphViewerInputMode graphViewerInputMode = new GraphViewerInputMode();
    graphViewerInputMode.setClickableItems(GraphItemTypes.NODE);
    graphViewerInputMode.addItemClickedListener((source, args) -> {
      if (args.getItem() instanceof INode) {
		  System.err.println("Clicked: " + args.getItem() + " (" + source + ")");
		  INode node4 = graph.createNode(new PointD(50, 50));
		  graph.addLabel(node4, "NODE");
		  IEdge edge3 = graph.createEdge((INode) args.getItem(), node4);
		  //LayoutExtensions.morphLayout(graphComponent, new IncrementalHierarchicLayouter(), Duration.ofSeconds(3), null);
		  LayoutExtensions.applyLayout(graphComponent.getGraph(), new OrganicLayouter());
      }
    });
    graphViewerInputMode.addItemDoubleClickedListener((source, args) -> {
      if (args.getItem() instanceof INode) {
		  System.err.println("Dbl Clicked: " + args.getItem() + " (" + source + ")");
		  INode node4 = graph.createNode(new PointD(50, 50));
		  graph.addLabel(node4, "DBL NODE");
		  IEdge edge3 = graph.createEdge((INode) args.getItem(), node4);
	LayoutExtensions.morphLayout(graphComponent, new IncrementalHierarchicLayouter(), Duration.ofSeconds(3), null);
      }
    });
    graphComponent.setInputMode(graphViewerInputMode);
	}
}
