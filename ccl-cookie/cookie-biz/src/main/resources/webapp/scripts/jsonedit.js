// Copyright (c) 2005 Rob Lanphier.
// See http://robla.net/2005/jsonwidget/LICENSE for license (BSD style)

var jsonwidget = function() {}

jsonwidget.language='zh';

jsonwidget.localstrings = {
    'zh': {'help': '帮助',
        'no': '否',
        'yes': '是',
        'del': '删除',
        'hide': '隐藏',
        'show': '显示',
        'delete': '删除',
        'Root node': '根节点',
        'Add property': '添加属性',
        'Add ': '添加 ',
        ' to ': ' 到 ',
        'Please select':'请选择...'
    },
};

// faux-gettext() implementation.
function _(s) {
    rc = s;
    if (typeof(jsonwidget.language)!='undefined' &&
        typeof(jsonwidget.localstrings[jsonwidget.language])!='undefined' &&
        jsonwidget.localstrings[jsonwidget.language][s]) {
        rc = jsonwidget.localstrings[jsonwidget.language][s];
    }
    return rc;
}

jsonwidget.stringToId = function (str) {
    // performs the easiest transformation to safe id, but is lossy
    if((typeof str)=='number') {
        return str.toString();
    }
    else if ((typeof str)=='string') {
        return str.replace(/[^a-z0-9\-_:\.]/gi, "");
    }
    else error();
};

jsonwidget.getTitleFromNode = function (schemanode, nodeindex) {
    if(undefined != schemanode.id) {
    	if(undefined != schemanode.text){
    		return schemanode.text;
    	}else{
    		return schemanode.id;
    	}
    }
    else {
        return nodeindex;
    }
}

jsonwidget.getNewValueForType = function(thistype) {
	var newvalue=null;
    switch(thistype) {
    case 'map':
        newvalue = new Object();
        break;

    case 'seq':
        newvalue = new Array();
        break;

    case 'number':
    case 'int':
        newvalue = 0;
        break;

    case 'str':
        newvalue = "";
        break;

    case 'bool':
        newvalue = false;
        break;
    default:
        newvalue = null;
        break;
    }
    return newvalue;
};


jsonwidget.getType = function (foo) {
    if(foo==null) {
        return undefined;
    }

    switch(typeof foo) {
        case "object":
        if(foo.constructor == Array) {
            return "seq";
        }
        else {
            return "map";
        }
        break;

        case "number":
        return "number";
        break;

        case "boolean":
        return "bool";
        break;

        case "string":
        return "str";
        break;

        default:
        return undefined;
        break;
    }
};

jsonwidget.getSchemaArray = function (parent) {
    var schema = new Object();

    schema.type = jsonwidget.getType(parent);

    switch (schema.type) {
    case 'map':
        schema.mapping = new Object();
        for (var name in parent) {
            schema.mapping[name]= jsonwidget.getSchemaArray(parent[name]);
        }
        break;
    case 'seq':
        schema.sequence = new Array();
        schema.sequence[0] = jsonwidget.getSchemaArray(parent[0]);
        break;
    }
    return schema;
};

jsonwidget.treeRef = function (node, parent, nodeindex, nodename) {
    this.node = node;
    this.parent = parent;
    this.nodeindex = nodeindex;
    this.nodename = nodename;
};

//
// jsonTreeRef object
//
// not yet bothering to make this a subclass of treeRef
jsonwidget.jsonTreeRef = function (node, parent, nodeindex, nodename, schemaref) {
    this.node = node;
    this.parent = parent;
    this.nodeindex = nodeindex;
    this.nodename = nodename;
    this.schemaref = schemaref;

    this.init = jsonwidget.jsonTreeRef.init;
    this.isUserKey = jsonwidget.jsonTreeRef.isUserKey;
    this.renamePropname = jsonwidget.jsonTreeRef.renamePropname;
    this.getType = jsonwidget.jsonTreeRef.getType;
    this.getTitle = jsonwidget.jsonTreeRef.getTitle;
    this.getFullIndex = jsonwidget.jsonTreeRef.getFullIndex;
    this.attachSchema = jsonwidget.jsonTreeRef.attachSchema;
    this.attachValidators=jsonwidget.jsonTreeRef.attachValidators;

    this.init();
};


jsonwidget.jsonTreeRef.init = function () {
    this.fullindex = this.getFullIndex();
    this.attachSchema();
};

jsonwidget.jsonTreeRef.attachSchema = function () {
    if(this.schemaref.node.type == 'any') {
        if(this.getType()=='map') {
            this.schemaref.node.mapping = {
                "extension": {
                    "id":"extension field",
                    "type":"any"
                }
            };
            this.schemaref.node.user_key = "extension";
        }
        else if(this.getType()=='seq') {
            this.schemaref.node.sequence = [
                    {
                        "id":"extension field",
                        "type":"any"
                    }
            ];
            this.schemaref.node.user_key = "extension";
        }
    }
};

jsonwidget.jsonTreeRef.getTitle = function () {
    if(undefined != this.nodename) {
        return this.nodename;
    }
    else {
        return jsonwidget.getTitleFromNode(this.node, this.nodeindex);
    }
};

jsonwidget.jsonTreeRef.isUserKey = function () {
    return this.userkeyflag;
};

jsonwidget.jsonTreeRef.renamePropname = function (newindex) {
    var oldindex = this.nodeindex;
    this.parent.node[newindex] = this.node;

    this.nodeindex = newindex;
    this.nodename = newindex;
    this.fullindex = this.getFullIndex();

    delete this.parent.node[oldindex];
};

