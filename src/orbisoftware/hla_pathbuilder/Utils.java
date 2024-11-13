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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.CaseUtils;

public class Utils {

	public String convertToCamelCase(String name) {

		String returnVal = "";

		if (name.equals("HLAobjectRoot"))
			return "hlaObjectRoot";
		if (name.equals("HLAinteractionRoot"))
			return "hlaInteractionRoot";
		else if (name.contains("_"))
			returnVal = CaseUtils.toCamelCase(name, false, '_');
		else if (name.contains("-"))
			returnVal = CaseUtils.toCamelCase(name, false, '-');
		else if (StringUtils.isAllLowerCase(name))
			returnVal = name;
		else if (StringUtils.isAllUpperCase(name))
			returnVal = name.toLowerCase();
		else if (Character.isLowerCase(name.charAt(0)))
			returnVal = name;
		else if (Character.isUpperCase(name.charAt(0)) && Character.isLowerCase(name.charAt(1)))
			returnVal = Character.toLowerCase(name.charAt(0)) + name.substring(1);
		else {
			boolean lowerFound = false;

			for (int i = 0; i < name.length(); i++) {
				if (Character.isUpperCase(name.charAt(i)) && !lowerFound)
					returnVal += Character.toLowerCase(name.charAt(i));
				else {
					lowerFound = true;
					returnVal += name.charAt(i);
				}
			}
		}

		return returnVal;
	}
	
	public String capitalizeFirstLetter(String name) {
		
		String modifiedString = Character.toUpperCase(name.charAt(0)) + name.substring(1);
		
		return modifiedString;
	}
	
	public String lowercaseFirstLetter(String name) {
		
		String modifiedString = Character.toLowerCase(name.charAt(0)) + name.substring(1);
		
		return modifiedString;
	}

	public String getEncodingType(String typeName) {

		String returnVal = "Unknown";

		switch (typeName) {

		case "HLAASCIIchar":
			returnVal = "HLAASCIIchar";
			break;

		case "Octet":
		case "RPRboolean": // Representation as HLAoctet in FOM
			returnVal = "HLAoctet";
			break;
			
		case "HLAboolean": // Representation as HLAinteger32BE in HLA standard
			returnVal = "HLAinteger32BE";
			break;

		case "Float32":
			returnVal = "HLAfloat32BE";
			break;

		case "Float64":
			returnVal = "HLAfloat64BE";
			break;

		case "Integer8":
		case "UnsignedInteger8":
			returnVal = "HLAinteger8BE";
			break;
			
		case "Integer16":
		case "UnsignedShort":
		case "UnsignedInteger16":
			returnVal = "HLAinteger16BE";
			break;

		case "Integer32":
		case "UnsignedInt":
		case "UnsignedInteger32":
			returnVal = "HLAinteger32BE";
			break;

		case "Integer64":
		case "UnsignedInteger64":
			returnVal = "HLAinteger64BE";
			break;
		}
		
		return returnVal;
	}
	
	public String convertFromRPRType(String typeName) {
		
		String returnVal = typeName; // In case passed in value is not a RPR type
		
		switch(typeName) {
		
		case "RPRunsignedInteger8BE":
			returnVal = "HLAinteger8BE";
			break;	
		
		case "RPRunsignedInteger16BE":
			returnVal = "HLAinteger16BE";
			break;
			
		case "RPRunsignedInteger32BE":
			returnVal = "HLAinteger32BE";
			break;
			
		case "RPRunsignedInteger64BE":
			returnVal = "HLAinteger64BE";
			break;
			
		}
		
		return returnVal;
	}
	
	public String getClassFromPrimitive(String primitiveName) {
		
		String returnVal = "Unknown";

		switch (primitiveName) {

		case "boolean":
			returnVal = "Boolean";
			break;

		case "float":
			returnVal = "Float";
			break;

		case "double":
			returnVal = "Double";
			break;

		case "byte":
			returnVal = "Byte";
			break;
			
		case "short":
			returnVal = "Short";
			break;

		case "int":
			returnVal = "Integer";
			break;

		case "long":
			returnVal = "Long";
			break;
		}
		
		return returnVal;
	}
	
	public String getPrimitiveFromClass(String primitiveName) {
		
		String returnVal = "Unknown";

		switch (primitiveName) {

		case "Boolean":
			returnVal = "boolean";
			break;

		case "Float":
			returnVal = "float";
			break;

		case "Double":
			returnVal = "double";
			break;

		case "Byte":
			returnVal = "byte";
			break;
			
		case "Short":
			returnVal = "short";
			break;

		case "Int":
		case "Integer":
			returnVal = "int";
			break;

		case "Long":
			returnVal = "long";
			break;
		}
		
		return returnVal;
	}

	public String getClassFromEncodingType(String typeName) {

		String returnVal = "Unknown";

		switch (typeName) {

		case "HLAASCIIchar":
		case "HLAoctet":
		case "RPRboolean":
			returnVal = "Byte";
			break;

		case "HLAboolean":
			returnVal = "Boolean";
			break;

		case "HLAfloat32BE":
			returnVal = "Float";
			break;

		case "HLAfloat64BE":
			returnVal = "Double";
			break;

		case "RPRunsignedInteger8BE":
		case "HLAinteger8BE":
			returnVal = "Byte";
			break;
			
		case "RPRunsignedInteger16BE":
		case "UnsignedShort":
		case "HLAinteger16BE":
			returnVal = "Short";
			break;

		case "RPRunsignedInteger32BE":
		case "UnsignedInt":
		case "HLAinteger32BE":
			returnVal = "Integer";
			break;

		case "RPRunsignedInteger64BE":
		case "HLAinteger64BE":
			returnVal = "Long";
			break;
		}
		
		return returnVal;
	}
	
