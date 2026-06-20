#!/usr/bin/env python3
"""
hla1516_fom_to_fed.py

Best-effort converter from an HLA 1516 / 1516e FOM XML document to an
HLA 1.3 FED (Lisp-like Federation Execution Data) file.

What it converts
----------------
* Object-class hierarchy and attributes
* Interaction-class hierarchy and parameters
* Attribute/interaction transportation and ordering
* HLA 1516 dimensions into generated HLA 1.3 routing spaces

Important limitations
---------------------
HLA 1.3 FED files do not carry the rich HLA 1516 datatype model. The converter
therefore writes datatype information into comments only. Your HLA 1.3
federates must use mutually agreed byte encodings for all attributes/parameters.

HLA 1516 dimensions are reusable and attach independently to model items.
HLA 1.3 instead binds each object attribute or interaction to one routing space.
This tool creates one generated routing space per unique ordered dimension list.
Review the output whenever DDM is used.

Usage
-----
    python hla1516_fom_to_fed.py input.xml output.fed
    python hla1516_fom_to_fed.py input.xml output.fed --federation MyFederation

The output syntax follows the conventional HLA 1.3 FED format used by many
RTIs. Validate the result with the specific HLA 1.3 RTI that will load it.
"""

from __future__ import annotations

import argparse
import re
import sys
import xml.etree.ElementTree as ET
from dataclasses import dataclass, field
from pathlib import Path
from typing import Iterable, Optional


# ---------------------------------------------------------------------------
# XML helpers -- deliberately namespace agnostic so the script works with
# both older IEEE 1516 XML and most IEEE 1516e FOM XML schemas.
# ---------------------------------------------------------------------------

def lname(tag: str) -> str:
    """Return an XML element's local (namespace-free) name."""
    return tag.rsplit("}", 1)[-1]


def children(elem: ET.Element, name: str) -> list[ET.Element]:
    return [child for child in list(elem) if lname(child.tag) == name]


def first_child(elem: ET.Element, *names: str) -> Optional[ET.Element]:
    wanted = set(names)
    for child in list(elem):
        if lname(child.tag) in wanted:
            return child
    return None


def text_of(elem: ET.Element, *names: str, default: str = "") -> str:
    child = first_child(elem, *names)
    if child is None or child.text is None:
        return default
    return " ".join(child.text.split())


def descendants(elem: ET.Element, name: str) -> Iterable[ET.Element]:
    for child in elem.iter():
        if child is not elem and lname(child.tag) == name:
            yield child


def clean_name(value: str, fallback: str) -> str:
    value = " ".join((value or "").split())
    return value if value else fallback


# ---------------------------------------------------------------------------
# Internal model
# ---------------------------------------------------------------------------

@dataclass
class Field:
    name: str
    datatype: str = ""
    transportation: str = ""
    order: str = ""
    dimensions: list[str] = field(default_factory=list)
    semantics: str = ""


@dataclass
class ObjectClass:
    name: str
    attributes: list[Field] = field(default_factory=list)
    children: list["ObjectClass"] = field(default_factory=list)


@dataclass
class InteractionClass:
    name: str
    parameters: list[Field] = field(default_factory=list)
    transportation: str = ""
    order: str = ""
    dimensions: list[str] = field(default_factory=list)
    children: list["InteractionClass"] = field(default_factory=list)


@dataclass
class Dimension:
    name: str
    upper_bound: str = ""


@dataclass
class Model:
    object_roots: list[ObjectClass] = field(default_factory=list)
    interaction_roots: list[InteractionClass] = field(default_factory=list)
    dimensions: dict[str, Dimension] = field(default_factory=dict)
    warnings: list[str] = field(default_factory=list)


# ---------------------------------------------------------------------------
# FOM parsing
# ---------------------------------------------------------------------------

def parse_dimensions(root: ET.Element, model: Model) -> None:
    """
    Handle common FOM representations:
      <dimensions><dimension><name>...</name><upperBound>...</upperBound>
    """
    for dim in descendants(root, "dimension"):
        # Ignore <dimension> references under <dimensions>; only declarations
        # normally have a direct <name> child.
        name = text_of(dim, "name")
        if not name:
            continue
        upper = text_of(dim, "upperBound", "upper-bound", "upperbound")
        if name not in model.dimensions:
            model.dimensions[name] = Dimension(name=name, upper_bound=upper)


def parse_dimension_refs(owner: ET.Element) -> list[str]:
    result: list[str] = []
    dims = first_child(owner, "dimensions")
    if dims is None:
        return result

    for dim in children(dims, "dimension"):
        # Reference can be text directly or <dimension><name>...</name>.
        value = clean_name((dim.text or ""), "")
        if not value:
            value = text_of(dim, "name")
        if value and value not in result:
            result.append(value)
    return result


def parse_field(elem: ET.Element, kind: str) -> Field:
    return Field(
        name=clean_name(text_of(elem, "name"), f"Unnamed{kind}"),
        datatype=text_of(elem, "dataType", "datatype"),
        transportation=text_of(elem, "transportation"),
        order=text_of(elem, "order"),
        dimensions=parse_dimension_refs(elem),
        semantics=text_of(elem, "semantics"),
    )


