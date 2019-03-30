/**
 *  GoQual Child Switch (v.0.0.1)
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
	definition(name: "GoQual Child Switch", namespace: "fison67", author: "fison67") {
		capability "Switch"
		capability "Actuator"
		capability "Sensor"
		capability "Refresh"
		capability "Configuration"
		capability "Health Check"
	}

}

def installed() {
	// This is set to a default value, but it is the responsibility of the parent to set it to a more appropriate number
	sendEvent(name: "checkInterval", value: 30 * 60, displayed: false, data: [protocol: "zigbee"])
}

void on() {
   log.debug("on")
	parent.childOn(device.deviceNetworkId)
}

void off() {
   log.debug("off")
	parent.childOff(device.deviceNetworkId)
}

def ping() {
   log.debug("ping")
	parent.childRefresh(device.deviceNetworkId)
}

def refresh() {
   log.debug("refresh")
	parent.childRefresh(device.deviceNetworkId)
}

def uninstalled() {
	parent.delete()
}
