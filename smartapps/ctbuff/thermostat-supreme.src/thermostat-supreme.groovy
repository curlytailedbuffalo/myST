/**
*  Thermostat Supreme
*
*  Copyright 2016 Curlytailedbuffalo
*
*  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License. You may obtain a copy of the License at:
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
*  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
*  for the specific language governing permissions and limitations under the License.
*
*/

definition(
    name: "Thermostat Supreme",
    namespace: "ctbuff",
    author: "Curlytailedbuffalo",
    description: "This smart app allows you to program your z-wave thermostat with schedules, motion triggers, presence triggers, and door/window sensor triggers.",
    category: "Green Living",
    iconUrl: "https://raw.githubusercontent.com/curlytailedbuffalo/myST/master/media/thermostat-supreme-120px.png",
    iconX2Url: "https://raw.githubusercontent.com/curlytailedbuffalo/myST/master/media/thermostat-supreme-240px.png",
    iconX3Url: "https://raw.githubusercontent.com/curlytailedbuffalo/myST/master/media/thermostat-supreme.png",
    singleInstance: true)

preferences {
	page(name: "mainPage", title: "Thermostat Supreme", content: "mainPage", install: true, uninstall:false)
    page(name: "mainThermostatPage", title: "Thermostat Supreme", content: "mainThermostatPage", install: true, uninstall:false)
	page(name: "settingsPage", title: "Thermostat Settings", content: "settingsPage", install: true, uninstall:false)
    page(name: "setPointsPage", title: "Setpoint Temperatures", content: "setPointsPage", install: false, uninstall:false)
    page(name: "triggersPage", title: "Temperature Sensors", content: "triggersPage", install: true, uninstall:false)
    page(name: "requireTriggersPage", title: "Require Trigger", content: "requireTriggersPage", install: true, uninstall:false)
    page(name: "aboutPage", title: "About ${textAppName()}", content: "aboutPage", install: false, uninstall:true)
    page(name: "aboutLicensePage", title: "${textAppName()} License", content: "aboutLicensePage", install: false, uninstall:true)
    page(name: "aboutDetailsPage", title: "${textAppName()} Details", content: "aboutDetailsPage", install: false, uninstall:true)
    page(name: "aboutInstructionsPage", title: "${textAppName()} Instructions", content: "aboutInstructionsPage", install: false, uninstall:true)
    
    page(name: "aboutChildAppsPage", title: "About Child Apps", content: "aboutChildAppsPage", install: false, uninstall:true)
    page(name: "aboutChildAppsTempPage", title: "About ${textChildTempAppName()}", content: "aboutChildAppsTempPage", install: false, uninstall:true)
    page(name: "aboutChildAppTempInstructionsPage", title: "${textAppName()} Instructions", content: "aboutChildAppTempInstructionsPage", install: false, uninstall:true)
    page(name: "aboutChildAppTempDetailsPage", title: "${textAppName()} Details", content: "aboutChildAppTempDetailsPage", install: false, uninstall:true)

}

def logsEnabled() {
	state.logging = settings?.debugLogging
	return settings?.debugLogging
}

def mainPage() {

    return dynamicPage(name: "mainPage", title: "Thermostat Supreme") {
        section("Main Thermostat Details") {
            href ("mainThermostatPage", title: "Main Thermostat", description: "Main settings and details for the thermostat", image: "https://raw.githubusercontent.com/curlytailedbuffalo/myST/master/media/thermostat-supreme-thermostat.png")
            paragraph "Current Thermostat: ${configuredThermostat()}"
        }
        section("External Temperature Sensors") {
            href ("triggersPage", title: "Temperature Sensors", description: "Tap to add external temperature sensors and other sensor triggers.", image: "https://raw.githubusercontent.com/curlytailedbuffalo/myST/master/media/thermostat-supreme-temp-sensors.png")
            paragraph "Configured Temp Sensors:\n ${configuredTemperatureSensors()}"
        }
        section("About") {
            href ("aboutPage", title: "About", description: "Thermostat Supreme info and uninstall.",  image: "https://raw.githubusercontent.com/curlytailedbuffalo/myST/master/media/thermostat-supreme-info.png")
        }
    }
}

