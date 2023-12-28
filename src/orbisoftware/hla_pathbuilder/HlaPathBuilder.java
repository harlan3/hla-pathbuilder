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
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import orbisoftware.hla_pathbuilder.Constants.Element;
import orbisoftware.hla_pathbuilder.db_classes.DbArrayDatatype;
import orbisoftware.hla_pathbuilder.db_classes.DbAttribute;
import orbisoftware.hla_pathbuilder.db_classes.DbEnumeratedDatatype;
import orbisoftware.hla_pathbuilder.db_classes.DbFixedRecordDatatype;
import orbisoftware.hla_pathbuilder.db_classes.DbFixedRecordField;
import orbisoftware.hla_pathbuilder.db_classes.DbInteraction;
import orbisoftware.hla_pathbuilder.db_classes.DbObject;
import orbisoftware.hla_pathbuilder.db_classes.DbParameter;
import orbisoftware.hla_pathbuilder.db_classes.DbSimpleDatatype;
import orbisoftware.hla_pathbuilder.db_classes.DbVariantRecordDatatype;
import orbisoftware.hla_pathbuilder.db_classes.DbVariantRecordField;

public class HlaPathBuilder {

	private DatabaseAPI databaseAPI = new DatabaseAPI();
	private static final String fileName = "RestaurantFOMmodule.xml";
	public static List<VariantSelect> variantSelectList = new ArrayList<VariantSelect>();
	public static Stack<String> pathBuilderStack = new Stack<String>();
	public Utils utils = new Utils();

	// If true, all variants and alternatives will be ignored that do not have a
	// VariantSelect created.
	public static boolean useVariantSelect = false;

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	void insertExtendsAttribute(int index, String origVariableName, String variableName, String dataType,
			UUID parentUUID) {

		// Insert attribute into database
		List<DbAttribute> list = new ArrayList<DbAttribute>();
		DbAttribute var = new DbAttribute();

		var.id = UUID.randomUUID().toString();
		var.index = index;
		var.origName = origVariableName;
		var.name = variableName;
		var.type = dataType;
		var.inherited = true;
		var.parentObject = parentUUID.toString();

		list.add(var);

		databaseAPI.insertIntoAttributeTable(list);

	}

