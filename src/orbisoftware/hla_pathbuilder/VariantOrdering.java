package orbisoftware.hla_pathbuilder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import orbisoftware.hla_pathbuilder.db_classes.DbVariantOrderingDatatype;

public class VariantOrdering {

	public void populateTable() {

		DatabaseAPI databaseAPI = new DatabaseAPI();
		String filePath = "variant_ordering.txt";
		String line;

		try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {

			while ((line = reader.readLine()) != null) {

				String splitString[] = line.split("\\|");
				String variant;
				String discriminant;
				String ordering;

				// Parse valid lines and ignore comments
				if (splitString.length > 1 && line.charAt(0) != '#') {

					variant = splitString[0].trim();
					discriminant = splitString[1].trim();
					ordering = splitString[2].trim();

					// Insert variant ordering into database
					List<DbVariantOrderingDatatype> list = new ArrayList<DbVariantOrderingDatatype>();
					DbVariantOrderingDatatype var = new DbVariantOrderingDatatype();

					var.id = UUID.randomUUID().toString();
					var.variant = variant;
					var.discriminant = discriminant;
					var.ordering = ordering;

					list.add(var);

					databaseAPI.insertIntoVariantOrdering(list);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
