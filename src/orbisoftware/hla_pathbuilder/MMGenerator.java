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

import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;

import orbisoftware.hla_pathbuilder.Constants.GeneratorStateMachine;
import orbisoftware.hla_pathbuilder.db_classes.DbArrayDatatype;

public class MMGenerator {

	private GeneratorStateMachine stateMachine;

	public static boolean haveRunOnce = false;

	private DatabaseAPI databaseAPI;
	public static final String mmSpecDir = "mm_specs";
	private String classNameFull;
	private String classNameShort;
	private String classHandle;

	private NodeElement metaDataNode;
	
	private String attributesTag;
	private String parametersTag;

	private List<String> documentLines;

	private String nextSquashAndMergeElement = "";
	private int nextSquashAndMergeElementNum = 0;
	private boolean mindMapMode = true;
	
	private NodeTree nodeTree;

	MMGenerator() {

	}

	private void updateStateMachine(String line) {

		if (line.contains("<info>")) {
			stateMachine = GeneratorStateMachine.Info_Block;
		} else if (line.contains("</info>")) {
			stateMachine = GeneratorStateMachine.Undefined_State;
		} else if (line.contains("<pathDef")) {
			stateMachine = GeneratorStateMachine.Path_Def;
		} else if (line.contains("</pathDef")) {
			stateMachine = GeneratorStateMachine.Undefined_State;
		} else if (line.contains("<metaData>")) {
			stateMachine = GeneratorStateMachine.MetaData;
		} else if (line.contains("</metaData>")) {
			stateMachine = GeneratorStateMachine.Undefined_State;
		}
	}
	
	private String queryArrayComponents(String element) {

		String returnVal = "";
		String elementTokens[] = element.split(" ");
		String arrayGUID = elementTokens[elementTokens.length - 1];

		String selectStatement = "SELECT * FROM ArrayDatatype where id = '" + arrayGUID + "'";

		List<DbArrayDatatype> list = databaseAPI.selectFromArrayDatatypeTable(selectStatement);

		for (DbArrayDatatype var : list) {

			/*
			 System.out.println("id = " + var.id); 
			 System.out.println("name = " + var.name); 
			 System.out.println("type = " + var.type);
			 System.out.println("cardinality = " + var.cardinality);
			 System.out.println("encoding = " + var.encoding); 
			 System.out.println();
			 */

			// Ignore 2nd field if it contains Array text
			if (elementTokens[1].contains("Array"))
				returnVal = var.name + " | " + "type=\"" + var.type + "\" cardinality=\"" + var.cardinality + "\" encoding=\""
						+ var.encoding + "\" | " + "TID=\"Array\"" + " | " + elementTokens[5];
			else
				returnVal = var.name + " " + elementTokens[1] + " | " + "type=\"" + var.type + "\" cardinality=\"" + var.cardinality + "\" encoding=\""
					+ var.encoding + "\" | " + "TID=\"Array\"" + " | " + elementTokens[5];
		}

		return returnVal;
	}

	private String squashAndMergeSimpleEnum(String basicElement, int basicElementNum) {

		String returnVal = "";

		if (basicElementNum != (nextSquashAndMergeElementNum + 1)) {

			System.out.println("Something went wrong. SquashAndMerge failed in MMGenerator.");
			return basicElement;
		}

		String simpleEnumTokens[] = nextSquashAndMergeElement.split("\\|");
		String basicTokens[] = basicElement.split("\\|");

		String simpleEnumSubTokens[] = simpleEnumTokens[0].split(" ");
		String basicSubTokens[] = basicTokens[0].split(" ");

		returnVal = basicSubTokens[0] + " " + simpleEnumSubTokens[1] + " | " + "TID=\"Basic\"" + " | "
				+ simpleEnumTokens[2];
		
		// Ignore bogus basic element value
		if (simpleEnumSubTokens[1].contains("Array"))
			returnVal = "";

		return returnVal;
	}

