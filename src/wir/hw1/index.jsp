<%@page contentType="text/html; charset=UTF-8"%>
<%@page pageEncoding="UTF-8"%>
<%@page import = "java.util.*, wir.hw1.*, wir.hw1.database.*, wir.hw1.index.*, wir.hw1.model.*" %>
<html>
    <head>
		<meta charset="UTF-8">
		<title>Small Search Engine</title>
		<style type="text/css"> a {text-decoration: none} </style>
	</head>
    <body link="blue" vlink="blue"> <center>
	
	<%
		// Initialization
		final int MAX_RESULT = 20;
		Class.forName("com.mysql.jdbc.Driver");
        SearchEngine searchEngine = SearchEngine.getInstance();
		String query = request.getParameter("query");
		query = (query == null) ? "" : query;
		String selectedModel = request.getParameter("model");
		SearchEngine.ModelType model = ((selectedModel == null) || (selectedModel.equals("boolean"))) ? SearchEngine.ModelType.BOOLEAN_MODEL : SearchEngine.ModelType.BOOLEAN_MODEL;
	%>

	<a href="http://140.116.154.121:8080/SearchEngine/index.jsp"><img src="logo.jpg" width="250"/></a>
	<br/><br/>
	<form method="get">
		<input type="text" name="query" size="60" value="<%= query %>" style="line-height: 20px;"/> &nbsp;
		<input type="submit" value="搜尋" style="line-height: 20px;"/>
		<br/><br/>
		<input type="radio" name="model" value="boolean" <% out.println(model==SearchEngine.ModelType.BOOLEAN_MODEL ? "checked" : ""); %> />Boolean Model &nbsp;
		<input type="radio" name="model" value="vector" <% out.println(model==SearchEngine.ModelType.VECTOR_MODEL ? "checked" : ""); %>/>Vector Model
	</form>
	<br/>
	
	<% 
		if ((query != null) && (query.length() > 0)) {
			// Search database
			long startTime = Calendar.getInstance().getTimeInMillis();
			List<Document> results = searchEngine.search(query, model);
			double elapsedTime = (Calendar.getInstance().getTimeInMillis() - startTime) / 1000.0;
			out.println(String.format("<font color='gray' size='3'>(Elapsed time: %.2f seconds)</font> <br/><br/>", elapsedTime));
			
			if (results.size() == 0) {
				out.println("No Documents Found. <br/>");
				return;
			}
			
			int count = 0;
			for (Document result : results) {
				out.println(String.format("<a href='http://140.116.154.121/docs/%s' target='_blank'>%s</a><br/>", result.getName(), result.getName()));
				String tag = "<font color=red>";
				StringBuilder snippet = new StringBuilder(result.getSnippet());				
				int index = snippet.indexOf(query);
				if (index >= 0)
					snippet.insert(index, tag).insert(index+query.length()+tag.length(), "</font>").append(" ...");				
				out.println(String.format("<table border='1' width='550'><tr><td>%s</td></tr></table><p>", snippet));
				if (++count == MAX_RESULT)
					break;
			}
		}
     %>

    </center></body>
</html>
