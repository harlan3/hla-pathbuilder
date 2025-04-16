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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import orbisoftware.hla_pathbuilder.db_classes.*;

public class DatabaseAPI {

	private String connectMemoryStr = "jdbc:derby:memory:myDB;create=true"; // in memory
	private String connectFileStr = "jdbc:derby:myDB;create=true"; // in file
	private static Connection conn = null;
	
	private Constants.Element elementIsType;
	
	public void initDatabase() {
		
        try
        {
        	if (HlaPathBuilder.useMemoryDb)
        		conn = DriverManager.getConnection(connectMemoryStr);
        	else
        		conn = DriverManager.getConnection(connectFileStr);
        }
        catch (Exception except)
        {
            except.printStackTrace();
        }
	}
	
	public void createTables() {
		
        try {
			run("CREATE TABLE Object ("
				+ "id VARCHAR(36) PRIMARY KEY, "
			    + "name VARCHAR(80), "
				+ "path VARCHAR(400), "
				+ "debugPath VARCHAR(400), "
				+ "parentObject VARCHAR(36))");
			
			run("CREATE TABLE Attribute ("
					+ "id VARCHAR(36) PRIMARY KEY, "
				    + "index INTEGER, "
					+ "origName VARCHAR(80), "
					+ "name VARCHAR(80), "
				    + "type VARCHAR(80), "
					+ "inherited BOOLEAN, "
				    + "parentObject VARCHAR(36))");			

			run("CREATE TABLE Interaction ("
					+ "id VARCHAR(36) PRIMARY KEY, "
				    + "name VARCHAR(80), "
					+ "path VARCHAR(400), "
				    + "debugPath VARCHAR(400), "
					+ "parentObject VARCHAR(36))");
			
			run("CREATE TABLE Parameter ("
					+ "id VARCHAR(36) PRIMARY KEY, "
				    + "index INTEGER, "
				    + "origName VARCHAR(80), "
					+ "name VARCHAR(80), "
				    + "type VARCHAR(80), "
					+ "inherited BOOLEAN, "
				    + "parentObject VARCHAR(36))");	

			run("CREATE TABLE BasicDatatype ("
					+ "id VARCHAR(36) PRIMARY KEY, "
				    + "name VARCHAR(80), "
					+ "type VARCHAR(80),"
					+ "size VARCHAR(80),"
					+ "endian VARCHAR(80))");    		

			run("CREATE TABLE SimpleDatatype ("
					+ "id VARCHAR(36) PRIMARY KEY, "
				    + "name VARCHAR(80), "
					+ "type VARCHAR(80))");
			
			run("CREATE TABLE EnumeratedDatatype ("
					+ "id VARCHAR(36) PRIMARY KEY, "
				    + "name VARCHAR(80), "
					+ "type VARCHAR(80))");
			
			run("CREATE TABLE EnumeratorDatatype ("
					+ "id VARCHAR(36) PRIMARY KEY, "
					+ "name VARCHAR(80), "
					+ "ordinalValue INTEGER, "
					+ "parentObject VARCHAR(36))");	
			
			run("CREATE TABLE ArrayDatatype ("
					+ "id VARCHAR(36) PRIMARY KEY, "
				    + "name VARCHAR(80), "
					+ "type VARCHAR(80), "
					+ "cardinality VARCHAR(80),"
					+ "encoding VARCHAR(80))");
			
			run("CREATE TABLE FixedRecordDatatype ("
					+ "id VARCHAR(36) PRIMARY KEY, "
				    + "name VARCHAR(80))");
			
			run("CREATE TABLE FixedRecordField ("
					+ "id VARCHAR(36) PRIMARY KEY, "
				    + "index INTEGER, "
				    + "origName VARCHAR(80), "
					+ "name VARCHAR(80), "
				    + "type VARCHAR(80), "
				    + "encoding VARCHAR(80), "
				    + "primitive VARCHAR(80), "
				    + "parentObject VARCHAR(36))");	
			
			run("CREATE TABLE VariantRecordDatatype ("
					+ "id VARCHAR(36) PRIMARY KEY, "
				    + "name VARCHAR(80))");
			
			run("CREATE TABLE VariantRecordField ("
					+ "id VARCHAR(36) PRIMARY KEY, "
				    + "index INTEGER, "
				    + "origName VARCHAR(80), "
					+ "name VARCHAR(80), "
				    + "type VARCHAR(80), "
					+ "discriminant BOOLEAN, "					
					+ "alternative BOOLEAN, "				    
				    + "parentObject VARCHAR(36))");	
			
			run("CREATE TABLE SemanticsDatatype ("
					+ "id VARCHAR(36) PRIMARY KEY, "
					+ "name VARCHAR(80), "
				    + "semantics VARCHAR(400))");
			
			run("CREATE TABLE VariantOrdering ("
					+ "id VARCHAR(36) PRIMARY KEY, "
					+ "variant VARCHAR(80), "
					+ "discriminant VARCHAR(80), "
				    + "ordering VARCHAR(80))");
					
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void setElementIsType(Constants.Element elementIsType) {
		
		this.elementIsType = elementIsType;
	}
	
	// SQL Table ID
	public Constants.TID getTID(SearchToken searchToken) {
		
		Constants.TID tid = Constants.TID.None;
		
		if (searchToken.tid != Constants.TID.None) {
			
			tid = searchToken.tid;
			return searchToken.tid;
		}
			
		if ((elementIsType == Constants.Element.Object) && (isObject(searchToken)))
			tid = Constants.TID.Object;
		else if ((elementIsType == Constants.Element.Interaction) && (isInteraction(searchToken)))
			tid = Constants.TID.Interaction;
		else if (isFixedRecord(searchToken))
			tid = Constants.TID.FixedRecord;
		else if (isVariantRecord(searchToken))
			tid = Constants.TID.VariantRecord;
		else if (isArrayRecord(searchToken))
			tid = Constants.TID.Array;
		else if (isBasicRecord(searchToken))
			tid = Constants.TID.Basic;
		else if (isSimpleRecord(searchToken))
			tid = Constants.TID.Simple;
		else if (isEnumeratedRecord(searchToken))
			tid = Constants.TID.Enumerated;
		
		return tid;
	}
	
	public SearchResults deepSearchForUUID(SearchToken searchToken) {
		
		SearchResults searchResults = new SearchResults(Constants.TID.None, Constants.NULL_UUID);
		searchResults.tid = getTID(searchToken);
		
		switch(searchResults.tid) {
		
		case Object:
			searchResults.uuid = getUUIDForObject(searchToken);
			break;
			
		case Interaction:
			searchResults.uuid = getUUIDForInteraction(searchToken);
			break;
			
		case FixedRecord:
			searchResults.uuid = getUUIDForFixedRecord(searchToken);
			break;
			
		case FixedRecordField:
			searchResults.uuid = getUUIDForFixedRecordField(searchToken);
			break;
			
		case VariantRecord:
			searchResults.uuid = getUUIDForVariantRecord(searchToken);
			break;
			
		case Array:
			searchResults.uuid = getUUIDForArrayRecord(searchToken);
			break;
			
		case Simple:
			searchResults.uuid = getUUIDForSimpleRecord(searchToken);
			break;
			
		case Enumerated:
			searchResults.uuid = getUUIDForEnumeratedRecord(searchToken);
			break;
			
		case Basic:
			searchResults.uuid = getUUIDForBasicRecord(searchToken);
			break;			
			
		case None:
		default:
			break;
		}
		
		return searchResults;
	}
	
	public void insertIntoObjectTable(List<DbObject> list) {

		try {
			Statement stmt = conn.createStatement();
			String tableName = "Object";

			for (DbObject var : list) {

				stmt.execute("INSERT INTO " + tableName + " VALUES ('" + 
					var.id + "','" + 
					var.name + "','" +
					var.path + "','" +
					var.debugPath + "','" +
					var.parentObject + "')");
			}
		} catch (SQLException sqlExcept) {
			sqlExcept.printStackTrace();
		}
	}
	
	public void insertIntoAttributeTable(List<DbAttribute> list) {

		try {
			Statement stmt = conn.createStatement();
			String tableName = "Attribute";

			for (DbAttribute var : list) {

				stmt.execute("INSERT INTO " + tableName + " VALUES ('" + 
					var.id + "'," + 
					var.index + ",'" +
					var.origName + "','" +
					var.name + "','" +
					var.type + "'," +
					var.inherited + ",'" +
					var.parentObject + "')");
			}
		} catch (SQLException sqlExcept) {
			sqlExcept.printStackTrace();
		}
	}

	public void insertIntoInteractionTable(List<DbInteraction> list) {

		try {
			Statement stmt = conn.createStatement();
			String tableName = "Interaction";

			for (DbInteraction var : list) {

				stmt.execute("INSERT INTO " + tableName + " VALUES ('" + 
					var.id + "','" + 
					var.name + "','" +
					var.path + "','" +
					var.debugPath + "','" +
					var.parentObject + "')");
			}
		} catch (SQLException sqlExcept) {
			sqlExcept.printStackTrace();
		}
	}
	
	public void insertIntoParameterTable(List<DbParameter> list) {

		try {
			Statement stmt = conn.createStatement();
			String tableName = "Parameter";

			for (DbParameter var : list) {

				stmt.execute("INSERT INTO " + tableName + " VALUES ('" + 
					var.id + "'," + 
					var.index + ",'" +
					var.origName + "','" +
					var.name + "','" +
					var.type + "'," +
					var.inherited + ",'" +
					var.parentObject + "')");
			}
		} catch (SQLException sqlExcept) {
			sqlExcept.printStackTrace();
		}
	}
	
	public void insertIntoBasicDatatypeTable(List<DbBasicDatatype> list) {

		try {
			Statement stmt = conn.createStatement();
			String tableName = "BasicDatatype";

			for (DbBasicDatatype var : list) {

				stmt.execute("INSERT INTO " + tableName + " VALUES ('" +  
					var.id + "','" + 
					var.name + "','" +
					var.type + "','" +
					var.size + "','" +
					var.endian + "')");		
			}
		} catch (SQLException sqlExcept) {
			sqlExcept.printStackTrace();
		}
	}
	
	public void insertIntoSimpleDatatypeTable(List<DbSimpleDatatype> list) {

		try {
			Statement stmt = conn.createStatement();
			String tableName = "SimpleDatatype";

			for (DbSimpleDatatype var : list) {

				stmt.execute("INSERT INTO " + tableName + " VALUES ('" + 
					var.id + "','" + 
					var.name + "','" +
					var.type + "')");
			}
		} catch (SQLException sqlExcept) {
			sqlExcept.printStackTrace();
		}
	}
	
	public void insertIntoEnumeratedDatatypeTable(List<DbEnumeratedDatatype> list) {

		try {
			Statement stmt = conn.createStatement();
			String tableName = "EnumeratedDatatype";

			for (DbEnumeratedDatatype var : list) {

				stmt.execute("INSERT INTO " + tableName + " VALUES ('" + 
					var.id + "','" + 
					var.name + "','" +
					var.type + "')");
			}
		} catch (SQLException sqlExcept) {
			sqlExcept.printStackTrace();
		}
	}
	
	public void insertIntoEnumeratorDatatypeTable(List<DbEnumeratorDatatype> list) {

		try {
			Statement stmt = conn.createStatement();
			String tableName = "EnumeratorDatatype";

			for (DbEnumeratorDatatype var : list) {

				stmt.execute("INSERT INTO " + tableName + " VALUES ('" + 
					var.id + "','" + 
					var.name + "'," +
					var.ordinalValue + ",'" + 
					var.parentObject + "')");
			}
		} catch (SQLException sqlExcept) {
			sqlExcept.printStackTrace();
		}
	}
	
	public void insertIntoArrayDatatypeTable(List<DbArrayDatatype> list) {

		try {
			Statement stmt = conn.createStatement();
			String tableName = "ArrayDatatype";

			for (DbArrayDatatype var : list) {

				stmt.execute("INSERT INTO " + tableName + " VALUES ('" + 
					var.id + "','" + 
					var.name + "','" +
					var.type + "','" +
					var.cardinality + "','" + 
					var.encoding + "')");
			}
		} catch (SQLException sqlExcept) {
			sqlExcept.printStackTrace();
		}
	}
	
	public void insertIntoFixedRecordDatatypeTable(List<DbFixedRecordDatatype> list) {

		try {
			Statement stmt = conn.createStatement();
			String tableName = "FixedRecordDatatype";

			for (DbFixedRecordDatatype var : list) {

				stmt.execute("INSERT INTO " + tableName + " VALUES ('" + 
					var.id + "','" + 
					var.name + "')");
			}
		} catch (SQLException sqlExcept) {
			sqlExcept.printStackTrace();
		}
	}
	
	public void insertIntoFixedRecordFieldTable(List<DbFixedRecordField> list) {

		try {
			Statement stmt = conn.createStatement();
			String tableName = "FixedRecordField";

			for (DbFixedRecordField var : list) {

				stmt.execute("INSERT INTO " + tableName + " VALUES ('" + 
					var.id + "'," + 
					var.index + ",'" +
					var.origName + "','" +
					var.name + "','" +
					var.type + "','" +
					var.encoding + "','" +
					var.primitive + "','" +
					var.parentObject + "')");
			}
		} catch (SQLException sqlExcept) {
			sqlExcept.printStackTrace();
		}
	}
	
	public void insertIntoVariantRecordDatatypeTable(List<DbVariantRecordDatatype> list) {

		try {
			Statement stmt = conn.createStatement();
			String tableName = "VariantRecordDatatype";

			for (DbVariantRecordDatatype var : list) {

				stmt.execute("INSERT INTO " + tableName + " VALUES ('" + 
					var.id + "','" + 
					var.name + "')");
			}
		} catch (SQLException sqlExcept) {
			sqlExcept.printStackTrace();
		}
	}
	
	public void insertIntoVariantRecordFieldTable(List<DbVariantRecordField> list) {

		try {
			Statement stmt = conn.createStatement();
			String tableName = "VariantRecordField";

			for (DbVariantRecordField var : list) {

				stmt.execute("INSERT INTO " + tableName + " VALUES ('" + 
					var.id + "'," + 
					var.index + ",'" +
					var.origName + "','" +
					var.name + "','" +
					var.type + "'," +
					var.discriminant + "," +
					var.alternative + ",'" +
					var.parentObject + "')");
			}
		} catch (SQLException sqlExcept) {
			sqlExcept.printStackTrace();
		}
	}
	
	public void insertIntoSemanticsDatatypeTable(List<DbSemanticsDatatype> list) {

		try {
			Statement stmt = conn.createStatement();
			String tableName = "SemanticsDatatype";

			for (DbSemanticsDatatype var : list) {

				stmt.execute("INSERT INTO " + tableName + " VALUES ('" + 
					var.id + "','" + 
					var.name + "','" +
					var.semantics + "')");
			}
		} catch (SQLException sqlExcept) {
			sqlExcept.printStackTrace();
		}
	}
	
	public void insertIntoVariantOrdering(List<DbVariantOrderingDatatype> list) {

		try {
			Statement stmt = conn.createStatement();
			String tableName = "VariantOrdering";

			for (DbVariantOrderingDatatype var : list) {

				stmt.execute("INSERT INTO " + tableName + " VALUES ('" + 
					var.id + "','" + 
					var.variant + "','" +
					var.discriminant + "','" +
					var.ordering + "')");
			}
		} catch (SQLException sqlExcept) {
			sqlExcept.printStackTrace();
		}
	}
	
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

		insertIntoAttributeTable(list);
	}
	
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

		insertIntoParameterTable(list);
	}
	
