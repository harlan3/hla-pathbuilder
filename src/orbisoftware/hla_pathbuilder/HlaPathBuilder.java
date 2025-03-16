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

import java.io.File;
import java.io.PrintStream;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import orbisoftware.hla_pathbuilder.Constants.*;
import orbisoftware.hla_pathbuilder.db_classes.*;

public class HlaPathBuilder {

	private DatabaseAPI databaseAPI = new DatabaseAPI();
	private static final String fomSupportTypes = "FOM_support_types.xml";
	public static final String protocolSpecDir = "protocol_specs";
	private static Stack<String> pathBuilderStack = new Stack<String>();
	private static Stack<String> debugStack = new Stack<String>();
	private static List<String> elementObjectList = new ArrayList<String>();
	private static List<String> elementInteractionList = new ArrayList<String>();
	private static final int maxSemanticLineWidth = 400;
	
	public Utils utils = new Utils();

	// If true, use memory based database. Otherwise use a file based database.
	public static boolean useMemoryDb = false;

	// If true, output of pathdefs will contain UUIDs along with the field names
	public static boolean uuidMarkupOutput = true;

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	void parseAttribute(Node node, int index, UUID parentUUID) {

		Node nodeChild = node.getFirstChild();

		String dataType = "";
		String variableName = "";
		String origVariableName = "";
		String semantics = "";
		
		while (nodeChild != null) {

			String name = nodeChild.getNodeName();

			if (name.equals("name")) {
				origVariableName = nodeChild.getTextContent();
				variableName = utils.convertToCamelCase(nodeChild.getTextContent());
			}

			if (name.equals("dataType")) {
				dataType = nodeChild.getTextContent();
			}
			
			if (name.equals("semantics")) {
				semantics = nodeChild.getTextContent();
			}

			nodeChild = nodeChild.getNextSibling();
		}

		// Insert attribute into database
		List<DbAttribute> list = new ArrayList<DbAttribute>();
		DbAttribute var = new DbAttribute();

		var.id = UUID.randomUUID().toString();
		var.index = index;
		var.origName = origVariableName;
		var.name = variableName;
		var.type = dataType;
		var.inherited = false;
		var.parentObject = parentUUID.toString();

		list.add(var);

		databaseAPI.insertIntoAttributeTable(list);
		
		// Insert semantics into database
		List<DbSemanticsDatatype> list2 = new ArrayList<DbSemanticsDatatype>();
		DbSemanticsDatatype var2 = new DbSemanticsDatatype();
		
		var2.id = var.id;
		var2.name = var.origName;
		var2.semantics = utils.truncateString(semantics, maxSemanticLineWidth);
		
		list2.add(var2);
		
		if (semantics.length() > 0)
			databaseAPI.insertIntoSemanticsDatatypeTable(list2);
		
	}

	String parseObject(Node node, String parentClass, UUID parentUUID) {

		String objectName = "";
		String typedefName = "";
		UUID objectUUID = null;
		int attributeIndex = 0;
		String semanticsUUID = "";
		String semantics = "";
		
		Node nodeChild = node.getFirstChild();
		
		while (nodeChild != null) {

			String name = nodeChild.getNodeName();

			if (name.equals("name")) {

				objectName = nodeChild.getTextContent();

				System.out.println("// Start Object");
				System.out.println("// " + objectName);
				System.out.println("typedef struct {");

				// Insert object into database
				objectUUID = UUID.randomUUID();
				List<DbObject> list = new ArrayList<DbObject>();

				typedefName = objectName;

				pathBuilderStack.push(objectName);
				debugStack.push(objectUUID.toString());

				DbObject var = new DbObject();
				
				semanticsUUID = objectUUID.toString();
				var.id = objectUUID.toString();
				var.name = objectName;
				var.path = pathBuilderStack.toString();
				var.debugPath = debugStack.toString();
				var.parentObject = parentUUID.toString();

				list.add(var);
				databaseAPI.insertIntoObjectTable(list);

				if (!parentClass.isEmpty()) {

					String origVariableName = parentClass;
					String variableName = utils.convertToCamelCase(parentClass);
					attributeIndex++;

					System.out.println("   " + parentClass + " " + origVariableName + "; // extends");
					databaseAPI.insertExtendsAttribute(attributeIndex, origVariableName, variableName, parentClass, objectUUID);

				}
			}
			
			if (name.equals("semantics")) {

				semantics = nodeChild.getTextContent();
				
				// Insert semantics into database
				List<DbSemanticsDatatype> list2 = new ArrayList<DbSemanticsDatatype>();
				DbSemanticsDatatype var2 = new DbSemanticsDatatype();
				
				var2.id = semanticsUUID;
				var2.name = "NA";
				var2.semantics = utils.truncateString(semantics, maxSemanticLineWidth);
				
				list2.add(var2);
				
				if (semantics.length() > 0)
					databaseAPI.insertIntoSemanticsDatatypeTable(list2);
			}

			if (name.equals("attribute")) {
				attributeIndex++;
				parseAttribute(nodeChild, attributeIndex, objectUUID);
			}

			// nested object definitions define object hierarchy
			if (name.equals("objectClass")) {

				System.out.println("} " + typedefName + ";");
				System.out.println("// End Object" + "\n");
				typedefName = parseObject(nodeChild, objectName, objectUUID);
				pathBuilderStack.pop();
				debugStack.pop();
			}

			nodeChild = nodeChild.getNextSibling();
		}

		if (parentClass.isEmpty()) {
			System.out.println("} " + typedefName + ";");
			System.out.println("// End Object" + "\n\n");
			pathBuilderStack.pop();
			debugStack.pop();
		}

		return typedefName;
	}

