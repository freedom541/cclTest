var api = function (baseUrl) {
    this.baseUrl = baseUrl;

    this.loadData = api.loadData;
    this.appendFormData=api.appendFormData;

    this.init = api.init;

    this.init();
};

api.init = function () {
    var resourceApi = this;

    $("#api_action_form_submit_btn").click(function () {
        if (!$("#api_action_form").form("validate")) {
            $.messager.alert('提示', '參數驗證不通過！', 'warning');
            return;
        }
        var d = {};
        $("#api_action_form").find('input,textarea').each(function () {
            if (this.value != "") {
                d[this.name] = this.value;
            }
        });
        d.workerClientId = $("#api_action_workerClientId").combobox("getValue");
        console.log(d);
        var data = {content: {}};
        data.content.configFileForm = d;
        var url = resourceApi.baseUrl + "resources/request/ConfigAdmin@createConfig";
        if (d.id) {
            url = resourceApi.baseUrl + "resources/request/ConfigAdmin@modifyConfig";
        }
        $.ajax({
            type: "POST",
            url: url,
            dataType: "json",
            data: JSON.stringify(data),
            headers: {
                "Content-Type": "application/json"
            }
        }).done(function (res) {
            resourceApi.loadData();
        });
    });

    var codeEditor = CodeMirror.fromTextArea(document
        .getElementById("api_action_configFileContent"), {
        styleActiveLine : true, // line选择是是否加亮
        styleSelectedText : true,
        lineNumbers : true, // 是否显示行数
        lineWrapping : true, // 是否自动换行
        matchBrackets : true,
        theme : "eclipse",
        mode: "yaml"
    });
    codeEditor.on("change", function() {
        $('#api_action_configFileContent').val(codeEditor.getValue());
    });
    resourceApi.yamlEditor=codeEditor;

    resourceApi.loadData();

};

api.loadData = function(){
    var resourceApi = this;
    var data = {content: {}};
    $.ajax({
        type: "POST",
        url: resourceApi.baseUrl + "resources/request/ConfigAdmin@findConfigList",
        dataType: "json",
        data: JSON.stringify(data),
        headers: {
            "Content-Type": "application/json"
        }
    }).done(
        function (rs) {
            $("#api_action_list_div").datagrid({
                data: rs.content,
                striped: true,
                fitColumns: true,
                singleSelect: true,
                columns: [[
                    {field: 'clientProject', title: '項目', width: 100},
                    {field: 'clientProfile', title: '环境', width: 60},
                    {field: 'clientHost', title: '主机', width: 80},
                    {field: 'clientPort', title: '端口', width: 50},
                    {field: 'configFileName', title: '配置文件名称', width: 150},
                    {field: 'version', title: '当前版本号', width: 100}
                ]],
                toolbar: [{
                    iconCls: 'icon-add',
                    handler: function () {
                        $("#api_action_list_div").datagrid("clearSelections");
                        resourceApi.appendFormData({id:"",workerClientId:"",profile:"",configFileName:"",configFileContent:""});
                    }
                },'-',{
                    iconCls: 'icon-application-go',
                    handler: function () {
                        var selectedRowData = $("#api_action_list_div").datagrid("getSelected");
                        if (null == selectedRowData) {
                            $.messager.alert('提示', '請選擇一行！');
                            return;
                        }
                        data.content = {clientId: selectedRowData.workerClientId,
                            configFileId: selectedRowData.id};
                        $.ajax({
                            type: "POST",
                            url: resourceApi.baseUrl + "resources/request/ConfigAdmin@pushFile",
                            dataType: "json",
                            data: JSON.stringify(data),
                            headers: {
                                "Content-Type": "application/json"
                            }
                        }).done(function () {
                            $.messager.alert('提示', '推送成功！');
                        });
                    }
                }],
                onSelect: function (rowIndex, rowData) {
                    resourceApi.appendFormData(rowData);
                }
            });
        });

    $.ajax({
        type: "POST",
        url: resourceApi.baseUrl + "resources/request/DataManage-WorkerClient@listByExample",
        dataType: "json",
        data: JSON.stringify(data),
        headers: {
            "Content-Type": "application/json"
        }
    }).done(
        function (rs) {
            var clientList=[];
            for(var i in rs.content){
                var client = rs.content[i];
                var c={};
                c.id=client.id;
                c.host="["+client.project+" > "+client.profile+" > "+client.host+"] ["+client.port+"]";
                clientList.push(c);
            }

            $("#api_action_workerClientId").combobox({
                valueField: 'id',
                textField: 'host',
                data: clientList
            });

            resourceApi.appendFormData({id:"",workerClientId:"",profile:"",configFileName:"",configFileContent:""});
        });

};

api.appendFormData = function (config) {
    //$("#api_request_div > div").hide();
    //$("#api_action_form_p_div").show();
    $("#api_action_form_div input[name='id']").val(config.id);
    $("#api_action_workerClientId").combobox("setValue",config.workerClientId);
    $("#api_action_form_div input[name='configFileName']").val(config.configFileName);
    //$("#api_action_form_div textarea[name='configFileContent']").val(config.configFileContent);
    this.yamlEditor.setValue(config.configFileContent);

    $("#api_action_form .easyui-validatebox").each(function () {
        $(this).validatebox("validate");
    });
};