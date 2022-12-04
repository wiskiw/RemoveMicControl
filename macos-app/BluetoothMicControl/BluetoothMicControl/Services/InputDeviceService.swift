//
//  InputDeviceService.swift
//  BluetoothMicControl
//
//  Created by Andrei Yablonski on 01.12.22.
//

import Foundation
import SimplyCoreAudio

class InputDeviceService {

    private var deviceListChangedObserver : NSObjectProtocol? = nil
    
    private var inputDevices : [InputDevice]
    private var isAllMuted = false
    
    var muteListener : ((Bool) -> ())?
    

    private static func getInputDevices(simplyCA : SimplyCoreAudio) -> [InputDevice]{
        return simplyCA.allInputDevices
            .compactMap{$0} // filter not nil
            .map{ device in
                NSLog("Found input device '\(device.name)'")
                return InputDevice(audioDevice: device)
            }
    }

    
    init (simplyCA : SimplyCoreAudio){
        self.inputDevices = InputDeviceService.getInputDevices(simplyCA: simplyCA)
        
        self.deviceListChangedObserver = NotificationCenter.default.addObserver(forName: .deviceListChanged,
                                                                           object: nil,
                                                                           queue: .main) { (notification) in
            NSLog("Rescanning input devices...")
            self.inputDevices.removeAll()
            self.inputDevices = InputDeviceService.getInputDevices(simplyCA: simplyCA)
            
            if (self.isAllMuted) {
                self.mute()
            }
        }
    }
    
    func mute() {
        self.inputDevices.forEach{
            $0.mute()
        }
        self.isAllMuted = true
        self.muteListener?(true)
    }
    
    func activate() {
        self.inputDevices.forEach{
            $0.activate()
        }
        self.isAllMuted = false
        self.muteListener?(false)
    }
    
    func isMuted() -> Bool{
        return self.isAllMuted
    }
    
    deinit {
        if let observer = deviceListChangedObserver {
            NotificationCenter.default.removeObserver(observer)
        }
    }
}