	void parseObjects(Node node) {

		Node nodeChild = node.getFirstChild();

		while (nodeChild != null) {

			String name = nodeChild.getNodeName();

			if (name.equals("objectClass"))
				parseObject(nodeChild, "", new UUID(0, 0));

			nodeChild = nodeChild.getNextSibling();
		}
	}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	void parseParameter(Node node, int index, UUID parentUUID) {

		Node nodeChild = node.getFirstChild();

		String dataType = "";
		String variableName = "";
		String origVariableName = "";
		String semantics = "";
		
		while (nodeChild != null) {

			String name = nodeChild.getNodeName();

			if (name.equals("name")) {
				origVariableName = nodeChild.getTextContent();
				variableName = utils.convertToCamelCase(nodeChild.getTextContent());
			}

			if (name.equals("dataType"))
				dataType = nodeChild.getTextContent();
			
			if (name.equals("semantics"))
				semantics = nodeChild.getTextContent();

			nodeChild = nodeChild.getNextSibling();
		}

		// Insert parameter into database
		List<DbParameter> list = new ArrayList<DbParameter>();

		DbParameter var = new DbParameter();

		var.id = UUID.randomUUID().toString();
		var.index = index;
		var.origName = origVariableName;
		var.name = variableName;
		var.type = dataType;
		var.inherited = false;
		var.parentObject = parentUUID.toString();

		list.add(var);

		databaseAPI.insertIntoParameterTable(list);
		
		// Insert semantics into database
		List<DbSemanticsDatatype> list2 = new ArrayList<DbSemanticsDatatype>();
		DbSemanticsDatatype var2 = new DbSemanticsDatatype();
		
		var2.id = var.id;
		var2.name = var.origName;
		var2.semantics = utils.truncateString(semantics, maxSemanticLineWidth);
		
		list2.add(var2);
		
		if (semantics.length() > 0)
			databaseAPI.insertIntoSemanticsDatatypeTable(list2);

		System.out.println("   " + dataType + " " + origVariableName + ";");
	}

	String parseInteraction(Node node, String parentClass, UUID parentUUID) {

		String interactionName = "";
		String typedefName = "";
		UUID interactionUUID = null;
		int parameterIndex = 0;
		String semanticsUUID = "";
		
		Node nodeChild = node.getFirstChild();

		while (nodeChild != null) {

			String name = nodeChild.getNodeName();

			if (name.equals("name")) {

				interactionName = nodeChild.getTextContent();
				System.out.println("// Start Interaction");
				System.out.println("// " + interactionName);
				System.out.println("typedef struct {");

				// Insert interaction into database
				interactionUUID = UUID.randomUUID();
				List<DbInteraction> list = new ArrayList<DbInteraction>();

				typedefName = interactionName;

				pathBuilderStack.push(interactionName);
				debugStack.push(interactionUUID.toString());

				DbInteraction var = new DbInteraction();

				semanticsUUID = interactionUUID.toString();
				var.id = interactionUUID.toString();
				var.name = interactionName;
				var.path = pathBuilderStack.toString();
				var.debugPath = debugStack.toString();
				var.parentObject = parentUUID.toString();

				list.add(var);
				databaseAPI.insertIntoInteractionTable(list);

				// print out the parent object that is extended by this struct
				if (!parentClass.isEmpty()) {

					String origVariableName = parentClass;
					String variableName = utils.convertToCamelCase(parentClass);
					parameterIndex++;

					System.out.println("   " + parentClass + " " + origVariableName + "; // extends");
					databaseAPI.insertExtendsParameter(parameterIndex, origVariableName, variableName, parentClass,
							interactionUUID);
				}
			}

			if (name.equals("semantics")) {

				// Insert semantics into database
				List<DbSemanticsDatatype> list = new ArrayList<DbSemanticsDatatype>();
				DbSemanticsDatatype var = new DbSemanticsDatatype();
				
				var.id = semanticsUUID;
				var.name = "NA";
				var.semantics = utils.truncateString(nodeChild.getTextContent(), maxSemanticLineWidth);
				
				list.add(var);
				
				if (var.semantics.length() > 0)
					databaseAPI.insertIntoSemanticsDatatypeTable(list);
			}
			
			if (name.equals("parameter")) {
				parameterIndex++;
				parseParameter(nodeChild, parameterIndex, interactionUUID);
			}

			// nested interaction definitions define interaction hierarchy
			if (name.equals("interactionClass")) {
				System.out.println("} " + typedefName + ";");
				System.out.println("// End Interaction");
				System.out.println();
				typedefName = parseInteraction(nodeChild, interactionName, interactionUUID);
				pathBuilderStack.pop();
				debugStack.pop();
			}

			nodeChild = nodeChild.getNextSibling();
		}

		if (parentClass.isEmpty()) {
			System.out.println("} " + typedefName + ";");
			System.out.println("// End Interaction");
			System.out.println();
			pathBuilderStack.pop();
			debugStack.pop();
		}

		return typedefName;
	}

