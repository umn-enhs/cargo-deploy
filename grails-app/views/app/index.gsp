<html>
    <head>
        <title>Welcome to Grails</title>
        <meta name="layout" content="main" />
    </head>
    <body>
        <div id="body">
			<h1>Welcome to Cargo Deploy <sec:username/>!</h1>
			<p>Server - <g:createLink controller="app" absolute="true" /></p>
			<hr/>
			<h2>War Files</h2>
			<g:each var="w" in="${warFiles}">
				<hr/>
				<h3>${w} - ${w.applicationName.replaceFirst('/','')} </h3>
				<h4>Test this app @ <a href="${g.createLink(controller:'app', absolute:true)}${w.context}"><g:createLink controller="app" absolute="true" />${w.context}</a></h3>
				<p>Plugins: <strong style="font-size: 0.7em;">${w.plugins?.join(', ')}</strong></p>
				<g:if test="${w.deployedApp}">
					<p>Deployed To?: <strong>${a.server}${a.context}</strong></p>
				</g:if>
				<g:if test="${w.deployable}">
					<div style="text-align:right;">
						<g:each var="h" in="${servers}">
						<g:link action="deploy" params="${ [server: h.server ] }" id="${w.context}" onclick="return confirm('Are you sure???')">
						<p> Deploy to ${h.url}/${w.context} </p>
						</g:link>
						</g:each>
					</div>
				</g:if>
			</g:each>
			<hr/>

			<h2>Deployed Apps</h2>
			<hr/>
			<g:each var="a" in="${deployedApps}">
			<p>Server: <strong>${a.url}${a.context}</strong> - 
				${a.status} -
				<g:link action="start" params="${ [server: a.server ] }" id="${a.appName}" onclick="return confirm('Are you sure?')">Start</g:link>,
				<g:link action="stop" params="${ [server: a.server ] }" id="${a.appName}" onclick="return confirm('Are you sure?')">Stop</g:link>,
				<g:link action="undeploy" params="${ [server: a.server ] }" id="${a.appName}" onclick="return confirm('Are you sure?')">Undeploy</g:link>
			</p>
			</g:each>
        </div>
    </body>
</html>
