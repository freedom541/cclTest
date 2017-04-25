var api = function(baseUrl) {
	this.baseUrl = baseUrl;

	this.module = null;
	this.action = null;
	this.request = new api.request();
	this.response = new api.response();

	// override if you want to use different ids
	this.htmlids = {
		"actiontreediv" : "api_action_treediv",
		"actionhistorydiv" : "api_action_historydiv",
		"serverlistdiv" : "api_server_listdiv",
		"reqwarningdiv" : "api_req_warningdiv",
		"reqdiv" : "api_reqdiv",
		"reqform" : "api_req_form",
		"reqformdiv" : "api_req_formdiv",
		"reqsourcetextform" : "api_req_sourcetextform",
		"reqsourcetextarea" : "api_req_sourcetextarea",
		"reqschemaformdiv" : "api_req_schemaformdiv",
		"reqschematextarea" : "api_req_schematextarea",
		"reqschemacontent" : "api_req_schemacontent",
		"reqschemaschematextarea" : "api_req_schemaschematextarea",
		"respdiv" : "api_respdiv",
		"respheaderdiv" : "api_resp_headerdiv",
		"respcontentdiv" : "api_resp_contentdiv",
		"respresultwindow" : "api_resp_resultwindow",
	};

	this.actionBtns = {
		"collapseallbtn" : "api_collapse_all_btn",
		"expandallbtn" : "api_expand_all_btn",
		"showhistorybtn" : "api_show_history_btn",
		"showtreebtn" : "api_show_tree_btn",
		"executebtn" : "api_execute_btn",
		"cancelbtn" : "api_cancel_btn",
		"reloadresultbtn" : "api_reload_result_btn",
		"enableModulebtn" : "api_module_enable_btn",
		"disableModulebtn" : "api_module_disable_btn",
	};
	this.init = api.init;
	this.loadTree = api.loadTree;
	this.loadHistory = api.loadHistory;
	this.clearRequest = api.clearRequest;
	this.buildRequestEditor = api.buildRequestEditor;
	this.clearResponse = api.clearResponse;
	this.buildResponsePanel = api.buildResponsePanel;
	this.execute = api.execute;
	this.cancel = api.cancel;

	this.init();
};

