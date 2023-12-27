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
	private DatabaseAPI databaseAPI;
	
	private String[] rootElementPathArray;
	private int currentRootPathIndex = 0;
	
	private Constants.Element elementIsType;
	
	BuildElementPaths() {
		
	}
	
	private boolean isTerminalEndpoint(SearchToken searchToken) {
		
		boolean terminalEndpoint = true;
		
		if (databaseAPI.isObject(searchToken))
			terminalEndpoint = false;
		else if (databaseAPI.isInteraction(searchToken))
			terminalEndpoint = false;
		else if (databaseAPI.isFixedRecord(searchToken))
			terminalEndpoint = false;
		else if (databaseAPI.isVariantRecord(searchToken))
			terminalEndpoint = false;
		else if (databaseAPI.isArrayRecord(searchToken))
			terminalEndpoint = false;
		
		return terminalEndpoint;
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

		if (foundMatch) {

			System.out.print("[");

			for (int i = foundIndex; i < pathFollowStack.size(); i++) {
				System.out.print(pathFollowStack.get(i));
				if (i != (pathFollowStack.size() - 1))
					System.out.print(", ");
			}

			System.out.println("]");
		} else if (currentRootPathIndex == rootElementPathArray.length - 1) {

			System.out.print("[");

			for (int i = 0; i < pathFollowStack.size(); i++) {
				System.out.print(pathFollowStack.get(i));
				if (i != (pathFollowStack.size() - 1))
					System.out.print(", ");
			}

			System.out.println("]");
		}
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
			
			if (elementIsType == Constants.Element.OBJECT)
				System.out.print("\nObject Path: [");
			else
				System.out.print("\nInteraction Path: [");
			
			for (int i=0; i<foundIndex; i++) {
				System.out.print(rootElementPathArray[i]);
				if (i != (foundIndex-1))
					System.out.print(", ");
			}
			
			currentRootPathIndex = foundIndex - 1;
			
			System.out.println("]");
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
    		System.out.println("parentObject = " + var.parentObject);
    		System.out.println();
    		*/
    		if (var == null)
    			System.out.println("null found");
    		
    		// The id here doesn't have any relational aspect, use NULL_UUID
    		uuidRefList.add(new SearchToken(DatabaseAPI.NULL_UUID, var.name, var.type));
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
    		
    		// The id here doesn't have any relational aspect, use NULL_UUID
    		uuidRefList.add(new SearchToken(DatabaseAPI.NULL_UUID, var.name, var.type));
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
    		System.out.println("parentObject = " + var.parentObject);
    		System.out.println();
			*/
    		
    		// The id here doesn't have any relational aspect, use NULL_UUID
    		uuidRefList.add(new SearchToken(DatabaseAPI.NULL_UUID, var.name, var.type));
    	}
    	
		traverseGeneric(uuidRefList);
	}
	
	private void traverseVariantRecord(String elementUUID) {
		
    	String selectStatement = "SELECT * FROM VariantRecordField where parentObject = '" + elementUUID + "'";
    	
    	List<DbVariantRecordField> list = databaseAPI.selectFromVariantRecordFieldTable(selectStatement);
    	List<SearchToken> uuidRefList = new ArrayList<SearchToken>();
    	
    	for (DbVariantRecordField var : list) {
    		
    		boolean ignore = HlaPathBuilder.useVariantSelect;
    		
    		/*
    		System.out.println("id = " + var.id);
    		System.out.println("index = " + var.index);
    		System.out.println("origName = " + var.origName);
    		System.out.println("name = " + var.name);
    		System.out.println("type = " + var.type);
    		System.out.println("discriminant = " + var.discriminant);
    		System.out.println("alternative = " + var.alternative);
    		System.out.println("parentObject = " + var.parentObject);
    		System.out.println();
			*/

    		for (VariantSelect variantSelect : HlaPathBuilder.variantSelectList) {
    			
    			if ((var.parentObject.equals(variantSelect.parentUUID)) &&
    				(var.discriminant == true))
    				ignore = false;
    			
        		if ((var.parentObject.equals(variantSelect.parentUUID)) &&
        			(var.name.equals(variantSelect.alternative)))
        			ignore = false;
    		}
    		
    		if (!ignore) {
	    		// The id here doesn't have any relational aspect, use NULL_UUID
	    		uuidRefList.add(new SearchToken(DatabaseAPI.NULL_UUID, var.name, var.type));
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
			
			// The id here doesn't have any relational aspect, use NULL_UUID
			uuidRefList.add(new SearchToken(DatabaseAPI.NULL_UUID, var.name, var.type));
		}

		traverseGeneric(uuidRefList);
	}
	
	
	private void traverseGeneric(List<SearchToken> searchTokenList) {
		
		for (SearchToken searchToken : searchTokenList) {
			
			pathFollowStack.push("(" + searchToken.type + ") " + searchToken.name);
    		
			if (!isTerminalEndpoint(searchToken)) {
				
				if ((elementIsType == Constants.Element.OBJECT) && (databaseAPI.isObject(searchToken)))
					traverseObject(databaseAPI.getUUIDForObject(searchToken));
				else if ((elementIsType == Constants.Element.INTERACTION) && (databaseAPI.isInteraction(searchToken)))
					traverseInteraction(databaseAPI.getUUIDForInteraction(searchToken));
				else if (databaseAPI.isFixedRecord(searchToken))
					traverseFixedRecord(databaseAPI.getUUIDForFixedRecord(searchToken));
				else if (databaseAPI.isVariantRecord(searchToken))
					traverseVariantRecord(databaseAPI.getUUIDForVariantRecord(searchToken));
				else if (databaseAPI.isArrayRecord(searchToken))
					traverseArrayRecord(searchToken);
			} else {
				displayPartialPath();
			}
			
			String poppedElement = pathFollowStack.pop();
			displayElementPathTransition(poppedElement);
			
		}
	}
	
	public void startTraversal(Constants.Element element, String elementUUID) {
		
		if (element == Constants.Element.OBJECT) {
			
			elementIsType = Constants.Element.OBJECT;
			rootElementPathArray = getObjectPath(elementUUID).replaceAll("\\[", "").
					replaceAll("\\]", "").replaceAll(" ", "").split(",");
			traverseObject(elementUUID);
		} else if (element == Constants.Element.INTERACTION) {
			
			elementIsType = Constants.Element.INTERACTION;
			rootElementPathArray = getInteractionPath(elementUUID).replaceAll("\\[", "").
					replaceAll("\\]", "").replaceAll(" ", "").split(",");
			traverseInteraction(elementUUID);
		}
	}
	
	public void setDatabase(DatabaseAPI databaseAPI) {
		
		this.databaseAPI = databaseAPI;
	}
}