    public List<DbObject> selectFromObjectTable(String selectStatement)
    {
    	List<DbObject> list = new ArrayList<DbObject>();
    	
        try
        {
        	Statement stmt = conn.createStatement();
        	ResultSet results = stmt.executeQuery(selectStatement);
        	
        	while (results.next()) {
        		
        		DbObject var = new DbObject();
        		
        		var.id = results.getString("id");
        		var.name = results.getString("name");
        		var.path = results.getString("path");
        		var.debugPath = results.getString("debugPath");
        		var.parentObject = results.getString("parentObject");
        		
        		list.add(var);
        	}
        	
        	results.close();
            stmt.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }
        
        return list;
    }
    
    public List<DbAttribute> selectFromAttributeTable(String selectStatement)
    {
    	List<DbAttribute> list = new ArrayList<DbAttribute>();
    	
        try
        {
        	Statement stmt = conn.createStatement();
        	ResultSet results = stmt.executeQuery(selectStatement);
        	
        	while (results.next()) {
        		
        		DbAttribute var = new DbAttribute();
        		
        		var.id = results.getString("id");
        		var.index = results.getInt("index");
        		var.origName = results.getString("origName");
        		var.name = results.getString("name");
        		var.type = results.getString("type");
        		var.inherited = results.getBoolean("inherited");
        		var.parentObject = results.getString("parentObject");
        		
        		list.add(var);
        	}
        	
        	results.close();
            stmt.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }
        
        return list;
    }
    