api.init = function() {
	var api = this;

	var je = new jsonwidget.editor();

	//je.views = [ 'form', 'source', 'schemaform', 'schemasource' ];
	je.views = [ 'form', 'source', 'schemasource' ];
	je.schemaEditInit();
	this.reqEditor = je;

	var codeEditor = CodeMirror.fromTextArea(document
			.getElementById(api.htmlids.reqsourcetextarea), {
		styleActiveLine : true, // line选择是是否加亮
		styleSelectedText : true,
		lineNumbers : true, // 是否显示行数
		lineWrapping : true, // 是否自动换行
		matchBrackets : true,
		theme : "eclipse"
	});
	codeEditor.on("change", function() {
		$('#' + api.htmlids.reqsourcetextarea).val(codeEditor.getValue());
	});
	this.reqSourceEditor = codeEditor;

	this.actionTree = api.loadTree();
	$("#" + api.htmlids.actiontreediv).append(this.actionTree);

	$('#'+api.htmlids.respresultwindow).window('close');
	
	$('#' + api.htmlids.respdiv).tabs({
		tabPosition : "bottom"
	});

	$('#' + api.htmlids.reqdiv).tabs({
		tabPosition : "bottom",
		onSelect : function(title, index) {
			var schema = api.request.schema;
			if (schema) {
				if (0 == index) {
					je.setView('form');
				}
				if (1 == index) {
					je.setView('source');
					var source = $('#' + api.htmlids.reqsourcetextarea).val();
					if (codeEditor)
						codeEditor.setValue(source);
				}
				/*if (2 == index) {
					je.setView('schemaform');
				}*/
				if (2 == index) {
					je.setView('schemasource');
					var source = $('#' + api.htmlids.reqschematextarea).val();
					$('#' + api.htmlids.reqschematextarea).hide();
					$('#' + api.htmlids.reqschemacontent).empty();
					$.JSONView(source, $('#' + api.htmlids.reqschemacontent));
				}
			}
		}
	});

	$("#" + api.actionBtns.collapseallbtn).click(function() {
		$(api.actionTree).tree('collapseAll');
	});

	$("#" + api.actionBtns.expandallbtn).click(function() {
		$(api.actionTree).tree('expandAll');
	});

	$("#" + api.actionBtns.showhistorybtn).click(function() {
		$("#" + api.htmlids.actiontreediv).hide();
		$("#" + api.actionBtns.collapseallbtn).hide();
		$("#" + api.actionBtns.expandallbtn).hide();
		$("#" + api.actionBtns.showhistorybtn).hide();
		$("#" + api.actionBtns.showtreebtn).show();
		$("#" + api.htmlids.actionhistorydiv).show();
		$('#' + api.actionBtns.executebtn).show();
		api.action = null;
		api.clearRequest();
		api.clearResponse();
		api.loadHistory();
	});

	$("#" + api.actionBtns.showtreebtn).click(function() {
		$("#" + api.htmlids.actiontreediv).show();
		$("#" + api.actionBtns.collapseallbtn).show();
		$("#" + api.actionBtns.expandallbtn).show();
		$("#" + api.actionBtns.showhistorybtn).show();
		$("#" + api.actionBtns.showtreebtn).show();
		$("#" + api.htmlids.actionhistorydiv).hide();
		$("#" + api.actionBtns.showtreebtn).hide();
		$('#' + api.actionBtns.executebtn).hide();
		api.action = null;
		api.clearRequest();
		api.clearResponse();
	});

	$("#" + api.actionBtns.executebtn).click(function() {
        api.execute();
	});

	$("#" + api.actionBtns.cancelbtn).click(function() {
        api.cancel();
	});

	$("#" + api.actionBtns.enableModulebtn).click(function() {
		$.ajax({
			type : "PUT",
			url : api.baseUrl+"resources/request/modules/"+api.module+"/enable",
			data : {},
			dataType : "json",
			headers : {
				"Content-Type" : "application/json"
			}
		}).complete(function(data, textStatus, jqXHR) {
			$.get("resources/request/modules/"+api.module+"/enabled", function(data) {
				if(data == true){
					$('#' + api.actionBtns.enableModulebtn).hide();
					$('#' + api.actionBtns.disableModulebtn).show();
				}else{
					$('#' + api.actionBtns.enableModulebtn).show();
					$('#' + api.actionBtns.disableModulebtn).hide();
				}
			});
		});
	});

	$("#" + api.actionBtns.disableModulebtn).click(function() {
		$.ajax({
			type : "PUT",
			url : api.baseUrl+"resources/request/modules/"+api.module+"/disable",
			data : {},
			dataType : "json",
			headers : {
				"Content-Type" : "application/json"
			}
		}).complete(function(data, textStatus, jqXHR) {
			$.get(api.baseUrl+"resources/request/modules/"+api.module+"/enabled", function(data) {
				if(data == true){
					$('#' + api.actionBtns.enableModulebtn).hide();
					$('#' + api.actionBtns.disableModulebtn).show();
				}else{
					$('#' + api.actionBtns.enableModulebtn).show();
					$('#' + api.actionBtns.disableModulebtn).hide();
				}
			});
		});
	});

	api.buildResponsePanel();
};

api.language = 'zh';
api.localstrings = {
	'zh' : {
		"action" : "动作",
		"state" : "状态",
		"code" : "响应码",
		"infolevel" : "消息级别",
		"taketime" : "耗时",
		"requestId" : "请求编号",
		"message" : "消息",
		"location" : "操作",
		"success" : "成功",
		"failure" : "失败",
		"info" : "消息",
		"warn" : "警告",
		"error" : "出错",
		"ms" : "毫秒",
		"queryresult" : "查询结果",
		"cancel" : "取消",
		"alert" : "提示",
		"selectapi" : "请选择API",
		"setreqparam" : "请设置请求参数",
		"paramunvalid" : "参数填写不正确",
	},
};

function __(s) {
	rc = s;
	if (typeof (api.language) != 'undefined'
			&& typeof (api.localstrings[api.language]) != 'undefined'
			&& api.localstrings[api.language][s]) {
		rc = api.localstrings[api.language][s];
	}
	return rc;
}

