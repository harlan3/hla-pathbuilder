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

package orbisoftware.hla_pathbuilder.test;

import java.util.ArrayList;
import java.util.List;

import orbisoftware.hla_pathbuilder.*;
import orbisoftware.hla_pathbuilder.db_classes.*;

public class TestDatabaseAPI {

    public void testInsert1() {
    	
    	DatabaseAPI databaseAPI = new DatabaseAPI();
    	List<DbObject> list = new ArrayList<DbObject>();
    	
    	DbObject var1 = new DbObject();
    	DbObject var2 = new DbObject();
    	DbObject var3 = new DbObject();
    	
    	var1.id = "11111";
    	var1.name = "cat";
    	var1.path = "hello";
    	var1.parentObject = "99999";
    	
    	var2.id = "22222";
    	var2.name = "dog";
    	var2.path = "goodbye";
    	var2.parentObject = "88888";
    	
    	var3.id = "33333";
    	var3.name = "monkey";
    	var3.path = "good evening";
    	var3.parentObject = "77777";
    	
    	list.add(var1);
    	list.add(var2);
    	list.add(var3);
    	
    	databaseAPI.insertIntoObjectTable(list);
    }
    
    public void testSelect1() {
    	
    	DatabaseAPI databaseAPI = new DatabaseAPI();
    	String selectStatement = "SELECT * FROM Object";
    	
    	List<DbObject> list = databaseAPI.selectFromObjectTable(selectStatement);
    	
    	for (DbObject var : list) {
    		
    		System.out.println("id = " + var.id);
    		System.out.println("name = " + var.name);
    		System.out.println("path = " + var.path);
    		System.out.println("parentObject = " + var.parentObject);
    		System.out.println();
    	}
    	
    }
    
    public void testInsert2() {
    	
    	DatabaseAPI databaseAPI = new DatabaseAPI();
    	List<DbAttribute> list = new ArrayList<DbAttribute>();
    	
    	DbAttribute var1 = new DbAttribute();
    	DbAttribute var2 = new DbAttribute();
    	DbAttribute var3 = new DbAttribute();
    	
    	var1.id = "11111";
    	var1.index = 111;
    	var1.name = "cat";
    	var1.type = "catType";
    	var1.inherited = true;
    	var1.parentObject = "mom";
    	
    	var2.id = "22222";
    	var2.index = 222;
    	var2.name = "dog";
    	var2.type = "dogType";
    	var2.inherited = false;
    	var2.parentObject = "dad";
    	
    	var3.id = "33333";
    	var3.index = 333;
    	var3.name = "monkey";
    	var3.type = "monkeyType";
    	var3.inherited = true;
    	var3.parentObject = "grandma";
    	
    	list.add(var1);
    	list.add(var2);
    	list.add(var3);
    	
    	databaseAPI.insertIntoAttributeTable(list);
    }
    
    public void testSelect2() {
    	
    	DatabaseAPI databaseAPI = new DatabaseAPI();
    	String selectStatement = "SELECT * FROM Attribute";
    	
    	List<DbAttribute> list = databaseAPI.selectFromAttributeTable(selectStatement);
    	
    	for (DbAttribute var : list) {
    		
    		System.out.println("id = " + var.id);
    		System.out.println("index = " + var.index);
    		System.out.println("name = " + var.name);
    		System.out.println("type = " + var.type);
    		System.out.println("inherited = " + var.inherited);
    		System.out.println("parentObject = " + var.parentObject);
    		System.out.println();
    	}
    	
    }
    
    public void testInsert3() {
    	
    	DatabaseAPI databaseAPI = new DatabaseAPI();
    	List<DbInteraction> list = new ArrayList<DbInteraction>();
    	
    	DbInteraction var1 = new DbInteraction();
    	DbInteraction var2 = new DbInteraction();
    	DbInteraction var3 = new DbInteraction();
    	
    	var1.id = "11111";
    	var1.name = "cat";
    	var1.path = "hello";
    	var1.parentObject = "99999";
    	
    	var2.id = "22222";
    	var2.name = "dog";
    	var2.path = "goodbye";
    	var2.parentObject = "88888";
    	
    	var3.id = "33333";
    	var3.name = "monkey";
    	var3.path = "good evening";
    	var3.parentObject = "77777";
    	
    	list.add(var1);
    	list.add(var2);
    	list.add(var3);
    	
    	databaseAPI.insertIntoInteractionTable(list);
    }
    
    public void testSelect3() {
    	
    	DatabaseAPI databaseAPI = new DatabaseAPI();
    	String selectStatement = "SELECT * FROM Interaction";
    	
    	List<DbInteraction> list = databaseAPI.selectFromInteractionTable(selectStatement);
    	
    	for (DbInteraction var : list) {
    		
    		System.out.println("id = " + var.id);
    		System.out.println("name = " + var.name);
    		System.out.println("path = " + var.path);
    		System.out.println("parentObject = " + var.parentObject);
    		System.out.println();
    	}
    	
    }
    