def mainThermostatPage() {

    return dynamicPage(name: "mainThermostatPage", title: "Supreme Thermostat") {
        section("Choose thermostat") {
            input name: "thermostat", type: "capability.thermostat", multiple: false, required: true
        }
        section("Settings") {
            href ("settingsPage", title: "Settings", description: "Tap to review thermostat settings.")
        }
        section("About") {
            paragraph "Sensor: ${tempLastRunAction()}"
            paragraph "Thermostat: ${lastRunAction()}"
        }
    }
}

def triggersPage(){
    return dynamicPage(name: "triggersPage", title: "Temperature Sensors") {
        section("Temperature Sensor(s)") {
        }
        section("Add Temperature Sensor") {
        	paragraph "Temperature sensors can be used to monitor rooms of the house and trigger the thermostat at configured setpoints.\nThey can also require presence/motion sensors to be active or door/window sensors to be closed."
            app "Thermostat Supreme Temp Sensor", "ctbuff", "Thermostat Supreme Temp Sensor", title: "Add Temperature Sensor", multiple: true, install: true
        }
    }
}

def requireTriggersPage(){
    return dynamicPage(name: "requireTriggersPage", title: "Require Triggers") {
        section("Global Settings") {
            input "requireAll", "bool", title: "Require All:", description: "If enabled all require triggers must be active, if disabled only 1 must be active",  required: false
        }
        section("Require Trigger(s)") {
        }
        section("Add Require Trigger") {
        	paragraph "Require triggers are sensors that must be active to use this temperature sensor.\nThese can be either motion sensors or presence sensors."
            app "Thermostat Supreme Require Trigger", "ctbuff", "Thermostat Supreme Require Trigger", title: "Add Require Trigger", page: "mainPage", multiple: true, install: true
        }
    }
}

def settingsPage() {
    return dynamicPage(name: "settingsPage", title: "Thermostat Settings") {
        section("") {
            input name: "mainSensor", type: "enum", title: "Main Sensor:", options: [none: "None", heat: "Heat", cool: "Cool"], description: "",  required: true
        }
        section("Setpoint Temperatures") {
        	if(settings?.heatSetPoint && settings?.heatVariance && settings?.coolSetPoint && settings?.coolVariance){
           		href ("setPointsPage", title: "Configure Setpoints")
            }else{
            	href ("setPointsPage", title: "Configure Setpoints", required:true)
            }
        }
        section("Negate Sensor(s)") {
        	input name: "negateDelay", type: "number", title: "Sensor Delay", description: "How many seconds the sensors must be opened to negate this temp sensor.", required: true
            input (name: "negate", type: "capability.contactSensor", required: false, multiple: true)
        }
        section("Require Trigger(s)") {
			href ("requireTriggersPage", title: "Require Triggers")
		}
        section("Other") {
            input(name: "debugLogging", title:"Enable Logging", type: "bool", required:false, defaultValue: true, description: "")
        }
    }
}


def setPointsPage(){
	return dynamicPage(name: "setPointsPage", title: "Setpoint Temperatures") {
        section("") {
            input name: "heatSetPoint", type: "decimal", title: "Heat Setpoint", required: true
            input name: "heatVariance", type: "decimal", title: "Heat Variance", range: "0..5", required: true
            input name: "coolSetPoint", type: "decimal", title: "Cool Setpoint", required: true
            input name: "coolVariance", type: "decimal", title: "Cool Variance", range: "0..5", required: true
        }
    }
}

