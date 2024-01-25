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

import java.util.ArrayList;
import java.util.List;

class NodeElement {
	
    List<NodeElement> children;
    
	String elementString = "";

    public NodeElement(String elementString) {
    	
        this.elementString = elementString;
        
        this.children = new ArrayList<>();
    }
    
    public boolean isNodeEqual(NodeElement nodeValue) {
    	
    	if (this.elementString.equals(nodeValue.elementString))
    		return true;
    	else
    		return false;
    }
    
    public boolean isNodeEqual(String nodeValue) {
    	
    	if (this.elementString.equals(nodeValue))
    		return true;
    	else
    		return false;
    }
}