api.loadTree = function() {
	var api = this;
	$("#"+api.htmlids.serverlistdiv).hide();
	$("#"+api.htmlids.actiontreediv).show();
	var treediv = document.createElement("ul");
	$(treediv).tree({
		url : api.baseUrl+"resources/request/actions",
		method : "get",
		animate : true,
		onSelect : function(node) {
			var treeSelected = api.action;
			var node = $(this).tree('getSelected');
			if (node) {
				if($(this).tree('isLeaf', node.target)){
					$('#' + api.actionBtns.executebtn).show();
                    $('#' + api.actionBtns.cancelbtn).show();
                    $('#' + api.actionBtns.enableModulebtn).hide();
                    $('#' + api.actionBtns.disableModulebtn).hide();
					if (node.id != treeSelected) {
						api.module = null;
						api.action = node.id;
						api.clearRequest();
						api.clearResponse();

						api.buildRequestEditor();
					}
				}else{
					api.module = node.id;
					$('#' + api.actionBtns.executebtn).hide();
                    $('#' + api.actionBtns.cancelbtn).hide();
					api.action = null;
					api.clearRequest();
					api.clearResponse();
					$.get(api.baseUrl+"resources/request/modules/"+node.id+"/enabled", function(data) {
						if(data == true){
							$('#' + api.actionBtns.enableModulebtn).hide();
							$('#' + api.actionBtns.disableModulebtn).show();
						}else{
							$('#' + api.actionBtns.enableModulebtn).show();
							$('#' + api.actionBtns.disableModulebtn).hide();
						}
					});
				}
			}
		}
	});
	return treediv;
};

api.loadHistory = function() {
	var api = this;
	$("#" + this.htmlids.actionhistorydiv).empty();
	$.get(api.baseUrl+"resources/request/history", function(data) {
		$.each(data, function(index, value) {
			var historydiv = document.createElement("div");
			$(historydiv).addClass("historyLink");
			var icon = document.createElement("span");
			$(icon).css("padding", "5px 5px 5px 20px");
			$(icon).addClass("icon-application-link");
			$(historydiv).append(icon);
			var historyLink = document.createElement("a");
			$(historyLink).text(value.label + "_" + value.date);
			$(historydiv).append(historyLink);
			$("#" + api.htmlids.actionhistorydiv).append(historydiv);

			$(historydiv).hover(function() {
				$(this).addClass("linkHover");
			}, function() {
				$(this).removeClass("linkHover");
			});

			$(historyLink).click(function() {
				$(historydiv).siblings().removeClass("linkSelected");
				$(historydiv).addClass("linkSelected");

				api.action = value.action;
				api.clearRequest();
				api.clearResponse();

				api.request.source = value.request;
				api.reqSourceEditor.setValue(value.request);
				api.buildRequestEditor();
			});
		});
	});
};

/*
 * ******************************************************
 */
api.request = function(source, schema) {
	this.source = source;
	this.schema = schema;
};

api.buildRequestEditor = function() {
	var api = this;
	$.ajax(api.baseUrl+"resources/request/" + this.action).done(function(data) {
		var json = JSON.stringify(data.request, null, "   ");
		api.request.schema = json;

		$('#' + api.htmlids.reqschematextarea).val(json);
		$('#' + api.htmlids.reqdiv).tabs('select', 0);
		$('#' + api.actionBtns.executebtn).linkbutton('enable');
	});
};

api.clearRequest = function() {
	var api = this;
	$("#" + api.htmlids.reqsourcetextarea).val("");
	if (api.reqSourceEditor) {
		api.reqSourceEditor.setValue("");
	}
	$('#' + api.htmlids.reqschematextarea).val("");
	api.request.source=null;
	api.request.schema=null;
	api.reqEditor.clear();
};

/*
 * ******************************************************************
 * 
 */

api.response = function(state, code, infolevel, taketime, requestId, content, message) {
	this.state = state;
	this.code = code;
	this.infolevel = infolevel;
	this.taketime = taketime;
	this.requestId = requestId;
	this.content = content;
	this.message = message;
};

api.buildResponsePanel = function() {
	$("#" + this.htmlids.respheaderdiv).empty();

	var actionapi=this;
	var response = actionapi.response;
	var cls = response.infolevel;
	var actionBtnDiv=document.createElement("div");
	$(actionBtnDiv).append(this.action);
	var actionDiv = buildHeaderDiv(__("action"), actionBtnDiv);
	$("#" + this.htmlids.respheaderdiv).append(actionDiv);
	var stateDiv = buildHeaderDiv(__("state"), null == response.state ? ""
			: (response.state ? __("success") : __("failure")), cls);
	$("#" + this.htmlids.respheaderdiv).append(stateDiv);
	var codeDiv = buildHeaderDiv(__("code"), response.code, cls);
	$("#" + this.htmlids.respheaderdiv).append(codeDiv);
	var taketimeDiv = buildHeaderDiv(__("taketime"),
			response.taketime ? (response.taketime + __("ms")) : "", cls);
	$("#" + this.htmlids.respheaderdiv).append(taketimeDiv);
	
	var requestIdDiv = buildHeaderDiv(__("requestId"), response.requestId);
	$("#" + this.htmlids.respheaderdiv).append(requestIdDiv);
	
	var messageDiv = buildHeaderDiv(__("message"), response.message);
	$("#" + this.htmlids.respheaderdiv).append(messageDiv);

	if (null == response.content) {
		$("#" + this.htmlids.respcontentdiv).text("");
	} else if (typeof (response.content) != "object") {
		$("#" + this.htmlids.respcontentdiv).append(
				"<div>" + response.content + "</div>");
	} else {
		$.JSONView(response.content, $("#" + this.htmlids.respcontentdiv));
	}

};

