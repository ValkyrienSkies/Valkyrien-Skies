package org.valkyrienskies.addon.control.renderer.atom_animation_parser.parsers;

import java.util.Scanner;
import java.util.Stack;

public class AtomParser {

    public final AtomParserElement head;
    private final Stack<AtomParserElement> elementBacktrace;

    public AtomParser(Scanner input) {
        this.head = new AtomParserElement();
        this.elementBacktrace = new Stack<>();
        elementBacktrace.add(head);

        while (input.hasNextLine()) {
            readLine(input.nextLine());
        }
    }

    private void readLine(String line) {
        // First remove these semicolons, as well as any leading or trailing spaces.
        line = line.replaceAll(";", "").trim();
        // First remove any whitespace from this string.
        String[] properties = line.split("[ \t]+");
        if (line.contains("{")) {
            // Create a new element on the tree, push it onto the backtrace.
            AtomParserElement element = new AtomParserElement(properties[0]);
            elementBacktrace.peek().branches.add(element);
            elementBacktrace.push(element);
        } else if (line.contains("}")) {
            // Finished with an element on the tree, pop it from the backtrace.
            elementBacktrace.pop();
        } else {
            AtomParserElement currentElement = elementBacktrace.peek();
            currentElement.properties.add(properties);
        }
    }

}