def parse_object_class(elem: ET.Element) -> ObjectClass:
    result = ObjectClass(name=clean_name(text_of(elem, "name"), "UnnamedObjectClass"))
    result.attributes = [parse_field(a, "Attribute") for a in children(elem, "attribute")]
    result.children = [parse_object_class(c) for c in children(elem, "objectClass")]
    return result


def parse_interaction_class(elem: ET.Element) -> InteractionClass:
    result = InteractionClass(
        name=clean_name(text_of(elem, "name"), "UnnamedInteractionClass"),
        transportation=text_of(elem, "transportation"),
        order=text_of(elem, "order"),
        dimensions=parse_dimension_refs(elem),
    )
    result.parameters = [parse_field(p, "Parameter") for p in children(elem, "parameter")]
    result.children = [parse_interaction_class(c) for c in children(elem, "interactionClass")]
    return result


def direct_class_roots(root: ET.Element, section_name: str, class_name: str) -> list[ET.Element]:
    """
    Find class roots contained in <objects> or <interactions> sections without
    accidentally selecting nested subclasses a second time.
    """
    roots: list[ET.Element] = []
    for section in descendants(root, section_name):
        roots.extend(children(section, class_name))
    return roots


def parse_fom(xml_path: Path) -> Model:
    try:
        root = ET.parse(xml_path).getroot()
    except ET.ParseError as exc:
        raise ValueError(f"{xml_path}: XML parse error: {exc}") from exc

    model = Model()
    parse_dimensions(root, model)

    obj_roots = direct_class_roots(root, "objects", "objectClass")
    int_roots = direct_class_roots(root, "interactions", "interactionClass")

    # Some older FOM XML serializations omit the wrapper sections.
    if not obj_roots:
        obj_roots = [x for x in descendants(root, "objectClass")
                     if lname(x.getparent().tag) != "objectClass"] if False else []
    if not obj_roots:
        model.warnings.append("No <objects>/<objectClass> hierarchy found.")
    else:
        model.object_roots = [parse_object_class(x) for x in obj_roots]

    if not int_roots:
        model.warnings.append("No <interactions>/<interactionClass> hierarchy found.")
    else:
        model.interaction_roots = [parse_interaction_class(x) for x in int_roots]

    return model


# ---------------------------------------------------------------------------
# FED output
# ---------------------------------------------------------------------------

def fed_atom(value: str) -> str:
    """
    FED identifiers are conventionally bare atoms. Quote only when needed,
    preserving common class names like ObjectRoot and HLAobjectRoot.
    """
    value = clean_name(value, "Unnamed")
    if re.fullmatch(r"[A-Za-z_][A-Za-z0-9_.:-]*", value):
        return value
    return '"' + value.replace("\\", "\\\\").replace('"', '\\"') + '"'


def comment(text: str) -> str:
    return "; " + text.replace("\n", " ").replace("\r", " ")


def map_transport(value: str) -> str:
    v = value.strip().lower().replace("_", "").replace("-", "")
    if v in {"hlabesteffort", "besteffort", "best effort"}:
        return "best_effort"
    # HLAreliable, empty, and unknown vendor values become reliable.
    return "reliable"


def map_order(value: str) -> str:
    v = value.strip().lower().replace("_", "").replace("-", "")
    if v in {"timestamp", "timestamporder", "timestamped"}:
        return "timestamp"
    return "receive"


def is_root_object_name(name: str) -> bool:
    return name.lower() in {"objectroot", "hlaobjectroot"}


def is_root_interaction_name(name: str) -> bool:
    return name.lower() in {"interactionroot", "hlainteractionroot"}


