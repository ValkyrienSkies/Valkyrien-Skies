package com.best108.atom_animation_reader.parsers;

import java.util.Scanner;
import java.util.Stack;

import valkyrienwarfare.mod.client.render.GibsAnimationRegistry;
import valkyrienwarfare.mod.coordinates.VectorImmutable;

public class PivotParser {

	private final Stack<PivotElement> pivotElements;
	
	public PivotParser(Scanner input) {
		this.pivotElements = new Stack<PivotElement>();
		while (input.hasNextLine()) {
			readLine(input.nextLine());
		}
	}
	
	private void readLine(String line) {
		// First remove any whitespace from this string.
		String[] properties = line.trim().split("[ \t]+");
		// Ignore empty and whitespace lines
		if (properties.length > 1) {
			if (properties[1].equals("pivot:")) {
				// We are on a pivot line, create a new pivot element.
				PivotElement pivotElement = new PivotElement(properties[0]);
				// Then push it onto the stack.
				pivotElements.push(pivotElement);
			} else if (properties[0].equals("translate")) {
				// We are on a pivot coordinates line
				switch(properties[1])
		        {
		            case "x:":
		            	pivotElements.peek().pivotX = Double.valueOf(properties[2]);
		                break;
		            case "y:":
		            	pivotElements.peek().pivotY = Double.valueOf(properties[2]);
		                break;
		            case "z:":
		            	pivotElements.peek().pivotZ = Double.valueOf(properties[2]);
		                break;
		        }
			}
		}
	}
	
	public void registerPivots() {
		for (PivotElement pivotElement : pivotElements) {
			GibsAnimationRegistry.registerPivot(pivotElement.modelName, pivotElement.getPivot());
		}
	}
	
	private class PivotElement {
		final String modelName;
		double pivotX;
		double pivotY;
		double pivotZ;
		
		PivotElement(String modelName) {
			this.modelName = modelName;
			this.pivotX = 0;
			this.pivotY = 0;
			this.pivotZ = 0;
		}
		
		VectorImmutable getPivot() {
			return new VectorImmutable(pivotX, pivotY, pivotZ);
		}
	}
	
}
