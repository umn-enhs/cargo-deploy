package edu.umn.enhs.staging

import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.codehaus.cargo.container.property.RemotePropertySet
//import org.codehaus.cargo.container.glassfish.GlassFish3xRuntimeConfiguration
import org.codehaus.cargo.container.tomcat.TomcatRuntimeConfiguration
import org.codehaus.cargo.container.tomcat.Tomcat6xRemoteDeployer
import org.codehaus.cargo.container.tomcat.Tomcat6xRemoteContainer
import org.codehaus.cargo.container.tomcat.TomcatWAR
import org.codehaus.cargo.container.deployable.WAR

class WarFinderService {

    static transactional = true

	def grailsApplication

    def findWars() {

		def webAppsFolder = getWebAppsFolder()

		// Find the war files in the folder
		def warFiles = webAppsFolder
			.listFiles( {dir, file -> file ==~ /.*?\.war/ } as FilenameFilter )

		// Return a list of WarFile objects
		return warFiles.collect{ new WarFile(it.absolutePath) }
    }

	def getWebAppsFolder() {
		// Get the application server's servletContext
		def servletContext = ServletContextHolder.getServletContext()

		// Find the applications install path
		def appPath = new File(servletContext.getRealPath('/'))

		// Find the app folder beneath it
		def webAppsFolder = new File(appPath.parent)

		// TODO: Disable this
		if (true) {
			webAppsFolder = new File('/var/lib/tomcat6/webapps')
		}

		return webAppsFolder
	}

	
	def startWebApp(String context, String serverName) {
		serviceWebApp(context, serverName, 'start')
	}
	def stopWebApp(String context, String serverName) {
		serviceWebApp(context, serverName, 'stop')
	}
	def unDeployWebApp(String context, String serverName) {
		serviceWebApp(context, serverName, 'undelploy')
	}

	private def serviceWebApp(String context, String serverName, String action) {
		Boolean returnStatus = false

		def deployer = getDeployer(serverName)
		if (deployer) {
			// create fake war name
			def warName = context + '.war'

			// creat fake war object
			def deployable = new WAR(warName)

			def deployedApps = getDeployedApps(deployer)

			def expectedStatus = 'UNKOWN'

			if (action == 'undeploying') {
				expectedStatus = 'running'
			} else if (action == 'start') {
				expectedStatus = 'stopped'
			} else if (action == 'stop') {
				expectedStatus = 'running'
			}

			if (deployedApps.find{ it.appName == context && it.status == expectedStatus}) {
				if (action == 'undeploy') {
					log.info "Undeploying ${context} on ${serverName}"
					returnStatus = deployer.undeploy(deployable)
				} else if (action == 'start') {
					log.info "Starting ${context} on ${serverName}"
					returnStatus = deployer.start(deployable)
				} else if (action == 'stop') {
					log.info "Stopping ${context} on ${serverName}"
					returnStatus = deployer.stop(deployable)
				}
			} else {
				log.info "app isn't ${expectedStatus}, can't ${action}!"
			}
		}
		return returnStatus
	}

	def deployWebApp(String warFileName, String serverName) {

		Boolean returnStatus = false

		if (warFileName.contains('/')) {
			throw new java.lang.IllegalAccessException(warFileName)
		} else {
			def webAppsFolder = getWebAppsFolder()

			def warFilePath = webAppsFolder.absolutePath + '/' +  warFileName
			def warFile = new WarFile(warFilePath)

			if (warFile && warFile.deployable) {
				log.info "Deploying ${warFile} to ${serverName}..."

				def deployer = getDeployer(serverName)
				if (deployer) {
					def deployable = new TomcatWAR(warFile.warFilePath)

					def deployedApps = getDeployedApps(deployer)

					if (deployedApps.find{ it.appName == warFile.context}) {
						log.info "Redeploying ${warFile.context} on ${serverName}"
						returnStatus = deployer.redeploy(deployable)
					} else {
						log.info "Deploying ${warFile.context} on ${serverName}"
						returnStatus = deployer.deploy(deployable)
					}
				}
			}
		}
		return returnStatus
	}

	def getAllDeployedApps() {

		def config = getConfig()
		def allApps = new HashSet<DeployedApp>()

		config.each{ server, settings ->
			def deployer = getDeployer(server)
			allApps.addAll(getDeployedApps(deployer))
		}

		return allApps.sort{ it.url + '/' + it.context }
	}
	
	def getDeployedApps(deployer) {

		Set<DeployedApp> appList = new HashSet<DeployedApp>()

		if (deployer) {

			def config = getConfig()
			def tomcatConfiguration = deployer.getConfiguration()
			def url = tomcatConfiguration.getPropertyValue(RemotePropertySet.URI).replace(/\/manager/, '')
			def serverName = ""

			config.each{ server, setting ->
				if ( setting.url == url ) {
					serverName = server
				}
			}

			def deployerList = deployer?.list()
			if (deployerList) {
				def applications = deployerList.replace('\r','')?.split('\n')

				String result = applications[0]
				if (result.startsWith('OK -') ) {
					//Success
					applications.each{ line ->
						def parts = line.split(':')
						if (parts.length == 4) {
							def appName = parts[3]
							if (! appName.contains('/') ) {
								def deployedApp = new DeployedApp()
								deployedApp.server = serverName
								deployedApp.url = url
								deployedApp.context = parts[0]
								deployedApp.appName = appName
								deployedApp.status = parts[1]
								appList.add(deployedApp)
							}
						}
					}
				} else {
					log.warn "FAIL, got back: ${result}"
				}
			}
		}
		return appList
	}

	def updateDeployement(warFiles, deployedApps) {
		warFiles.each{ warFile ->
			def deployedApp = deployedApps.find{ it.context == warFile.context }
			if (deployedApp) {
				warFile.deployedApp = deployedApp
			}
		}
		return warFiles
	}

	def getDeployer(String configName) {
		def config = getConfig()
		def server = config[configName]


		if (server?.type == 'tomcat' && server?.version == 6) {
			def serverURL = new URL(server.url + '/manager').toExternalForm()

			def tomcatConfiguration = new TomcatRuntimeConfiguration() 

			/* Supports:
			 * cargo.remote.username = true
			 * cargo.remote.password = true
			 * cargo.remote.uri = true
			 * cargo.servlet.port = true
			 * cargo.hostname = true
			 * cargo.protocol = true
			 */
			tomcatConfiguration.setProperty(RemotePropertySet.USERNAME, server.username)
			tomcatConfiguration.setProperty(RemotePropertySet.PASSWORD, server.password)
			tomcatConfiguration.setProperty(RemotePropertySet.URI, serverURL)
			//tomcatConfiguration.setProperty(TomcatPropertySet.MANAGER_URL, serverURL)

			def tomcatContainer = new Tomcat6xRemoteContainer(tomcatConfiguration)

			return new Tomcat6xRemoteDeployer(tomcatContainer)
		}
	}

	def getConfig() {
		return grailsApplication.config.cargo.servers
	}
}