    public void testInsert4() {
    	
    	DatabaseAPI databaseAPI = new DatabaseAPI();
    	List<DbParameter> list = new ArrayList<DbParameter>();
    	
    	DbParameter var1 = new DbParameter();
    	DbParameter var2 = new DbParameter();
    	DbParameter var3 = new DbParameter();
    	
    	var1.id = "11111";
    	var1.index = 111;
    	var1.name = "cat";
    	var1.type = "catType";
    	var1.inherited = true;
    	var1.parentObject = "mom";
    	
    	var2.id = "22222";
    	var2.index = 222;
    	var2.name = "dog";
    	var2.type = "dogType";
    	var2.inherited = false;
    	var2.parentObject = "dad";
    	
    	var3.id = "33333";
    	var3.index = 333;
    	var3.name = "monkey";
    	var3.type = "monkeyType";
    	var3.inherited = true;
    	var3.parentObject = "grandma";
    	
    	list.add(var1);
    	list.add(var2);
    	list.add(var3);
    	
    	databaseAPI.insertIntoParameterTable(list);
    }
    
    public void testSelect4() {
    	
    	DatabaseAPI databaseAPI = new DatabaseAPI();
    	String selectStatement = "SELECT * FROM Parameter";
    	
    	List<DbParameter> list = databaseAPI.selectFromParameterTable(selectStatement);
    	
    	for (DbParameter var : list) {
    		
    		System.out.println("id = " + var.id);
    		System.out.println("index = " + var.index);
    		System.out.println("name = " + var.name);
    		System.out.println("type = " + var.type);
    		System.out.println("inherited = " + var.inherited);
    		System.out.println("parentObject = " + var.parentObject);
    		System.out.println();
    	}
    	
    }
    
    public void testInsert5() {
    	
    	DatabaseAPI databaseAPI = new DatabaseAPI();
    	List<DbSimpleDatatype> list = new ArrayList<DbSimpleDatatype>();
    	
    	DbSimpleDatatype var1 = new DbSimpleDatatype();
    	DbSimpleDatatype var2 = new DbSimpleDatatype();
    	DbSimpleDatatype var3 = new DbSimpleDatatype();
    	
    	var1.id = "11111";
    	var1.name = "cat";
    	var1.type = "catType";
    	
    	var2.id = "22222";
    	var2.name = "dog";
    	var2.type = "dogType";
    	
    	var3.id = "33333";
    	var3.name = "monkey";
    	var3.type = "monkeyType";
    	
    	list.add(var1);
    	list.add(var2);
    	list.add(var3);
    	
    	databaseAPI.insertIntoSimpleDatatypeTable(list);
    }
    
    public void testSelect5() {
    	
    	DatabaseAPI databaseAPI = new DatabaseAPI();
    	String selectStatement = "SELECT * FROM SimpleDatatype";
    	
    	List<DbSimpleDatatype> list = databaseAPI.selectFromSimpleDatatypeTable(selectStatement);
    	
    	for (DbSimpleDatatype var : list) {
    		
    		System.out.println("id = " + var.id);
    		System.out.println("name = " + var.name);
    		System.out.println("type = " + var.type);
    		System.out.println();
    	}
    	
    }
    
    public void testInsert6() {
    	
    	DatabaseAPI databaseAPI = new DatabaseAPI();
    	List<DbEnumeratedDatatype> list = new ArrayList<DbEnumeratedDatatype>();
    	
    	DbEnumeratedDatatype var1 = new DbEnumeratedDatatype();
    	DbEnumeratedDatatype var2 = new DbEnumeratedDatatype();
    	DbEnumeratedDatatype var3 = new DbEnumeratedDatatype();
    	
    	var1.id = "11111";
    	var1.name = "cat";
    	var1.type = "catType";
    	
    	var2.id = "22222";
    	var2.name = "dog";
    	var2.type = "dogType";
    	
    	var3.id = "33333";
    	var3.name = "monkey";
    	var3.type = "monkeyType";
    	
    	list.add(var1);
    	list.add(var2);
    	list.add(var3);
    	
    	databaseAPI.insertIntoEnumeratedDatatypeTable(list);
    }
    
    public void testSelect6() {
    	
    	DatabaseAPI databaseAPI = new DatabaseAPI();
    	String selectStatement = "SELECT * FROM EnumeratedDatatype";
    	
    	List<DbEnumeratedDatatype> list = databaseAPI.selectFromEnumeratedDatatypeTable(selectStatement);
    	
    	for (DbEnumeratedDatatype var : list) {
    		
    		System.out.println("id = " + var.id);
    		System.out.println("name = " + var.name);
    		System.out.println("type = " + var.type);
    		System.out.println();
    	}
    	
    }
    