def aboutPage() {
   return dynamicPage(name: "aboutPage", title: "About ${textAppName()}") {
        section {
            paragraph "App Name: ${textAppName()}"
            href ("aboutDetailsPage", title: "${textAppName()} Details")
        }
        section("Instructions") {
            href ("aboutInstructionsPage", title: "${textAppName()} Instructions")
        }
        section("Child Apps") {
            href ("aboutChildAppsPage", title: "Child App Info")
        }
        section("License") {
            href ("aboutLicensePage", title: "${textAppName()} License")
        }
        section("Tap button below to remove all thermostat settings and children smartapps."){
        }
	}
}

def aboutChildAppsPage() {
   return dynamicPage(name: "aboutChildAppsPage", title: "About Child Apps") {
        section("Child Temperature Sensor") {
            href ("aboutChildAppsTempPage", title: "About ${textChildTempAppName()}")
        }
        section("Child Require Sensor") {
            href ("aboutChildAppsTempPage", title: "About ${textChildTempAppName()}")
        }
	}
}

def aboutInstructionsPage() {
   return dynamicPage(name: "aboutInstructionsPage", title: "${textAppName()} Instructions") {
        section {
            paragraph textHelp()
        }
	}
}


def aboutLicensePage() {
   return dynamicPage(name: "aboutLicensePage", title: "${textAppName()} License") {
        section {
            paragraph "${textCopyright()}\n\n${textLicense()}\n"
        }
	}
}

def aboutDetailsPage() {
   return dynamicPage(name: "aboutDetailsPage", title: "${textAppName()} Details") {
        section {
            paragraph "App Name:\n ${textAppName()}\n\n${textVersion()}\n\n${textContributors()}\n\n"
        }
	}
}

def aboutChildAppsTempPage() {
   return dynamicPage(name: "aboutChildAppsTempPage", title: "About ${textChildTempAppName()}") {
        section {
            paragraph "App Name: ${textChildTempAppName()}"
            href ("aboutChildAppTempDetailsPage", title: "${textChildTempAppName()} Details")
        }
        section("Instructions") {
            href ("aboutChildAppTempInstructionsPage", title: "${textChildTempAppName()} Instructions")
        }
        section("License") {
            href ("aboutLicensePage", title: "${textChildTempAppName()} License")
        }
	}
}

def aboutChildAppTempInstructionsPage() {
   return dynamicPage(name: "aboutChildAppTempInstructionsPage", title: "${textChildTempAppName()} Instructions") {
        section {
            paragraph textChildTempHelp()
        }
	}
}

def aboutChildAppTempDetailsPage() {
   return dynamicPage(name: "aboutChildAppTempDetailsPage", title: "${textChildTempAppName()} Details") {
        section {
            paragraph "App Name:\n ${textChildTempAppName()}\n\n${textVersion()}\n\n${textContributors()}\n\n"
        }
	}
}

def installed() {
    logEvent("installed", "with settings: ${settings}", "info")
    
    state.triggers = [:]
    state.triggers[thermostat.deviceNetworkId] = "neutral"
    state.heatStartPoint = heatSetPoint - heatVariance
    state.coolStartPoint = coolSetPoint + coolVariance
    logEvent("updated", "heat start point = ${state.heatStartPoint}", "debug")
    logEvent("updated", "cool start point = ${state.coolStartPoint}", "debug")
    initialize()
}

def updated() {
    logEvent("updated", "with settings: ${settings}", "info")
    
    state.heatStartPoint = heatSetPoint - heatVariance
    state.coolStartPoint = coolSetPoint + coolVariance
    logEvent("updated", "heat start point = ${state.heatStartPoint}", "debug")
    logEvent("updated", "cool start point = ${state.coolStartPoint}", "debug")
    unsubscribe()
    initialize()
}

def initialize() {
    if(location.timeZone == null){
       log.warn "Location is not set! Go to your ST app and set your location"
    }
    subscribeToEvents()
    def negateTriggers = [:]
    
    if(settings?.negate){
        settings?.negate.each{ dev ->
        	negateTriggers[dev.deviceNetworkId] = dev.currentValue("contact")
            if (dev.currentValue("contact") == "open"){
                negateTriggers[dev.deviceNetworkId] = 'open'
            }
        }
        state.negateTriggers = negateTriggers
    }
    temperatureHandler([value: "init"])
    
}