	public void updateElementTreeFromLine(String line, boolean formatted) {

		String elementTokens[] = line.split(",");
		NodeElement cursor = nodeTree.root;

		String prevElementString = "";

		cursor = nodeTree.root;

		for (int i = 0; i < elementTokens.length; i++) {

			String nextElementString;
			
			if (!formatted) {
				nextElementString = elementTokens[i].replaceAll("[\\[\\]]", "").trim();
				nextElementString = nextElementString.replaceAll("[()]", "");
			} else
				nextElementString = elementTokens[i];

			if (nextElementString.contains("TID=\"Array\"")) {

				prevElementString = nextElementString;
				nextElementString = queryArrayComponents(nextElementString);
			} else if (nextElementString.contains("TID=\"Simple\"") || nextElementString.contains("TID=\"Enumerated\"")) {

				nextSquashAndMergeElement = nextElementString;
				nextSquashAndMergeElementNum = i;
				prevElementString = nextElementString;
				continue;
			} else if (nextElementString.contains("TID=\"Basic\"")) {

				if (prevElementString.contains("TID=\"Simple\"") || prevElementString.contains("TID=\"Enumerated\"")) {
					
					nextElementString = squashAndMergeSimpleEnum(nextElementString, i);
				
					// Ignore bogus value
					if (nextElementString.equals(""))
						continue;
				}
				
				if (prevElementString.contains("TID=\"Array\"")) {
					
					String fields[] = nextElementString.split(" ");
					
					// Don't display these string basic types
					if (Character.isUpperCase(fields[1].charAt(0))) {
						
						if (fields[0].contains("HLAASCIIchar") || fields[1].contains("Array"))
							continue;
					}
				}
				
			} else if (nextElementString.contains("TID=\"FixedRecord\"")) {
				
				if (prevElementString.contains("TID=\"Array\"")) {
					
					// Remove bogus field name. Struct field only.
					nextElementString = nextElementString.replaceAll(" \\|", "");
					String fields[] = nextElementString.split(" ");
					nextElementString = fields[0] + " | " + fields[2] + " | " + fields[3] + " | " + fields[4];
				}
			}

			if (i > 0)
				nextElementString = " " + nextElementString;

			if (nodeTree.childNodeExists(nodeTree.root, nextElementString))
				cursor = nodeTree.getNode(nodeTree.root, nextElementString);
			else {
				if (cursor != null)
					cursor = nodeTree.insertNode(cursor, nextElementString, formatted);
			}
		}
	}