    public void testInsert7() {
    	
    	DatabaseAPI databaseAPI = new DatabaseAPI();
    	List<DbArrayDatatype> list = new ArrayList<DbArrayDatatype>();
    	
    	DbArrayDatatype var1 = new DbArrayDatatype();
    	DbArrayDatatype var2 = new DbArrayDatatype();
    	DbArrayDatatype var3 = new DbArrayDatatype();
    	
    	var1.id = "11111";
    	var1.name = "cat";
    	var1.type = "catType";
    	var1.cardinality = "1";
    	
    	var2.id = "22222";
    	var2.name = "dog";
    	var2.type = "dogType";
    	var2.cardinality = "2";
    	
    	var3.id = "33333";
    	var3.name = "monkey";
    	var3.type = "monkeyType";
    	var3.cardinality = "3";
    	
    	list.add(var1);
    	list.add(var2);
    	list.add(var3);
    	
    	databaseAPI.insertIntoArrayDatatypeTable(list);
    }
    
    public void testSelect7() {
    	
    	DatabaseAPI databaseAPI = new DatabaseAPI();
    	String selectStatement = "SELECT * FROM ArrayDatatype";
    	
    	List<DbArrayDatatype> list = databaseAPI.selectFromArrayDatatypeTable(selectStatement);
    	
    	for (DbArrayDatatype var : list) {
    		
    		System.out.println("id = " + var.id);
    		System.out.println("name = " + var.name);
    		System.out.println("type = " + var.type);
    		System.out.println("cardinality = " + var.cardinality);
    		System.out.println();
    	}
    	
    }
    
    public void testInsert8() {
    	
    	DatabaseAPI databaseAPI = new DatabaseAPI();
    	List<DbFixedRecordDatatype> list = new ArrayList<DbFixedRecordDatatype>();
    	
    	DbFixedRecordDatatype var1 = new DbFixedRecordDatatype();
    	DbFixedRecordDatatype var2 = new DbFixedRecordDatatype();
    	DbFixedRecordDatatype var3 = new DbFixedRecordDatatype();
    	
    	var1.id = "11111";
    	var1.name = "cat";
    	
    	var2.id = "22222";
    	var2.name = "dog";
    	
    	var3.id = "33333";
    	var3.name = "monkey";
    	
    	list.add(var1);
    	list.add(var2);
    	list.add(var3);
    	
    	databaseAPI.insertIntoFixedRecordDatatypeTable(list);
    }
    
    public void testSelect8() {
    	
    	DatabaseAPI databaseAPI = new DatabaseAPI();
    	String selectStatement = "SELECT * FROM FixedRecordDatatype";
    	
    	List<DbFixedRecordDatatype> list = databaseAPI.selectFromFixedRecordDatatypeTable(selectStatement);
    	
    	for (DbFixedRecordDatatype var : list) {
    		
    		System.out.println("id = " + var.id);
    		System.out.println("name = " + var.name);
    		System.out.println();
    	}
    	
    }
    
    public void testInsert9() {
    	
    	DatabaseAPI databaseAPI = new DatabaseAPI();
    	List<DbFixedRecordField> list = new ArrayList<DbFixedRecordField>();
    	
    	DbFixedRecordField var1 = new DbFixedRecordField();
    	DbFixedRecordField var2 = new DbFixedRecordField();
    	DbFixedRecordField var3 = new DbFixedRecordField();
    	
    	var1.id = "11111";
    	var1.index = 111;
    	var1.name = "cat";
    	var1.type = "catType";
    	var1.parentObject = "mom";
    	
    	var2.id = "22222";
    	var2.index = 222;
    	var2.name = "dog";
    	var2.type = "dogType";
    	var2.parentObject = "dad";
    	
    	var3.id = "33333";
    	var3.index = 333;
    	var3.name = "monkey";
    	var3.type = "monkeyType";
    	var3.parentObject = "grandma";
    	
    	list.add(var1);
    	list.add(var2);
    	list.add(var3);
    	
    	databaseAPI.insertIntoFixedRecordFieldTable(list);
    }
    
    public void testSelect9() {
    	
    	DatabaseAPI databaseAPI = new DatabaseAPI();
    	String selectStatement = "SELECT * FROM FixedRecordField";
    	
    	List<DbFixedRecordField> list = databaseAPI.selectFromFixedRecordFieldTable(selectStatement);
    	
    	for (DbFixedRecordField var : list) {
    		
    		System.out.println("id = " + var.id);
    		System.out.println("index = " + var.index);
    		System.out.println("name = " + var.name);
    		System.out.println("type = " + var.type);
    		System.out.println("parentObject = " + var.parentObject);
    		System.out.println();
    	}
    	
    }

