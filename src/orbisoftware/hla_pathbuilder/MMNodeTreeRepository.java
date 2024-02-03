package orbisoftware.hla_pathbuilder;

import java.util.HashMap;

public class MMNodeTreeRepository {

	private static MMNodeTreeRepository instance;
	private HashMap<String, NodeTree> repository = new HashMap<String, NodeTree>();

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

	public void put(String name, NodeTree nodeTree) {

		repository.put(name, nodeTree);
	}

	public NodeTree get(String name) {

		return repository.get(name);
	}
}
