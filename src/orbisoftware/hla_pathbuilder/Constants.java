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

public class Constants {

	// Element type
	public enum Element { 
		Object,
		Interaction
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
		VariantRecordField,
		Basic
	}
	
	// Generator State Machine
	public enum GeneratorStateMachine {
		Undefined_State,
		Info_Block,
		Path_Def,
		MetaData
	}
}
