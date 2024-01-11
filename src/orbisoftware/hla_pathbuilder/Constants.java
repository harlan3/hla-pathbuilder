package orbisoftware.hla_pathbuilder;

public class Constants {

	public enum Element { 
		OBJECT,
		INTERACTION
	}
	
	// Table ID
	public enum TID {
		None,
		Object,
		Attribute,
		Interaction,
		Parameter,
		Simple,
		Enumerated,
		Array,
		FixedRecord,
		FixedRecordField,
		VariantRecord,
		VariantRecordField
	}
}
