//
//  MicState+UiExtensions.swift
//  BluetoothMicControl
//
//  Created by Andrei Yablonski on 4.12.22.
//

import Foundation
import SwiftUI


extension MicState {

    private static let STATUS_BAR_ICON_SIZE = 19
    
    func getMicStatusBarIcon() -> NSImage{
        switch (self){
        case .muted:
            let icon = NSImage(named: "ic_mic_off")!
            icon.size = NSSize(width: MicState.STATUS_BAR_ICON_SIZE, height: MicState.STATUS_BAR_ICON_SIZE)
            icon.isTemplate = true
            return icon
        
        case .activated:
            let icon = NSImage(named: "ic_mic_on")!
            icon.size = NSSize(width: MicState.STATUS_BAR_ICON_SIZE, height: MicState.STATUS_BAR_ICON_SIZE)
            icon.isTemplate = true
            return icon
        }
    }
}
