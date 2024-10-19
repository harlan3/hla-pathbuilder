package orbisoftware.hla_pathbuilder;

public class VariantDiscriminantEntry {
	
	public String variantID;
	public VariantDiscriminantResolver.Status variantDiscriminantStatus;
	public String variantDiscriminantFieldID;
	
	public VariantDiscriminantEntry() {
		
		variantID = "";
		variantDiscriminantStatus = VariantDiscriminantResolver.Status.Searching;
		variantDiscriminantFieldID = "";
	}
}