    public void testInsert10() {
    	
    	DatabaseAPI databaseAPI = new DatabaseAPI();
    	List<DbVariantRecordDatatype> list = new ArrayList<DbVariantRecordDatatype>();
    	
    	DbVariantRecordDatatype var1 = new DbVariantRecordDatatype();
    	DbVariantRecordDatatype var2 = new DbVariantRecordDatatype();
    	DbVariantRecordDatatype var3 = new DbVariantRecordDatatype();
    	
    	var1.id = "11111";
    	var1.name = "cat";
    	
    	var2.id = "22222";
    	var2.name = "dog";
    	
    	var3.id = "33333";
    	var3.name = "monkey";
    	
    	list.add(var1);
    	list.add(var2);
    	list.add(var3);
    	
    	databaseAPI.insertIntoVariantRecordDatatypeTable(list);
    }
    
    public void testSelect10() {
    	
    	DatabaseAPI databaseAPI = new DatabaseAPI();
    	String selectStatement = "SELECT * FROM VariantRecordDatatype";
    	
    	List<DbVariantRecordDatatype> list = databaseAPI.selectFromVariantRecordDatatypeTable(selectStatement);
    	
    	for (DbVariantRecordDatatype var : list) {
    		
    		System.out.println("id = " + var.id);
    		System.out.println("name = " + var.name);
    		System.out.println();
    	}
    	
    }
    
    public void testInsert11() {
    	
    	DatabaseAPI databaseAPI = new DatabaseAPI();
    	List<DbVariantRecordField> list = new ArrayList<DbVariantRecordField>();
    	
    	DbVariantRecordField var1 = new DbVariantRecordField();
    	DbVariantRecordField var2 = new DbVariantRecordField();
    	DbVariantRecordField var3 = new DbVariantRecordField();
    	
    	var1.id = "11111";
    	var1.index = 111;
    	var1.name = "cat";
    	var1.type = "catType";
    	var1.discriminant = true;
    	var1.alternative = true;
    	var1.parentObject = "mom";
    	
    	var2.id = "22222";
    	var2.index = 222;
    	var2.name = "dog";
    	var2.type = "dogType";
    	var2.discriminant = true;
    	var2.alternative = true;
    	var2.parentObject = "dad";
    	
    	var3.id = "33333";
    	var3.index = 333;
    	var3.name = "monkey";
    	var3.type = "monkeyType";
    	var3.discriminant = true;
    	var3.alternative = true;
    	var3.parentObject = "grandma";
    	
    	list.add(var1);
    	list.add(var2);
    	list.add(var3);
    	
    	databaseAPI.insertIntoVariantRecordFieldTable(list);
    }
    
    public void testSelect11() {
    	
    	DatabaseAPI databaseAPI = new DatabaseAPI();
    	String selectStatement = "SELECT * FROM VariantRecordField";
    	
    	List<DbVariantRecordField> list = databaseAPI.selectFromVariantRecordFieldTable(selectStatement);
    	
    	for (DbVariantRecordField var : list) {
    		
    		System.out.println("id = " + var.id);
    		System.out.println("index = " + var.index);
    		System.out.println("name = " + var.name);
    		System.out.println("type = " + var.type);
    		System.out.println("discriminant = " + var.discriminant);
    		System.out.println("alternative = " + var.alternative);
    		System.out.println("parentObject = " + var.parentObject);
    		System.out.println();
    	}
    	
    }
    
    public static void main(String[] args)
    {
    	DatabaseAPI databaseAPI = new DatabaseAPI();
    	TestDatabaseAPI testDatabase = new TestDatabaseAPI();
    	
    	databaseAPI.initDatabase();
    	databaseAPI.createTables();
    	
    	testDatabase.testInsert1();
    	testDatabase.testSelect1();
    	
    	//testDatabase.testInsert2();
    	//testDatabase.testSelect2();
    	
    	//testDatabase.testInsert3();
    	//testDatabase.testSelect3();
    	
    	//testDatabase.testInsert4();
    	//testDatabase.testSelect4();
    	
    	//testDatabase.testInsert5();
    	//testDatabase.testSelect5();
    	
    	//testDatabase.testInsert6();
    	//testDatabase.testSelect6();   
    	
    	//testDatabase.testInsert7();
    	//testDatabase.testSelect7();    
    	
    	//testDatabase.testInsert8();
    	//testDatabase.testSelect8(); 
    	
    	//testDatabase.testInsert9();
    	//testDatabase.testSelect9(); 
    	
    	//testDatabase.testInsert10();
    	//testDatabase.testSelect10(); 
    	
    	//testDatabase.testInsert11();
    	//testDatabase.testSelect11(); 
    }
}