api.clearResponse = function() {
	this.response = new api.response();
	this.buildResponsePanel();
};


function buildHeaderDiv(label, content, cls) {
	var headerDiv = document.createElement("div");
	$(headerDiv).addClass("responseHeader");
	var headerlableDiv = document.createElement("div");
	$(headerlableDiv).addClass("title");
	$(headerDiv).append(headerlableDiv);
	var headerlable = document.createElement("label");
	$(headerlable).text(label);
	$(headerlableDiv).append(headerlable);
	var headertext = document.createElement("div");
	$(headertext).addClass("msg");
	if (cls)
		$(headertext).addClass(cls);
	$(headertext).html(content);
	$(headerDiv).append(headertext);
	return headerDiv;
}

api.execute = function() {
	var sendAction = this.action;
	if (null == sendAction) {
		$.messager.alert(__("alert"), __("selectapi"), 'error');
		return;
	}
	var url = sendAction;

	var je = this.reqEditor;
	if (je.currentView != 'source') {
		je.updateJSON();
	}
	this.request.source = $("#" + this.htmlids.reqsourcetextarea).val();
	var data = this.request.source;

	var validator = $("#" + this.htmlids.reqform).form('validate');
	if (!validator) {
		$.messager.alert(__("alert"), __("paramunvalid"), 'error');
		return;
	}

	this.clearResponse();

	this.buildResponsePanel();

	var actionapi = this;
	$.ajax({
		type : "POST",
		url : actionapi.baseUrl+"resources/request/" + url,
		data : data,
		dataType : "json",
		headers : {
			"Content-Type" : "application/json"
		}
	}).done(
		function(data, textStatus, jqXHR) {
			actionapi.response = new api.response(data.success,
					data.code, data.level, data.takeTime, data.requestId,
					data.content,data.message);
			actionapi.buildResponsePanel();

		})
	.fail(
		function(jqXHR, textStatus, errorThrown) {
			var data = jqXHR.responseJSON;

			if (data) {
				actionapi.response = new api.response(data.success,
						jqXHR.status + "<br/>" + jqXHR.statusText,
						data.level, data.takeTime, data.requestId,
						data.content,data.message);
			} else {
				actionapi.response = new api.response(
						actionapi.action, false, jqXHR.status
								+ "<br/>" + jqXHR.statusText,
						"error", 0);
			}
			actionapi.buildResponsePanel();
		});
};

api.cancel = function(){
    var je = this.reqEditor;
    if (je.currentView != 'source') {
        je.updateJSON();
    }
    var requestData = $("#" + this.htmlids.reqsourcetextarea).val();
    var requestSource=JSON.parse(requestData);
    var actionapi = this;
    $.ajax({
        type : "PUT",
        url : actionapi.baseUrl+"resources/request/cancel",
        data : requestSource.requestId,
        dataType : "json",
        headers : {
            "Content-Type" : "application/json"
        }
    }).done(
        function(data, textStatus, jqXHR) {
            actionapi.response = new api.response(data.success,
                data.code, data.level, data.takeTime, data.requestId,
                data.content,data.message);
            actionapi.buildResponsePanel();

        })
        .fail(
        function(jqXHR, textStatus, errorThrown) {
            var data = jqXHR.responseJSON;

            if (data) {
                actionapi.response = new api.response(data.success,
                    jqXHR.status + "<br/>" + jqXHR.statusText,
                    data.level, data.takeTime, data.requestId,
                    data.content,data.message);
            } else {
                actionapi.response = new api.response(
                    actionapi.action, false, jqXHR.status
                    + "<br/>" + jqXHR.statusText,
                    "error", 0);
            }
            actionapi.buildResponsePanel();
        });
};