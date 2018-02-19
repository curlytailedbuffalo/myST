/**
*  Thermostat Supreme Require Trigger
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
    name: "Thermostat Supreme Require Trigger",
    namespace: "ctbuff",
    author: "Curlytailedbuffalo",
    description: "This child SmartApp allows you to modify how a motion sensor triggers the thermostat.",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/GreenLiving/Cat-GreenLiving.png",
        iconX2Url: "https://s3.amazonaws.com/smartapp-icons/GreenLiving/Cat-GreenLiving@2x.png",
        iconX3Url: "https://s3.amazonaws.com/smartapp-icons/GreenLiving/Cat-GreenLiving@3x.png")

preferences {
	page(name: "mainPage", title: "Require Trigger", content: "mainPage", install: true, uninstall:false)
    page(name: "aboutPage", title: "About ${textAppName()}", content: "aboutPage", install: false, uninstall:true)

}

def logsEnabled() {
	atomicState.logging = parent.logsEnabled()
	return parent.logsEnabled()
}

def mainPage() {
	
    return dynamicPage(name: "mainPage", title: "Require Trigger") {
    	section("") {
            label(name: "label", title: "Name This Trigger", required: true, multiple: false, submitOnChange: true)
        }
        section("Choose sensors") {
        	if(settings?.presence){
            	input name: "motion", type: "capability.motionSensor", title: "Motion Sensor(s)", multiple: true, required: false, submitOnChange: true
            }else{
            	input name: "motion", type: "capability.motionSensor", title: "Motion Sensor(s)", multiple: true, required: true, submitOnChange: true
            }
            if(settings?.motion){
            	input name: "presence", type: "capability.presenceSensor", title: "Presence Sensor(s)", multiple:true, required: false, submitOnChange: true
            }else{
            	input name: "presence", type: "capability.presenceSensor", title: "Presence Sensor(s)", multiple:true, required: true, submitOnChange:true
            }
            
        }
        section("Settings") {
        	if(settings?.motion){
            	input name: "activationTimeMotion", type: "number", title: "Motion Active Time", required: true, description: "The amount of minutes after motion sensor \"motion\" to continue to consider the area occupied."
        	}
            if(settings?.presence){
            	input name: "activationTimePresence", type: "number", title: "Not Present Delay", range: "0..1440", required: true, description: "The amount of minutes after presence sensor \"not present\" to continue to consider the area occupied."
        	}
            
        }
        section("About") {
            paragraph "Last Run: ${lastRunTime()}"
            paragraph "Last Action: ${lastRunAction()}"
            paragraph "Trigger Status: ${getStateValue('triggerStatus')}"
            href ("aboutPage", title: "About", description: "SmartApp info and uninstall.")
        }
    }
}

def aboutPage() {
    return dynamicPage(name: "aboutPage", title: "About ${textAppName()}") {
        section {
            paragraph "${textCopyright()}\n\n${textContributors()}\n\n${textLicense()}\n"
        }
        section("Instructions") {
            paragraph textHelp()
        }
        section("Tap button below to remove all thermostat settings and children smartapps."){
        }
	}
}

def installed() {
    logEvent("installed", "with settings: ${settings}", "info")
    initialize()
}

def updated() {
    logEvent("updated", "with settings: ${settings}", "info")
    
    unsubscribe()
    initialize()
}

def initialize() {
    subscribeToEvents()
    checkInitialStatus()
}


def subscribeToEvents() {
    logEvent("subscribeToEvents", "Starting", "info")
    if(settings?.motion){
    	logEvent("subscribeToEvents", "subscribing to motion sensor(s)", "debug")
    	subscribe(motion, "motion", triggerHandler)
    }
    if(settings?.presence){
    	logEvent("subscribeToEvents", "subscribing to presence sensor(s)", "debug")
    	subscribe(presence, "presence", triggerHandler)
    }
    def subs = app.getSubscriptions()
    subs.each{ s ->
        logEvent("subscribeToEvents", "subscription set: device = ${s.device}, attribute = ${s.data}, handler = ${s.handler}", "debug")
    }
}

def checkInitialStatus(){
	atomicState.triggerStatus = 'inactive'
	if(settings?.motion){
    	settings?.motion.each{ dev ->
        	log.trace("${dev.currentState('motion').value}")
        	if(dev.currentState('motion').value == 'active'){
            	atomicState.triggerStatus = 'active'
            }
        }
    }
    if(settings?.presence){
    	settings?.presence.each{ dev ->
        	log.trace("${dev.currentState('presence').value}")
        	if(dev.currentState('presence').value == 'present'){
            	atomicState.triggerStatus = 'active'
            }
        }
    }
}

def triggerHandler(evt) {
    logEvent("triggerHandler", "Starting with event ${evt}", "debug")
    logEvent("triggerHandler", "event value = ${evt.value}", "debug")
    setLastRunTime()
    def activeTime = settings?.activationTime
    def delay = 0
    
    if (evt.value == "active" || evt.value == "inactive") {
    	delay = settings.activationTimeMotion
    } else {
    	delay = settings.activationTimePresence
    }
    setLastRunTrigger(evt.displayName)
    if (evt.value == "active" || evt.value == "not present") {
        setStateTriggerStatusActive()
        runIn(60*delay, setStateTriggerStatusInactive)
    } else if (evt.value == "present") {
        setStateTriggerStatusActive()
    }
    
}

def setStateTriggerStatusActive(){
	logEvent("setStateTriggerStatusActive", "Starting", "debug")
    setLastRunAction('Active')
    atomicState.triggerStatus = 'active'
    activateParent()
    
}

def activateParent(){
	if (parent.checkRequirements()){
    	logEvent("activateParent", "checkRequirements() = true", "debug")
    	parent.activatedByRequirement()
    } else {
    	logEvent("activateParent", "checkRequirements() = false", "debug")
    }
}

def deactivateParent(){
	if (!parent.checkRequirements()){
    	logEvent("deactivateParent", "checkRequirements() = false", "debug")
    	parent.deactivatedByRequirement()
    } else {
    	logEvent("deactivateParent", "checkRequirements() = true", "debug")
    }
}

def setStateTriggerStatusInactive(){
	logEvent("setStateTriggerStatusInactive", "Starting", "debug")
    setLastRunAction('Inactive')
    atomicState.triggerStatus = 'inactive'
    deactivateParent()
}

def getStateValue(key) {
    return atomicState[key]
}

def logEvent(methodName, content, type) {

	if (logsEnabled() == true || logsEnabled() == null){

        if (type == "debug"){
            log.debug "TSupremeRequire[${methodName}] > ${content}"
        } else if (type == "info"){
            log.info "TSupremeRequire[${methodName}] > ${content}"
        } else if (type == "error"){
            log.error "TSupremeRequire[${methodName}] > ${content}"
        }
    }
}

def setLastRunAction(action) {
    atomicState.lastRunAction = action
}

def setLastRunTrigger(trigger) {
    atomicState.lastRunTrigger = trigger
}

def setLastRunTime() {
    atomicState.lastRunTime = new Date().format("MMMMM dd, h:mm aa", location.timeZone)
}


private def lastRunAction() {
    return atomicState.lastRunAction + " | " + atomicState.lastRunTrigger
}
private def lastRunTime() {
    return atomicState.lastRunTime
}
private def lastRunTrigger() {
    return atomicState.lastRunTrigger
}

private def textAppName() {
	return "Require Trigger"
}
private def textVersion() {
    def version = "Child App Version: ${version()}"
    return "${version}"
}
private def textCopyright() {
    return "Copyright © 2018 Curlytaileddbuffalo"
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
        "This SmartApp is a child of the Thermostat Supreme Triggers child SmartApp that allows customizing motion sensor triggers.\n "+
        "Configuration includes:\n\n "+
        "[Active Time] - if a motion sensor is set as a requirement the thermostat will not use the corrresponding temp sensor to trigger heating/cooling if the sensor is not detecting motion. this allows the sensor to still trigger heating/cooling a specified number of minutes after motion has been detected, even if the sensor has already gone back to \"no motion\" ."
    return text
}
private def textContributors() {
    return "Contributors:\nCurlytailedbuffalo"
}
