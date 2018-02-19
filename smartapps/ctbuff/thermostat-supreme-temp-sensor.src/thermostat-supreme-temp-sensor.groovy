/**
*  Thermostat Supreme Temp Sensor
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
        name: "Thermostat Supreme Temp Sensor",
        namespace: "ctbuff",
        author: "Curlytailedbuffalo",
        description: "Thermostat with all the features",
        category: "Green Living",
        iconUrl: "https://s3.amazonaws.com/smartapp-icons/GreenLiving/Cat-GreenLiving.png",
        iconX2Url: "https://s3.amazonaws.com/smartapp-icons/GreenLiving/Cat-GreenLiving@2x.png",
        iconX3Url: "https://s3.amazonaws.com/smartapp-icons/GreenLiving/Cat-GreenLiving@3x.png")

preferences {
    page(name: "mainPage", title: "Thermostat Supreme Temp Sensor", content: "mainPage", install: true, uninstall:false)
    page(name: "settingsPage", title: "Sensor Settings", content: "settingsPage", install: true, uninstall:false)
    page(name: "setPointsPage", title: "Setpoint Temperatures", content: "setPointsPage", install: false, uninstall:false)
    page(name: "requireTriggersPage", title: "Require Trigger", content: "requireTriggersPage", install: true, uninstall:false)
}

def logsEnabled() {
	state.logging = parent.logsEnabled()
	return parent.logsEnabled()
}

def mainPage() {
    return dynamicPage(name: "mainPage", title: "Thermostat Supreme Temp Sensor") {
        section("Choose temperature sensor") {
            label(name: "label", title: "Name This Sensor", required: true, submitOnChange: true)
            input "temperatureSensor", "capability.temperatureMeasurement", required: true, multiple: false
        }
        section("Sensor Settings") {
            href ("settingsPage", title: "Sensor Settings", description: "Tap to review main thermostat settings.")
        }
        section("About") {
            paragraph "Last Run: ${lastRunTime()}"
            paragraph "Last Action: ${lastRunAction()}"
        }
    }
}

def settingsPage(){
    return dynamicPage(name: "settingsPage", title: "Sensor Settings") {
        section("") {
            input "mainSensor", "enum", title: "Main Sensor:", options: [none: 'None', heat: 'Heat', cool: 'Cool'], description: "",  required: true
        }
        section("Setpoint Temperatures") {
           if(settings?.heatSetPoint && settings?.heatVariance && settings?.coolSetPoint && settings?.coolVariance){
           		href ("setPointsPage", title: "Configure Setpoints")
            }else{
            	href ("setPointsPage", title: "Configure Setpoints", required:true)
            }
        }
        section("Negate Triggers") {
            input name: "negate", type: "capability.contactSensor", required: false, multiple: true, description: "These sensors will negate this temperature sensor if they are opened."
        }
        section("Require Trigger(s)") {
			href ("requireTriggersPage", title: "Require Triggers")
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

def installed() {
    logEvent("installed", "with settings: ${settings}", "info")
    
    state.triggers = [:]
    state.triggers[temperatureSensor.deviceNetworkId] = "neutral"
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


def subscribeToEvents() {
    logEvent("subscribeToEvents", "Starting", "info")
    
    subscribe(temperatureSensor, "temperature", temperatureHandler)
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
        activate(temperatureSensor.deviceNetworkId)
    }
    
}

def delayedContactOpen(data){
	logEvent("delayedContactEvent", "Starting with contact sensor =  $data.contactId", "debug")
    state.negateTriggers[data.contactId] = "open"
    setLastRunDetails("Negate [tentative]", "${data.contactLabel}")
    negate(temperatureSensor.deviceNetworkId)
}

def presenceHandler(){}

def temperatureHandler(evt) {
	setLastRunDetails()
    logEvent("temperatureHandler", "Starting with event ${evt}", "debug")
    logEvent("temperatureHandler", "event value ${evt.value}", "debug")
    
    def override = false
    def sensorTemp = temperatureSensor.currentTemperature
    def thermostatMode = parent.thermostat.currentThermostatMode
    def thermostatFan = parent.thermostat.currentThermostatFanMode
    setLastRunDetails(null, temperatureSensor.displayName)
    def status = checkRequirements()
    def heatStartPoint = state.heatStartPoint
    def coolStartPoint = state.coolStartPoint
    def tId = temperatureSensor.deviceNetworkId
    
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
    }
    
}

def negate(id){
    parent.negate(id, mainSensor)
}

def activate(id){
    parent.activate(id, mainSensor)
}

def activatedByRequirement(){
	parent.activate(temperatureSensor.deviceNetworkId, mainSensor)
}

def deactivatedByRequirement(){
	parent.negate(temperatureSensor.deviceNetworkId, mainSensor)
}

def checkNegate(){
    logEvent("checkNegate", "Starting", "debug")
    def nTrigs = state.negateTriggers
    if (nTrigs) {
        nTrigs.each{ n ->
            if (n == true){
            	setLastRunDetails("Negate [tentative]", dev.displayName)
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
            log.debug "TSupremeTempSensor[${methodName}] > ${content}"
        } else if (type == "info"){
            log.info "TSupremeTempSensor[${methodName}] > ${content}"
        } else if (type == "error"){
            log.error "TSupremeTempSensor[${methodName}] > ${content}"
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
    
    parent.setLastRunDetails(action, trigger)
}

private def lastRunAction() {
    return state.lastRunAction + " | " + state.lastRunTrigger
}
private def lastRunTime() {
    return state.lastRunTime
}
private def lastRunTrigger() {
    return state.lastRunTrigger
}
