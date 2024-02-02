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

import jargs.gnu.CmdLineParser;
import orbisoftware.hla_pathbuilder.HlaPathBuilder;

public class MainApplication {
	
	private static String fomFilename = "";
	private static String elementModel = "";
	
	private static void printUsage() {

		System.out.println("Usage: HlaPathBuilder [OPTION]...");
		System.out.println("Generate proto specs and mindmap files.");
		System.out.println();
		System.out.println("   -f, --fom          FOM file used by HLA federation");
		System.out.println("   -e, --element      Element model file which controls which of the FOM models are generated");
		System.out.println("   -h, --help         Show this help message");

	}

	public static void main(String[] args) {

		HlaPathBuilder hlaPathBuilder = new HlaPathBuilder();
		CmdLineParser parser = new CmdLineParser();

		CmdLineParser.Option fomOption = parser.addStringOption('f', "fom");
		CmdLineParser.Option elementOption = parser.addStringOption('e', "element");
		CmdLineParser.Option helpOption = parser.addBooleanOption('h', "help");

		try {
			parser.parse(args);
		} catch (CmdLineParser.OptionException e) {
			System.out.println(e.getMessage());
			printUsage();
			System.exit(0);
		}

		String fomValue = (String) parser.getOptionValue(fomOption);
		String elementValue = (String) parser.getOptionValue(elementOption);
		Boolean helpValue = (Boolean) parser.getOptionValue(helpOption);

		if ((helpValue != null) || (fomValue == null || elementValue == null)) {
			printUsage();
			System.exit(0);
		}

		fomFilename = fomValue;
		elementModel = elementValue;
		
		hlaPathBuilder.generateDatabase(fomFilename, elementModel);
	}
}