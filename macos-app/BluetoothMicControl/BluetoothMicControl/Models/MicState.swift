//
//  ChangeVolumeDirection.swift
//  BluetoothMicControl
//
//  Created by Andrei Yablonski on 4.12.22.
//

import Foundation

enum MicState {
    case muted
    case activated
    
    static func create(isMuted:Bool) -> MicState {
        if (isMuted){
            return .muted
        } else {
            return .activated
        }
    }
}
