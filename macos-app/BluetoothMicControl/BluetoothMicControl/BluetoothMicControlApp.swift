//
//  BluetoothMicControlApp.swift
//  BluetoothMicControl
//
//  Created by Andrei Yablonski on 30.11.22.
//

import SwiftUI
import AudioToolbox
import AVKit

@main
struct BluetoothMicControlApp: App {

    var body: some Scene {
            WindowGroup {
                MicControlView(vm:MicControlViewModel())
            }
        }

    init() {
        switch AVCaptureDevice.authorizationStatus(for: .audio) {
            case .notDetermined:
                AVCaptureDevice.requestAccess(for: .audio) { granted in
                    if !granted {
                        NSLog("Can't get access to the mic.")
                        exit(1)
                    }
                }
            
            case .denied:
                fallthrough
            case .restricted:
                NSLog("Can't get access to the mic.")
                exit(1)
            default:
                print("Already has permission");
        }
    }
    
}
