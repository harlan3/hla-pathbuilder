#! /usr/local/bin/python3
import sys
from xml.dom.minidom import parse

# output xml file
f = open("elementModel.xml", "w")

# fom file as first arguement
fomFile = sys.argv[1]

datasource = open(fomFile)
dom = parse(datasource)

def getChildrenByName(node):
    for child in node.childNodes:
        if child.localName=='name':
            yield child
# objects
objectList = []

f.write("<?xml version='1.0' encoding='UTF-8'?>\n")
f.write("<elementModel>\n")
f.write("    <objects>\n")

objectTags = dom.getElementsByTagName("objectClass") 
for node in objectTags:
    alist = getChildrenByName(node)
    for a in alist:
        nodeName = a.childNodes[0].nodeValue
        #print(nodeName)
        if (nodeName != "HLAobjectRoot"):
            objectList.append(nodeName)

objectList = list(set(objectList))
objectList.sort()

for objectName in objectList:
    f.write("        <name>" + objectName + "</name>\n")

f.write("    </objects>\n")

# interactions
interactionList = []

f.write("    <interactions>\n")

interactionTags = dom.getElementsByTagName("interactionClass") 
for node in interactionTags:
    alist = getChildrenByName(node)
    for a in alist:
        nodeName = a.childNodes[0].nodeValue
        #print(nodeName)
        if (nodeName != "HLAinteractionRoot"):
            interactionList.append(nodeName)

interactionList = list(set(interactionList))
interactionList.sort()

for interactionName in interactionList:
    f.write("        <name>" + interactionName + "</name>\n")

f.write("    </interactions>\n")

f.write("</elementModel>\n")
f.close()