    public List<DbInteraction> selectFromInteractionTable(String selectStatement)
    {
    	List<DbInteraction> list = new ArrayList<DbInteraction>();
    	
        try
        {
        	Statement stmt = conn.createStatement();
        	ResultSet results = stmt.executeQuery(selectStatement);
        	
        	while (results.next()) {
        		
        		DbInteraction var = new DbInteraction();
        		
        		var.id = results.getString("id");
        		var.name = results.getString("name");
        		var.path = results.getString("path");
        		var.debugPath = results.getString("debugPath");
        		var.parentObject = results.getString("parentObject");
        		
        		list.add(var);
        	}
        	
        	results.close();
            stmt.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }
        
        return list;
    }
    
    public List<DbParameter> selectFromParameterTable(String selectStatement)
    {
    	List<DbParameter> list = new ArrayList<DbParameter>();
    	
        try
        {
        	Statement stmt = conn.createStatement();
        	ResultSet results = stmt.executeQuery(selectStatement);
        	
        	while (results.next()) {
        		
        		DbParameter var = new DbParameter();
        		
        		var.id = results.getString("id");
        		var.index = results.getInt("index");
        		var.origName = results.getString("origName");
        		var.name = results.getString("name");
        		var.type = results.getString("type");
        		var.inherited = results.getBoolean("inherited");
        		var.parentObject = results.getString("parentObject");
        		
        		list.add(var);
        	}
        	
        	results.close();
            stmt.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }
        
        return list;
    }
    
