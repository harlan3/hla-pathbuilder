package orbisoftware.hla_pathbuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MMNodeTreeRepository {

	private static MMNodeTreeRepository instance;
	private HashMap<String, NodeTree> repository = new HashMap<String, NodeTree>();
	private List<String> elementObjectList = new ArrayList<String>();
	private List<String> elementInteractionList = new ArrayList<String>();
	
	private MMNodeTreeRepository() {

		if (instance != null) {
			throw new Error();
		}
	}

	public static synchronized MMNodeTreeRepository getInstance() {
		
		if (instance == null) {
			instance = new MMNodeTreeRepository();
		}
		return instance;
	}

	public void putNodeTree(String name, NodeTree nodeTree) {

		repository.put(name, nodeTree);
	}

	public NodeTree getNodeTree(String name) {

		return repository.get(name);
	}
	
	public void addObjectName(String name) {
		
		elementObjectList.add(name);
	}
	
	public String getObjectName(int index) {
		
		return elementObjectList.get(index);
	}
	
	public void addInteractionName(String name) {
		
		elementInteractionList.add(name);
	}
	
	public void getInteractionName(int index) {
		
		elementInteractionList.get(index);
	}
}
