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

	public String getEncodingType(String typeName) {

		String returnVal = "Unknown";

		switch (typeName) {

		case "HLAASCIIchar":
			returnVal = "HLAASCIIchar";
			break;

		case "NullTerminatedASCIIString":
			returnVal = "NullTerminatedASCIIString";
			break;

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

		case "Integer16":
		case "UnsignedInteger16":
			returnVal = "HLAinteger16BE";
			break;

		case "Integer32":
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

	public String getClassFromEncodingType(String typeName) {

		String returnVal = "Unknown";

		switch (typeName) {

		case "HLAASCIIchar":
			returnVal = "Byte";
			break;

		case "NullTerminatedASCIIString":
			returnVal = "NullTerminatedASCIIString";
			break;

		case "RPRboolean":
			returnVal = "Octet";
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

		case "RPRunsignedInteger16BE":
		case "HLAinteger16BE":
			returnVal = "Short";
			break;

		case "RPRunsignedInteger32BE":
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
	
	public String getPrimitiveFromEncodingType(String typeName) {

		String returnVal = "Unknown";

		switch (typeName) {

		case "HLAASCIIchar":
			returnVal = "byte";
			break;

		case "NullTerminatedASCIIString":
			returnVal = "NullTerminatedASCIIString";
			break;

		case "RPRboolean":
			returnVal = "octet";
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

		case "RPRunsignedInteger16BE":
		case "HLAinteger16BE":
			returnVal = "short";
			break;

		case "RPRunsignedInteger32BE":
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