def setStateNegateTrigger(Map triggers){
	state.negateTriggers = triggers
}

def setStateNegateTrigger(String key, String trigger){
	state.negateTriggers[key] = trigger
}

def subscribeToEvents() {
    logEvent("subscribeToEvents", "Starting", "info")
    
    subscribe(thermostat, "temperature", temperatureHandler)
    logEvent("subscribeToEvents", "subscribing to temperature change", "info")
    if(settings?.negate){
        logEvent("subscribeToEvents", "subscribing to contact sensors [negate]", "debug")
        subscribe(negate, "contact", contactHandler)
    }
    def subs = app.getSubscriptions()
    subs.each{ s ->
        logEvent("subscribeToEvents", "subscription set: device = ${s.device}, attribute = ${s.data}, handler = ${s.handler}", "debug")
    }
}

def contactHandler(evt){
    def eventValue = "${evt.value}"
    def devNetId = evt.device.deviceNetworkId
    def devLabel = evt.device.displayName
    logEvent("contactHandler", "Starting with event ${evt}", "debug")
    logEvent("contactHandler", "event value ${eventValue}", "debug")
    
    if (eventValue == "open") {
    	runIn(negateDelay, delayedContactOpen, [data: [contactId: "${devNetId}", contactLabel: "${devLabel}"]])
    } else if (eventValue == "closed") {
    	state.negateTriggers[devNetId] = eventValue
        setLastRunDetails("Activate [tentative]", "${devLabel}")
        setTempLastRunDetails("Activate [tentative]", "${devLabel}")
        
        activate(thermostat.deviceNetworkId)
    }
    
}

def delayedContactOpen(data){
	logEvent("delayedContactEvent", "Starting with contact sensor =  $data.contactId", "debug")
    state.negateTriggers[data.contactId] = "open"
    setLastRunDetails("Negate [tentative]", "${data.contactLabel}")
    setTempLastRunDetails("Negate [tentative]", "${data.contactLabel}")
    negate(thermostat.deviceNetworkId)
}

def temperatureHandler(evt) {
	
    logEvent("temperatureHandler", "Starting with event ${evt}", "debug")
    logEvent("temperatureHandler", "event value ${evt.value}", "debug")
    
    def override = false
    def sensorTemp = thermostat.currentTemperature
    def thermostatMode = thermostat.currentThermostatMode
    def thermostatFan = thermostat.currentThermostatFanMode
    setLastRunDetails(null, thermostat.displayName)
    setTempLastRunDetails(null, thermostat.displayName)
    def status = checkRequirements()
    
    def heatStartPoint = state.heatStartPoint
    def coolStartPoint = state.coolStartPoint
    def tId = thermostat.deviceNetworkId
    
    logEvent("temperatureHandler", "thermostat current mode = $thermostatMode", "debug")
    logEvent("temperatureHandler", "thermostat fan current mode = $thermostatFan", "debug")
    logEvent("temperatureHandler", "thermostat temperature is: $sensorTemp", "debug")
    logEvent("temperatureHandler", "thermostat heatStartPoint = ${heatStartPoint}", "debug")
    logEvent("temperatureHandler", "thermostat heatSetPoint = ${heatSetPoint}", "debug")
    logEvent("temperatureHandler", "thermostat coolStartPoint = ${coolStartPoint}", "debug")
    logEvent("temperatureHandler", "thermostat coolSetPoint = ${coolSetPoint}", "debug")
    logEvent("temperatureHandler", "checkrequirements status = ${status}", "debug")
    
    if (status) {
            
        if (thermostatMode == "heat") {
            if (sensorTemp < heatStartPoint) {
                activate(tId)
            } else if (sensorTemp >= heatSetPoint) {
                negate(tId)
            }

        } else if (thermostatMode == "cool") {
            if (sensorTemp > acStartPoint) {
                negate(tId)
            } else if (sensorTemp <= acSetPoint) {
                activate(tId)
            }

        }
    } else {
        negate(tId)
    }
    
}

