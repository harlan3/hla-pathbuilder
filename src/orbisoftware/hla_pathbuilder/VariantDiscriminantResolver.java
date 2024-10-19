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
