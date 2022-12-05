//
//  OutputDevice.swift
//  BluetoothMicControl
//
//  Created by Andrei Yablonski on 3.12.22.
//

import Foundation
import SimplyCoreAudio

class OutputDevice : Hashable {
    
    let audioDevice: AudioDevice
    
    private var lastVolume : Float32 = 0.0
    private var ignoreNext : Bool = false
    
    var volumeChangedListener: ((Float32, Float32)->(Bool))? = nil
    
    init (audioDevice: AudioDevice){
        self.audioDevice = audioDevice
        self.lastVolume = self.getVolume()

        //        NSLog("Init OutputDevice '\(audioDevice.name)', volume:\(self.lastVolume)")
    }
    
    func syncVolumeWithSystem() {
        if (self.ignoreNext){
            self.ignoreNext = false
            return
        }
        
        let old = self.lastVolume
        let new = self.getVolume()
        
        if (old == new || self.volumeChangedListener == nil) {
            return
        }
        
        let isRollbackRequired = self.volumeChangedListener?(old, new) ?? false
        
        if (isRollbackRequired) {
            self.ignoreNext = true
            self.setVolume(volume: old)
        } else {
            self.lastVolume = new
        }
    }
    
    func getVolume() -> Float32 {
        return self.audioDevice.virtualMainVolume(scope: .output) ?? 0.0
    }
    
    func setVolume(volume : Float32) {
        self.audioDevice.setVirtualMainVolume(volume, scope: .output)
    }
    
    func hash(into hasher: inout Hasher) {
        hasher.combine(self.audioDevice.id)
    }
    
    static func == (lhs: OutputDevice, rhs: OutputDevice) -> Bool {
        return lhs.audioDevice.id == rhs.audioDevice.id
    }
}
