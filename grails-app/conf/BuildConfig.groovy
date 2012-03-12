grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
//grails.project.war.file = "target/${appName}-${appVersion}.war"
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
        grailsPlugins()
        grailsHome()
        grailsCentral()
        mavenRepo "http://artifact.ncs.umn.edu/plugins-release"
    }
    dependencies {
		// Needed to get ant libraries in WAR file
		compile "org.apache.ant:ant:1.7.1"
		compile "org.apache.ant:ant-launcher:1.7.1"
        runtime 'mysql:mysql-connector-java:5.1.18'
    }
    plugins {
		compile ":tomcat:${grailsVersion}"

		compile ":spring-security-core:1.2.7.3"
		compile ":spring-security-ldap:1.0.6"
		compile ":spring-security-shibboleth-native-sp:1.0.3"
		compile ":ncs-web-template:0.2"
	}
}
