package edu.umn.enhs.staging

class WarFile implements Serializable {

	File warFile
	DeployedApp deployedApp
	String context
	String warFileName
	String warFilePath
	String appFolderPath
	String webXmlPath
	String grailsXmlPath
	String applicationName
	Boolean deployable = false
	Set<String> plugins = [] as Set

	public WarFile(String filePath) {

		def war = new File(filePath)

		if (war.exists() && war.isFile() && war.canRead() ) {

			// We found a readable war file
			warFile = war
			warFilePath = warFile.absolutePath
			warFileName = warFile.name

			appFolderPath = warFilePath.reverse().replaceFirst(/raw\./, '').reverse()

			def appFolder = new File(appFolderPath)

			if (appFolder.exists() && appFolder.isDirectory()) {
				appFolderPath = appFolder.absolutePath
				context = appFolder.name

				def webXml = new File(appFolderPath + '/WEB-INF/web.xml')
				if ( webXml.exists() && webXml.isFile() && webXml.canRead() ) {
					// We found a web XML file
					webXmlPath = webXml.absolutePath

					def webXmlContent = new XmlSlurper().parseText(webXml.getText())
					def displayName = webXmlContent.'display-name'.text()
					if (displayName) {
						applicationName = displayName
					}
					deployable = true
				}

				def grailsXml = new File(appFolder.absolutePath + '/WEB-INF/grails.xml')
				if ( grailsXml.exists() && grailsXml.isFile() && grailsXml.canRead() ) {
					// We found a web XML file
					grailsXmlPath = grailsXml.absolutePath

					def grailsXmlContent = new XmlSlurper().parseText(grailsXml.getText())
					def pluginList = grailsXmlContent.plugins.plugin.each{ plugin ->
						String newPluginName = plugin.text().reverse()
							.replaceFirst(/nigulP/,'').reverse()
						plugins.add( newPluginName )
					}
				}
			}
		} else {
			throw new java.io.IOException("Unable to read WAR file : ${filePath}")
		}
	}

	String toString() { warFileName }

}