	public void generateFromFile(String filename, Constants.Element element) {

		try {
			File inputFile = null;
			nodeTree = new NodeTree(Constants.NULL_UUID);
			String treeName[] = filename.split("\\.");
			
			// Store trees for use outside of HLA Pathbuilder
			MMNodeTreeRepository.getInstance().putNodeTree(treeName[0], nodeTree);
			
			if (!haveRunOnce) {

				File specDir = new File(System.getProperty("user.dir") + File.separator + mmSpecDir);
				File objDir = new File(
						System.getProperty("user.dir") + File.separator + mmSpecDir + File.separator + "Objects");
				File intDir = new File(
						System.getProperty("user.dir") + File.separator + mmSpecDir + File.separator + "Interactions");

				if (Files.exists(specDir.toPath()))
					FileUtils.forceDelete(specDir);

				FileUtils.forceMkdir(specDir);
				FileUtils.forceMkdir(objDir);
				FileUtils.forceMkdir(intDir);

				haveRunOnce = true;
			}

			if (element == Constants.Element.Object)
				inputFile = new File(System.getProperty("user.dir") + File.separator + HlaPathBuilder.protocolSpecDir
						+ File.separator + "Objects" + File.separator + filename);
			else if (element == Constants.Element.Interaction)
				inputFile = new File(System.getProperty("user.dir") + File.separator + HlaPathBuilder.protocolSpecDir
						+ File.separator + "Interactions" + File.separator + filename);

			String outputFilename = filename.replaceAll(".txt", ".mm");
			PrintStream outputStream = null;

			if (element == Constants.Element.Object) {

				outputStream = new PrintStream(new File(System.getProperty("user.dir") + File.separator + mmSpecDir
						+ File.separator + "Objects" + File.separator + outputFilename));
			} else if (element == Constants.Element.Interaction) {

				outputStream = new PrintStream(new File(System.getProperty("user.dir") + File.separator + mmSpecDir
						+ File.separator + "Interactions" + File.separator + outputFilename));
			}

			PrintStream console = System.out;
			System.setOut(outputStream);

			documentLines = FileUtils.readLines(inputFile, "utf-8");
			
			// Create link at the root of the tree for MetaData

			for (String line : documentLines) {

				updateStateMachine(line);

				if (stateMachine == GeneratorStateMachine.Info_Block) {

					if (line.contains("name = ")) {
						classNameFull = line.split("=")[1].trim();
						classNameFull = classNameFull.replaceAll(".txt", "");

						classNameShort = classNameFull.split("_")[0];
					}

					if (line.contains("path =")) {
						classHandle = line.split("=")[1].trim();
						classHandle = classHandle.replaceAll("[\\[\\]]", ""); // remove brackets
						classHandle = classHandle.replaceAll("\\s+", ""); // remove whitespace
						classHandle = classHandle.replaceAll(",", "."); // replace commas with periods

						updateElementTreeFromLine("<map version=\"1.0.1\">", false);
						
						this.metaDataNode = nodeTree.insertNode(this.nodeTree.root, "MetaData", false);
						
						updateElementTreeFromLine("ID=\"" + UUID.randomUUID() + "\"" + " TEXT=\"" + classNameShort
								+ "\"" + " className=\"" + classNameFull + "\"" + " classHandle=\"" + classHandle + "\""
								+ " FOLDED=\"true\"" + ">", false);
					}
				}

				if (stateMachine == GeneratorStateMachine.Path_Def) {

					if (line.contains("Path:")) {
						String pathTokens[];
						String path = line.split(":")[1];
						path = path.replaceAll("[\\[\\]]", ""); // remove brackets
						path = path.replaceAll("\\s+", ""); // remove whitespace
						path = path.replaceAll(",", "."); // replace commas with periods
						pathTokens = path.split("\\.");

						String pathLine = "ID=\"" + UUID.randomUUID() + "\"" + " TEXT=\""
								+ pathTokens[(pathTokens.length - 1)] + "\"" + " path=\"" + path + "\""
								+ " FOLDED=\"true\"" + ">";
						updateElementTreeFromLine(pathLine, false);
					}

					if (!line.contains("<pathDef") && (!line.contains("</pathDef") && (!line.contains("Path:"))))
						updateElementTreeFromLine(line, false);
				}
				
				if (stateMachine == GeneratorStateMachine.MetaData) {
					
					if (line.contains("Attributes:")) {
						
						String lineTokens[] = line.split(":");
						attributesTag = "<attributes>" + lineTokens[1].replaceAll("[\\[\\]]", "").trim() + "</attributes>";
					}
					
					if (line.contains("Attributes Length:")) {
						
						String lineTokens[] = line.split(":");
						String attributesLengthTag = "<attributesLength>" + lineTokens[1].replaceAll("[\\[\\]]", "").trim() + "</attributesLength>";
						
						nodeTree.insertNode(this.metaDataNode, "<metaData>", false);
						nodeTree.insertNode(this.metaDataNode, "   " + attributesTag, false);
						nodeTree.insertNode(this.metaDataNode, "   " + attributesLengthTag, false);
						nodeTree.insertNode(this.metaDataNode, "</metaData>", false);
					}
					
					if (line.contains("Parameters:")) {
						
						String lineTokens[] = line.split(":");
						parametersTag = "<parameters>" + lineTokens[1].replaceAll("[\\[\\]]", "").trim() + "</parameters>";
					}
					
					if (line.contains("Parameters Length:")) {
						
						String lineTokens[] = line.split(":");
						String parametersLengthTag = "<parametersLength>" + lineTokens[1].replaceAll("[\\[\\]]", "").trim() + "</parametersLength>";
						
						nodeTree.insertNode(this.metaDataNode, "<metaData>", false);
						nodeTree.insertNode(this.metaDataNode, "   " + parametersTag, false);
						nodeTree.insertNode(this.metaDataNode, "   " + parametersLengthTag, false);
						nodeTree.insertNode(this.metaDataNode, "</metaData>", false);
					}		
				}
			}
			
			// two folding nodes at bottom to complete matching node tags
			updateElementTreeFromLine("   </node>", true);
			updateElementTreeFromLine("</node>", true);
			
			this.nodeTree.setDefaults();
			this.nodeTree.traverseTree(nodeTree.root, mindMapMode); // true specifies mindmap instead of xml format
			
			System.setOut(console);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setDatabase(DatabaseAPI databaseAPI) {

		this.databaseAPI = databaseAPI;
	}
}