jsonwidget.jsonTreeRef.getType = function () {
    var nodetype = this.schemaref.node.type;
    if(nodetype == 'any') {
        if(this.node == undefined) {
            return undefined;
        }
        else {
            return jsonwidget.getType(this.node);
        }
    }
    else {
        return nodetype;
    }
};

jsonwidget.jsonTreeRef.getFullIndex = function () {
    if(this.parent==undefined) {
        return "json_root";
    }
    else {
        return this.parent.getFullIndex() + "." + jsonwidget.stringToId(this.nodeindex);
    }
};

jsonwidget.jsonTreeRef.attachValidators = function (){
	this.validator={};
	var validType=[];
	if("number" == this.schemaref.node.type || "int" ==this.schemaref.node.type){
		validType.push("number");
	}
	if(this.schemaref.node.required){
		this.validator.required=true;
	}

	if(this.schemaref.node["validators"]){
		for(var i in this.schemaref.node["validators"]){
			var valid=this.schemaref.node["validators"][i];
			if("size"==valid.type){
				if(valid.min > 0){
					validType.push("minlength["+valid.min+"]");
				}
				if(valid.max > 0){
					validType.push("maxlength["+valid.max+"]");
				}
			}
			if("email"==valid.type){
				validType.push("email");
			}
			if("pattern"==valid.type){
				validType.push("pattern[/"+valid.regexp+"/g]");
			}
		}
	}
	this.validator.validType=validType;
};

//
// schemaIndex object
//

jsonwidget.schemaIndex = function (schema) {
    this.root = schema;
    this.idtable = {};
    this.debugwindow = document.getElementById("je_warningdiv");

    if(!this.root) {
        return null;
    }
    this.indexSubtree = jsonwidget.schemaIndex.indexSubtree;
    this.newRef = jsonwidget.schemaIndex.newRef;

    this.indexSubtree(this.root);
};

jsonwidget.schemaIndex.indexSubtree = function (schemanode) {
    var nodetype = schemanode.type;

    switch(nodetype) {
    case 'map':
        for(var i in schemanode.mapping) {
            this.indexSubtree(schemanode.mapping[i]);
        }
        break;
    case 'seq':
        for(var i in schemanode.sequence) {
            this.indexSubtree(schemanode.sequence[i]);
        }
        break;
    }

    if(undefined != schemanode.id) {
        this.idtable[schemanode.id] = schemanode;
    }
};

jsonwidget.schemaIndex.newRef = function (node, parent, nodeindex, nodename) {
    if(node.type == 'idref') {
        node =  this.idtable[node.idref];
    }
    return new jsonwidget.treeRef(node, parent, nodeindex, nodename);
};


//
// editor object
//

jsonwidget.editor = function () {

    this.jsonLookupById = new Array();
    this.bgcolor = "#f0f0ff";

    //override if you want to use different ids
    this.htmlids = {
		"warningdiv" : "api_req_warningdiv",
		"formdiv" : "api_req_formdiv",
		"schemaformdiv" : "api_req_schemaformdiv",
		"sourcetextarea" : "api_req_sourcetextarea",
		"schematextarea" : "api_req_schematextarea",
		"schemaschematextarea" : "api_req_schemaschematextarea",
		"byexamplebutton" : "api_req_byexamplebutton",
		"sourcetextform" : "api_req_sourcetextform"
    };

    this.debuglevel=0;

    this.views = ['form','source'];
    this.formview = 'form';

	this.showByExampleButton = false;

    this.classname = {
        fgbutton: "je_foreground",
        bgbutton: "je_background"
    };

    this.formdiv = document.getElementById(this.htmlids.formdiv);

    this.showForm = jsonwidget.editor.showForm;
    this.clearForm = jsonwidget.editor.clearForm;
    this.attachHandlers = jsonwidget.editor.attachHandlers;
    this.setNode = jsonwidget.editor.setNode;
    this.updateNode = jsonwidget.editor.updateNode;
    this.deleteNode = jsonwidget.editor.deleteNode;
    this.getArrayInputAttrs = jsonwidget.editor.getArrayInputAttrs;
    this.getHelpButton = jsonwidget.editor.getHelpButton;
    this.getAddToSeqButton = jsonwidget.editor.getAddToSeqButton;
    this.getDeleteButton = jsonwidget.editor.getDeleteButton;
    this.addPropToMapping = jsonwidget.editor.addPropToMapping;
    this.getAddButton = jsonwidget.editor.getAddButton;
    this.attachArrayInput = jsonwidget.editor.attachArrayInput;
    this.showPropnameInput = jsonwidget.editor.showPropnameInput;
    this.getPropnameSpan = jsonwidget.editor.getPropnameSpan;
    this.attachSimplePropertyInput = jsonwidget.editor.attachSimplePropertyInput;
    this.getStringInput = jsonwidget.editor.getStringInput;
    this.getBoolInput = jsonwidget.editor.getBoolInput;
    this.getNumberInput = jsonwidget.editor.getNumberInput;
    this.getTypeSelectInput = jsonwidget.editor.getTypeSelectInput;
    this.attachNodeInput = jsonwidget.editor.attachNodeInput;
    this.setView = jsonwidget.editor.setView;
    this.getStatusLightDelay = jsonwidget.editor.getStatusLightDelay;
    this.setStatusLight = jsonwidget.editor.setStatusLight;
    this.clearStatusLight = jsonwidget.editor.clearStatusLight;
    this.toggleToFormActual = jsonwidget.editor.toggleToFormActual;
    this.updateJSON = jsonwidget.editor.updateJSON;
    this.error = jsonwidget.editor.error;
    this.debugOut = jsonwidget.editor.debugOut;
    this.warningOut = jsonwidget.editor.warningOut;
    this.clearWarnings = jsonwidget.editor.clearWarnings;
    this.contextHelp = jsonwidget.editor.contextHelp;
    this.confirmDelete = jsonwidget.editor.confirmDelete;
    this.getSchema = jsonwidget.editor.getSchema;
    this.schemaEditInit = jsonwidget.editor.schemaEditInit;
    this.byExampleInit = jsonwidget.editor.byExampleInit;
    this.createSchemaFromExample = jsonwidget.editor.createSchemaFromExample;
    this.attachByExampleText = jsonwidget.editor.attachByExampleText;
    this.setFormOnSubmit = jsonwidget.editor.setFormOnSubmit;
    this.clear=jsonwidget.editor.clear;
};

