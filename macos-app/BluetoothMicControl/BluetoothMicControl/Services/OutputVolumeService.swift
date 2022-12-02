//
//  OutputVolumeService.swift
//  BluetoothMicControl
//
//  Created by Andrei Yablonski on 30.11.22.
//

import Foundation
import AudioToolbox

class OutputVolumeService {
    
    private lazy var deviceId : AudioDeviceID =  self.getDefaultOutputDeviceID()
    private lazy var onVolumeChangedCallback : AudioObjectPropertyListenerBlock = { _, _ in self.handleVolumeChangedEvent()}
    private lazy var volumePropertyAddress = getVolumePropertyAddress()
    private lazy var lastVolume : Float = self.getVolume(defaultOutputDeviceID: self.deviceId)

    var onVolumeChangedListener: ((Float, Float)->())! = nil
    
    init() {
        print("VolumeWatcherService init")
        print("Init volume \(self.lastVolume)")
        
        AudioObjectAddPropertyListenerBlock(
            self.deviceId,
            &self.volumePropertyAddress,
            DispatchQueue.main,
            self.onVolumeChangedCallback
        )
    }
    
    private func getVolumePropertyAddress() -> AudioObjectPropertyAddress {
        return AudioObjectPropertyAddress(
            mSelector: kAudioHardwareServiceDeviceProperty_VirtualMainVolume,
            mScope: kAudioDevicePropertyScopeOutput,
            mElement: kAudioObjectPropertyElementMain)
    }
    
    private func getDefaultOutputDeviceID() -> AudioDeviceID {
        var defaultOutputDeviceID = AudioDeviceID(0)
        var defaultOutputDeviceIDSize = UInt32(MemoryLayout.size(ofValue: defaultOutputDeviceID))

        var getDefaultOutputDevicePropertyAddress = AudioObjectPropertyAddress(
            mSelector: kAudioHardwarePropertyDefaultOutputDevice,
            mScope: kAudioObjectPropertyScopeGlobal,
            mElement: AudioObjectPropertyElement(kAudioObjectPropertyElementMain))

        AudioObjectGetPropertyData(
            AudioObjectID(kAudioObjectSystemObject),
            &getDefaultOutputDevicePropertyAddress,
            0,
            nil,
            &defaultOutputDeviceIDSize,
            &defaultOutputDeviceID)
        
        return defaultOutputDeviceID
    }
    
    private func handleVolumeChangedEvent() {
        let newVolume = self.getVolume(defaultOutputDeviceID: deviceId)
        
        if (self.lastVolume != newVolume){
            let old = lastVolume
            self.lastVolume = newVolume
            
            if self.onVolumeChangedListener != nil {
                self.onVolumeChangedListener(old, newVolume)
            }
          
        }

    }

    func getVolume(defaultOutputDeviceID : AudioDeviceID) -> Float{
        var volume = Float32(0.0)
        var volumeSize = UInt32(MemoryLayout.size(ofValue: volume))
        
        AudioObjectGetPropertyData(
            self.deviceId,
            &self.volumePropertyAddress,
            0,
            nil,
            &volumeSize,
            &volume)

        return volume
    }
    
    func setVolume(volume : Float) {
        var volume32 = Float32(volume) // 0.0 ... 1.0
        let volumeSize = UInt32(MemoryLayout.size(ofValue: volume32))

        AudioObjectSetPropertyData(
            self.deviceId,
            &self.volumePropertyAddress,
            0,
            nil,
            volumeSize,
            &volume32)
    }
    
    deinit {
        AudioObjectRemovePropertyListenerBlock(
            self.deviceId,
            &self.volumePropertyAddress,
            DispatchQueue.main,
            self.onVolumeChangedCallback
        )
    }
}