	void parseInteractions(Node node) {

		Node nodeChild = node.getFirstChild();

		while (nodeChild != null) {

			String name = nodeChild.getNodeName();

			if (name.equals("interactionClass"))
				parseInteraction(nodeChild, "", new UUID(0, 0));

			nodeChild = nodeChild.getNextSibling();
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	void parseBasicData(Node nodeChild) {

		String basicType = "";
		String size = "";
		String endian = "";
		String semantics = "";
		
		boolean hasData = false;

		nodeChild = nodeChild.getFirstChild();

		while (nodeChild != null) {

			String name = nodeChild.getNodeName();
			hasData = true;

			if (name.equals("name"))
				basicType = nodeChild.getTextContent();

			if (name.equals("size"))
				size = nodeChild.getTextContent();

			if (name.equals("endian"))
				endian = nodeChild.getTextContent();
			
			if (name.equals("semantics"))
				semantics = nodeChild.getTextContent();

			nodeChild = nodeChild.getNextSibling();
		}

		if (hasData) {

			List<DbBasicDatatype> list = new ArrayList<DbBasicDatatype>();

			DbBasicDatatype var1 = new DbBasicDatatype();

			var1.id = UUID.randomUUID().toString();
			var1.name = basicType;
			var1.type = "";
			var1.size = size;
			var1.endian = endian;

			list.add(var1);

			databaseAPI.insertIntoBasicDatatypeTable(list);
			
			// Insert semantics into database
			List<DbSemanticsDatatype> list2 = new ArrayList<DbSemanticsDatatype>();
			DbSemanticsDatatype var2 = new DbSemanticsDatatype();
			
			var2.id = var1.id;
			var2.name = "NA";
			var2.semantics = utils.truncateString(semantics, maxSemanticLineWidth);
			
			list2.add(var2);
			
			if (semantics.length() > 0)
				databaseAPI.insertIntoSemanticsDatatypeTable(list2);

			System.out.println("const " + basicType + " = " + size + ";");
		}
	}

	void parseBasicDataTypes(Node node) {

		System.out.println("// Start Basic Data Constants");

		Node nodeChild = node.getFirstChild();

		while (nodeChild != null) {

			parseBasicData(nodeChild);

			nodeChild = nodeChild.getNextSibling();
		}

		System.out.println("// End Basic Data Constants");
		System.out.println();
	}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	void parseSimpleData(Node nodeChild) {

		String simpleType = "";
		String representation = "";
		boolean hasData = false;
		String semantics = "";
		
		nodeChild = nodeChild.getFirstChild();

		while (nodeChild != null) {

			String name = nodeChild.getNodeName();
			hasData = true;

			if (name.equals("name"))
				simpleType = nodeChild.getTextContent();

			if (name.equals("representation"))
				representation = nodeChild.getTextContent();
			
			if (name.equals("semantics"))
				semantics = nodeChild.getTextContent();

			nodeChild = nodeChild.getNextSibling();
		}

		if (hasData) {

			List<DbSimpleDatatype> list = new ArrayList<DbSimpleDatatype>();

			DbSimpleDatatype var1 = new DbSimpleDatatype();

			var1.id = UUID.randomUUID().toString();
			var1.name = simpleType;
			var1.type = representation;

			list.add(var1);

			databaseAPI.insertIntoSimpleDatatypeTable(list);

			// Insert semantics into database
			List<DbSemanticsDatatype> list2 = new ArrayList<DbSemanticsDatatype>();
			DbSemanticsDatatype var2 = new DbSemanticsDatatype();
			
			var2.id = var1.id;
			var2.name = "NA";
			var2.semantics = utils.truncateString(semantics, maxSemanticLineWidth);
			
			list2.add(var2);
			
			if (semantics.length() > 0)
				databaseAPI.insertIntoSemanticsDatatypeTable(list2);
			
			System.out.println("typedef " + representation + " " + simpleType + ";");
		}
	}

	void parseSimpleDataTypes(Node node) {

		System.out.println("// Start Simple Data Types");

		Node nodeChild = node.getFirstChild();

		while (nodeChild != null) {

			parseSimpleData(nodeChild);

			nodeChild = nodeChild.getNextSibling();
		}

		System.out.println("// End Simple Data Types");
		System.out.println();
	}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	void parseEnumeratedData(Node nodeChild) {

		String currentParentObject = "";
		String enumeratedType = "";
		String representation = "";
		String semantics = "";
		boolean hasData = false;

		nodeChild = nodeChild.getFirstChild();
		currentParentObject = UUID.randomUUID().toString();
		
		while (nodeChild != null) {

			String name = nodeChild.getNodeName();
			hasData = true;

			if (name.equals("name"))
				enumeratedType = nodeChild.getTextContent();

			if (name.equals("representation")) {
				representation = nodeChild.getTextContent();
				System.out.println("enum " + enumeratedType + " : " + representation + ";");
			}
			
			if (name.equals("semantics"))
				semantics = nodeChild.getTextContent();
			
			if (name.equals("enumerator"))
				parseEnumeratorData(nodeChild, currentParentObject);

			nodeChild = nodeChild.getNextSibling();
		}

		if (hasData) {

			List<DbEnumeratedDatatype> list = new ArrayList<DbEnumeratedDatatype>();

			DbEnumeratedDatatype var = new DbEnumeratedDatatype();

			var.id = currentParentObject;
			var.name = enumeratedType;
			var.type = representation;
			currentParentObject = var.id;
			
			list.add(var);

			databaseAPI.insertIntoEnumeratedDatatypeTable(list);
			
			// Insert semantics into database
			List<DbSemanticsDatatype> list2 = new ArrayList<DbSemanticsDatatype>();
			DbSemanticsDatatype var2 = new DbSemanticsDatatype();
			
			var2.id = var.id;
			var2.name = "NA";
			var2.semantics = utils.truncateString(semantics, maxSemanticLineWidth);
			
			list2.add(var2);
			
			if (semantics.length() > 0)
				databaseAPI.insertIntoSemanticsDatatypeTable(list2);
		}
	}

	void parseEnumeratedDataTypes(Node node) {

		System.out.println("// Start Enumerated Data Types");

		Node nodeChild = node.getFirstChild();

		while (nodeChild != null) {

			parseEnumeratedData(nodeChild);

			nodeChild = nodeChild.getNextSibling();
		}

		System.out.println("// End Enumerated Data Types");
		System.out.println("");
	}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	void parseEnumeratorData(Node nodeChild, String parentObject) {

		String enumeratorType = "";
		int ordinalValue = 0;
		boolean hasData = false;

		nodeChild = nodeChild.getFirstChild();

		while (nodeChild != null) {

			String name = nodeChild.getNodeName();
			hasData = true;

			if (name.equals("name"))
				enumeratorType = nodeChild.getTextContent();

			if (name.equals("value"))
				ordinalValue = Integer.parseInt(nodeChild.getTextContent());

			nodeChild = nodeChild.getNextSibling();
		}

		if (hasData) {

			List<DbEnumeratorDatatype> list = new ArrayList<DbEnumeratorDatatype>();

			DbEnumeratorDatatype var = new DbEnumeratorDatatype();

			var.id = UUID.randomUUID().toString();
			var.name = enumeratorType;
			var.ordinalValue = ordinalValue;
			var.parentObject = parentObject;
			
			list.add(var);

			databaseAPI.insertIntoEnumeratorDatatypeTable(list);

			System.out.println("   enumerator " + var.name + " " + var.ordinalValue + " " + var.parentObject + ";");
		}
	}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	void parseArrayData(Node nodeChild) {

		String arrayType = "";
		String dataType = "";
		String cardinality = "";
		String encoding = "";
		String semantics = "";
		
		boolean hasData = false;

		nodeChild = nodeChild.getFirstChild();

		while (nodeChild != null) {

			String name = nodeChild.getNodeName();
			hasData = true;

			if (name.equals("name"))
				arrayType = nodeChild.getTextContent();

			if (name.equals("dataType"))
				dataType = nodeChild.getTextContent();

			if (name.equals("cardinality"))
				cardinality = nodeChild.getTextContent();

			if (name.equals("semantics"))
				semantics = nodeChild.getTextContent();
			
			if (name.equals("encoding"))
				encoding = nodeChild.getTextContent();

			nodeChild = nodeChild.getNextSibling();
		}

		if (hasData) {

			List<DbArrayDatatype> list = new ArrayList<DbArrayDatatype>();

			DbArrayDatatype var1 = new DbArrayDatatype();

			var1.id = UUID.randomUUID().toString();
			var1.name = arrayType;
			var1.type = dataType;
			var1.cardinality = cardinality;
			var1.encoding = encoding;

			list.add(var1);

			databaseAPI.insertIntoArrayDatatypeTable(list);

			// Insert semantics into database
			List<DbSemanticsDatatype> list2 = new ArrayList<DbSemanticsDatatype>();
			DbSemanticsDatatype var2 = new DbSemanticsDatatype();
			
			var2.id = var1.id;
			var2.name = "NA";
			var2.semantics = utils.truncateString(semantics, maxSemanticLineWidth);
			
			list2.add(var2);
			
			if (semantics.length() > 0)
				databaseAPI.insertIntoSemanticsDatatypeTable(list2);
			
			System.out.println("typedef " + dataType + " " + arrayType + " " + cardinality + " " + encoding + ";");
		}
	}

	void parseArrayDataTypes(Node node) {

		System.out.println("// Start Array Data Types");

		Node nodeChild = node.getFirstChild();

		while (nodeChild != null) {

			parseArrayData(nodeChild);

			nodeChild = nodeChild.getNextSibling();
		}

		System.out.println("// End Array Data Types");
		System.out.println();
	}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	void parseFixedRecordField(Node node, int index, UUID parentUUID) {

		Node nodeChild = node.getFirstChild();

		String dataType = "";
		String variableName = "";
		String origVariableName = "";
		String encoding = "";
		String primitive = "";
		String semantics = "";
		
		while (nodeChild != null) {

			String name = nodeChild.getNodeName();

			if (name.equals("name")) {
				variableName = utils.convertToCamelCase(nodeChild.getTextContent());
				origVariableName = nodeChild.getTextContent();

				if (variableName.equals("class"))
					variableName = "classValue";
			}

			if (name.equals("dataType")) {
				dataType = nodeChild.getTextContent();
				encoding = utils.getEncodingType(dataType);
				primitive = utils.getClassFromEncodingType(encoding);
			}
			
			if (name.equals("semantics"))
				semantics = nodeChild.getTextContent();

			nodeChild = nodeChild.getNextSibling();
		}

		List<DbFixedRecordField> list = new ArrayList<DbFixedRecordField>();

		DbFixedRecordField var = new DbFixedRecordField();

		var.id = UUID.randomUUID().toString();
		var.index = index;
		var.origName = origVariableName;
		var.name = variableName;
		var.type = dataType;
		var.encoding = encoding;
		var.primitive = primitive;
		var.parentObject = parentUUID.toString();

		list.add(var);

		databaseAPI.insertIntoFixedRecordFieldTable(list);
		
		// Insert semantics into database
		List<DbSemanticsDatatype> list2 = new ArrayList<DbSemanticsDatatype>();
		DbSemanticsDatatype var2 = new DbSemanticsDatatype();
		
		var2.id = var.id;
		var2.name = var.name;
		var2.semantics = utils.truncateString(semantics, maxSemanticLineWidth);
		
		list2.add(var2);
		
		if (semantics.length() > 0)
			databaseAPI.insertIntoSemanticsDatatypeTable(list2);

		System.out.println("   " + dataType + " " + variableName + ";");
	}

	void parseFixedRecordData(Node node) {

		String fixedRecordName = "";
		boolean hasData = false;
		UUID objectUUID = null;
		int fixedRecordFieldIndex = 0;
		String semanticsUUID = "";
		String semantics = "";
		String varName = "";
		
		Node nodeChild = node.getFirstChild();

		while (nodeChild != null) {

			String name = nodeChild.getNodeName();
			hasData = true;

			if (name.equals("name")) {
				fixedRecordName = nodeChild.getTextContent();
				System.out.println("// Start Fixed Record");
				System.out.println("// " + fixedRecordName);
				System.out.println("typedef struct {");

				objectUUID = UUID.randomUUID();
				semanticsUUID = objectUUID.toString();
				
				List<DbFixedRecordDatatype> list = new ArrayList<DbFixedRecordDatatype>();
				DbFixedRecordDatatype var = new DbFixedRecordDatatype();

				var.id = objectUUID.toString();
				var.name = fixedRecordName;
				varName = fixedRecordName;
				
				list.add(var);

				databaseAPI.insertIntoFixedRecordDatatypeTable(list);
			}

			if (name.equals("semantics")) {
				
				semantics = nodeChild.getTextContent();
				
				// Insert semantics into database
				List<DbSemanticsDatatype> list2 = new ArrayList<DbSemanticsDatatype>();
				DbSemanticsDatatype var2 = new DbSemanticsDatatype();
				
				var2.id = semanticsUUID;
				var2.name = varName;
				var2.semantics = utils.truncateString(semantics, maxSemanticLineWidth);
				
				list2.add(var2);
				
				if (semantics.length() > 0)
					databaseAPI.insertIntoSemanticsDatatypeTable(list2);
			}

			if (name.equals("field")) {
				fixedRecordFieldIndex++;
				parseFixedRecordField(nodeChild, fixedRecordFieldIndex, objectUUID);
			}

			nodeChild = nodeChild.getNextSibling();
		}

		if (hasData) {
			System.out.println("} " + fixedRecordName + ";");
			System.out.println("// End Fixed Record");
			System.out.println();
		}
	}

	void parseFixedRecordDataTypes(Node node) {

		Node nodeChild = node.getFirstChild();

		while (nodeChild != null) {

			parseFixedRecordData(nodeChild);

			nodeChild = nodeChild.getNextSibling();
		}
	}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	void parseDiscriminant(String dataType, String origDiscriminantName, String discriminantName, int index,
			UUID parentUUID) {

		List<DbVariantRecordField> list = new ArrayList<DbVariantRecordField>();

		DbVariantRecordField var = new DbVariantRecordField();

		var.id = UUID.randomUUID().toString();
		var.index = index;
		var.origName = origDiscriminantName;
		var.name = discriminantName;
		var.type = dataType;
		var.discriminant = true;
		var.alternative = false;
		var.parentObject = parentUUID.toString();

		list.add(var);

		databaseAPI.insertIntoVariantRecordFieldTable(list);
	}

	void parseAlternative(Node node, int index, UUID parentUUID) {

		Node nodeChild = node.getFirstChild();

		String dataType = "";
		String variableName = "";
		String semantics = "";
		
		while (nodeChild != null) {

			String name = nodeChild.getNodeName();

			if (name.equals("name"))
				variableName = utils.convertToCamelCase(nodeChild.getTextContent());

			if (name.equals("dataType"))
				dataType = nodeChild.getTextContent();
			
			if (name.equals("semantics"))
				semantics = nodeChild.getTextContent();

			nodeChild = nodeChild.getNextSibling();
		}

		List<DbVariantRecordField> list = new ArrayList<DbVariantRecordField>();

		DbVariantRecordField var = new DbVariantRecordField();

		var.id = UUID.randomUUID().toString();
		var.index = index;
		var.name = variableName;
		var.type = dataType;
		var.discriminant = false;
		var.alternative = true;
		var.parentObject = parentUUID.toString();

		list.add(var);

		databaseAPI.insertIntoVariantRecordFieldTable(list);
		
		// Insert semantics into database
		List<DbSemanticsDatatype> list2 = new ArrayList<DbSemanticsDatatype>();
		DbSemanticsDatatype var2 = new DbSemanticsDatatype();
		
		var2.id = var.id;
		var2.name = var.name;
		var2.semantics = utils.truncateString(semantics, maxSemanticLineWidth);
		
		list2.add(var2);
		
		if (semantics.length() > 0)
			databaseAPI.insertIntoSemanticsDatatypeTable(list2);

		System.out.println("   " + dataType + " " + variableName + "; // Alternative");
	}

	void parseVariantRecordData(Node node) {

		String variantRecordName = "";
		String origDiscriminantName = "";
		String discriminantName = "";
		String dataType = "";
		boolean hasData = false;
		UUID objectUUID = null;
		int variantRecordFieldIndex = 0;
		String semanticsUUID = "";
		
		Node nodeChild = node.getFirstChild();

		while (nodeChild != null) {

			String name = nodeChild.getNodeName();
			hasData = true;

			if (name.equals("name")) {
				variantRecordName = nodeChild.getTextContent();
				System.out.println("// Start Variant Record");
				System.out.println("// " + variantRecordName);
				System.out.println("typedef struct {");

				objectUUID = UUID.randomUUID();
				semanticsUUID = objectUUID.toString();
				
				List<DbVariantRecordDatatype> list = new ArrayList<DbVariantRecordDatatype>();

				DbVariantRecordDatatype var = new DbVariantRecordDatatype();

				var.id = objectUUID.toString();
				var.name = variantRecordName;

				list.add(var);

				databaseAPI.insertIntoVariantRecordDatatypeTable(list);
			}

			if (name.equals("discriminant")) {
				origDiscriminantName = nodeChild.getTextContent();
				discriminantName = utils.convertToCamelCase(nodeChild.getTextContent());
			}

			if (name.equals("dataType")) {
				variantRecordFieldIndex++;
				dataType = nodeChild.getTextContent();
				parseDiscriminant(dataType, origDiscriminantName, discriminantName, variantRecordFieldIndex,
						objectUUID);
				System.out.println("   " + dataType + " " + discriminantName + "; // Discriminant");
			}

			if (name.equals("alternative")) {
				variantRecordFieldIndex++;
				parseAlternative(nodeChild, variantRecordFieldIndex, objectUUID);
			}
			
			if (name.equals("semantics")) {
				
				// Insert semantics into database
				List<DbSemanticsDatatype> list = new ArrayList<DbSemanticsDatatype>();
				DbSemanticsDatatype var = new DbSemanticsDatatype();
				
				var.id = semanticsUUID;
				var.name = "NA";
				var.semantics = utils.truncateString(nodeChild.getTextContent(), maxSemanticLineWidth);
				
				list.add(var);
				
				if (var.semantics.length() > 0)
					databaseAPI.insertIntoSemanticsDatatypeTable(list);
			}

			nodeChild = nodeChild.getNextSibling();
		}

		if (hasData) {
			System.out.println("} " + variantRecordName + ";");
			System.out.println("// End Variant Record");
			System.out.println("");
		}
	}

	void parseVariantRecordDataTypes(Node node) {

		Node nodeChild = node.getFirstChild();

		while (nodeChild != null) {

			parseVariantRecordData(nodeChild);

			nodeChild = nodeChild.getNextSibling();
		}
	}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	void parseDataTypes(Node node) {

		Node nodeChild = node.getFirstChild();

		while (nodeChild != null) {

			String name = nodeChild.getNodeName();

			if (name.equals("simpleDataTypes"))
				parseSimpleDataTypes(nodeChild);

			if (name.equals("enumeratedDataTypes"))
				parseEnumeratedDataTypes(nodeChild);

			if (name.equals("arrayDataTypes"))
				parseArrayDataTypes(nodeChild);

			if (name.equals("fixedRecordDataTypes"))
				parseFixedRecordDataTypes(nodeChild);

			if (name.equals("variantRecordDataTypes"))
				parseVariantRecordDataTypes(nodeChild);

			if (name.equals("basicDataRepresentations"))
				parseBasicDataTypes(nodeChild);

			nodeChild = nodeChild.getNextSibling();
		}
	}
	
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void generateDatabase(String fomFilename, String elementModel, 
			String postfixString, boolean processVariantOrdering)
	{
		HlaPathBuilder hlaPathBuilder = new HlaPathBuilder();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		BuildElementPaths buildElementPaths = new BuildElementPaths();
		MMGenerator mmGenerator = new MMGenerator();
		
		try {
			
			Path myDbPath = Paths.get(System.getProperty("user.dir") + File.separator + "myDB");
			boolean dbExist = Files.exists(myDbPath);

			databaseAPI.initDatabase();	

			if (useMemoryDb || (!useMemoryDb && !dbExist)) {
				
				PrintStream outputStream = new PrintStream(new File("TypeDefs.h"));
				PrintStream console = System.out;
				System.setOut(outputStream);
				
				databaseAPI.createTables();
				
				if (processVariantOrdering) {
					// Parse the variant orderings file and import into table
					// This is used by hla-codegen1516e-encoding.
					VariantOrdering variantOrdering = new VariantOrdering();
					variantOrdering.populateTable();
				}
				
				// First pass for dataTypes only
				DocumentBuilder db1 = dbf.newDocumentBuilder();
				Document doc1 = db1.parse(new File(fomFilename));

				Node node = doc1.getFirstChild();
				Node nodeChild = node.getFirstChild();

				while (nodeChild != null) {

					String name = nodeChild.getNodeName();

					if (name.equals("dataTypes"))
						parseDataTypes(nodeChild);

					nodeChild = nodeChild.getNextSibling();
				}

				// Second pass for FOM support dataTypes only
				DocumentBuilder db2 = dbf.newDocumentBuilder();
				Document doc2 = db2.parse(new File(fomSupportTypes));

				node = doc2.getFirstChild();
				nodeChild = node.getFirstChild();

				while (nodeChild != null) {

					String name = nodeChild.getNodeName();

					if (name.equals("dataTypes"))
						parseDataTypes(nodeChild);

					nodeChild = nodeChild.getNextSibling();
				}

				// Third pass for objects and interactions
				DocumentBuilder db3 = dbf.newDocumentBuilder();
				Document doc3 = db3.parse(new File(fomFilename));

				node = doc3.getFirstChild();
				nodeChild = node.getFirstChild();

				while (nodeChild != null) {

					String name = nodeChild.getNodeName();

					if (name.equals("objects"))
						parseObjects(nodeChild);

					if (name.equals("interactions"))
						parseInteractions(nodeChild);

					nodeChild = nodeChild.getNextSibling();
				}
				
				System.setOut(console);
				outputStream.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		// Fourth pass to load element models
		try {
			DocumentBuilder db4 = dbf.newDocumentBuilder();
			Document doc4 = db4.parse(new File(elementModel));

			Node node = doc4.getFirstChild();
			Node nodeChild = node.getFirstChild();

			while (nodeChild != null) {

				String name = nodeChild.getNodeName();

				if (name.equals("objects")) {
					NodeList nodes = nodeChild.getChildNodes();
					for (int i = 0; i < nodes.getLength(); i++) {
						String textContent = nodes.item(i).getTextContent();
						if (!textContent.isBlank())
							elementObjectList.add(textContent);
					}
				}

				if (name.equals("interactions")) {
					NodeList nodes = nodeChild.getChildNodes();
					for (int i = 0; i < nodes.getLength(); i++) {
						String textContent = nodes.item(i).getTextContent();
						if (!textContent.isBlank())
							elementInteractionList.add(textContent);
					}
				}

				nodeChild = nodeChild.getNextSibling();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Remove and then recreate protocol specification, object and interaction
		// directories
		try {
			File specDir = new File(System.getProperty("user.dir") + File.separator + protocolSpecDir);
			File objDir = new File(
					System.getProperty("user.dir") + File.separator + protocolSpecDir + File.separator + "Objects");
			File intDir = new File(System.getProperty("user.dir") + File.separator + protocolSpecDir + File.separator
					+ "Interactions");

			if (Files.exists(specDir.toPath()))
				FileUtils.forceDelete(specDir);

			FileUtils.forceMkdir(specDir);
			FileUtils.forceMkdir(objDir);
			FileUtils.forceMkdir(intDir);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Set database API
		buildElementPaths.setDatabase(databaseAPI);
		
		// Process all objects defined in elementObjectList
		try {

			for (String elementObject : elementObjectList) {

					String selectStatement = "SELECT * FROM Object WHERE name='" + elementObject + "'";

					List<DbObject> list = databaseAPI.selectFromObjectTable(selectStatement);

					for (DbObject var : list) {

						byte[] pathBytes = var.path.getBytes();
						String cityHashHex = CityHash.cityHash64Hex(pathBytes, 0, pathBytes.length);
						MMNodeTreeRepository.getInstance().addObjectName(var.name + "_" + cityHashHex + "_" + postfixString);
						
						PrintStream outputStream = new PrintStream(new File(System.getProperty("user.dir") + File.separator
								+ protocolSpecDir + File.separator + "Objects" + File.separator + var.name + "_" + cityHashHex + "_" + postfixString + ".txt"));
						PrintStream console = System.out;
						System.setOut(outputStream);
						
						System.out.println("<info>");
						System.out.println("id = " + var.id);
						System.out.println("name = " + var.name + "_" + cityHashHex + "_" + postfixString);
						System.out.println("path = " + var.path);
						if (uuidMarkupOutput)
							System.out.println("debugPath = " + var.debugPath);
						System.out.println("parentObject = " + var.parentObject);			
						System.out.println("</info>");
						
						buildElementPaths.resetState();
						buildElementPaths.startTraversal(Element.Object, var.id);
						
						System.out.println("\n");
						System.setOut(console);
						
						mmGenerator.resetState();
						mmGenerator.setDatabase(databaseAPI);
						mmGenerator.generateFromFile(var.name + "_" + cityHashHex + "_" + postfixString + ".txt", Element.Object);
					}
				}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Process all interactions defined in elementInteractionList
		try {

			for (String elementInteraction : elementInteractionList) {

					String selectStatement = "SELECT * FROM Interaction WHERE name='" + elementInteraction + "'";

					List<DbInteraction> list = databaseAPI.selectFromInteractionTable(selectStatement);

					for (DbInteraction var : list) {

						byte[] pathBytes = var.path.getBytes();
						String cityHashHex = CityHash.cityHash64Hex(pathBytes, 0, pathBytes.length);
						MMNodeTreeRepository.getInstance().addInteractionName(var.name + "_" + cityHashHex + "_" + postfixString);
						
						PrintStream outputStream = new PrintStream(new File(System.getProperty("user.dir") + File.separator
								+ protocolSpecDir + File.separator + "Interactions" + File.separator + var.name + "_" + cityHashHex + "_" + postfixString + ".txt"));
						PrintStream console = System.out;
						System.setOut(outputStream);
						
						System.out.println("<info>");
						System.out.println("id = " + var.id);
						System.out.println("name = " + var.name + "_" + cityHashHex + "_" + postfixString);
						System.out.println("path = " + var.path);
						if (uuidMarkupOutput)
							System.out.println("debugPath = " + var.debugPath);
						System.out.println("parentObject = " + var.parentObject);			
						System.out.println("</info>");
						
						buildElementPaths.resetState();
						buildElementPaths.startTraversal(Element.Interaction, var.id);
						
						System.out.println("\n");
						System.setOut(console);
						
						mmGenerator.resetState();
						mmGenerator.setDatabase(databaseAPI);
						mmGenerator.generateFromFile(var.name + "_" + cityHashHex + "_" + postfixString + ".txt", Element.Interaction);
					}
				}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