jsonwidget.editor.schemaEditInit = function () {
    this.schemaedit = new jsonwidget.editor();
    var je = this;
    this.schemaedit.setView = function (viewtoset) {
        je.setView(viewtoset);
    };
    this.schemaedit.setStatusLight = function (statustext) {
        je.setStatusLight(statustext);
    };
    this.schemaedit.clearStatusLight = function () {
        je.clearStatusLight();
    };
    this.schemaedit.getStatusLightDelay = function (jsonref) {
        return je.getStatusLightDelay(jsonref);
    };
    this.schemaedit.formview = 'schemaform';
	this.schemaedit.formdiv = document.getElementById(this.htmlids.schemaformdiv);
    this.schemaedit.htmlids.formdiv = this.htmlids.schemaformdiv;
    this.schemaedit.htmlids.sourcetextarea = this.htmlids.schematextarea;
    this.schemaedit.htmlids.schematextarea = this.htmlids.schemaschematextarea;
};

jsonwidget.editor.byExampleInit = function () {
	this.showByExampleButton = true;
};

jsonwidget.editor.clear = function (){
	this.jsonLookupById=new Array();
	this.clearForm();
	var jsonarea = document.getElementById(this.htmlids.sourcetextarea);
	jsonarea.value="";
	var schemaarea = document.getElementById(this.htmlids.schematextarea);
	schemaarea.value="";
	this.rootjson=null;
	this.jsondata=null;
	this.schemaindex=null;
	this.currentView=null;
};

jsonwidget.editor.createSchemaFromExample = function () {
    this.updateJSON();
    if(this.jsondata == null) {
        alert("I can't generate a very interesting schema from a blank document.  Try adding a little something to your JSON first");
    }
    else {
        this.schemaedit.jsondata = jsonwidget.getSchemaArray(this.jsondata);
        this.showByExampleButton = false;
        this.schemaedit.updateJSON();
        this.setView(this.formview);
    }
};

jsonwidget.editor.attachByExampleText = function () {
    var examplebutton = document.createElement('span');
    var je = this;
    examplebutton.id = this.htmlids.byexamplebutton;
    examplebutton.style.cursor = "pointer";
    examplebutton.appendChild(document.createTextNode("[Create schema by example]"));
    examplebutton.onclick = function () {
        je.createSchemaFromExample();
    };

    this.formdiv.appendChild(examplebutton);
    this.formdiv.appendChild(document.createTextNode(" - clicking this button will generate a schema based on the current JSON file, and load it into the schema area.  It will also lock the current document into its current structure."));
};

jsonwidget.editor.clearForm = function () {
    var parent = this.formdiv.parentNode;
    var nextsibling = this.formdiv.nextSibling;
    parent.removeChild(this.formdiv);
    this.formdiv.innerHTML = "";
    parent.insertBefore(this.formdiv, nextsibling);
};

jsonwidget.editor.attachHandlers = function () {
    var jsoneditobj = this;
    document.body.style.background = jsoneditobj.bgcolor;
    this.formdiv.onchange = function (event) {
        if($(event.target).hasClass("jeclass")) {
            var jsonnode = jsoneditobj.jsonLookupById[event.target.id.substr(8)];
            var nodetype = jsonnode.getType();

            if(nodetype == undefined) {
                var value=jsonwidget.getNewValueForType(event.target.value);
                jsonnode.node = value;
                jsoneditobj.updateNode(jsonnode);
            }
            else {
            	var value = null;
                if(nodetype == 'str') {
                    value = event.target.value;
                }
                else if(nodetype == 'int') {
                    value = parseInt(event.target.value);
                }
                else if(nodetype == 'number') {
                	value = parseFloat(event.target.value);
                }
                else if(nodetype == 'bool') {
                    value = event.target.checked;
                }

                jsonnode.node = value;
                if(jsonnode.parent != undefined) {
                    jsonnode.parent.node[jsonnode.nodeindex] = value;
                }
                else {
                    jsoneditobj.jsondata = value;
                }
            }
        }
    };

    this.formdiv.onclick = function (event) {


        var target = window.event ? window.event.srcElement : event.target;
        if(target.className == "rmx") {
            var jsonnode = jsoneditobj.jsonLookupById[target.id.substr(9)];
            jsonwidget.editor.confirmDelete(jsoneditobj, jsonnode, target);
        }
    };
};

jsonwidget.editor.setNode = function (jsonnode, value) {
    if(jsonnode.parent != undefined) {
        jsonnode.parent.node[jsonnode.nodeindex] = value;
    }
    else {
        this.jsondata = value;
    }
};

