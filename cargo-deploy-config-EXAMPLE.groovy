// cargo.server.names = [ 'https://secure.ncs.umn.edu', 'https://www.ncs.umn.edu' ]
cargo.servers = [ 
	app: [
		url: 'https://app.example.org',
		type: 'tomcat',
		version: 6,
		username: 'TOMCAT_MANAGER_USERNAME',
		password: 'TOMCAT_MANAGER_PASSWORD' ],
	www: [
		url: 'https://www.example.org',
		type: 'tomcat',
		version: 6,
		username: 'TOMCAT_MANAGER_USERNAME',
		password: 'TOMCAT_MANAGER_PASSWORD' ] ]

