/*
 *  HLA PathBuilder
 *
 *  Copyright (C) 2024 Harlan Murphy
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VariantDiscriminantResolver {

	public enum Status { Uninitialized, Searching, Found };
	
	public Map<String, VariantDiscriminantEntry> variantDiscriminantMap = new ConcurrentHashMap<>();
	
	public void addNewVariantToResolve(String variantID) {
	
		if (!variantDiscriminantMap.containsKey(variantID))
			variantDiscriminantMap.put(variantID, new VariantDiscriminantEntry());
	}
	
	public Status variantDiscriminantStatus(String variantID) {
		
		if (variantDiscriminantMap.get(variantID) == null)
			return Status.Uninitialized;
		else
			return variantDiscriminantMap.get(variantID).variantDiscriminantStatus;

	}
	
	public boolean searchingForDiscriminant() {
		
		boolean foundSearchEntry = false;
		
		for (Map.Entry<String, VariantDiscriminantEntry> entry : variantDiscriminantMap.entrySet()) {
			
			String key = entry.getKey();
			VariantDiscriminantEntry variantDiscriminantEntry = entry.getValue();
			
			if (variantDiscriminantEntry.variantDiscriminantStatus == Status.Searching) {
				
				foundSearchEntry = true;
				break;
			}
		}
		
		return foundSearchEntry;
	}
	
	public void discriminantFound(String variantDiscriminantFieldID) {
		
		boolean foundSearchEntry = false;
		
		for (Map.Entry<String, VariantDiscriminantEntry> entry : variantDiscriminantMap.entrySet()) {
			
			String key = entry.getKey();
			VariantDiscriminantEntry variantDiscriminantEntry = entry.getValue();
			
			if (variantDiscriminantEntry.variantDiscriminantStatus == Status.Searching) {
				
				variantDiscriminantEntry.variantDiscriminantFieldID = variantDiscriminantFieldID;
				variantDiscriminantEntry.variantDiscriminantStatus = Status.Found;
				foundSearchEntry = true;
				break;
			}
		}
	}
	
	public boolean discriminantFieldExists(String variantDiscriminantFieldID) {
		
		boolean foundDiscriminant = false;
		
		for (Map.Entry<String, VariantDiscriminantEntry> entry : variantDiscriminantMap.entrySet()) {
			
			String key = entry.getKey();
			VariantDiscriminantEntry variantDiscriminantEntry = entry.getValue();
			
			if (variantDiscriminantEntry.variantDiscriminantStatus == Status.Found) {
				
				if (variantDiscriminantEntry.variantDiscriminantFieldID == variantDiscriminantFieldID)
					foundDiscriminant = true;
				else
					foundDiscriminant = false;
				
				break;
			}
		}
		
		return foundDiscriminant;
	}
}