    public List<DbSimpleDatatype> selectFromSimpleDatatypeTable(String selectStatement)
    {
    	List<DbSimpleDatatype> list = new ArrayList<DbSimpleDatatype>();
    	
        try
        {
        	Statement stmt = conn.createStatement();
        	ResultSet results = stmt.executeQuery(selectStatement);
        	
        	while (results.next()) {
        		
        		DbSimpleDatatype var = new DbSimpleDatatype();
        		
        		var.id = results.getString("id");
        		var.name = results.getString("name");
        		var.type = results.getString("type");
        		
        		list.add(var);
        	}
        	
        	results.close();
            stmt.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }
        
        return list;
    }
    
    public List<DbBasicDatatype> selectFromBasicDatatypeTable(String selectStatement)
    {
    	List<DbBasicDatatype> list = new ArrayList<DbBasicDatatype>();
    	
        try
        {
        	Statement stmt = conn.createStatement();
        	ResultSet results = stmt.executeQuery(selectStatement);
        	
        	while (results.next()) {
        		
        		DbBasicDatatype var = new DbBasicDatatype();
        		
        		var.id = results.getString("id");
        		var.name = results.getString("name");
        		var.type = "";
        		var.size = results.getString("size");
        		var.endian = results.getString("endian");
        		
        		list.add(var);
        	}
        	
        	results.close();
            stmt.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }
        
        return list;
    }
    