	void parseAttribute(Node node, int index, UUID parentUUID) {

		Node nodeChild = node.getFirstChild();

		String dataType = "";
		String variableName = "";
		String origVariableName = "";

		while (nodeChild != null) {

			String name = nodeChild.getNodeName();

			if (name.equals("name")) {
				origVariableName = nodeChild.getTextContent();
				variableName = utils.convertToCamelCase(nodeChild.getTextContent());
			}

			if (name.equals("dataType")) {
				dataType = nodeChild.getTextContent();
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

		System.out.println("   " + dataType + " " + variableName + ";");
	}

	String parseObject(Node node, String parentClass, UUID parentUUID) {

		String objectName = "";
		String typedefName = "";
		UUID objectUUID = null;
		int attributeIndex = 0;

		Node nodeChild = node.getFirstChild();

		while (nodeChild != null) {

			String name = nodeChild.getNodeName();

			if (name.equals("name")) {

				objectName = nodeChild.getTextContent();

				System.out.println("// Start Object");
				System.out.println("// " + objectName);
				System.out.println("typedef struct {");

				typedefName = objectName;
				pathBuilderStack.push(objectName);

				// Insert object into database
				objectUUID = UUID.randomUUID();
				List<DbObject> list = new ArrayList<DbObject>();

				DbObject var = new DbObject();

				var.id = objectUUID.toString();
				var.name = objectName;
				var.path = pathBuilderStack.toString();
				var.parentObject = parentUUID.toString();

				list.add(var);
				databaseAPI.insertIntoObjectTable(list);

				if (!parentClass.isEmpty()) {

					String origVariableName = parentClass;
					String variableName = utils.convertToCamelCase(parentClass);
					attributeIndex++;

					System.out.println("   " + parentClass + " " + variableName + "; // extends");
					insertExtendsAttribute(attributeIndex, origVariableName, variableName, parentClass, objectUUID);

				}
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
			}

			nodeChild = nodeChild.getNextSibling();
		}

		if (parentClass.isEmpty()) {
			System.out.println("} " + typedefName + ";");
			System.out.println("// End Object" + "\n\n");
			pathBuilderStack.pop();
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

	void insertExtendsParameter(int index, String origVariableName, String variableName, String dataType,
			UUID parentUUID) {

		// Insert parameter into database
		List<DbParameter> list = new ArrayList<DbParameter>();

		DbParameter var = new DbParameter();

		var.id = UUID.randomUUID().toString();
		var.index = index;
		var.origName = origVariableName;
		var.name = variableName;
		var.type = dataType;
		var.inherited = true;
		var.parentObject = parentUUID.toString();

		list.add(var);

		databaseAPI.insertIntoParameterTable(list);
	}

	void parseParameter(Node node, int index, UUID parentUUID) {

		Node nodeChild = node.getFirstChild();

		String dataType = "";
		String variableName = "";
		String origVariableName = "";

		while (nodeChild != null) {

			String name = nodeChild.getNodeName();

			if (name.equals("name")) {
				origVariableName = nodeChild.getTextContent();
				variableName = utils.convertToCamelCase(nodeChild.getTextContent());
			}

			if (name.equals("dataType"))
				dataType = nodeChild.getTextContent();

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

		System.out.println("   " + dataType + " " + variableName + ";");
	}

	String parseInteraction(Node node, String parentClass, UUID parentUUID) {

		String interactionName = "";
		String typedefName = "";
		UUID interactionUUID = null;
		int parameterIndex = 0;

		Node nodeChild = node.getFirstChild();

		while (nodeChild != null) {

			String name = nodeChild.getNodeName();

			if (name.equals("name")) {

				interactionName = nodeChild.getTextContent();
				System.out.println("// Start Interaction");
				System.out.println("// " + interactionName);
				System.out.println("typedef struct {");

				typedefName = interactionName;
				pathBuilderStack.push(interactionName);

				// Insert interaction into database
				interactionUUID = UUID.randomUUID();
				List<DbInteraction> list = new ArrayList<DbInteraction>();

				DbInteraction var = new DbInteraction();

				var.id = interactionUUID.toString();
				var.name = interactionName;
				var.path = pathBuilderStack.toString();
				var.parentObject = parentUUID.toString();

				list.add(var);
				databaseAPI.insertIntoInteractionTable(list);

				// print out the parent object that is extended by this struct
				if (!parentClass.isEmpty()) {

					String origVariableName = parentClass;
					String variableName = utils.convertToCamelCase(parentClass);
					parameterIndex++;

					System.out.println("   " + parentClass + " " + variableName + "; // extends");
					insertExtendsParameter(parameterIndex, origVariableName, variableName, parentClass,
							interactionUUID);
				}
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
			}

			nodeChild = nodeChild.getNextSibling();
		}

		if (parentClass.isEmpty()) {
			System.out.println("} " + typedefName + ";");
			System.out.println("// End Interaction");
			System.out.println();
			pathBuilderStack.pop();
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

	void parseSimpleData(Node nodeChild) {

		String simpleType = "";
		String representation = "";
		boolean hasData = false;

		nodeChild = nodeChild.getFirstChild();

		while (nodeChild != null) {

			String name = nodeChild.getNodeName();
			hasData = true;

			if (name.equals("name"))
				simpleType = nodeChild.getTextContent();

			if (name.equals("representation"))
				representation = nodeChild.getTextContent();

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

		String enumeratedType = "";
		String representation = "";
		boolean hasData = false;

		nodeChild = nodeChild.getFirstChild();

		while (nodeChild != null) {

			String name = nodeChild.getNodeName();
			hasData = true;

			if (name.equals("name"))
				enumeratedType = nodeChild.getTextContent();

			if (name.equals("representation"))
				representation = nodeChild.getTextContent();

			nodeChild = nodeChild.getNextSibling();
		}

		if (hasData) {

			List<DbEnumeratedDatatype> list = new ArrayList<DbEnumeratedDatatype>();

			DbEnumeratedDatatype var = new DbEnumeratedDatatype();

			var.id = UUID.randomUUID().toString();
			var.name = enumeratedType;
			var.type = representation;

			list.add(var);

			databaseAPI.insertIntoEnumeratedDatatypeTable(list);

			System.out.println("typedef " + representation + " " + enumeratedType + ";");
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

	void parseArrayData(Node nodeChild) {

		String arrayType = "";
		String dataType = "";
		String cardinality = "";
		String encoding = "";
		
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
				primitive = utils.getPrimitiveFromEncodingType(encoding);
			}

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

		System.out.println("   " + dataType + " " + variableName + ";");
	}

	void parseFixedRecordData(Node node) {

		String fixedRecordName = "";
		boolean hasData = false;
		UUID objectUUID = null;
		int fixedRecordFieldIndex = 0;

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

				List<DbFixedRecordDatatype> list = new ArrayList<DbFixedRecordDatatype>();
				DbFixedRecordDatatype var = new DbFixedRecordDatatype();

				var.id = objectUUID.toString();
				var.name = fixedRecordName;

				list.add(var);

				databaseAPI.insertIntoFixedRecordDatatypeTable(list);
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

		while (nodeChild != null) {

			String name = nodeChild.getNodeName();

			if (name.equals("name"))
				variableName = utils.convertToCamelCase(nodeChild.getTextContent());

			if (name.equals("dataType"))
				dataType = nodeChild.getTextContent();

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

			nodeChild = nodeChild.getNextSibling();
		}
	}

	public static void main(String[] args) {

		HlaPathBuilder hlaPathBuilder = new HlaPathBuilder();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		BuildElementPaths buildElementPaths = new BuildElementPaths();

		try {

			// hlaPathBuilder.databaseAPI.initDatabase();

			PrintStream outputStream = new PrintStream(new File("TypeDefs.h"));
			PrintStream console = System.out;

			System.setOut(outputStream);

			hlaPathBuilder.databaseAPI.initDatabase();
			hlaPathBuilder.databaseAPI.createTables();

			// First pass for dataTypes only
			DocumentBuilder db1 = dbf.newDocumentBuilder();
			Document doc1 = db1.parse(new File(fileName));

			Node node = doc1.getFirstChild();
			Node nodeChild = node.getFirstChild();

			while (nodeChild != null) {

				String name = nodeChild.getNodeName();

				if (name.equals("dataTypes"))
					hlaPathBuilder.parseDataTypes(nodeChild);

				nodeChild = nodeChild.getNextSibling();
			}

			// Second pass for objects and interactions
			DocumentBuilder db2 = dbf.newDocumentBuilder();
			Document doc2 = db2.parse(new File(fileName));

			node = doc2.getFirstChild();
			nodeChild = node.getFirstChild();

			while (nodeChild != null) {

				String name = nodeChild.getNodeName();

				if (name.equals("objects"))
					hlaPathBuilder.parseObjects(nodeChild);

				if (name.equals("interactions"))
					hlaPathBuilder.parseInteractions(nodeChild);

				nodeChild = nodeChild.getNextSibling();
			}

			System.setOut(console);
			outputStream.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			PrintStream outputStream = new PrintStream(new File("PathDefs.txt"));
			PrintStream console = System.out;

			System.setOut(outputStream);

			buildElementPaths.setDatabase(hlaPathBuilder.databaseAPI);


/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			// Set up a variant select (all other variants and alternatives will be
			// ignored).

			String uuidVariantRecord = hlaPathBuilder.databaseAPI
					.getUUIDForVariantRecord(new SearchToken(DatabaseAPI.NULL_UUID, "", "WaiterValue"));
			HlaPathBuilder.variantSelectList.add(new VariantSelect(uuidVariantRecord, "Rating"));

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			// Select from Object table - "Waiter"

			{
				String selectStatement = "SELECT * FROM Object WHERE name='Waiter'";

				List<DbObject> list = hlaPathBuilder.databaseAPI.selectFromObjectTable(selectStatement);

				for (DbObject var : list) {

					System.out.println("id = " + var.id);
					System.out.println("name = " + var.name);
					System.out.println("path = " + var.path);
					System.out.println("parentObject = " + var.parentObject);
					System.out.println();

					buildElementPaths.startTraversal(Element.OBJECT, var.id);
				}
			}

			System.out.println("\n");


/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			// Select from Interaction table - "MainCourseServed"

			{
				String selectStatement = "SELECT * FROM Interaction WHERE name='MainCourseServed'";

				List<DbObject> list = hlaPathBuilder.databaseAPI.selectFromObjectTable(selectStatement);

				for (DbObject var : list) {

					System.out.println("id = " + var.id);
					System.out.println("name = " + var.name);
					System.out.println("path = " + var.path);
					System.out.println("parentObject = " + var.parentObject);
					System.out.println();

					buildElementPaths.startTraversal(Element.INTERACTION, var.id);
				}
			}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			
			System.out.println("\n");
			System.setOut(console);
			outputStream.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
