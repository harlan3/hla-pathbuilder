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
	
	// SQL Table ID
	private Constants.TID getTID(SearchToken searchToken) {
		
		Constants.TID tid = Constants.TID.None;
		
		if ((elementIsType == Constants.Element.Object) && (databaseAPI.isObject(searchToken)))
			tid = Constants.TID.Object;
		else if ((elementIsType == Constants.Element.Interaction) && (databaseAPI.isInteraction(searchToken)))
			tid = Constants.TID.Interaction;
		else if (databaseAPI.isFixedRecord(searchToken))
			tid = Constants.TID.FixedRecord;
		else if (databaseAPI.isVariantRecord(searchToken))
			tid = Constants.TID.VariantRecord;
		else if (databaseAPI.isArrayRecord(searchToken))
			tid = Constants.TID.Array;
		else if (databaseAPI.isSimpleRecord(searchToken))
			tid = Constants.TID.Simple;
		else if (databaseAPI.isEnumeratedRecord(searchToken))
			tid = Constants.TID.Enumerated;
		else if (databaseAPI.isBasicRecord(searchToken))
			tid = Constants.TID.Basic;
		
		return tid;
	}
	
	private SearchResults deepSearchForUUID(SearchToken searchToken) {
		
		SearchResults searchResults = new SearchResults(Constants.TID.None, DatabaseAPI.NULL_UUID);
		searchResults.tid = getTID(searchToken);
		
		switch(searchResults.tid) {
		
		case Object:
			searchResults.uuid = databaseAPI.getUUIDForObject(searchToken);
			break;
			
		case Interaction:
			searchResults.uuid = databaseAPI.getUUIDForInteraction(searchToken);
			break;
			
		case FixedRecord:
			searchResults.uuid = databaseAPI.getUUIDForFixedRecord(searchToken);
			break;
			
		case VariantRecord:
			searchResults.uuid = databaseAPI.getUUIDForVariantRecord(searchToken);
			break;
			
		case Array:
			searchResults.uuid = databaseAPI.getUUIDForArrayRecord(searchToken);
			break;
			
		case Simple:
			searchResults.uuid = databaseAPI.getUUIDForSimpleRecord(searchToken);
			break;
			
		case Enumerated:
			searchResults.uuid = databaseAPI.getUUIDForEnumeratedRecord(searchToken);
			break;
			
		case Basic:
			searchResults.uuid = databaseAPI.getUUIDForBasicRecord(searchToken);
			break;			
			
		case None:
		default:
			break;
		}
		
		return searchResults;
	}
	
	private String getObjectPath(String elementUUID) {
		
    	String selectStatement = "SELECT * FROM Object WHERE id = '" + elementUUID + "'";
    	List<DbObject> objList = databaseAPI.selectFromObjectTable(selectStatement);
    	String objPath = null;
    	
		if (objList.size() >= 1)
			if (HlaPathBuilder.uuidMarkupOutput)
				objPath = objList.get(0).id;
			else
				objPath = objList.get(0).path;
		
		return objPath;	
	}
	
	private String getInteractionPath(String elementUUID) {
		
    	String selectStatement = "SELECT * FROM Interaction WHERE id = '" + elementUUID + "'";
    	List<DbInteraction> interactionList = databaseAPI.selectFromInteractionTable(selectStatement);
    	String interactionPath = null;
    	
		if (interactionList.size() >= 1)
			if (HlaPathBuilder.uuidMarkupOutput)
				interactionPath = interactionList.get(0).id;
			else
				interactionPath = interactionList.get(0).path;
		
		return interactionPath;	
	}
	
	private void insertPath(int index) {
		
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
			
			if (elementIsType == Constants.Element.Object)
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
    		
    		SearchResults searchResults = deepSearchForUUID(new SearchToken(DatabaseAPI.NULL_UUID, Constants.TID.None, var.name, var.type));
    		uuidRefList.add(new SearchToken(searchResults.uuid, searchResults.tid, var.name, var.type));
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
    		
    		SearchResults searchResults = deepSearchForUUID(new SearchToken(DatabaseAPI.NULL_UUID, Constants.TID.None, var.name, var.type));
    		uuidRefList.add(new SearchToken(searchResults.uuid, searchResults.tid, var.name, var.type));
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
    		
    		SearchResults searchResults = deepSearchForUUID(new SearchToken(DatabaseAPI.NULL_UUID, Constants.TID.None, var.name, var.type));
    		uuidRefList.add(new SearchToken(searchResults.uuid, searchResults.tid, var.name, var.type));
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

    			SearchResults searchResults = deepSearchForUUID(new SearchToken(DatabaseAPI.NULL_UUID, Constants.TID.None, var.name, var.type));
	    		uuidRefList.add(new SearchToken(searchResults.uuid, searchResults.tid, var.name, var.type));
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
			
			SearchResults searchResults = deepSearchForUUID(new SearchToken(DatabaseAPI.NULL_UUID, Constants.TID.None, var.name, var.type));
			uuidRefList.add(new SearchToken(searchResults.uuid, searchResults.tid, var.name, var.type));
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
			System.out.println();
			*/
			
			SearchResults searchResults = deepSearchForUUID(new SearchToken(DatabaseAPI.NULL_UUID, Constants.TID.None, var.name, var.type));
			uuidRefList.add(new SearchToken(searchResults.uuid, searchResults.tid, var.name, var.type));
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
			System.out.println();
			*/
			
			SearchResults searchResults = deepSearchForUUID(new SearchToken(DatabaseAPI.NULL_UUID, Constants.TID.None, var.name, var.type));
			uuidRefList.add(new SearchToken(searchResults.uuid, searchResults.tid, var.name, var.type));
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
			System.out.println();
			*/
			
			SearchResults searchResults = deepSearchForUUID(new SearchToken(DatabaseAPI.NULL_UUID, Constants.TID.None, var.name, var.type));
			uuidRefList.add(new SearchToken(searchResults.uuid, searchResults.tid, var.name, var.type));
		}

		traverseGeneric(uuidRefList);
	}
	
	private void traverseGeneric(List<SearchToken> searchTokenList) {
		
		for (SearchToken searchToken : searchTokenList) {
						
			if (HlaPathBuilder.uuidMarkupOutput)
				pathFollowStack.push(searchToken.uuid); 
			else
				pathFollowStack.push("(" + searchToken.type + ") " + searchToken.name); 
			
			switch(getTID(searchToken)) {
			
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
			
			elementIsType = Constants.Element.Object;
			rootElementPathArray = getObjectPath(elementUUID).replaceAll("\\[", "").
					replaceAll("\\]", "").replaceAll(" ", "").split(",");
			traverseObject(elementUUID);
		} else if (element == Constants.Element.Interaction) {
			
			elementIsType = Constants.Element.Interaction;
			rootElementPathArray = getInteractionPath(elementUUID).replaceAll("\\[", "").
					replaceAll("\\]", "").replaceAll(" ", "").split(",");
			traverseInteraction(elementUUID);
		}
	}
	
	public void setDatabase(DatabaseAPI databaseAPI) {
		
		this.databaseAPI = databaseAPI;
	}
}
