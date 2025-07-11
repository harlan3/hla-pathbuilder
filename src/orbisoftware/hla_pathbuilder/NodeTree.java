/*
 *  HLA PathBuilder
 *
 *  Copyright (C) 2022 Harlan Murphy
 *  Orbis Software - orbisoftware@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package orbisoftware.hla_pathbuilder;

import java.util.Iterator;
import org.apache.commons.lang3.StringUtils;

// This class traverses the FOM tree and generates the xml or mindmap file for each 
// of the objects and interactions
public class NodeTree {

	private DatabaseAPI databaseAPI = new DatabaseAPI();
	public NodeElement root;
	private int stackDepth;
	private int startNodeCount;
	private int endNodeCount;
	private int pathCount;
	
	private Utils utils = new Utils();
	
	public NodeTree(String rootValue) {

		this.root = new NodeElement(rootValue, false);
		this.setDefaults();
	}
	
	public void setDefaults() {
		
		this.stackDepth = 0;
		this.startNodeCount = 0;
		this.endNodeCount = 0;
		this.pathCount = 0;
	}

	public NodeElement insertNode(NodeElement parent, String value, boolean formatted) {
		
		NodeElement newNode = new NodeElement(value, formatted);

		newNode.formatted = formatted;
		parent.children.add(newNode);
		return newNode;
	}

	public boolean replaceNode(NodeElement parent, String oldValue, String newValue) {

		for (int i = 0; i < parent.children.size(); i++) {
			NodeElement child = parent.children.get(i);
			if (child.isNodeEqual(oldValue)) {
				parent.children.set(i, new NodeElement(newValue, false));
				return true;
			} else {
				if (replaceNode(child, oldValue, newValue)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean deleteNode(NodeElement parent, String value) {

		Iterator<NodeElement> iterator = parent.children.iterator();
		while (iterator.hasNext()) {
			NodeElement child = iterator.next();
			if (child.isNodeEqual(value)) {
				iterator.remove();
				return true;
			} else {
				if (deleteNode(child, value)) {
					return true;
				}
			}
		}
		return false;
	}

	public NodeElement getParent(NodeElement root, NodeElement node) {

		if (root == node) {
			return null; // Root node has no parent
		}

		for (NodeElement child : root.children) {
			if (child == node) {
				return root;
			} else {
				NodeElement parent = getParent(child, node);
				if (parent != null) {
					return parent;
				}
			}
		}
		return null;
	}

	public NodeElement getNode(NodeElement root, String value) {

		if (root.isNodeEqual(value)) {
			return root;
		}

		for (NodeElement child : root.children) {
			NodeElement foundNode = getNode(child, value);
			if (foundNode != null) {
				return foundNode;
			}
		}
		return null;
	}

	public NodeElement getChild(NodeElement parent, String value) {

		for (NodeElement child : parent.children) {
			if (child.isNodeEqual(value)) {
				return child;
			}
		}
		return null;
	}

	public boolean childNodeExists(NodeElement parent, String value) {

		for (NodeElement child : parent.children) {
			if (child.isNodeEqual(value) || childNodeExists(child, value)) {
				return true;
			}
		}
		return false;
	}

	public boolean parentNodeExists(NodeElement root, NodeElement node) {

		if (root == node) {
			return true;
		}

		for (NodeElement child : root.children) {
			if (parentNodeExists(child, node)) {
				return true;
			}
		}
		return false;
	}

	void setStackDepthInc() {
		stackDepth = stackDepth + 3;
	}

	void setStackDepthDec() {
		stackDepth = stackDepth - 3;

		if (stackDepth < 1)
			stackDepth = 0;
	}

	private String insertIndentSpaces() {

		String returnVal = "";

		if (stackDepth > 0)
			returnVal = String.format("%" + (stackDepth) + "s", "");

		return returnVal;
	}

	public int getDocMetaDataStartNode() {

		return startNodeCount;
	}

	public int getDocMetaDataEndNode() {

		return endNodeCount;
	}

	public void printContents(String contents) {

		if (contents.equals(""))
			return;

		if (contents.trim().equals("</node>")) {

			contents = insertIndentSpaces();
			contents += "</node>";
			setStackDepthDec();
		}

		System.out.println(contents);
		
		startNodeCount += StringUtils.countMatches(contents, "<node");
		endNodeCount += StringUtils.countMatches(contents, "</node>");
	}

	private String conditionalInsertEndNode(String elementString) {

		if (elementString.contains("path=") || elementString.contains("classHandle=")
				|| elementString.contains("<attributes") || elementString.contains("</attributes")
				|| elementString.contains("<parameters") || elementString.contains("</parameters")
				|| elementString.contains("<metaData") || elementString.contains("</metaData") 
				|| elementString.contains("<map version") || elementString.contains("</map version")
				|| elementString.equals("MetaData"))
			return "";
		else {
			String returnVal = "";

			setStackDepthInc();
			returnVal = insertIndentSpaces();
			returnVal += "</node>";
			setStackDepthDec();

			return returnVal;
		}
	}

	public void traverseTree(NodeElement node, boolean mindMapMode) {

		if (node == null) {
			return;
		}
		
		if (node.elementString != Constants.NULL_UUID && !node.elementString.equals("")) {

			String elementNodes[] = node.elementString.split("\\|");
			
			if (node.elementString.contains("isDiscriminant=\"true\"")) {
				
				setStackDepthInc();
				String format = insertIndentSpaces();
				
				String nameSplit[] = elementNodes[0].trim().split(" | ");
				
				String nameStr = "";
				String lineNumberStr = "";
				
				lineNumberStr = utils.extractLineNumberContentNoBraces(nameSplit[0]);
				
				if (MMGenerator.debugLineNumbersInOutput)
					nameStr = nameSplit[0];
				else
					nameStr = utils.removeLineNumberContent(nameSplit[0]);
				
				String semanticsText = databaseAPI.getSemanticsText(elementNodes[1], nameStr, nameSplit[1]);

				printContents(
						format + "<node ID=\"" + elementNodes[2].trim() + "\" " + "TEXT=\""
						+ nameStr + " " + nameSplit[1] + "\" " + elementNodes[3].trim() + " " + 
						elementNodes[1].trim() + " SEMANTICS=\"" + semanticsText + "\" FOM_LINE_NUMBER=\"" + lineNumberStr
						+ "\" FOLDED=\"true\">");
				
				return;
			}
			
			if (elementNodes[0].contains("HLAopaqueData")) {
				
				setStackDepthInc();
				String format = insertIndentSpaces();
				
				String nameSplit[] = elementNodes[0].trim().split(" ");
				
				String nameStr = "";
				String lineNumberStr = "";
				
				lineNumberStr = utils.extractLineNumberContentNoBraces(nameSplit[0]);
				
				if (MMGenerator.debugLineNumbersInOutput)
					nameStr = nameSplit[0];
				else
					nameStr = utils.removeLineNumberContent(nameSplit[0]);
				
				String semanticsText = databaseAPI.getSemanticsText("TID=\"Array\"", nameStr + "Imp", nameSplit[1]);

				printContents(
						format + "<node ID=\"" + elementNodes[3].trim() + "\" " + "TEXT=\""
						+ nameStr + "Imp " + nameSplit[1] + "\"" +
						" TID=\"Array\" classtype=\"HLAbyte\" cardinality=\"Dynamic\" encoding=\"HLAvariableArray\"" + 
						" SEMANTICS=\"" + semanticsText + "\" FOM_LINE_NUMBER=\"" + lineNumberStr + "\" FOLDED=\"true\">");
				return;
			}
			
			if (elementNodes[0].contains("HLAunicodeString")) {
				
				setStackDepthInc();
				String format = insertIndentSpaces();
				
				String nameSplit[] = elementNodes[0].trim().split(" ");
				
				String nameStr = "";
				String lineNumberStr = "";
				
				lineNumberStr = utils.extractLineNumberContentNoBraces(nameSplit[0]);
				
				if (MMGenerator.debugLineNumbersInOutput)
					nameStr = nameSplit[0];
				else
					nameStr = utils.removeLineNumberContent(nameSplit[0]);
				
				String semanticsText = databaseAPI.getSemanticsText("TID=\"Array\"", nameStr + "Imp", nameSplit[1]);

				printContents(
						format + "<node ID=\"" + elementNodes[3].trim() + "\" " + "TEXT=\""
						+ nameStr + "Imp " + nameSplit[1] + "\"" +
						" TID=\"Array\" classtype=\"HLAoctetPairBE\" cardinality=\"Dynamic\" encoding=\"HLAvariableArray\"" + 
						" SEMANTICS=\"" + semanticsText + "\" FOM_LINE_NUMBER=\"" + lineNumberStr + "\" FOLDED=\"true\">");
				return;
			}
			
			if (elementNodes[0].contains("HLAASCIIstring")) {
				
				setStackDepthInc();
				String format = insertIndentSpaces();
				
				String nameSplit[] = elementNodes[0].trim().split(" ");
				
				String nameStr = "";
				String lineNumberStr = "";
				
				lineNumberStr = utils.extractLineNumberContentNoBraces(nameSplit[0]);
				
				if (MMGenerator.debugLineNumbersInOutput)
					nameStr = nameSplit[0];
				else
					nameStr = utils.removeLineNumberContent(nameSplit[0]);
				
				String semanticsText = databaseAPI.getSemanticsText("TID=\"Array\"", nameStr + "Imp", nameSplit[1]);

				printContents(
						format + "<node ID=\"" + elementNodes[2].trim() + "\" " + "TEXT=\"" + nameStr + "Imp " + nameSplit[1] + "\"" +
						" TID=\"Array\" classtype=\"HLAASCIIchar\" cardinality=\"Dynamic\" encoding=\"HLAvariableArray\"" + 
						" SEMANTICS=\"" + semanticsText + "\" FOM_LINE_NUMBER=\"" + lineNumberStr + "\" FOLDED=\"true\">");
				return;
			}

			if (node.elementString.contains("TID=\"Array\"")) {

				setStackDepthInc();
				String format = insertIndentSpaces();

				String nameSplit[] = elementNodes[0].trim().split(" ");
				
				String nameStr = "";
				String lineNumberStr = "";
				
				lineNumberStr = utils.extractLineNumberContentNoBraces(nameSplit[0]);
				
				if (MMGenerator.debugLineNumbersInOutput)
					nameStr = nameSplit[0];
				else
					nameStr = utils.removeLineNumberContent(nameSplit[0]);
				
				if (nameSplit.length > 1) {
					
					String semanticsText = databaseAPI.getSemanticsText("TID=\"Array\"", nameStr, nameSplit[1]);
					
					String classEncodingType = elementNodes[1].trim();
					
					if (classEncodingType.contains("HLAunicodeString"))
						classEncodingType = classEncodingType.replaceAll("HLAunicodeString", "HLAunicodeStringImp");
					
					printContents(
							format + "<node ID=\"" + elementNodes[3].trim() + "\" " + "TEXT=\""
							+ nameStr + " " + nameSplit[1] + "\" " + elementNodes[2].trim() + " " + classEncodingType 
							+  " SEMANTICS=\"" + semanticsText + "\" FOM_LINE_NUMBER=\"" + lineNumberStr
							+ "\" FOLDED=\"true\">");
				} else {
	
					String semanticsText = databaseAPI.getSemanticsText("TID=\"Array\"", nameStr);
							
					printContents(
						format + "<node ID=\"" + elementNodes[3].trim() + "\" " + "TEXT=\""
						+ nameStr + "\" " + elementNodes[2].trim() + " " + elementNodes[1].trim() 
						+ " SEMANTICS=\"" + semanticsText + "\" FOM_LINE_NUMBER=\"" + lineNumberStr
						+ "\" FOLDED=\"true\">");
				}

			} else if (node.elementString.contains("TID=\"FixedRecord\"")) {

				setStackDepthInc();
				String format = insertIndentSpaces();

				String nameSplit[] = elementNodes[0].trim().split(" ");
				
				String nameStr = "";
				String lineNumberStr = "";

				lineNumberStr = utils.extractLineNumberContentNoBraces(nameSplit[0]);
				
				if (MMGenerator.debugLineNumbersInOutput)
					nameStr = nameSplit[0];
				else
					nameStr = utils.removeLineNumberContent(nameSplit[0]);
				
				if (nameSplit.length > 1) {
					
					String semanticsText = databaseAPI.getSemanticsText("TID=\"FixedRecord\"", nameStr, nameSplit[1]);

					if (nameSplit[1].endsWith("Array"))
						nameSplit[1] = utils.convertToCamelCase(nameStr);

					printContents(format + "<node ID=\"" + elementNodes[2].trim() + "\" " + "TEXT=\"" + nameStr
							+ " " + nameSplit[1] + "\" " + elementNodes[1].trim() 
							+ " SEMANTICS=\"" + semanticsText + "\" FOM_LINE_NUMBER=\"" + lineNumberStr
							+ "\" FOLDED=\"true\">");
				} else {
					
					String semanticsText = databaseAPI.getSemanticsText("TID=\"FixedRecord\"", nameStr);

					printContents(format + "<node ID=\"" + elementNodes[2].trim() + "\" " + "TEXT=\"" + nameStr
							+ "\" " + elementNodes[1].trim() + " SEMANTICS=\"" + semanticsText 
							+ "\" FOM_LINE_NUMBER=\"" + lineNumberStr + "\" FOLDED=\"true\">");
				}

			} else if (node.elementString.contains("path=")) {

				pathCount++;

				// indent for path
				setStackDepthInc();

				// inject closing node for path
				if (pathCount > 1) {

					setStackDepthDec();
					String format = insertIndentSpaces();
					printContents(format + "</node>");
					setStackDepthInc();
				}

				String format = insertIndentSpaces();
				printContents(format + "<node " + node.elementString);

			} else if (node.elementString.contains("classHandle=")) {

				String format = insertIndentSpaces();
				printContents(format + "<node " + node.elementString);
			} else if (node.elementString.contains("</node>") && (node.formatted == false)) {

				setStackDepthDec();
				String format = insertIndentSpaces();
				printContents(format + "</node>");
			} else if (node.elementString.contains("</node>") && (node.formatted == true)) {

				printContents(node.elementString);

			} else if (node.elementString.contains("<attributes>")
					|| node.elementString.contains("<attributesLength>")) {

				printContents(node.elementString);

			} else if (node.elementString.contains("<parameters>")
					|| node.elementString.contains("<parametersLength>")) {

				printContents(node.elementString);

			} else if (node.elementString.contains("<metaData>") || node.elementString.contains("</metaData>")) {

				printContents(node.elementString);

			} else if (node.elementString.contains("<map version")) {

				if (mindMapMode) // mindmap format
					printContents(node.elementString);
				else { // xml format
					printContents("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
					printContents("<doc>");
				}
			} else if (node.elementString.contains("MetaData")) {
				// placeholder in tree
			} else {
				setStackDepthInc();
				String format = insertIndentSpaces();
				String semanticsText = "";
				
				// Fix nodes that are referencing non existent RPR types in support of codegen
				String rprRemoval[] = elementNodes[0].trim().split(" ");
				
				if (utils.removeLineNumberContent(rprRemoval[0]).startsWith("RPR")) {
					
					String nameStr = "";
					String lineNumberStr = "";

					lineNumberStr = utils.extractLineNumberContentNoBraces(rprRemoval[0]);
					
					if (MMGenerator.debugLineNumbersInOutput)
						nameStr = rprRemoval[0];
					else
						nameStr = utils.removeLineNumberContent(rprRemoval[0]);
					
					String nodeType = utils.convertFromRPRType(nameStr);
					
					semanticsText = databaseAPI.getSemanticsText(elementNodes[1], "", rprRemoval[1].trim());

					printContents(format + "<node ID=\"" + elementNodes[2].trim() + "\" " + "TEXT=\""
							+ nodeType.trim() + " " + rprRemoval[1].trim() 
							+ "\" " + elementNodes[1].trim() + " SEMANTICS=\"" + semanticsText 
							+ "\" FOM_LINE_NUMBER=\"" + lineNumberStr + "\" FOLDED=\"true\">");
				} else {
									
					String nameSplit[] = elementNodes[0].trim().split(" ");
					
					
					String nameStr = "";
					String lineNumberStr = "";

					lineNumberStr = utils.extractLineNumberContentNoBraces(nameSplit[0]);
					
					if (MMGenerator.debugLineNumbersInOutput)
						nameStr = nameSplit[0];
					else
						nameStr = utils.removeLineNumberContent(nameSplit[0]);
					
					semanticsText = databaseAPI.getSemanticsText(elementNodes[1], nameStr, nameSplit[1]);
					
					if (nameSplit.length > 1 && elementNodes.length > 2) {
						
						printContents(format + "<node ID=\"" + elementNodes[2].trim() + "\" " + "TEXT=\""
								+ nameStr.trim() + " " + nameSplit[1].trim() 
								+ "\" " + elementNodes[1].trim() + " SEMANTICS=\"" + semanticsText 
								+ "\" FOM_LINE_NUMBER=\"" + lineNumberStr + "\" FOLDED=\"true\">");
					} else {
						
						printContents(format + "<node ID=\"" + elementNodes[2].trim() + "\" " + "TEXT=\""
								+ nameStr.trim() + "\" " + elementNodes[1].trim() 
								+ " SEMANTICS=\"" + semanticsText 
								+ "\" FOM_LINE_NUMBER=\"" + lineNumberStr + "\" FOLDED=\"true\">");
					}
				}
			}
		}

		for (NodeElement child : node.children) {
			traverseTree(child, mindMapMode);
			if ((!child.formatted) && (!child.elementString.equals("")))
				printContents(conditionalInsertEndNode(child.elementString));
		}
	}
}
