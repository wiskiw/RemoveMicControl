//
//  VolumeWatcherService.swift
//  BluetoothMicControl
//
//  Created by Andrei Yablonski on 01.12.22.
//

import AudioToolbox

class MicrophoneService {
    
    init (){
        self.printDeviceCount()
    }
    
    private func printDeviceCount()  {
        var devicesPropertyAddress = AudioObjectPropertyAddress(mSelector: kAudioHardwarePropertyDevices, mScope: kAudioObjectPropertyScopeGlobal,
                                                                mElement: kAudioObjectPropertyElementMain)
        var propertySize = UInt32(0)

        AudioObjectGetPropertyDataSize(AudioObjectID(kAudioObjectSystemObject), &devicesPropertyAddress, 0, nil, &propertySize)

        let numberOfDevices = Int(propertySize) / MemoryLayout<AudioDeviceID>.size
        print("numberOfDevices: \(numberOfDevices)")
    }
}