def externalTempHanler(){

}

def negate(id, mode='auto'){
	def thermostatMode = thermostat.currentThermostatMode
	if (thermostatMode == "heat" && (mainSensor == "heat" || mode == "heat")) {
    	logEvent("negate", "override status = true", "debug")
		state.triggers[id] = "negate"
	}else if (thermostatMode == "cool" && (mainSensor == "cool" || mode == "cool")){
    	logEvent("negate", "override status = true", "debug")
    	state.triggers[id] = "negate"
    }else{
    	logEvent("negate", "override status = false", "debug")
    	state.triggers[id] = "neutral"
    }
    processTriggers()
}

def activate(id, mode='auto'){
	def thermostatMode = thermostat.currentThermostatMode
    if (thermostatMode == "heat" && (mainSensor == "heat" || mode == "heat")) {
    	logEvent("activate", "override status = true", "debug")
        state.triggers[id] = "activate"
    } else if (thermostatMode == "cool" && (mainSensor == "cool" || mode == "cool")){
    	logEvent("activate", "override status = true", "debug")
        state.triggers[id] = "activate"
    } else {
    	logEvent("activate", "override status = false", "debug")
    	state.triggers[id] = "activate"
    }
    processTriggers()
}

def activatedByRequirement(){
	activate(thermostat.deviceNetworkId)
}

def deactivatedByRequirement(){
	negate(thermostat.deviceNetworkId)
}

def processTriggers(){
    def triggers = state.triggers
    logEvent("processTriggers", "Processing triggers = ${triggers}", "debug")
    def activate = null
    def negate = null
    if (triggers?.find { key, value -> value == 'activate' }) {
        start()
    } else if (triggers?.find { key, value -> value == 'negate' }) {
        stop()
    }
}

def start(){
    logEvent("start", "Processing start", "debug")
    def thermostatMode = thermostat.currentThermostatMode
    def operatingState = thermostat.currentThermostatOperatingState
    logEvent("start", "operating state = ${operatingState}, thermostat mode = ${thermostatMode}", "debug")
    setLastRunDetails("Already running", null)
    setTempLastRunDetails("Already running", null)
    if (thermostatMode == "heat" && operatingState == "idle"){
        startHeating()
    } else if (thermostatMode == "cool" && operatingState == "idle") {
        startCooling()
    } else {
    	logEvent("start", "the thermostat is already running", "info")
        setLastRunDetails("Already running", null)
        setTempLastRunDetails("Already running", null)
    }
}

def stop(){
    logEvent("stop", "Processing stop", "debug")
    def thermostatMode = thermostat.currentThermostatMode
    def operatingState = thermostat.currentThermostatOperatingState
    logEvent("stop", "operating state = ${operatingState}", "debug")
    if (thermostatMode == "heat" && operatingState != "idle"){
        stopHeating()
    } else if (thermostatMode == "cool" && operatingState != "idle") {
        stopCooling()
    } else {
    	logEvent("stop", "the thermostat is already set to idle - no need to stop anything", "info")
        setLastRunDetails("Activate idle", null)
        setTempLastRunDetails("Activate idle", null)
    }
}


def startHeating(){
    setLastRunDetails("Activate heat", null)
    setTempLastRunDetails("Activate heat", null)
    logEvent("startHeating", "Starting heating because of ${state.trigger}", "info")
}

def startCooling(){
	setLastRunDetails("Activate cool", null)
    setTempLastRunDetails("Activate cool", null)
    logEvent("startCooling", "Starting cooling because of ${state.trigger}", "info")
}

def stopHeating(){
	setLastRunDetails("idle", null)
    setTempLastRunDetails("idle", null)
    logEvent("stopHeating", "Stop heating because of ${state.trigger}", "info")
}