jsonwidget.editor.updateNode = function (jsonnode) {
    var value = jsonnode.node;
    if(jsonnode.parent != undefined) {
        jsonnode.parent.node[jsonnode.nodeindex] = value;
        jsonnode.attachSchema();

        var rownode = document.createElement("div");
        jsonnode.domparent.parentNode.replaceChild(rownode, jsonnode.domparent);
        jsonnode.domparent = rownode;

        var je = this;
        this.setStatusLight("working...");
        setTimeout(function () {je.attachNodeInput(jsonnode);je.clearStatusLight();},this.getStatusLightDelay(jsonnode));
    }
    else {
        this.jsondata = value;
        this.updateJSON();
        this.setView(this.formview);
    }
};

jsonwidget.editor.deleteNode = function (jsonindex) {
    var jsonnode = this.jsonLookupById[jsonindex];
    var parent = jsonnode.parent;
    jsonnode.domparent.parentNode.removeChild(jsonnode.domparent);
    if(parent != undefined) {
        if (parent.node instanceof Array) {
            parent.node.splice(jsonnode.nodeindex,1);
        }
        else {
            delete parent.node[jsonnode.nodeindex];
        }
        delete this.jsonLookupById[jsonindex];
        this.updateNode(parent);
    }
    else {
        this.jsondata = null;
        delete this.jsonLookupById[jsonindex];
        this.updateJSON();
        this.setView(this.formview);
    }
};

jsonwidget.editor.getArrayInputAttrs = function (jsonref) {
    var jsoni;
    var schemai;

    var contentsid='contents.'+jsonref.fullindex;

    var retval = document.createElement("div");
	$(retval).css("width",($(jsonref.domparent).width())+"px");

    retval.id=contentsid;

    for(var i in jsonref.node) {
        var rownode = document.createElement("div");
		$(rownode).css("padding","0 0 10px 0");
		$(rownode).css("width",($(retval).width()-20)+"px");
		 var userkeyflag = false;
        if(jsonref.getType()=='map') {
            var nodename = i;
            if(jsonref.schemaref.node.mapping[i]==undefined) {
                if(jsonref.schemaref.node.user_key==undefined) {
                    this.warningOut("warning: unrecognized key: "+i+"<br/>");
                    continue;
                }
                else {
                    userkeyflag = true;
                    var j = jsonref.schemaref.node.user_key;
                    schemai = this.schemaindex.newRef(jsonref.schemaref.node.mapping[j], jsonref.schemaref, j, j);
                }
            }
            else {
                userkeyflag = false;
                nodename = jsonwidget.getTitleFromNode(jsonref.schemaref.node.mapping[i], i);
                schemai = this.schemaindex.newRef(jsonref.schemaref.node.mapping[i], jsonref.schemaref, i, i);

            }
            jsoni = new jsonwidget.jsonTreeRef(jsonref.node[i], jsonref, i, nodename, schemai);
        }
        else if (jsonref.getType()=='seq') {
            schemai = this.schemaindex.newRef(jsonref.schemaref.node.sequence[0], jsonref.schemaref, 0, i);
            jsoni = new jsonwidget.jsonTreeRef(jsonref.node[i], jsonref, i, i, schemai);
        }
        jsoni.userkeyflag = userkeyflag;
        jsoni.domparent = rownode;
        this.jsonLookupById[jsoni.fullindex]=jsoni;
        this.attachNodeInput(jsoni);
        retval.appendChild(rownode);
    }
    if(jsonref.getType()=='map') {
        var schemap = jsonref.schemaref.node.mapping;
        for(var i in schemap) {
            //this.debugOut(1, schemap[i].required);

            if(jsonref.node[i]==undefined && schemap[i].required == true) {
                var rownode = document.createElement("div");
				$(rownode).css("padding","0 0 10px 0");
				$(rownode).css("width",($(retval).width()-20)+"px");
                jsoni = this.addPropToMapping(jsonref, i);
                jsoni.domparent = rownode;
                this.attachNodeInput(jsoni);
                retval.appendChild(rownode);
            }
        }
    }

    return retval;
};


jsonwidget.editor.getHelpButton = function (jsonref) {
    var helpid='help.'+jsonref.fullindex;
    var retval = document.createElement("span");

    retval.className="je_help";
    retval.setAttribute("title", "help");
    retval.id=helpid;
    retval.appendChild(document.createTextNode("["+_('help')+"]"));
    return retval;
}

jsonwidget.editor.getDeleteButton = function (jsonref) {
    var retval = document.createElement("span");
    var rmid='rmbutton.'+jsonref.fullindex;

    if(!jsonref.schemaref.node.required) {
        retval.className="rmx";
        retval.setAttribute("title", "delete");
    }
    else {
        retval.className="je_disabled";
        retval.setAttribute("title", "required attribute - deletion not allowed");
    }
    retval.id=rmid;
    retval.appendChild(document.createTextNode("["+_("del")+"]"));
    return retval;
}


jsonwidget.editor.addPropToMapping = function (jsonref, prop) {
    var newname = prop;
    var newindex = prop;
    var schemap = jsonref.schemaref.node.mapping;

    if(prop == jsonref.schemaref.node.user_key) {
        newindexbase = jsonwidget.stringToId(prop);
        for(var i=1;true;i++) {
            newindex = newindexbase + "_" + i;
            if(jsonref.node[newindex] == undefined) {
                newname = newindex;
                break;
            }
        }
    }
    else {
        newname = jsonwidget.getTitleFromNode(schemap[prop], prop);
    }

    var newschema = this.schemaindex.newRef(schemap[prop], jsonref.schemaref, prop, prop);
    var newvalue = jsonwidget.getNewValueForType(newschema.node.type);
    var newjson = new jsonwidget.jsonTreeRef(newvalue, jsonref, newindex, newname, newschema);
    if (jsonref.node instanceof Array) {
        jsonref.node.push(newvalue);
    }
    else {
        jsonref.node[newindex]=newvalue;
    }
    this.jsonLookupById[newjson.fullindex]=newjson;
    return newjson;
};

