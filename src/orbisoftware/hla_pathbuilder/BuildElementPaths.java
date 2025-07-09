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

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import orbisoftware.hla_pathbuilder.db_classes.*;

public class BuildElementPaths {

	private Stack<String> pathFollowStack = new Stack<String>();
	private ArrayList<String> attrParams = new ArrayList<String>();
	private DatabaseAPI databaseAPI;
	
	private String[] rootElementPathArray;
	private int currentRootPathIndex = 0;
	private int currentPathDef = 0;
	
	private String currentPath = "";
	
	private static String rollingKeyHash;
	
	BuildElementPaths() {
		
	}
	
	public void resetState() {
		
		pathFollowStack.clear();
		attrParams.clear();
		rootElementPathArray = new String[0];
		currentRootPathIndex = 0;
		currentPathDef = 0;
		currentPath = "";
		rollingKeyHash = "";
	}
	
	private String getObjectPath(String elementUUID) {
		
    	String selectStatement = "SELECT * FROM Object WHERE id = '" + elementUUID + "'";
    	List<DbObject> objList = databaseAPI.selectFromObjectTable(selectStatement);
    	String objPath = null;
    	
		if (objList.size() >= 1)
			objPath = objList.get(0).path;
		
		return objPath;	
	}
	
	private String getInteractionPath(String elementUUID) {
		
    	String selectStatement = "SELECT * FROM Interaction WHERE id = '" + elementUUID + "'";
    	List<DbInteraction> interactionList = databaseAPI.selectFromInteractionTable(selectStatement);
    	String interactionPath = null;
    	
		if (interactionList.size() >= 1)
			interactionPath = interactionList.get(0).path;
		
		return interactionPath;	
	}
	
	private void insertPath(int index) {

		// Add element to the attributes/parameters list
		String[] split = pathFollowStack.get(index).split("\\s+");
		
		if (attrParams.size() > 0) {
			if (!split[1].equals(attrParams.get(attrParams.size()-1)))
				attrParams.add(split[1]);
		} else
			attrParams.add(split[1]);
		
		System.out.print("[");
		
		for (int i = index; i < pathFollowStack.size()-1; i++) {
			System.out.print(pathFollowStack.get(i));
			if (i != (pathFollowStack.size() - 2))
				System.out.print(", ");
		}

		System.out.println("]");
	}
	
	private void displayPartialPath() {
		
		boolean foundMatch = false;
		int foundIndex = -1;
		int elementCount = 0;
		
		String matchItem = rootElementPathArray[currentRootPathIndex];
		
		for (String pathElement : pathFollowStack) {
			
			String cleanPathElement = pathElement.replaceAll(".*?\\((.*?)\\).*","$1");
			
			if(cleanPathElement.equals(matchItem)) {
				
				foundMatch = true;
				foundIndex = elementCount + 1;
				break;
			}
			
			elementCount++;
		}

		if (foundMatch)
			insertPath(foundIndex);
		else if (currentRootPathIndex == rootElementPathArray.length - 1)
			insertPath(0);
	}
	
	private void displayElementPathTransition(String poppedElement) {
		
		String cleanElement = poppedElement.replaceAll(".*?\\((.*?)\\).*","$1");
		boolean foundMatch = false;
		int foundIndex = -1;
		int elementCount = 0;
		
		for (String pathElement : rootElementPathArray) {
			
			if(pathElement.equals(cleanElement)) {
				
				foundMatch = true;
				foundIndex = elementCount + 2;
				break;
			}
			
			elementCount++;
		}
		
		if (foundMatch) {
			
			if (currentPathDef > 0)
				System.out.println("</pathDef" + currentPathDef + ">");
			currentPathDef++;
			System.out.println("\n<pathDef" + currentPathDef + ">");
			
			currentPath = "Path: [";
			
			for (int i=0; i<foundIndex; i++) {
				currentPath += rootElementPathArray[i];
				if (i != (foundIndex-1))
					currentPath += (", ");
			}
			
			currentRootPathIndex = foundIndex - 1;
			
			currentPath += "]";
			
			System.out.println(currentPath);
		}
	}
	