def stopCooling(){
	setLastRunDetails("idle", null)
    setTempLastRunDetails("idle", null)
    logEvent("stopCooling", "Stop cooling because of ${state.trigger}", "info")
}

def checkNegate(){
    logEvent("checkNegate", "Starting", "debug")
    def nTrigs = state.negateTriggers
    if (nTrigs) {
        nTrigs.each{ n ->
            if (n == true){
                setLastRunDetails("Negate [tentative]", dev.displayName)
                setTempLastRunDetails("Negate [tentative]", dev.displayName)
                return true
            }
        }
    }
    
    return false
}

def checkRequired(){
    logEvent("checkRequired", "Starting", "debug")
    if (settings?.requireAll == true) {
    	def returnVal = true
    	logEvent("checkRequired", "checking required devices with requireAll - no inactivve allowed", "debug")
        def triggerChildren = findAllChildAppsByName("Thermostat Supreme Require Trigger")
        if (triggerChildren) {
            triggerChildren.each{ child ->
            	logEvent("checkRequired", "child = ${child}", "debug")
                if (child.getStateValue('triggerStatus') == 'inactive') {
                	logEvent("checkRequired", "child is inactive", "debug")
                    setLastRunDetails("Negate [tentative]", child.getLabel())
                    setTempLastRunDetails("Negate [tentative]", child.getLabel())
                    returnVal = false
                }
            }
            return returnVal
        }
        return true
    } else {
    	def returnVal = false
    	logEvent("checkRequired", "checking required devices for a single active one", "debug")
        def triggerChildren = findAllChildAppsByName("Thermostat Supreme Require Trigger")
        if (triggerChildren) {
            triggerChildren.each{ child ->
            	logEvent("checkRequired", "child = ${child}", "debug")
                if (child.getStateValue('triggerStatus') == 'active') {
                	logEvent("checkRequired", "require child ${child.label} is active", "debug")
                    returnVal = true
                }
            }
            if (!returnVal) {
                setLastRunDetails("Negate [tentative]", 'Require Sensor(s)')
                setTempLastRunDetails("Negate [tentative]", 'Require Sensor(s)')
            }
            return returnVal
        }
        return true
    }
}

def checkRequirements(){
    logEvent("checkRequirements", "Starting", "debug")
    if (checkNegate() == true){
        return false
    }
    
    def r = checkRequired()
    logEvent("checkRequirements", "reuired = ${r}", "debug")
    if (r){
        return true
    } else {
    	return false
    }
}

def logEvent(methodName, content, type) {

	if (logsEnabled() == true || logsEnabled() == null){

        if (type == "debug"){
            log.debug "TSupreme[${methodName}] > ${content}"
        } else if (type == "info"){
            log.info "TSupreme[${methodName}] > ${content}"
        } else if (type == "error"){
            log.error "TSupreme[${methodName}] > ${content}"
        }
    }
}

def setLastRunDetails(action = null, trigger=null){
    if (trigger != null) {
        state.lastRunTrigger = trigger
    }
    if (action != null) {
        state.lastRunAction = action
    }
    
    state.lastRunTime = new Date().format("MMMMM dd, h:mm aa", location.timeZone)
    
}

def setTempLastRunDetails(action = null, trigger=null){
    if (trigger != null) {
        state.tempLastRunTrigger = trigger
    }
    if (action != null) {
        state.tempLastRunAction = action
    }
    state.tempLastRunTime = new Date().format("MMMMM dd, h:mm aa", location.timeZone)
}

private def configuredTemperatureSensors(){
	def childApps = findAllChildAppsByName("Thermostat Supreme Temp Sensor")
    def paragraph = "None Configured"
    def counter = 0
    if (childApps) {
    	paragraph = ""
        childApps.each{ child ->
        	counter = counter + 1
            paragraph = paragraph + "    ${counter}. ${child.getLabel()}\n"
        }
    }
    return paragraph
}