    public List<DbEnumeratedDatatype> selectFromEnumeratedDatatypeTable(String selectStatement)
    {
    	List<DbEnumeratedDatatype> list = new ArrayList<DbEnumeratedDatatype>();
    	
        try
        {
        	Statement stmt = conn.createStatement();
        	ResultSet results = stmt.executeQuery(selectStatement);
        	
        	while (results.next()) {
        		
        		DbEnumeratedDatatype var = new DbEnumeratedDatatype();
        		
        		var.id = results.getString("id");
        		var.name = results.getString("name");
        		var.type = results.getString("type");
        		
        		list.add(var);
        	}
        	
        	results.close();
            stmt.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }
        
        return list;
    }
    
    public List<DbEnumeratorDatatype> selectFromEnumeratorDatatypeTable(String selectStatement)
    {
    	List<DbEnumeratorDatatype> list = new ArrayList<DbEnumeratorDatatype>();
    	
        try
        {
        	Statement stmt = conn.createStatement();
        	ResultSet results = stmt.executeQuery(selectStatement);
        	
        	while (results.next()) {
        		
        		DbEnumeratorDatatype var = new DbEnumeratorDatatype();
        		
        		var.id = results.getString("id");
        		var.name = results.getString("name");
        		var.ordinalValue = results.getInt("ordinalValue");
        		var.parentObject = results.getString("parentObject");

        		list.add(var);
        	}
        	
        	results.close();
            stmt.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }
        
        return list;
    }
	
    public List<DbArrayDatatype> selectFromArrayDatatypeTable(String selectStatement)
    {
    	List<DbArrayDatatype> list = new ArrayList<DbArrayDatatype>();
    	
        try
        {
        	Statement stmt = conn.createStatement();
        	ResultSet results = stmt.executeQuery(selectStatement);
        	
        	while (results.next()) {
        		
        		DbArrayDatatype var = new DbArrayDatatype();
        		
        		var.id = results.getString("id");
        		var.name = results.getString("name");
        		var.type = results.getString("type");
        		var.cardinality = results.getString("cardinality");
        		var.encoding = results.getString("encoding");
        		
        		list.add(var);
        	}
        	
        	results.close();
            stmt.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }
        
        return list;
    }
    
    public List<DbFixedRecordDatatype> selectFromFixedRecordDatatypeTable(String selectStatement)
    {
    	List<DbFixedRecordDatatype> list = new ArrayList<DbFixedRecordDatatype>();
    	
        try
        {
        	Statement stmt = conn.createStatement();
        	ResultSet results = stmt.executeQuery(selectStatement);
        	
        	while (results.next()) {
        		
        		DbFixedRecordDatatype var = new DbFixedRecordDatatype();
        		
        		var.id = results.getString("id");
        		var.name = results.getString("name");
        		
        		list.add(var);
        	}
        	
        	results.close();
            stmt.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }
        
        return list;
    }
    
    public List<DbFixedRecordField> selectFromFixedRecordFieldTable(String selectStatement)
    {
    	List<DbFixedRecordField> list = new ArrayList<DbFixedRecordField>();
    	
        try
        {
        	Statement stmt = conn.createStatement();
        	ResultSet results = stmt.executeQuery(selectStatement);
        	
        	while (results.next()) {
        		
        		DbFixedRecordField var = new DbFixedRecordField();
        		
        		var.id = results.getString("id");
        		var.index = results.getInt("index");
        		var.origName = results.getString("origName");
        		var.name = results.getString("name");
        		var.type = results.getString("type");
        		var.encoding = results.getString("encoding");
        		var.primitive = results.getString("primitive");
        		var.parentObject = results.getString("parentObject");
        		
        		list.add(var);
        	}
        	
        	results.close();
            stmt.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }
        
        return list;
    }
    
    public List<DbVariantRecordDatatype> selectFromVariantRecordDatatypeTable(String selectStatement)
    {
    	List<DbVariantRecordDatatype> list = new ArrayList<DbVariantRecordDatatype>();
    	
        try
        {
        	Statement stmt = conn.createStatement();
        	ResultSet results = stmt.executeQuery(selectStatement);
        	
        	while (results.next()) {
        		
        		DbVariantRecordDatatype var = new DbVariantRecordDatatype();
        		
        		var.id = results.getString("id");
        		var.name = results.getString("name");
        		
        		list.add(var);
        	}
        	
        	results.close();
            stmt.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }
        
        return list;
    }
    