jsonwidget.editor.getAddButton = function (jsonref, prop) {
    var schemap = jsonref.schemaref.node.mapping;
    var addlink = document.createElement("a");
    addlink.style.textDecoration = "underline";
    addlink.style.cursor = "pointer";
    var helpcontent=jsonwidget.editor.contextHelp(schemap[prop],prop);
    $(addlink).tooltip({
    	trackMouse:true,
    	position: 'bottom',
    	content: helpcontent
    });
    var je=this;
    addlink.onclick = function () {
    	$(addlink).tooltip("destroy");
        je.addPropToMapping(jsonref, prop);
        je.updateNode(jsonref);
    };
    addlink.appendChild(document.createTextNode(jsonwidget.getTitleFromNode(schemap[prop], prop)));

    return addlink;
};

jsonwidget.editor.getAddToSeqButton = function (jsonref) {
    var childschema = jsonref.schemaref.node.sequence[0];
    var je = this;
    var addlink = document.createElement("a");
    var itemname = jsonwidget.getTitleFromNode(childschema, "item");
    addlink.style.textDecoration = "underline";
    addlink.style.cursor = "pointer";
    addlink.onclick = function () {
        var newname = 0;
        var newindex = 0;
        var newschema = je.schemaindex.newRef(childschema, jsonref.schemaref, 0, newindex);
        var newvalue=jsonwidget.getNewValueForType(newschema.node.type);

        var newjson = new jsonwidget.jsonTreeRef(newvalue, jsonref, newindex, newindex, newschema);
        if (jsonref.node instanceof Array) {
            jsonref.node.push(newvalue);
        }
        else {
            jsonref.node[newindex]=newvalue;
        }
        je.jsonLookupById[newjson.fullindex]=newjson;
        je.updateNode(jsonref);
    }
    var nodename = jsonref.getTitle();

    addlink.appendChild(document.createTextNode(_("Add ")+ itemname + _(" to ") + nodename ));
    return addlink;
}

jsonwidget.editor.attachArrayInput = function (jsonref) {
    var retval = this.getArrayInputAttrs(jsonref);

    if(jsonref.getType()=='map') {
        var schemap = jsonref.schemaref.node.mapping;
        var numprops = 0;
        for(var i in schemap) {
            if(jsonref.node[i]==undefined || i == jsonref.schemaref.node.user_key) {
                numprops++;
                if(numprops == 1) {
                    retval.appendChild(document.createTextNode(_("Add property")+": "));
                }
                else {
                    retval.appendChild(document.createTextNode(", "));
                }
                retval.appendChild(this.getAddButton(jsonref,i));
            }
        }
    }

    if(jsonref.getType()=='seq') {
        retval.appendChild(this.getAddToSeqButton(jsonref));
    }

	$(retval).css("padding","10px");
	jsonref.domparent.appendChild(retval);

	var jsoneditobj=this;


	$(retval).panel({
		title: jsonref.nodename,
		collapsible:true ,
		closable:true,
		onClose:function(){
			jsoneditobj.deleteNode(jsonref.fullindex, retval);
		}
	});
	var header=$(retval).panel("header");
	var helpcontent=jsonwidget.editor.contextHelp(jsonref.schemaref.node,jsonref.nodename);
	$(header).tooltip({
		position: 'bottom',
		content: helpcontent
	});
}

jsonwidget.editor.showPropnameInput = function (jsonref,htmlnode) {
    var nameinput = document.createElement("input");
    var nameinputid = 'nameinputid.'+jsonref.fullindex;
    nameinput.className = "jeclass";
    nameinput.className="je_userkey";
    nameinput.id=nameinputid;
    nameinput.size=20;
    nameinput.type="text";
    nameinput.setAttribute("value", jsonref.nodename);

    var je = this;
    var parent = htmlnode.parentNode;
    var putback = function () {
        jsonref.renamePropname(this.value);
        parent.replaceChild(je.getPropnameSpan(jsonref),nameinput);
        je.updateNode(jsonref);
        je.jsonLookupById[jsonref.fullindex]=jsonref;
        je.updateJSON();
    }

    nameinput.onblur = putback;
    nameinput.onchange = putback;
    parent.replaceChild(nameinput, htmlnode);
    nameinput.focus();
}

jsonwidget.editor.getPropnameSpan = function (jsonref) {
    var nameinput = document.createElement("span");
    nameinput.appendChild(document.createTextNode(jsonref.nodename));
    var nameinputid = 'nameinputid.'+jsonref.fullindex;
    nameinput.className="je_userkey";
    nameinput.id=nameinputid;
    var je = this;
    nameinput.onclick = function (event) {
        je.showPropnameInput(jsonref,this);
    }
    return nameinput;
}

