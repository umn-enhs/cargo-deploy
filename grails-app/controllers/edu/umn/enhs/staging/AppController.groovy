package edu.umn.enhs.staging

import grails.plugins.springsecurity.Secured

@Secured(['ROLE_NCS_IT'])
class AppController {

	def warFinderService

	def index = {

		def warFiles = warFinderService.findWars()
		def deployedApps = warFinderService.getAllDeployedApps()
		def config = warFinderService.getConfig()
		def servers = []
		config.each{ server, settings ->
			servers.add([server:server, url: settings.url])
		}

		warFiles = warFinderService.updateDeployement(warFiles, deployedApps)

		[ warFiles: warFiles, deployedApps: deployedApps, servers: servers ]
	}

	def deploy = {

		def fileName = params.id
		def serverName = params.server

		def result = warFinderService.deployWebApp(fileName + '.war', serverName)

		flash.message = "Deploy Result: ${result}"
		redirect(action:"index")
		return
	}
	def undeploy = {

		def context = params.id
		def serverName = params.server

		def result = warFinderService.unDeployWebApp(context, serverName)

		flash.message = "Undeploy Result: ${result}"
		redirect(action:"index")
		return
	}
}