    public List<DbVariantRecordField> selectFromVariantRecordFieldTable(String selectStatement)
    {
    	List<DbVariantRecordField> list = new ArrayList<DbVariantRecordField>();
    	
        try
        {
        	Statement stmt = conn.createStatement();
        	ResultSet results = stmt.executeQuery(selectStatement);
        	
        	while (results.next()) {
        		
        		DbVariantRecordField var = new DbVariantRecordField();
        		
        		var.id = results.getString("id");
        		var.index = results.getInt("index");
        		var.origName = results.getString("origName"); 
        		var.name = results.getString("name");
        		var.type = results.getString("type");
        		var.discriminant = results.getBoolean("discriminant");
        		var.alternative = results.getBoolean("alternative");
        		var.parentObject = results.getString("parentObject");
        		
        		list.add(var);
        	}
        	
        	results.close();
            stmt.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }
        
        return list;
    }
    
    public List<DbSemanticsDatatype> selectFromSemanticsDatatypeTable(String selectStatement)
    {
    	List<DbSemanticsDatatype> list = new ArrayList<DbSemanticsDatatype>();
    	
        try
        {
        	Statement stmt = conn.createStatement();
        	ResultSet results = stmt.executeQuery(selectStatement);
        	
        	while (results.next()) {
        		
        		DbSemanticsDatatype var = new DbSemanticsDatatype();
        		
        		var.id = results.getString("id");
        		var.name = results.getString("name");
        		var.semantics = results.getString("semantics"); 
        		
        		list.add(var);
        	}
        	
        	results.close();
            stmt.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }
        
        return list;
    }
    
    public List<DbVariantOrderingDatatype> selectFromVariantOrderingDatatypeTable(String selectStatement)
    {
    	List<DbVariantOrderingDatatype> list = new ArrayList<DbVariantOrderingDatatype>();
    	
        try
        {
        	Statement stmt = conn.createStatement();
        	ResultSet results = stmt.executeQuery(selectStatement);
        	
        	while (results.next()) {
        		
        		DbVariantOrderingDatatype var = new DbVariantOrderingDatatype();
        		
        		var.id = results.getString("id");
        		var.variant = results.getString("variant");
        		var.discriminant = results.getString("discriminant");
        		var.ordering = results.getString("ordering");
        		
        		list.add(var);
        	}
        	
        	results.close();
            stmt.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }
        
        return list;
    }
    
    private String getSemanticsDatatypeForName(String name) {
    	
    	String returnSemanticsField = "";
		String selectStatement = "SELECT * FROM SemanticsDatatype WHERE name = '" + name + "'";
		
		List<DbSemanticsDatatype> returnVal = selectFromSemanticsDatatypeTable(selectStatement);
		
		if (returnVal.size() >= 1)
			returnSemanticsField = returnVal.get(0).semantics;
		
		return returnSemanticsField;
    }
    
    private String getSemanticsDatatypeForUUID(String uuidString) {
    	
    	String returnSemanticsField = "";
		String selectStatement = "SELECT * FROM SemanticsDatatype WHERE id = '" + uuidString + "'";
		
		List<DbSemanticsDatatype> returnVal = selectFromSemanticsDatatypeTable(selectStatement);
		
		if (returnVal.size() >= 1)
			returnSemanticsField = returnVal.get(0).semantics;
		
		return returnSemanticsField;
    }
    
	public String getSemanticsText(String tid, String type, String name) {
		
		Utils utils = new Utils();
		
		SearchResults searchResults = deepSearchForUUID(new SearchToken(Constants.NULL_UUID, 
				utils.getTIDFromText(tid.trim()), name.trim(), type.trim()));
		
		String semanticsText = getSemanticsDatatypeForUUID(searchResults.uuid).trim();
		
		if (!name.equals("")) {
			
			if (semanticsText.equals(""))
				semanticsText = getSemanticsDatatypeForName(name.trim()).trim();
		}
		
		return semanticsText;
	}
	
	public String getSemanticsText(String tid, String type) {
		
		Utils utils = new Utils();
		
		SearchResults searchResults = deepSearchForUUID(new SearchToken(Constants.NULL_UUID, utils.getTIDFromText(tid.trim()), "", type));
		String semanticsText = getSemanticsDatatypeForUUID(searchResults.uuid).trim();
		
		return semanticsText;
	}
    
    public String getUUIDForObject(SearchToken searchToken) {
    	
    	String returnUUID = Constants.NULL_UUID;
		String selectStatement = "SELECT * FROM Object WHERE name = '" + searchToken.type + "'";
		
		List<DbObject> returnVal = selectFromObjectTable(selectStatement);
		
		if (returnVal.size() >= 1)
			returnUUID = returnVal.get(0).id;
		
		return returnUUID;
    }
    