jsonwidget.editor.attachSimplePropertyInput = function (jsonref) {
    var valuetype = jsonref.getType();

    var inputelement;
    switch (valuetype) {
    case 'str':
        inputelement = this.getStringInput(jsonref);
        break;

    case 'bool':
        inputelement = this.getBoolInput(jsonref);
        break;

    case 'number':
    case 'int':
        inputelement = this.getNumberInput(jsonref);
        break;

    case 'any':
    case undefined:
        inputelement = this.getTypeSelectInput(jsonref);
        break;

    default:
        this.warningOut("unknown type: " + valuetype);
        inputelement = this.getTypeSelectInput(jsonref);
        break;
    }

	var parent = jsonref.domparent;
	var namenode = document.createElement("span");
	namenode.className="je_field_label";
	if(jsonref.isUserKey()) {
		namenode.appendChild(this.getPropnameSpan(jsonref));
	}
	else {
		namenode.appendChild(document.createTextNode(jsonref.nodename));
	}
	var helpcontent=this.contextHelp(jsonref.schemaref.node,jsonref.nodename);
	$(namenode).tooltip({
		position: 'bottom',
		content: helpcontent
	});


	var valuenode = document.createElement("span");
	var controlnode = document.createElement("span");

	inputelement.className="jeclass";
    valuenode.appendChild(inputelement);

    jsonref.attachValidators();
    if(jsonref.schemaref.node['format']) {
    	var format=jsonref.schemaref.node['format'];
    	if("date" == format){
    		jsonref.validator.validType.push("date");
    	}else if("datetime" == format){
    		jsonref.validator.validType.push("datetime");
    	}else if("time" == format){
    		jsonref.validator.validType.push("time");
    	}
    }
    $(inputelement).validatebox(jsonref.validator);

    var controldiv = document.createElement("span");
    controldiv.className="je_topcontrols";
    controldiv.appendChild(this.getDeleteButton(jsonref));

    controlnode.appendChild(controldiv);

    parent.appendChild(namenode);
    parent.appendChild(valuenode);
    parent.appendChild(controlnode);
}

jsonwidget.editor.getStringInput = function (jsonref) {
    var curvalue = jsonref.node;
    var inputid = 'inputid.'+jsonref.fullindex;
    var inputelement;
    if(jsonref.schemaref.node['enum']==undefined) {
        //switching to DOM creation in hopes of preventing injection problems
    	if(jsonref.schemaref.node['format']==undefined||"textarea" != jsonref.schemaref.node['format']){
    		inputelement = document.createElement("input");
    		inputelement.id=inputid;
    		inputelement.size=20;
    		inputelement.type="text";
    		inputelement.setAttribute("value", curvalue);
    	}else{
    		inputelement = document.createElement("textarea");
    		inputelement.rows=5;
    		inputelement.cols=30;
    		inputelement.id=inputid;
    		inputelement.appendChild(document.createTextNode(curvalue));
    	}
    } else {
        inputelement = document.createElement("select");
        var validvalue = false;
        inputelement.id=inputid;
        var dicts=jsonref.schemaref.node['enum'];
        for(var ordinal in dicts) {
            option = document.createElement("option");
              /////////////////
            var _ordinal = dicts[ordinal];
            var keys = Object.keys(_ordinal);
            option.appendChild(document.createTextNode(_ordinal[keys[0]]));
            option.value=keys[0];
            if(keys[0]==curvalue) {
            /////////////////
//            option.appendChild(document.createTextNode(dicts[ordinal].value));
//            option.value=dicts[ordinal].key;
//            if(dicts[ordinal].key==curvalue) {
                option.selected = true;
                validvalue = true;
            }
            inputelement.appendChild(option);
        }
        if(!validvalue) {
            this.setNode(jsonref, Object.keys(dicts[0])[0]);
        }
    }

    return inputelement;
}

jsonwidget.editor.getTypeSelectInput = function (jsonref) {
    var inputid = 'inputid.'+jsonref.fullindex;
    var types = ['any','str','int','number','bool','seq','map'];

    var inputelement = document.createElement("select");
    inputelement.id=inputid;
    for(var i in types) {
        var option = document.createElement("option");
        option.appendChild(document.createTextNode(types[i]));
        inputelement.appendChild(option);
    }
    return inputelement;
}


jsonwidget.editor.getBoolInput = function getBoolInput(jsonref) {
    var inputid = 'inputid.'+jsonref.fullindex;

    var inputelement = document.createElement("input");
    inputelement.id=inputid;
    inputelement.type="checkbox";

    if(jsonref.node) {
        inputelement.setAttribute("checked", "true");
    }
    return inputelement;
}

jsonwidget.editor.getNumberInput = function getNumberInput(jsonref) {
    var curvalue = jsonref.node;
    var inputid = 'inputid.'+jsonref.fullindex;
    var inputelement;

    if(jsonref.node == null) {
        jsonref.node = '';
    }

    if(jsonref.schemaref.node['enum']==undefined) {
        //switching to DOM creation in hopes of preventing injection problems
    	inputelement = document.createElement("input");
	    inputelement.id=inputid;
	    inputelement.size=5;
	    inputelement.type="text";
	    inputelement.setAttribute("value", jsonref.node);
	    inputelement.setAttribute("align", "right");
    } else {
        inputelement = document.createElement("select");
        var validvalue = false;
        inputelement.id=inputid;

        var option=document.createElement("option");
		option.appendChild(document.createTextNode(_("Please select")));
		inputelement.appendChild(option);
		var dicts=jsonref.schemaref.node['enum'];
        for(var ordinal in dicts) {
            option = document.createElement("option");
            /////////////////
            var _ordinal = dicts[ordinal];
            var keys = Object.keys(_ordinal);
            option.appendChild(document.createTextNode(_ordinal[keys[0]]));
            option.value=keys[0];
            if(keys[0]==curvalue) {
            /////////////////
//            option.appendChild(document.createTextNode(dicts[ordinal].value));
//            option.value=dicts[ordinal].key;
//            if(dicts[ordinal].key==curvalue) {
                option.selected = true;
                validvalue = true;
            }
            inputelement.appendChild(option);
        }
        if(!validvalue) {
            this.setNode(jsonref, 0);
        }
    }
    return inputelement;
}

