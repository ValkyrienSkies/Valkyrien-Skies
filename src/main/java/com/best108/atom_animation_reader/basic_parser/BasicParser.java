package com.best108.atom_animation_reader.basic_parser;

import java.util.Scanner;
import java.util.Stack;

public class BasicParser {

	public final ParserElement head;
	private final Stack<ParserElement> elementBacktrace;
	
	public BasicParser(Scanner input) {
		this.head = new ParserElement();
		this.elementBacktrace = new Stack<ParserElement>();
		elementBacktrace.add(head);
		
		while (input.hasNext()) {
			readLine(input.nextLine());
		}
	}
	
	private void readLine(String line) {
		// First remove these semicolons.
		line = line.replaceAll(";", "").trim();
		// First remove any whitespace from this string.
		String[] properties = line.split("[ \t]+");
		if (line.contains("{")) {
			// Create a new element on the tree, push it onto the backtrace.
			ParserElement element = new ParserElement(properties[0]);
			elementBacktrace.peek().branches.add(element);
			elementBacktrace.push(element);
		} else if (line.contains("}")) {
			// Finished with an element on the tree, pop it from the backtrace.
			elementBacktrace.pop();
		} else {
			ParserElement currentElement = elementBacktrace.peek();
			currentElement.properties.add(properties);
		}
	}

}
