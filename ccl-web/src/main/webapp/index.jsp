<%--
  Created by IntelliJ IDEA.
  User: Administrator
  Date: 15-11-3
  Time: 上午9:12
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!-- 支持EL表达式 -->
<%@ page isELIgnored="false" %>
<html>
<body>
<h2>Hello World!</h2>
<form action="mytest" method="post">
    <input name="name">
    return:${name}
    <input value="提交" type="submit">
</form>
</body>
</html>