jsonwidget.editor.attachNodeInput = function attachNodeInput(jsonref) {
    var nodetype = jsonref.getType();
    var startTime=new Date().getTime();
    var endTime;

    if(jsonref.schemaref == undefined) {
        this.warningOut("Unrecognized index: "+jsonref.toSource()+"<br/>\n");
        //try uncommenting to see if anything breaks...if so, add this
        //return;
    }

    if(jsonref.node == undefined) {
        jsonref.node = jsonwidget.getNewValueForType(nodetype);
    }

    switch(nodetype) {
        case 'map':
        case 'seq':
        this.attachArrayInput(jsonref);
        break;

        default:
        this.attachSimplePropertyInput(jsonref);
        break;
    }
    endTime=new Date().getTime();
    jsonref.rendertime = endTime - startTime;
}

jsonwidget.editor.setView = function setView (viewtoset) {
    this.setStatusLight("working...");

    if(this.currentView){
    	switch (this.currentView) {
    	case 'form':
    		this.updateJSON();
    		break;
    	case 'schemaform':
    		this.schemaedit.updateJSON();
    		break;
    	}
    }

    var je = this;

    switch (viewtoset) {
    case "source":
        // sometimes we set currentView to 'source' before calling setView to
        // bypass updateJSON, so that it won't smash the source field contents
        var tryUpdateJSON = (this.currentView&&this.currentView != 'source');
        setTimeout(function () {
            if(tryUpdateJSON) {
                je.updateJSON();
            }
           // document.getElementById(je.htmlids.sourcetextarea).style.display="inline";
            je.clearStatusLight();
        },this.getStatusLightDelay(null));
        break;

    case "form":
        setTimeout(function () {je.toggleToFormActual()},this.getStatusLightDelay(this.rootjson));
        break;

    case "schemasource":
        setTimeout(function () {
            je.updateJSON();
            //document.getElementById(je.htmlids.schematextarea).style.display="inline";
            je.clearStatusLight();
        },this.schemaedit.getStatusLightDelay(null));
        break;
    case "schemaform":
        setTimeout(function () {je.schemaedit.toggleToFormActual()},this.getStatusLightDelay(this.schemaedit.rootjson));
        break;
    }
    this.currentView = viewtoset;
}


jsonwidget.editor.getStatusLightDelay = function (jsonref) {
    if(jsonref != null && jsonref != undefined && jsonref.rendertime != undefined) {
        var timeout = jsonref.rendertime / 20;
    }
    else {
        if(document.getElementById(this.htmlids.sourcetextarea).value.length>5000) {
            var timeout = 400;
        }
        else {
            var timeout = 40;
        }
    }
    return timeout;
}

jsonwidget.editor.setStatusLight = function (statustext) {
    if(this.statusLight == undefined) {
        this.statusLight = document.createElement("div");
        this.statusLight.id = "statusLight";
        this.statusLight.style.position = "fixed";
        this.statusLight.style.top = "0";
        this.statusLight.style.right = "0";
        this.statusLight.style.background = "red";
        this.statusLight.style.color = "white";
        this.statusLight.innerHTML=statustext;
        this.statusLight = document.body.insertBefore(this.statusLight, document.body.firstChild);
    }
    else {
        this.statusLight.innerHTML=statustext;
        this.statusLight.style.visibility = "visible";
    }
}

jsonwidget.editor.clearStatusLight = function () {
    this.statusLight.style.visibility = "hidden";
}

jsonwidget.editor.toggleToFormActual = function () {
    var endTime;
    var startTime=new Date().getTime();

    this.debugwindow = document.getElementById(this.htmlids.warningdiv);
    this.warningwindow = document.getElementById(this.htmlids.warningdiv);
    this.clearWarnings();

    var startTime=new Date().getTime();
    var endTime;
    var jsonarea = document.getElementById(this.htmlids.sourcetextarea);

    endTime=new Date().getTime();
    this.debugOut(1, '#1a Elapsed time: '+((endTime-startTime)/1000));

    var schema = this.getSchema();
    if(!schema) {
    	return null;
    }
    this.schemaindex = new jsonwidget.schemaIndex(schema);

    endTime=new Date().getTime();
    this.debugOut(1, '#1b Elapsed time: '+((endTime-startTime)/1000));

    try {
        this.jsondata = JSON.parse(jsonarea.value);
    }
    catch (error) {
    	var errorText=error.text==null?error.message:error.text;
        var errorstring = '';
        if(/^\s*$/.test(jsonarea.value)) {
	        this.jsondata = jsonwidget.getNewValueForType(schema.type);
        }
        else {
            if(error.at<40) {
                errorstring += errorText.substr(0,error.at);
            }
            else {
                errorstring += errorText.substr(error.at-40,40);
            }
            errorstring += "<span style='background-color: yellow'>";
            errorstring += errorText.substr(error.at,1);
            errorstring += "</span>";
            errorstring += eerrorText.substr(error.at+1,39);

            try {
                this.warningOut("JSON Parse error at char "+error.at+" near <pre>"+errorstring+"</pre>  Full error: "+error.toSource());
            }
            catch (error2) {
                this.warningOut("JSON Parse error at char "+error.at+" near <pre>"+errorstring+"</pre>");
            }
            this.currentView = 'source';
            this.setView('source');
            return;
        }
    }

    var nodename = jsonwidget.getTitleFromNode(schema, _("Root node"));
    var rootschema = this.schemaindex.newRef(schema, null, null, nodename);
    var rootjson = new jsonwidget.jsonTreeRef(this.jsondata, null, null, nodename, rootschema);
    this.rootjson = rootjson;
    this.jsonLookupById[rootjson.fullindex]=rootjson;

    this.clearForm();

    rootjson.domparent = this.formdiv;
    this.attachNodeInput(rootjson);
    if(this.showByExampleButton) {
        this.attachByExampleText();
    }

    endTime=new Date().getTime();
    this.debugOut(1, '#2 Elapsed time: '+((endTime-startTime)/1000));
    this.attachHandlers();
    this.clearStatusLight();
}