    public boolean isObject(SearchToken searchToken) {
    	
    	String selectStatement;
    	
    	if (searchToken.uuid != Constants.NULL_UUID)
    		selectStatement = "SELECT * FROM Object WHERE id = '" + searchToken.uuid + "'";
    	else
    		selectStatement = "SELECT * FROM Object WHERE name = '" + searchToken.type + "'";
    		
    	List<DbObject> returnVal = selectFromObjectTable(selectStatement);
    	
    	return returnVal.size() >= 1;
    }
    
    public String getUUIDForAttribute(SearchToken searchToken) {
    	
    	String returnUUID = Constants.NULL_UUID;
		String selectStatement = "SELECT * FROM Attribute WHERE name = '" + searchToken.type + "'";
		
		List<DbAttribute> returnVal = selectFromAttributeTable(selectStatement);
		
		if (returnVal.size() >= 1)
			returnUUID = returnVal.get(0).id;
		
		return returnUUID;
    }
    
    public String getUUIDForInteraction(SearchToken searchToken) {
    	
    	String returnUUID = Constants.NULL_UUID;
		String selectStatement = "SELECT * FROM Interaction WHERE name = '" + searchToken.type + "'";
		
		List<DbInteraction> returnVal = selectFromInteractionTable(selectStatement);
		
		if (returnVal.size() >= 1)
			returnUUID = returnVal.get(0).id;
		
		return returnUUID;
    }
    
    public boolean isInteraction(SearchToken searchToken) {
    	
    	String selectStatement;
    	
    	if (searchToken.uuid != Constants.NULL_UUID)
    		selectStatement = "SELECT * FROM Interaction WHERE id = '" + searchToken.uuid + "'";
    	else
    		selectStatement = "SELECT * FROM Interaction WHERE name = '" + searchToken.type + "'";
    	
    	List<DbInteraction> returnVal = selectFromInteractionTable(selectStatement);
    	
    	return returnVal.size() >= 1;
    }
    
    public String getUUIDForParameter(SearchToken searchToken) {
    	
    	String returnUUID = Constants.NULL_UUID;
		String selectStatement = "SELECT * FROM Parameter WHERE name = '" + searchToken.type + "'";
		
		List<DbParameter> returnVal = selectFromParameterTable(selectStatement);
		
		if (returnVal.size() >= 1)
			returnUUID = returnVal.get(0).id;
		
		return returnUUID;
    }
    
    public String getUUIDForFixedRecord(SearchToken searchToken) {
    	
    	String returnUUID = Constants.NULL_UUID;
		String selectStatement = "SELECT * FROM FixedRecordDatatype WHERE name = '" + searchToken.type + "'";
		
		List<DbFixedRecordDatatype> returnVal = selectFromFixedRecordDatatypeTable(selectStatement);
		
		if (returnVal.size() >= 1)
			returnUUID = returnVal.get(0).id;
		
		return returnUUID;
    }
    
    public String getUUIDForFixedRecordField(SearchToken searchToken) {
    	
    	String returnUUID = Constants.NULL_UUID;
		String selectStatement = "SELECT * FROM FixedRecordField WHERE name = '" + searchToken.name + "' and type = '" + searchToken.type + "'";
		
		List<DbFixedRecordField> returnVal = selectFromFixedRecordFieldTable(selectStatement);
		
		if (returnVal.size() >= 1)
			returnUUID = returnVal.get(0).id;
		
		return returnUUID;
    }
    
    public boolean isFixedRecord(SearchToken searchToken) {
    	
    	String selectStatement;
    	
    	if (searchToken.uuid != Constants.NULL_UUID)
    		selectStatement = "SELECT * FROM FixedRecordDatatype WHERE id = '" + searchToken.uuid  + "'";
    	else
    		selectStatement = "SELECT * FROM FixedRecordDatatype WHERE name = '" + searchToken.type + "'";

    	List<DbFixedRecordDatatype> returnVal = selectFromFixedRecordDatatypeTable(selectStatement);
    	
    	return returnVal.size() >= 1;
    }
    
    public String getUUIDForVariantRecord(SearchToken searchToken) {
    	
    	String returnUUID = Constants.NULL_UUID;
		String selectStatement = "SELECT * FROM VariantRecordDatatype WHERE name = '" + searchToken.type + "'";
		
		List<DbVariantRecordDatatype> returnVal = selectFromVariantRecordDatatypeTable(selectStatement);
		
		if (returnVal.size() >= 1)
			returnUUID = returnVal.get(0).id;
		
		return returnUUID;
    }
    