	private void traverseObject(String elementUUID) {
		
		String selectStatement = "SELECT * FROM Attribute WHERE parentObject = '" + elementUUID + "'";
    	List<DbAttribute> list = databaseAPI.selectFromAttributeTable(selectStatement);
    	
    	List<SearchToken> uuidRefList = new ArrayList<SearchToken>();
    	
    	for (DbAttribute var : list) {
    		
    		/*
    		System.out.println("id = " + var.id);
    		System.out.println("index = " + var.index);
    		System.out.println("origName = " + var.origName);
    		System.out.println("name = " + var.name);
    		System.out.println("type = " + var.type);
    		System.out.println("inherited = " + var.inherited);
    		System.out.println("lineNum = " + var.lineNum);
    		System.out.println("parentObject = " + var.parentObject);
    		System.out.println();
    		*/
    		
    		SearchResults searchResults = databaseAPI.deepSearchForUUID(new SearchToken(Constants.NULL_UUID, var.lineNum, Constants.TID.None, var.origName, var.type));
    		uuidRefList.add(new SearchToken(searchResults.uuid, var.lineNum, searchResults.tid, var.origName, var.type));
    	}
    	
		traverseGeneric(uuidRefList);
	}
	
	private void traverseInteraction(String elementUUID) {
		
    	String selectStatement = "SELECT * FROM Parameter where parentObject = '" + elementUUID + "'";
    	
    	List<DbParameter> list = databaseAPI.selectFromParameterTable(selectStatement);
    	List<SearchToken> uuidRefList = new ArrayList<SearchToken>();
    	
    	for (DbParameter var : list) {
    		
    		/*
    		System.out.println("id = " + var.id);
    		System.out.println("index = " + var.index);
    		System.out.println("origName = " + var.origName);
    		System.out.println("name = " + var.name);
    		System.out.println("type = " + var.type);
    		System.out.println("inherited = " + var.inherited);
    		System.out.println("parentObject = " + var.parentObject);
    		System.out.println();
    		*/
    		
    		SearchResults searchResults = databaseAPI.deepSearchForUUID(new SearchToken(Constants.NULL_UUID, var.lineNum, Constants.TID.None, var.origName, var.type));
    		uuidRefList.add(new SearchToken(searchResults.uuid, var.lineNum, searchResults.tid, var.origName, var.type));
    	}
    	
		traverseGeneric(uuidRefList);
	}
	
	private void traverseFixedRecord(String elementUUID) {
		
    	String selectStatement = "SELECT * FROM FixedRecordField where parentObject = '" + elementUUID + "'";
    	
    	List<DbFixedRecordField> list = databaseAPI.selectFromFixedRecordFieldTable(selectStatement);
    	List<SearchToken> uuidRefList = new ArrayList<SearchToken>();
    	
    	for (DbFixedRecordField var : list) {
    		
    		/*
    		System.out.println("id = " + var.id);
    		System.out.println("index = " + var.index);
    		System.out.println("origName = " + var.origName);
    		System.out.println("name = " + var.name);
    		System.out.println("type = " + var.type);
    		System.out.println("encoding = " + var.encoding);
    		System.out.println("primitive = " + var.primitive);
    		System.out.println("lineNum = " + var.lineNum);
    		System.out.println("parentObject = " + var.parentObject);
    		System.out.println();
			*/
    		
    		SearchResults searchResults = databaseAPI.deepSearchForUUID(new SearchToken(Constants.NULL_UUID, var.lineNum, Constants.TID.None, var.name, var.type));
    		uuidRefList.add(new SearchToken(searchResults.uuid, var.lineNum, searchResults.tid, var.name, var.type));
    	}
    	
		traverseGeneric(uuidRefList);
	}
	
