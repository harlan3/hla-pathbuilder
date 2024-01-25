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
import java.util.List;

import org.apache.commons.io.FileUtils;

public class CodeGenerator {

	CodeGenerator() {}
	
	private int currentLine;
	private int lastLine;
	
	public void generateFromFile(String filename, Constants.Element element) {
		
		try {
			File file = null;
			
			if (element == Constants.Element.Object)
				file = new File(System.getProperty("user.dir") + File.separator
					+ HlaPathBuilder.protocolSpecDir + File.separator + "Objects" + File.separator + filename);
			else if (element == Constants.Element.Interaction)
				file = new File(System.getProperty("user.dir") + File.separator
						+ HlaPathBuilder.protocolSpecDir + File.separator + "Interactions" + File.separator + filename);
			
			List<String> lines = FileUtils.readLines(file, "utf-8");
			
			lastLine = lines.size();	
			
			int x=0;
			x++;
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