    public boolean isVariantRecord(SearchToken searchToken) {
    	
    	String selectStatement;
    	
    	if (searchToken.uuid != Constants.NULL_UUID)
    		selectStatement = "SELECT * FROM VariantRecordDatatype WHERE id = '" + searchToken.uuid + "'";
    	else
    		selectStatement = "SELECT * FROM VariantRecordDatatype WHERE name = '" + searchToken.type + "'";
    	
    	List<DbVariantRecordDatatype> returnVal = selectFromVariantRecordDatatypeTable(selectStatement);
    	
    	return returnVal.size() >= 1;
    }
    
    public String getUUIDForArrayRecord(SearchToken searchToken) {
    	
    	String returnUUID = Constants.NULL_UUID;
		String selectStatement = "SELECT * FROM ArrayDatatype WHERE name = '" + searchToken.type + "'";
		
		List<DbArrayDatatype> returnVal = selectFromArrayDatatypeTable(selectStatement);
		
		if (returnVal.size() >= 1)
			returnUUID = returnVal.get(0).id;
		
		return returnUUID;
    }
    
    public boolean isArrayRecord(SearchToken searchToken) {
    	
    	String selectStatement;
    	
    	if (searchToken.uuid != Constants.NULL_UUID)
    		selectStatement = "SELECT * FROM ArrayDatatype WHERE id = '" + searchToken.uuid + "'";
    	else
    		selectStatement = "SELECT * FROM ArrayDatatype WHERE name = '" + searchToken.type + "'";
    		
    	List<DbArrayDatatype> returnVal = selectFromArrayDatatypeTable(selectStatement);
    	
    	return returnVal.size() >= 1;	
    	
    }
    
    public String getUUIDForSimpleRecord(SearchToken searchToken) {
    	
    	String returnUUID = Constants.NULL_UUID;
		String selectStatement = "SELECT * FROM SimpleDatatype WHERE name = '" + searchToken.type + "'";
		
		List<DbSimpleDatatype> returnVal = selectFromSimpleDatatypeTable(selectStatement);
		
		if (returnVal.size() >= 1)
			returnUUID = returnVal.get(0).id;
		
		return returnUUID;
    }
    
    public boolean isSimpleRecord(SearchToken searchToken) {
    	
    	String selectStatement;
    	
    	if (searchToken.uuid != Constants.NULL_UUID)
    		selectStatement = "SELECT * FROM SimpleDatatype WHERE id = '" + searchToken.uuid + "'";
    	else
    		selectStatement = "SELECT * FROM SimpleDatatype WHERE name = '" + searchToken.type + "'";
    	
    	List<DbSimpleDatatype> returnVal = selectFromSimpleDatatypeTable(selectStatement);
    	
    	return returnVal.size() >= 1;	
    }
    
    public String getUUIDForBasicRecord(SearchToken searchToken) {
    	
    	String returnUUID = Constants.NULL_UUID;
		String selectStatement = "SELECT * FROM BasicDatatype WHERE name = '" + 
				searchToken.name.substring(0, 1).toUpperCase() + searchToken.name.substring(1) +  "'";
		
		List<DbBasicDatatype> returnVal = selectFromBasicDatatypeTable(selectStatement);
		
		if (returnVal.size() >= 1)
			returnUUID = returnVal.get(0).id;
		
		return returnUUID;
    }
    
    public boolean isBasicRecord(SearchToken searchToken) {
    	
    	String selectStatement;
    	
    	if (searchToken.uuid != Constants.NULL_UUID)
    		selectStatement = "SELECT * FROM BasicDatatype WHERE id = '" + searchToken.uuid + "'";
    	else
    		selectStatement = "SELECT * FROM BasicDatatype WHERE name = '" + searchToken.type + "'";
    	
    	List<DbBasicDatatype> returnVal = selectFromBasicDatatypeTable(selectStatement);
    	
    	return returnVal.size() >= 1;	
    }
    
    public String getUUIDForEnumeratedRecord(SearchToken searchToken) {
    	
    	String returnUUID = Constants.NULL_UUID;
		String selectStatement = "SELECT * FROM EnumeratedDatatype WHERE name = '" + searchToken.type + "'";
		
		List<DbEnumeratedDatatype> returnVal = selectFromEnumeratedDatatypeTable(selectStatement);
		
		if (returnVal.size() >= 1)
			returnUUID = returnVal.get(0).id;
		
		return returnUUID;
    }
    
    public boolean isEnumeratedRecord(SearchToken searchToken) {
    	
    	String selectStatement;
    	
    	if (searchToken.uuid != Constants.NULL_UUID)
    		selectStatement = "SELECT * FROM EnumeratedDatatype WHERE id = '" + searchToken.uuid + "'";
    	else
    		selectStatement = "SELECT * FROM EnumeratedDatatype WHERE name = '" + searchToken.type + "'";
    	
    	List<DbEnumeratedDatatype> returnVal = selectFromEnumeratedDatatypeTable(selectStatement);
    	
    	return returnVal.size() >= 1;	
    }
    
    private void run(String sql) throws SQLException {
    	
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }
}
