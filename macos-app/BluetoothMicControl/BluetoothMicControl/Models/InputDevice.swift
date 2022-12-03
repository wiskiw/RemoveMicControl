//
//  InputDevice.swift
//  BluetoothMicControl
//
//  Created by Andrei Yablonski on 3.12.22.
//

import Foundation
import SimplyCoreAudio

class InputDevice {
    
    private let CHANNEL : UInt32 = 0
    
    private let audioDevice : AudioDevice
    private let originalVolume : Float32!
    
    init (audioDevice : AudioDevice) {
        self.audioDevice = audioDevice
        
        // TODO check channels
        self.originalVolume = audioDevice.volume(channel: 0, scope: .input)
        
        
//        NSLog("Init InputDevice '\(audioDevice.name)', volume:\(self.originalVolume)")
    }
    
    func mute() {
        let isMuted = self.audioDevice.setMute(true, channel: self.CHANNEL, scope: .input)
        
        // to move volume bar in System Preferences
        let isVolumeSet = self.audioDevice.setVolume(0.0, channel: self.CHANNEL, scope: .input)
        
        if (isMuted || isVolumeSet){
            NSLog("Mic '\(self.audioDevice.name)' muted")
        } else {
            NSLog("Failed to mute '\(self.audioDevice.name)'")
        }
    }
    
    func activate() {
        // TODO check channels
        let isActivated = self.audioDevice.setMute(false, channel: self.CHANNEL, scope: .input)
        
        // to move volume bar in System Preferences
        let isVolumeSet = self.audioDevice.setVolume(self.originalVolume, channel: self.CHANNEL, scope: .input)
        
        if (isActivated || isVolumeSet){
            NSLog("Mic '\(self.audioDevice.name)' activated")
        } else {
            NSLog("Failed to activate '\(self.audioDevice.name)'")
        }
        
        // to move volume bar in System Preferences
        self.audioDevice.setVolume(0, channel: 0, scope: .input)
    }
}