class FedWriter:
    def __init__(self, model: Model, federation_name: str) -> None:
        self.model = model
        self.federation_name = federation_name
        self.lines: list[str] = []
        self.space_for_dimensions: dict[tuple[str, ...], str] = {}
        self.used_space_names: set[str] = set()
        self._discover_spaces()

    def _discover_spaces(self) -> None:
        def register(dims: list[str]) -> None:
            key = tuple(dims)
            if not key or key in self.space_for_dimensions:
                return
            base = "Space_" + "_".join(re.sub(r"[^A-Za-z0-9_]", "_", d) for d in key)
            name = base
            index = 2
            while name in self.used_space_names:
                name = f"{base}_{index}"
                index += 1
            self.used_space_names.add(name)
            self.space_for_dimensions[key] = name

        def scan_object(c: ObjectClass) -> None:
            for a in c.attributes:
                register(a.dimensions)
            for child in c.children:
                scan_object(child)

        def scan_interaction(c: InteractionClass) -> None:
            register(c.dimensions)
            for child in c.children:
                scan_interaction(child)

        for root in self.model.object_roots:
            scan_object(root)
        for root in self.model.interaction_roots:
            scan_interaction(root)

    def emit(self, text: str = "") -> None:
        self.lines.append(text)

    def write_spaces(self, indent: str) -> None:
        self.emit(indent + "(spaces")
        for dims, space in self.space_for_dimensions.items():
            self.emit(indent + f"  (space {fed_atom(space)}")
            for dimension in dims:
                bound = self.model.dimensions.get(dimension, Dimension(dimension)).upper_bound
                if bound:
                    self.emit(indent + f"    (dimension {fed_atom(dimension)})"
                              + "  " + comment(f"HLA 1516 upperBound={bound}"))
                else:
                    self.emit(indent + f"    (dimension {fed_atom(dimension)})")
            self.emit(indent + "  )")
        self.emit(indent + ")")

    def emit_attribute(self, attr: Field, indent: str) -> None:
        space = self.space_for_dimensions.get(tuple(attr.dimensions))
        parts = ["(attribute", fed_atom(attr.name), map_transport(attr.transportation), map_order(attr.order)]
        if space:
            parts.append(fed_atom(space))
        self.emit(indent + " ".join(parts) + ")")
        details: list[str] = []
        if attr.datatype:
            details.append(f"1516 dataType={attr.datatype}")
        if attr.semantics:
            details.append(f"semantics={attr.semantics}")
        if details:
            self.emit(indent + "  " + comment("; ".join(details)))

    def emit_object(self, obj: ObjectClass, indent: str) -> None:
        self.emit(indent + f"(class {fed_atom(obj.name)}")
        for attr in obj.attributes:
            self.emit_attribute(attr, indent + "  ")
        for child in obj.children:
            self.emit_object(child, indent + "  ")
        self.emit(indent + ")")

    def emit_parameter(self, param: Field, indent: str) -> None:
        self.emit(indent + f"(parameter {fed_atom(param.name)})")
        details: list[str] = []
        if param.datatype:
            details.append(f"1516 dataType={param.datatype}")
        if param.semantics:
            details.append(f"semantics={param.semantics}")
        if details:
            self.emit(indent + "  " + comment("; ".join(details)))

    def emit_interaction(self, interaction: InteractionClass, indent: str) -> None:
        space = self.space_for_dimensions.get(tuple(interaction.dimensions))
        parts = [
            "(class",
            fed_atom(interaction.name),
            map_transport(interaction.transportation),
            map_order(interaction.order),
        ]
        if space:
            parts.append(fed_atom(space))
        self.emit(indent + " ".join(parts))
        for parameter in interaction.parameters:
            self.emit_parameter(parameter, indent + "  ")
        for child in interaction.children:
            self.emit_interaction(child, indent + "  ")
        self.emit(indent + ")")

    def build(self) -> str:
        self.emit(comment("Generated by hla1516_fom_to_fed.py"))
        self.emit(comment("Review datatype comments and generated routing spaces before use."))
        for warning in self.model.warnings:
            self.emit(comment("WARNING: " + warning))
        self.emit()

        self.emit("(FED")
        self.emit(f"  (Federation {fed_atom(self.federation_name)})")
        self.emit("  (FEDversion v1.3)")
        self.write_spaces("  ")

        self.emit("  (objects")
        if self.model.object_roots:
            for obj in self.model.object_roots:
                self.emit_object(obj, "    ")
        else:
            self.emit("    (class ObjectRoot)")
        self.emit("  )")

        self.emit("  (interactions")
        if self.model.interaction_roots:
            for interaction in self.model.interaction_roots:
                self.emit_interaction(interaction, "    ")
        else:
            self.emit("    (class InteractionRoot reliable receive)")
        self.emit("  )")
        self.emit(")")
        self.emit()
        return "\n".join(self.lines)


def derive_federation_name(input_path: Path) -> str:
    stem = re.sub(r"[^A-Za-z0-9_.:-]", "_", input_path.stem)
    return stem or "ConvertedFederation"


def main() -> int:
    parser = argparse.ArgumentParser(
        description="Convert a single HLA 1516/1516e FOM XML file into an HLA 1.3 FED file."
    )
    parser.add_argument("input_xml", type=Path, help="Source HLA 1516 / 1516e FOM XML file")
    parser.add_argument("output_fed", type=Path, help="Destination HLA 1.3 FED file")
    parser.add_argument(
        "--federation",
        help="Federation name to place in the FED file (default: source file stem)",
    )
    args = parser.parse_args()

    if not args.input_xml.is_file():
        parser.error(f"Input file does not exist or is not a file: {args.input_xml}")

    try:
        model = parse_fom(args.input_xml)
        federation_name = args.federation or derive_federation_name(args.input_xml)
        output = FedWriter(model, federation_name).build()
        args.output_fed.parent.mkdir(parents=True, exist_ok=True)
        args.output_fed.write_text(output, encoding="utf-8", newline="\n")
    except (OSError, ValueError) as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        return 2

    print(f"Wrote {args.output_fed}")
    if model.warnings:
        for warning in model.warnings:
            print(f"WARNING: {warning}", file=sys.stderr)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