	private void traverseVariantRecord(String elementUUID) {
		
    	String selectStatement = "SELECT * FROM VariantRecordField where parentObject = '" + elementUUID + "'";
    	
    	List<DbVariantRecordField> list = databaseAPI.selectFromVariantRecordFieldTable(selectStatement);
    	List<SearchToken> uuidRefList = new ArrayList<SearchToken>();
    	
    	for (DbVariantRecordField var : list) {
    		
    		// Output variant containing the discriminant and one alternative path.
    		//boolean useVariantRecord = (index < 2);
    		
    		boolean useVariantRecord = true;
    		
    		/*
    		System.out.println("id = " + var.id);
    		System.out.println("index = " + var.index);
    		System.out.println("origName = " + var.origName);
    		System.out.println("name = " + var.name);
    		System.out.println("type = " + var.type);
    		System.out.println("discriminant = " + var.discriminant);
    		System.out.println("alternative = " + var.alternative);
    		System.out.println("lineNum = " + var.lineNum);
    		System.out.println("parentObject = " + var.parentObject);
    		System.out.println();
			*/
    		
    		if (useVariantRecord) {

    			SearchResults searchResults = databaseAPI.deepSearchForUUID(new SearchToken(Constants.NULL_UUID, var.lineNum, Constants.TID.None, var.name, var.type));
	    		uuidRefList.add(new SearchToken(searchResults.uuid, var.lineNum, searchResults.tid, var.name, var.type));
    		}
    	}
    	
    	traverseGeneric(uuidRefList);
	}
	
	private void traverseArrayRecord(SearchToken searchToken) {
		
    	String selectStatement = "SELECT * FROM ArrayDatatype where name = '" + searchToken.type + "'";
    	
    	List<DbArrayDatatype> list = databaseAPI.selectFromArrayDatatypeTable(selectStatement);
    	List<SearchToken> uuidRefList = new ArrayList<SearchToken>();

		for (DbArrayDatatype var : list) {

			/*
			System.out.println("id = " + var.id);
			System.out.println("name = " + var.name);
			System.out.println("type = " + var.type);
			System.out.println("cardinality = " + var.cardinality);
			System.out.println("encoding = " + var.encoding);
			System.out.println();
			*/
			
			SearchResults searchResults = databaseAPI.deepSearchForUUID(new SearchToken(Constants.NULL_UUID, var.lineNum, Constants.TID.None, var.name, var.type));
			uuidRefList.add(new SearchToken(searchResults.uuid, var.lineNum, searchResults.tid, var.name, var.type));
		}

		traverseGeneric(uuidRefList);
	}
	
	
	private void traverseSimpleRecord(SearchToken searchToken) {
		
    	String selectStatement = "SELECT * FROM SimpleDatatype where name = '" + searchToken.type + "'";
    	
    	List<DbSimpleDatatype> list = databaseAPI.selectFromSimpleDatatypeTable(selectStatement);
    	List<SearchToken> uuidRefList = new ArrayList<SearchToken>();

		for (DbSimpleDatatype var : list) {

			/*
			System.out.println("id = " + var.id);
			System.out.println("name = " + var.name);
			System.out.println("type = " + var.type);
			System.out.println("lineNum = " + var.lineNum);
			System.out.println();
			*/
			
			SearchResults searchResults = databaseAPI.deepSearchForUUID(new SearchToken(Constants.NULL_UUID, var.lineNum, Constants.TID.None, var.name, var.type));
			uuidRefList.add(new SearchToken(searchResults.uuid, var.lineNum, searchResults.tid, var.name, var.type));
		}

		traverseGeneric(uuidRefList);
	}
	
	private void traverseEnumeratedRecord(SearchToken searchToken) {
		
    	String selectStatement = "SELECT * FROM EnumeratedDatatype where name = '" + searchToken.type + "'";
    	
    	List<DbEnumeratedDatatype> list = databaseAPI.selectFromEnumeratedDatatypeTable(selectStatement);
    	List<SearchToken> uuidRefList = new ArrayList<SearchToken>();

		for (DbEnumeratedDatatype var : list) {

			/*
			System.out.println("id = " + var.id);
			System.out.println("name = " + var.name);
			System.out.println("type = " + var.type);
			System.out.println("lineNum = " + var.lineNum);
			System.out.println();
			*/
			
			SearchResults searchResults = databaseAPI.deepSearchForUUID(new SearchToken(Constants.NULL_UUID, var.lineNum, Constants.TID.None, var.name, var.type));
			uuidRefList.add(new SearchToken(searchResults.uuid, var.lineNum, searchResults.tid, var.name, var.type));
		}

		traverseGeneric(uuidRefList);
	}
	
