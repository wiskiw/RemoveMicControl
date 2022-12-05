//
//  InputDevice.swift
//  BluetoothMicControl
//
//  Created by Andrei Yablonski on 3.12.22.
//

import Foundation
import SimplyCoreAudio

class InputDevice : Hashable {
    
    private static let channel : UInt32 = 0
    
    let audioDevice : AudioDevice
    private let originalVolume : Float32
    
    init (audioDevice : AudioDevice) {
        self.audioDevice = audioDevice

        self.originalVolume = audioDevice.virtualMainVolume(scope: .input) ?? 1.0
        
        
//        NSLog("Init InputDevice '\(audioDevice.name)', volume:\(self.originalVolume)")
    }
    
    func mute() {
        let isMuted = setVolume(volume: 0.0)

        if (isMuted){
            NSLog("Mic '\(self.audioDevice.name)' muted")
        } else {
            NSLog("Failed to mute '\(self.audioDevice.name)'")
        }
    }
    
    func activate() {
        let isActivated = setVolume(volume: self.originalVolume)
        if (isActivated){
            NSLog("Mic '\(self.audioDevice.name)' activated")
        } else {
            NSLog("Failed to activate '\(self.audioDevice.name)'")
        }
    }
    
    private func setVolume(volume : Float32) -> Bool{
        let channelsCount = self.audioDevice.channels(scope: .input)
        let isMute = volume <= 0.0
        
        var isSetMuteChannelsSuccessfuly = true
        for channel in 0 ..< channelsCount {
            isSetMuteChannelsSuccessfuly = isSetMuteChannelsSuccessfuly && self.audioDevice.setMute(isMute, channel: channel, scope: .input)
        }

        // to move volume bar in System Preferences
        let isVolumeSetSuccessfuly = self.audioDevice.setVirtualMainVolume(volume, scope: .input)

        return isSetMuteChannelsSuccessfuly && isVolumeSetSuccessfuly
    }
    
    func hash(into hasher: inout Hasher) {
        hasher.combine(self.audioDevice.id)
    }
    
    static func == (lhs: InputDevice, rhs: InputDevice) -> Bool {
        return lhs.audioDevice.id == rhs.audioDevice.id
    }
}