	public boolean isPrimitiveClass(String typeName) {

		boolean returnVal = false;

		switch (typeName) {

		case "Byte":
			returnVal = true;
			break;

		case "Boolean":
			returnVal = true;
			break;

		case "Float":
			returnVal = true;
			break;

		case "Double":
			returnVal = true;
			break;

		case "Short":
			returnVal = true;
			break;

		case "Integer":
			returnVal = true;
			break;

		case "Long":
			returnVal = true;
			break;
		}
		
		return returnVal;
	}
	
	public boolean isPrimitiveHLAClass(String typeName) {

		boolean returnVal = false;

		switch (typeName) {

		case "HLAASCIIchar":
		case "HLAoctet":
		case "RPRboolean":
			returnVal = true;
			break;
			
		case "HLAboolean":
			returnVal = true;
			break;

		case "HLAfloat32BE":
			returnVal = true;
			break;

		case "HLAfloat64BE":
			returnVal = true;
			break;

		case "RPRunsignedInteger8BE":
		case "HLAinteger8BE":
			returnVal = true;
			break;
			
		case "RPRunsignedInteger16BE":
		case "UnsignedShort":
		case "HLAinteger16BE":
			returnVal = true;
			break;

		case "RPRunsignedInteger32BE":
		case "UnsignedInt":
		case "HLAinteger32BE":
			returnVal = true;
			break;

		case "RPRunsignedInteger64BE":
		case "HLAinteger64BE":
			returnVal = true;
			break;
		}
		
		return returnVal;
	}
	
	public String getDataValueFromPrimitiveClass(String primitiveClass) {
		
		String returnVal = "Unknown";

		switch (primitiveClass) {

		case "Byte":
			returnVal = "int8";
			break;

		case "Boolean":
			returnVal = "bool8";
			break;

		case "Float":
			returnVal = "float32";
			break;

		case "Double":
			returnVal = "double64";
			break;

		case "Short":
			returnVal = "int16";
			break;

		case "Integer":
			returnVal = "int32";
			break;

		case "Long":
			returnVal = "int64";
			break;
		}
		
		return returnVal;
	}

	public int getNumberBytesFromEncodingType(String typeName) {

		int returnVal = 0;

		switch (typeName) {

		case "HLAASCIIchar":
		case "HLAoctet":
		case "RPRboolean":
			returnVal = 1;
			break;

		case "HLAboolean":
			returnVal = 1;
			break;

		case "HLAfloat32BE":
			returnVal = 4;
			break;

		case "HLAfloat64BE":
			returnVal = 8;
			break;

		case "RPRunsignedInteger8BE":
		case "HLAinteger8BE":
			returnVal = 1;
			break;
			
		case "RPRunsignedInteger16BE":
		case "UnsignedShort":
		case "HLAinteger16BE":
			returnVal = 2;
			break;

		case "RPRunsignedInteger32BE":
		case "UnsignedInt":
		case "HLAinteger32BE":
			returnVal = 4;
			break;

		case "RPRunsignedInteger64BE":
		case "HLAinteger64BE":
			returnVal = 8;
			break;
		}
		
		return returnVal;
	}
	
	public int getNumberBytesFromPrimitiveType(String primitiveTypeName) {

		int returnVal = 0;

		switch (primitiveTypeName) {

		case "boolean":
			returnVal = 1;
			break;

		case "float":
			returnVal = 4;
			break;

		case "double":
			returnVal = 8;
			break;

		case "byte":
			returnVal = 1;
			break;
			
		case "short":
			returnVal = 2;
			break;

		case "int":
			returnVal = 4;
			break;

		case "long":
			returnVal = 8;
			break;
		}
		
		return returnVal;
	}
	
	public String getPrimitiveFromEncodingType(String typeName) {

		String returnVal = "Unknown";

		switch (typeName) {

		case "HLAASCIIchar":
		case "HLAoctet":
		case "RPRboolean":
			returnVal = "byte";
			break;
			
		case "HLAboolean":
			returnVal = "boolean";
			break;

		case "HLAfloat32BE":
			returnVal = "float";
			break;

		case "HLAfloat64BE":
			returnVal = "double";
			break;

		case "RPRunsignedInteger8BE":
		case "HLAinteger8BE":
			returnVal = "byte";
			break;
			
		case "RPRunsignedInteger16BE":
		case "UnsignedShort":
		case "HLAinteger16BE":
			returnVal = "short";
			break;

		case "RPRunsignedInteger32BE":
		case "UnsignedInt":
		case "HLAinteger32BE":
			returnVal = "int";
			break;

		case "RPRunsignedInteger64BE":
		case "HLAinteger64BE":
			returnVal = "long";
			break;
		}
		
		return returnVal;
	}
}
