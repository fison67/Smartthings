/**
 *  GoQual Switch (v.0.0.1)
 *
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

metadata {
    definition(name: "GoQual Switch", namespace: "fison67", author: "fison67") {
        capability "Actuator"
        capability "Configuration"
        capability "Refresh"
        capability "Health Check"
        capability "Switch"

        command "childOn", ["string"]
        command "childOff", ["string"]
		command "childRefresh"

        fingerprint profileId: "0104", deviceId: "0100", endpoint: "01", inClusters: "0006, 0000, 0003", outClusters: "0019", manufacturer: "", model: "", deviceJoinName: "GQ Switch"
        fingerprint profileId: "0104", deviceId: "0100", endpoint: "02", inClusters: "0006, 0000, 0003", outClusters: "0019", manufacturer: "", model: "", deviceJoinName: "GQ Switch"
        fingerprint profileId: "0104", deviceId: "0100", endpoint: "03", inClusters: "0006, 0000, 0003", outClusters: "0019", manufacturer: "", model: "", deviceJoinName: "GQ Switch"
        
        fingerprint profileId: "0104", deviceId: "0100", endpoint: "01", inClusters: "0006, 0000, 0003", manufacturer: "", model: "", deviceJoinName: "GQ Switch"
        fingerprint profileId: "0104", deviceId: "0100", endpoint: "02", inClusters: "0006, 0000, 0003", manufacturer: "", model: "", deviceJoinName: "GQ Switch"
        fingerprint profileId: "0104", deviceId: "0100", endpoint: "03", inClusters: "0006, 0000, 0003", manufacturer: "", model: "", deviceJoinName: "GQ Switch"
    }
	preferences {
	}
   
}

def installed() {
    updateDataValue("onOff", "catchall")
}

def updated() {
}

def parse(String description) {
    log.debug "description is $description"
    Map map = zigbee.getEvent(description)
    if (map) {
        if (description ?.startsWith('on/off')) {
            log.debug "receive on/off message without endpoint id"
            sendHubCommand(refresh().collect { new hubitat.device.HubAction(it) }, 0)
        } else {
            Map descMap = zigbee.parseDescriptionAsMap(description)
            log.debug "$descMap"
			try{
				def ep = null
				if(descMap.endpoint != null){
					ep = descMap.endpoint
				}else if(descMap.destinationEndpoint != null){
					ep = descMap.destinationEndpoint
				}
				if(ep != null){
					def childDevice = childDevices.find {
						it.deviceNetworkId == "$device.deviceNetworkId:${ep}"
					}
					if (childDevice) {
						childDevice.sendEvent(map)
					}else{
						def dni = "${device.deviceNetworkId}:${ep}"
						def target = addChildDevice("GoQual Child Switch", dni, [completedSetup: true, label: "${device.displayName} - ${ep}", isComponent : false])	
					}
				}
			
			}catch(err){
				log.error "Error >> ${err}"
			}
            
        }
    }
}

private getChildEndpoint(String dni) {
    dni.split(":")[1]
}

def on() {
    log.debug("on")
    zigbee.on()
}

def off() {
    log.debug("off")
    zigbee.off()
}

def childOn(String dni) {
	def childDNI = getChildEndpoint(dni) as int
	log.debug(" child on ${dni} >> ${childDNI}")
	def cmd = [
		"he cmd 0x${device.deviceNetworkId} 0x${childDNI} 0x0006 1 {}"
	]
	return cmd

}

def childOff(String dni) {
	def childDNI = getChildEndpoint(dni) as int
	log.debug(" child off ${dni} >> ${childDNI}")
	def cmd = [
		"he cmd 0x${device.deviceNetworkId} 0x${childDNI} 0x0006 0 {}"
    ]
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
    return refresh()
}

def refresh() {
    return zigbee.readAttribute(0x0006, 0x0000, [destEndpoint: 0xFF])
}

def childRefresh(String dni) {
    return zigbee.readAttribute(0x0006, 0x0000, [destEndpoint: getChildEndpoint(dni)])
}

def poll() {
    refresh()
}

def healthPoll() {
    log.debug "healthPoll()"
    def cmds = refresh()
    cmds.each { sendHubCommand(new hubitat.device.HubAction(it)) }
}

def configureHealthCheck() {
    Integer hcIntervalMinutes = 12
    if (!state.hasConfiguredHealthCheck) {
        log.debug "Configuring Health Check, Reporting"
        unschedule("healthPoll")
        runEvery5Minutes("healthPoll")
        def healthEvent = [name: "checkInterval", value: hcIntervalMinutes * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID]]
        // Device-Watch allows 2 check-in misses from device
        sendEvent(healthEvent)
        childDevices.each {
            it.sendEvent(healthEvent)
        }
        state.hasConfiguredHealthCheck = true
    }
}

def configure() {
    log.debug "configure()"
    configureHealthCheck()
    //the switch will send out device anounce message at ervery 2 mins as heart beat,setting 0x0099 to 1 will disable it.
    return zigbee.writeAttribute(0x0000, 0x0099, 0x20, 0x01, [mfgCode: 0x0000])
}