jsonwidget.editor.updateJSON = function () {
    var jsonarea = document.getElementById(this.htmlids.sourcetextarea);
    var parent = jsonarea.parentNode;
    var nextsibling = jsonarea.nextSibling;
    parent.removeChild(jsonarea);
    if(this.jsondata == null) {
        jsonarea.value = "";
    }
    else {
        jsonarea.value = JSON.stringify(this.jsondata,null,"  ");
    }
    parent.insertBefore(jsonarea, nextsibling);
}

jsonwidget.editor.error = function (m) {
    throw {
        name: 'jsonedit_error',
        message: m
    }
}

jsonwidget.editor.debugOut = function (level, text) {
    if(level<=this.debuglevel) {
        this.debugwindow.innerHTML += text+"<br/>\n";
    }
}

jsonwidget.editor.warningOut = function (text) {
    this.warningwindow.innerHTML += text+"<br/>\n";
}

jsonwidget.editor.clearWarnings = function () {
    this.warningwindow.innerHTML ="";
}


jsonwidget.editor.contextHelp = function(jsonnodeschema,nodename) {
    var helpdiv = document.createElement("div");
    var title = document.createElement("div");
    title.appendChild(document.createTextNode(jsonnodeschema.id));
    title.style.fontWeight = "bold";

    var sourcename = document.createElement("div");
    var sourcetext = document.createTextNode("(index: '" +
    		jsonnodeschema.id +
                                             "',type: '" +
    		jsonnodeschema.type +
    "')");
    sourcename.appendChild(sourcetext);
    sourcename.style.fontStyle="italic";
    sourcename.style.fontSize="smaller";

    var description = document.createElement("div");
    description.innerHTML=jsonnodeschema.text;

    helpdiv.appendChild(title);
    helpdiv.appendChild(sourcename);
    helpdiv.appendChild(description);
    helpdiv.className = "je_helpdiv";

    return helpdiv;
}


jsonwidget.editor.confirmDelete = function (jsoneditobj, jsonref, el) {
    var rmyesid='rmyesid.'+jsonref.fullindex;
    var rmnoid='rmnoid.'+jsonref.fullindex;

    el.innerHTML='';
    el.appendChild(document.createElement("br"));
    el.appendChild(document.createTextNode(_("delete")+"? "));
    var yesbutton = document.createElement("span");
    yesbutton.onclick = function (event) {
        // ie workarounds:
        var target = window.event ? window.event.srcElement : event.target;
        if(!event) {event = window.event}

        jsoneditobj.deleteNode(jsonref.fullindex, target);
        if(undefined != event.stopPropagation) {
            event.stopPropagation();
        }
        else {
            event.cancelBubble = true;
        }
    }
    yesbutton.appendChild(document.createTextNode("["+_("yes")+"]"));

    var nobutton = document.createElement("span");
    nobutton.onclick = function (event) {
        // ie workarounds:
        var target = window.event ? window.event.srcElement : event.target;
        if(!event) {event = window.event}

        target.parentNode.innerHTML = "["+_("del")+"]";
        if(undefined != event.stopPropagation) {
            event.stopPropagation();
        }
        else {
            event.cancelBubble = true;
        }
    }
    nobutton.appendChild(document.createTextNode("["+_("no")+"]"));

    el.appendChild(yesbutton);
    el.appendChild(nobutton);
}

jsonwidget.editor.getSchema = function () {
    var schemaarea = document.getElementById(this.htmlids.schematextarea);
    if("" != schemaarea.value){
    	try {
    		var retval = JSON.parse(schemaarea.value);
    	}
    	catch (error) {
    		var errorstring = '';
    		if(error.text == undefined) {
    			throw(error);
    		}
    		if(error.at<40) {
    			errorstring += error.text.substr(0,error.at);
    		}
    		else {
    			errorstring += error.text.substr(error.at-40,40);
    		}
    		errorstring += "<span style='background-color: yellow'>";
    		errorstring += error.text.substr(error.at,1);
    		errorstring += "</span>";
    		errorstring += error.text.substr(error.at+1,39);

    		this.warningOut("JSON Parse error at char "+error.at+" near <pre>"+errorstring+"</pre>  Full error: "+error.toSource());
    	}
    	return retval;
    }
    return null;
}

jsonwidget.editor.setFormOnSubmit = function () {
    var je = this;
    var sourcetextform = document.getElementById(this.htmlids.sourcetextform);
    sourcetextform.onsubmit = function () {
        if(je.currentView != 'source') {
            je.updateJSON();
        }
    };
};
