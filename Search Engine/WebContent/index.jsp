<%@page pageEncoding="UTF-8"%>
<%@page contentType="text/html; charset=UTF-8"%>
<%@page import = "java.util.*, java.io.*, java.net.URL" %>
<%@page import = "wir.hw1.*, wir.hw1.data.*, wir.hw1.database.*, wir.hw1.index.*, wir.hw1.model.*, wir.hw1.util.*" %>
<%@page import = "org.apache.commons.io.*" %>
<%@page import = "org.json.*" %>

<html>
    <head>
		<meta charset="UTF-8">
		<title>Small Search Engine</title>
		<style type="text/css"> 
			a { text-decoration: none } 
			html, body { text-align: center; width: 90% }
			table { margin: 0 auto; }
		</style>
	</head>
    <body link="blue" vlink="blue"> 
	
	<%
		/* Initialization */ 
		SearchEngine searchEngine = (SearchEngine) request.getSession().getAttribute("instance");
		if (searchEngine == null) {
			String root = getServletContext().getRealPath("/WEB-INF");
			JSONObject conf = new JSONObject(FileUtils.readFileToString(new File(root, "data/config.json")));
			conf.put("root", root);
			request.getSession().setAttribute("instance", SearchEngine.getInstance(conf));
		}
		
		final int MAX_NUM_RESULT = 20;
		
		String query = request.getParameter("query");
		query = (query == null) ? "" : query;
		
		String selectedModelType = request.getParameter("model");
		SearchEngine.ModelType model = ((selectedModelType == null) || (selectedModelType.equals("boolean"))) ? SearchEngine.ModelType.BOOLEAN_MODEL : SearchEngine.ModelType.VECTOR_MODEL;
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
		/* Search database */
		if ((query != null) && (query.length() > 0)) {
			SearchResult result = searchEngine.search(query, searchEngine.createModel(model));
			List<Document> rankings = result.getDocuments();
			double elapsedTime = result.getElapsedMillis() / 1000.0;
			
			if (result.getDocuments().size() == 0) {
				out.println("No Documents Found. <br/>");
				return;
			}
			
			out.println(String.format("<font color='gray' size='3'>(Elapsed time: %.2f seconds)</font> <br/><br/>", elapsedTime));
			
			int count = 0;
			for (Document doc : rankings) {
				out.println(String.format("<a href='http://140.116.154.121/docs/%s' target='_blank'>%s</a><br/>", doc.getName(), doc.getName()));
				String tag = "<font color=red>";
				StringBuilder snippet = new StringBuilder(doc.getSnippet());				
				int index = snippet.indexOf(query);
				if (index >= 0)
					snippet.insert(index, tag).insert(index+query.length()+tag.length(), "</font>").append(" ...");				
				out.println(String.format("<table border='1' width='550'><tr><td>%s</td></tr></table><p>", snippet));
				
				if (++count >= MAX_NUM_RESULT)
					break;
			}
		}
     %>

    </body>
</html>