	private void traverseBasicRecord(SearchToken searchToken) {
		
    	String selectStatement = "SELECT * FROM BasicDatatype where name = '" + searchToken.type + "'";
    	
    	List<DbBasicDatatype> list = databaseAPI.selectFromBasicDatatypeTable(selectStatement);
    	List<SearchToken> uuidRefList = new ArrayList<SearchToken>();

		for (DbBasicDatatype var : list) {

			/*
			System.out.println("id = " + var.id);
			System.out.println("name = " + var.name);
			System.out.println("type = " + var.type);
			System.out.println("size = " + var.size);
			System.out.println("endian = " + var.endian);
			System.out.println("lineNum = " + var.lineNum);
			System.out.println();
			*/
			
			SearchResults searchResults = databaseAPI.deepSearchForUUID(new SearchToken(Constants.NULL_UUID, var.lineNum, Constants.TID.None, var.name, var.type));
			uuidRefList.add(new SearchToken(searchResults.uuid, var.lineNum, searchResults.tid, var.name, var.type));
		}

		traverseGeneric(uuidRefList);
	}
	
	private void traverseGeneric(List<SearchToken> searchTokenList) {
		
		for (SearchToken searchToken : searchTokenList) {
						
			String hashPathString = currentPath + " " + rollingKeyHash + " " + searchToken.type + " " + searchToken.name + " " + searchToken.uuid;
			rollingKeyHash = CityHash.cityHash64Hex(hashPathString.getBytes(), 0 ,hashPathString.length());
			
			if (HlaPathBuilder.uuidMarkupOutput)
				pathFollowStack.push("{" + searchToken.lineNumber + "}" + "(" + searchToken.type + ") " + searchToken.name + " | " + "TID=\"" + searchToken.tid + "\" | " + rollingKeyHash + " | " + searchToken.uuid); 
			else
				pathFollowStack.push("{" + searchToken.lineNumber + "}" + "(" + searchToken.type + ") " + searchToken.name);
			
			switch(databaseAPI.getTID(searchToken)) {
			
			case Object:
				traverseObject(databaseAPI.getUUIDForObject(searchToken));
				break;
				
			case Interaction:
				traverseInteraction(databaseAPI.getUUIDForInteraction(searchToken));
				break;
				
			case FixedRecord:
				traverseFixedRecord(databaseAPI.getUUIDForFixedRecord(searchToken));
				break;
				
			case VariantRecord:
				traverseVariantRecord(databaseAPI.getUUIDForVariantRecord(searchToken));
				break;
				
			case Array:
				traverseArrayRecord(searchToken);
				break;
				
			case Simple:
				traverseSimpleRecord(searchToken);
				break;
				
			case Enumerated:
				traverseEnumeratedRecord(searchToken);
				break;
				
			case Basic:
				traverseBasicRecord(searchToken);
				break;
				
			case None:	
			default:
				displayPartialPath();
				break;
			}
			
			String poppedElement = pathFollowStack.pop();
			displayElementPathTransition(poppedElement);
			
		}
	}
	
	public void startTraversal(Constants.Element element, String elementUUID) {
		
		if (element == Constants.Element.Object) {
			
			databaseAPI.setElementIsType(Constants.Element.Object);
			
			rootElementPathArray = getObjectPath(elementUUID).replaceAll("\\[", "").
					replaceAll("\\]", "").replaceAll(" ", "").split(",");
			traverseObject(elementUUID);
			if (currentPathDef > 0)
				System.out.println("</pathDef" + currentPathDef + ">");
			System.out.println();
			System.out.println("<metaData>");
			System.out.println("Attributes: " + this.attrParams.toString());
			System.out.println("Attributes Length: " + this.attrParams.size());
			System.out.println("</metaData>");
		} else if (element == Constants.Element.Interaction) {
			
			databaseAPI.setElementIsType(Constants.Element.Interaction);
			
			rootElementPathArray = getInteractionPath(elementUUID).replaceAll("\\[", "").
					replaceAll("\\]", "").replaceAll(" ", "").split(",");
			traverseInteraction(elementUUID);
			if (currentPathDef > 0)
				System.out.println("</pathDef" + currentPathDef + ">");
			System.out.println();
			System.out.println("<metaData>");
			System.out.println("Parameters: " + this.attrParams.toString());
			System.out.println("Parameters Length: " + this.attrParams.size());
			System.out.println("</metaData>");
		}
	}
	
	public void setDatabase(DatabaseAPI databaseAPI) {
		
		this.databaseAPI = databaseAPI;
	}
}