private def lastRunAction() {
    return state.lastRunAction + " | " + state.lastRunTrigger
}
private def lastRunTime() {
    return state.lastRunTime
}
private def tempLastRunTime() {
    return state.tempLastRunTime
}
private def tempLastRunAction() {
    return state.tempLastRunAction + " | " + state.tempLastRunTrigger
}
private def configuredThermostat(){
	if (thermostat) {
		return "${thermostat.displayName}"
    } else {
    	return "Not Configured"
    }
}
private def textAppName() {
	return "Thermostat Supreme"
}
private def textVersion() {
    def version = "Main App Version:\n ${version()}"
    def childCount = childApps.size()
    return "${version}\n\n # Child Apps:\n ${childCount}"
}
private def textCopyright() {
    return "Copyright © 2018 Curlytaileddbuffalo"
}

private def version(){
	return "0.9beta"
}
private def textLicense() {
	def text =
		"Licensed under the Apache License, Version 2.0 (the \"License\"); "+
		"you may not use this file except in compliance with the License. "+
		"You may obtain a copy of the License at"+
		"\n\n"+
		"    http://www.apache.org/licenses/LICENSE-2.0"+
		"\n\n"+
		"Unless required by applicable law or agreed to in writing, software "+
		"distributed under the License is distributed on an \"AS IS\" BASIS, "+
		"WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. "+
		"See the License for the specific language governing permissions and "+
		"limitations under the License."
    return text
}
private def textHelp() {
	def text =
        "Thermostat Supreme is a smartapp that can help you utilize external sensors for triggering, or "+
        "stopping, your heating/cooling system.\nThese triggers can be based on external temperature sensors, motion sensors, presence sensors, and contact sensors.\n\n "+
        "Initial configuration will consists of choosing a thermostat to be controlled. The temperature value will be used as a sensor and heating/cooling based on your configured setpoints. \n"+
        "Next you may configure presence or motion sensors that must be actived for the temperature to trigger the thermostat mode. You may also configure contact sensors that must be closed.\n\n "+
        "If you plan on using external temperature sensors the configuration for them is similar. \nThe thermostat and each exernal temp sensor can have its own setpoints and presence/motion/contact requirements.\n\n "+
        "[Thermostat] - the main thermostat being controlled/also acts as a temp sensor.\n\n "+
        "[Exernal Temp Sensors] - external temp sensors that can be used to trigger the thermostat.\n\n " +
        "[Heat Variance] - the degrees below your setpoint that will start heating.\n\n "+
        "[Cool Variance] - the degrees above your setpoint that will start cooling.\n\n "+
        "[Negate Triggers] - window/door sensors that, when opened, will stop this sensor\'s setpoints from being considered.\n\n "+
        "[Required Triggers] - motion sensors or presence sensors that must be active for this sensors setpoints to be considered.\n\n "
    return text
}
private def textContributors() {
    return "Contributors:\nCurlytailedbuffalo"
}

private def textChildTempAppName() {
    return "Thermostat Supreme Temp Sensor"
}
private def textChildTempVersion() {
    def version = "Child App Version: ${version()}"
    return "${version}\n"
}
private def textChildTempHelp() {
	def text =
        "This is a child of the Thermostat Supreme smartapp that allows you to utilize external temperature sensors for triggering, or "+
        "stopping your heating/cooling system.\n It can be configured the same as the main smartapp.\n\n "+
        "[Main Sensor] - you may have many sensors activated at the same time, the main sensor takes priority when the thermostat is set to the desired mode.\n "+
        "[Heat Variance] - the degrees below your setpoint that will start heating.\n\n "+
        "[Cool Variance] - the degrees above your setpoint that will start cooling.\n\n "+
        "[Negate Triggers] - window/door sensors that, when opened, will stop this sensor\'s setpoints from being considered.\n\n "+
        "[Required Triggers] - motion sensors or presence sensors that must be active for this sensors setpoints to be considered.\n\n "
    return text
}
